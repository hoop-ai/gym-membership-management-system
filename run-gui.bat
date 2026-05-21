@echo off
REM Double-click this file to launch the Gym Membership Manager GUI.
cd /d "%~dp0"
java -jar GymManagerGUI.jar
if errorlevel 1 (
    echo.
    echo Could not launch the GUI. Make sure Java 8 or newer is installed.
    echo Try running:  java -version
    pause
)
