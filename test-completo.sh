#!/usr/bin/env bash
# ============================================================
# test-veterinaria.sh
# Pruebas funcionales end-to-end del Sistema de Control de Citas
# para Veterinaria (microservicios + PostgreSQL).
#
# Requisitos:
#   - curl disponible
#   - Los 4 microservicios levantados (puertos 8081..8084) y el API GATEWAY (8080)
#
# Uso:
#   bash test-veterinaria.sh
#   BASE_URL=http://localhost:8090 bash test-veterinaria.sh   (otro puerto)
# ============================================================

# Todas las peticiones pasan por el API GATEWAY (puerto 8080), que enruta a cada
# microservicio. Se puede cambiar con la variable de entorno BASE_URL, por ejemplo:
#   BASE_URL=http://localhost:8090 bash test-veterinaria.sh
BASE="${BASE_URL:-http://localhost:8080}"

# El gateway expone los 4 microservicios bajo el mismo host:puerto.
URL_AUTH="$BASE"
URL_MASC="$BASE"
URL_CITA="$BASE"
URL_EXP="$BASE"

PASS=0
FAIL=0

# --- helper: extrae el primer valor de una clave string o numerica de un JSON ---
jstr() { echo "$1" | sed -n 's/.*"'"$2"'":"\([^"]*\)".*/\1/p' | head -n1; }
jnum() { echo "$1" | sed -n 's/.*"'"$2"'":\([0-9][0-9]*\).*/\1/p' | head -n1; }

# --- helper: realiza una peticion y deja el codigo en HTTP_CODE y el cuerpo en BODY ---
call() {
  local method=$1 url=$2 token=$3 body=$4
  local resp
  if [ -n "$body" ]; then
    if [ -n "$token" ]; then
      resp=$(curl -s -X "$method" "$url" -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d "$body" -w $'\n%{http_code}')
    else
      resp=$(curl -s -X "$method" "$url" -H "Content-Type: application/json" -d "$body" -w $'\n%{http_code}')
    fi
  else
    if [ -n "$token" ]; then
      resp=$(curl -s -X "$method" "$url" -H "Authorization: Bearer $token" -w $'\n%{http_code}')
    else
      resp=$(curl -s -X "$method" "$url" -w $'\n%{http_code}')
    fi
  fi
  HTTP_CODE=$(echo "$resp" | tail -n1)
  BODY=$(echo "$resp" | sed '$d')
}

check() {
  local nombre=$1 esperado=$2 obtenido=$3
  if [ "$esperado" = "$obtenido" ]; then
    printf '[OK]   %s  (esperado %s)\n' "$nombre" "$esperado"
    PASS=$((PASS+1))
  else
    printf '[FAIL] %s  (esperado %s, obtenido %s)\n' "$nombre" "$esperado" "$obtenido"
    FAIL=$((FAIL+1))
  fi
}

# NOTA: no usar una funcion login() con $() (subshell), porque HTTP_CODE se
# asignaria dentro del subshell y no se veria en el shell principal. Por eso el
# login se hace con call directo.

# Dia futuro aleatorio (2..21): asi el script puede ejecutarse varias veces sin
# chocar con el limite de 2 citas PENDIENTES por cliente y por dia.
DIA=$((RANDOM % 20 + 2))
MANANA_HORA_1000=$(date -d "+$DIA day" +%Y-%m-%dT10:00:00)
MANANA_HORA_1015=$(date -d "+$DIA day" +%Y-%m-%dT10:15:00)
MANANA_HORA_1030=$(date -d "+$DIA day" +%Y-%m-%dT10:30:00)
MANANA_HORA_1100=$(date -d "+$DIA day" +%Y-%m-%dT11:00:00)
MANANA_HORA_1400=$(date -d "+$DIA day" +%Y-%m-%dT14:00:00)
MANANA_FECHA=$(date -d "+$DIA day" +%Y-%m-%d)
AYER_HORA_1000=$(date -d "-1 day" +%Y-%m-%dT10:00:00)
EN_90_MIN=$(date -d "+90 minutes" +%Y-%m-%dT%H:%M:%S)
EN_5_HORAS=$(date -d "+5 hours" +%Y-%m-%dT%H:%M:%S)

echo ""
echo "=== 1. SEGURIDAD / AUTENTICACION ==="

call POST "$URL_AUTH/api/v1/auth/login" "" "{\"email\":\"admin@veterinaria.com\",\"password\":\"Admin123*\"}"
check "Login ADMIN" 200 "$HTTP_CODE"
ADMIN_TOKEN=$(jstr "$BODY" "token")

call POST "$URL_AUTH/api/v1/auth/login" "" "{\"email\":\"vet@veterinaria.com\",\"password\":\"Vet123*\"}"
check "Login VET" 200 "$HTTP_CODE"
VET_TOKEN=$(jstr "$BODY" "token")
VET_ID=$(jnum "$BODY" "id")

