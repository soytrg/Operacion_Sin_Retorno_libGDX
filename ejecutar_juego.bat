@echo off
echo ===================================
echo  CERRANDO PROCESOS JAVA ANTERIORES
echo ===================================
taskkill /F /IM java.exe /T 2>nul
timeout /t 2 /nobreak >nul

echo.
echo ===================================
echo  EJECUTANDO JUEGO
echo ===================================
gradlew.bat :lwjgl3:run
pause