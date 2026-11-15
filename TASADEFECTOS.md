# Tasa de defectos por iteracion (D37)

*Objetivo: medir, de forma auditable y reproducible, cuantos defectos produce cada iteracion y de que clase
son, para decidir con datos si el modelo cuantizado en uso (Qwen3.8-Flash-Next `iq3_s`) es suficiente o hay
que pasar a uno mas potente en ciertos trabajos. Regla de registro: decision **D37** en `DECISIONS.md`; el
contador por cierre va tambien en el registro de sesion de `STATUS.md`.*

Corte de datos: **15/11/2026, rama `feature/iter05-cdu12-citas` (`b912554`, 13 commits) con todas las puertas
tecnicas en verde; el PR #42 ya fusionado por Ismael (merge `08a7ed8`, 14/11, 12 commits de codigo) y #40
cerrada a mano, quedando la matriz TDOP + estos docs en el PR de cierre**. Este corte sustituye al del
11/11/2026, que era de mitad de tramo (`7abea47`).

## 1. Que es un defecto y como se cuenta

Cuatro familias (solo la ultima mide la valia del modelo de codigo; las demas miden proceso y entorno):

| Familia | Que abarca | Como se detecta |
|---|---|---|
| **F1 Proceso** | Incumplir un protocolo acordado (D29/D30, fusiones fuera de sitio, issues cerradas sin artefacto, ficheros de version mal alineados) | Revision de Ismael o comprobacion posterior |
| **F2 Entorno/herramienta** | Trampas de la cadena de build o de las herramientas (doclint, Testcontainers/Docker, recortes de PlantUML) | Local o CI |
| **F3 Semantica/contrato** | Spec TDOP o decision de diseno que choca con el contrato publicado | Rojo D33 o revision |
| **F4 Codigo** | Defecto real en codigo de produccion (comportamiento distinto del contrato) | Pruebas, TC humanos, revision |

**Que SII cuenta:** todo defecto que haya dejado rastro en el repo (commit corrector, decision D30/D35, registro
de sesion) o que saliera a CI. Cada defecto lleva su evidencia en el libro de la seccion 2.

**Que NO cuenta (regla de exclusi6n, sesgo conocido):** lo que pillan el compilador o `mvn verify` local antes
de pushear. Ejemplos reales no contabilizados: el enfoque Flyway `{vendor}` descartado en la D34 *antes* de
commitearlo (duplicaba versiones al escanear Flyway 11 ambas subcarpetas), las dos dudas semanticas de Iter5
(solicitud apta y preaviso) resueltas en los `.puml` antes de escribir codigo, y el rojo planificado de CI en
`7abea47`/`e44f7c1` (puerta JaCoCo 0.00 < 0.80 de un modulo aun sin tests: es un hito del plan, no un defecto).
Por eso las cifras por debajo estan sesgadas a mano: miden defectos que **escaparon** del auto-chequeo del agente.

## 2. Libro de defectos (11/11/2026; sin nuevos en el cierre de Iter5, 15/11/2026)

