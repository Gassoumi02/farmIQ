@echo off
REM Script de lancement ameliore pour FarmIQ

echo ========================================
echo   FarmIQ - User Management System
echo   Version Corrigee
echo ========================================
echo.

cd /d "%~dp0"

set MODE=%1
if "%MODE%"=="" set MODE=main

if "%MODE%"=="verify" (
    echo [INFO] Verification du systeme...
    powershell -ExecutionPolicy Bypass -File verify-setup.ps1
    goto :end
)

set MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set PATH=%MAVEN_HOME%\bin;%PATH%
)

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Maven non trouve. Utilisez IntelliJ Maven.
    pause
    exit /b 1
)

if "%MODE%"=="main" (
    echo [INFO] Lancement de l'interface...
    call mvn clean javafx:run
) else if "%MODE%"=="test" (
    echo [INFO] Tests automatiques...
    call mvn exec:java -Dexec.mainClass="com.farmiq.TestMain"
) else if "%MODE%"=="quick" (
    echo [INFO] Menu interactif...
    call mvn exec:java -Dexec.mainClass="com.farmiq.QuickTestMain"
)

:end
pause
