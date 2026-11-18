@echo off
setlocal
cd /d "%~dp0"
set "SAPE_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "SAPE_JAVA=%JAVA_HOME%\bin\java.exe"
"%SAPE_JAVA%" --module-path "%~dp0lib\windows" --add-modules javafx.controls -jar "%~dp0bin\sape.jar"
if errorlevel 1 (
 echo.
 echo Nao foi possivel iniciar. Instale Java 17 ou superior para Windows x64.
 echo Consulte LEIA-ME.md. Execute compilar.bat depois de alterar o codigo.
 pause
)
endlocal
