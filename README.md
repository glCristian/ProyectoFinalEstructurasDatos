# 🎢 Tech-Park UQ — Sistema de Gestión de Parque de Atracciones

Sistema integral para la gestión operativa del parque de diversiones **Tech-Park UQ**.  
Desarrollado con **Java + Javalin** (backend) y **React + Vite** (frontend).

---

## 📁 Estructura del proyecto

```
tech-park/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/techpark/
│       │   │   ├── App.java                          ← Punto de entrada Javalin (puerto 8080)
│       │   │   ├── model/
│       │   │   │   ├── Atraccion.java
│       │   │   │   ├── Zona.java
│       │   │   │   ├── Visitante.java
│       │   │   │   ├── Operador.java
│       │   │   │   ├── Administrador.java
│       │   │   │   ├── Ticket.java
│       │   │   │   ├── TipoTicket.java               ← Enum: GENERAL | FAMILIAR | FAST_PASS
│       │   │   │   ├── TipoAtraccion.java            ← Enum: ACUATICA | MECANICA_ALTURA | …
│       │   │   │   └── EstadoAtraccion.java          ← Enum: ACTIVA | EN_MANTENIMIENTO | CERRADA
│       │   │   ├── structures/                       ← IMPLEMENTACIONES PROPIAS (sin java.util)
│       │   │   │   ├── ListaEnlazada.java            ← Historial de visitas, lista de operadores
│       │   │   │   ├── SetPropio.java                ← Favoritos del visitante (tabla hash)
│       │   │   │   ├── ColaPrioridad.java            ← Min-heap: FastPass(1) > General(2)
│       │   │   │   ├── ABB.java                      ← Árbol BST para búsqueda de atracciones
│       │   │   │   └── Grafo.java                    ← Lista adyacencia + BFS + Dijkstra
│       │   │   ├── service/
│       │   │   │   ├── ParqueService.java            ← Singleton: repositorio central + lógica
│       │   │   │   ├── RutaService.java              ← Dijkstra / BFS / grafo para vista
│       │   │   │   └── CargaDatosService.java        ← Carga desde archivo plano o escenario
│       │   │   └── controller/
│       │   │       ├── AtraccionController.java
│       │   │       ├── ZonaController.java
│       │   │       ├── VisitanteController.java
│       │   │       ├── OperadorController.java
│       │   │       ├── AdminController.java
│       │   │       ├── ClimaController.java
│       │   │       └── RutaController.java
│       │   └── resources/
│       │       └── datos_prueba.txt                  ← Escenario inicial en formato plano
│       └── test/java/com/techpark/
│           └── TechParkTest.java                     ← 8 pruebas unitarias (JUnit 5)
│
└── frontend/
    ├── package.json
    ├── vite.config.js                                ← Proxy /api → localhost:8080
    ├── index.html
    └── src/
        ├── main.jsx
        ├── App.jsx                                   ← Router + Sidebar
        ├── styles/global.css
        ├── services/
        │   └── api.js                               ← Todas las llamadas HTTP al backend
        ├── components/
        │   ├── AtraccionCard.jsx
        │   ├── ZonaCard.jsx
        │   ├── ColaComponent.jsx                    ← Cola en tiempo real + procesar siguiente
        │   ├── RutaComponent.jsx                    ← Dijkstra interactivo
        │   ├── TablaEstadisticas.jsx                ← Tabla genérica configurable
        │   └── MapaInteractivo.jsx                  ← Cytoscape.js + resaltado de ruta
        └── pages/
            ├── Inicio.jsx                           ← Dashboard general del parque
            ├── PanelVisitante.jsx                   ← Registro, cola, historial, favoritos
            ├── PanelOperador.jsx                    ← Zonas, atracciones, colas, estado
            ├── PanelAdmin.jsx                       ← CRUD zonas/atracciones, clima, datos
            ├── PanelRutas.jsx                       ← Dijkstra + BFS + gestión del grafo
            ├── Estadisticas.jsx                     ← Reportes y KPIs
            └── MapaInteractivo.jsx                  ← Mapa visual del grafo con Cytoscape
```

---

## 🚀 Cómo levantar el proyecto

### Backend

```bash
cd backend
mvn clean package -q
java -jar target/tech-park-backend-1.0-SNAPSHOT.jar
# ✅ Servidor en http://localhost:8080
```

O en modo desarrollo:
```bash
mvn compile exec:java -Dexec.mainClass="com.techpark.App"
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# ✅ App en http://localhost:5173
```

---

