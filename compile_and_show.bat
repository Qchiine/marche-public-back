@echo off
REM Script de compilation du projet marches-backend
REM Corrige l'erreur d'encodage dans application.properties

title Compilation marches-backend
cd /d C:\Users\DELL\Desktop\marche-public-back0

echo.
echo ========================================
echo COMPILATION marches-backend
echo ========================================
echo.
echo Lancement: mvnw.cmd clean compile
echo.

REM Exécuter le clean compile avec sortie vers fichier pour voir les erreurs
call mvnw.cmd clean compile > build_output.txt 2>&1

REM Afficher le résultat
type build_output.txt

REM Vérifier le résultat
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [SUCCESS] BUILD REUSSI! ✅
    echo ========================================
    echo.
    echo Prochaine etape: mvnw.cmd spring-boot:run
    echo.
) else (
    echo.
    echo ========================================
    echo [ERREUR] BUILD A ECHOUE! ❌
    echo ========================================
    echo.
)

pause
