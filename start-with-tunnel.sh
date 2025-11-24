#!/bin/bash

# Script para iniciar servicios con DevTunnel

echo "Iniciando servicios con Docker Compose..."
docker-compose up -d

echo "Esperando a que los servicios estén listos..."
sleep 30  # Ajusta según sea necesario

echo "Verificando que los servicios estén corriendo..."
if ! docker-compose ps | grep -q "Up"; then
    echo "Error: Los servicios no están corriendo correctamente."
    exit 1
fi

devtunnel create --name $TUNNEL_NAME 2>/dev/null || echo "Túnel ya existe o error al crear"
echo "Exponiendo puerto 8080 (backend)..."
devtunnel port create $TUNNEL_NAME --port 8080
devtunnel show $TUNNEL_NAME
echo "Creando túnel DevTunnel..."

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

# Crear túnel si no existe (si falla, mostramos advertencia pero seguimos)
# El CLI actual usa el ID posicional en lugar de --name
"$DEV_TUNNEL_BIN" create "$TUNNEL_NAME" 2>/dev/null || echo "Túnel ya existe o error al crear"

# Exponer puerto del backend (8080)
echo "Exponiendo puerto 8080 (backend)..."
"$DEV_TUNNEL_BIN" port create "$TUNNEL_NAME" -p 8080 --protocol http || echo "Error exponiendo puerto 8080"

# Opcional: Exponer puerto de la BD (3306) - NO RECOMENDADO POR SEGURIDAD
echo "Exponiendo puerto 3306 (base de datos) - ADVERTENCIA: Esto expone la BD directamente, usa solo para demo y con precaución."
"$DEV_TUNNEL_BIN" port create "$TUNNEL_NAME" -p 3306 || echo "Error exponiendo puerto 3306"

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

echo "¡Listo! Copia la URL del túnel y actualiza api-config.js en tu frontend desplegado."
echo "Ejemplo: BASE_URL: 'https://TU_URL.devtunnels.ms/api'"
echo ""
echo "Para detener: docker-compose down"
echo "Para detener el túnel: devtunnel delete $TUNNEL_NAME"