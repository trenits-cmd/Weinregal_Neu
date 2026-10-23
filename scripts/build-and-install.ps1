# Baut die Debug-APK und installiert sie direkt auf dem angeschlossenen Smartphone.
# Voraussetzungen:
#   - Smartphone per USB angeschlossen, USB-Debugging aktiviert
#   - Android SDK / adb vorhanden
#   - Android Studio installiert (liefert das benoetigte Java)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "== Weinregal aufs Handy ==" -ForegroundColor Cyan

# --- Java finden -----------------------------------------------------------
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaCandidates = @(
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Android\Android Studio1\jbr",
        "$env:LOCALAPPDATA\Programs\Android Studio\jbr"
    )
    foreach ($candidate in $javaCandidates) {
        if (Test-Path "$candidate\bin\java.exe") {
            $env:JAVA_HOME = $candidate
            break
        }
    }
}

if (-not $env:JAVA_HOME) {
    Write-Host "Kein Java gefunden. Bitte Android Studio installieren oder JAVA_HOME setzen." -ForegroundColor Red
    exit 1
}

# --- adb finden ------------------------------------------------------------
function Find-Adb {
    $candidates = @()
    if ($env:ANDROID_HOME) { $candidates += "$env:ANDROID_HOME\platform-tools\adb.exe" }
    if ($env:ANDROID_SDK_ROOT) { $candidates += "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" }
    $candidates += "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path "$root\local.properties") {
        $sdkLine = Get-Content "$root\local.properties" | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) {
            $sdk = $sdkLine.Substring("sdk.dir=".Length)
            $candidates += "$sdk\platform-tools\adb.exe"
        }
    }
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) { return $candidate }
    }
    $adbInPath = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbInPath) { return $adbInPath.Source }
    return $null
}

$adb = Find-Adb
if (-not $adb) {
    Write-Host "adb nicht gefunden. Bitte Android SDK Platform-Tools installieren." -ForegroundColor Red
    exit 1
}
Write-Host "adb: $adb"

# --- APK bauen -------------------------------------------------------------
Write-Host "Baue APK ..." -ForegroundColor Cyan
& "$root\gradlew.bat" :app:assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build fehlgeschlagen." -ForegroundColor Red
    exit $LASTEXITCODE
}

$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) {
    Write-Host "APK nicht gefunden: $apk" -ForegroundColor Red
    exit 1
}

# --- Geraet pruefen --------------------------------------------------------
$devices = & $adb devices | Select-String -Pattern "device$" | Where-Object { $_ -notmatch "^List of devices" }
if (-not $devices) {
    Write-Host "Kein Smartphone gefunden." -ForegroundColor Yellow
    Write-Host "Bitte per USB anschliessen, USB-Debugging aktivieren und entsperren."
    Write-Host "Danach dieses Skript erneut starten."
    Start-Process explorer.exe $apk
    exit 1
}

# --- Installieren ----------------------------------------------------------
Write-Host "Installiere auf dem Smartphone ..." -ForegroundColor Cyan
& $adb install -r $apk
if ($LASTEXITCODE -ne 0) {
    Write-Host "Installation fehlgeschlagen." -ForegroundColor Red
    exit $LASTEXITCODE
}

& $adb shell am start -n "com.example.weinregal_neu/.MainActivity" | Out-Null

Write-Host "Fertig. Weinregal startet auf dem Smartphone." -ForegroundColor Green
Write-Host "APK: $apk"
