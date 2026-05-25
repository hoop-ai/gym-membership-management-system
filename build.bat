@echo off
setlocal
rem Clean
if exist bin rmdir /S /Q bin
mkdir bin
rem Compile
javac -d bin src\main\java\*.java
if errorlevel 1 (echo BUILD FAILED & exit /b 1)
rem Package
cd bin
jar cfe ..\gym.jar Main *.class
cd ..
echo Built gym.jar successfully.
