@echo off
rem Baut die App und installiert sie auf dem angeschlossenen Smartphone.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-and-install.ps1"
pause
