# Tech-Park UQ

An end-to-end amusement park management system with a Java backend and a React frontend. The project combines business logic, custom data structures, file-based persistence, and a graphical interface focused on operations, administration, and route exploration.

## Overview

Tech-Park UQ is designed to simulate and manage the operations of a theme park. It supports attractions, zones, visitors, operators, virtual queues, internal routes, reports, and weather alerts. The application is started from a single script: `run.sh`.

The backend exposes a REST API and centralizes the system logic. The frontend consumes that API and presents role-specific panels.

## How to Run

The recommended way to start the entire system is:

```bash
./run.sh
```

The script launches both the backend and the frontend in the same execution. Once the startup completes, the application is available at:

- Frontend: `http://localhost:5173/login`
- Backend: `http://localhost:8080`

To stop both services, press `Ctrl+C` in the terminal where the script is running.

## Project Architecture

```text
ProyectoFinalEstructurasDatos/
├── backend/
│   ├── pom.xml
│   ├── data/
│   │   ├── admins.json
│   │   ├── atracciones.json
│   │   ├── cierres_clima.json
│   │   ├── operadores.json
│   │   ├── visitantes.json
│   │   └── zonas.json
│   └── src/main/java/com/techpark/
│       ├── App.java
│       ├── controller/
│       ├── model/
│       ├── service/
│       ├── structures/
│       └── util/
└── frontend/
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── components/
        ├── context/
        ├── pages/
        ├── services/
        └── styles/
```

## Backend

The backend is built with **Java 17**, **Javalin**, and **Maven**. Its main responsibility is to expose the API, apply the business rules, and coordinate access to system data.

### Main layers

- `controller`: receives HTTP requests and translates them into domain operations.
- `service`: concentrates park logic, queue management, routes, data loading, and validation.
- `model`: defines the domain entities such as visitors, attractions, operators, zones, and tickets.
- `structures`: contains the manually implemented data structures used to solve project-specific needs.
- `util`: groups support utilities, mainly for serialization and data handling.

### Persistence and data handling

This project does not use an external relational database. Current persistence is handled through JSON files located in `backend/data/`.

This approach makes it possible to:

- store information locally in a simple way;
- load initial data without depending on a database engine;
- keep traceability for entities such as visitors, operators, zones, attractions, and weather closures;
- make the system easy to run in academic or demo environments.

In addition, the backend includes both a test scenario loader and flat-file reading support for system initialization or base data restoration.

### Custom data structures

A central part of the project is the manual implementation of data structures in `backend/src/main/java/com/techpark/structures/`. These structures do not rely on `java.util` as the primary solution for the system’s critical use cases.

- `ListaEnlazada`: visit history, operators by zone, and auxiliary lists.
- `SetPropio`: visitor favorites, avoiding duplicates through a chained hash table.
- `ColaPrioridad`: virtual queue management with priority by ticket type and FIFO order within each level.
- `ABB`: efficient attraction search by name through a binary search tree.
- `Grafo`: internal park connections for BFS and Dijkstra route calculation.

### Key business rules

- queue control and priority-based service;
- operator assignment to zones;
- attraction state changes;
- preventive maintenance and weather closures;
- optimal internal route calculation;
- operational report generation.

## Frontend

The frontend is built with **React 18** and **Vite**. Its purpose is to provide a clear interface for each user type and consume the backend API in a decoupled way.

### Graphical interface

The application provides a visual experience divided by role:

- visitor panel for registration, favorites, history, balance, and queues;
- operator panel for attraction operations and state management;
- administrator panel for general maintenance, reporting, and data loading;
- routes module and interactive map for visualizing park connections.

The interactive map uses **Cytoscape.js** to represent the park graph and highlight calculated routes.

## Technologies Used

- Java 17
- Javalin 6
- Maven
- Gson
- JUnit 5
- React 18
- Vite 5
- React Router DOM 6
- Cytoscape.js

## Testing

The backend test suite validates the behavior of the custom structures and several park business rules.

```bash
cd backend
mvn test
```

Detailed test documentation is available in [backend/TESTS.md](backend/TESTS.md).

## Input Data Format

The project supports flat-file data loading for system initialization.

```text
# Comment
ZONE;id;name;capacity
ATTRACTION;id;name;type;cycleCapacity;minHeight;minAge;extraCost;zoneId
CONNECTION;idA;idB;weight
OPERATOR;id;name;document;zoneId
VISITOR;id;name;document;age;height;balance;ticketType;ticketPrice
ADMIN;id;name;document;password
```

Reference file: [backend/src/main/resources/datos_prueba.txt](backend/src/main/resources/datos_prueba.txt)

## Implemented Structures

| Structure | Main use |
|---|---|
| `ListaEnlazada<T>` | Dynamic collections and visit history |
| `SetPropio<T>` | Deduplicated favorites |
| `ColaPrioridad<T>` | Priority-based virtual queue |
| `ABB<K, V>` | Ordered attraction lookup |
| `Grafo` | Internal navigation and route calculation |

## System Roles

| Role | Capabilities |
|---|---|
| Visitor | Registration, virtual queue, history, favorites, optimal route, balance top-up |
| Operator | Management of attractions in their zone, state control, technical review, and queue processing |
| Administrator | CRUD for zones and attractions, operator management, weather alerts, reports, and data loading |

## REST API

The backend exposes endpoints for:

- authentication and role-based access;
- management of zones, attractions, visitors, and operators;
- queue and ticket administration;
- weather consultation and preventive closures;
- route calculation with the internal graph;
- operational reports and data loading.

## Frontend Structure

Relevant components and pages:

- `components/`: cards, queues, routes, tables, interactive map, and layouts;
- `pages/`: home screen, login, role panels, statistics, and map views;
- `services/api.js`: single HTTP consumption layer for the backend;
- `context/AuthContext.jsx`: global authentication state.

## Final Notes

- The project is intended to run locally with a simple startup flow.
- Persistence is file-based rather than database-driven.
- The custom data structures are a core part of the academic and functional design of the system.
