# ============================================================
# reset-db.ps1
# Borra y recrea las 4 bases de datos de PostgreSQL (estado limpio).
# Recomendado: detener los servicios antes (stop-all.ps1) para evitar
# conexiones abiertas.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File .\reset-db.ps1
# ============================================================

$psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$env:PGPASSWORD = "admin"

$bases = @("usuarios_db", "mascotas_db", "citas_db", "expedientes_db")
foreach ($db in $bases) {
    Write-Host "Recreando $db ..." -ForegroundColor Yellow
    & $psql -U postgres -d postgres -c "DROP DATABASE IF EXISTS $db WITH (FORCE);"
    & $psql -U postgres -d postgres -c "CREATE DATABASE $db;"
}
Write-Host "Bases de datos recreadas." -ForegroundColor Green
