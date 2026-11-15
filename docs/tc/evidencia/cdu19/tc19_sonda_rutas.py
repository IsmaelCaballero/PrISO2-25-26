"""Sonda de cierre: que status+cuerpo devuelven rutas NO mapeadas dentro de la cadena
de seguridad, con token valido de admin. Sirve para documentar el comportamiento real
de los TC19-09/TC12-05 en el servidor integrado (no en MockMvc)."""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
REG = []


def call(etiqueta, metodo, ruta, token=None, cuerpo=None):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    req = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    if datos:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            REG.append((etiqueta, metodo, ruta, r.status, r.read().decode()[:220]))
    except urllib.error.HTTPError as e:
        REG.append((etiqueta, metodo, ruta, e.code, e.read().decode()[:220]))
    except Exception as e:  # noqa: BLE001
        REG.append((etiqueta, metodo, ruta, "EXC", repr(e)))


req = urllib.request.Request(BASE + "/api/auth/login",
                             data=json.dumps({"usuario": "admin", "password": "iso2-admin"}).encode(),
                             method="POST")
req.add_header("Content-Type", "application/json")
with urllib.request.urlopen(req, timeout=15) as r:
    tok = json.loads(r.read().decode())["token"]

sondas = [
    ("GET integracion ruta inexistente", "GET", "/api/integracion/no-existe"),
    ("POST integracion ruta solo-lectura", "POST", "/api/integracion/sistemas"),
    ("GET integracion doble barra", "GET", "/api/integracion/sistemas//ping"),
    ("GET historias sincronizar (solo POST)", "GET",
     "/api/integracion/historias/api-paciente-ok/sincronizar"),
    ("GET citas ruta inexistente", "GET", "/api/citas/no-existe"),
    ("DELETE citas ruta inexistente", "DELETE", "/api/citas/xx"),
    ("GET rutas fuera de modulo", "GET", "/api/no-existe"),
]
call("huecos libres medico 13/11 (params LocalDate correctos)", "GET",
     "/api/citas/huecos?profesionalId=medico&desde=2026-11-13&hasta=2026-11-13"
     "&duracionMinutos=30", tok)
call("huecos con desde mal tipado (date-time)", "GET",
     "/api/citas/huecos?profesionalId=medico&desde=2026-11-13T00:00:00&hasta=2026-11-13"
     "&duracionMinutos=30", tok)

for e, m, r in sondas:
    call(e + " [con token]", m, r, tok)
for e, m, r in sondas:
    call(e + " [sin token]", m, r, None)

out = []
for e, m, r, st, cuerpo in REG:
    out.append("--- %s | %s %s -> %s\n    %s" % (e, m, r, st, (cuerpo or "").replace("\n", " ")))
sys.stdout.buffer.write("\n".join(out).encode("utf-8"))
with open(r"C:\Users\Ismae\AppData\Local\Temp\opencode\tc19_sonda_rutas.json", "w",
          encoding="utf-8") as fh:
    json.dump([{"paso": e, "metodo": m, "ruta": r, "status": st, "cuerpo": c}
               for e, m, r, st, c in REG], fh, ensure_ascii=False, indent=1)
