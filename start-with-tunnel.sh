#!/bin/bash

# Script para iniciar servicios con DevTunnel

TUNNEL_NAME="farmaya-tunnel"

# Buscar ejecutable devtunnel en PATH o en rutas comunes
DEV_TUNNEL_BIN="$(command -v devtunnel 2>/dev/null || true)"
if [ -z "$DEV_TUNNEL_BIN" ]; then
    # rutas habituales donde la extensión/instalador puede colocar el binario
    if [ -x "$HOME/.dotnet/tools/devtunnel" ]; then
        DEV_TUNNEL_BIN="$HOME/.dotnet/tools/devtunnel"
    elif [ -x "$HOME/bin/devtunnel" ]; then
        DEV_TUNNEL_BIN="$HOME/bin/devtunnel"
    fi
fi

if [ -z "$DEV_TUNNEL_BIN" ]; then
    echo "ERROR: no se encontró el ejecutable 'devtunnel'."
    echo "Instálalo desde VS Code (paleta: 'Dev Tunnels: Install CLI') o usando .NET/global tools, y asegúrate de que esté en tu PATH." 
    echo "Rutas sugeridas: ~/.dotnet/tools/ o ~/bin/"
    exit 1
fi

echo "Iniciando servicios con Docker Compose..."
docker-compose up -d

echo "Esperando a que los servicios estén listos..."
sleep 30  # Ajusta según sea necesario

echo "Verificando que los servicios estén corriendo..."
if ! docker-compose ps | grep -q "Up"; then
    echo "Error: Los servicios no están corriendo correctamente."
    exit 1
fi

echo "Creando túnel DevTunnel..."
# Crear túnel si no existe (si falla por conflicto, ya existe)
"$DEV_TUNNEL_BIN" create "$TUNNEL_NAME" 2>/dev/null || echo "Túnel ya existe o error al crear"

# Función para exponer puerto si no existe
expose_port() {
    local port=$1
    local protocol=$2
    local desc=$3
    echo "Exponiendo puerto $port ($desc)..."
    if "$DEV_TUNNEL_BIN" port show "$TUNNEL_NAME" -p "$port" >/dev/null 2>&1; then
        echo "Puerto $port ya está expuesto."
    else
        "$DEV_TUNNEL_BIN" port create "$TUNNEL_NAME" -p "$port" --protocol "$protocol" || echo "Error exponiendo puerto $port"
    fi
}

# Exponer puerto del backend (8080)
expose_port 8080 http "backend"

# Exponer puerto de la BD (3306) - ADVERTENCIA
expose_port 3306 auto "base de datos - ADVERTENCIA: Expone BD directamente, usa solo para demo"

echo "Mostrando información del túnel..."
SHOW_OUT="$($DEV_TUNNEL_BIN show "$TUNNEL_NAME" 2>&1 || true)"
if echo "$SHOW_OUT" | grep -qi "Login required"; then
    cat <<EOF
ERROR: Necesitas iniciar sesión en Dev Tunnels para ver/crear túneles.
Por favor ejecuta en otra terminal:

    $DEV_TUNNEL_BIN login

Sigue las instrucciones para autenticarte y luego vuelve a ejecutar:

    ./start-with-tunnel.sh

EOF
    exit 2
fi

echo "$SHOW_OUT"

echo "¡Listo! Tu frontend desplegado puede conectarse al backend a través del túnel."
# Extraer el Tunnel ID completo de la salida
TUNNEL_ID=$(echo "$SHOW_OUT" | grep "Tunnel ID" | sed 's/.*: //' | tr -d '\n')
echo "URLs:"
echo "  - Backend: https://$TUNNEL_ID.devtunnels.ms:8080/"
echo "  - BD (si expuesta): https://$TUNNEL_ID.devtunnels.ms:3306/"
echo ""
echo "Para detener: docker-compose down"
echo "Para detener el túnel: $DEV_TUNNEL_BIN delete $TUNNEL_NAME"