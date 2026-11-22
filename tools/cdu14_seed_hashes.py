#!/usr/bin/env python3
"""Hashes de la semilla V1.6.0 (espera_auditoria).

Canon de AuditoriaEspera.canonico:
  ACCION|esperaId|antes->despues|usuario|motivo|instante|hashAnterior
con antes/motivo nulos como cadena vacia y hashAnterior nulo como 'GENESIS'.

La cadena es GLOBAL por id ascendente (asentar usa findTopByOrderByIdDesc y
verificarAuditoria usa findAllByOrderByIdAsc), igual que cita_auditoria: el primer
eslabon enlaza con GENESIS y cada fila enlaza con la anterior de la TABLA.

Instantes sin nanos en ISO-Z para que Instant.toString() round-tree igual desde
TIMESTAMP(6) WITH TIME ZONE (h2) y DATETIME(6) UTC (mysql); es el mismo criterio
que la semilla de cita_auditoria de V1.5.0.
"""
import hashlib

def canon(accion, espera, antes, despues, usuario, motivo, instante, h_ant):
    return ("%s|%s|%s->%s|%s|%s|%s|%s" % (
        accion, espera, antes or "", despues, usuario, motivo or "", instante,
        h_ant or "GENESIS"))

def sha256hex(texto):
    return hashlib.sha256(texto.encode("utf-8")).hexdigest()

# Orden cronologico de siembra (el id de la tabla sigue este orden; en la semilla
# el instante del eslabon ALTA coincide con la fecha_solicitud de la entrada).
# El esperaId del contrato es UUID (INEXISTENTE/MAL_FORMATADO en la spec), asi que la
# semilla usa UUIDs fijos legibles (e5c014 + numero de entrada) y no las etiquetas
# ESPERA-SEM-*, que solo viven aqui y en los comentarios de la migracion como nombre
# humano de cada fila.
# Demo: la cola ACTIVA de SEM-RM-RODILLA queda URGENTE (alta posterior, reubicada)
# DELANTE de ORDINARIA (alta anterior): la prioridad manda sobre la antiguedad.
SEM = {
    "001": "e5c01400-0000-4000-8000-000000000001",
    "002": "e5c01400-0000-4000-8000-000000000002",
    "003": "e5c01400-0000-4000-8000-000000000003",
    "004": "e5c01400-0000-4000-8000-000000000004",
    "005": "e5c01400-0000-4000-8000-000000000005",
}

FILA = [
    ("ALTA",        "002", None,       "ACTIVA",   "recepcionista", None,                                       "2026-11-05T09:00:00Z"),
    ("ALTA",        "005", None,       "ACTIVA",   "recepcionista", None,                                       "2026-11-05T11:00:00Z"),
    ("ALTA",        "001", None,       "ACTIVA",   "recepcionista", None,                                       "2026-11-06T07:00:00Z"),
    ("RETIRADA",    "005", "ACTIVA",   "RETIRADA", "recepcionista", "el paciente ya fue atendido en otro centro", "2026-11-06T08:30:00Z"),
    ("ALTA",        "003", None,       "ACTIVA",   "recepcionista", None,                                       "2026-11-06T09:30:00Z"),
    ("ALTA",        "004", None,       "ACTIVA",   "recepcionista", None,                                       "2026-11-06T09:45:00Z"),
    ("ASIGNACION",  "004", "ACTIVA",   "ASIGNADA", "recepcionista", None,                                       "2026-11-06T10:15:00Z"),
    ("REUBICACION", "001", "ORDINARIA","URGENTE",  "recepcionista", "revaloracion medica: dolor agudo",         "2026-11-07T07:00:00Z"),
]

anterior = None
for (accion, etiqueta, antes, despues, usuario, motivo, instante) in FILA:
    espera = SEM[etiqueta]
    c = canon(accion, espera, antes, despues, usuario, motivo, instante, anterior)
    h = sha256hex(c)
    print(f"-- {accion:12} {espera}")
    print(f"--   canon = {c}")
    print("    ('%s', '%s', %s, '%s', %s, '%s', '%s', %s)," % (
        accion, espera,
        ("'%s'" % antes) if antes else "NULL",
        despues,
        ("'%s'" % motivo) if motivo else "NULL",
        instante, h,
        ("'%s'" % anterior) if anterior else "NULL"))
    anterior = h
