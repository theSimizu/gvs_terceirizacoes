@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\Start-GvsDesktop.ps1"
set "gvsExitCode=%errorlevel%"
if not "%gvsExitCode%"=="0" (
    echo.
    echo O GVS nao foi iniciado. Confira a orientacao acima.
    pause
)
popd
exit /b %gvsExitCode%
