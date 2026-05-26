package com.techpark.service;

import com.techpark.model.*;
import com.techpark.structures.*;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Servicio principal del parque Tech-Park UQ.
 * Actúa como repositorio central en memoria y orquesta la lógica de negocio.
 */
public class ParqueService {

    // ── Singleton ─────────────────────────────────────────────────────
    private static ParqueService instancia;

    public static ParqueService getInstance() {
        if (instancia == null) instancia = new ParqueService();
        return instancia;
    }

    // ── Repositorios en memoria ────────────────────────────────────────
    private final Map<String, Atraccion>      atracciones  = new LinkedHashMap<>();
    private final Map<String, Zona>           zonas        = new LinkedHashMap<>();
    private final Map<String, Visitante>      visitantes   = new LinkedHashMap<>();
    private final Map<String, Operador>       operadores   = new LinkedHashMap<>();
    private final Map<String, Administrador>  admins       = new LinkedHashMap<>();

    /** Cola de prioridad por atracción: id_atraccion → cola de visitantes */
    private final Map<String, ColaPrioridad<Visitante>> colasPorAtraccion = new HashMap<>();

    /** Notificaciones pendientes por visitante */
    private final Map<String, List<String>> notificacionesPorVisitante = new HashMap<>();

    /** Historial de cierres por clima. */
    private final List<CierreClimaticoRegistro> historialCierresClima = new ArrayList<>();

    /** ABB de atracciones indexado por nombre */
    private final ABB<String, Atraccion> arbolAtracciones = new ABB<>();

    /** Grafo del parque */
    private final Grafo grafo = new Grafo();

    /** Estado climático actual */
    private boolean alertaClimatica = false;
    private String  tipoAlerta      = "";

    // ── Capacidad total del parque ─────────────────────────────────────
    private int capacidadMaximaParque = 1000;
    private int visitantesActualesParque = 0;

    // ── Constructor privado ────────────────────────────────────────────
    private ParqueService() {}

    // ────────────────────────────────────────────────────────────────
    //  ATRACCIONES
    // ────────────────────────────────────────────────────────────────

    /** Registra una nueva atracción en el sistema. */
    public boolean agregarAtraccion(Atraccion a) {
        if (atracciones.containsKey(a.getId())) return false;
        atracciones.put(a.getId(), a);
        arbolAtracciones.insertar(a.getNombre().toLowerCase(), a);
        grafo.agregarNodo(a.getId());
        colasPorAtraccion.put(a.getId(), new ColaPrioridad<>());
        return true;
    }

    /** Retorna una atracción por ID. */
    public Atraccion getAtraccion(String id) { return atracciones.get(id); }

    /** Retorna todas las atracciones. */
    public Collection<Atraccion> getAtracciones() { return atracciones.values(); }

    /** Busca una atracción por nombre (ABB). */
    public Atraccion buscarAtraccionPorNombre(String nombre) {
        return arbolAtracciones.buscar(nombre.toLowerCase());
    }

    /** Actualiza el estado de una atracción. */
    public boolean actualizarEstadoAtraccion(String id, EstadoAtraccion estado, String motivo) {
        Atraccion a = atracciones.get(id);
        if (a == null) return false;
        a.setEstado(estado);
        a.setMotivoCierre(estado == EstadoAtraccion.ACTIVA ? null : motivo);
        return true;
    }

    /** Actualiza campos editables de una atracción (sin cambiar ID). */
    public boolean actualizarAtraccion(String id, Map<String, Object> cambios) {
        Atraccion a = atracciones.get(id);
        if (a == null || cambios == null) return false;

        String nombreAnterior = a.getNombre();

        if (cambios.containsKey("nombre")) {
            String nombre = (String) cambios.get("nombre");
            if (nombre != null && !nombre.isBlank()) a.setNombre(nombre);
        }
        if (cambios.containsKey("tipo")) {
            String tipo = (String) cambios.get("tipo");
            if (tipo != null) a.setTipo(TipoAtraccion.valueOf(tipo.toUpperCase()));
        }
        if (cambios.containsKey("capacidadMaximaPorCiclo")) {
            a.setCapacidadMaximaPorCiclo(((Number) cambios.get("capacidadMaximaPorCiclo")).intValue());
        }
        if (cambios.containsKey("alturaMinima")) {
            a.setAlturaMinima(((Number) cambios.get("alturaMinima")).doubleValue());
        }
        if (cambios.containsKey("edadMinima")) {
            a.setEdadMinima(((Number) cambios.get("edadMinima")).intValue());
        }
        if (cambios.containsKey("costoAdicional")) {
            a.setCostoAdicional(((Number) cambios.get("costoAdicional")).doubleValue());
        }
        if (cambios.containsKey("zonaId")) {
            String zonaId = (String) cambios.get("zonaId");
            if (zonaId != null && !zonaId.isBlank()) a.setZonaId(zonaId);
        }

        if (nombreAnterior != null && !nombreAnterior.equalsIgnoreCase(a.getNombre())) {
            arbolAtracciones.eliminar(nombreAnterior.toLowerCase());
            arbolAtracciones.insertar(a.getNombre().toLowerCase(), a);
        }

        return true;
    }

