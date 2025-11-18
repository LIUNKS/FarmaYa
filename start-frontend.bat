@echo off
echo ========================================
echo    FarmaYa Frontend Server
echo ========================================
echo.
echo Iniciando servidor en puerto 8081...
echo Presiona Ctrl+C para detener el servidor
echo.
echo Accede al frontend en: http://localhost:8081
echo.

cd frontend\farmacia-merysalud
python -m http.server 8081

pause