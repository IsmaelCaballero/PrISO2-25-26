#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generador TDOP de la D33: convierte la especificacion de tablas de decision de
entrada (``docs/test/iterXX/tdop-<cdu>-espec.json``) en los CSV que consumen a la vez

* los tests JUnit parametrizados (``@CsvFileSource``), y
* las plantillas de TC humanos de ``docs/tc`` (``tools/tc_plantillas.py``).

Una sola fuente de casos: el mismo id ``TCnn-X01`` aparece en el JUnit y en la
plantilla, asi que la ejecucion automatica y la manual trazan sobre el mismo caso.

Tipos de composicion por tabla (campo ``kind``):

* ``pairwise``  -> cobertura de todos los pares de niveles de factores distintos
                   (voraz aleatorio con reparacion, semilla fija). Con
                   ``noCombinaInvalidos`` una fila nunca acumula dos niveles fuera
                   de clase valida; con ``noEmparejar`` los niveles del factor
                   indicado se prueban uno por fila sobre base valida y no generan
                   pares obligatorios (si no, un rechazo de ese factor taparia el
                   resultado de los demas niveles y la fila no seria atribuible).
                   ``filasBase`` son combinaciones fijadas a mano que el voraz no
                   puede podar, y ``estadosBase`` exige que la tabla observe cada
                   uno de esos estados esperados (se comprueba al generar).
* ``full``      -> producto completo (solo cuando el producto es pequeno).
* ``filas``     -> enumeracion completa escrita a mano en la spec (tabla D: con dos
                   factores el producto es pequeno, pero se prefiere escribirlo a
                   poder generarlo y podarlo).
* ``conjetura`` -> filas escritas a mano en la propia spec (tabla E).

Uso (desde la raiz del repositorio):

    python tools/pairwise.py docs/test/iter01/tdop-cdu16-espec.json \
        --resources iso2-catalogo/src/test/resources/tdop

