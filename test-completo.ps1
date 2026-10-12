# ============================================================
# test-veterinaria.ps1
# Pruebas funcionales end-to-end del Sistema de Control de Citas
# para Veterinaria (microservicios + PostgreSQL).
#
# Uso:
#   1) Tener PostgreSQL corriendo, los 4 microservicios (8081..8084) y el API
#      GATEWAY (8080) levantados. Ver start-all.ps1.
#   2) Ejecutar:  powershell -ExecutionPolicy Bypass -File .\test-veterinaria.ps1
#      (o con otro puerto:  $env:BASE_URL='http://localhost:8090'; .\test-veterinaria.ps1)
# ============================================================

$ErrorActionPreference = "Continue"

# Todas las peticiones pasan por el API GATEWAY (puerto 8080), que enruta a cada
# microservicio. Se puede cambiar el puerto con la variable de entorno BASE_URL.
$BASE = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }

# Nota: PowerShell NO distingue mayusculas/minusculas en variables, por eso usamos
# nombres de URL distintos ($URL_...) y no chocan con las respuestas ($expReg, etc.).
# El gateway expone los 4 microservicios bajo el mismo host:puerto.
$URL_AUTH = $BASE
$URL_MASC = $BASE
$URL_CITA = $BASE
$URL_EXP  = $BASE

$script:pass = 0
$script:fail = 0

function Check($nombre, $esperado, $obtenido) {
    if ("$esperado" -eq "$obtenido") {
        Write-Host ("[OK]   {0}  (esperado {1})" -f $nombre, $esperado) -ForegroundColor Green
        $script:pass++
    } else {
        Write-Host ("[FAIL] {0}  (esperado {1}, obtenido {2})" -f $nombre, $esperado, $obtenido) -ForegroundColor Red
        $script:fail++
    }
}

# Usamos System.Net.Http.HttpClient: devuelve SIEMPRE el codigo de estado HTTP,
# incluso en respuestas de error (400/401/403/404/409), sin lanzar excepciones.
Add-Type -AssemblyName System.Net.Http
$script:http = New-Object System.Net.Http.HttpClient

function Invoke-Api {
    param($Method, $Url, $Body = $null, $Token = $null)
    $req = New-Object System.Net.Http.HttpRequestMessage
    $req.Method = New-Object System.Net.Http.HttpMethod($Method)
    $req.RequestUri = [System.Uri]$Url
    if ($Token) { $req.Headers.Add("Authorization", "Bearer $Token") }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 10
        $req.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, "application/json")
    }
    try {
        $resp = $script:http.SendAsync($req).GetAwaiter().GetResult()
        $content = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $b = $null
        if ($content) { try { $b = $content | ConvertFrom-Json } catch { $b = $content } }
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Body = $b }
    } catch {
        return [pscustomobject]@{ Status = 0; Body = $_.Exception.Message }
    }
}

function Login($email, $password) {
    $r = Invoke-Api -Method "POST" -Url "$URL_AUTH/api/v1/auth/login" -Body @{ email = $email; password = $password }
    return $r
}

function FechaFutura($dias, $hora) {
    return (Get-Date).Date.AddDays($dias).AddHours($hora).ToString("yyyy-MM-ddTHH:mm:ss")
}

# Dia futuro aleatorio (2..21): asi el script puede ejecutarse varias veces sin
# chocar con el limite de 2 citas PENDIENTES por cliente y por dia.
$dias = Get-Random -Minimum 2 -Maximum 21

Write-Host "`n=== 1. SEGURIDAD / AUTENTICACION ===" -ForegroundColor Cyan

$admin = Login "admin@veterinaria.com" "Admin123*"
Check "Login ADMIN" 200 $admin.Status
$adminToken = $admin.Body.token

$vet = Login "vet@veterinaria.com" "Vet123*"
Check "Login VET" 200 $vet.Status
$vetToken = $vet.Body.token

$cli = Login "cliente@veterinaria.com" "Cliente123*"
Check "Login CLIENTE" 200 $cli.Status
$cliToken = $cli.Body.token
$cliId = $cli.Body.usuario.id

$bad = Login "admin@veterinaria.com" "clave-mala"
Check "Login con clave incorrecta -> 401" 401 $bad.Status

$noToken = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/mis-mascotas"
Check "Endpoint protegido sin token -> 401" 401 $noToken.Status

$badToken = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/mis-mascotas" -Token "token.invalido.xxx"
Check "Token invalido -> 401" 401 $badToken.Status

Write-Host "`n=== 2. REGISTRO PUBLICO (no puede escalar rol) ===" -ForegroundColor Cyan
$rand = Get-Random
$newEmail = "nuevo$rand@test.com"
$reg = Invoke-Api -Method "POST" -Url "$URL_AUTH/api/v1/auth/register" -Body @{
    nombre = "Nuevo Cliente"; telefono = "555-9999"; email = $newEmail
    password = "secreto123"; rol = "ADMIN"
}
Check "Registro publico -> 201" 201 $reg.Status
Check "Rol forzado a CLIENTE (ignora ADMIN del body)" "CLIENTE" $reg.Body.rol
$newCliToken = (Login $newEmail "secreto123").Body.token

