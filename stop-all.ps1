# ============================================================
# stop-all.ps1
# Detiene los microservicios y el API Gateway (procesos java lanzados
# desde este proyecto). Uso:
#   powershell -ExecutionPolicy Bypass -File .\stop-all.ps1
# ============================================================

Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -like '*Programa_Veterinaria*' } |
    ForEach-Object {
        Write-Host "Deteniendo PID $($_.ProcessId)" -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }
Write-Host "Servicios detenidos." -ForegroundColor Green
