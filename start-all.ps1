# ============================================================
# start-all.ps1
# Levanta los 4 microservicios y el API GATEWAY.
#
# Uso (desde la raiz del proyecto):
#   powershell -ExecutionPolicy Bypass -File .\start-all.ps1
#   powershell -ExecutionPolicy Bypass -File .\start-all.ps1 -GatewayPort 8090
#
# Requisitos: haber compilado antes (mvn clean install -DskipTests) y tener
# PostgreSQL corriendo con las 4 bases de datos creadas (ver reset-db.ps1).
# ============================================================

param(
    [int]$GatewayPort = 8080
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$logs = Join-Path $env:TEMP "veterinaria-logs"
New-Item -ItemType Directory -Force -Path $logs | Out-Null

# Ruta directa al java.exe del JDK (evita el shim de javapath).
$java = "C:\Program Files\Java\jdk-21\bin\java.exe"
if (-not (Test-Path $java)) { $java = "java" }

$servicios = @("auth-usuario", "mascota-service", "cita-service", "expediente-service")
foreach ($s in $servicios) {
    $jar = Join-Path $root "$s\target\$s-1.0.0.jar"
    if (-not (Test-Path $jar)) {
        Write-Host "Falta $jar. Compila primero: mvn clean install -DskipTests" -ForegroundColor Red
        exit 1
    }
    Start-Process -FilePath $java -ArgumentList "-jar", $jar `
        -RedirectStandardOutput (Join-Path $logs "$s.out.log") `
        -RedirectStandardError  (Join-Path $logs "$s.err.log") `
        -WindowStyle Hidden
    Write-Host "Iniciado $s" -ForegroundColor Green
}

$gw = Join-Path $root "api-gateway\target\api-gateway-1.0.0.jar"
Start-Process -FilePath $java -ArgumentList "-jar", $gw, "--server.port=$GatewayPort" `
    -RedirectStandardOutput (Join-Path $logs "api-gateway.out.log") `
    -RedirectStandardError  (Join-Path $logs "api-gateway.err.log") `
    -WindowStyle Hidden
Write-Host "Iniciado api-gateway en el puerto $GatewayPort" -ForegroundColor Green

Write-Host "Esperando 40 segundos a que arranquen los servicios..." -ForegroundColor Yellow
Start-Sleep -Seconds 40

$puertos = @(8081, 8082, 8083, 8084, $GatewayPort)
foreach ($p in $puertos) {
    $listo = Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue
    if ($listo) { Write-Host "Puerto $p ESCUCHANDO" -ForegroundColor Green }
    else        { Write-Host "Puerto $p NO responde (revisa $logs)" -ForegroundColor Red }
}