$dup = Invoke-Api -Method "POST" -Url "$URL_AUTH/api/v1/auth/register" -Body @{
    nombre = "Duplicado"; email = $newEmail; password = "secreto123"
}
Check "Email duplicado -> 409" 409 $dup.Status

$inv = Invoke-Api -Method "POST" -Url "$URL_AUTH/api/v1/auth/register" -Body @{
    nombre = ""; email = "correo-malo"; password = "123"
}
Check "Datos invalidos -> 400" 400 $inv.Status

Write-Host "`n=== 3. MASCOTAS ===" -ForegroundColor Cyan
$mascota = Invoke-Api -Method "POST" -Url "$URL_MASC/api/v1/mascotas" -Token $cliToken -Body @{
    nombre = "Firulais"; especie = "PERRO"; raza = "Labrador"; edad = 3
}
Check "CLIENTE registra mascota -> 201" 201 $mascota.Status
Check "Mascota asignada al cliente del token" $cliId $mascota.Body.clienteId
$mascotaId = $mascota.Body.id

$mis = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/mis-mascotas" -Token $cliToken
Check "GET mis-mascotas -> 200" 200 $mis.Status
Check "mis-mascotas devuelve al menos 1" $true ([bool](@($mis.Body).Count -ge 1))

$vetVe = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/$mascotaId" -Token $vetToken
Check "VET consulta mascota -> 200" 200 $vetVe.Status

$cliVe = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/$mascotaId" -Token $cliToken
Check "CLIENTE intenta GET /mascotas/{id} -> 403" 403 $cliVe.Status

# Mascota de otro cliente (accede a la misma mascota por su token pero endpoint propio)
$otrasMis = Invoke-Api -Method "GET" -Url "$URL_MASC/api/v1/mascotas/mis-mascotas" -Token $newCliToken
Check "Otro cliente no ve la mascota ajena" 0 @($otrasMis.Body | Where-Object { $_.id -eq $mascotaId }).Count

Write-Host "`n=== 4. CITAS - REGLAS DE NEGOCIO ===" -ForegroundColor Cyan
$vetId = $vet.Body.usuario.id

# 4a. Cita valida
$f1 = FechaFutura $dias 10
$c1 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = $f1; motivo = "Vacunacion"
}
Check "Cita valida -> 201" 201 $c1.Status
$citaId = $c1.Body.id

# 4b. Cruce de horario (10:15 solapa 10:00)
$c2 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = (FechaFutura $dias 10).Replace("10:00:00", "10:15:00"); motivo = "Solape"
}
Check "Veterinario ocupado (10:15 con 10:00) -> 409" 409 $c2.Status

# 4c. Cita consecutiva 10:30 -> valida
$c3 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = (FechaFutura $dias 10).Replace("10:00:00", "10:30:00"); motivo = "Revision"
}
Check "Cita consecutiva 10:30 -> 201" 201 $c3.Status

# 4d. Tercera cita pendiente del mismo dia -> 409 (limite 2)
$c4 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = (FechaFutura $dias 10).Replace("10:00:00", "11:00:00"); motivo = "Tercera"
}
Check "Tercera cita pendiente del dia -> 409" 409 $c4.Status

# 4e. Fecha pasada -> 400
$c5 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = (FechaFutura -1 10); motivo = "Pasada"
}
Check "Fecha pasada -> 400" 400 $c5.Status

# 4f. Mascota inexistente -> 404
$c6 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $cliToken -Body @{
    mascotaId = 999999; veterinarioId = $vetId; fechaHora = (FechaFutura 2 9); motivo = "Inexistente"
}
Check "Mascota inexistente -> 404" 404 $c6.Status

Write-Host "`n=== 5. CANCELACION (regla 2 horas) ===" -ForegroundColor Cyan
# Cita a 90 minutos -> no se puede cancelar
$f90 = (Get-Date).AddMinutes(90).ToString("yyyy-MM-ddTHH:mm:ss")
$c90 = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $newCliToken -Body @{
    mascotaId = $mascotaId; veterinarioId = $vetId; fechaHora = $f90; motivo = "Pronta"
}
# Nota: la mascota es del otro cliente; el nuevo cliente no puede agendar -> 403
Check "Cliente no puede agendar en mascota ajena -> 403" 403 $c90.Status

$otherMasc = Invoke-Api -Method "POST" -Url "$URL_MASC/api/v1/mascotas" -Token $newCliToken -Body @{
    nombre = "Michu"; especie = "GATO"; raza = "Siames"; edad = 2
}
$otherMascId = $otherMasc.Body.id
$c90b = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $newCliToken -Body @{
    mascotaId = $otherMascId; veterinarioId = $vetId; fechaHora = $f90; motivo = "Pronta"
}
Check "Cita a 90 min -> 201" 201 $c90b.Status
$canc90 = Invoke-Api -Method "PATCH" -Url "$URL_CITA/api/v1/citas/$($c90b.Body.id)/cancelar" -Token $newCliToken
Check "Cancelar con menos de 2 horas -> 400" 400 $canc90.Status

