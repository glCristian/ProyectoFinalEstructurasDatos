#!/bin/bash

# Función para cerrar los procesos al presionar Ctrl+C
cleanup() {
    echo ""
    echo "🛑 Deteniendo los servicios de Tech-Park..."
    # Matar todos los subprocesos de este script
    kill $(jobs -p) 2>/dev/null
    exit
}

# Capturar las señales de cierre (Ctrl+C)
trap cleanup SIGINT SIGTERM

echo "========================================================"
echo "🎢  Iniciando Tech-Park (Backend + Frontend)  "
echo "========================================================"

# 1. Iniciar Backend
echo "⏳ [1/2] Iniciando el servidor Backend (Java/Maven)..."
cd backend || exit
mvn clean compile
mvn exec:java -Dexec.mainClass="com.techpark.App" &
BACKEND_PID=$!
cd ..

echo "⏳ Esperando 5 segundos a que inicie el backend..."
sleep 5

# 2. Iniciar Frontend
echo "⏳ [2/2] Iniciando el Frontend (React/Vite)..."
cd frontend || exit
# Instalamos dependencias de forma silenciosa por si acaso
npm install --silent 
npm run dev &
FRONTEND_PID=$!
cd ..

echo "========================================================"
echo "✅ ¡Todo está corriendo exitosamente!"
echo "🔹 Frontend (Interfaz): http://localhost:5173/login"
echo "🔹 Backend (API):       http://localhost:8080"
echo ""
echo "👉 Presiona Ctrl+C en esta terminal para detener ambos y salir."
echo "========================================================"

# Esperar a que el usuario presione Ctrl+C
wait