| ID | Epoca | Familia | Defecto | Evidencia | Fugado? |
|---|---|---|---|---|---|
| DEF-01 | Iter0 | F1 | Coma faltante en la fila iter18 de `componentes-versiones+Calidad.csv` (columnas desfasadas) | fix `a5caab6` | SI (commiteado y corregido despues) |
| DEF-02 | Iter0 | F1 | Desalineacion de columnas en `componentes-versiones.csv` (filas iter7/8/13 e iter20 desfasada) | fix `df7c715` | SI |
| DEF-03 | Iter1 | F3 | Spec TDOP decia `CODIGO_EN_USO` y el contrato publica `CODIGO_DUPLICADO` (se arregla la spec, no el test) | sesion 17, wiki `Iter1-CDU16-catalogo` | SI (Iter1 se fusiono verde el 08/11; la spec salto al aplicar D33 el 09/11) |
| DEF-04 | Iter1 | F3 | Spec TDOP no reflejaba el 400 por tamano de pagina | sesion 17 (rojo D33: 16 fallos de 115 casos) | SI |
| DEF-05 | Iter1 | F4 | Las cotas del contrato no se validaban en el servicio: `codigo` de 41 caracteres o `pagina=abc` acababan en 500 `ERROR_INTERNO` contra la BD, no en 400; `categoria` sin validar contra la lista del contrato. 14 de los 16 fallos del rojo D33 mapean a esta unica familia | D35 en `DECISIONS.md`; fix `b2f7356`; wiki Iter1 (16 fallos del rojo) | SI (Iter1 quedo verde con 24/24 tests y JaCoCo 96,5 %; el hueco lo destapa la matriz D33 dias despues) |
| DEF-06 | Iter3 | F4 | El 404 de detalle de agenda responda sin cuerpo de error del contrato | fix `ae79d7e` | No (pillado en la propia rama, antes de fusionar) |
| DEF-07 | Iter3 | F1 | PR #17 (`feature/iter03-cdu13-agendas` -> `main`) abierto y fusionado por error; deja `main` a medias y rompe la correspondencia `main` = releases | merge `5e0ef29`, reparado con `--force-with-lease`; ADR D30 (`DECISIONS.md` l.171) | SI (llego a fusionarse) |
| DEF-08 | Iter3 | F1 | Issues #21/#22/#23 cerradas el 08/11 12:34 como `completed` sin artefacto detras (sin TC, sin `.staruml`, wiki sin paginas); reabiertas con la comprobacion | registro de sesion 12 en `STATUS.md` | SI |
| DEF-09 | Iter4 | F2 | `doclint` del javadoc estricto no admite `@throws` en un `record` | fix `7f85678` | SI (commiteado y corregido; no llego a CI) |
| DEF-10 | Iter4 | F1 | PR #29 fusiona `develop` -> `main` (merge `d12388b`) sin etiqueta de release: segunda desviacion de D30 | registro de sesion 16 en `STATUS.md` | SI |
| DEF-11 | D34 | F1 | Issue #34 queda cerrada el 09/11 00:26 junto a #35 sin ningun trabajo de D34 detras; reabierta | registro de sesion 17 en `STATUS.md` | SI |
| DEF-12 | D34 | F2 | Testcontainers 1.21.3 (API Docker 1.32, la gestiona el BOM de Boot 3.5.4) contra Docker Engine 29 (rechaza clientes < 1.40): HTTP 400 y los tests de MySQL **se saltaban en silencio** (falso verde posible). Fix: `testcontainers-bom` 1.21.4 antes de `spring-boot-dependencies` + JaCoCo 0.8.15 (Java 25) | registro de sesion 18 en `STATUS.md` | No (detectado el mismo dia, antes de fusionar; pero es la trampa de falso verde mas seria del proyecto) |
| DEF-13 | Iter5 | F2 | El PNG `cdu06-clases-diseno.png` quedo recortado por el limite por defecto de PlantUML (4096 px, recorta en silencio sin error) | re-render `0a58892`; receta en `STATUS.md` §4 (`-DPLANTUML_LIMIT_SIZE=16384`) | SI (el PNG recortado quedo commiteado) |

Unidad de conteo: **familia de hueco**, no caso de prueba. Los 16 fallos del rojo D33 de Iter1 son 1 defecto
F4 (DEF-05) + 2 F3 (DEF-03/04), no 16 defectos.

**Cierre de Iter5 (15/11/2026): no se anade ningun DEF nuevo.** Dos cosas se rozaron y se quedaron por debajo de
la regla de exclusion de la seccion 1: el arranque en falso de la generacion TDOP (`mvn_tdop3`, resuelto en local
antes de pushear, sin rastro en git ni en CI) y el supuesto desvio de contrato del `PATCH /api/citas/{id}` de
reprogramacion, que al mirar el modelo de CDU12 (`docs/modelo/iter05/cdu12-secuencia.puml`) resulto no ser desvio:
la propia nota del modelo deja escrito que no existen endpoints PUT/PATCH de citas y la ruta real es
`POST /api/citas/{id}/reprogramar`. No hay DEF-14: el contrato no se rompio.

## 3. Tabla por iteracion

Commits del tramo = `git rev-list --count` entre etiquetas (el tramo posterior a `cfg-iter04` mezcla la
aplicacion de D33 a Iter1, los TC humanos y la D34; se agrupa como tramo inter-iteraciones).

