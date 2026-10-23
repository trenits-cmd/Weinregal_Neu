# Legt eine Verknuepfung "Weinregal aufs Handy" auf dem Desktop an.
# Danach genuegt ein Doppelklick: bauen, installieren, App starten.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$target = Join-Path $root "Auf-Handy-installieren.cmd"

if (-not (Test-Path $target)) {
    Write-Host "Startdatei nicht gefunden: $target" -ForegroundColor Red
    exit 1
}

$desktop = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktop "Weinregal aufs Handy.lnk"

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $target
$shortcut.WorkingDirectory = $root
$shortcut.IconLocation = "%SystemRoot%\System32\shell32.dll,220"
$shortcut.Description = "Weinregal bauen und auf das Smartphone installieren"
$shortcut.Save()

Write-Host "Verknuepfung erstellt: $shortcutPath" -ForegroundColor Green
Write-Host "Doppelklick startet Build + Installation."