call POST "$URL_AUTH/api/v1/auth/login" "" "{\"email\":\"cliente@veterinaria.com\",\"password\":\"Cliente123*\"}"
check "Login CLIENTE" 200 "$HTTP_CODE"
CLI_TOKEN=$(jstr "$BODY" "token")
CLI_ID=$(jnum "$BODY" "id")

login "admin@veterinaria.com" "clave-mala" >/dev/null
check "Login con clave incorrecta -> 401" 401 "$HTTP_CODE"

call GET "$URL_MASC/api/v1/mascotas/mis-mascotas" "" ""
check "Endpoint protegido sin token -> 401" 401 "$HTTP_CODE"

call GET "$URL_MASC/api/v1/mascotas/mis-mascotas" "token.invalido.xxx" ""
check "Token invalido -> 401" 401 "$HTTP_CODE"

echo ""
echo "=== 2. REGISTRO PUBLICO (no puede escalar rol) ==="
NUEVO_EMAIL="nuevo$RANDOM@test.com"
call POST "$URL_AUTH/api/v1/auth/register" "" "{\"nombre\":\"Nuevo\",\"telefono\":\"555-9999\",\"email\":\"$NUEVO_EMAIL\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}"
check "Registro publico -> 201" 201 "$HTTP_CODE"
ROL=$(jstr "$BODY" "rol")
check "Rol forzado a CLIENTE (ignora ADMIN del body)" "CLIENTE" "$ROL"

call POST "$URL_AUTH/api/v1/auth/register" "" "{\"nombre\":\"Dup\",\"email\":\"$NUEVO_EMAIL\",\"password\":\"secreto123\"}"
check "Email duplicado -> 409" 409 "$HTTP_CODE"

call POST "$URL_AUTH/api/v1/auth/register" "" "{\"nombre\":\"\",\"email\":\"correo-malo\",\"password\":\"123\"}"
check "Datos invalidos -> 400" 400 "$HTTP_CODE"

call POST "$URL_AUTH/api/v1/auth/login" "" "{\"email\":\"$NUEVO_EMAIL\",\"password\":\"secreto123\"}"
NEW_TOKEN=$(jstr "$BODY" "token")

echo ""
echo "=== 3. MASCOTAS ==="
call POST "$URL_MASC/api/v1/mascotas" "$CLI_TOKEN" "{\"nombre\":\"Firulais\",\"especie\":\"PERRO\",\"raza\":\"Labrador\",\"edad\":3}"
check "CLIENTE registra mascota -> 201" 201 "$HTTP_CODE"
MASCOTA_ID=$(jnum "$BODY" "id")
MASC_CLIENTE=$(jnum "$BODY" "clienteId")
check "Mascota asignada al cliente del token" "$CLI_ID" "$MASC_CLIENTE"

call GET "$URL_MASC/api/v1/mascotas/mis-mascotas" "$CLI_TOKEN" ""
check "GET mis-mascotas -> 200" 200 "$HTTP_CODE"

call GET "$URL_MASC/api/v1/mascotas/$MASCOTA_ID" "$VET_TOKEN" ""
check "VET consulta mascota -> 200" 200 "$HTTP_CODE"

call GET "$URL_MASC/api/v1/mascotas/$MASCOTA_ID" "$CLI_TOKEN" ""
check "CLIENTE intenta GET /mascotas/{id} -> 403" 403 "$HTTP_CODE"

call GET "$URL_MASC/api/v1/mascotas/mis-mascotas" "$NEW_TOKEN" ""
if echo "$BODY" | grep -q "\"id\":$MASCOTA_ID"; then AJENA_SI=1; else AJENA_SI=0; fi
check "Otro cliente no ve la mascota ajena" 0 "$AJENA_SI"

echo ""
echo "=== 4. CITAS - REGLAS DE NEGOCIO ==="
call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$MANANA_HORA_1000\",\"motivo\":\"Vacunacion\"}"
check "Cita valida -> 201" 201 "$HTTP_CODE"
CITA_ID=$(jnum "$BODY" "id")

call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$MANANA_HORA_1015\",\"motivo\":\"Solape\"}"
check "Veterinario ocupado (10:15 con 10:00) -> 409" 409 "$HTTP_CODE"

call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$MANANA_HORA_1030\",\"motivo\":\"Revision\"}"
check "Cita consecutiva 10:30 -> 201" 201 "$HTTP_CODE"

call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$MANANA_HORA_1100\",\"motivo\":\"Tercera\"}"
check "Tercera cita pendiente del dia -> 409" 409 "$HTTP_CODE"

call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$AYER_HORA_1000\",\"motivo\":\"Pasada\"}"
check "Fecha pasada -> 400" 400 "$HTTP_CODE"

call POST "$URL_CITA/api/v1/citas" "$CLI_TOKEN" "{\"mascotaId\":999999,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$MANANA_HORA_1400\",\"motivo\":\"Inexistente\"}"
check "Mascota inexistente -> 404" 404 "$HTTP_CODE"