| Etapa | Tramo de commits | Commits | F1 | F2 | F3 | F4 | Total | Fugados | Def/100 commits |
|---|---|--:|--:|--:|--:|--:|--:|--:|--:|
| Iter0 · CFG-00 esqueleto | `..cfg-iter00` | 18 | 2 | 0 | 0 | 0 | 2 | 2 | 11,1 |
| Iter1 · CDU16 catalogo | `cfg-iter00..cfg-iter01` | 10 | 0 | 0 | 2 | 1 | 3 | 3 | 30,0 |
| Iter2 · CDU17 seguridad | `cfg-iter01..cfg-iter02` | 14 | 0 | 0 | 0 | 0 | 0 | 0 | 0,0 |
| Iter3 · CDU13 agendas | `cfg-iter02..cfg-iter03` | 11 | 2 | 0 | 0 | 1 | 3 | 2 | 27,3 |
| Iter4 · CDU06 solicitudes | `cfg-iter03..cfg-iter04` | 20 | 1 | 1 | 0 | 0 | 2 | 2 | 10,0 |
| Tramo D33-Iter1 + TC + D34 | `cfg-iter04..6feabdf` | 21 | 1 | 1 | 0 | 0 | 2 | 2 | 9,5 |
| Iter5 · CDU12 citas | rama `6feabdf..b912554` | 13 | 0 | 1 | 0 | 0 | 1 | 1 | 7,7 |
| **Total** | | **107** | **6** | **3** | **2** | **2** | **13** | **11** | **12,1** |

Nota: DEF-03/04/05 son codigo de Iter1 (tramo 10 commits) aunque se descubrieron al aplicar D33 en el tramo
inter-iteraciones; se imputan al tramo de origen porque es el codigo que los contiene. La fila de Iter5 cuenta
los 13 commits de la rama `feature/iter05-cdu12-citas` (`6feabdf..b912554`): 12 de ellos ya estan en `develop`
por la fusion del PR #42 (merge `08a7ed8`, 14/11) y solo `b912554` (matriz TDOP) mas este commit de docs suben
en el PR de cierre; el tag `cfg-iter05` ira sobre la fusion de ese PR de cierre y ahi se revalida la cuenta,
que no deberia cambiar.

## 4. Metricas de apoyo (para el denominador)

- **CI:** 72 ejecuciones, 69 en verde y 3 fallidas; las tres en `feature/iter05-cdu12-citas` y las tres la puerta
  JaCoCo planificada (modulo `iso2-citas` todavia sin tests: `e44f7c1`, `7abea47`, `a276e9c`). El check
  `construir` quedo **en verde** en la rama desde `4b162cb` (suite CDU12) y verde tambien en el PR de cierre
  con la matriz TDOP dentro. Cero compilaciones y cero tests
  rotados llegaron a CI: la barrera local (`mvn -B -ntp clean verify` antes de pushear) funciona.
- **Pruebas:** **637 tests** en el reactor completo (`mvn -B -ntp clean verify` del 15/11/2026, **0 saltados**,
  Docker Engine 29.8.0 levantado para los 3 Testcontainers de MySQL; BUILD SUCCESS). Por modulos: **citas 238**
  (dentro `Cdu12MatrizTdopTest` **172**), catalogo 139, server-app 90 (`CitasApiTest` 18 + 3 Testcontainers),
  client-app 80 (`ApiClienteCitasTest` 13), seguridad 36, solicitudes 27, agendas 20, common 2 y 5 baseline de 1.
- **Cobertura JaCoCo (linea) tras Iter5:** citas **95,8 %** (puerta 0,80 superada), server-app 88,3 %,
  client-app 95,8 %, solicitudes 88,0 %, catalogo 97,5 %, seguridad 96,0 %, agendas 81,8 %.
- **Metodo D33 ya aplicado en dos CDU:** CDU16 (matriz de **115** casos) y CDU12 (matriz de **172** casos:
  A38 + B31 + C29 + D27 + E34 + F13), ambos generados desde su spec JSON con `tools/pairwise.py` y proyectados
  a la plantilla de TC humanos con `tools/tc_plantillas.py`.
- **Tamano:** 10.046 lineas Java de `src/main` y 7.364 de `src/test` (ratio 0,73). Iter5 aporta las 5 matrices
  TDOP y sube el peso de test sobre todo el reactor.
- **Conjetura de errores (D33) cumplida en Iter1:** el rojo de la matriz encontro en 16 casos de 115 lo que
  los 24/24 tests originales de Iter1 no encontraron. El rojo vale; por eso se mide por familias, no por casos.

## 5. Como volver a calcular todo esto

