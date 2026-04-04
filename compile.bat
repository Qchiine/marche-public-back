@echo off
REM Script de compilation du projet marches-backend
REM Utilise le Maven wrapper

echo ========================================
echo Compilation du projet marches-backend
echo ========================================
echo.

REM Aller au répertoire du projet
cd /d C:\Users\DELL\Desktop\marche-public-back0

echo Cleaning and compiling...
echo.

REM Exécuter le clean compile
call mvnw.cmd clean compile

REM Vérifier le résultat
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESS! ✅
    echo ========================================
    pause
) else (
    echo.
    echo ========================================
    echo BUILD FAILED! ❌
    echo ========================================
    pause
)
