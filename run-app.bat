@echo off
setlocal ENABLEDELAYEDEXPANSION

rem --- Configuration ---
if "%JAVA_FX%"=="" (
  echo [Error] JAVA_FX environment variable not set.
  echo Set it to your JavaFX SDK lib folder. Example:
  echo   setx JAVA_FX "C:\^\javafx-sdk-17\lib"
  echo Then reopen the terminal and run this script again.
  exit /b 1
)

set SRC_DIR=src\main\java
set OUT_DIR=out
set LIB_DIR=lib

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

rem Build sources list
dir /s /b "%SRC_DIR%\*.java" > sources.txt

rem Compose module path (JavaFX + optional lib jars)
set MOD_PATH=%JAVA_FX%
if exist "%LIB_DIR%" set MOD_PATH=%MOD_PATH%;%LIB_DIR%

rem Compile
echo [Compile] Using module-path: %MOD_PATH%
javac --module-path "%MOD_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -d "%OUT_DIR%" @sources.txt
if errorlevel 1 (
  echo [Error] Compilation failed.
  exit /b 1
)

rem Run application
set CP=%OUT_DIR%
if exist "%LIB_DIR%" set CP=%CP%;%LIB_DIR%\*

echo [Run] Launching HealthEase app...
java --module-path "%JAVA_FX%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "%CP%" com.example.healthease.controllers.HelloApplication
endlocal

