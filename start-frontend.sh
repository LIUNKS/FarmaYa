#!/bin/bash
# FarmaYa Frontend Server Startup Script (Linux/Mac)
# Este script inicia el servidor del frontend en el puerto 8081

echo "========================================"
echo "    FarmaYa Frontend Server"
echo "========================================"
echo ""
echo "Iniciando servidor en puerto 8081..."
echo "Presiona Ctrl+C para detener el servidor"
echo ""
echo "Accede al frontend en: http://localhost:8081"
echo ""

# Cambiar al directorio del frontend
cd frontend/farmacia-merysalud

# Iniciar servidor HTTP
python3 -m http.server 8081