echo ""
echo "=== 5. CANCELACION (regla 2 horas) ==="
call POST "$URL_CITA/api/v1/citas" "$NEW_TOKEN" "{\"mascotaId\":$MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$EN_90_MIN\",\"motivo\":\"Ajena\"}"
check "Cliente no puede agendar en mascota ajena -> 403" 403 "$HTTP_CODE"

call POST "$URL_MASC/api/v1/mascotas" "$NEW_TOKEN" "{\"nombre\":\"Michu\",\"especie\":\"GATO\",\"raza\":\"Siames\",\"edad\":2}"
OTRA_MASCOTA_ID=$(jnum "$BODY" "id")

call POST "$URL_CITA/api/v1/citas" "$NEW_TOKEN" "{\"mascotaId\":$OTRA_MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$EN_90_MIN\",\"motivo\":\"Pronta\"}"
check "Cita a 90 min -> 201" 201 "$HTTP_CODE"
CITA_90=$(jnum "$BODY" "id")

call PATCH "$URL_CITA/api/v1/citas/$CITA_90/cancelar" "$NEW_TOKEN" ""
check "Cancelar con menos de 2 horas -> 400" 400 "$HTTP_CODE"

call POST "$URL_CITA/api/v1/citas" "$NEW_TOKEN" "{\"mascotaId\":$OTRA_MASCOTA_ID,\"veterinarioId\":$VET_ID,\"fechaHora\":\"$EN_5_HORAS\",\"motivo\":\"Lejana\"}"
check "Cita a 5 horas -> 201" 201 "$HTTP_CODE"
CITA_5H=$(jnum "$BODY" "id")

call PATCH "$URL_CITA/api/v1/citas/$CITA_5H/cancelar" "$NEW_TOKEN" ""
check "Cancelar con mas de 2 horas -> 200" 200 "$HTTP_CODE"
check "Estado CANCELADA" "CANCELADA" "$(jstr "$BODY" "estado")"

call PATCH "$URL_CITA/api/v1/citas/$CITA_ID/cancelar" "$NEW_TOKEN" ""
check "Cliente cancela cita ajena -> 403" 403 "$HTTP_CODE"

echo ""
echo "=== 6. AGENDA ==="
call GET "$URL_CITA/api/v1/citas/agenda?fecha=$MANANA_FECHA" "$VET_TOKEN" ""
check "VET consulta su agenda -> 200" 200 "$HTTP_CODE"

call GET "$URL_CITA/api/v1/citas/agenda" "$CLI_TOKEN" ""
check "CLIENTE no accede a agenda -> 403" 403 "$HTTP_CODE"

echo ""
echo "=== 7. EXPEDIENTES ==="
call POST "$URL_EXP/api/v1/expedientes" "$VET_TOKEN" "{\"citaId\":$CITA_ID,\"diagnostico\":\"Otitis leve\",\"tratamiento\":\"Antibiotico 5 dias\",\"pesoKg\":12.5}"
check "VET registra expediente -> 201" 201 "$HTTP_CODE"

call GET "$URL_CITA/api/v1/citas/$CITA_ID" "$VET_TOKEN" ""
check "Cita pasa a COMPLETADA" "COMPLETADA" "$(jstr "$BODY" "estado")"

call POST "$URL_EXP/api/v1/expedientes" "$VET_TOKEN" "{\"citaId\":$CITA_ID,\"diagnostico\":\"Otro\",\"tratamiento\":\"Otro\",\"pesoKg\":12.6}"
check "Expediente duplicado para la misma cita -> 400" 400 "$HTTP_CODE"

call GET "$URL_EXP/api/v1/expedientes/mascota/$MASCOTA_ID" "$CLI_TOKEN" ""
check "CLIENTE ve historial de su mascota -> 200" 200 "$HTTP_CODE"

call GET "$URL_EXP/api/v1/expedientes/mascota/$MASCOTA_ID" "$NEW_TOKEN" ""
check "CLIENTE ve historial de mascota ajena -> 403" 403 "$HTTP_CODE"

call POST "$URL_EXP/api/v1/expedientes" "$CLI_TOKEN" "{\"citaId\":$CITA_ID,\"diagnostico\":\"x\",\"tratamiento\":\"y\",\"pesoKg\":1}"
check "CLIENTE no crea expedientes -> 403" 403 "$HTTP_CODE"

echo ""
echo "=== 8. RECURSOS INEXISTENTES / ROLES ==="
call GET "$URL_CITA/api/v1/citas/999999" "$ADMIN_TOKEN" ""
check "Cita inexistente -> 404" 404 "$HTTP_CODE"

call GET "$URL_AUTH/api/v1/usuarios" "$CLI_TOKEN" ""
check "CLIENTE no lista usuarios -> 403" 403 "$HTTP_CODE"

call GET "$URL_AUTH/api/v1/usuarios" "$ADMIN_TOKEN" ""
check "ADMIN lista usuarios -> 200" 200 "$HTTP_CODE"

echo ""
echo "============================================"
echo "RESULTADO: $PASS OK, $FAIL FAIL"
echo "============================================"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
