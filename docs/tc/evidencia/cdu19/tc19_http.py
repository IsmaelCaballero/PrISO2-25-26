"""Ejecucion de los TC humanos CDU19 (TC19-01..TC19-09) por HTTP contra el servidor en marcha.

Vuelca un registro JSON con status + cuerpo de cada peticion, para rellenar la columna
'Resultado real' de docs/tc/cdu19-tc-humanos.md. No sustituye la validacion D28 de Ismael.
"""
import json
import sys
import time
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
REG = []


def call(tc, etiqueta, metodo, ruta, cuerpo=None, token=None, content_type="application/json", raw=None):
    datos = None
    if raw is not None:
        datos = raw.encode("utf-8")
    elif cuerpo is not None:
        datos = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    if datos is not None:
        req.add_header("Content-Type", content_type)
    if token:
        req.add_header("Authorization", "Bearer " + token)
    t0 = time.monotonic()
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            status, texto = r.status, r.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        status, texto = e.code, e.read().decode("utf-8", "replace")
    except Exception as e:  # noqa: BLE001
        status, texto = "EXC", repr(e)
    ms = int((time.monotonic() - t0) * 1000)
    REG.append({"tc": tc, "paso": etiqueta, "metodo": metodo, "ruta": ruta,
                "status": status, "ms": ms, "cuerpo": texto[:900]})
    return status, texto


def jsonde(texto):
    try:
        return json.loads(texto)
    except Exception:  # noqa: BLE001
        return None


def login(usuario, password):
    s, t = call("setup", "login " + usuario, "POST", "/api/auth/login",
                {"usuario": usuario, "password": password})
    d = jsonde(t) or {}
    return d.get("token"), s, d


# ---------------------------------------------------------------- TC19-01
tok, st, _ = login("admin", "iso2-admin")
s, t = call("TC19-01", "login SSO IdP correcto", "POST", "/api/integracion/sso/login",
            {"usuario": "idp-admin", "password": "idp-pass"}, tok)
s2, t2 = call("TC19-01", "login SSO contrasena err6nea", "POST", "/api/integracion/sso/login",
              {"usuario": "idp-admin", "password": "mal"}, tok)
s3, t3 = call("TC19-01", "login SSO sin contrasena", "POST", "/api/integracion/sso/login",
              {"usuario": "idp-admin"}, tok)
s3b, t3b = call("TC19-01", "login SSO clave= alt", "POST", "/api/integracion/sso/login",
                {"usuario": "idp-admin", "contrasena": "idp-pass"}, tok)

