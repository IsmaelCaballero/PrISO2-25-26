"""Complemento del TC19-04: paso de citas con integracion degradada y hueco dentro de franja.

Franjas sembradas: medico 2026-11-09/11/13 de 08:00 a 15:00; tecnico 2026-11-10/12 de
09:00 a 14:00. Huecos ocupados por semilla el 13/11: 08:30-09:00 (SEM-001),
10:00-10:30 (SEM-003). Libre: 09:00-10:00.
"""
import json
import sys
import time
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
REG = []


def call(tc, etiqueta, metodo, ruta, cuerpo=None, token=None):
    datos = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8") if cuerpo is not None else None
    req = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    if datos is not None:
        req.add_header("Content-Type", "application/json")
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


tok, t = call("setup", "login admin", "POST", "/api/auth/login",
              {"usuario": "admin", "password": "iso2-admin"})[1], None
token = json.loads(REG[-1]["cuerpo"])["token"]

# 1) Integracion degradada (HCE/RIS/PACS caidos) y despues una cita dentro de franja libre.
for sis in ("hce", "ris", "pacs"):
    call("TC19-04", "simulacion %s CAIDO" % sis, "POST", "/api/integracion/simulacion",
         {"sistema": sis, "modo": "CAIDO", "latenciaMs": 0, "timeoutMs": 0,
          "reintentos": 0}, token)
s, t = call("TC19-04", "agendar cita 13/11 09:00 con integracion caida", "POST", "/api/citas",
            {"solicitudId": None, "profesionalId": "medico", "inicio": "2026-11-13T09:00:00",
             "duracionMinutos": 30}, token)
cita_id = (json.loads(t) or {}).get("citaId") if s == 201 else None
# 2) Segundo intento sobre el mismo hueco: debe chocar contra el UNIQUE (409).
call("TC19-04", "repetir cita mismo hueco (doble clic)", "POST", "/api/citas",
     {"solicitudId": None, "profesionalId": "medico", "inicio": "2026-11-13T09:00:00",
      "duracionMinutos": 30}, token)
# 3) Comprobar la lista de huecos libres del medico el 13/11 (el 09:00 ya no debe salir).
call("TC19-04", "huecos libres medico 13/11", "GET",
     "/api/citas/huecos?profesionalId=medico&desde=2026-11-13T00:00:00&hasta=2026-11-14T00:00:00"
     "&duracionMinutos=30", token=token)
# 4) Integracion degradada sigue degradada mientras citas funciona (estudio RIS -> 503).
call("TC19-04", "estudio RIS con caida activa", "GET", "/api/integracion/estudios/ACC-0001",
     token=token)
call("TC19-04", "ping HCE con caida activa", "GET", "/api/integracion/sistemas/hce/ping",
     token=token)
# 5) Devolver el sistema a NORMAL y cancelar la cita creada (no dejar basura).
for sis in ("hce", "ris", "pacs"):
    call("setup", "simulacion %s NORMAL" % sis, "POST", "/api/integracion/simulacion",
         {"sistema": sis, "modo": "NORMAL", "latenciaMs": 0, "timeoutMs": 0,
          "reintentos": 0}, token)
if cita_id:
    call("setup", "cancelar cita creada", "POST", "/api/citas/%s/cancelar" % cita_id,
         {"motivo": "cierre de prueba TC19-04"}, token)

sys.stdout.buffer.write(json.dumps(REG, ensure_ascii=False, indent=1).encode("utf-8"))
with open(r"C:\Users\Ismae\AppData\Local\Temp\opencode\tc19_registro_cita.json", "w",
          encoding="utf-8") as fh:
    json.dump(REG, fh, ensure_ascii=False, indent=1)
