@echo off
echo ======================================
echo  Systeme de Gestion RH - JavaFX
echo ======================================
echo.

REM Set JavaFX path
set JAVAFX_PATH=C:\javafx-sdk-21.0.9\lib

echo Verification de JavaFX...
if not exist "%JAVAFX_PATH%" (
    echo ERREUR: JavaFX SDK non trouve a %JAVAFX_PATH%
    echo Veuillez installer JavaFX SDK ou modifier le chemin dans ce script.
    pause
    exit /b 1
)

echo JavaFX trouve: %JAVAFX_PATH%
echo.

echo Compilation du projet avec Maven...
call mvn clean compile
if errorlevel 1 (
    echo ERREUR: Echec de la compilation
    pause
    exit /b 1
)

echo.
echo Lancement de l'application...
echo.

call mvn javafx:run

pause
