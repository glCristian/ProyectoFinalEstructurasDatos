package com.techpark.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.google.gson.reflect.TypeToken;
import com.techpark.model.Administrador;
import com.techpark.model.Atraccion;
import com.techpark.model.Operador;
import com.techpark.model.Ticket;
import com.techpark.model.TipoTicket;
import com.techpark.model.Visitante;
import com.techpark.model.Zona;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

public class PersistenciaService {
    private static final Path RUTA_DATA = resolverRutaData();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
            })
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
                try {
                    return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    throw new JsonParseException(e);
                }
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> {
                return new com.google.gson.JsonPrimitive(src.toString());
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                try {
                    return LocalDateTime.parse(json.getAsString());
                } catch (Exception e) {
                    throw new JsonParseException(e);
                }
            })
            .create();

    // Guardar todo el estado
    public void guardarTodo(ParqueService parque) {
        guardarArchivo("atracciones.json", parque.getAtracciones());
        guardarArchivo("zonas.json",       parque.getZonas());
        guardarArchivo("visitantes.json",  serializarVisitantes(parque.getVisitantes()));
        guardarArchivo("operadores.json",  parque.getOperadores());
        guardarArchivo("admins.json",      parque.getAdmins());
        guardarArchivo("cierres_clima.json", parque.getHistorialCierresClima());
        
    }

    // Cargar al arrancar
    public void cargarTodo(ParqueService parque) {
        List<Zona> zonas = leerLista("zonas.json", new TypeToken<List<Zona>>() {}.getType());
        for (Zona z : zonas) {
            parque.agregarZona(z);
        }

        List<Atraccion> atracciones = leerLista("atracciones.json", new TypeToken<List<Atraccion>>() {}.getType());
        for (Atraccion a : atracciones) {
            parque.agregarAtraccion(a);
        }

        parque.reconstruirGrafoDesdeAtracciones();
        parque.sembrarMapaBaseSiHaceFalta();

        List<VisitantePersistido> visitantesPersistidos = leerVisitantesPersistidos("visitantes.json");
        Map<String, Visitante> visitantesCargados = new LinkedHashMap<>();
        for (VisitantePersistido persistido : visitantesPersistidos) {
            Visitante visitante = persistido.aModelo();
            parque.agregarVisitanteCargado(visitante);
            visitantesCargados.put(visitante.getId(), visitante);
        }

        reconstruirTickets(visitantesPersistidos, visitantesCargados);

        List<Operador> operadores = leerLista("operadores.json", new TypeToken<List<Operador>>() {}.getType());
        for (Operador op : operadores) {
            parque.agregarOperador(op);
        }

        List<Administrador> admins = leerLista("admins.json", new TypeToken<List<Administrador>>() {}.getType());
        for (Administrador admin : admins) {
            parque.agregarAdmin(admin);
        }

        List<ParqueService.CierreClimaticoRegistro> cierresClima = leerLista(
            "cierres_clima.json",
            new TypeToken<List<ParqueService.CierreClimaticoRegistro>>() {}.getType()
        );
        parque.cargarHistorialCierresClima(cierresClima);
    }

    private void guardarArchivo(String nombre, Object datos) {
        try {
            if (!Files.exists(RUTA_DATA)) {
                Files.createDirectories(RUTA_DATA);
            }
            Files.writeString(RUTA_DATA.resolve(nombre), gson.toJson(datos));
        } catch (Throwable t) {
            // Capturamos Throwable para que un fallo de serialización no rompa el flujo de negocio.
            System.err.println("Error al guardar archivo " + nombre + ": " + t.getMessage());
        }
    }

    private List<VisitantePersistido> serializarVisitantes(Collection<Visitante> visitantes) {
        List<VisitantePersistido> persistidos = new ArrayList<>();
        if (visitantes == null) {
            return persistidos;
        }

        for (Visitante visitante : visitantes) {
            persistidos.add(VisitantePersistido.desdeModelo(visitante));
        }

        return persistidos;
    }

    private List<VisitantePersistido> leerVisitantesPersistidos(String nombre) {
        Path path = RUTA_DATA.resolve(nombre);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        try {
            String contenido = Files.readString(path);
            if (contenido == null || contenido.isBlank()) {
                return Collections.emptyList();
            }

            JsonElement raiz = JsonParser.parseString(contenido);
            if (!raiz.isJsonArray()) {
                return Collections.emptyList();
            }

            List<VisitantePersistido> visitantes = new ArrayList<>();
            for (JsonElement elemento : raiz.getAsJsonArray()) {
                VisitantePersistido persistido = gson.fromJson(elemento, VisitantePersistido.class);
                if (persistido != null) {
                    visitantes.add(persistido);
                }
            }
            return visitantes;
        } catch (Exception e) {
            System.err.println("Error al leer archivo " + nombre + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void reconstruirTickets(List<VisitantePersistido> visitantesPersistidos, Map<String, Visitante> visitantesCargados) {
        Map<String, Ticket> ticketsCanonicos = new LinkedHashMap<>();
        Map<String, List<Visitante>> miembrosPorTicket = new LinkedHashMap<>();

        for (VisitantePersistido persistido : visitantesPersistidos) {
            if (persistido == null || persistido.ticketComprado == null || persistido.ticketComprado.id == null) {
                continue;
            }

            String ticketId = persistido.ticketComprado.id;
            Ticket ticket = ticketsCanonicos.get(ticketId);
            if (ticket == null) {
                ticket = persistido.ticketComprado.aModelo();
                ticketsCanonicos.put(ticketId, ticket);
            }

            List<Visitante> miembros = miembrosPorTicket.computeIfAbsent(ticketId, id -> new ArrayList<>());
            if (persistido.ticketComprado.miembrosDeLaFamilia != null) {
                for (MiembroFamiliarPersistido miembroPersistido : persistido.ticketComprado.miembrosDeLaFamilia) {
                    if (miembroPersistido == null || miembroPersistido.id == null || miembroPersistido.id.isBlank()) {
                        continue;
                    }

                    Visitante miembro = visitantesCargados.get(miembroPersistido.id);
                    if (miembro == null) {
                        miembro = miembroPersistido.aModelo();
                    }

                    boolean yaAgregado = miembros.stream().anyMatch(actual -> miembroPersistido.id.equals(actual.getId()));
                    if (!yaAgregado) {
                        miembros.add(miembro);
                    }
                }
            }

            Visitante visitante = visitantesCargados.get(persistido.id);
            if (visitante != null) {
                visitante.setTicketComprado(ticket);
                visitante.setPuedeDentrar(ticket.isActivo());
                visitante.setNumeroPersonasFamilia(ticket.getNumeroPersonasIncluidas());
                if (visitante.getId().equals(ticket.getVisitanteId())) {
                    visitante.setLiderGrupo(visitante);
                } else {
                    Visitante lider = visitantesCargados.get(ticket.getVisitanteId());
                    visitante.setLiderGrupo(lider != null ? lider : visitante);
                }
            }
        }

        for (Map.Entry<String, Ticket> entry : ticketsCanonicos.entrySet()) {
            Ticket ticket = entry.getValue();
            List<Visitante> miembros = miembrosPorTicket.getOrDefault(entry.getKey(), new ArrayList<>());
            if (miembros.isEmpty()) {
                Visitante lider = visitantesCargados.get(ticket.getVisitanteId());
                if (lider != null) {
                    miembros = new ArrayList<>();
                    miembros.add(lider);
                }
            }

            ticket.setMiembrosDeLaFamilia(miembros);
            ticket.setNumeroPersonasIncluidas(Math.max(1, miembros.size()));
            for (Visitante miembro : miembros) {
                if (miembro == null) {
                    continue;
                }
                miembro.setTicketComprado(ticket);
                miembro.setPuedeDentrar(ticket.isActivo());
                miembro.setNumeroPersonasFamilia(ticket.getNumeroPersonasIncluidas());
                Visitante lider = visitantesCargados.get(ticket.getVisitanteId());
                miembro.setLiderGrupo(lider != null ? lider : miembro);
                List<Visitante> grupo = new ArrayList<>(miembros);
                miembro.setMiembrosGrupoFamiliar(grupo);
            }
        }
    }

    private <T> List<T> leerLista(String nombre, Type type) {
        Path path = RUTA_DATA.resolve(nombre);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        try {
            String contenido = Files.readString(path);
            List<T> lista = gson.fromJson(contenido, type);
            return lista != null ? lista : Collections.emptyList();
        } catch (IOException e) {
            System.err.println("Error al leer archivo " + nombre + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static Path resolverRutaData() {
        Path backendData = Paths.get("backend", "data");
        if (Files.exists(backendData)) {
            return backendData;
        }

        Path dataLocal = Paths.get("data");
        if (Files.exists(dataLocal)) {
            return dataLocal;
        }

        return backendData;
    }

    private static final class VisitantePersistido {
        String id;
        String nombre;
        String documento;
        int edad;
        double altura;
        String fotoPaseUrl;
        String username;
        String passwordHash;
        double saldoVirtual;
        TicketPersistido ticketComprado;
        boolean puedeDentrar;
        int numeroPersonasFamilia;
        com.techpark.structures.ListaEnlazada<String> historialVisitas;
        com.techpark.structures.SetPropio<String> favoritos;

        static VisitantePersistido desdeModelo(Visitante visitante) {
            VisitantePersistido dto = new VisitantePersistido();
            dto.id = visitante.getId();
            dto.nombre = visitante.getNombre();
            dto.documento = visitante.getDocumento();
            dto.edad = visitante.getEdad();
            dto.altura = visitante.getAltura();
            dto.fotoPaseUrl = visitante.getFotoPaseUrl();
            dto.username = visitante.getUsername();
            dto.passwordHash = visitante.getPasswordHash();
            dto.saldoVirtual = visitante.getSaldoVirtual();
            dto.puedeDentrar = visitante.isPuedeDentrar();
            dto.numeroPersonasFamilia = visitante.getNumeroPersonasFamilia();
            dto.historialVisitas = visitante.getHistorialVisitas();
            dto.favoritos = visitante.getFavoritos();

            Ticket ticket = visitante.getTicketComprado();
            if (ticket != null) {
                TicketPersistido persistido = TicketPersistido.desdeModelo(ticket);
                dto.ticketComprado = persistido;
            }

            return dto;
        }

        Visitante aModelo() {
            Visitante visitante = new Visitante();
            visitante.setId(id);
            visitante.setNombre(nombre);
            visitante.setDocumento(documento);
            visitante.setEdad(edad);
            visitante.setAltura(altura);
            visitante.setFotoPaseUrl(fotoPaseUrl);
            visitante.setUsername(username);
            visitante.setPasswordHash(passwordHash);
            visitante.setSaldoVirtual(saldoVirtual);
            visitante.setPuedeDentrar(puedeDentrar);
            visitante.setNumeroPersonasFamilia(numeroPersonasFamilia);
            if (historialVisitas != null) {
                visitante.setHistorialVisitas(historialVisitas);
            }
            if (favoritos != null) {
                visitante.setFavoritos(favoritos);
            }
            return visitante;
        }
    }

    private static final class TicketPersistido {
        String id;
        TipoTicket tipo;
        String visitanteId;
        LocalDate fechaEmision;
        LocalDate fechaVencimiento;
        boolean activo;
        double precio;
        int numeroPersonasIncluidas;
        List<MiembroFamiliarPersistido> miembrosDeLaFamilia;
        double precioCompra;
        LocalDateTime fechaCompra;

        static TicketPersistido desdeModelo(Ticket ticket) {
            TicketPersistido dto = new TicketPersistido();
            dto.id = ticket.getId();
            dto.tipo = ticket.getTipo();
            dto.visitanteId = ticket.getVisitanteId();
            dto.fechaEmision = ticket.getFechaEmision();
            dto.fechaVencimiento = ticket.getFechaVencimiento();
            dto.activo = ticket.isActivo();
            dto.precio = ticket.getPrecio();
            dto.numeroPersonasIncluidas = ticket.getNumeroPersonasIncluidas();
            dto.precioCompra = ticket.getPrecioCompra();
            dto.fechaCompra = ticket.getFechaCompra();

            List<MiembroFamiliarPersistido> miembros = new ArrayList<>();
            for (Visitante miembro : ticket.getMiembrosDeLaFamilia()) {
                if (miembro == null) {
                    continue;
                }
                miembros.add(MiembroFamiliarPersistido.desdeModelo(miembro));
            }
            dto.miembrosDeLaFamilia = miembros;
            return dto;
        }

        Ticket aModelo() {
            Ticket ticket = new Ticket();
            ticket.setId(id);
            ticket.setTipo(tipo);
            ticket.setVisitanteId(visitanteId);
            ticket.setFechaEmision(fechaEmision);
            ticket.setFechaVencimiento(fechaVencimiento);
            ticket.setActivo(activo);
            ticket.setPrecio(precioCompra > 0 ? precioCompra : precio);
            ticket.setNumeroPersonasIncluidas(numeroPersonasIncluidas);
            ticket.setPrecioCompra(precioCompra > 0 ? precioCompra : precio);
            ticket.setFechaCompra(fechaCompra);
            return ticket;
        }
    }

    private static final class MiembroFamiliarPersistido {
        String id;
        String nombre;
        String documento;
        int edad;
        double altura;

        static MiembroFamiliarPersistido desdeModelo(Visitante visitante) {
            MiembroFamiliarPersistido dto = new MiembroFamiliarPersistido();
            dto.id = visitante.getId();
            dto.nombre = visitante.getNombre();
            dto.documento = visitante.getDocumento();
            dto.edad = visitante.getEdad();
            dto.altura = visitante.getAltura();
            return dto;
        }

        Visitante aModelo() {
            return new Visitante(id, nombre, documento, edad, altura);
        }
    }
}