## 🔌 Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/ping` | Health check |
| GET | `/atracciones` | Listar todas |
| POST | `/atracciones` | Crear |
| GET | `/atracciones/{id}` | Obtener por ID |
| GET | `/atracciones/buscar/{nombre}` | Buscar por nombre (ABB) |
| PUT | `/atracciones/{id}/estado` | Cambiar estado |
| PUT | `/atracciones/{id}/revision` | Registrar revisión técnica |
| DELETE | `/atracciones/{id}` | Eliminar |
| GET | `/atracciones/{id}/cola/tamanio` | Tamaño de la cola |
| GET | `/zonas` | Listar zonas |
| POST | `/zonas` | Crear zona |
| DELETE | `/zonas/{id}` | Eliminar zona |
| GET | `/visitantes` | Listar visitantes |
| POST | `/visitantes` | Registrar visitante |
| DELETE | `/visitantes/{id}` | Retirar visitante |
| POST | `/visitantes/{id}/cola/{atraccionId}` | Ingresar a cola |
| GET | `/visitantes/{id}/historial` | Historial de visitas |
| GET | `/visitantes/{id}/favoritos` | Favoritos |
| POST | `/visitantes/{id}/favoritos/{atraccionId}` | Agregar favorito |
| DELETE | `/visitantes/{id}/favoritos/{atraccionId}` | Quitar favorito |
| POST | `/visitantes/{id}/saldo` | Recargar saldo |
| GET | `/operadores` | Listar operadores |
| POST | `/operadores` | Crear operador |
| PUT | `/operadores/{id}/zona` | Asignar zona |
| DELETE | `/operadores/{id}` | Eliminar |
| POST | `/operadores/atracciones/{atraccionId}/procesar` | Procesar siguiente de la cola |
| GET | `/clima` | Estado climático actual |
| POST | `/clima/alerta` | Activar alerta (cierra ACUATICA + MECANICA_ALTURA) |
| DELETE | `/clima/alerta` | Desactivar alerta |
| GET | `/rutas/dijkstra?origen=A1&destino=A5` | Ruta óptima |
| GET | `/rutas/bfs?origen=A1` | Recorrido BFS |
| GET | `/rutas/grafo` | Grafo completo para la GUI |
| POST | `/rutas/conexion` | Conectar dos atracciones |
| GET | `/admin/reportes/top-atracciones?n=5` | Top atracciones |
| GET | `/admin/reportes/mantenimiento` | Atracciones en mantenimiento |
| GET | `/admin/reportes/cierres-clima` | Cierres por clima |
| GET | `/admin/reportes/ingreso-diario` | Ingreso diario |
| GET | `/admin/reportes/tiempo-espera-promedio` | Tiempo promedio espera |
| GET | `/admin/reportes/aforo` | Aforo actual vs máximo |
| POST | `/admin/datos/prueba` | Cargar escenario de prueba |
| POST | `/admin/datos/archivo` | Cargar desde archivo plano |

---

## 🧪 Pruebas unitarias

```bash
cd backend
mvn test
```

La suite de `backend/src/test/java/com/techpark/TechParkTest.java` cubre 10 casos unitarios independientes:
1. `ListaEnlazada` — agregar, buscar, eliminar y conservar orden.
2. `SetPropio` — evitar duplicados y mantener consistencia.
3. `ColaPrioridad` — FastPass antes que General y FIFO por nivel.
4. `ABB` — insertar, buscar, recorrido inorden y eliminación.
5. `Grafo` — BFS, Dijkstra y nodo aislado.
6. `Atraccion` — mantenimiento preventivo automático al llegar a 500 visitantes.
7. `Visitante` — historial de visitas y favoritos como estructuras separadas.
8. `ParqueService` — compra de ticket general, bloqueo de duplicados y cancelación.
9. `ParqueService` — compra de ticket familiar y cancelación del grupo completo.
10. `ParqueService` — alerta climática con registro histórico y reapertura de atracciones.

Guía detallada: [backend/TESTS.md](backend/TESTS.md)

---

## 🗂️ Formato del archivo plano

```
# Comentario
ZONA;id;nombre;capacidad
ATRACCION;id;nombre;tipo;capacidadCiclo;alturaMin;edadMin;costoAdicional;zonaId
CONEXION;idA;idB;peso
OPERADOR;id;nombre;documento;zonaId
VISITANTE;id;nombre;documento;edad;altura;saldo;tipoTicket;precioTicket
ADMIN;id;nombre;documento;clave
```

Archivo de ejemplo: `backend/src/main/resources/datos_prueba.txt`

---

## 📐 Estructuras de datos propias

| Clase | Uso en el sistema |
|-------|-------------------|
| `ListaEnlazada<T>` | Historial de visitas · Lista de operadores por zona · Lista de IDs de atracciones por zona |
| `SetPropio<T>` | Favoritos del visitante (tabla hash con encadenamiento) |
| `ColaPrioridad<T>` | Fila virtual de atracciones (min-heap, FIFO por nivel) |
| `ABB<K,V>` | Catálogo de atracciones indexado por nombre para búsqueda rápida |
| `Grafo` | Mapa físico del parque (lista de adyacencia + BFS + Dijkstra) |

---

## 👥 Roles del sistema

| Rol | Acceso |
|-----|--------|
| **Visitante** | Registro, cola virtual, historial, favoritos, ruta óptima, recarga de saldo |
| **Operador** | Gestión de atracciones de su zona, cambio de estado, revisión técnica, procesar cola |
| **Administrador** | CRUD zonas/atracciones, gestión de operadores, alertas climáticas, reportes, carga de datos |

---

## ⚙️ Tecnologías

- **Java 17** + **Javalin 6** + **Maven**
- **Gson** para serialización JSON
- **JUnit 5** para pruebas unitarias
- **React 18** + **Vite 5**
- **React Router DOM 6**
- **Cytoscape.js** para el mapa interactivo del grafo