# ---------------------------------------------------------------- TC19-02
s, t = call("TC19-02", "sincronizar api-paciente-ok", "POST",
            "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
sA, tA = call("TC19-02", "auditoria", "GET", "/api/integracion/auditoria", token=tok)

# ---------------------------------------------------------------- TC19-03
call("TC19-03", "estudio ACC-0001", "GET", "/api/integracion/estudios/ACC-0001", token=tok)
call("TC19-03", "estudio acc-0001 minusculas", "GET", "/api/integracion/estudios/acc-0001", token=tok)
call("TC19-03", "estudio ACC-9999", "GET", "/api/integracion/estudios/ACC-9999", token=tok)
call("TC19-03", "listado sistemas", "GET", "/api/integracion/sistemas", token=tok)
call("TC19-03", "ping pacs", "GET", "/api/integracion/sistemas/pacs/ping", token=tok)

# ---------------------------------------------------------------- TC19-04 (degradado)
call("TC19-04", "ris CAIDO", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-04", "pacs CAIDO", "POST", "/api/integracion/simulacion",
     {"sistema": "pacs", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-04", "ping ris caido", "GET", "/api/integracion/sistemas/ris/ping", token=tok)
call("TC19-04", "estudio con ris caido", "GET", "/api/integracion/estudios/ACC-0001", token=tok)
login("admin", "iso2-admin")
call("TC19-04", "catalogo pruebas", "GET", "/api/catalogo/pruebas", token=tok)
call("TC19-04", "agendas", "GET", "/api/agendas", token=tok)
call("TC19-04", "citas sin solicitud", "POST", "/api/citas",
     {"solicitudId": None, "profesionalId": "medico", "inicio": "2026-11-30T09:00:00",
      "duracionMinutos": 30}, tok)
# restaurar
call("TC19-04", "ris NORMAL", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-04", "pacs NORMAL", "POST", "/api/integracion/simulacion",
     {"sistema": "pacs", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)

# ---------------------------------------------------------------- TC19-05 (timeout en caliente)
call("TC19-05", "hce LENTO 250/100 r0", "POST", "/api/integracion/simulacion",
     {"sistema": "hce", "modo": "LENTO", "latenciaMs": 250, "timeoutMs": 100, "reintentos": 0}, tok)
call("TC19-05", "sincronizar con LENTO", "POST",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
call("TC19-05", "hce LENTO_TIMEOUT 250/100 r1", "POST", "/api/integracion/simulacion",
     {"sistema": "hce", "modo": "LENTO_TIMEOUT", "latenciaMs": 250, "timeoutMs": 100,
      "reintentos": 1}, tok)
call("TC19-05", "sincronizar con LENTO_TIMEOUT", "POST",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
call("TC19-05", "ver politica", "GET", "/api/integracion/simulacion", token=tok)
call("TC19-05", "hce NORMAL", "POST", "/api/integracion/simulacion",
     {"sistema": "hce", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-05", "sincronizar recuperado", "POST",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)

# ---------------------------------------------------------------- TC19-06 (idempotencia)
for i in range(3):
    call("TC19-06", "sincronizar estable %d" % (i + 1), "POST",
         "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
call("TC19-06", "auditoria tras 3", "GET", "/api/integracion/auditoria", token=tok)
for i in range(2):
    call("TC19-06", "sincronizar rotativo %d" % (i + 1), "POST",
         "/api/integracion/historias/api-paciente-rotativo/sincronizar", token=tok)
call("TC19-06", "auditoria tras rotativo", "GET", "/api/integracion/auditoria", token=tok)

# ---------------------------------------------------------------- TC19-07 (hallazgo degradado HC)
call("TC19-07", "sincronizar con HCE NORMAL", "POST",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
call("TC19-07", "hce CAIDO", "POST", "/api/integracion/simulacion",
     {"sistema": "hce", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-07", "resincronizar con copia local", "POST",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)
call("TC19-07", "hce NORMAL otra vez", "POST", "/api/integracion/simulacion",
     {"sistema": "hce", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)

# ---------------------------------------------------------------- TC19-08 (permisos)
tokens = {}
for u, p in [("medico", "iso2-medico"), ("tecnico", "iso2-tecnico"),
             ("recepcionista", "iso2-rec"), ("celador", "iso2-ses")]:
    tk, _, _ = login(u, p)
    tokens[u] = tk
for u, tk in tokens.items():
    call("TC19-08", "%s GET sistemas" % u, "GET", "/api/integracion/sistemas", token=tk)
    call("TC19-08", "%s GET ping hce" % u, "GET", "/api/integracion/sistemas/hce/ping", token=tk)
    call("TC19-08", "%s POST simulacion" % u, "POST", "/api/integracion/simulacion",
         {"sistema": "ris", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tk)
call("TC19-08", "admin GET sistemas", "GET", "/api/integracion/sistemas", token=tok)
call("TC19-08", "admin POST simulacion", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-08", "auditoria sin token", "GET", "/api/integracion/auditoria")

# ---------------------------------------------------------------- TC19-09 (entryos raros)
call("TC19-09", "sistema vacio", "POST", "/api/integracion/simulacion",
     {"sistema": "", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-09", "sistema espacios", "POST", "/api/integracion/simulacion",
     {"sistema": "   ", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-09", "sistema volador", "POST", "/api/integracion/simulacion",
     {"sistema": "volador", "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-09", "modo IMPOSIBLE", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "IMPOSIBLE", "latenciaMs": 0, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-09", "latencia negativa", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "NORMAL", "latenciaMs": -5, "timeoutMs": 0, "reintentos": 0}, tok)
call("TC19-09", "latencia ausente", "POST", "/api/integracion/simulacion",
     {"sistema": "ris", "modo": "NORMAL"}, tok)
call("TC19-09", "sso cuerpo null", "POST", "/api/integracion/sso/login", None, tok)
call("TC19-09", "sso texto plano", "POST", "/api/integracion/sso/login", token=tok,
     raw="hola", content_type="text/plain")
call("TC19-09", "sistemas//ping", "GET", "/api/integracion/sistemas//ping", token=tok)
call("TC19-09", "POST sistemas", "POST", "/api/integracion/sistemas", {}, tok)
call("TC19-09", "GET sincronizar", "GET",
     "/api/integracion/historias/api-paciente-ok/sincronizar", token=tok)

sys.stdout.buffer.write(json.dumps(REG, ensure_ascii=False, indent=1).encode("utf-8"))
with open(r"C:\Users\Ismae\AppData\Local\Temp\opencode\tc19_registro.json", "w",
          encoding="utf-8") as fh:
    json.dump(REG, fh, ensure_ascii=False, indent=1)