    /** Elimina una atracción. */
    public boolean eliminarAtraccion(String id) {
        Atraccion a = atracciones.remove(id);
        if (a == null) return false;
        for (Atraccion otra : atracciones.values()) {
            otra.quitarConexion(id);
        }
        arbolAtracciones.eliminar(a.getNombre().toLowerCase());
        grafo.eliminarNodo(id);
        colasPorAtraccion.remove(id);
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    //  ZONAS
    // ────────────────────────────────────────────────────────────────

    public boolean agregarZona(Zona z) {
        if (zonas.containsKey(z.getId())) return false;
        zonas.put(z.getId(), z);
        return true;
    }

    public Zona getZona(String id) { return zonas.get(id); }
    public Collection<Zona> getZonas() { return zonas.values(); }

    public boolean eliminarZona(String id) {
        return zonas.remove(id) != null;
    }

    // ────────────────────────────────────────────────────────────────
    //  VISITANTES
    // ────────────────────────────────────────────────────────────────

    /** Registra un visitante sin emitir ticket. */
    public boolean registrarVisitante(Visitante v) {
        if (visitantes.containsKey(v.getId())) return false;
        if (visitantesActualesParque >= capacidadMaximaParque) return false; // aforo completo
        v.setPuedeDentrar(false);
        v.setNumeroPersonasFamilia(1);
        visitantes.put(v.getId(), v);
        visitantesActualesParque++;
        return true;
    }

    /** Compatibilidad temporal: registra el visitante sin emitir ticket. */
    @Deprecated
    public boolean registrarVisitante(Visitante v, TipoTicket tipoTicket, double precioTicket) {
        return registrarVisitante(v);
    }

    public Visitante getVisitante(String id) { return visitantes.get(id); }
    public Collection<Visitante> getVisitantes() { return visitantes.values(); }

    /** Retira un visitante del parque. */
    public boolean retirarVisitante(String id) {
        Visitante eliminado = visitantes.remove(id);
        if (eliminado == null) return false;

        if (visitantesActualesParque > 0) visitantesActualesParque--;

        for (Map.Entry<String, ColaPrioridad<Visitante>> entry : colasPorAtraccion.entrySet()) {
            ColaPrioridad<Visitante> colaActual = entry.getValue();
            if (colaActual == null || colaActual.estaVacia()) continue;

            ColaPrioridad<Visitante> nuevaCola = new ColaPrioridad<>();
            for (Visitante visitante : colaActual.toList()) {
                if (visitante != null && !id.equals(visitante.getId())) {
                    nuevaCola.insertar(visitante, visitante.getPrioridad());
                }
            }
            entry.setValue(nuevaCola);
        }

        notificacionesPorVisitante.remove(id);
        return true;
    }

    /** Agrega un visitante cargado desde persistencia sin reemitir ticket. */
    public boolean agregarVisitanteCargado(Visitante v) {
        if (visitantes.containsKey(v.getId())) return false;
        v.sincronizarEstadoTicket();
        visitantes.put(v.getId(), v);
        visitantesActualesParque++;
        return true;
    }

    /** Recarga saldo virtual del visitante. */
    public void recargarSaldo(String visitanteId, double monto) {
        Visitante v = visitantes.get(visitanteId);
        if (v != null && monto > 0) {
            v.recargarSaldo(monto);
        }
    }

    /** Compra una entrada general. */
    public Ticket comprarTicketGeneral(String visitanteId) {
        Visitante visitante = visitantes.get(visitanteId);
        if (visitante == null) return null;
        if (obtenerTicketVigente(visitante) != null) return null;
        double precio = ConfiguracionTicket.precioGeneral;
        if (visitante.getSaldoVirtual() < precio) return null;
        if (!visitante.descontarSaldo(precio)) return null;

        Ticket ticket = new Ticket("T-" + visitante.getId(), TipoTicket.GENERAL, visitante.getId(), precio, 1, List.of(visitante));
        ticket.setPrecioCompra(precio);
        visitante.setTicketComprado(ticket);
        visitante.setPuedeDentrar(true);
        visitante.setNumeroPersonasFamilia(1);
        visitante.setLiderGrupo(visitante);
        visitante.setMiembrosGrupoFamiliar(new ArrayList<>());
        return ticket;
    }

    /** Compra una entrada FastPass. */
    public Ticket comprarTicketFastPass(String visitanteId) {
        Visitante visitante = visitantes.get(visitanteId);
        if (visitante == null) return null;
        if (obtenerTicketVigente(visitante) != null) return null;
        double precio = ConfiguracionTicket.precioFastPass;
        if (visitante.getSaldoVirtual() < precio) return null;
        if (!visitante.descontarSaldo(precio)) return null;

        Ticket ticket = new Ticket("T-" + visitante.getId(), TipoTicket.FAST_PASS, visitante.getId(), precio, 1, List.of(visitante));
        ticket.setPrecioCompra(precio);
        visitante.setTicketComprado(ticket);
        visitante.setPuedeDentrar(true);
        visitante.setNumeroPersonasFamilia(1);
        visitante.setLiderGrupo(visitante);
        visitante.setMiembrosGrupoFamiliar(new ArrayList<>());
        return ticket;
    }

    /** Compra una entrada familiar para un grupo de 2 a 5 personas. */
    public Ticket comprarTicketFamiliar(String visitanteId, int numeroPersonas, List<String> idsOtrosMiembros) {
        Visitante lider = visitantes.get(visitanteId);
        if (lider == null) return null;
        if (obtenerTicketVigente(lider) != null) return null;
        if (numeroPersonas < ConfiguracionTicket.minimoPersonasFamilia || numeroPersonas > ConfiguracionTicket.maximoPersonasFamilia) {
            return null;
        }
        if (idsOtrosMiembros == null || idsOtrosMiembros.size() != numeroPersonas - 1) return null;

        List<Visitante> miembros = new ArrayList<>();
        miembros.add(lider);

        for (String idMiembro : idsOtrosMiembros) {
            if (idMiembro == null || idMiembro.isBlank() || idMiembro.equals(visitanteId)) return null;
            Visitante miembro = visitantes.get(idMiembro);
            if (miembro == null) return null;
            if (obtenerTicketVigente(miembro) != null) return null;
            boolean repetido = miembros.stream().anyMatch(v -> v.getId().equals(miembro.getId()));
            if (repetido) return null;
            miembros.add(miembro);
        }

        double precio = ConfiguracionTicket.obtenerPrecioFamiliar(numeroPersonas);
        if (precio <= 0) return null;
        if (lider.getSaldoVirtual() < precio) return null;
        if (!lider.descontarSaldo(precio)) return null;

        Ticket ticket = new Ticket("T-" + lider.getId(), TipoTicket.FAMILIAR, lider.getId(), precio, numeroPersonas, miembros);
        ticket.setPrecioCompra(precio);
        ticket.setMiembrosDeLaFamilia(miembros);

        for (Visitante miembro : miembros) {
            miembro.setTicketComprado(ticket);
            miembro.setPuedeDentrar(true);
            miembro.setNumeroPersonasFamilia(numeroPersonas);
            miembro.setLiderGrupo(lider);
            miembro.setMiembrosGrupoFamiliar(new ArrayList<>(miembros));
        }
        lider.setLiderGrupo(lider);
        return ticket;
    }

    /** Compra una entrada familiar creando miembros internos asociados al ticket. */
    public Ticket comprarTicketFamiliarConMiembros(String visitanteId, int numeroPersonas, List<Visitante> miembrosAdicionales) {
        Visitante lider = visitantes.get(visitanteId);
        if (lider == null) return null;
        if (obtenerTicketVigente(lider) != null) return null;
        if (numeroPersonas < ConfiguracionTicket.minimoPersonasFamilia || numeroPersonas > ConfiguracionTicket.maximoPersonasFamilia) {
            return null;
        }
        if (miembrosAdicionales == null || miembrosAdicionales.size() != numeroPersonas - 1) return null;

        List<Visitante> miembros = new ArrayList<>();
        miembros.add(lider);

        for (int i = 0; i < miembrosAdicionales.size(); i++) {
            Visitante recibido = miembrosAdicionales.get(i);
            if (recibido == null) return null;

            String nombre = recibido.getNombre() != null ? recibido.getNombre().trim() : "";
            if (nombre.isBlank()) return null;

            int edad = recibido.getEdad();
            double altura = recibido.getAltura();
            if (edad < 0 || altura <= 0) return null;

            String idInterno = generarIdMiembroFamiliar(visitanteId, i, miembros);
            Visitante miembro = new Visitante(idInterno, nombre, idInterno, edad, altura);
            miembro.setPuedeDentrar(true);
            miembro.setNumeroPersonasFamilia(numeroPersonas);
            miembro.setLiderGrupo(lider);
            miembros.add(miembro);
        }

        double precio = ConfiguracionTicket.obtenerPrecioFamiliar(numeroPersonas);
        if (precio <= 0) return null;
        if (lider.getSaldoVirtual() < precio) return null;
        if (!lider.descontarSaldo(precio)) return null;

        Ticket ticket = new Ticket("T-" + lider.getId(), TipoTicket.FAMILIAR, lider.getId(), precio, numeroPersonas, miembros);
        ticket.setPrecioCompra(precio);
        ticket.setMiembrosDeLaFamilia(miembros);

        lider.setTicketComprado(ticket);
        lider.setPuedeDentrar(true);
        lider.setNumeroPersonasFamilia(numeroPersonas);
        lider.setLiderGrupo(lider);
        lider.setMiembrosGrupoFamiliar(new ArrayList<>(miembros));

        for (int i = 1; i < miembros.size(); i++) {
            Visitante miembro = miembros.get(i);
            miembro.setPuedeDentrar(true);
            miembro.setNumeroPersonasFamilia(numeroPersonas);
            miembro.setLiderGrupo(lider);
            miembro.setMiembrosGrupoFamiliar(new ArrayList<>(miembros));
        }
        return ticket;
    }

    /** Cancela la entrada del visitante o del grupo familiar asociado. */
    public boolean cancelarTicket(String visitanteId) {
        Visitante visitante = visitantes.get(visitanteId);
        if (visitante == null) return false;
        Ticket ticket = obtenerTicketVigente(visitante);
        if (ticket == null) return false;

        ticket.desactivar();
        List<Visitante> miembros = ticket.getMiembrosDeLaFamilia();
        if (miembros == null || miembros.isEmpty()) {
            visitante.setPuedeDentrar(false);
            return true;
        }

        for (Visitante miembro : miembros) {
            if (miembro != null) {
                miembro.setPuedeDentrar(false);
            }
        }
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    //  OPERADORES
    // ────────────────────────────────────────────────────────────────

    public boolean agregarOperador(Operador op) {
        if (operadores.containsKey(op.getId())) return false;
        operadores.put(op.getId(), op);
        return true;
    }

    public Operador getOperador(String id) { return operadores.get(id); }
    public Collection<Operador> getOperadores() { return operadores.values(); }

    /**
     * Asigna un operador a una zona.
     * Garantiza que no haya conflictos y que la atracción no quede sin operador.
     */
    public boolean asignarOperadorAZona(String operadorId, String zonaId) {
        Operador op = operadores.get(operadorId);
        Zona     z  = zonas.get(zonaId);
        if (op == null || z == null) return false;

        // Remover de zona anterior si existía
        if (op.getZonaAsignadaId() != null) {
            Zona zonaAnterior = zonas.get(op.getZonaAsignadaId());
            if (zonaAnterior != null) zonaAnterior.removerOperador(operadorId);
        }
        op.asignarZona(zonaId);
        z.asignarOperador(operadorId);
        return true;
    }

    public boolean eliminarOperador(String id) {
        Operador op = operadores.get(id);
        if (op == null) return false;
        removerOperadorDeZona(id);
        operadores.remove(id);
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    //  ADMINISTRADORES
    // ────────────────────────────────────────────────────────────────

    public boolean agregarAdmin(Administrador admin) {
        if (admins.containsKey(admin.getId())) return false;
        admins.put(admin.getId(), admin);
        return true;
    }

    public Administrador getAdmin(String id) { return admins.get(id); }
    public Collection<Administrador> getAdmins() { return admins.values(); }

    // ────────────────────────────────────────────────────────────────
    //  COLA DE ATRACCIONES
    // ────────────────────────────────────────────────────────────────

    /**
     * Ingresa a un visitante en la cola de una atracción.
     * Valida: estado de atracción, restricciones de seguridad y saldo si aplica.
     */
    public ResultadoIngresoCola ingresarACola(String visitanteId, String atraccionId) {
        Visitante v = visitantes.get(visitanteId);
        Atraccion a = atracciones.get(atraccionId);
        if (v == null) return ResultadoIngresoCola.fallo("Visitante no encontrado");
        if (a == null) return ResultadoIngresoCola.fallo("Atracción no encontrada");

        Ticket ticket = obtenerTicketVigente(v);
        if (ticket == null || !v.isPuedeDentrar()) return ResultadoIngresoCola.fallo("Debes comprar una entrada primero");
        if (a.getEstado() != EstadoAtraccion.ACTIVA) return ResultadoIngresoCola.fallo("Atracción no disponible: " + a.getEstado());

        Zona zona = zonas.get(a.getZonaId());
        if (zona != null && !zona.tieneCupo()) {
            return ResultadoIngresoCola.fallo("La zona " + zona.getNombre() + " ha alcanzado su aforo máximo");
        }

        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (cola == null) return ResultadoIngresoCola.fallo("Atracción no encontrada");

        List<Visitante> grupo = obtenerGrupoParaCola(v);
        List<Visitante> permitidos = new ArrayList<>();
        List<ResultadoIngresoCola.MiembroCola> rechazados = new ArrayList<>();

        for (Visitante miembro : grupo) {
            if (miembro == null) {
                rechazados.add(new ResultadoIngresoCola.MiembroCola(null, "Integrante desconocido", "Datos incompletos"));
                continue;
            }
            if (estaEnCola(atraccionId, miembro.getId())) {
                rechazados.add(new ResultadoIngresoCola.MiembroCola(miembro.getId(), miembro.getNombre(), "Ya está en la cola"));
                continue;
            }
            if (!a.cumpleRestricciones(miembro)) {
                rechazados.add(new ResultadoIngresoCola.MiembroCola(miembro.getId(), miembro.getNombre(), "No cumple restricciones de edad o altura"));
                continue;
            }
            permitidos.add(miembro);
        }

        if (permitidos.isEmpty()) {
            return ResultadoIngresoCola.fallo(construirMensajeRechazo(rechazados), List.of(), rechazados, tamanioCola(atraccionId));
        }

        double costoAtraccion = calcularCostoAtraccion(v, a);
        if (costoAtraccion > 0 && !ticket.esFastPass()) {
            Visitante pagador = ticket.esFamiliar() ? obtenerLiderGrupo(v) : v;
            if (pagador == null || pagador.getSaldoVirtual() < costoAtraccion) {
                return ResultadoIngresoCola.fallo("Saldo insuficiente para acceder a esta atracción");
            }
            if (!pagador.descontarSaldo(costoAtraccion)) {
                return ResultadoIngresoCola.fallo("Saldo insuficiente para acceder a esta atracción");
            }
        }

        for (Visitante miembro : permitidos) {
            cola.insertar(miembro, miembro.getPrioridad());
        }
        actualizarTiempoEspera(atraccionId);
        List<ResultadoIngresoCola.MiembroCola> ingresados = permitidos.stream()
            .map(miembro -> new ResultadoIngresoCola.MiembroCola(miembro.getId(), miembro.getNombre(), null))
            .toList();
        String mensaje = rechazados.isEmpty()
            ? (ticket.esFamiliar() ? "Todos los integrantes ingresaron a la cola" : "Ingresado a la cola")
            : construirMensajeParcial(ingresados, rechazados);
        return ResultadoIngresoCola.exito(mensaje, ingresados, rechazados, tamanioCola(atraccionId));
    }

    /** Procesa el siguiente visitante de la cola de una atracción. */
    public Visitante procesarSiguiente(String atraccionId) {
        List<Visitante> lote = procesarCiclo(atraccionId);
        return lote.isEmpty() ? null : lote.get(0);
    }

    /** Procesa un ciclo completo de visitantes según la capacidad de la atracción. */
    public List<Visitante> procesarCiclo(String atraccionId) {
        Atraccion a = atracciones.get(atraccionId);
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (a == null || cola == null || cola.estaVacia()) return new ArrayList<>();

        List<Visitante> lote = new ArrayList<>();
        int max = Math.max(a.getCapacidadMaximaPorCiclo(), 1);
        while (!cola.estaVacia() && lote.size() < max) {
            lote.add(cola.extraer());
        }

        for (Visitante v : lote) {
            v.registrarVisita(atraccionId);
        }

        boolean mantenimiento = a.registrarCiclo(lote.size());
        if (mantenimiento) {
            // notificar (aquí solo se registra; la GUI debe consultarlo)
            System.out.println("[ALERTA] Atracción " + a.getNombre() + " entra en mantenimiento preventivo.");
            notificarColaAtraccion(atraccionId,
                "La atracción " + a.getNombre() + " entra en mantenimiento preventivo.");
        }

        actualizarTiempoEspera(atraccionId);
        return lote;
    }

    /** Retorna el tamaño actual de la cola de una atracción. */
    public int tamanioCola(String atraccionId) {
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        return cola != null ? cola.tamanio() : 0;
    }

    /** Retorna la posición de un visitante en la cola (1 = primero). */
    public int posicionEnCola(String visitanteId, String atraccionId) {
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (cola == null || visitanteId == null) return -1;

        List<Visitante> orden = cola.toOrderedList();
        for (int i = 0; i < orden.size(); i++) {
            Visitante v = orden.get(i);
            if (v != null && visitanteId.equals(v.getId())) return i + 1;
        }
        return -1;
    }

    /** Estima el tiempo de espera en minutos para una posición dada. */
    public double tiempoEsperaParaPosicion(String atraccionId, int posicion) {
        Atraccion a = atracciones.get(atraccionId);
        if (a == null || posicion <= 0) return 0.0;
        double tiempoPorCiclo = 5.0;
        int capacidad = Math.max(a.getCapacidadMaximaPorCiclo(), 1);
        int ciclosAntes = (posicion - 1) / capacidad;
        return ciclosAntes * tiempoPorCiclo;
    }

    // ────────────────────────────────────────────────────────────────
    //  CLIMA
    // ────────────────────────────────────────────────────────────────

    /**
     * Activa una alerta climática y cierra automáticamente las atracciones
     * de tipo ACUATICA o MECANICA_ALTURA.
     */
    public List<String> activarAlertaClimatica(String tipoAlerta) {
        this.alertaClimatica = true;
        this.tipoAlerta      = tipoAlerta;
        List<String> cerradas = new ArrayList<>();
        for (Atraccion a : atracciones.values()) {
            if (a.getTipo() == TipoAtraccion.ACUATICA || a.getTipo() == TipoAtraccion.MECANICA_ALTURA) {
                if (a.getEstado() == EstadoAtraccion.ACTIVA) {
                    a.cerrar("Cierre por alerta climática: " + tipoAlerta);
                    cerradas.add(a.getId());
                    historialCierresClima.add(new CierreClimaticoRegistro(
                        a.getId(),
                        a.getNombre(),
                        a.getTipo(),
                        tipoAlerta,
                        a.getMotivoCierre(),
                        LocalDateTime.now()
                    ));
                    notificarColaAtraccion(a.getId(),
                        "La atracción " + a.getNombre() + " fue cerrada por " + tipoAlerta);
                }
            }
        }
        return cerradas;
    }

    /** Desactiva la alerta climática y reabre atracciones cerradas por la alerta. */
    public void desactivarAlertaClimatica() {
        this.alertaClimatica = false;
        this.tipoAlerta      = "";

        // Reactivar atracciones que fueron cerradas por la alerta climática
        for (Atraccion a : atracciones.values()) {
            if (a.getEstado() == EstadoAtraccion.CERRADA
                && a.getMotivoCierre() != null
                && a.getMotivoCierre().toLowerCase().contains("climática")) {
                a.setEstado(EstadoAtraccion.ACTIVA);
                a.setMotivoCierre(null);
                // Notificar a los visitantes de la cola que la atracción reabrió
                notificarColaAtraccion(a.getId(), "La atracción " + a.getNombre() + " ha reabierto tras desactivar la alerta climática.");
            }
        }
    }

    public boolean isAlertaClimatica() { return alertaClimatica; }
    public String  getTipoAlerta()     { return tipoAlerta; }

    // ────────────────────────────────────────────────────────────────
    //  GRAFO
    // ────────────────────────────────────────────────────────────────

    public Grafo getGrafo() { return grafo; }

    /** Conecta dos atracciones en el grafo con el peso dado. */
    public void conectarAtracciones(String idA, String idB, double peso) {
        Atraccion a = atracciones.get(idA);
        Atraccion b = atracciones.get(idB);
        if (a == null || b == null || idA.equals(idB)) return;
        if (estanConectadasLocalmente(idA, idB)) return;
        a.agregarConexion(idB);
        a.registrarPesoConexion(idB, peso);
        b.agregarConexion(idA);
        b.registrarPesoConexion(idA, peso);
        grafo.conectar(idA, idB, peso);
    }

    /** Desconecta dos atracciones en el grafo. */
    public boolean desconectarAtracciones(String idA, String idB) {
        Atraccion a = atracciones.get(idA);
        Atraccion b = atracciones.get(idB);
        if (a == null || b == null || !grafo.existeNodo(idA) || !grafo.existeNodo(idB)) return false;
        a.quitarConexion(idB);
        b.quitarConexion(idA);
        grafo.desconectar(idA, idB);
        return true;
    }

    /** Actualiza el peso de la conexión entre dos atracciones. */
    public boolean actualizarConexion(String idA, String idB, double peso) {
        if (!grafo.existeNodo(idA) || !grafo.existeNodo(idB)) return false;
        if (!estanConectadasLocalmente(idA, idB)) return false;
        Atraccion a = atracciones.get(idA);
        Atraccion b = atracciones.get(idB);
        if (a != null) a.registrarPesoConexion(idB, peso);
        if (b != null) b.registrarPesoConexion(idA, peso);
        grafo.desconectar(idA, idB);
        grafo.conectar(idA, idB, peso);
        return true;
    }

    /** Reconstruye el grafo en memoria a partir de las conexiones persistidas en cada atracción. */
    public void reconstruirGrafoDesdeAtracciones() {
        grafo.limpiar();
        for (Atraccion atraccion : atracciones.values()) {
            grafo.agregarNodo(atraccion.getId());
        }

        Set<String> procesadas = new HashSet<>();
        for (Atraccion atraccion : atracciones.values()) {
            for (String destino : atraccion.getConexiones()) {
                if (destino == null || destino.isBlank() || atraccion.getId().equals(destino)) continue;
                String clave = claveConexion(atraccion.getId(), destino);
                if (procesadas.add(clave)) {
                    grafo.conectar(atraccion.getId(), destino, pesoConexionGuardada(atraccion.getId(), destino));
                }
            }
        }
    }

    /** Define una topología base si el parque no tiene conexiones guardadas. */
    public void sembrarMapaBaseSiHaceFalta() {
        asignarPosicionesMapaPorDefectoSiHaceFalta();
        if (tieneConexiones()) return;

        conectarAtracciones("A1", "A2", 50);
        conectarAtracciones("A1", "A5", 100);
        conectarAtracciones("A2", "A3", 80);
        conectarAtracciones("A3", "A4", 30);
        conectarAtracciones("A3", "A7", 90);
        conectarAtracciones("A4", "A5", 90);
        conectarAtracciones("A5", "A6", 40);
        conectarAtracciones("A6", "A7", 70);
        conectarAtracciones("A7", "A8", 20);
        conectarAtracciones("A8", "A1", 100);
    }

    /** Asigna posiciones fijas al mapa para las atracciones sin coordenadas. */
    public void asignarPosicionesMapaPorDefectoSiHaceFalta() {
        Map<String, double[]> posiciones = posicionesMapaBase();

        int indiceExtra = 0;
        for (Atraccion atraccion : atracciones.values()) {
            if (atraccion.tienePosicion()) continue;
            double[] posicion = posiciones.get(atraccion.getId());
            if (posicion == null) {
                posicion = new double[]{420 + (indiceExtra * 90), 300 + ((indiceExtra % 2) * 120)};
                indiceExtra++;
            }
            atraccion.setPosicionX(posicion[0]);
            atraccion.setPosicionY(posicion[1]);
        }
    }

    /** Restaura todas las posiciones al layout base del parque. */
    public void restaurarPosicionesMapaPorDefecto() {
        Map<String, double[]> posiciones = posicionesMapaBase();
        int indiceExtra = 0;

        for (Atraccion atraccion : atracciones.values()) {
            double[] posicion = posiciones.get(atraccion.getId());
            if (posicion == null) {
                posicion = new double[]{420 + (indiceExtra * 90), 300 + ((indiceExtra % 2) * 120)};
                indiceExtra++;
            }
            atraccion.setPosicionX(posicion[0]);
            atraccion.setPosicionY(posicion[1]);
        }
    }

    /** Actualiza la posición visual de una atracción en el mapa. */
    public boolean actualizarPosicionAtraccion(String atraccionId, double x, double y) {
        Atraccion atraccion = atracciones.get(atraccionId);
        if (atraccion == null) return false;
        atraccion.setPosicionX(x);
        atraccion.setPosicionY(y);
        return true;
    }

    private boolean tieneConexiones() {
        for (Atraccion atraccion : atracciones.values()) {
            if (!atraccion.getConexiones().isEmpty()) return true;
        }
        return false;
    }

    private boolean estanConectadasLocalmente(String idA, String idB) {
        Atraccion a = atracciones.get(idA);
        Atraccion b = atracciones.get(idB);
        return a != null && b != null
            && a.getConexiones().contains(idB)
            && b.getConexiones().contains(idA);
    }

    private String claveConexion(String idA, String idB) {
        return idA.compareTo(idB) < 0 ? idA + "-" + idB : idB + "-" + idA;
    }

    private Map<String, double[]> posicionesMapaBase() {
        Map<String, double[]> posiciones = new LinkedHashMap<>();
        posiciones.put("A1", new double[]{150, 120});
        posiciones.put("A2", new double[]{300, 220});
        posiciones.put("A3", new double[]{620, 120});
        posiciones.put("A4", new double[]{760, 250});
        posiciones.put("A5", new double[]{190, 420});
        posiciones.put("A6", new double[]{360, 540});
        posiciones.put("A7", new double[]{620, 420});
        posiciones.put("A8", new double[]{780, 540});
        return posiciones;
    }

    private double pesoConexionPorDefecto(String idA, String idB) {
        return switch (claveConexion(idA, idB)) {
            case "A1-A2" -> 50;
            case "A1-A5" -> 120;
            case "A2-A3" -> 80;
            case "A3-A4" -> 30;
            case "A3-A7" -> 150;
            case "A4-A5" -> 90;
            case "A5-A6" -> 40;
            case "A6-A7" -> 70;
            case "A7-A8" -> 20;
            case "A1-A8" -> 200;
            default -> 100.0;
        };
    }

    private double pesoConexionGuardada(String idA, String idB) {
        Atraccion a = atracciones.get(idA);
        Atraccion b = atracciones.get(idB);
        if (a != null) {
            double peso = a.getPesoConexion(idB);
            if (!Double.isNaN(peso)) return peso;
        }
        if (b != null) {
            double peso = b.getPesoConexion(idA);
            if (!Double.isNaN(peso)) return peso;
        }
        return pesoConexionPorDefecto(idA, idB);
    }

    // ────────────────────────────────────────────────────────────────
    //  REPORTES
    // ────────────────────────────────────────────────────────────────

    /** Retorna las atracciones con más ciclos completados (top N). */
    public List<Atraccion> topAtracciones(int n) {
        List<Atraccion> lista = new ArrayList<>(atracciones.values());
        lista.sort((a, b) -> Integer.compare(b.getCiclosCompletados(), a.getCiclosCompletados()));
        return lista.subList(0, Math.min(n, lista.size()));
    }

    /** Retorna todas las atracciones en mantenimiento. */
    public List<Atraccion> atraccionesEnMantenimiento() {
        List<Atraccion> lista = new ArrayList<>();
        for (Atraccion a : atracciones.values())
            if (a.getEstado() == EstadoAtraccion.EN_MANTENIMIENTO) lista.add(a);
        return lista;
    }

    /** Retorna el historial de cierres por clima. */
    public List<CierreClimaticoRegistro> historialCierresClima() {
        return new ArrayList<>(historialCierresClima);
    }

    /** Ingreso diario: suma de precios de tickets activos sin duplicar tickets compartidos. */
    public double calcularIngresoDiario() {
        double total = 0;
        Set<String> ticketsProcesados = new HashSet<>();
        for (Visitante v : visitantes.values())
            {
                Ticket ticket = obtenerTicketVigente(v);
                if (ticket != null && ticket.isActivo() && ticketsProcesados.add(ticket.getId())) {
                    total += ticket.getPrecio();
                }
            }
        return total;
    }

    /** Tiempo promedio de espera de todas las atracciones activas. */
    public double tiempoPromedioEspera() {
        double suma = 0; int count = 0;
        for (Atraccion a : atracciones.values()) {
            if (a.getEstado() == EstadoAtraccion.ACTIVA) { suma += a.getTiempoEsperaEstimado(); count++; }
        }
        return count > 0 ? suma / count : 0;
    }

    // ── Getters de estado del parque ────────────────────────────────────
    public int getCapacidadMaximaParque() { return capacidadMaximaParque; }
    public void setCapacidadMaximaParque(int cap) { this.capacidadMaximaParque = cap; }
    public int getVisitantesActualesParque() { return visitantesActualesParque; }
    public ABB<String, Atraccion> getArbolAtracciones() { return arbolAtracciones; }

    // ────────────────────────────────────────────────────────────────
    //  TIEMPO DE ESPERA
    // ────────────────────────────────────────────────────────────────

    private void actualizarTiempoEspera(String atraccionId) {
        Atraccion a = atracciones.get(atraccionId);
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (a == null || cola == null) return;
        double tiempoPorCiclo = 5.0;
        int capacidad = Math.max(a.getCapacidadMaximaPorCiclo(), 1);
        int ciclosNecesarios = (int) Math.ceil((double) cola.tamanio() / capacidad);
        a.setTiempoEsperaEstimado(ciclosNecesarios * tiempoPorCiclo);
    }

    // ────────────────────────────────────────────────────────────────
    //  NOTIFICACIONES A VISITANTES
    // ────────────────────────────────────────────────────────────────

    public void agregarNotificacion(String visitanteId, String mensaje) {
        notificacionesPorVisitante
            .computeIfAbsent(visitanteId, k -> new ArrayList<>())
            .add(mensaje);
    }

    public List<String> getNotificaciones(String visitanteId) {
        return notificacionesPorVisitante.getOrDefault(visitanteId, new ArrayList<>());
    }

    public void limpiarNotificaciones(String visitanteId) {
        notificacionesPorVisitante.remove(visitanteId);
    }

    public List<CierreClimaticoRegistro> getHistorialCierresClima() {
        return new ArrayList<>(historialCierresClima);
    }

    public void cargarHistorialCierresClima(List<CierreClimaticoRegistro> registros) {
        historialCierresClima.clear();
        if (registros != null) {
            historialCierresClima.addAll(registros);
        }
    }

    public static final class CierreClimaticoRegistro {
        private String atraccionId;
        private String atraccionNombre;
        private TipoAtraccion tipoAtraccion;
        private String tipoAlerta;
        private String motivoCierre;
        private LocalDateTime fechaCierre;

        public CierreClimaticoRegistro() {}

        public CierreClimaticoRegistro(String atraccionId, String atraccionNombre, TipoAtraccion tipoAtraccion,
                                       String tipoAlerta, String motivoCierre, LocalDateTime fechaCierre) {
            this.atraccionId = atraccionId;
            this.atraccionNombre = atraccionNombre;
            this.tipoAtraccion = tipoAtraccion;
            this.tipoAlerta = tipoAlerta;
            this.motivoCierre = motivoCierre;
            this.fechaCierre = fechaCierre;
        }

        public String getAtraccionId() { return atraccionId; }
        public String getAtraccionNombre() { return atraccionNombre; }
        public TipoAtraccion getTipoAtraccion() { return tipoAtraccion; }
        public String getTipoAlerta() { return tipoAlerta; }
        public String getMotivoCierre() { return motivoCierre; }
        public LocalDateTime getFechaCierre() { return fechaCierre; }
    }

    private void notificarColaAtraccion(String atraccionId, String mensaje) {
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (cola == null || cola.estaVacia()) return;
        for (Visitante v : cola.toList()) {
            if (v != null) agregarNotificacion(v.getId(), mensaje);
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  OPERADORES
    // ────────────────────────────────────────────────────────────────

    public String removerOperadorDeZona(String operadorId) {
        Operador op = operadores.get(operadorId);
        if (op == null || op.getZonaAsignadaId() == null) return "OK";

        String zonaId = op.getZonaAsignadaId();
        long otrosOperadores = contarOperadoresEnZona(zonaId, operadorId);

        Zona z = zonas.get(zonaId);
        if (z != null) z.removerOperador(operadorId);
        op.removerZona();

        return otrosOperadores == 0
            ? "ADVERTENCIA: La zona " + zonaId + " quedaría sin operadores"
            : "OK";
    }

    public String advertenciaRemoverOperador(String operadorId) {
        Operador op = operadores.get(operadorId);
        if (op == null || op.getZonaAsignadaId() == null) return "OK";
        String zonaId = op.getZonaAsignadaId();
        long otrosOperadores = contarOperadoresEnZona(zonaId, operadorId);
        return otrosOperadores == 0
            ? "ADVERTENCIA: La zona " + zonaId + " quedaría sin operadores"
            : "OK";
    }

    private long contarOperadoresEnZona(String zonaId, String operadorId) {
        return operadores.values().stream()
            .filter(o -> !o.getId().equals(operadorId) && zonaId.equals(o.getZonaAsignadaId()))
            .count();
    }

    public double calcularCostoAtraccion(Visitante v, Atraccion a) {
        Ticket ticket = obtenerTicketVigente(v);
        if (ticket == null || a == null) return 0.0;
        if (ticket.esFastPass()) return 0.0;
        return ConfiguracionTicket.obtenerCostoAtraccion(a.getId(), a.getCostoAdicional());
    }

    public String validarAccesoAAtraccion(String visitanteId, String atraccionId) {
        Visitante visitante = visitantes.get(visitanteId);
        Atraccion atraccion = atracciones.get(atraccionId);
        if (visitante == null) return "Visitante no encontrado";
        if (atraccion == null) return "Atracción no encontrada";
        if (atraccion.getEstado() != EstadoAtraccion.ACTIVA) return "Atracción no disponible: " + atraccion.getEstado();

        Ticket ticket = obtenerTicketVigente(visitante);
        if (ticket == null || !visitante.isPuedeDentrar()) return "Debes comprar una entrada primero";

        if (ticket.esFamiliar()) {
            List<String> rechazados = new ArrayList<>();
            for (Visitante miembro : obtenerGrupoParaCola(visitante)) {
                if (miembro == null) {
                    rechazados.add("Integrante desconocido");
                } else if (!atraccion.cumpleRestricciones(miembro)) {
                    rechazados.add(miembro.getNombre());
                }
            }
            return rechazados.isEmpty() ? null : "No cumplen restricciones: " + String.join(", ", rechazados);
        }

        if (!atraccion.cumpleRestricciones(visitante)) return "No cumple restricciones de edad o altura";
        return null;
    }

    private Ticket obtenerTicketVigente(Visitante visitante) {
        if (visitante == null) return null;
        Ticket ticket = visitante.getTicketComprado();
        if (ticket == null) ticket = visitante.getTicketActivo();
        return ticket != null && ticket.isActivo() ? ticket : null;
    }

    private Visitante obtenerLiderGrupo(Visitante visitante) {
        if (visitante == null) return null;
        Visitante lider = visitante.getLiderGrupo();
        return lider != null ? lider : visitante;
    }

    private List<Visitante> obtenerGrupoParaCola(Visitante visitante) {
        Ticket ticket = obtenerTicketVigente(visitante);
        if (ticket == null) return List.of(visitante);
        if (!ticket.esFamiliar()) return List.of(visitante);

        List<Visitante> miembros = ticket.getMiembrosDeLaFamilia();
        if (miembros != null && !miembros.isEmpty()) {
            return miembros;
        }

        Visitante lider = obtenerLiderGrupo(visitante);
        if (lider == null) return List.of(visitante);

        miembros = lider.getMiembrosGrupoFamiliar();
        if (miembros == null || miembros.isEmpty()) {
            return List.of(visitante);
        }
        return miembros;
    }

    private String construirMensajeRechazo(List<ResultadoIngresoCola.MiembroCola> rechazados) {
        if (rechazados == null || rechazados.isEmpty()) {
            return "Ningún integrante cumple las condiciones";
        }
        return "No cumplen condiciones: " + rechazados.stream()
            .map(miembro -> miembro.getNombre() != null ? miembro.getNombre() : "Integrante desconocido")
            .distinct()
            .reduce((a, b) -> a + ", " + b)
            .orElse("Ningún integrante cumple las condiciones");
    }

    private String construirMensajeParcial(List<ResultadoIngresoCola.MiembroCola> ingresados, List<ResultadoIngresoCola.MiembroCola> rechazados) {
        String nombreIngresados = ingresados == null || ingresados.isEmpty()
            ? "Ningún integrante"
            : ingresados.stream().map(ResultadoIngresoCola.MiembroCola::getNombre).reduce((a, b) -> a + ", " + b).orElse("Ningún integrante");
        String nombreRechazados = rechazados == null || rechazados.isEmpty()
            ? ""
            : rechazados.stream()
                .map(miembro -> miembro.getNombre() != null ? miembro.getNombre() : "Integrante desconocido")
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        if (nombreRechazados.isBlank()) {
            return "Ingresaron a la cola: " + nombreIngresados;
        }
        return "Ingresaron a la cola: " + nombreIngresados + ". No cumplen condiciones: " + nombreRechazados;
    }

    private String generarIdMiembroFamiliar(String liderId, int indice, List<Visitante> miembrosExistentes) {
        String base = "FAM-" + liderId + "-" + (indice + 1);
        String candidato = base;
        int intento = 1;
        Set<String> idsExistentes = new HashSet<>();
        if (miembrosExistentes != null) {
            for (Visitante miembro : miembrosExistentes) {
                if (miembro != null && miembro.getId() != null) {
                    idsExistentes.add(miembro.getId());
                }
            }
        }
        while (visitantes.containsKey(candidato) || idsExistentes.contains(candidato)) {
            candidato = base + "-" + intento++;
        }
        return candidato;
    }

    public static class ResultadoIngresoCola {
        private final boolean exitoso;
        private final String mensaje;
        private final List<MiembroCola> ingresados;
        private final List<MiembroCola> rechazados;
        private final int tamanioCola;

        public ResultadoIngresoCola(boolean exitoso, String mensaje, List<MiembroCola> ingresados, List<MiembroCola> rechazados, int tamanioCola) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.ingresados = ingresados != null ? new ArrayList<>(ingresados) : new ArrayList<>();
            this.rechazados = rechazados != null ? new ArrayList<>(rechazados) : new ArrayList<>();
            this.tamanioCola = tamanioCola;
        }

        public static ResultadoIngresoCola exito(String mensaje, List<MiembroCola> ingresados, List<MiembroCola> rechazados, int tamanioCola) {
            return new ResultadoIngresoCola(true, mensaje, ingresados, rechazados, tamanioCola);
        }

        public static ResultadoIngresoCola fallo(String mensaje) {
            return new ResultadoIngresoCola(false, mensaje, List.of(), List.of(), 0);
        }

        public static ResultadoIngresoCola fallo(String mensaje, List<MiembroCola> ingresados, List<MiembroCola> rechazados, int tamanioCola) {
            return new ResultadoIngresoCola(false, mensaje, ingresados, rechazados, tamanioCola);
        }

        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public List<MiembroCola> getIngresados() { return ingresados; }
        public List<MiembroCola> getRechazados() { return rechazados; }
        public int getTamanioCola() { return tamanioCola; }

        public static class MiembroCola {
            private final String id;
            private final String nombre;
            private final String motivo;

            public MiembroCola(String id, String nombre, String motivo) {
                this.id = id;
                this.nombre = nombre;
                this.motivo = motivo;
            }

            public String getId() { return id; }
            public String getNombre() { return nombre; }
            public String getMotivo() { return motivo; }
        }
    }

    private boolean estaEnCola(String atraccionId, String visitanteId) {
        ColaPrioridad<Visitante> cola = colasPorAtraccion.get(atraccionId);
        if (cola == null || visitanteId == null) return false;
        return cola.toList().stream().anyMatch(v -> v != null && visitanteId.equals(v.getId()));
    }
}
