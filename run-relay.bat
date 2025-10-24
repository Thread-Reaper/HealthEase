@echo off
setlocal

set OUT_DIR=out

if not exist "%OUT_DIR%" (
  echo [Error] Build output not found in %OUT_DIR%.
  echo Run run-app.bat once to compile the project, or compile in your IDE.
  exit /b 1
)

set CP=%OUT_DIR%
if exist "lib" set CP=%CP%;lib\*

set PORT=%1
if "%PORT%"=="" set PORT=5050

echo [Run] Starting relay server on port %PORT% ...
java -cp "%CP%" com.example.healthease.relay.RelayServer %PORT%
endlocal

