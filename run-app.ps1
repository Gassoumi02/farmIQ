# run-app.ps1
# Script de lancement simple pour FarmIQ

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   FarmIQ - Lancement Application         ║" -ForegroundColor Green  
Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# Aller dans le répertoire du projet
Set-Location $PSScriptRoot

# Chercher Maven
$mavenPaths = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files (x86)\apache-maven\bin\mvn.cmd",
    "mvn"
)

$mvnCmd = $null
foreach ($path in $mavenPaths) {
    if ($path -eq "mvn") {
        try {
            $null = Get-Command mvn -ErrorAction Stop
            $mvnCmd = "mvn"
            Write-Host "✅ Maven trouvé dans le PATH" -ForegroundColor Green
            break
        } catch {
            continue
        }
    } elseif (Test-Path $path) {
        $mvnCmd = $path
        Write-Host "✅ Maven trouvé: IntelliJ" -ForegroundColor Green
        break
    }
}

if (-not $mvnCmd) {
    Write-Host "❌ Maven non trouvé !" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions:" -ForegroundColor Yellow
    Write-Host "  1. Installez Maven: https://maven.apache.org/download.cgi" -ForegroundColor Gray
    Write-Host "  2. Ou utilisez IntelliJ: View > Tool Windows > Maven" -ForegroundColor Gray
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "🚀 Compilation et lancement..." -ForegroundColor Cyan
Write-Host ""

# Lancer l'application
if ($mvnCmd -eq "mvn") {
    & mvn clean javafx:run
} else {
    & $mvnCmd clean javafx:run
}

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Application fermée normalement" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors de l'exécution" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez:" -ForegroundColor Yellow
    Write-Host "  - MySQL est démarré" -ForegroundColor Gray
    Write-Host "  - La base 'farmiq' existe" -ForegroundColor Gray
    Write-Host "  - config.properties est configuré" -ForegroundColor Gray
}

Write-Host ""
pause
