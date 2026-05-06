@echo off
echo ===================================================
echo Starting UniEvents Spring Boot Application...
echo ===================================================

cd /d "D:\FSD 2\FSAD"

:: Start the application in a separate terminal window so we can view logs
start "UniEvents Server" cmd /k ".\mvnw.cmd spring-boot:run"

echo.
echo Waiting 12 seconds for the server to wake up...
timeout /t 12 /nobreak > nul

echo.
echo Opening localhost in your default browser...
start http://localhost:8080
