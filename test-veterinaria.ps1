# ============================================================
# test-veterinaria.ps1  (version PowerShell del script de la rubrica)
#
# Mismas pruebas que test-veterinaria.sh, pero sin depender de jq/curl:
# usa System.Net.Http.HttpClient. Salvo que se indique otro puerto, usa el
# API GATEWAY en 8080. La prueba de estres usa ForEach-Object -Parallel
# (no requiere ApacheBench).
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File .\test-veterinaria.ps1
#   $env:BASE_URL="http://localhost:8090/api/v1"; .\test-veterinaria.ps1
# ============================================================

$ErrorActionPreference = "Continue"
Add-Type -AssemblyName System.Net.Http

$BASE = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080/api/v1" }
$ADMIN_EMAIL   = "admin@veterinaria.com"
$ADMIN_PASS    = "Admin123*"
$CLIENTE_EMAIL = "dueno@veterinaria.com"
$CLIENTE_PASS  = "Cliente123*"

$http = New-Object System.Net.Http.HttpClient

function Send-Json($method, $url, $bodyJson, $token) {
    $req = New-Object System.Net.Http.HttpRequestMessage
    $req.Method = New-Object System.Net.Http.HttpMethod($method)
    $req.RequestUri = [System.Uri]$url
    if ($token) { $req.Headers.Add("Authorization", "Bearer $token") }
    if ($null -ne $bodyJson) {
        $req.Content = New-Object System.Net.Http.StringContent($bodyJson, [System.Text.Encoding]::UTF8, "application/json")
    }
    $resp = $http.SendAsync($req).GetAwaiter().GetResult()
    $txt = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    return [pscustomobject]@{ Status = [int]$resp.StatusCode; Body = $txt }
}

Write-Host "INICIANDO PRUEBAS DE VETERINARIA" -ForegroundColor Cyan

# 1. Registro de CLIENTE
$reg = Send-Json "POST" "$BASE/auth/register" (@{
    nombre = "Pedro Armas"; telefono = "55551234"; email = $CLIENTE_EMAIL; password = $CLIENTE_PASS
} | ConvertTo-Json) $null
Write-Host $reg.Body

# 2. Login ADMIN
$adminResp = Send-Json "POST" "$BASE/auth/login" (@{ email = $ADMIN_EMAIL; password = $ADMIN_PASS } | ConvertTo-Json) $null
$ADMIN_TOKEN = ($adminResp.Body | ConvertFrom-Json).token

# 3. Login CLIENTE
$cliResp = Send-Json "POST" "$BASE/auth/login" (@{ email = $CLIENTE_EMAIL; password = $CLIENTE_PASS } | ConvertTo-Json) $null
$CLIENTE_TOKEN = ($cliResp.Body | ConvertFrom-Json).token

# 4. Registro de Mascota
$mascResp = Send-Json "POST" "$BASE/mascotas" (@{
    nombre = "Zafir"; especie = "PERRO"; raza = "Beagle"; edad = 3
} | ConvertTo-Json) $CLIENTE_TOKEN
$MASCOTA_ID = ($mascResp.Body | ConvertFrom-Json).id

# 5. Test 403: CLIENTE intenta crear expediente
$exp = Send-Json "POST" "$BASE/expedientes" (@{
    citaId = 1; diagnostico = "Consulta general"; tratamiento = "Desparasitante"; pesoKg = 12.5
} | ConvertTo-Json) $CLIENTE_TOKEN
if ($exp.Status -eq 403) {
    Write-Host "Seguridad Validada: 403 Forbidden correctamente." -ForegroundColor Green
} else {
    Write-Host "Seguridad NO validada: se esperaba 403 y se obtuvo $($exp.Status)" -ForegroundColor Red
}

# 6. Prueba de estres: 100 peticiones, 10 en paralelo (sin ab)
Write-Host "Ejecutando prueba de estres (100 peticiones, 10 en paralelo)..." -ForegroundColor Yellow
$urlAgenda = "$BASE/citas/agenda?fecha=2026-10-06"
$codes = 1..100 | ForEach-Object -Parallel {
    $h = New-Object System.Net.Http.HttpClient
    $r = New-Object System.Net.Http.HttpRequestMessage
    $r.Method = New-Object System.Net.Http.HttpMethod("GET")
    $r.RequestUri = [System.Uri]$using:urlAgenda
    $r.Headers.Add("Authorization", "Bearer $using:ADMIN_TOKEN")
    try { return [int]$h.SendAsync($r).GetAwaiter().GetResult().StatusCode }
    catch { return 0 }
} -ThrottleLimit 10

$codes | Group-Object | Sort-Object Name | ForEach-Object {
    Write-Host ("{0} {1}" -f $_.Count, $_.Name)
}

Write-Host "PRUEBAS COMPLETADAS"