```powershell
# tramos y totales de commits
git rev-list --count cfg-iter00
git rev-list --count cfg-iter00..cfg-iter01   # y sucesivos; rama en curso: origin/develop..HEAD
# commits correctores y su scope
git log --pretty="%h %ad %s" --date=short origin/develop | Select-String -Pattern "^\w+ \d{4}-\d\d-\d\d fix"
# CI: total, conclusion y fallidos
gh run list --repo IsmaelCaballero/PrISO2-25-26 --limit 100 --json conclusion,headBranch,displayTitle,createdAt
# tests por modulo (tras un mvn verify)
Get-ChildItem -Recurse -Filter surefire-reports -Directory | ForEach-Object {
  $n = 0; Get-ChildItem $_.FullName -Filter *.txt | ForEach-Object {
    $m = Select-String -Path $_.FullName -Pattern "Tests run: (\d+)" | Select-Object -First 1
    if ($m) { $n += [int]$m.Matches[0].Groups[1].Value } }; "$($_.Parent.Parent.Name): $n" }
```

## 6. Que dice la serie (veredicto del 11/11/2026, revalidado en el cierre de Iter5 el 15/11/2026)

1. **El 46 % de los defectos (6/13) son F1 de proceso**, no de codigo: fusiones fuera de `main`/`develop`,
   issues cerradas sin artefacto, CSVs de version desalineados. Eso no lo arregla un modelo mas caro; lo
   arreglan las reglas (D29, D30) y su comprobacion. Desde D30 no ha vuelto un F1 de fusiones.
2. **F4 (codigo) sigue en 0 en Iter2, Iter4, D34 e Iter5: 1 en Iter1 y 1 en Iter3.** 2 defectos de codigo en
   107 commits (**1,9/100**), ninguno escapado a CI. Con los datos hasta Iter5 el flash cuantizado ha sido
   suficiente para CRUD, migraciones, documentacion y generacion TDOP.
3. **El riesgo real del cuantizado no es F4 sino el falso verde**: DEF-12 (Testcontainers saltando en silencio)
   y DEF-13 (PNG recortado en silencio) son trampas que dan verde a algo roto, y la unica familia F4 grave
   (DEF-05/D35) fue un hueco que los tests originales *no miraban*. Eso pesaba justo sobre Iter5, que ya esta
   cerrada en rama: **concurrencia real de 2 hilos** peleando por el mismo hueco (D36, UNIQUE + la BD decide el
   duelo), maquina de estados de 5 estados con su 409 `TRANSICION_INVALIDA`, cancelacion que libera el hueco y
   reapertura la solicitud, y puerta JaCoCo 0,80. El duelo concurrente esta probado con dos hilos reales y los
   Testcontainers de MySQL corren de verdad (0 saltados con Docker arriba), asi que el escenario de falso verde
   de DEF-12 quedo vigilado: los 3 tests de MySQL figuran como ejecutados, no saltados.
4. **Cierre de Iter5: no salta ningun umbral.** Ni mas de 2 F4 en una familia (CDU12 lleva 0), ni una decision
   de diseno erronea que obligue a reescribir codigo commiteado, ni un falso verde colado en CI. La matriz D33
   de CDU12 (172 casos) no destapo ningun F4 de produccion: lo que tenia que destapar (la solicitud apta solo en
   `creada`, el profesional sin agenda como `FUERA_DE_FRANJA`, `TRANSICION_INVALIDA` ganando a `PLAZO_CANCELACION`)
   ya estaba resuelto en los `.puml` antes de escribir codigo (D31), y por eso cae bajo la regla de exclusion.
   Sigue el flash cuantizado para el codigo de Iter6 (CDU19 / CFG-06).
5. **Umbrales para pedir modelo mas potente** (revisar en cada cierre, sobre esta tabla):
   - una familia de CDU acumula **> 2 defectos F4 detectados en revision/TC humanos**, o
   - **1 decision de diseno erronea que obliga a reescribir codigo ya commiteado** (hasta ahora: cero; el
     `{vendor}` de Flyway se descarto antes de commitear), o
   - **1 falso verde colado en CI** (hasta ahora: cero).
   Mientras no salten, se sigue con el flash cuantizado + D33 + CI; si saltan, el modelo mas potente se pide
   para ese trabajo concreto (diseno de contrato y pruebas de concurrencia/transiciones), no para el resto.

## 7. Plantilla para el cierre de cada iteracion

Al cerrar cada CFG (D29), anadir al registro de sesion de `STATUS.md` y actualizar a la vez la tabla de la
seccion 3 y el libro de la seccion 2 de este fichero:

```markdown
**Datos de defectos <IterN/CFG-NN>:** commits <tag-a>..<tag-b> = N ·
F1 proceso a · F2 entorno b · F3 contrato c · F4 codigo d · total e (fugados f) ·
e*100/N por 100 commits · detalle en TASADEFECTOS.md (DEF-xx..)
```
