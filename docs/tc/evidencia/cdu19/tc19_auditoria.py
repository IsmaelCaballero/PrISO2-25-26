"""Evidencia completa de la cadena de auditoria para TC19-02 y TC19-06.

Los registros generales guardan el cuerpo cortado a 700 caracteres, y justo ahi es donde la
auditoria necesita el cuerpo entero (hay que poder ver el eslabon ultimo). Esta pasada deja la
auditoria completa en docs/tc/evidencia/cdu19/auditoria-cdu19.json, con el JWT fuera.

Secuencia: login -> 3 sincronizaciones del paciente estable -> auditoria -> 2 del rotativo ->
auditoria. Se deja la simulacion en NORMAL.
"""
import json
import re
import urllib.error
import urllib.request
from pathlib import Path

BASE = "http://localhost:8080"
SALIDA = Path(r"C:\ProgIA\PrISO2-2025-2026\docs\tc\evidencia\cdu19\auditoria-cdu19.json")
JWT = re.compile(r'("token"\s*:\s*")[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+(")')


def sanea(txt):
    if txt is None:
        return None
    return JWT.sub(r"\1<JWT-omitido>\2", txt)


def http(metodo, ruta, cuerpo=None, token=None):
    """Crudo: el login necesita el token real, asi que el saneado solo se aplica al grabar."""
    datos = json.dumps(cuerpo).encode("utf-8") if cuerpo is not None else None
    req = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=25) as r:
            return r.status, r.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")


def normal(reg):
    return http("POST", "/api/integracion/simulacion",
                {"sistema": reg["sistema"], "modo": "NORMAL", "latenciaMs": 0,
                 "timeoutMs": 0, "reintentos": 0})


def main():
    _, cuerpo = http("POST", "/api/auth/login",
                     {"usuario": "admin", "password": "iso2-admin"})
    tok = json.loads(cuerpo)["token"]
    registro = []

    def apunta(paso, metodo, ruta, token=None):
        st, cpo = http(metodo, ruta, token=token)
        registro.append({"paso": paso, "metodo": metodo, "ruta": ruta, "status": st,
                         "cuerpo": cpo})

    for pac in ("api-paciente-ok", "api-paciente-ok", "api-paciente-ok"):
        st, cpo = http("POST", "/api/integracion/historias/%s/sincronizar" % pac,
                       cuerpo={}, token=tok)
        registro.append({"paso": "sincronizar " + pac, "metodo": "POST",
                         "ruta": "/api/integracion/historias/%s/sincronizar" % pac,
                         "status": st,
                         "huella": json.loads(cpo).get("huella"),
                         "reutilizado": json.loads(cpo).get("reutilizado")})
    apunta("auditoria tras 3 del paciente estable", "GET", "/api/integracion/auditoria", tok)

    for _ in range(2):
        st, cpo = http("POST", "/api/integracion/historias/api-paciente-rotativo/sincronizar",
                       cuerpo={}, token=tok)
        registro.append({"paso": "sincronizar api-paciente-rotativo", "metodo": "POST",
                         "ruta": "/api/integracion/historias/api-paciente-rotativo/sincronizar",
                         "status": st,
                         "huella": json.loads(cpo).get("huella"),
                         "reutilizado": json.loads(cpo).get("reutilizado")})
    apunta("auditoria tras el rotativo", "GET", "/api/integracion/auditoria", tok)

    normal({"sistema": "hce"})
    SALIDA.write_text(sanea(json.dumps(registro, ensure_ascii=False, indent=1)),
                      encoding="utf-8")

    for r in registro:
        if r["paso"].startswith("auditoria"):
            cpo = json.loads(r["cuerpo"])
            esla = cpo["eslabones"]
            print("%s -> %s | cadenaIntegra=%s totalRegistros=%s eslabones=%s"
                  % (r["paso"], r["status"], cpo["cadenaIntegra"], cpo["totalRegistros"],
                     len(esla)))
            for e in esla[-4:]:
                print("   #%s %s/%s actor=%s hashAnt=%s hash=%s"
                      % (e["secuencia"], e["accion"], e["resultado"], e["actor"],
                         (e["hashAnterior"] or "")[:14], e["hash"][:14]))
        else:
            print("%s -> %s | reutilizado=%s huella=%s"
                  % (r["paso"], r["status"], r.get("reutilizado"),
                     (r.get("huella") or "")[:14]))
    print("escrito en", SALIDA)


if __name__ == "__main__":
    main()
