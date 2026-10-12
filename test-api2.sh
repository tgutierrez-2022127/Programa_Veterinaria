#!/bin/bash
# ============================================================
# test-veterinaria.sh  (script de pruebas de la rubrica)
#
# Todas las peticiones salen por el API GATEWAY en el puerto 8080,
# que enruta a los 4 microservicios (auth 8081, mascotas 8082,
# citas 8083, expedientes 8084).
#
# Uso:
#   bash test-veterinaria.sh
#   BASE_URL=http://localhost:8090/api/v1 bash test-veterinaria.sh   (otro puerto)
# ============================================================

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"
ADMIN_EMAIL="admin@veterinaria.com"
ADMIN_PASS="Admin123*"
CLIENTE_EMAIL="dueno@veterinaria.com"
CLIENTE_PASS="Cliente123*"

echo "INICIANDO PRUEBAS DE VETERINARIA"

# 1. Registro de CLIENTE
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Pedro Armas","telefono":"55551234","email":"'"$CLIENTE_EMAIL"'","password":"'"$CLIENTE_PASS"'"}' | jq .

# 2. Login ADMIN
ADMIN_LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"'"$ADMIN_EMAIL"'","password":"'"$ADMIN_PASS"'"}')
ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESP | jq -r '.token // .accessToken')

# 3. Login CLIENTE
CLIENTE_LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"'"$CLIENTE_EMAIL"'","password":"'"$CLIENTE_PASS"'"}')
CLIENTE_TOKEN=$(echo $CLIENTE_LOGIN_RESP | jq -r '.token // .accessToken')

# 4. Registro de Mascota
MASCOTA_RESP=$(curl -s -X POST "$BASE_URL/mascotas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENTE_TOKEN" \
  -d '{"nombre":"Zafir","especie":"PERRO","raza":"Beagle","edad":3}')
MASCOTA_ID=$(echo $MASCOTA_RESP | jq -r '.id')

# 5. Test 403: CLIENTE intenta crear expediente
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/expedientes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENTE_TOKEN" \
  -d '{"citaId":1,"diagnostico":"Consulta general","tratamiento":"Desparasitante","pesoKg":12.5}')

if [ "$HTTP_STATUS" -eq 403 ]; then
  echo "Seguridad Validada: 403 Forbidden correctamente."
fi

# 6. Prueba de estrés
if command -v ab &> /dev/null; then
  ab -n 500 -c 50 -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/citas/agenda?fecha=2026-10-06"
else
  seq 100 | xargs -n 1 -P 10 -I {} curl -s -o /dev/null -w "%{http_code}\n" \
    -X GET "$BASE_URL/citas/agenda?fecha=2026-10-06" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | sort | uniq -c
fi

echo "PRUEBAS COMPLETADAS"
