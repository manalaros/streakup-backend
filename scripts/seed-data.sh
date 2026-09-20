#!/usr/bin/env bash

set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
DEMO_PASSWORD="${DEMO_PASSWORD:-password123}"
ALICE_EMAIL="demo.alice@example.com"
BOB_EMAIL="demo.bob@example.com"
ALICE_USERNAME="demoAlice"
BOB_USERNAME="demoBob"

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Error: falta el comando requerido: $1" >&2
    exit 1
  }
}

require_command curl
require_command python3

status_code() {
  curl -sS -o /dev/null -w '%{http_code}' "$@"
}

json_value() {
  python3 -c 'import json, sys; print(json.load(sys.stdin)[sys.argv[1]])' "$1"
}

echo "Comprobando API en ${API_URL}..."
if [[ "$(status_code "${API_URL}/api/health")" != "200" ]]; then
  echo "Error: la API no responde. Levántala primero con ./mvnw spring-boot:run o docker compose up --build." >&2
  exit 1
fi

register_user() {
  local username="$1"
  local email="$2"
  local payload
  payload=$(printf '{"username":"%s","email":"%s","password":"%s"}' "$username" "$email" "$DEMO_PASSWORD")
  local code
  code=$(status_code -X POST "${API_URL}/api/users" -H 'Content-Type: application/json' -d "$payload")
  case "$code" in
    201|409) ;;
    *)
      echo "Error registrando ${email}: HTTP ${code}" >&2
      exit 1
      ;;
  esac
}

login_user() {
  local email="$1"
  local payload
  payload=$(printf '{"email":"%s","password":"%s"}' "$email" "$DEMO_PASSWORD")
  curl -sS -X POST "${API_URL}/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "$payload"
}

echo "Creando o reutilizando usuarios demo..."
register_user "$ALICE_USERNAME" "$ALICE_EMAIL"
register_user "$BOB_USERNAME" "$BOB_EMAIL"

alice_login=$(login_user "$ALICE_EMAIL")
bob_login=$(login_user "$BOB_EMAIL")
alice_token=$(printf '%s' "$alice_login" | json_value token)
bob_token=$(printf '%s' "$bob_login" | json_value token)
alice_id=$(printf '%s' "$alice_login" | python3 -c 'import json,sys; print(json.load(sys.stdin)["user"]["id"])')
bob_id=$(printf '%s' "$bob_login" | python3 -c 'import json,sys; print(json.load(sys.stdin)["user"]["id"])')

create_activity_if_missing() {
  local token="$1"
  local date="$2"
  local type="$3"
  local duration="$4"
  local notes="$5"
  local activities
  activities=$(curl -sS "${API_URL}/api/activities" -H "Authorization: Bearer ${token}")
  if printf '%s' "$activities" | python3 -c \
    'import json,sys; data=json.load(sys.stdin); sys.exit(0 if any(a.get("date")==sys.argv[1] and a.get("notes")==sys.argv[2] for a in data) else 1)' \
    "$date" "$notes"; then
    return
  fi
  curl -sS -f -X POST "${API_URL}/api/activities" \
    -H "Authorization: Bearer ${token}" \
    -H 'Content-Type: application/json' \
    -d "{\"type\":\"${type}\",\"date\":\"${date}\",\"durationMinutes\":${duration},\"notes\":\"${notes}\"}" \
    >/dev/null
}

echo "Creando actividades demo..."
create_activity_if_missing "$alice_token" "2026-09-15" "RUNNING" 30 "seed-alice-running"
create_activity_if_missing "$alice_token" "2026-09-17" "YOGA" 20 "seed-alice-yoga"
create_activity_if_missing "$bob_token" "2026-09-16" "CYCLING" 45 "seed-bob-cycling"
create_activity_if_missing "$bob_token" "2026-09-19" "GYM" 60 "seed-bob-gym"

alice_friends=$(curl -sS "${API_URL}/api/friends" -H "Authorization: Bearer ${alice_token}")
if printf '%s' "$alice_friends" | python3 -c \
  'import json,sys; data=json.load(sys.stdin); target=int(sys.argv[1]); sys.exit(0 if any(f.get("requester",{}).get("id")==target or f.get("addressee",{}).get("id")==target for f in data) else 1)' \
  "$bob_id"; then
  already_friends=0
else
  already_friends=1
fi

if [[ "$already_friends" -ne 0 ]]; then
  echo "Creando o reutilizando amistad demo..."
  request_response=$(curl -sS -w '\n%{http_code}' -X POST \
    "${API_URL}/api/friends/requests/${bob_id}" \
    -H "Authorization: Bearer ${alice_token}")
  request_code=$(printf '%s' "$request_response" | tail -n1)
  if [[ "$request_code" == "201" ]]; then
    request_id=$(printf '%s' "$request_response" | sed '$d' | json_value id)
    curl -sS -f -X POST "${API_URL}/api/friends/requests/${request_id}/accept" \
      -H "Authorization: Bearer ${bob_token}" >/dev/null
  elif [[ "$request_code" == "200" ]]; then
    :
  elif [[ "$request_code" == "409" ]]; then
    pending=$(curl -sS "${API_URL}/api/friends/requests" -H "Authorization: Bearer ${bob_token}")
    request_id=$(printf '%s' "$pending" | python3 -c \
      'import json,sys; data=json.load(sys.stdin); target=int(sys.argv[1]); matches=[x["id"] for x in data if x.get("requester",{}).get("id")==target]; print(matches[0] if matches else "")' \
      "$alice_id")
    if [[ -n "$request_id" ]]; then
      curl -sS -f -X POST "${API_URL}/api/friends/requests/${request_id}/accept" \
        -H "Authorization: Bearer ${bob_token}" >/dev/null
    fi
  else
    echo "Error creando amistad demo: HTTP ${request_code}" >&2
    exit 1
  fi
fi

echo
echo "Datos demo preparados correctamente."
echo "Alice: ${ALICE_EMAIL} / ${DEMO_PASSWORD} (id ${alice_id})"
echo "Bob:   ${BOB_EMAIL} / ${DEMO_PASSWORD} (id ${bob_id})"
