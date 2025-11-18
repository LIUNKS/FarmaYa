# FarmaYa Frontend Server Startup Script
# Este script inicia el servidor del frontend en el puerto 8081

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    FarmaYa Frontend Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Iniciando servidor en puerto 8081..." -ForegroundColor Green
Write-Host "Presiona Ctrl+C para detener el servidor" -ForegroundColor Yellow
Write-Host ""
Write-Host "Accede al frontend en: http://localhost:8081" -ForegroundColor Green
Write-Host ""

# Cambiar al directorio del frontend
Set-Location "frontend\farmacia-merysalud"

# Iniciar servidor HTTP
python -m http.server 8081