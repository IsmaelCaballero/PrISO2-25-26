#!/usr/bin/env python3
"""Evidencia TC14-H05 (humanas, D28): puerta determinista + auditoria encadenada.

Sube el backlog TDOP al server (profile h2) y, tras cada operacion, relee el
hash de verificacion. Si la cola cambia, el eslabon de auditoria debe contener
el hash anterior (la cadena es verificable). La cola pedida tres veces con el
mismo estado y filtros debe traer el mismo orden byte a byte.

Uso: python tools/cdu14_smoke.py [base_url]   (requiere el server arrancado)
Salida: tabla de pasos y PASS/FAIL final; exit code 0/1.
"""
import json
import sys
import urllib.error
import urllib.request

BASE = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")
NUEVA = "/nueva-prueba"
BASE_BACKLOG = "/api/await/citas/cola-por-usuario?estado=ACTIVA"
BASE_DETAKE = "/api/await/citas/cola-retirar"
BASE_COLA = "/api/await/citas/cola"
BASE_DESDE = "/api/await/citas/cola-desde"
BASE_ASIGNAR = "/api/await/citas/asignar"
BASE_AUD = "/api/await/citas/auditoria"
BASE_HUECOS = "/api/await/agendas/huecos/libres"


def post(path, token, cuerpo):
    req = urllib.request.Request(
        BASE + path,
        data=json.dumps(cuerpo).encode("utf-8"),
        headers={"Content-Type": "application/json",
                 "Authorization": "Bearer " + token},
        method="POST")
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}


def get(path, token):
    req = urllib.request.Request(
        BASE + path, headers={"Authorization": "Bearer " + token})
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}


def main():
    st, tok = post("/api/integracion/sso/login", "",
                   {"usuario": "admin", "contrasena": "iso2-admin"})
    if st != 200:
        print(f"FALLO: login {st} {tok}")
        return 1
    token = tok["accessToken"]
    fallos = []

    # 1. Backlog: alta determinista sobre huecos libres (sin TDOP CSV: orden fijo a mano)
    st, huecos = get(BASE_HUECOS + "?profesionalId=prof-esperanza&pruebaCodigo=" + NUEVA, token)
    if st != 200:
        print(f"FALLO: huecos libres {st} {huecos}")
        return 1
    h = sorted(huecos, key=lambda x: x["inicio"])
    print(f"{len(h)} huecos libres de prof-esperanza para {NUEVA}")
    for i, prio in enumerate(["URGENTE", "ORDINARIA", "PREFERENTE", "URGENTE", "ORDINARIA"]):
        st, e = post("/api/await/citas/cola-alta", token,
                     {"pacienteId": f"smoke-{i}", "pruebaCodigo": NUEVA,
                      "profesionalId": "prof-esperanza", "prioridad": prio})
        print(f"  alta[{i}] {prio}: {st} id={e.get('id')} cre={e.get('creadaEn')}")
        if st != 201:
            fallos.append(f"alta[{i}] {st}")

    # 2. Puerta determinista: la cola, tres veces, mismo orden exacto
    lecturas = []
    for _ in range(3):
        st, cola = get(BASE_COLA + "?pruebaCodigo=" + NUEVA, token)
        lecturas.append([f"{e['id']}|{e['prioridad']}|{e['creadaEn']}" for e in cola])
    if len(set(map(tuple, lecturas))) != 1:
        fallos.append("la cola no trajo el mismo orden en tres lecturas")
    print("\norden de la cola (3 lecturas identicas =",
          len(set(map(tuple, lecturas))) == 1, "):")
    for fila in lecturas[0]:
        print("  " + fila)

    # 3. Reubicacion: subir prioridad a la ultima y verificar cadena de auditoria
    antes = post(BASE_AUD + "/verificar", token, {})[1]
    ultimo = lecturas[0][-1].split("|")[0]
    st, _ = post("/api/await/citas/cola-cambiar-prioridad", token,
                 {"esperaId": ultimo, "prioridad": "PREFERENTE", "motivo": "smoke iter7"})
    print(f"\ncambiar-prioridad de {ultimo}: {st}")
    if st != 200:
        fallos.append(f"cambiar-prioridad {st}")
    st, aud = post(BASE_AUD + "/verificar", token, {})
    print(f"verificar auditoria: {st} verificable={aud.get('verificable')} eslabones={aud.get('eslabonesVerificados')}")
    if not aud.get("verificable"):
        fallos.append("cadena de auditoria no verificable tras reubicacion")
    tras = post(BASE_AUD + "/verificar", token, {})[1]
    if tras.get("ultimoHash") != aud.get("ultimoHash"):
        fallos.append("el hash final no coincide tras verificar")
    if antes.get("ultimoHash") and aud.get("eslabonesVerificados", 0) <= antes.get("eslabonesVerificados", 0):
        fallos.append("no hay eslabones nuevos tras la reubicacion")

    # 4. Asignacion desde cola con eslabon ENLAZE y retirada con RETIRO
    st, cola2 = get(BASE_COLA + "?pruebaCodigo=" + NUEVA, token)
    primero = cola2[0]
    inicio = h[0]["inicio"]
    st, res = post(BASE_DESDE, token,
                   {"esperaId": primero["id"], "inicio": inicio, "duracionMinutos": 30})
    print(f"\nasignar-desde-cola {primero['id']} en {inicio}: {st} cita={res.get('citaId')}")
    if st not in (200, 201):
        fallos.append(f"asignar-desde-cola {st}")
    st, _ = post(BASE_DETAKE, token, {"esperaId": cola2[1]["id"], "motivo": "smoke retiro"})
    print(f"retirar {cola2[1]['id']}: {st}")
    if st != 200:
        fallos.append(f"retirar {st}")
    st, aud2 = post(BASE_AUD + "/verificar", token, {})
    print(f"verificar final: {st} verificable={aud2.get('verificable')} eslabones={aud2.get('eslabonesVerificados')}")
    if not aud2.get("verificable"):
        fallos.append("cadena no verificable tras asignacion y retiro")

    # 5. Baseline CDU12 intacta: asignar normal sigue respondiendo
    st, _ = post(BASE_ASIGNAR, token,
                 {"pacienteId": "smoke-base", "pruebaCodigo": NUEVA,
                  "profesionalId": "prof-esperanza", "inicio": h[-1]["inicio"],
                  "duracionMinutos": 30})
    print(f"\nbaseline CDU12 /asignar: {st}")
    if st not in (200, 201, 409):
        fallos.append(f"baseline {st}")

    print("\n== RESULTADO ==")
    if fallos:
        print("FAIL:", "; ".join(fallos))
        return 1
    print("PASS: cola determinista, cadena de auditoria encadenada y baseline CDU12 viva")
    return 0


if __name__ == "__main__":
    sys.exit(main())
