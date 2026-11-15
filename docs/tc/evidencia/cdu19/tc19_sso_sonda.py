"""Sonda final TC19-01: conjetura de errores con la clave correcta 'contrasena'."""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
out = []


def call(etiqueta, cuerpo):
    req = urllib.request.Request(BASE + "/api/integracion/sso/login",
                                 data=json.dumps(cuerpo).encode(), method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("Authorization", "Bearer " + TOK)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            out.append((etiqueta, r.status, r.read().decode()[:260]))
    except urllib.error.HTTPError as e:
        out.append((etiqueta, e.code, e.read().decode()[:260]))


req = urllib.request.Request(BASE + "/api/auth/login",
                             data=json.dumps({"usuario": "admin", "password": "iso2-admin"}).encode(),
                             method="POST")
req.add_header("Content-Type", "application/json")
with urllib.request.urlopen(req, timeout=15) as r:
    TOK = json.loads(r.read().decode())["token"]

call("contrasena erronea (clave correcta)", {"usuario": "idp-admin", "contrasena": "mal"})
call("usuario IdP inexistente", {"usuario": "idp-que-no-existe", "contrasena": "idp-pass"})
call("usuario vacio", {"usuario": "", "contrasena": "idp-pass"})
call("dos factores correctos repetidos", {"usuario": "idp-admin", "contrasena": "idp-pass"})

txt = "\n".join("--- %s -> %s\n    %s" % (e, st, (c or "").replace("\n", " "))
               for e, st, c in out)
sys.stdout.buffer.write(txt.encode("utf-8"))
with open(r"C:\Users\Ismae\AppData\Local\Temp\opencode\tc19_sso_sonda.json", "w",
          encoding="utf-8") as fh:
    json.dump([{"paso": e, "status": st, "cuerpo": c} for e, st, c in out],
              fh, ensure_ascii=False, indent=1)