Escribe ``tdop-<cdu>-<tabla>.csv`` junto a la spec y, si se indica ``--resources``,
una copia identica en el classpath de pruebas. Los CSV usan ``;`` como separador,
UTF-8 sin BOM y saltos de linea CRLF, y no contienen comillas ni ``;`` en los valores:
los valores largos se expresan con tokens (``REP:1000:P``) que expande el test.
"""

from __future__ import annotations

import argparse
import itertools
import json
import os
import random
import sys

CAMPOS_FIJOS = ["id", "setup", "esperadoStatus", "esperadoCodigo", "nota"]


# ----------------------------------------------------------------------------- utilidades


def limpiar(valor: str) -> str:
    """Quita del CSV los caracteres que romperian ``@CsvFileSource``."""
    return str(valor).replace(";", ",").replace('"', "'").strip()


def niveles_de(tabla: dict) -> list[list[dict]]:
    return [factor["niveles"] for factor in tabla["factores"]]


def producto(tabla: dict) -> int:
    total = 1
    for factor in tabla["factores"]:
        total *= len(factor["niveles"])
    return total


def pares_de(fila: list[int], n: int) -> set[tuple[int, int, int, int]]:
    return {(i, fila[i], j, fila[j])
            for i, j in itertools.combinations(range(n), 2)}


# ----------------------------------------------------------------------------- composicion


def pairwise(tabla: dict, semilla: int) -> list[list[int]]:
    """Cubre todos los pares obligatorios con el menor numero de filas.

    Tres pasos: (1) una fila base con todos los niveles validos y un caso por cada
    nivel invalido, (2) se tapan los pares que faltan eligiendo siempre el par cuyo
    nivel esta menos repetido entre los pendientes y completando el resto de factores
    con el nivel que aporta mas pares nuevos, y (3) poda de filas redundantes.

    Si la tabla declara ``noCombinaInvalidos`` (practica habitual en TDOP), una fila
    nunca acumula dos niveles fuera de su clase valida: asi el rechazo observado tiene
    un unico motivo atribuible y los pares (invalido, invalido) quedan fuera de la
    obligacion de cobertura.
    """
    azar = random.Random(semilla)
    niveles = niveles_de(tabla)
    n = len(niveles)
    invalido = [[not nivel.get("valido", True) for nivel in factor["niveles"]]
                for factor in tabla["factores"]]
    validos = [[k for k, mal in enumerate(fila) if not mal] for fila in invalido]
    defecto = [fila[0] if fila else 0 for fila in validos]
    sin_dos = bool(tabla.get("noCombinaInvalidos"))
    # Factores que no emparejan (``noEmparejar``): combinar dos de sus niveles
    # rechazados con otros factores taparia el motivo real del rechazo (un cuerpo
    # ilegitimo oculta cualquier otro fallo). Se prueban uno por fila sobre una
    # base valida y no generan pares obligatorios.
    excluidos = {k for k, f in enumerate(tabla["factores"])
                 if f["nombre"] in tabla.get("noEmparejar", [])}

    obligatorios: set[tuple[int, int, int, int]] = set()
    for i, j in itertools.combinations(range(n), 2):
        if i in excluidos or j in excluidos:
            continue
        for vi in range(len(niveles[i])):
            for vj in range(len(niveles[j])):
                if sin_dos and invalido[i][vi] and invalido[j][vj]:
                    continue
                obligatorios.add((i, vi, j, vj))

    cubiertos: set[tuple[int, int, int, int]] = set()
    filas: list[list[int]] = []
    intocables: set[int] = set()

    def añadir(fila: list[int], intocable: bool = False) -> None:
        filas.append(fila)
        if intocable:
            intocables.add(id(fila))
        cubiertos.update(p for p in pares_de(fila, n) if p in obligatorios)

    def completa(fila: list[int], fijos: set[int]) -> list[int]:
        """Elige, para los factores libres, el nivel que cubre mas pares pendientes."""
        for k in excluidos:
            if k not in fijos:
                fila[k] = defecto[k]
        orden = [k for k in range(n) if k not in fijos and k not in excluidos]
        azar.shuffle(orden)
        fijados = set(fijos)
        for k in orden:
            hay_invalido = sin_dos and any(invalido[m][fila[m]] for m in fijados)
            mejor, ganancia = None, -1
            for v in range(len(niveles[k])):
                if hay_invalido and invalido[k][v]:
                    continue
                prueba = list(fila)
                prueba[k] = v
                aporte = len((pares_de(prueba, n) & obligatorios) - cubiertos)
                if aporte > ganancia:
                    mejor, ganancia = v, aporte
            fila[k] = mejor if mejor is not None else azar.choice(validos[k])
            fijados.add(k)
        return fila

    def factor_pos(nombre: str) -> int:
        for k, f in enumerate(tabla["factores"]):
            if f["nombre"] == nombre:
                return k
        raise ValueError("no existe el factor %s en la tabla %s"
                         % (nombre, tabla["id"]))

    def nivel_pos(k: int, valor: str) -> int:
        for j, nivel in enumerate(tabla["factores"][k]["niveles"]):
            if nivel["valor"] == valor:
                return j
        raise ValueError("la fila base %s=%s no existe en la tabla %s"
                         % (tabla["factores"][k]["nombre"], valor, tabla["id"]))

    # 1) camino feliz, las filas base declaradas en la spec (reglas cuya observacion
    # el voraz podria acabar ensombreciendo) y un caso por cada nivel invalido.
    añadir(list(defecto))
    for fila_fija in tabla.get("filasBase", []):
        fila = list(defecto)
        for factor, valor in fila_fija.items():
            k = factor_pos(factor)
            fila[k] = nivel_pos(k, valor)
        añadir(fila, intocable=True)
    if sin_dos:
        for i in range(n):
            for vi in range(len(niveles[i])):
                if invalido[i][vi]:
                    fila = list(defecto)
                    fila[i] = vi
                    añadir(completa(fila, {i}), intocable=(i in excluidos))

    # 2) a tapar los pares restantes: en cada ronda se prueban los pares cuyos
    # niveles estan menos repetidos y se queda la fila que mas pares nuevos aporta.
    while pendientes := (obligatorios - cubiertos):
        frec: dict[tuple[int, int], int] = {}
        for i, vi, j, vj in pendientes:
            frec[(i, vi)] = frec.get((i, vi), 0) + 1
            frec[(j, vj)] = frec.get((j, vj), 0) + 1
        candidatas = sorted(pendientes,
                            key=lambda p: (frec[(p[0], p[1])] + frec[(p[2], p[3])], p))
        mejor_fila, mejor_aporte = None, -1
        for i, vi, j, vj in candidatas[:80]:
            prueba = list(defecto)
            prueba[i], prueba[j] = vi, vj
            aporte = len((pares_de(completa(prueba, {i, j}), n) & obligatorios) - cubiertos)
            if aporte > mejor_aporte:
                mejor_fila, mejor_aporte = prueba, aporte
        añadir(mejor_fila)

    # 3) poda: si una fila no aporta ningun par unico, se elimina. No se poda una
    # fila declarada en filasBase, ni la unica que prueba un nivel de un factor que
    # no empareja (si no, ese nivel no se ejecutaria nunca).
    cambio = True
    while cambio:
        cambio = False
        for indice_fila in reversed(range(len(filas))):
            fila = filas[indice_fila]
            if id(fila) in intocables:
                continue
            if sin_dos and not any(invalido[k][fila[k]] for k in range(n)):
                continue  # las filas totalmente validas son el camino feliz: no se podan
            resto = filas[:indice_fila] + filas[indice_fila + 1:]
            cubiertos_resto: set[tuple[int, int, int, int]] = set()
            niveles_resto: set[tuple[int, int]] = set()
            for otra in resto:
                cubiertos_resto.update(pares_de(otra, n))
                niveles_resto.update((k, otra[k]) for k in excluidos)
            if {(k, fila[k]) for k in excluidos} - niveles_resto:
                continue
            if obligatorios <= cubiertos_resto:
                filas.pop(indice_fila)
                cambio = True

    for k in excluidos:
        sin_probar = set(range(len(niveles[k]))) - {f[k] for f in filas}
        if sin_probar:
            raise SystemExit("el factor %r de la tabla %s tiene niveles sin probar: %s"
                             % (tabla["factores"][k]["nombre"], tabla["id"],
                                [niveles[k][v]["valor"] for v in sorted(sin_probar)]))
    return filas


def composicion(tabla: dict, semilla: int) -> list[list[int]]:
    niveles = niveles_de(tabla)
    if tabla["kind"] == "full":
        return [list(indice) for indice in itertools.product(
            *[range(len(l)) for l in niveles])]
    if tabla["kind"] == "pairwise":
        return pairwise(tabla, semilla)
    raise ValueError("esta composicion no genera filas: " + tabla["kind"])


# ----------------------------------------------------------------------------- resultados


def evaluar(tabla: dict, fila: list[int]) -> tuple[dict, list[tuple[str, dict]]]:
    """Aplica las reglas declaradas en la spec, en orden.

    Cada regla lleva un ``si`` con alguna de estas formas: ``{"invalido": true}``
    (hay algun nivel fuera de clase valida), ``{"factor": f, "invalido": true}``
    (el nivel del factor f esta fuera de su clase valida),
    ``{"factor": f, "igual"|"distinto": v}`` (el nivel del factor f vale / no vale v)
    y ``{}`` como regla por defecto. Devuelve (regla que gana, niveles invalidos).
    """
    posiciones = {f["nombre"]: k for k, f in enumerate(tabla["factores"])}
    invalidos = [(factor["nombre"], factor["niveles"][fila[posiciones[factor["nombre"]]]])
                 for factor in tabla["factores"]
                 if not factor["niveles"][fila[posiciones[factor["nombre"]]]].get("valido", True)]

    for regla in tabla["reglas"]:
        condicion = regla.get("si", {})
        if not condicion:
            return regla, invalidos
        if "factor" in condicion:
            k = posiciones[condicion["factor"]]
            nivel = tabla["factores"][k]["niveles"][fila[k]]
            if condicion.get("invalido"):
                if not nivel.get("valido", True):
                    return regla, invalidos
                continue
            if "igual" in condicion and nivel["valor"] == condicion["igual"]:
                return regla, invalidos
            if "distinto" in condicion and nivel["valor"] != condicion["distinto"]:
                return regla, invalidos
            continue
        if condicion.get("invalido"):
            if invalidos:
                return regla, invalidos
            continue
        raise ValueError("regla %r de la tabla %s no es evaluable"
                         % (condicion, tabla["id"]))
    raise ValueError("la tabla %s no tiene regla por defecto" % tabla["id"])


def nota_de(tabla: dict, fila: list[int], regla: dict, invalidos: list) -> str:
    if invalidos:
        detalles = " + ".join("%s: %s" % (f, n["motivo"]) for f, n in invalidos)
        return "%s | %s" % (regla["porque"], detalles)
    bordes = []
    for factor, indice in zip(tabla["factores"], fila):
        nivel = factor["niveles"][indice]
        if nivel.get("criterio"):
            bordes.append("%s=%s (%s)" % (factor["nombre"], nivel["valor"],
                                          nivel["criterio"]))
    if bordes:
        return "%s | valores limite: %s" % (regla["porque"], ", ".join(bordes))
    return regla["porque"]


def setup_de(tabla: dict, fila: list[int], indice_tabla: str) -> str:
    partes = []
    for factor, indice in zip(tabla["factores"], fila):
        mapa = factor.get("setupPor")
        if mapa:
            valor = factor["niveles"][indice]["valor"]
            if valor in mapa:
                partes.append(mapa[valor])
    if not partes:
        setups = tabla.get("setups") or ["NINGUNO"]
        rotacion = indice_tabla % len(setups)
        partes.append(setups[rotacion])
    return ",".join(partes)


def nivel_invalido(tabla: dict, fila: list[int]) -> tuple[str, str]:
    for factor, indice in zip(tabla["factores"], fila):
        nivel = factor["niveles"][indice]
        if nivel.get("campo"):
            return nivel.get("campo", "-"), nivel.get("nivel", "-")
    return "-", "-"


# ----------------------------------------------------------------------------- salidas


def filas_de(tabla: dict, semilla: int) -> list[dict]:
    """Filas normales (tablas A-D): una fila por combinacion compuesta."""
    columnas = [f["nombre"] for f in tabla["factores"]]
    if "campoInvalido" in tabla.get("columnasExtra", []):
        columnas += ["campoInvalido", "valorInvalido"]
    cabecera = ["id"] + columnas + CAMPOS_FIJOS[1:]

    resultado = []
    for numero, fila in enumerate(composicion(tabla, semilla), start=1):
        regla, invalidos = evaluar(tabla, fila)
        registro = {}
        for factor, indice in zip(tabla["factores"], fila):
            registro[factor["nombre"]] = factor["niveles"][indice]["valor"]
        if "campoInvalido" in columnas:
            registro["campoInvalido"], registro["valorInvalido"] = nivel_invalido(tabla, fila)
        registro["id"] = "%s-%s%02d" % (tabla["id_prefix"], tabla["id"], numero)
        registro["setup"] = setup_de(tabla, fila, numero)
        registro["esperadoStatus"] = regla["estado"]
        registro["esperadoCodigo"] = regla.get("codigo", "-")
        registro["nota"] = nota_de(tabla, fila, regla, invalidos)
        resultado.append({"cabecera": cabecera, "registro": registro})
    return resultado


def filas_conjetura(tabla: dict, prefijo: str) -> list[dict]:
    cabecera = ["id"] + tabla["columnas"][1:]
    salida = []
    for numero, fila in enumerate(tabla["filas"], start=1):
        registro = {"id": "%s-%s%02d" % (prefijo, tabla["id"], numero)}
        for columna in tabla["columnas"][1:]:
            registro[columna] = fila[columna]
        salida.append({"cabecera": cabecera, "registro": registro})
    return salida


def escribir_csv(ruta: str, filas: list[dict]) -> None:
    cabecera = filas[0]["cabecera"]
    lineas = [";".join(cabecera)]
    for fila in filas:
        lineas.append(";".join(limpiar(fila["registro"][c]) for c in cabecera))
    os.makedirs(os.path.dirname(ruta) or ".", exist_ok=True)
    with open(ruta, "w", encoding="utf-8", newline="\r\n") as destino:
        destino.write("\n".join(lineas) + "\n")


def invalidos_por_valor(tabla: dict) -> set[tuple[str, str]]:
    return {(f["nombre"], nivel["valor"])
            for f in tabla["factores"] for nivel in f["niveles"]
            if not nivel.get("valido", True)}


def pares_esperados(tabla: dict) -> set[tuple[str, str, str, str]]:
    """Pares que la composicion debe cubrir, segun el modo de la tabla.

    Con ``noCombinaInvalidos`` los pares (invalido, invalido) quedan fuera de la
    obligacion: una fila con dos valores fuera de clase no deja atribuir el rechazo.
    """
    mal = invalidos_por_valor(tabla)
    sin_emparejar = set(tabla.get("noEmparejar", []))
    esperados = set()
    for fa, fb in itertools.combinations(tabla["factores"], 2):
        if fa["nombre"] in sin_emparejar or fb["nombre"] in sin_emparejar:
            continue
        for na in fa["niveles"]:
            for nb in fb["niveles"]:
                if tabla.get("noCombinaInvalidos") \
                        and (fa["nombre"], na["valor"]) in mal \
                        and (fb["nombre"], nb["valor"]) in mal:
                    continue
                esperados.add((fa["nombre"], na["valor"], fb["nombre"], nb["valor"]))
    return esperados


def verificar_pares(tabla: dict, filas: list[dict]) -> str:
    """Comprueba que la composicion cubre todos los pares (para la evidencia)."""
    if tabla["kind"] not in ("pairwise", "full"):
        return "sin combinacion (filas escritas a mano)"
    vistos = set()
    for fila in filas:
        combinada = [(f["nombre"], fila["registro"][f["nombre"]])
                     for f in tabla["factores"]]
        for (fa, va), (fb, vb) in itertools.combinations(combinada, 2):
            vistos.add((fa, va, fb, vb))
    esperados = pares_esperados(tabla)
    faltan = esperados - vistos
    if faltan:
        raise SystemExit("faltan %d pares por cubrir en la tabla %s: %r"
                         % (len(faltan), tabla["id"], sorted(faltan)[:5]))
    if tabla["kind"] == "full":
        return "producto completo cubierto (%d filas)" % len(filas)
    return "%d/%d pares cubiertos (100%%)" % (len(vistos), len(esperados))


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("spec")
    parser.add_argument("--resources", help="copia identica de los CSV a este directorio")
    parser.add_argument("--only", help="genera solo las tablas indicadas, ej. A,C")
    args = parser.parse_args(argv)

    with open(args.spec, encoding="utf-8") as origen:
        spec = json.load(origen)

    base = os.path.dirname(os.path.abspath(args.spec))
    prefijo = spec["idPrefijo"]
    solo = set(args.only.split(",")) if args.only else None

    primera = sorted(k for k in spec if k.startswith("tabla_"))
    if not primera:
        raise SystemExit("la spec no tiene ninguna tabla_")
    # tdop-<cdu>-<tabla>.csv -> <cdu>
    nombre_base = os.path.splitext(spec[primera[0]]["csv"])[0].replace("tdop-", "")
    tallo = "-".join(nombre_base.split("-")[:-1])

    for clave in primera:
        tabla = spec[clave]
        if solo and tabla["id"] not in solo:
            continue
        tabla["id_prefix"] = prefijo
        filas = (filas_conjetura(tabla, prefijo)
                 if tabla["kind"] in ("conjetura", "filas")
                 else filas_de(tabla, spec["semilla"]))
        # el voraz puede ensombrecer una regla: si la spec exige estados, se comprueba
        # que al menos una fila los observa (si falta, se declara una filasBase).
        vistos_estado = {f["registro"]["esperadoStatus"] for f in filas}
        faltan = sorted(set(tabla.get("estadosBase", [])) - vistos_estado)
        if faltan:
            raise SystemExit("la tabla %s no observa los estados exigidos: %s"
                             % (tabla["id"], ", ".join(faltan)))
        destino = os.path.join(base, tabla["csv"])
        escribir_csv(destino, filas)
        copias = [destino]
        if args.resources:
            copia = os.path.join(args.resources, tabla["csv"].replace("tdop-", ""))
            escribir_csv(copia, filas)
            copias.append(copia)
        print("tabla %s (%s): %d filas, %s -> %s | %s" % (
            tabla["id"], tabla["kind"], len(filas),
            ("producto %d" % producto(tabla)) if "factores" in tabla
            else "filas escritas a mano",
            verificar_pares(tabla, filas), "; ".join(copias)))

    # El mapa estado -> (http, codigoError) tambien cae a resources: el test no puede
    # inventarse los codigos HTTP, tienen que salir de la misma spec que las filas.
    if spec.get("estados"):
        filas_estado = [{
            "cabecera": ["estado", "http", "codigoError", "descripcion"],
            "registro": {"estado": nombre,
                         "http": dato["http"],
                         "codigoError": dato.get("codigo", "-"),
                         "descripcion": dato.get("descripcion", "")},
        } for nombre, dato in spec["estados"].items()]
        destino = os.path.join(base, "tdop-%s-esperados.csv" % tallo)
        escribir_csv(destino, filas_estado)
        copias = [destino]
        if args.resources:
            copia = os.path.join(args.resources, "%s-esperados.csv" % tallo)
            escribir_csv(copia, filas_estado)
            copias.append(copia)
        print("mapa de estados: %d estados -> %s" % (len(filas_estado), "; ".join(copias)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
