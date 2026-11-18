# Tasa de defectos por iteracion (D37)

*Objetivo: medir, de forma auditable y reproducible, cuantos defectos produce cada iteracion y de que clase
son, para decidir con datos si el modelo cuantizado en uso (Qwen3.8-Flash-Next `iq3_s`) es suficiente o hay
que pasar a uno mas potente en ciertos trabajos. Regla de registro: decision **D37** en `DECISIONS.md`; el
contador por cierre va tambien en el registro de sesion de `STATUS.md`.*

Corte de datos: **15/11/2026, al cerrar la Iter6/CFG-06: tramo `cfg-iter05..cfg-iter06` (21 commits: 19 de
codigo en `feature/iter06-cdu19-integracion` + el commit de cierre `docs/iter06-cierre` + su fusion), PR #45
fusionado por Ismael (`0ef4f1a`) y reactor revalidado en `2388b7e` con 839 pruebas / 0 saltados**. El corte
anterior, de la Iter5 (`cfg-iter04..cfg-iter05`, 12 commits con el PR #42 fusionado en `08a7ed8`), queda como
fila de la tabla; ya no es el encabezado. Antes de ese hubo un corte de mitad de tramo el 11/11 (`7abea47`).

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

## 2. Libro de defectos (11/11/2026; sin nuevos en el cierre de Iter5; DEF-14 y DEF-15 en el cierre de Iter6,
15/11/2026; DEF-16, DEF-17 y DEF-18 en el cierre de Iter7, 18/11/2026)

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
| **DEF-14** | Iter6 | **F4** | `IntegracionProperties` (record de config del CDU19) **no llevaba `@ConfigurationProperties(prefix = "iso2.integracion")`**: el bloque `iso2.integracion:` del `application.yml` del servidor no se enlazaba y en runtime siempre valian los valores por defecto declarados en el propio record. Nadie se entero porque **los 126 TC de la matriz y los tests de adaptador/servicio construyen las propiedades a mano**, sin YAML: el contrato de configuracion no lo miraba ninguna prueba y el reactor estaba verde. El `.puml` de diseno si la llevaba, asi que es un desvio al escribir el codigo (**D31**), no un error de modelo. Solo se habria visto con una prueba de arranque de contexto con YAML, que el modulo no tenia | Introducido en `7a03e0c` (CI en verde con el defecto dentro, rama `feature/iter06-cdu19-integracion`); fix en `f5411c1`, cuatro commits despues y dentro de la misma rama, asi que a `develop` ya bajo corregido; el **prefix** no lo rompe ningun TC actual y su huella sigue viva como hueco de cobertura: **11 de 30 ramas** de la clase sin ejercitar (deuda D-02) | **SI** (commiteado y con CI en verde; lo pilla la propia matriz D33 al escribirse, antes de fusionar) |
| **DEF-15** | Iter6 | **F2** | **El registro durable se perdio por una sesion de agente abierta sin plan ni git**: entre dos lecturas seguidas, `STATUS.md` (45.715 B) y `PROMPTS.md` (88.267 B) quedaron recortados a 3.034 B y 317 B, con la cabecera y una fila de tabla intactas y el resto (registro de sesiones 16-23, deudas, decisiones D35-D37, prompt 22 y media guia de estilo) sustituido por un trozo de frase cortado a mitad de palabra. Descartado el `git checkout`: el `index.lock` huerfano de las 10:39 y que solo se vieran afectadas esas dos cabeceras apuntan a escrituras completas desde la otra ventana (`mcp__local__write_file` sustituye el fichero entero) | Recuperado al 100 % desde `HEAD` (`5f7f568` recupera, `c78e6ab`/`d994cfa` reescriben). **Lo que salvo el registro fue el commit continuo, no los snapshots de OpenCode**, que no se desactivaron (deuda 25) | No (el dano quedo en el arbol de trabajo; nunca se commiteo) |
| **DEF-16** | Iter7 | **F4** | El advice de errores de la cola (`ListaEsperaManejadorErrores`) llevaba un **catch-all de `Exception` que se comia el `HttpMediaTypeNotSupportedException`** que lanza Spring **antes** de entrar al metodo: un alta con `Content-Type: text/plain` respondia **500 `ERROR_INTERNO`** en lugar del 415 del contrato. Segundo tajo del mismo hueco: el primer arreglo devolvia el 415 **sin `ApiError` ni codigo estable**, cuando la convencion del repo ya la fijaba **D35** en `CatalogoManejadorErrores` (`CONTENIDO_NO_ACEPTADO`); hubo que realinear el 415 de la cola a esa convencion y anadir el codigo al estado canonico `CONTENIDO` de la spec | Nacio con la capa web en `2c7ea4e`; lo destapa la fila **TC14-H02** (`CONTENT-TIPO-ERRONEO`) de la tabla de conjeturas, roja en `status` y en `codigo`; fix `b38c149` (ya no disfraza el 415) y `21d9a3c` (codigo estable del contrato) | No (pillado en la propia rama por la matriz D33, antes de fusionar; el unico run CI del tramo es el del PR #48, ya con el fix dentro) |
| **DEF-17** | Iter7 | **F4** | A la entidad de la cola **le faltaba el bloqueo optimista `@Version` que la convencion D10 ya exige** desde CDU12: dos operaciones concurrentes sobre la misma entrada (`prioridad`/`retirar`/`asignar`) se pisaban y **ambas escribian**, con su auditoria duplicada. Es el mismo error que ya se cometo en Iter5, replicado en la CDU nueva | Nacio con el dominio de la cola en `1fda042`; lo destapa **TC14-E10** de la tabla de secuencias/concurrencia; fix `d8f95fe` (`@Version`, 409 `CONFLICTO_ENTRADA_NO_ACTIVA` para el perdedor y columna `version` en `V1.6.0` h2+mysql, con el modelo D31 alineado) | No (pillado en la propia rama por la matriz; nunca bajo a `develop`) |
| **DEF-18** | Iter7 | **F3** | **Una familia de expectativas de spec mal fijadas**, no tres defectos: la spec de la cola decia **500** para `profesionalId` sin agenda y el codigo hace **409 `FUERA_DE_FRANJA`** (sin agenda no hay ninguna franja donde quepa el hueco); las secuencias **E02/E03/E12** esperaban **`OK`** y el alta de la cola promete **`CREADO`** (201); y la conjetura **H06** asumia que Spring se queda con el primer valor de un `@RequestParam` repetido cuando **los junta** en una cadena (`api-prueba,api-otra`). Los tres eran expectativas falsas: el codigo era correcto y mas alineado con RF14.2 de lo que decia la matriz | Spec `docs/test/iter07/tdop-cdu14-espec.json`, fixes de spec `9c8510e`, `850d585` y la realineacion de H06 en el cierre de la tabla H (D33 permite alinear en verde las filas de conjetura contra comportamiento confirmado, y aqui el 400 se confirmo por HTTP contra el servidor montado) | No (la spec defectuosa vive en la propia rama; la matriz nunca se fusiono verde con ella) |

Unidad de conteo: **familia de hueco**, no caso de prueba. Los 16 fallos del rojo D33 de Iter1 son 1 defecto
F4 (DEF-05) + 2 F3 (DEF-03/04), no 16 defectos.

**Cierre de Iter5 (15/11/2026): no se anade ningun DEF nuevo.** Dos cosas se rozaron y se quedaron por debajo de
la regla de exclusion de la seccion 1: el arranque en falso de la generacion TDOP (`mvn_tdop3`, resuelto en local
antes de pushear, sin rastro en git ni en CI) y el supuesto desvio de contrato del `PATCH /api/citas/{id}` de
reprogramacion, que al mirar el modelo de CDU12 (`docs/modelo/iter05/cdu12-secuencia.puml`) resulto no ser desvio:
la propia nota del modelo deja escrito que no existen endpoints PUT/PATCH de citas y la ruta real es
`POST /api/citas/{id}/reprogramar`. No hay DEF-14: el contrato no se rompio.

**Cierre de Iter6 (15/11/2026): si hay DEF-14 (F4) y DEF-15 (F2).** El 14 es el `@ConfigurationProperties`
ausente en `IntegracionProperties`: el numero 14 sigue libre de la nota anterior, porque aquella declaraba que
*Iter5* no lo generaba; aqui si lo genera Iter6. Cuenta como F4 fugado porque vivio commiteado y con CI verde
desde `7a03e0c` hasta `f5411c1`, en la propia rama, y a `develop` ya bajo corregido. El 15 es el vaciado de `STATUS.md`/`PROMPTS.md`; se imputa a Iter6 porque ocurrio
en su tramo, aunque el dano nunca llego al repositorio. No se anaden mas: los 3 rojos de CI del tramo siguen
siendo la puerta JaCoCo planificada de Iter5, y el resto de hallazgos del spike (D-01..D-07 de
`docs/informes/spike-cdu19.md`) son **deudas de alcance**, no defectos: comportamiento acordado y documentado,
no comportamiento distinto del contrato.

**Cierre de Iter7 (18/11/2026): DEF-16 (F4), DEF-17 (F4) y DEF-18 (F3).** Se cuenta por **familias, no por
casos** (regla de la seccion 1): las tres expectativas de spec mal fijadas (`sin agenda`, E02/E03/E12 y la
conjetura H06) son **una** familia F3, no tres defectos, y las dos capas del 415 (disfraz de 500 y 415 sin
codigo estable) son **un** defecto F4 con dos commits de arreglo. No se anade nada mas, por dos motivos:

* La **conjetura H06 no es un defecto** sino una conjetura falsa. Vale registrarla (es el dato que impide
  volver a inventarse un 400 "porque se le escapa"), pero el hueco estaba en la spec, y ahi ya esta dentro de
  la familia DEF-18.
* Las **trampas de entorno del tramo** (el `-Dtest=Cdu14MatrizTdopTest#tdopTablaH` que ejecuta **0 pruebas**
  porque todos los metodos son `@ParameterizedTest`, y el contador de JaCoCo mal contado en PowerShell por la
  colision `$bc`/`$BC`, que daba totales falsos) se detectaron y corrigieron **en local, antes de pushear**:
  caen en la regla de exclusion de la seccion 1, igual que el arranque en falso de la generacion TDOP de
  Iter5. Si hubieran llegado a CI, serian F2.

Lo que si deja Iter7 como senal metodologica: **la tabla de conjeturas (H) encontro un F4 real (DEF-16) y una
conjetura falsa (H06) en 13 filas**, y la tabla de secuencias/concurrencia (E) encontro el F4 de integridad
(DEF-17) en su fila E10. Ninguno de los dos llego a `develop`: los pilla la matriz dentro de la propia rama.

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
| Iter6 · CDU19 integracion | `cfg-iter05..cfg-iter06` | 22 | 0 | 1 | 0 | 1 | 2 | 1 | 9,1 |
| Iter7 · CDU14 listas de espera | rama `cfg-iter06..21d9a3c` | 33 | 0 | 0 | 1 | 2 | 3 | 0 | 9,1 |
| **Total** | | **162** | **6** | **4** | **3** | **5** | **18** | **13** | **11,1** |

Nota: DEF-03/04/05 son codigo de Iter1 (tramo 10 commits) aunque se descubrieron al aplicar D33 en el tramo
inter-iteraciones; se imputan al tramo de origen porque es el codigo que los contiene. La fila de Iter5 cuenta
los 13 commits de la rama `feature/iter05-cdu12-citas` (`6feabdf..b912554`): 12 de ellos ya estan en `develop`
por la fusion del PR #42 (merge `08a7ed8`, 14/11) y solo `b912554` (matriz TDOP) mas este commit de docs suben
en el PR de cierre; el tag `cfg-iter05` ira sobre la fusion de ese PR de cierre y ahi se revalida la cuenta,
que no deberia cambiar. **Revalidado: `cfg-iter05` queda sobre la fusion de cierre `cbb1901` y la cuenta no
cambió.**

Nota de Iter6: los 22 commits del tramo `cfg-iter05..cfg-iter06` son los 19 de la rama
`feature/iter06-cdu19-integracion` + el merge `0ef4f1a` del PR #45 + el commit de docs de cierre de
`docs/iter06-cierre` + su fusion, que es donde va el tag `cfg-iter06`; `git rev-list --count` cuenta los merges,
como en los tramos anteriores. El unico F4 es DEF-14 y el unico F2 es DEF-15. Ningun F1: la iteracion se ha
regido por D29/D30/D34/D36 sin desviaciones de protocolo (rama de una sola CDU, PR a `develop`, CI verde antes
de fusionar, issue cerrada con artefacto detras).

Nota de Iter7: los 33 commits del tramo son `git rev-list --count cfg-iter06..21d9a3c`, medido el 18/11 con la
rama `feature/iter07-cdu14-listas-espera` ya pusheada y el arbol limpio, usando el **mismo metodo** que en
Iter6 (`git rev-list --count cfg-iter05..207a506` = 22, contando los merges). Faltan por sumarse el merge del
PR #48, el commit de docs de cierre y su fusion, asi que al poner el tag `cfg-iter07` la cuenta subira a ~36
y **se revalida aqui**, como se hizo en Iter5 e Iter6; la tasa de la fila (9,1/100) esta calculada sobre 33 y
bajaria a ~8,3 con 36. Los 3 defectos imputados a Iter7 (DEF-16, DEF-17, DEF-18) nacen todos en la propia rama
y ninguno llega a `develop`.

## 4. Metricas de apoyo (para el denominador)

- **CI:** **83 ejecuciones** (contador del 15/11 con `gh run list --limit 100`), **80 en verde y 3
  fallidas**; las tres fallidas son las mismas de siempre, la puerta JaCoCo planificada de Iter5 en
  `feature/iter05-cdu12-citas` (`e44f7c1`, `7abea47`, `a276e9c`). Las **11 ejecuciones nuevas del tramo
  Iter6 fueron todas en verde**, incluido el check `construir` del PR #45 antes de fusionar. Cero
  compilaciones y cero tests rotados llegaron a CI: la barrera local (`mvn -B -ntp clean verify` antes
  de pushear) sigue funcionando. Ojo con el matiz de DEF-14: verde en CI no significaba correcto, el
  defecto vivio commiteado dentro de ejecuciones verdes.
- **Pruebas:** **839 tests** en el reactor completo (`mvn -B -ntp clean verify` revalidado en `2388b7e`
  el 15/11/2026, **0 saltados**, Docker arriba para los Testcontainers de MySQL; BUILD SUCCESS). Por
  modulos: **integracion 203** (nuevo; dentro `Cdu19MatrizTdopTest` **126**), citas 238 (matriz CDU12
  172), catalogo 139, server-app 90 (`CitasApiTest` 18 + 3 Testcontainers), client-app 80, seguridad
  36, solicitudes 27, agendas 20, common 2 y 4 baseline de 1 (comunicacion, informes, pacientes,
  reporting). Iter6 suma **202 pruebas** sobre las 637 de Iter5.
- **Cobertura JaCoCo (linea / rama) tras Iter6:** integracion **96,5 / 78,4** ( puerta 0,70), citas
  **95,8 / 90,8** (puerta 0,80), client-app 95,8 / 88,7 (0,60), catalogo 97,5 / 82,5 (0,60), seguridad
  96,1 / 90,9 (0,85), server-app 88,5 / 82,4 (0,60), solicitudes 88,0 / 65,5 (0,85), agendas 81,8 /
  67,5 (0,60) y **common 0,0 / 0,0 (0 de 139 lineas, 0 de 46 ramas)**: el pom del modulo no fija puerta
  y hereda el 0,00 de la raiz, asi que CI no se entera (deuda nueva **D-08**). Por ramas sin cubrir, el
  peor modulo ya no es integracion sino **common, con 46 de 46**; le siguen integracion con **37 de 171**
  (ahi esta la huella de DEF-14, deuda D-02), agendas 25 de 77, solicitudes 19 de 55, citas 15 de 163,
  catalogo y client-app 7 cada uno, seguridad 4 y server-app 3. En lineas sin cubrir mandan otra vez
  common (139) y despues agendas (40), citas y solicitudes (17 cada una), integracion y server-app (13),
  client-app (11), seguridad (6) y catalogo (5).
  **Reactor: 1.744 de 2.005 lineas (87,0 %) y 512 de 675 ramas (75,9 %)**.
- **Metodo D33 ya aplicado en cuatro CDU:** CDU16 (**115** casos), CDU12 (**172**), CDU19 (**126** en 7
  CSV: A21 sincronizar + B7 idempotencia + C15 sso + D12 estudios + E17 ping + F40 simulacion + G14
  transporte; el octavo CSV, `tdop-cdu19-esperados.csv`, es la tabla de codigos esperados y no cuenta
  como matriz) y **CDU14 (`Cdu14MatrizTdopTest`, 253 filas en 8 CSV: A40 + B55 + C30 + D27 + E12 + F64 +
  G12 + H13)**. Total **666 filas** de matriz, todas generadas desde su spec JSON con
  `tools/pairwise.py` y proyectadas a la plantilla de TC humanos con `tools/tc_plantillas.py` (D33: el
  bloque TDOP de los docs no se edita a mano, y rellenar la tabla manual es no-op al regenerar,
  comprobado el 15/11). **Aclaracion al cierre de Iter7 (18/11/2026):** en la guia de continuation del
  17/11 anote que la matriz de CDU14 cubria **"439 de 439 combinaciones"** como si fuera el total; el numero
  es cierto pero es **solo el de la tabla F**, porque el recuento de pares es **por tabla**, no del
  documento entero. Lo completo: **A 90/90, B 186/186 y F 439/439, o sea 715 de 715 pares obligatorios
  cubiertos** en las tres tablas combinables, con las otras cinco enumeradas a mano. Y el recorte si es de
  verdad: **A 1.050 combinaciones -> 40 filas, B 7.560 -> 55 y F 29.160 -> 64**, es decir **37.770 -> 159
  filas**. Matiz operativo de la herramienta, para el que retome la D33: en la spec JSON **no hay campo
  `modo` ni `reducir`**; `tools/pairwise.py` decide solo mirando la forma de la tabla: si lleva `factores`
  la combina por parejas (y la recortan los flags `noCombinaInvalidos` / `noEmparejar` y las `reglas`), y si
  lleva `filas` la reproduce tal cual, enumerada a mano. Por eso en CDU14 combinan A, B y F y van
  enumeradas C, D, E, G y H. Los sondeos de cierre contra el servidor real montado
  (`docs/tc/evidencia/cdu19/`) no dieron ningun defecto de CDU, pero si dos hallazgos de montaje que quedan
  como deudas: las rutas sin mapear contestan **401 `AUTENTICACION_REQUERIDA` con cuerpo `ApiError` incluso
  con token valido** (deuda **D-09**) y la divergencia de codigos entre matriz aislada y aplicacion montada
  que se explica mas abajo (deuda **D-10**).
- **Tamano:** **13.343 lineas Java en 181 ficheros de `src/main`** y **10.646 en 63 de `src/test`**
  (ratio **0,80**, antes 0,73). Iter6 anade ~3.300 lineas de producto (el modulo integracion entero) y
  ~3.280 de prueba.
- **Conjetura de errores (D33) cumplida en Iter1:** el rojo de la matriz encontro en 16 casos de 115 lo
  que los 24/24 tests originales de Iter1 no encontraron. El rojo vale; por eso se mide por familias, no
  por casos. En Iter6 la familia G de conjetura de CDU19 no destapo ningun F4 nuevo, pero su hermana de
  CDU12 si deja una leccion metodologica: TC12-C04, TC12-C21 y TC12-D03 esperan **500** por el
  `TypeMismatchException` del `@RequestParam LocalDate` en `/api/citas/huecos`, pero al ejercitar el 15/11 el
  servidor real montado con `desde=no-es-fecha` (TC19-04, evidencia en
  `docs/tc/evidencia/cdu19/registro-citas-y-huecos.json`) devolvio **400 `DATOS_INVALIDOS`**. Montado, el
  `GlobalExceptionHandler` de `iso2-common` si tiene consejo para ese caso, mientras la matriz aislada del
  modulo solo carga el consejo local `CitaManejadorErrores`, que no lo tiene. No es un defecto colado (la
  spec de CDU12 ya contemplaba la alternativa y decide la rojo), pero si revela que **una matriz por modulo
  puede esperar un codigo distinto del de la aplicacion montada**: queda como deuda **D-10**.

**Revalidado tras Iter7 (18/11/2026, medido sobre los `target/` del `mvn -B -ntp verify` de `21d9a3c`).**

- **CI:** **87 ejecuciones** en total (72 de `CI`, 13 de `Javadoc a Pages` y 2 del workflow suelto de
  aquel episodio de `Agent Merge on PR #29`), con **69 verde y 3 rojas** en `CI`: las tres rojas siguen
  siendo la puerta JaCoCo planificada de Iter5. Desde el cierre de Iter6 solo hay **una ejecucion nueva**
  (la 72, el check `construir` del PR #48, **success** en 3m33s). Merito del metodo, pero con una lectura
  honesta: en Iter7 la rama se pushea casi al final, asi que **CI solo vio el estado ya corregido**; los
  253 rojos y los dos F4 se resolvieron detras de la barrera local (`mvn -B -ntp clean verify` y la matriz
  antes de pushear).
- **Pruebas:** **1.092** en el reactor, **0 fallos y 0 saltadas**. Por modulos: citas **491** (dentro
  `Cdu14MatrizTdopTest` **253**, `Cdu12MatrizTdopTest` 172, `CitaServicioTest` 30, `MaquinaEstadosCitaTest`
  15, `CitaControladorTest` 11, concurrencia 5, arquitectura 4, baseline 1), integracion 203, catalogo 139,
  server-app 90, client-app 80, seguridad 36, solicitudes 27, agendas 20, common 2 y 4 baselines de 1.
  Iter7 suma **253 pruebas** sobre las 839 de Iter6.
- **Cobertura JaCoCo (linea / rama):** citas **93,5 / 89,5** (696 de 744 lineas, 257 de 287 ramas: el
  modulo crece 301 lineas y la proporcion baja), integracion 96,5 / 78,4, catalogo 97,5 / 82,5, seguridad
  96,1 / 90,9, client-app 95,8 / 88,7, solicitudes 88,0 / 65,5, server-app 88,7 / 82,4, agendas 81,8 /
  67,5 y **common 0,0 / 0,0 (0 de 164 lineas, 0 de 56 ramas)**. El reactor: **2.059 de 2.376 lineas
  (86,7 %) y 621 de 809 ramas (76,8 %)**; la cobertura baja medio punto porque **el codigo nuevo de
  `iso2-common` (las tres clases de fachada de la cola: 25 lineas y 10 ramas nuevas) nace sin ninguna
  prueba**, y eso agranda la deuda **D-08** sin tocar la puerta del modulo. El aviso del 17/11 era justo:
  **la cuenta manual de JaCoCo en PowerShell estaba mal** (la variable `$bc` se pisa con `$BC` por la
  insensibilidad a mayusculas de PowerShell y el total de ramas salia 451 / 16,4 %). Corregido: ahora las
  cuentas salen del **`jacoco.csv` de cada modulo**, con el mismo script para los dos refs, y el propio
  script es la prueba de que la cifra anterior era imposible (451 ramas cubiertas sobre 2.754 totales
  inventadas). **Aviso para el siguiente que mida:** el `jacoco.csv` solo se escribe en la fase `verify`;
  si se ejecutan sueltos los `@SpringBootTest` de la matriz, no se reescribe y la medida esta
  desactualizada.
- **Tamano:** **15.454 lineas Java en 191 ficheros de `src/main`** y **12.377 en 64 de `src/test`**
  (ratio **0,80**, igual que en Iter6). Iter7 anade **2.111 lineas de producto**: **2.089 en 10 ficheros
  nuevos** (7 de `iso2-citas`: dominio de cola + auditoria + repositorios + fachada + controlador + advice;
  3 de `iso2-common`: fachada y dos excepciones) y el resto en ficheros ajenos a la CDU que se tocan para
  el RBAC. Las pruebas suman **1.731 lineas nuevas**. Contado con `git ls-tree -r` sobre
  `cfg-iter06` y `HEAD` con el mismo metodo en los dos lados; en el tramo se tocan **57 ficheros nuevos
  mas 16 modificados** (antes tenia anotado "33 ficheros nuevos", que era otro error de lectura mio y ya
  no esta en el cuerpo del PR). Del lado de producto solo se tocan **3 ficheros ajenos a la CDU**:
  `Permiso` y `MatrizPermisos` (los permisos `lista_espera:leer` / `lista_espera:escribir`) y
  `ConfigSeguridad` (las rutas `/api/listas-espera/**`), mas las dos migraciones `V1.6.0` de h2 y mysql;
  `MatrizPermisosTest` se actualiza con ellos.

## 5. Como volver a calcular todo esto

Recetas revalidadas el 15/11/2026 en el cierre de Iter6 (PowerShell 7; los avisos `NativeCommandError`
de `git push`/`gh` son ruido, se comprueba con `git ls-remote`).

```powershell
# tramos y totales de commits (rev-list cuenta los merges, como en las filas de la seccion 3)
git rev-list --count cfg-iter00
git rev-list --count cfg-iter00..cfg-iter01   # y sucesivos; rama en curso: origin/develop..HEAD
git rev-list --count cfg-iter05..origin/develop   # tramo Iter6 tal como esta hoy
# commits correctores y su scope
git log --pretty="%h %ad %s" --date=short origin/develop | Select-String -Pattern "^\w+ \d{4}-\d\d-\d\d fix"
# donde nacio y donde se arregl6 un defecto de codigo (para la columna de evidencia)
git log --pretty="%h %ad %s" --date=short -- <ruta del fichero tocado por el fix>
```

```powershell
# CI: volcar a fichero y parsear en Python (--jq con espacios se rompe; utf16 no vale)
gh run list --repo IsmaelCaballero/PrISO2-25-26 --limit 100 `
  --json conclusion,displayTitle,headBranch,createdAt | Out-File -Encoding utf8 ci_runs.json
```

```python
# ci_runs.json trae BOM: abrir con encoding="utf-8-sig"
import collections, json
d = json.load(open("ci_runs.json", encoding="utf-8-sig"))
print(len(d), collections.Counter(r["conclusion"] for r in d))
print([r["headBranch"] for r in d if r["conclusion"] != "success"])
```

```powershell
# tests por modulo y del reactor (solo valido tras un mvn -B -ntp clean verify COMPLETO)
Get-ChildItem -Recurse -Filter surefire-reports -Directory | ForEach-Object {
  $n = 0; Get-ChildItem $_.FullName -Filter *.txt | ForEach-Object {
    $m = Select-String -Path $_.FullName -Pattern "Tests run: (\d+)" | Select-Object -First 1
    if ($m) { $n += [int]$m.Matches[0].Groups[1].Value } }; "$($_.Parent.Parent.Name): $n" }
# filas por familia de la matriz D33 (los CSV TDOP van separados por ";" y el id es TCnn-<letra>)
Get-ChildItem docs\test\iter06 -Filter *.csv | ForEach-Object {
  $ids = Get-Content $_.FullName | Select-Object -Skip 1 | Where-Object { $_.Trim() } |
         ForEach-Object { ($_ -split ';')[0] }
  $letras = $ids | ForEach-Object { if ($_ -match 'TC19-([A-G])') { $matches[1] } }
  $g = $letras | Group-Object | ForEach-Object { "$($_.Name)$($_.Count)" }
  "$($_.Name) -> $($g -join ',')" }   # el CSV *_esperados.csv es tabla de esperados, no matriz
```

```python
# cobertura JaCoCo por modulo y del reactor (jacoco.csv usa coma y trae BOM)
import csv
from pathlib import Path
tot = [0, 0, 0, 0]  # linea_missed, linea_covered, rama_missed, rama_covered
for p in sorted(Path(".").glob("iso2-*/target/site/jacoco/jacoco.csv")):
    m = [0, 0, 0, 0]
    for r in csv.DictReader(open(p, encoding="utf-8-sig", newline="")):
        for i, k in enumerate(("LINE_MISSED", "LINE_COVERED", "BRANCH_MISSED", "BRANCH_COVERED")):
            m[i] += int(r[k])
    for i in range(4):
        tot[i] += m[i]
    print(p.parts[0], "%.1f %%" % (100 * m[1] / sum(m[:2])), "%.1f %%" % (100 * m[3] / sum(m[2:])))
print("reactor", "%.1f %%" % (100 * tot[1] / sum(tot[:2])), "%.1f %%" % (100 * tot[3] / sum(tot[2:])))
```

```python
# tamano y ratio test/producto
from pathlib import Path
for kind in ("main", "test"):
    fs = [p for p in Path(".").rglob("src/%s/**/*.java" % kind) if "target" not in p.parts]
    print(kind, len(fs), sum(len(p.read_text(encoding="utf-8").splitlines()) for p in fs))
```

Dos trampas ya vistas: `jacoco.csv` viene con **BOM** (usar `utf-8-sig`, si no la primera cabecera
sale `LINE_MISSED` vacia), y la cobertura solo es la del ultimo `verify` completo de los `target/`:
un `verify -pl` parcial deja cifras viejas de otros modulos. **Tercera trampa, anadida el 18/11: no hacer
la suma a mano en PowerShell.** Los nombres de variable no distinguen mayusculas, y en la cuenta de Iter7
`$bc` (ramas cubiertas) y `$BC` se pisaron entre si y salio un total imposible (451 ramas cubiertas, 16,4 %).
Sacar la cuenta con un script de tres lineas sobre los `jacoco.csv` de todos los modulos, como el de arriba,
y usar **el mismo script en los dos refs** que se comparan.

## 6. Que dice la serie (veredicto del 11/11/2026, revalidado en el cierre de Iter6 el 15/11/2026 y en el
cierre de Iter7 el 18/11/2026)

1. **El 33 % de los defectos (6/18) siguen siendo F1 de proceso**, no de codigo: fusiones fuera de
   `main`/`develop`, issues cerradas sin artefacto, CSVs de version desalineados. Eso no lo arregla un
   modelo mas caro; lo arreglan las reglas (D29, D30) y su comprobacion. **Iter6 y Iter7 cierran con 0 F1**:
   rama de una sola CDU, PR a `develop`, CI verde antes de fusionar, issue con artefacto detras y tag sobre
   la fusion de cierre.
2. **F4 (codigo): 1 en Iter1, 1 en Iter3, 0 en Iter2/Iter4/D34/Iter5, 1 en Iter6 y 2 en Iter7.** 5 defectos
   de codigo en 162 commits (**3,1/100**, antes 2,3/100). **Iter7 es el tramo con mas F4 de todo el
   proyecto**, y con uno de los dos casos (DEF-16) viviendo commiteado casi dos dias: nace en `2c7ea4e`
   (16/11) y se arregla el 18/11 en `b38c149`/`21d9a3c`.
3. **El riesgo real del cuantizado no es el numero de F4 sino el falso verde, y DEF-14 lo roza.** Con
   DEF-12 (Testcontainers saltando en silencio) y DEF-13 (PNG recortado en silencio) el falso verde era
   de *herramienta*; DEF-14 es el primero de *contrato*: las 126 pruebas de la matriz y las 77 de
   adaptador/servicio construian las propiedades a mano, sin YAML, asi que **ninguna prueba miraba el
   contrato de configuracion** y el reactor estaba verde encima del defecto. Matic importante: lo pillo
   *escribir* la matriz D33, no *ejecutarla*, y dentro de la misma rama, asi que nunca llego a `develop`
   con CI verde. El umbral "1 falso verde colado en CI" no se cruza al pie de la letra, pero es la vez
   que mas cerca se ha estado. **En Iter7 la ventana CI se cierra mas**: la rama se pushea casi al final,
   asi que CI solo vio el estado ya corregido (un unico run, el del PR #48, verde) y los 253 rojos se
   resolvieron detras de la barrera local. Eso es bueno para `develop` y malo para el registro: **ningun
   rojo de Iter7 quedo documentado en CI**, y quien lea las acciones no vera la mitad del trabajo.
4. **Mitigacion acordada, no cambiar de modelo**: tapar el hueco con una **prueba de arranque de contexto
   con YAML** (deuda **D-02**, 11 de 30 ramas de la clase de propiedades siguen sin ejercitar) y **fijar
   puerta JaCoCo en `iso2-common`** (deuda **D-08**: tras Iter7 son **0 de 164 lineas y 0 de 56 ramas**,
   porque las tres clases nuevas de la fachada de la cola nacen sin prueba, y el pom del modulo no fija
   minimo, asi que hereda el 0,00 de la raiz y CI no se entera). Ninguna de las dos es codigo de CDU: son
   pruebas de contrato de configuracion, y se haran en el tramo que Ismael diga.
   **Y una mitigacion nueva que pide Iter7:** DEF-17 **es la misma equivocacion de Iter5** (entidad de
   agregado mutable sin `@Version`, detectada tambien por TC humanos). Cuando un error se repite en la CDU
   siguiente, la mitigation no es un modelo mas listo sino una **comprobacion mecanica**: queda pendiente de
   aceptar una prueba tipo ArchUnit que exija `@Version` a toda entidad de `dominio` que se modifique por
   API (la familia de pruebas de arquitectura ya vive en `iso2-citas/src/test/.../arquitectura`, asi que el
   sitio esta). No la numero como deuda hasta que Ismael la acepte.
5. **Umbrales para pedir modelo mas potente** (revisar en cada cierre, sobre la tabla de la seccion 3):
   - una familia de CDU acumula **> 2 defectos F4 detectados en revision/TC humanos** (CDU14 lleva **2**,
     CDU19 1, CDU16 1, CDU13 1, CDU12 0: **a un defecto del umbral**, y ademas los dos de CDU14 los pillo
     la matriz D33 dentro de la rama, no la revision ni los TC humanos, que es como esta redactado el
     disparador), o
   - **1 decision de diseno erronea que obligue a reescribir codigo ya commiteado** (hasta ahora cero: el
     `{vendor}` de Flyway se descarto antes de commitear, el refinado de CDU19 en `9ca712f` era
     documentacion antes de codigo, y en Iter7 los dos fixes (415 y `@Version`) son anadir lo que faltaba,
     no reescribir una decision de diseno), o
   - **1 falso verde colado en CI** (DEF-14 es el primer F4 con CI verde encima, pero corregido en la
     propia rama: se queda a un paso del umbral, no lo cruza. Ningun defecto de Iter7 llega a CI).
   Mientras no salten, se sigue con el flash cuantizado + D33 + CI. **Se confirma por tercera vez que la
   matriz D33 rinde**: en Iter6 las 126 filas de CDU19 no destaparon ningun F4 nuevo, y en Iter7 las 253 de
   CDU14 destapan **dos** (DEF-16 en la tabla de conjeturas H y DEF-17 en la secuencia E10) mas una familia
   de expectativas de spec falsas (DEF-18). Con la D33 en su sitio, el flash cuantizado saca esto; **lo que
   NO habria sacado el modelo mas caro son las dos expectativas de spec mal fijadas ni el `@Version`
   olvidado**, que son despiste de quien escribe la spec, y ahi lo que funciona es la comprobacion.
6. **Puertas de la release R2 alfa (27/11), que es donde cae Iter7 y donde estaban los umbrales del punto 6
   anterior: cumplidas con margen.** Medido con `mvn -B -ntp verify` en `21d9a3c`: **linea JaCoCo del
   reactor 86,7 %** (puerta >= 70) y **rama 76,8 %** (puerta >= 55), y **un TC RBAC por cada endpoint
   nuevo**: son **8 endpoints** los de `/api/listas-espera` (alta, cola, detalle, auditoria de entrada,
   auditoria verificable, prioridad, asignar y retirar) y los cubre **TC14-08**, el TC humano de permisos,
   con `ADMINISTRADOR`/`MEDICO`/`RECEPCIONISTA` en las cuatro lecturas y solo
   `ADMINISTRADOR`/`RECEPCIONISTA` en las cuatro escrituras (`TECNICO` y `CELADOR` fuera), aplicado en
   `ConfigSeguridad` y `MatrizPermisos` con los permisos `lista_espera:leer` / `lista_espera:escribir`.
   La exigencia >= 85 % de citas/solicitudes/seguridad es la puerta de **R5 final**, no la de ahora, pero
   citas ya esta en 93,5 / 89,5.

## 7. Plantilla para el cierre de cada iteracion

Al cerrar cada CFG (D29), anadir al registro de sesion de `STATUS.md` y actualizar a la vez la tabla de la
seccion 3 y el libro de la seccion 2 de este fichero:

```markdown
**Datos de defectos <IterN/CFG-NN>:** commits <tag-a>..<tag-b> = N ·
F1 proceso a · F2 entorno b · F3 contrato c · F4 codigo d · total e (fugados f) ·
e*100/N por 100 commits · detalle en TASADEFECTOS.md (DEF-xx..)
```