# Cita a 5 horas -> se puede cancelar
$f5h = (Get-Date).AddHours(5).ToString("yyyy-MM-ddTHH:mm:ss")
$c5h = Invoke-Api -Method "POST" -Url "$URL_CITA/api/v1/citas" -Token $newCliToken -Body @{
    mascotaId = $otherMascId; veterinarioId = $vetId; fechaHora = $f5h; motivo = "Lejana"
}
Check "Cita a 5 horas -> 201" 201 $c5h.Status
$canc5h = Invoke-Api -Method "PATCH" -Url "$URL_CITA/api/v1/citas/$($c5h.Body.id)/cancelar" -Token $newCliToken
Check "Cancelar con mas de 2 horas -> 200" 200 $canc5h.Status
Check "Estado CANCELADA" "CANCELADA" $canc5h.Body.estado

$cancAjena = Invoke-Api -Method "PATCH" -Url "$URL_CITA/api/v1/citas/$citaId/cancelar" -Token $newCliToken
Check "Cliente cancela cita ajena -> 403" 403 $cancAjena.Status

Write-Host "`n=== 6. AGENDA ===" -ForegroundColor Cyan
$manana = (Get-Date).Date.AddDays($dias).ToString("yyyy-MM-dd")
$agenda = Invoke-Api -Method "GET" -Url "$URL_CITA/api/v1/citas/agenda?fecha=$manana" -Token $vetToken
Check "VET consulta su agenda -> 200" 200 $agenda.Status

$agendaCli = Invoke-Api -Method "GET" -Url "$URL_CITA/api/v1/citas/agenda" -Token $cliToken
Check "CLIENTE no accede a agenda -> 403" 403 $agendaCli.Status

Write-Host "`n=== 7. EXPEDIENTES ===" -ForegroundColor Cyan
$exp = Invoke-Api -Method "POST" -Url "$URL_EXP/api/v1/expedientes" -Token $vetToken -Body @{
    citaId = $citaId; diagnostico = "Otitis leve"; tratamiento = "Antibiotico 5 dias"; pesoKg = 12.5
}
Check "VET registra expediente -> 201" 201 $exp.Status

$citaAfter = Invoke-Api -Method "GET" -Url "$URL_CITA/api/v1/citas/$citaId" -Token $vetToken
Check "Cita pasa a COMPLETADA" "COMPLETADA" $citaAfter.Body.estado

$expDup = Invoke-Api -Method "POST" -Url "$URL_EXP/api/v1/expedientes" -Token $vetToken -Body @{
    citaId = $citaId; diagnostico = "Otro"; tratamiento = "Otro"; pesoKg = 12.6
}
Check "Expediente duplicado para la misma cita -> 400" 400 $expDup.Status

$hist = Invoke-Api -Method "GET" -Url "$URL_EXP/api/v1/expedientes/mascota/$mascotaId" -Token $cliToken
Check "CLIENTE ve historial de su mascota -> 200" 200 $hist.Status

$histAjena = Invoke-Api -Method "GET" -Url "$URL_EXP/api/v1/expedientes/mascota/$mascotaId" -Token $newCliToken
Check "CLIENTE ve historial de mascota ajena -> 403" 403 $histAjena.Status

$cliExp = Invoke-Api -Method "POST" -Url "$URL_EXP/api/v1/expedientes" -Token $cliToken -Body @{
    citaId = $citaId; diagnostico = "x"; tratamiento = "y"; pesoKg = 1
}
Check "CLIENTE no crea expedientes -> 403" 403 $cliExp.Status

Write-Host "`n=== 8. RECURSOS INEXISTENTES / ROLES ===" -ForegroundColor Cyan
$citaInex = Invoke-Api -Method "GET" -Url "$URL_CITA/api/v1/citas/999999" -Token $adminToken
Check "Cita inexistente -> 404" 404 $citaInex.Status

$usrNoAdmin = Invoke-Api -Method "GET" -Url "$URL_AUTH/api/v1/usuarios" -Token $cliToken
Check "CLIENTE no lista usuarios -> 403" 403 $usrNoAdmin.Status

$usrAdmin = Invoke-Api -Method "GET" -Url "$URL_AUTH/api/v1/usuarios" -Token $adminToken
Check "ADMIN lista usuarios -> 200" 200 $usrAdmin.Status

Write-Host "`n============================================" -ForegroundColor Yellow
Write-Host ("RESULTADO: {0} OK, {1} FAIL" -f $script:pass, $script:fail) -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
if ($script:fail -gt 0) { exit 1 } else { exit 0 }
