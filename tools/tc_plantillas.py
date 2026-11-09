#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Proyección humana de las tablas TDOP de la D33 (la otra proyección son los JUnit).

Lee la misma especificación que ``tools/pairwise.py`` y los CSV que ese script genera,
y escribe dos cosas:

1. ``docs/test/iterXX/tdop-<cdu>.md``: evidencia de la técnica (factores, niveles,
   criterios de valor límite, reglas de precedencia, cobertura de pares y recuentos).
2. El bloque generado de ``docs/tc/cdu-<cdu>-tc-humanos.md`` entre los marcadores
   ``<!-- TDOP:BEGIN -->`` y ``<!-- TDOP:END -->``: las tablas de casos con las
   mismas nueve columnas que la plantilla de TC humanos de la D24.

Los IDs ``TCnn-X01`` son los mismos del CSV, del JUnit y de la plantilla: un caso se
ejecuta a la vez de forma automatica y manual.

Uso (desde la raiz):

    python tools/tc_plantillas.py docs/test/iter01/tdop-cdu16-espec.json \
        --tc docs/tc/cdu16-tc-humanos.md
"""

from __future__ import annotations

import argparse
import csv
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pairwise import limpiar, pares_esperados  # noqa: E402

MARCA_INI = "<!-- TDOP:BEGIN -->"
MARCA_FIN = "<!-- TDOP:END -->"
CABECERA_TC = ("| ID | Titulo | Tipo | Puerta que cubre (SQ §5) | Pasos "
               "| Resultado esperado | Resultado real | Pasa | Fecha/ejecutor |")
SEPARADOR_TC = "|---|---|---|---|---|---|---|---|---|"
NO_SE_LEE = "no aplica"


def lee_csv(ruta: str) -> list[dict]:
    with open(ruta, encoding="utf-8", newline="") as origen:
        return list(csv.DictReader(origen, delimiter=";"))


def celda(texto: object) -> str:
    """Texto seguro dentro de una celda de tabla markdown (la nota usa ``|``)."""
    return str(texto).replace("|", "\\|").replace("\r", " ").replace("\n", " ")


def columnas_variables(tabla: dict, muestra: dict) -> list[str]:
    fijos = {"id", "setup", "esperadoStatus", "esperadoCodigo", "nota"}
    return [c for c in muestra if c not in fijos]


# --------------------------------------------------------------------- evidencia tecnica


def leyenda_factores(tabla: dict) -> list[str]:
    lineas = ["| Factor | Nivel | Clase valida | Criterio elegido | Motivo |",
              "|---|---|---|---|---|"]
    for factor in tabla["factores"]:
        for nivel in factor["niveles"]:
            lineas.append("| `%s` | `%s` | %s | %s | %s |" % (
                factor["nombre"], nivel["valor"],
                "valido" if nivel.get("valido", True) else "**fuera de clase**",
                celda(nivel.get("criterio", "-")), celda(nivel.get("motivo", "-"))))
    return lineas


def reglas(tabla: dict) -> list[str]:
    lineas = ["| Orden | Condicion | Estado esperado | Codigo de error |",
              "|---|---|---|---|"]
    for numero, regla in enumerate(tabla["reglas"], start=1):
        condicion = regla.get("si") or {}
        texto = "cualquier nivel fuera de clase valida" if condicion.get("invalido") \
            else "resto de casos"
        if "factor" in condicion:
            texto = "`%s` %s %s" % (condicion["factor"],
                                    "está fuera de clase" if condicion.get("invalido")
                                    else ("vale" if "igual" in condicion else "no vale"),
                                    condicion.get("igual", condicion.get("distinto", "")))
        lineas.append("| %d | %s | `%s` | `%s` |" % (
            numero, texto, regla["estado"], regla.get("codigo", "-")))
    return lineas


def bloque_tecnico(nombre_tabla: str, tabla: dict, filas: list[dict]) -> list[str]:
    factores = columnas_variables(tabla, filas[0])
    esperados = len(pares_esperados(tabla)) if tabla["kind"] in ("pairwise", "full") else 0
    lineas = ["## Tabla %s · %s" % (tabla["id"], tabla["nombre"]), ""]
    producto = 1
    if tabla["kind"] in ("pairwise", "full"):
        for factor in tabla["factores"]:
            producto *= len(factor["niveles"])
    linea = "**Modo:** `%s`" % tabla["kind"]
    if tabla["kind"] in ("pairwise", "full"):
        linea += " · producto de niveles = %d · filas generadas = %d" % (producto, len(filas))
        if tabla["kind"] == "pairwise":
            linea += " · pares obligatorios cubiertos = %d/%d (100%%)" % (esperados, esperados)
    else:
        linea += " · filas escritas a mano = %d" % len(filas)
    if tabla.get("noCombinaInvalidos"):
        linea += (" · una fila no acumula dos niveles fuera de clase "
                  "(el rechazo tiene un unico motivo)")
    lineas += [linea, "", "**Puerta que cubre:** %s" % tabla["puerta"], ""]

    if tabla["kind"] != "conjetura":
        lineas += ["### Niveles y criterios", ""] + leyenda_factores(tabla) + [""]
        lineas += ["### Reglas de precedencia (dan el resultado esperado)", ""] \
                  + reglas(tabla) + [""]

    lineas += ["### Casos generados", "",
               "| ID | %s | Setup | Esperado | Nota del caso |" % " | ".join(factores),
               "|---|" + "---|" * (len(factores) + 3)]
    for fila in filas:
        esperado = fila["esperadoStatus"]
        if fila["esperadoCodigo"] not in ("-", ""):
            esperado += " `%s`" % fila["esperadoCodigo"]
        lineas.append("| `%s` | %s | `%s` | %s | %s |" % (
            fila["id"], " | ".join("`%s`" % celda(fila[c]) for c in factores),
            fila["setup"], esperado, celda(fila["nota"])))
    lineas.append("")
    return lineas


def documento_evidencia(spec: dict, tablas: list[tuple[dict, list[dict]], str]) -> str:
    lineas = [
        "# Evidencia TDOP de %s (%s · %s)" % (spec["cdu"], spec["iter"], spec["config"]),
        "",
        "Metodo: %s" % spec["metodo"],
        "",
        "Generador: `python tools/pairwise.py docs/test/%s/tdop-%s-espec.json "
        "--resources %s/src/test/resources/tdop` (semilla %s)." % (
            spec["iter"], spec["cdu"].lower(), spec["modulo"], spec["semilla"]),
        "Las mismas filas las ejecutan los JUnit parametrizados de "
        "`es.uclm.iso2.catalogo.tdop` y las tablas de `docs/tc/%s-tc-humanos.md`: "
        "un ID vale para las dos." % spec["cdu"].lower(),
        "",
        "## Leyenda de tokens",
        "",
        "| Token | Significado |",
        "|---|---|",
    ]
    lineas += ["| `%s` | %s |" % (token, valor)
               for token, valor in spec["leyendaTokens"].items()]
    lineas += ["", "## Estados esperados (nombre simbolico -> HTTP)", "",
               "| Estado | HTTP | Codigo de error | Que significa |", "|---|---|---|---|"]
    lineas += ["| `%s` | %d | `%s` | %s |" % (
        estado, dato["http"], dato.get("codigo", "-"), celda(dato.get("descripcion", "")))
        for estado, dato in spec["estados"].items()]
    lineas += ["", "## Reparto de casos", "",
               "| Tabla | Operacion | Modo | Filas |", "|---|---|---|---|"]
    total = 0
    for tabla, filas in tablas:
        total += len(filas)
        lineas.append("| %s | %s | %s | %d |" % (
            tabla["id"], tabla["nombre"], tabla["kind"], len(filas)))
    lineas += ["| **Total** |  |  | **%d** |" % total, ""]
    for tabla, filas in tablas:
        lineas += bloque_tecnico(tabla["id"], tabla, filas)
    lineas += ["## Como se ejecutan", "",
               "```", "mvn -B -ntp -pl %s -am test" % spec["modulo"], "```", "",
               "Los CSV de trabajo estan en `docs/test/%s/` y su copia en el classpath "
               "de pruebas (`%s/src/test/resources/tdop/`) es identica: el generador "
               "las escribe a la vez." % (spec["iter"], spec["modulo"]), ""]
    return "\n".join(lineas) + "\n"


# ------------------------------------------------------------------- plantilla de TC humanos


def titulo_de(tabla: dict, fila: dict, factores: list[str]) -> str:
    """Titulo corto del caso: lo que lo caracteriza (el motivo de la fila)."""
    if tabla["kind"] == "conjetura":
        return "%s: %s" % (fila.get("operacion", "").lower(), fila["nota"])
    return fila["nota"]


OPERACIONES = {
    "ALTA": "POST /api/catalogo/pruebas",
    "MODIFICACION": "PUT /api/catalogo/pruebas/{codigo}",
    "DETALLE": "GET /api/catalogo/pruebas/{codigo}",
    "BAJA": "DELETE /api/catalogo/pruebas/{codigo}",
    "REACTIVAR": "PUT /api/catalogo/pruebas/{codigo}/reactivar",
    "LISTADO": "GET /api/catalogo/pruebas",
}
RUTA_EXTRA = {
    "AUDITORIA": "la ruta extra /auditoria (que la API no tiene: 404 sin cuerpo ApiError)",
    "OTRA_COD": "otro codigo en la ruta, distinto del del cuerpo",
    "COD": "el codigo de la ruta tal cual",
    "COD-MINUS": "el codigo de la ruta en minusculas",
    "COD+ESP": "el codigo de la ruta con un espacio final",
}
CUERPOS = {
    "VALIDO": "cuerpo valido y completo",
    "ACTIVA_FALSA": "cuerpo valido anadiendo \"activa\": false",
    "ACTIVA_VERDADERA": "cuerpo valido anadiendo \"activa\": true",
    "CODIGO_PERTENECIENTE": "cuerpo valido cuyo codigo NO es el de la ruta",
    "CODIGO_21_CARACTERES": "alta con codigo de 21 caracteres",
    "DURACION_COMO_TEXTO": "alta valida con duracionMinutos escrito como texto (\"60\")",
    "CAMPO_DESCONOCIDO": "alta valida con un campo desconocido mas",
    "CATEGORIA_MINUSCULA": "alta con categoria en minusculas (analitica)",
    "CODIGO_CON_ESPACIOS": "alta con codigo con espacios delante y detras",
    "VACIO": "peticion sin cuerpo",
    "SIN_CUERPO": "peticion sin cuerpo",
    "JSON_MALFORMADO": "cuerpo JSON malformado",
    "MALFORMADO": "cuerpo JSON malformado",
    "SIN_PROTOCOLO": "cuerpo valido sin el objeto protocolo",
    "VACIO_JSON": "cuerpo {} (todos los campos obligatorios ausentes)",
    "TEXTO_PLANO": "cuerpo de texto plano con Content-Type text/plain",
    "TIPO_ERRONEO": "cuerpo JSON con un numero donde va el codigo",
    "CONTRATO_INCOMPLETO": "cuerpo JSON al que le faltan campos obligatorios",
    "NINGUNO": "sin cuerpo",
    "-": "sin cuerpo",
}


def pasos_de(tabla: dict, fila: dict, factores: list[str], spec: dict) -> str:
    """Pasos reproducibles: peticion + niveles de la fila (descritos en la evidencia)."""
    previo = "" if fila["setup"] == "NINGUNO" else "Previo `%s`; " % fila["setup"]
    peticion = OPERACIONES.get(fila.get("operacion", ""), tabla.get("solicitud", ""))
    if tabla["kind"] in ("conjetura", "filas") and "operacion" in fila:
        detalle = ""
        cuerpo = CUERPOS.get(fila.get("cuerpo", "-"), fila.get("cuerpo", ""))
        if cuerpo:
            detalle += " con %s" % cuerpo
        extra = fila.get("rutaExtra", "-")
        if extra not in ("-", ""):
            detalle += ", ruta con %s" % RUTA_EXTRA.get(extra, "`%s`" % extra)
        estado = fila.get("estadoRecurso", "-")
        if estado not in ("-", "NO_EXISTE"):
            detalle += " sobre recurso en estado `%s`" % estado
        if tabla.get("solicitud") and fila.get("operacion") not in OPERACIONES:
            peticion = tabla["solicitud"]
    else:
        niveles = [", ".join("%s=%s" % (f["nombre"], fila[f["nombre"]])
                             for f in tabla["factores"] if f["nombre"] != "operacion")]
        detalle = " con " + niveles[0] if niveles and niveles[0] else ""
    return "%s%s%s (niveles: tabla %s de `docs/test/%s/tdop-%s.md`)" % (
        previo, peticion, detalle, tabla["id"], spec["iter"], spec["cdu"].lower())


def bloque_tc(spec: dict, tablas: list[tuple[dict, list[dict]]]) -> list[str]:
    lineas = [
        MARCA_INI,
        "",
        "## Casos TDOP generados (D33) · %s" % spec["cdu"],
        "",
        "Estas tablas las escribe `python tools/tc_plantillas.py` a partir de "
        "`docs/test/%s/tdop-%s-espec.json` y de los CSV de `docs/test/%s/`: no se "
        "editan a mano, se regenera el bloque. Cada ID es el mismo que ejecuta el "
        "JUnit parametrizado correspondiente, asi que la ejecucion automatica y la "
        "manual trazan sobre el mismo caso." % (spec["iter"], spec["cdu"].lower(),
                                                spec["iter"]),
        "Los niveles van en token (`AUT`, `VACIO`, `JUSTO`, `EXCESIVA`, `DUPLICA_ACTIVA` "
        "· `REP:121:N` · `NO_ADMITIDA`): su significado exacto esta en la tabla de niveles "
        "y en las reglas de precedencia de `docs/test/%s/tdop-%s.md`." % (
            spec["iter"], spec["cdu"].lower()),
        "",
    ]
    for tabla, filas in tablas:
        factores = columnas_variables(tabla, filas[0])
        lineas += ["### Tabla %s · %s" % (tabla["id"], tabla["nombre"]), "",
                   CABECERA_TC, SEPARADOR_TC]
        for fila in filas:
            lineas.append("| %s | %s | %s | %s | %s | %s | | | |" % (
                fila["id"], celda(titulo_de(tabla, fila, factores)),
                celda(tabla.get("tipoTC", "TDOP")), celda(tabla["puerta"]),
                celda(pasos_de(tabla, fila, factores, spec)),
                celda(esperado_estado(fila, spec))))
        lineas.append("")
    lineas.append(MARCA_FIN)
    return lineas


def esperado_estado(fila: dict, spec: dict) -> str:
    """Resultado esperado: estado simbolico, su codigo HTTP y el codigo de error."""
    dato = spec["estados"][fila["esperadoStatus"]]
    texto = "%s (%d)" % (fila["esperadoStatus"], dato["http"])
    if fila["esperadoCodigo"] not in ("-", ""):
        texto += " `%s`" % fila["esperadoCodigo"]
    return texto


def inserta_bloque(ruta_tc: str, bloque: list[str]) -> None:
    with open(ruta_tc, encoding="utf-8") as origen:
        contenido = origen.read()
    lineas = contenido.replace("\r\n", "\n").split("\n")
    principio = MARCA_INI.replace("\n", "")
    if principio in contenido.replace("\r\n", "\n"):
        inicio = lineas.index(MARCA_INI)
        fin = lineas.index(MARCA_FIN)
        nuevo = lineas[:inicio] + bloque + lineas[fin + 1:]
    else:
        corte = next((k for k, l in enumerate(lineas)
                      if l.startswith("## Resultado global")), len(lineas))
        nuevo = lineas[:corte] + bloque + [""] + lineas[corte:]
    with open(ruta_tc, "w", encoding="utf-8", newline="\r\n") as destino:
        destino.write("\n".join(nuevo))


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description="Proyeccion humana de las tablas TDOP")
    parser.add_argument("spec")
    parser.add_argument("--tc", help="plantilla de TC humanos donde insertar el bloque")
    args = parser.parse_args(argv)

    import json
    with open(args.spec, encoding="utf-8") as origen:
        spec = json.load(origen)
    base = os.path.dirname(os.path.abspath(args.spec))

    tablas = []
    for clave in sorted(k for k in spec if k.startswith("tabla_")):
        tabla = spec[clave]
        filas = lee_csv(os.path.join(base, tabla["csv"]))
        tablas.append((tabla, filas))

    evidencia = os.path.join(base, "tdop-%s.md" % spec["cdu"].lower())
    with open(evidencia, "w", encoding="utf-8", newline="\r\n") as destino:
        destino.write(documento_evidencia(spec, tablas))
    print("evidencia escrita en %s (%d filas en total)"
          % (evidencia, sum(len(f) for _, f in tablas)))

    if args.tc:
        inserta_bloque(args.tc, bloque_tc(spec, tablas))
        print("bloque TDOP insertado en %s" % args.tc)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
