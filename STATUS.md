# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-09 (sesión 17 · **Iter1 · CDU16 / CFG-01 CERRADA por completo**: método **D33**
aplicado (issue **#35**, PR **#36** → merge `f43721c`) y **TC humanos de CDU16 ejecutados y documentados**
(PR **#37** → merge `3601fd6`); tag **`cfg-iter01` reanotado** sobre el mismo commit `df09bca`; wiki de la Iter1
actualizada (merge `7b36c5c`); **#35 cerrada a mano por Ismael**; **limpiadas 12 ramas remotas y 12 locales**.
Sesión 16: cierre de Iter4 + D33/D34/D35. Prompts 15 y 16)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔ **Completada, etiquetada y publicada** — PR #7 fusionado a `develop` (`641e72d`), tag `cfg-iter00`; **R0 publicada en `main`**: PR #19 fusionado (merge `e165622`) con el tag anotado **`v0.1.0`** (regla **D30**: release desde `release/R0`) |
| Elaboración (Iter1–6) | 🟡 En curso — **Iter1 (CDU16 / CFG-01) CERRADA DEL TODO el 09/11**: puertas técnicas (tag `cfg-iter01`, reanotado sobre `df09bca`), **D33 aplicado** (PR #36 → merge `f43721c`) y **TC humanos de CDU16 hechos** (PR #37 → merge `3601fd6`). Iter2 (CDU17) y Iter3 (CDU13) cerradas en ramas y tagueadas (`cfg-iter02` → `870524e`, `cfg-iter03` → `b072b34`) pero **aún sin TC humanos**; Iter4 (CDU06) fusionada (PR #31 → merge `a3fe109`, tag `cfg-iter04`) y **sin TC humanos** (plantilla `docs/tc/cdu06-tc-humanos.md` creada, 14 casos). Lo que queda vivo: los **TC humanos de CDU17/CDU13/CDU06** y **extender el método D33 a Iter2/Iter3**. Las evidencias de modelado quedan resueltas con PlantUML (`.puml` + `.png` versionados, D31; StarUML descartado) |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Avance cuantificado (09/11/2026), sobre el plan de `docs/planificacion-iteraciones+Calidad.csv`:** el total
planificado son **612 h con IA** repartidas en 22 iteraciones (Iter0 40 h; Elaboración Iter1-6 188 h;
Construcción Iter7-20 304 h; Transición Iter21 80 h). **Fusionadas y tagueadas van Iter0-Iter4 = 156 h ≈ 25,5 %
del esfuerzo** (5 de 22 iteraciones). **Cerradas del todo, con TC humanos hechos e issue cerrada a mano, solo
Iter0 (CFG-00) e Iter1 (CFG-01) = 62 h ≈ 10,1 %.** En calendario vamos por delante: el plan ponía Iter1-3 del
09/11 al 13/11 e Iter4-6 del 16/11 al 20/11, y la Iter4 ya estaba fusionada el 08/11.

**Iteración en curso:** **Iter5 · CDU12 citas (CFG-05) — aún sin arrancar**. El 09/11 (sesión 17) se cerró
**del todo la Iter1 · CDU16 / CFG-01**, que es la primera que completa el ciclo entero: diseño → código →
puertas técnicas → **D33** → **TC humanos** → issue cerrada a mano → wiki al día. Sus dos trabajos de la sesión
fueron la issue **#35** (método **D33** sobre las pruebas de CDU16: PR **#36** → merge `f43721c`) y la
ejecución de los **TC humanos de CDU16** (PR **#37** → merge `3601fd6`). Antes, en la sesión 16, se cerró
**Iter4 · CDU06 (CFG-04)**: rama `feature/iter04-cdu06-solicitudes` con 4 commits (diseño PlantUML `cce3af2` →
servidor `71ce94b` → fix doclint `7f85678` → cliente `b4c4e27`) **fusionada por Ismael con el PR #31 (merge
`a3fe109`)** y el PR de docs **#32** (merge `3b02718`), tag **`cfg-iter04`** sobre `a3fe109`, wiki
`Iter4-CDU06-solicitudes` y **#30 cerrada a mano** por Ismael (19:01, D29 por adelantado).

**Dos cambios de enfoque pedidos por Ismael el 08/11:** (1) el **método D33** de pruebas — clases de
equivalencia + teorema del valor límite + conjetura de errores compuestos con **pairwise**, ejecutados con
**JUnit/Maven** y proyectados tales cuales a las **plantillas de TC humanos**, con JavaDoc en los ficheros de
test y la **clase probada** identificada en la wiki; **ya aplicado a la Iter1/CDU16** (evidencia en
`docs/test/iter01/`), pendiente de extender a Iter2/CDU17 e Iter3/CDU13. (2) La **capa de persistencia MySQL**
con Docker (MySQL + phpMyAdmin) y los patrones **DAO** y **Agente** (`AgenteMySQL`) — **D34 aceptada y
redactada**, con los cinco puntos ya decididos por Ismael (Testcontainers con MySQL 8, Flyway dual
`db/migration/h2` + `db/migration/mysql`, `gestoroperaciones` solo DML con usuario DDL aparte para Flyway,
`com.mysql:mysql-connector-j` sobre MySQL 8.4 con volumen, y rama propia `feature/iter05-persistencia-mysql`) y
**la implementación pendiente** (issue **#34**). Iter2 e Iter3 cerradas también con sus tags (`cfg-iter02` →
`870524e`, `cfg-iter03` → `b072b34`). Lo que mantiene abiertas **CFG-02/CFG-03/CFG-04**: los **TC humanos**
(D28) y, en CFG-02/CFG-03, **extenderles el método D33**. **CFG-01 ya no está abierta**: cerradas las puertas
técnicas, la D33 y sus TC humanos.

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · Git Flow: `main` + `develop` activas.
- **Fusionados:** PR #1 (plan PUD), PR #2 (calidad+config), PR #4 (`releaseplan`→`develop`),
  PR #5 (`feature/docs-prompt-log`), PR #8 (docs Iter0), **PR #7** (Iter0 código, merge `641e72d`),
  **PR #10** (Iter1/CDU16, merge `df09bca`), **PR #11** (docs sync cierre sesión 7), **PR #12**
  (higiene CFG-00/01, merge `67ed627`), **PR #14** (Iter2/CDU17, merge `870524e`) y **PR #18**
  (Iter3/CDU13, merge `b072b34`). CI verde en esos merges. **Rama `feature/iter03-cdu13-agendas` borrada**
  (remoto y local) tras el merge. **PR #24** (docs de la sesión 12) fusionado por Ismael: merge `c1219a8`.
  En la sesión 13 se fusionan **PR #25** (merge `1e4e4a6`) y **PR #26** (merge `d305763`), ambos de la rama
  `docs/plantuml-evidencia` (evidencias PlantUML D31 + plantillas de TC humanos + sync de STATUS/PROMPTS).
  En la sesión 14 se fusiona el **PR #28** (`ci/javadoc-pages` → `develop`, commit `00f93e9`, merge
  `c8fe2d7`). En las sesiones 15-16 se fusionan **PR #31** (código Iter4/CDU06, merge **`a3fe109`**) y
  **PR #32** (`docs/cierre-iter04-sesiones-14-15` → `develop`, merge **`3b02718`**), CI `construir` en verde.
  **Ramas `feature/iter04-cdu06-solicitudes` y `docs/cierre-iter04-sesiones-14-15` borradas** (remoto y local)
  tras el merge. En la **sesión 17 (09/11)** se fusionan el **PR #33** (cierre documental de la sesión 16 +
  D33/D34/D35 + plantilla `cdu06-tc-humanos.md`, merge **`cf2c707`**), el **PR #36** (pruebas D33 y arreglos D35
  de la Iter1/CDU16, rama `feature/iter01-pruebas-d33`, merge **`f43721c`**) y el **PR #37** (los TC humanos de
  CDU16 rellenados y ejecutados, commit `e70bc50`, merge **`3601fd6`**): **tip actual de `develop` =
  `3601fd6`**, CI `construir` en verde en los tres.
- **Higiene de ramas (09/11, sesión 17):** borradas **12 ramas remotas y 12 locales** ya fusionadas
  (`ci/javadoc-pages`, `docs/cierre-iter04-material`, `docs/cierre-sesion-7`, `docs/higiene-config-cfg00-01`,
  `feature/docs-estado-iter0`, `feature/docs-plan-inicial`, `feature/docs-prompt-log`, `feature/iter00-cfg00`,
  `feature/iter01-pruebas-d33`, `feature/iter02-cdu17-seguridad`, `release/R0`, `releaseplan`). La convención de
  la sesión 8 de «conservar las ramas fusionadas» queda **revocada**: todo su contenido es alcanzable desde
  `develop` y los puntos de interés están fijados por los tags `cfg-iterXX` y `v0.1.0`. **Quedan** `main`,
  `develop` y `feature/iter01-cdu16-catalogo` (esta **no fusionada**: su único commit único `19dff72` es una nota
  de cierre de jornada del 07/11 ya sustituida por este fichero; en la próxima sesión se decide si se borra).
  La rama local `main` se recreó apuntando a `origin/main`.
- **Release R0 publicada 08/11:** PR #19 (`release/R0` → `main`) fusionado por Ismael con merge commit
  `e165622` (padres `1245138` + `641e72d`) y tag anotado **`v0.1.0`** creado sobre ese merge: CFG-00 ya
  tiene las dos etiquetas de su fila del mapa (`v0.1.0;cfg-iter00`). Issue #20 **cerrada a mano por
  Ismael** (D29). `main` vuelve a contener solo contenido de release etiquetada y no aporta nada que no
  esté en `develop`: `git diff --stat origin/develop...origin/main` queda vacío, así que la publicación de
  R1 no arrastrará restos del PR #17. Rama `release/R0` **borrada** en remoto y local el 09/11 (el tag `v0.1.0`
  ya fija ese punto).
- **Open / en curso (09/11):** **no queda ningún PR abierto** (#33, #36 y #37 ya fusionados). En la sesión 17 se
  abre el PR de este sync documental → `develop`. **Issues abiertas: ninguna**, y hay que reabrir el seguimiento
  de dos: **#35** (pruebas D33) la cerró Ismael a mano el **09/11 00:26**, cuando la matriz todavía no se había
  ejecutado; el trabajo llegó después (PRs **#36** y **#37**) y se dejó constancia en un comentario de cierre, así
  que **no se reabre**. Pero **#34** (persistencia D34) se cerró **a la vez** (09/11 00:26) **sin trabajo detrás**:
  D34 sigue **aceptada y sin implementar** (no hay Docker, ni MySQL, ni DAO, ni `AgenteMySQL` en el repo). Para la
  próxima sesión hace falta **reabrir #34 o abrir una issue nueva** con la implementación de D34 (la apertura/cierre
  de issues es de Ismael, D29).
- **Javadoc publicado en GitHub Pages (D32, sesión 14):** workflow nuevo `.github/workflows/javadoc.yml`
  (push a `develop` + `workflow_dispatch`) que ejecuta `mvn -B -ntp javadoc:aggregate` y publica
  `target/reports/apidocs`. URL base **`https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/`**; solo la
  última compilación, sin instantáneas por tag. Issue #27 **cerrada a mano por Ismael** (19:48). También se
  corrigió en `ci.yml` el path del artefacto de Javadoc (`target/site/apidocs` → `target/reports/apidocs`).
- **Wiki del proyecto ampliada (sesión 14):** merge `90d5b26` en la rama `master` del repo wiki añade a las
  cuatro páginas por CFG la **ficha Javadoc enlazada de cada clase** y una **sección Testing**.
- **Wiki de la Iter1 rehecha con el método D33 (sesión 17, 09/11):** merge **`7b36c5c`** de `master` en el repo
  wiki (`5207bae..7b36c5c`). La página `Iter1-CDU16-catalogo.md` divide su **Testing** en tres bloques: el
  baseline de `cfg-iter01`; **«Rediseño TDOP (D33) — PR #36»** con las matrices **A33 + B15 + C36 + D9 + E22 =
  115** casos, `Cdu16MatrizTdopTest` como **clase probada**, los 16 fallos del rojo y los dos ajustes de spec, y
  las cotas nuevas de D35; y **«Testeo humano (D28) — ejecutado 09/11/2026»** con la tabla caso a caso. Cifras
  contrastadas con `jacoco.csv`: **139 tests en `iso2-catalogo`** (24 heredados + 115 TDOP) y **JaCoCo real de
  `iso2-catalogo` 97,5 % líneas (195/200) y 82,5 % ramas (33/40)**.
- **Incidencia y reparación de `main` (08/11, registrada como D30):** el PR #17
  (`feature/iter03-cdu13-agendas` → `main`, merge `5e0ef29`) se abrió por error. Como antes de ese PR
  `main` estaba justo en el commit raíz `1245138` y el único commit de `main` no alcanzable desde
  `develop` era el propio merge, se descartó con `git push --force-with-lease` **sin perder trabajo**
  (los 15 ficheros / 1432 líneas ya estaban en `develop`). `main` quedó en `1245138` y ahora recibe la
  release desde `release/R0` (rama creada sobre `cfg-iter00` = `641e72d`, árbol idéntico a CFG-00).
  Se descartó `revert -m 1` (impediría re-aplicar esos ficheros al fusionar `develop` en el futuro).
- **Tags de configuración creados:** `cfg-iter00` → `641e72d`, `cfg-iter01` → `df09bca`,
  `cfg-iter02` → `870524e`, `cfg-iter03` → `b072b34`, **`cfg-iter04` → `a3fe109`** (los cinco anotados y
  subidos) y la etiqueta de release **`v0.1.0` → `e165622`** (merge del PR #19 en `main`, anotada y subida).
- **Tag `cfg-iter01` reanotado el 09/11 (misma decisión de Ismael, misma posición):** el tag sigue apuntando al
  **mismo commit `df09bca`** (el merge del PR #10, el único de `develop` que hay entre `cfg-iter00` y
  `cfg-iter02`: CFG-01 solo vive ahí) pero el objeto del tag cambia `af95aff` → **`429d67f`**, subido con
  `git push --force-with-lease`. El mensaje nuevo fija las puertas técnicas de CFG-01 y añade el bloque
  **«Reanotado el 09/11/2026 (mismo commit, mismo árbol)»** que explica que el rediseño D33 y las cotas D35 están
  en `develop` (`f43721c`), que los TC humanos de CDU16 ya están ejecutados, que el `.staruml` no procede (D31) y
  el hallazgo del TC16-03. **No se repuntó a `f43721c`** para no cambiar qué configuración etiqueta el mapa de
  CFG-01.
- **Nueva desviación en `main` (08/11 19:01) — cerrada documentando (decisión de Ismael, sesión 16):** el
  **PR #29** (`develop` → `main`) se fusionó con merge commit **`d12388b`** (padres `e165622` = `v0.1.0` +
  `c8fe2d7`). Contradice **D30** (`main` solo recibe ramas `release/RN`) y **no lleva etiqueta de release**:
  `main` quedó por delante de `v0.1.0` con contenido de `develop` sin CFG cerrada por release. Ismael decide
  **dejarlo y documentarlo**: se deja la historia como está (sin revert ni reescritura del remoto) y se cierra
  la anomalía publicando **R1 desde una rama `release/R1` creada sobre el tag `cfg-iter04`** con su PR a `main`
  y su tag `v0.1.1`, que es lo que fija D30. Registrado en D30. **El agente no toca `main`** en ningún caso.
- **Issues cerradas a mano (D29):** #6 (Iter0/CFG-00), #9 (Iter1/CDU16), #13 (Iter2/CDU17),
  #15 (Iter3/CDU13), #20 (publicar R0, tras el merge `e165622` + tag `v0.1.0`), **#27 (publicar el Javadoc
  en Pages, 19:48, tras el merge `c8fe2d7` del PR #28)**, **#30 (Iter4/CDU06, 19:01)** y, el **09/11 00:26**,
  **#35** (pruebas D33 de la Iter1) y **#34** (persistencia D34). De las dos últimas: **#35 no se reabre** — el
  trabajo llegó después del cierre (PRs #36 y #37) y quedó anotado en un comentario de cierre
  ([comentario del 09/11](https://github.com/IsmaelCaballero/PrISO2-25-26/issues/35#issuecomment-6744772304));
  **#34 en cambio se cerró sin trabajo detrás** y su implementación sigue pendiente (ver Deuda nº 19).
- **TC humanos de CDU16 ejecutados por el agente (09/11) — excepción puntual a D28 autorizada por Ismael:**
  D28 sigue vigente (los TC humanos son de Ismael), pero el 09/11 **el agente los ejecutó y los rellenó** vía
  HTTP contra el servidor local en `localhost:8080`, y eso está dicho en el propio doc: cada fila de la tabla
  lleva el executor y la nota de la excepción. Resultado: **TC16-01…TC16-06 en 16 pasos TC16H-01…TC16H-16,
  todos correctos**, y **5 asientos nuevos de auditoría encadenada** (baja lógica de `TC16-PBA-01` + 4
  reactivaciones), con `auditoriaVerificable() = true`. Dos cosas quedan para Ismael: **refrendar el resultado**
  y **repetirlos él en el navegador/H2 cuando le venga bien** (la ejecución del agente vale como evidencia, no
  como su trabajo). El doc además se corrigió: decía «5 TC humanos» y la plantilla tiene **6**.
- **Issue #30 (Iter4/CDU06) — cierre por adelantado:** Ismael la cerró a las **19:01** (+0100), cuando la
  rama feature solo tenía el commit de diseño (`cce3af2`, 19:04) y el código aún no estaba commiteado; el
  cross-reference del PR #31 llega a las 19:55. **No se reabre**: el trabajo está hecho y en el PR #31, así
  que D29 queda cumplida y el cierre simplemente es anterior al merge.
- **Issues #21, #22 y #23 — situación final (08/11 tarde):** el agente las reabrió al ver que su cierre
  de las 12:34–12:35 no tenía artefactos detrás; **Ismael las volvió a cerrar (12:56–12:57) y no se
  reabrirán**: el seguimiento queda en este `STATUS.md`. Cierre real: **#22 modelado** → resuelto con
  **D31** (evidencias `.puml` + `.png` versionados; StarUML descartado); **#21 TC humanos** → plantillas
  creadas en `docs/tc/` (estructura + puertas prefijadas), pendientes de que Ismael las rellene y ejecute
  (siguen siendo criterio de cierre de CFG-01/02/03, D28); **#23 Kanban + wiki** → wiki creada con su
  página `Home.md` enlazando los ficheros vivos; el Kanban (GitHub Project) no es verificable con el token
  del agente (sin scope `read:project`): queda a la revisión visual de Ismael.
- Identidad git del repo: IsmaelCaballero@users.noreply.github.com (cambiar si se prefiere el correo UCLM).

## Registro de sesiones

| Sesión | Fecha | Hecho | Tokens aprox. |
|---|---|---|---|
| 1 | 07/11/2026 | Planificación PUD Teoría 1: RF→CDU (20), 21 iteraciones, releases, coste ≈16.530 € vs 26.230 € sin IA. Ficheros: docs/planificacion-PUD.md, docs/planificacion-iteraciones.csv, DECISIONS.md (D1–D9) | ~15k |
| 2 | 07/11/2026 | Reglas del equipo registradas (CONTEXT.md), PLAN.md operativo con desglose servidor/cliente + plan de despliegue alfa/beta/final + presupuesto de tokens por iteración, STATUS.md. Clonado repo GitHub, main+develop, pendientes D18 (StarUML) y decisiones D10–D20 | ~12k |
| 3 | 07/11/2026 | Planificación de evolución de componentes: docs/componentes-versionado.md (14 submódulos Maven, mapeo CDU→componente, riesgo T1–T4, incrementos 1.y.0, esfuerzo 492 h/12.300 €/≈2,1M tokens, matriz componente×iteración) + docs/componentes-versiones.csv. Alineados PLAN.md y planificacion-iteraciones.csv (baseline 1.0.0 + versiones 1.x.0). ADRs D22–D23 (esquema de versionado + inversión Iter7↔9) | ~14k |
| 4 | 07/11/2026 | Plan de calidad + plan de gestión de configuración y mapa de liberaciones (rama `releaseplan`): SOFTWAREQUALITY.md (modelo ISO 25010 de 5 características, DoD+calidad, impacto por componente y por CDU, puertas), docs/mapa-configuraciones.md+csv (CFG-00…CFG-21, alfa 20/11 y 27/11, beta 04/12, rc 11/12, definitiva 15/12 por criterios, rollback), versiones +Calidad de los tres planes: docs/planificacion-PUD+Calidad.md (v2.0.0), docs/planificacion-iteraciones+Calidad.csv, docs/componentes-versionado+Calidad.md + docs/componentes-versiones+Calidad.csv. ADRs D24–D27. Arreglado encabezado §1 de componentes-versionado.md (14 módulos) | ~35k |
| 5 | 07/11/2026 | Fix de render de CSV en GitHub (comas que faltaban: filas iter7/8/13/20 del base en `feature` y fila iter18 del +Calidad en `releaseplan`; todos los CSV verificados uniformes). Reapertura de PR #1/#2 cerrados sin fusión; Ismael fusiona #1 y #2 y abre #3 (`develop`→`main`). Detección de que el contenido de sesión 4 no llegó a `develop` → **PR #4** (`releaseplan`→`develop`). Nuevo **PROMPTS.md** (registro cronológico de prompts del alumno, PR #5) y arranque de la etapa de código: issue Iter0 + **Prompt 08** preparado en PROMPTS.md (esqueleto Maven CFG-00) | ~30k |
| 6 | 07/11/2026 | **Iter0 de código (Prompt 08 ejecutado, issue #6):** reactor `iso2-parent` con 14 módulos — `iso2-common` 1.0.0 (10 fachadas de contratos con DTOs record anidados + Paginacion/ApiError + ArchUnit sin frameworks y sin ciclos), 10 módulos de dominio 1.0.0 baseline (package-info con mapa de incrementos + test de humo), `iso2-server-app` 0.1.0 (Spring Boot 3.5.4 + H2 en fichero + JPA, contexto OK), `iso2-client-app` 0.1.0 (JavaFX 21.0.4, test headless OK), workflow CI (`verify javadoc:javadoc`, JDK 21, artefactos jacoco/surefire/apidocs), puertas JaCoCo (0.00 en Iter0) y `javadoc.failOnError` por módulo. `mvn verify javadoc:javadoc` BUILD SUCCESS 14/14. Commit `e89863b` en `feature/iter00-cfg00`; **PR #7** → `develop`. Ismael fusiona mientras #4/#5 quedaban fusionados | ~45k |
| 7 | 07/11/2026 | **Iter1 · CDU16 (Prompt 09 ejecutado, issue #9):** D18 aceptada (fuentes PlantUML + export manual a StarUML) y nueva **D28** (los TC humanos se diseñan sobre código funcional; el agente solo escribe pruebas técnicas de humo). Rama `feature/iter01-cdu16-catalogo`: `common`+`catalogo` a 1.1.0-SNAPSHOT y apps a 0.1.1-SNAPSHOT, parent con `-parameters` en el compiler (sin eso `@RequestParam`/`@PathVariable` dan 400). `iso2-catalogo` 1.1.0: dominio `TipoPrueba` (+`Protocolo`/`Politica` embebidos), auditoría SHA-256 encadenada con `auditoriaVerificable()`, baja lógica idempotente y códigos no reutilizables, REST `/api/catalogo/pruebas` (201/200/204 + reactivación 200, errores 400/404/409 con `ApiError`) — 24/24 tests, JaCoCo 96,5 %. `iso2-server-app` 0.1.1: Flyway `V1.1.0__cdu16_catalogo.sql`, `ConfigSeguridad` Basic BCrypt admin/celador (RBAC transitorio; JWT en CDU17), smoke 401/403/200/201/400 — 6/6, 91,7 %. `iso2-client-app` 0.1.1: `ApiClienteCatalogo`/`ApiException` + `VistaAdminCatalogo` — 6/6, capa `api` 79,2 % (UI JavaFX excluida del gate). `docs/modelo/iter01/`: 4 diagramas PlantUML + README de exportación StarUML, validados con PlantUML 1.2025.2 y revisados a mano; el de casos de uso lleva actores de rol (Médico/Técnico/Celador/Recepcionista) solo a UC16.5 y UC7 de auditoría fuera de la frontera. Commits `bb337f5`..`ad99b21` + merge `7996c88`; **PR #10 a `develop` fusionado por Ismael** (merge `df09bca`, CI verde) justo después del PR #7 (merge `641e72d`). Pendientes de Ismael: TC humanos (D28), export `.staruml`, tags y cierre manual de #6/#9 (ver D29) | ~55k |
| 8 | 08/11/2026 | **Higiene de configuración CFG-00/CFG-01:** fusión del PR #11 (docs sync, merge `2e5d4b3`); tags anotados `cfg-iter00`→`641e72d` y `cfg-iter01`→`df09bca` subidos a remoto; issues #6 y #9 cerrados a mano con comentario D29. Decisión: `v0.1.0` (R0) se tagueará sobre `main` al abrir el PR nuevo `develop`→`main`; ramas antiguas conservadas (no borrar). STATUS.md sincronizado en rama `docs/higiene-config-cfg00-01` | — |
| 9 | 08/11/2026 | **Iter2 · CDU17 seguridad RBAC+JWT (Prompt 10 ejecutado, issue #13):** rama `feature/iter02-cdu17-seguridad` desde `develop` (`67ed627`); `iso2.version` 1.1.0-SNAPSHOT y apps 0.1.2-SNAPSHOT (jjwt 0.12.6 en el parent). `iso2-seguridad` 1.1.0 (módulo nuevo): dominio `Usuario` JPA (BCrypt + bloqueo temporal 5 intentos/15 min), `Rol` (ADMINISTRADOR/MEDICO/TECNICO/CELADOR/RECEPCIONISTA), `Permiso` + `MatrizPermisos` autoritativa; `SeguridadServicio` implementa `AutenticacionFacade` con `@Transactional(noRollbackFor=CredencialesInvalidasException)` (sin eso el bloqueo nunca persistía — lo destapó el test de integración); `JwtUtils` HS256 con secreto solo por `ISO2_JWT_SECRET` (fail-fast); `POST /api/auth/login` + `JwtAutenticacionFiltro` stateless — 36/36 tests, JwtUtils 100 % línea. `iso2-server-app` 0.1.2: cadena stateless + matriz RBAC por endpoint (GET catálogo cualquier rol, escritura solo ADMIN, h2-console ADMIN vía JWT), 401/403 con `ApiError`, Flyway `V1.2.0__cdu17_seguridad.sql` con 6 cuentas semilla — 23/23 tests (matriz RBAC completa + ciclo de bloqueo real). `iso2-client-app` 0.1.2: `ApiClienteAutenticacion` + `VistaInicioSesion` login-first, catálogo Bearer, `NavegacionPorRol` (editable solo ADMIN) + `VistaConsultaCatalogo` — 16/16 tests. `docs/modelo/iter02/`: 4 diagramas PlantUML + README, renderizados a SVG sin errores (PlantUML 1.2025.2, `-Playout smetana`). `mvn -B -ntp verify javadoc:javadoc` BUILD SUCCESS 14/14. Commits `c8213ee`..`2f8090a` + docs sync; **PR #14 → `develop` abierto, pendiente de fusión de Ismael** (tag `cfg-iter02` y cierre manual de #13 al fusionar, D29) | ~55k |
| 10 | 08/11/2026 | **Iter3 · CDU13 agendas — parte servidora (Prompt 11 ejecutado, issue #15):** sesión «a» de la división en dos (CONTEXT.md §7, iteración de alta). Iter2 cerrada antes por Ismael (PR #14 fusionado, merge `870524e`; **#13 cerrada; tag `cfg-iter02` aún pendiente**). Rama `feature/iter03-cdu13-agendas` desde `870524e`. `iso2-common` 1.1.0: `AgendaFacade` extendida en MINOR sin tocar el baseline congelado (`franjasLibres`+`FranjaDTO`) — `listarAgendas`/`buscarPorProfesional`/`crearAgenda`/`anadirFranja`/`eliminarFranja`/`anadirFestivo`/`eliminarFestivo` + registros `AgendaDTO`/`FranjaGestionDTO`/`FestivoDTO` + excepciones `AgendaNoEncontradoException`/`AgendaConflictoException` (SOLAPE/FESTIVO/AGENDA_DUPLICADA). `iso2-agendas` 1.1.0: dominio JPA `Agenda`/`Franja`/`Festivo` + auditoría SHA-256 encadenada con GENESIS; reglas: sin solape (bordes que se tocan NO solapan), el festivo manda (franja en festivo, festivo con franjas y festivo repetido → FESTIVO), agenda duplicada → AGENDA_DUPLICADA, `inicio < fin` → 400; REST `/api/agendas` con 404/409/400. **Fix notable:** Hibernate inserta los hijos en cascada con id `@IDENTITY` pero no reasigna el id en la instancia en memoria (probado con test de descarte) → `FranjaRepositorio`/`FestivoRepositorio` persisten los hijos directamente para que el DTO de alta devuelva id. RBAC: `Permiso` +AGENDA_LEER/AGENDA_ESCRIBIR; matriz: ADMIN todo, MEDICO/TECNICO/RECEPCIONISTA leen, RECEPCIONISTA además escribe, CELADOR sin agendas. `iso2-server-app` 0.1.3: montado `iso2-agendas` `${iso2.version}` (el resto sigue 1.0.0), cadena de seguridad de agendas, Flyway `V1.3.0__cdu13_agendas.sql` (4 tablas + semilla dev). Tests: `AgendaServicioTest` 15/15 + ArchUnit 4/4 + baseline 1/1 (JaCoCo ≥ 60 % en verde), `AgendasApiTest` 16/16 (RBAC + solape/festivo por HTTP con H2). `docs/modelo/iter03/`: 4 diagramas PlantUML + README validados a SVG. `mvn -B -ntp verify` BUILD SUCCESS 14/14 + javadoc OK. Commits `e02f425`..`0366247` subidos a remoto **sin PR** (el PR se abre en 11b). Queda para **Prompt 11b**: `iso2-client-app` 0.1.3 + tests de integración + apertura del PR de Iter3 | ~55k |
| 11 | 08/11/2026 | **Iter3 · CDU13 agendas — parte clienta + integración + PR (Prompt 11b ejecutado, issue #15):** sesión «b» en la misma rama `feature/iter03-cdu13-agendas`. `iso2-client-app` 0.1.3-SNAPSHOT: `ApiClienteAgendas` contra `/api/agendas` (listar, detalle, alta/baja de agenda/franja/festivo, franjas libres) con el token del login real, `ApiException.mensaje()` nuevo para mostrar el texto del servidor en la UI; barra de secciones Catalogo/Agendas (escena 1100x680) con `VistaGestionAgendas` (edicion, ADMIN/RECEPCIONISTA) y `VistaConsultaAgendas` (lectura, MEDICO/TECNICO) segun `NavegacionPorRol` (`agendasVisiblePara`/`agendasEditablesPara`, CELADOR fuera); 37/37 tests, 11 de ellos con servidor HTTP local (Jackson manda la hora ISO con segundos `08:00:00`). `iso2-server-app`: dep test-scoped de `iso2-client-app` 0.1.3 (version explicita, sin ciclo) + `AgendasClienteServidorIntegracionTest` 7/7 con la app servidora en RANDOM_PORT consumida por los clientes HTTP reales: flujo recepcionista con `CONFLICTO_SOLAPE`/`CONFLICTO_FESTIVO`, festivo que manda en ambos sentidos, duplicada 409, 404 con codigo en cuerpo, CELADOR 403 y franjas libres de la semilla del 9 al 13/11 con el festivo del 10 fuera (ids `it-*`, fechas 2027). **Fix destapado por la integracion:** el detalle de agenda inexistente respondia 404 con cuerpo vacio -> lanza `AgendaNoEncontradoException` y el advice devuelve `AGENDA_NO_ENCONTRADA`. Puertas CFG-03 tecnicas en verde (solapes/festivos por HTTP en ambos sentidos, ArchUnit, JaCoCo >= 60 %, ambas apps 0.1.3); `mvn -B -ntp verify` BUILD SUCCESS (server-app 46/46, client-app 37/37). Commits `ae79d7e`..`56dac60` subidos; **PR #18 → `develop` abierto** (sin `Closes`, D29), pendiente de fusion de Ismael + cierre manual de #15 + tag `cfg-iter03` | — |
| 12 | 08/11/2026 | **Cierre de Iter3 en GitHub + reparación de `main` y release R0 publicada (Prompt 11c):** Ismael fusiona **PR #18** a `develop` (merge `b072b34`), cierra **#15** a mano y la CI queda verde. El agente crea y sube los tags anotados **`cfg-iter02` → `870524e`** y **`cfg-iter03` → `b072b34`** con el mismo estilo que `cfg-iter00/01` (puertas técnicas + pendientes D28/D18). Detectada la desviación de `main`: el **PR #17** (`feature/iter03-cdu13-agendas` → `main`, merge `5e0ef29`) se abrió por error; verificado con `git log origin/main --not origin/develop` que el único commit descartable era el propio merge (nada de `main` estaba fuera de `develop`) y que `main` estaba justo en el raíz `1245138`. Reparación con visto bueno de Ismael: `git push --force-with-lease=refs/heads/main:5e0ef29… origin 1245138:refs/heads/main` → `origin/main` = `1245138`. Creada la rama **`release/R0`** sobre `cfg-iter00` (`641e72d`, árbol idéntico a CFG-00) y abierto el **PR #19 → `main`** para publicar R0 con `v0.1.0` sobre su merge. Borrada la rama `feature/iter03-cdu13-agendas` (remoto y local). Nuevas issues de seguimiento: **#20** publicar R0, **#21** TC humanos CDU16/17/13, **#22** exports `.staruml` Iter1–3, **#23** Kanban + wiki. Nueva **ADR D30** en `DECISIONS.md` (`main` solo recibe merges de `release/RN` + comprobación previa) y §4 de `docs/mapa-configuraciones.md` actualizado. Ismael fusiona el **PR #19** con merge commit `e165622` (padres `1245138` + `641e72d`) y el agente crea y sube el anotado **`v0.1.0`** sobre ese merge: hito R0 completado y CFG-00 con las dos etiquetas de su fila del mapa; **#20 cerrada a mano por Ismael** (D29). Comprobación adicional de fin de sesión: #21, #22 y #23 se habían cerrado a las 12:34–12:35 con motivo `completed` sin artefactos detrás (sin commits de TC humanos, sin `.staruml` versionado y wiki sin páginas), así que quedan **reabiertas** con un comentario en cada una con la comprobación hecha. Ficheros de esta sesión: `DECISIONS.md`, `docs/mapa-configuraciones.md`, `STATUS.md`, `PROMPTS.md` en la rama `docs/cierre-iter03-y-r0` → PR a `develop` | — |

| 13 | 08/11/2026 | **Modelado con PlantUML (D31): PNG versionados + plantillas de TC (Prompt 11d):** verificado `tools/plantuml.jar` 30.02 (Java 25.0.1): regenera los PNG de forma reproducible (2 diagramas de iter01, idénticos byte a byte). Comprobado que **no existe ningún `.staruml`** en ramas, historia, wiki ni disco → nueva **ADR D31** (artefactos de modelado = `.puml` + `.png` versionados en `docs/modelo/iterXX/`, incrustados en el README con enlace al tamaño completo; herramienta `tools/plantuml.jar` no versionada) y D18 anotada como revisada. `CONTEXT.md` §4 renombrado a "Modelado por iteración (PlantUML)". README de iter01/02/03 adaptados: imágenes `<a href><img width=…></a>` + sección de render. Creadas **plantillas de TC humanos** en `docs/tc/` (README + `cdu16/17/13-tc-humanos.md` con Tipo/Puerta prefijados desde la matriz de `SOFTWAREQUALITY.md`, pasos y resultados en blanco para Ismael, D28). Wiki del repo (`PrISO2-25-26.wiki.git`): `Home.md` reindexado + páginas `Iter0-CFG-00-esqueleto`, `Iter1-CDU16-catalogo`, `Iter2-CDU17-seguridad` e `Iter3-CDU13-agendas`, cada una con la tabla de componentes y versiones de la iteración, los 4 diagramas embebidos a tamaño reducido con enlace al PNG completo (D31) y las puertas de su CFG; 12 PNG copiados a `images/` del repo wiki (merge `50fd48b`). Verificado en el HTML renderizado que `<img width>` y los enlaces resuelven. Rama `docs/plantuml-evidencia` + **PR #25** a `develop` (CI verde, pendiente de fusión de Ismael). Ismael fusiona antes el PR #24 (merge `c1219a8`) y re-cierra #21/#22/#23 (el seguimiento pasa a STATUS) | — |
| 14 | 08/11/2026 | **Javadoc agregado en GitHub Pages (D32, Prompt 12) + wiki ampliada:** petición de Ismael de enlazar las wiki-páginas a la ficha Javadoc de cada clase → hace falta un Javadoc publicado y estable. Issue **#27** creada y **cerrada a mano por Ismael (19:48)**. Rama `ci/javadoc-pages`: workflow nuevo `.github/workflows/javadoc.yml` (push a `develop` + `workflow_dispatch`, JDK 21, `mvn -B -ntp javadoc:aggregate`, `upload-pages-artifact` de `target/reports/apidocs` + `deploy-pages`, permisos `pages:write`/`id-token:write`), `docs/javadoc-index.html` como índice y **fix en `ci.yml`**: el artefacto de Javadoc apuntaba a `**/target/site/apidocs/**` cuando el plugin escribe en `target/reports/apidocs`. ADR **D32** en `DECISIONS.md` (Pages publicado desde `develop`, URL base `https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/`, solo la última compilación, sin instantáneas por tag; las wiki-páginas enlazan la ficha de cada clase). Commit `00f93e9` + **PR #28 → `develop` fusionado por Ismael** (merge `c8fe2d7`, tip actual de `develop`); deploy de Pages verificado. En el repo wiki, merge **`90d5b26`**: las cuatro páginas por CFG ganan la **ficha Javadoc enlazada de cada clase** y una **sección Testing**. Creada también la issue **#30** (Iter4 · CDU06, CFG-04) con el prompt ejecutable en el cuerpo | — |
| 15 | 08/11/2026 | **Iter4 · CDU06 solicitudes (CFG-04, Prompt 13) — flujo diseño-primero:** rama `feature/iter04-cdu06-solicitudes` desde `develop` (`c8fe2d7`). **Primero el modelo** (commit `cce3af2`, 19:04): `docs/modelo/iter04/` con los 4 `.puml` + 4 `.png` (casos de uso, análisis, diseño y secuencia) validados con `tools/plantuml.jar -tpng -Playout smetana`; el diseño fija el dominio `Solicitud` con **huella SHA-256 del canónico**, `creadaEn` truncado a `MICROS`, anti-duplicado de servicio, Flyway `V1.4.0`, permisos `solicitud:leer/escribir` y puerta `jacoco.line.min=0.85`. **Servidor** (`71ce94b`, 19:34): `iso2.version` y `iso2-solicitudes` a **1.1.0-SNAPSHOT**, apps **0.1.4-SNAPSHOT**; `SolicitudFacade` extendida en MINOR (`prescribir`/`listarPorMedico`/`detalle`/`trazable`) sin tocar el baseline congelado; dominio JPA con RF obligatorios (indicación clínica, prioridad, tipo de prueba, profesional, paciente), no repudio (autor + timestamp + huella), duplicado imposible, `Clock` inyectable; REST `/api/solicitudes` con RBAC y Flyway `V1.4.0__cdu06_solicitudes.sql`. **Fix** (`7f85678`): doclint no admite `@throws` en un `record`. **Cliente** (`b4c4e27`, 19:51): `ApiClienteSolicitudes` (5 campos; el autor NO viaja en el cuerpo, lo fija el servidor), `VistaSolicitudesMedico` + `VistaConsultaSolicitudes` y puertas `solicitudesVisiblesPara`/`solicitudesPrescribiblesPara`. **Reactor verde** `mvn -B -ntp verify javadoc:javadoc`: **229 tests** (Iter3: 172; solicitudes 1→24, server-app 46→60, client-app 37→57) y **JaCoCo de `iso2-solicitudes` 87,2 %** (109/125 líneas, 32/50 ramas) ≥ 85 %. **PR #31 → `develop` abierto** (19:55, sin `Closes`) con la CI `construir` **en verde** (run 42512406096, 2m6s). #30 ya la había cerrado Ismael a las 19:01 (D29 por adelantado) | — |
| 16 | 08/11/2026 | **Cierre de Iter4 en GitHub + nueva estrategia de pruebas (D33) y arranque de la persistencia MySQL (Prompt 14):** Ismael fusiona **PR #31** (merge `a3fe109`) y **PR #32** (merge `3b02718`), CI verde. El agente crea y sube el tag anotado **`cfg-iter04` sobre `a3fe109`** (el merge del código, no el documental posterior) y borra la rama `feature/iter04-cdu06-solicitudes` (remoto y local). Wiki: página nueva **`Iter4-CDU06-solicitudes.md`** con 4 PNG en `images/` y `Home.md` enlazado (merge `5207bae` del repo wiki). Creada la plantilla que faltaba, **`docs/tc/cdu06-tc-humanos.md`** (14 casos TC06-01…TC06-14 sobre la superficie real: `POST /api/solicitudes`, `GET /mias`, `GET /{id}`, `GET /{id}/integridad`, semillas `SEM-RX-TORAX`/`SEM-RM-RODILLA`). Detectada **nueva desviación de D30**: el PR #29 fusionó `develop` → `main` (merge `d12388b`, 19:01) sin etiqueta de release. Nuevas directrices de Ismael: **D33** — diseñar los casos con **clases de equivalencia + teorema del valor límite + conjetura de errores**, composarlos con **pairwise**, ejecutarlos con **JUnit/Maven** y usar **esos mismos casos** en las plantillas de TC humanos; el agente genera casos y plantillas; se empieza por **las pruebas de la Iteración 1**, con **JavaDoc** en los ficheros de test y la wiki al día **identificando la clase probada**; y **persistencia MySQL** con **Docker (MySQL + phpMyAdmin)**, usuario `gestoroperaciones` y patrones **DAO** y **Agente** particularizado en **`AgenteMySQL`** (ADR **D34** al implementarse) | — |
| 17 | 09/11/2026 | **Iter1/CFG-01 cerrada del todo: método D33 sobre CDU16 + TC humanos (Prompts 15 y 16):** rama `feature/iter01-pruebas-d33` desde `develop`. Primero la **spec** (`355ade9`): `docs/test/iter01/tdop-cdu16-espec.json` con las clases de equivalencia, los puntos límite y la conjetura de errores de CDU16, y dos generadores nuevos en `tools/` — **`pairwise.py`** (compone con pairwise sobre restricciones y generadores de valores, sin librerías) y **`tc_plantillas.py`** (proyecta la matriz a la plantilla de TC humanos entre `<!-- TDOP:BEGIN -->`/`<!-- TDOP:END -->`). Matrices generadas: **A33 + B15 + C36 + D9 + E22 = 115 casos** en `docs/test/iter01/tdop-cdu16.md` y en `iso2-catalogo/src/test/resources/tdop/`. Rojo real: **16 fallos** sobre 115, que destaparon los huecos de D35 y **dos discrepancias de spec** (`CODIGO_DUPLICADO`, y el 400 por tamaño de página) → **se arregla la spec, no el test**. Fixes (`b2f7356`, D35): `LímitesCatalogo` valida las cotas en el servicio devolviendo 400 en vez de reventar contra la BD, y se valida `pagina`/`tamano`; antes, **el `.puml` de iter01** (`3a17a9a`, D31). Verde (`f73ac80`): **115/115 TDOP** y **139 tests en `iso2-catalogo`**, **JaCoCo 97,5 % líneas (195/200) y 82,5 % ramas (33/40)** sobre la puerta real `jacoco.line.min=0.60`. **PR #36 → `develop` fusionado por Ismael** (merge `f43721c`), CI verde. Wiki rehecha (merge `7b36c5c`) con la matriz, la clase probada y los 16 fallos del rojo. **TC humanos de CDU16 rellenados y ejecutados por el agente** (excepción puntual a D28, anotada en el doc): TC16-01…06 en 16 pasos, 5 asientos de auditoría nuevos y el doc corregido (decía 5 TC, son 6) → **PR #37 fusionado** (commit `e70bc50`, merge `3601fd6`, tip de `develop`). Tag **`cfg-iter01` reanotado** sobre `df09bca` (objeto `af95aff` → `429d67f`, `--force-with-lease`) con el bloque «Reanotado el 09/11/2026». Comentario de cierre en **#35** (Ismael la había cerrado a mano a las 00:26). **Borradas 12 ramas remotas y 12 locales** fusionadas. Detectado: **#34 quedó cerrada a la vez (00:26) sin trabajo de D34 detrás**. BD H2 local `./data/iso2-server` **conservada** con las filas `TC16H-*` y los asientos de auditoría | — |
| 18 | 11/11/2026 | **D34 implementada: MySQL 8.4 en Docker + Flyway dual + capa DAO/Agente (Prompt 17, issue #34 reabierta):** rama `feature/iter05-persistencia-mysql` desde `develop` (`737b932`, merge del PR #38). Commits: `b15d927` (`docker/`: `mysql:8.4` en host **3307** + phpMyAdmin **8081** + volumen `iso2-mysql-datos` + `init/01-usuarios.sql` con `gestoroperaciones` solo DML y `iso2_flyway` con DDL; root `Iso2Curso25-26-Root`), `64e92eb` (Flyway **dual** `db/migration/h2` + `db/migration/mysql` elegida **por perfil** — descartado `{vendor}` porque Flyway 11 escanea ambas subcarpetas y produce versiones duplicadas; perfil `mysql` en el `application.yml`; `mysql-connector-j` + `flyway-mysql`; apps subidas a **0.1.5-SNAPSHOT**, tren de la release CFG-05), `ae40ad7` (diseño-primero D31: `docs/modelo/iter05/persistencia-clases.{puml,png}` + README **antes** del código) y `bba56f8` (paquete **nuevo** `es.uclm.iso2.server.persistencia`: interfaz `Agente` + `AgenteH2`/`AgenteMySQL` elegidos por `DatabaseMetaData.getDatabaseProductName()`, DAO JDBC `DiagnosticoPersistenciaDao`, REST `GET /api/persistencia/diagnostico` solo ADMINISTRADOR (regla nueva en `ConfigSeguridad`), 9 tests de API en H2 + 3 Testcontainers contra `mysql:8.4` real, incluido TC-D34-MY-03: `gestoroperaciones` no puede hacer DDL → error 1142). **Incidente Testcontainers/Docker:** el BOM de Boot 3.5.4 gestiona TC 1.21.3, que habla la API Docker 1.32, y Docker Engine 29 rechaza clientes < 1.40 → HTTP 400 → los tests se **saltaban en silencio**; fix = importar **`testcontainers-bom` 1.21.4 ANTES** de `spring-boot-dependencies` en el padre (la propiedad `testcontainers.version` no vale con BOM importado) + JaCoCo del padre **0.8.12 → 0.8.15** (Java 25). Decisión de Ismael sobre el punto suelto de D34: **JPA sigue siendo el acceso primario del dominio; DAO + Agente es capa nueva en `iso2-server-app`**, no sustituye a los `Repository`. Puertas en verde: `mvn -B -ntp clean verify` = **356 tests, 0 saltados** (server-app 60→72) con JaCoCo y javadoc estricto OK; los tests de MySQL autodesactivables sin Docker (`disabledWithoutDocker`). **PR pendiente** a `develop`; Ismael debe cerrar **#34** a mano al fusionar y refrendar los TC humanos de CDU16 en la H2 nueva; **#40** creada con el prompt de Iter5/CDU12 | — |

## Deuda / pendientes inmediatos

1. ~~Confirmar decisión D18~~ → D18 **revisada por D31** (08/11): fuentes PlantUML + `.png` renderizados con `tools/plantuml.jar`, ambos versionados e incrustados en los README de `docs/modelo/iterXX/`; la exportación a StarUML queda descartada.
2. ~~Instrucciones de código de Ismael~~ → Iter0/Iter1 ejecutados con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #7 o en el de Iter1.
3. ~~**TC humanos de CDU16**~~ → **Hechos 09/11** (excepción puntual a D28 autorizada por Ismael): ejecutados y
   rellenados por el agente vía HTTP contra `localhost:8080`, **TC16-01…TC16-06 en 16 pasos TC16H-01…TC16H-16,
   todos correctos**, en `docs/tc/cdu16-tc-humanos.md` (PR **#37** → merge `3601fd6`) con el executor anotado en
   cada fila. Queda pendiente que **Ismael los refrende y, si quiere, los repita él** en la H2 local
   (`./data/iso2-server`, conservada con las filas `TC16H-*`). Eran criterio de cierre de Iter1/CFG-01: **CFG-01
   ya está cerrada del todo**. ~~issue #21~~ cerrada como contenedor.
4. ~~Exportar `.staruml` de iter01~~ → **No procede (D31)**: los artefactos de CFG-01 son los `.puml` +
   `.png` de `docs/modelo/iter01/`, ya versionados e incrustados en su README (PR de docs sesión 13).
5. **Kanban + wiki** (#23, cerrada por Ismael): la wiki existe en su repo `PrISO2-25-26.wiki` y ya tiene
   `Home.md` reindexado + una página por iteración cerrada (Iter0..Iter3) con los componentes, sus
   versiones y los diagramas de cada CDU. El GitHub Project (Kanban) no puede verificarse con el token del agente (sin
   scope `read:project`): **revisión visual por Ismael** — si no existe o le falta algo, decir qué y se crea.
6. ~~Tags `cfg-iter00`/`cfg-iter01` y cierre manual de #6/#9~~ → **Hecho 08/11** (ver Estado del repositorio). `v0.1.0` publicado sobre el merge `e165622` del PR #19 y **#20 cerrada a mano**.
7. **TC humanos de CDU17** (D28): plantilla en `docs/tc/cdu17-tc-humanos.md` con matriz RBAC y bloqueo
   prefijados; **Ismael rellena y ejecuta** sobre el código del PR #14 ya fusionado (`870524e` = tag
   `cfg-iter02`). Son criterio de cierre de Iter2/CFG-02.
8. ~~Exportar `.staruml` de iter02~~ → **No procede (D31)**: artefactos de CFG-02 = `.puml` + `.png` de
   `docs/modelo/iter02/`, ya versionados e incrustados.
9. ~~Tag anotado `cfg-iter02`~~ → **Hecho 08/11**: anotado y subido sobre `870524e`.
10. **TC humanos de CDU13** (D28): plantilla en `docs/tc/cdu13-tc-humanos.md` con semilla V1.3.0 y códigos
    de conflicto prefijados; **Ismael rellena y ejecuta** sobre Iter3 fusionado (`b072b34` = tag
    `cfg-iter03`). Son criterio de cierre de Iter3/CFG-03 junto con la frontera ArchUnit (ya verde).
11. ~~Exportar `.staruml` de iter03~~ → **No procede (D31)**: artefactos de CFG-03 = `.puml` + `.png` de
    `docs/modelo/iter03/`, ya versionados e incrustados.
12. ~~Tag anotado `cfg-iter03` + cierre manual de #15~~ → **Hecho 08/11**: tag subido sobre `b072b34` e #15 cerrada a mano (D29).
13. ~~Publicar R0 en `main` (issue #20)~~ → **Hecho 08/11, issue cerrada a mano**: PR #19 fusionado con
    merge commit `e165622` y tag anotado `v0.1.0` subido sobre ese merge. Regla nueva **D30**: `main` solo
    recibe ramas `release/RN`, nunca `feature/…`.
14. ~~Cierre de Iter4 en GitHub~~ → **Hecho 08/11 (sesión 16)**: Ismael fusiona **PR #31** (merge `a3fe109`)
    y **PR #32** (merge `3b02718`); tag anotado **`cfg-iter04` creado sobre `a3fe109`** y subido; rama
    `feature/iter04-cdu06-solicitudes` borrada (remoto y local); página de wiki **`Iter4-CDU06-solicitudes`**
    publicada (merge `5207bae` del repo wiki) con los 4 diagramas, la tabla de componentes con fichas
    Javadoc (D32) y la sección Testing. Issue **#30 ya cerrada** (19:01, D29 por adelantado).
15. **TC humanos de CDU06** (D28): plantilla **creada** en `docs/tc/cdu06-tc-humanos.md` (14 casos
    TC06-01…TC06-14 con la superficie real de la API y las semillas de `V1.4.0__cdu06_solicitudes.sql`,
    `SEM-RX-TORAX` y `SEM-RM-RODILLA`); **Ismael la rellena y ejecuta**. De las cuatro plantillas, **CDU16 ya
    está ejecutada** (nº 3, 09/11); siguen pendientes **CDU17 y CDU13** (nº 7 y nº 10) y estas propias.
16. ~~Publicar el Javadoc en GitHub Pages (issue #27)~~ → **Hecho 08/11, issue cerrada a mano**: D32,
    workflow `javadoc.yml` desde `develop`, URL `…/apidocs/` verificada y wiki enlazando las fichas.
    Sin instantáneas por tag: si una wiki de iteración futura necesita la ficha exacta de esa CFG, se
    enlaza a `develop` (siempre la última compilación).
17. **Nueva desviación de D30 en `main` — decidida y cerrada documentando (08/11, sesión 16):** el PR #29
    fusionó `develop` → `main` (merge `d12388b`, 08/11 19:01) sin rama `release/RN` ni etiqueta, así que `main`
    quedó por delante de `v0.1.0`. Ismael decide **dejarlo y documentarlo**: no se toca la historia de `main` y
    la anomalía se cierra publicando **R1 desde `release/R1` sobre el tag `cfg-iter04`** con su tag `v0.1.1`.
    Registrado en D30. El agente no actúa sobre `main` sin visto bueno.
18. **Método D33 sobre las pruebas — HECHO para Iter1/CDU16, pendiente de extender a Iter2/CDU17 e
    Iter3/CDU13** (~~issue #35~~, cerrada a mano por Ismael; rama `feature/iter01-pruebas-d33` ya borrada tras
    el PR **#36** → merge `f43721c`). Lo ejecutado en CDU16: spec en `docs/test/iter01/tdop-cdu16-espec.json`
    (clases de equivalencia + puntos límite + conjetura de errores), compositora **`tools/pairwise.py`**,
    proyectador a plantilla **`tools/tc_plantillas.py`**, matriz **A33 + B15 + C36 + D9 + E22 = 115** en
    `docs/test/iter01/tdop-cdu16.md` y en `iso2-catalogo/src/test/resources/tdop/`, JUnit con JavaDoc y
    `@DisplayName` con el id del caso, y wiki de la Iter1 con la **clase probada** (`Cdu16MatrizTdopTest`).
    Los huecos detectados se arreglaron con **D35** (`LímitesCatalogo` en el servicio + validación de
    `pagina`/`tamano` + 415) previa edición de los `.puml` de `docs/modelo/iter01/` (D31). **Lo que queda:**
    aplicar el mismo procedimiento a `iso2-seguridad` (CDU17) y `iso2-agendas` (CDU13), con sus plantillas de
    TC humanos regeneradas (`docs/tc/cdu17-tc-humanos.md`, `docs/tc/cdu13-tc-humanos.md`) y sus páginas de wiki.
19. ~~**Nueva capa de persistencia MySQL — D34 aceptada y concretada, implementación SIN EMPEZAR**~~ →
    **Implementada 11/11/2026 (sesión 18)** en `feature/iter05-persistencia-mysql` (commits `b15d927`..`bba56f8`
    + docs): **Docker** con `mysql:8.4` (host **3307**) + **phpMyAdmin** (8081) + volumen `iso2-mysql-datos`;
    base `iso2` con `gestoroperaciones` **solo DML** y `iso2_flyway` con **DDL** para Flyway; **Flyway dual**
    `db/migration/h2` + `db/migration/mysql` por perfil; **patrón DAO** y **patrón Agente** (`AgenteH2`/
    `AgenteMySQL`) como **capa nueva** en `iso2-server-app` (JPA sigue siendo el acceso primario del dominio,
    decidió Ismael); **Testcontainers con `mysql:8.4`** en la puerta (3 tests) y reactor verde **356 tests,
    0 saltados**. Queda de Ismael: **fusionar el PR** a `develop` y **cerrar #34 a mano** (D29). El aviso de
    seguimiento (cierre de #34 del 09/11 sin trabajo detrás) quedó zanjado reabriendo la issue el 11/11.

## Punto exacto de continuación para la próxima sesión

**Arrancar desde `develop` = `737b932`** (tip tras el merge del PR #38; CI `construir` en verde). La **D34 está
implementada** (Deuda nº 19): la rama `feature/iter05-persistencia-mysql` tiene sus commits listos y **su PR a
`develop` espera fusión de Ismael**, que al fusionar cierra **#34** a mano (D29). En GitHub quedan abiertos:
ese PR de D34, el **PR #39** (`opencode.json`) y la issue **#40** (prompt ejecutable de Iter5/CDU12). La
**Iter1 · CDU16 / CFG-01 quedó cerrada del todo** el 09/11 — puertas técnicas + método **D33** + **TC humanos**
+ issue cerrada a mano + wiki rehecha — y ese es el ciclo completo que hay que replicar en CFG-02/03/04/05.
Todo lo demás que había en esta sección es historia ya recogida en el «Registro de sesiones»; aquí solo queda
lo vivo.

### 0) Arranque en el equipo nuevo (10/11/2026)

A partir del 10/11 se trabaja en otra máquina y en otra ubicación, **partiendo de un clon nuevo**. Nada vive ya
en esta máquina salvo lo que está en GitHub, así que el arranque es:

1. ~~Fusionar el **PR #38** y clonar~~ → **Hecho 11/11**: `develop` = `737b932` (merge del PR #38) y el equipo
   nuevo trabaja sobre el clon `C:\ProgIA\PrISO2-2025-2026`. Los tags (`cfg-iter00`…`cfg-iter04`,
   `cfg-iter01` reanotado y `v0.1.0`) llegaron con el clon; la wiki es el repo aparte
   `PrISO2-25-26.wiki.git`, rama `master` (D32).
2. ~~Identidad de git y autenticación~~ → **Hecho 11/11**: `user.name` = `IsmaelCaballero`,
   `user.email` = `IsmaelCaballero@users.noreply.github.com` y `gh auth login` en regla (el agente abre issues
   con `gh`, D29; los cierres son de Ismael).
3. ~~Herramientas~~ → **Comprobado 11/11**: JDK 25 (vale: `maven.compiler.release` es 21), Maven 3.9.x,
   Python 3.12 para `tools/pairwise.py` y `tools/tc_plantillas.py`, y **Docker Desktop** (Engine 29), que ya no
   solo es útil para la D34: los tests Testcontainers de MySQL corren en `mvn verify` (sin Docker se
   autodesactivan, pero la puerta D34 exige Docker para contarlos). Ojo al acople
   Testcontainers↔Docker Engine 29 documentado en D34: el `testcontainers-bom` del padre debe ir **antes** de
   `spring-boot-dependencies`.
4. Primera compilación: `mvn -B verify` en la raíz. El `~/.m2` del equipo nuevo está vacío, así que la primera
   ejecución baja todas las dependencias (Maven Central, sin repositorio privado ni `settings.xml`). Antes de
   compilar un módulo suelto con `-pl` sin `-am`, lanzar una vez `mvn -B install -DskipTests` para que existan
   en el repositorio local los artefactos `es.uclm.iso2:*`.
5. **La base de datos H2 no viaja** (`data/` está en `.gitignore`): en la máquina nueva se regenera vacía con
   Flyway y las semillas al arrancar el servidor. Los datos de los TC humanos de CDU16 (filas `TC16H-*` y los
   5 asientos de auditoría) se quedan en esta máquina; su única evidencia ya son
   `docs/tc/cdu16-tc-humanos.md` y la wiki. Recomendado: no copiar el `.zip` de `./data/iso2-server` y empezar
   limpio; si se quieren refrendar los TC, repetirlos contra la H2 nueva.

### 1) Antes de programar nada (acciones de Ismael, D29)

1. ~~**Reabrir la issue #34 o abrir una nueva para D34**~~ → **Hecho 11/11**: #34 reabierta con el comentario de
   la comprobación; D34 ya está implementada sobre ella (Deuda nº 19). Queda de Ismael **cerrarla a mano** al
   fusionar su PR (D29).
2. ~~**Abrir la issue de Iter5/CDU12 (CFG-05)**~~ → **Hecho 11/11**: issue **#40** con el prompt ejecutable en
   el cuerpo.
3. ~~Decidir si **se borra la rama `feature/iter01-cdu16-catalogo`**~~ → **Hecho 11/11**: borrada (remoto y
   local); era la única suelta y su commit `19dff72` era una nota sustituida por este fichero.
4. **Refrendar los TC humanos de CDU16** que ejecutó el agente el 09/11 (excepción puntual a D28 anotada en el
   doc) y, si quiere, repetirlos él mismo. A partir del 10/11 eso se hace contra la H2 nueva del otro equipo:
   las filas `TC16H-*` y los 5 asientos de auditoría se quedan en esta máquina (apartado 0, punto 5).

### 2) Siguiente iteración: Iter5 · CDU12 citas (CFG-05)

**Primero: Ismael fusiona el PR de la D34** (y cierra #34 a mano, D29; fusiona también el PR #39 si le vale).
La rama `feature/iter05-cdu12-citas` se abre **desde `develop` ya con la D34 dentro**, con `Ref: #40` en cada
commit. Versiones: las apps **ya están en 0.1.5-SNAPSHOT** (la D34 subió el tren de la release en esta rama) y
`iso2-citas` pasa a **1.1.0**; la migración Flyway **`V1.5.0__…` hay que escribirla en las dos carpetas**,
`db/migration/h2/` **y** `db/migration/mysql/` (D34 dual: si solo se pone en `h2/`, el perfil `mysql` se queda
en V1.4.0 y los tests Testcontainers fallan). Modelo en `docs/modelo/iter05/` —que ya tiene el diagrama de la
capa de persistencia— añadiendo los **4 `.puml` + `.png` de CDU12** incrustados en su README (D31) **antes de
escribir código** (flujo diseño-primero, convención desde Iter4). Puertas de CFG-05 en
`docs/mapa-configuraciones.csv`: **concurrencia de hueco** (doble reserva sobre el mismo hueco → una sola cita,
prueba con dos hilos), **rollback transaccional** y **máquina de estados** de la cita, con cobertura JaCoCo
**≥ 80 %** en `iso2-citas`. Fecha objetivo **20/11/2026**. Orden de lectura al llegar: CONTEXT.md →
DECISIONS.md (D28-D35) → STATUS.md → PLAN.md §4 → matriz CFG-05. Después de Iter5 queda **Iter6 · CDU19
integración (CFG-06)** con el hito **`v0.2.0-alpha`**.

**La D34 ya está ejecutada** (Deuda nº 19 y registro sesión 18), así que Iter5 parte de: MySQL 8.4 en Docker
(host **3307**, `docker compose up -d` desde `docker/`), Flyway dual por perfil con usuario DDL `iso2_flyway` y
usuario de aplicación `gestoroperaciones` (solo DML), y la capa DAO/Agente en `es.uclm.iso2.server.persistencia`
como **capa nueva de `iso2-server-app`**: **JPA/Spring Data sigue siendo el acceso primario del dominio** (decisión
de Ismael del 11/11 que zanjaba el último punto suelto de D34), y CDU12 usará Repositorios JPA como el resto del
dominio. Los tests Testcontainers (`mysql:8.4`) ya corren dentro del reactor; sin Docker se autodesactivan.

### 3) Pendientes heredados que no cerró ninguna iteración

- **TC humanos de CDU17, CDU13 y CDU06** (Deuda nº 7, nº 10 y nº 15): plantillas ya creadas en `docs/tc/`;
  los rellena y ejecuta Ismael (D28). Son criterio de cierre de CFG-02/CFG-03/CFG-04.
- **Extender el método D33 a Iter2 (`iso2-seguridad`) e Iter3 (`iso2-agendas`)** (Deuda nº 18): repetir el
  procedimiento de CDU16 (spec JSON en `docs/test/iterNN/` → `python tools/pairwise.py` →
  `python tools/tc_plantillas.py` → JUnit en verde → wiki con la **clase probada**) y regenerar sus plantillas
  de TC dentro de `<!-- TDOP:BEGIN -->`/`<!-- TDOP:END -->`; los TC escritos a mano van fuera de ese bloque.
- **Publicar R1** desde `release/R1` sobre el tag `cfg-iter04` con etiqueta **`v0.1.1`**, para cerrar la
  desviación de D30 que dejó el PR #29 en `main` (la historia de `main` no se toca y el agente no la pisa).
- Revisión visual del **Kanban** (GitHub Project, #23): el token del agente no tiene scope `read:project`.

### 4) Reglas vivas y datos útiles

**D28** (los TC humanos son de Ismael; el agente genera los casos y las plantillas con D33 pero no los
ejecuta — la del 09/11 fue una excepción autorizada y queda anotada en el doc y en este fichero), **D29** (las
issues las abre y las cierra Ismael), **D30** (el agente no fusiona PRs ni toca `main`; sí tiene autorizado
pushear a `master` del repo wiki), **D31** (modelado PlantUML: `.puml` + `.png` versionados, sin StarUML),
**D32** (Javadoc publicado en Pages desde `develop`, `…/apidocs/`), **D33** (clases de equivalencia + teorema
del valor límite + conjetura de errores compuestos con **pairwise**, los mismos casos en JUnit y en los TC;
las tablas TDOP **solo** se generan con `python tools/pairwise.py` y `python tools/tc_plantillas.py`), **D34**
(MySQL en Docker + DAO + Agente, **implementada 11/11/2026**: JPA sigue siendo el acceso primario del dominio y
la capa DAO/Agente es nueva en `iso2-server-app`), **D35** (las cotas del contrato se validan en el servicio →
400, nunca contra la BD → 500) y el **flujo diseño-primero** (editar el `.puml` antes que el código cuando
cambia un invariante).

Datos para volver a ejecutar TC por HTTP: `mvn spring-boot:run -pl iso2-server-app` deja el servidor en
`http://localhost:8080` con H2 en `./data`; login con campo **`password`** y semillas `admin/iso2-admin`,
`medico/iso2-medico`, `tecnico/iso2-tecnico`, `recepcionista/iso2-rec` y `celador/iso2-ses`; 401 →
`AUTENTICACION_REQUERIDA`, 403 → `FORBIDDEN`. En el equipo nuevo `./data` se regenera vacía con Flyway y las
semillas (las filas `TC16H-*` de los TC de CDU16 se quedaron en la máquina antigua; ver §0 punto 5).

**Datos D34 (persistencia):** `docker compose up -d` desde `docker/` levanta **`mysql:8.4` en el host 3307**
(phpMyAdmin en **8081**, volumen `iso2-mysql-datos`; `down -v` borra los datos). Para correr el servidor
contra MySQL: `$env:ISO2_JWT_SECRET="…"; mvn -pl iso2-server-app spring-boot:run
"-Dspring-boot.run.profiles=mysql"` — la aplicación usa `gestoroperaciones` (solo DML) y Flyway `iso2_flyway`
(DDL), con `ISO2_MYSQL_HOST/ISO2_MYSQL_PORT/ISO2_DB_USER/ISO2_DB_PASSWORD/ISO2_FLYWAY_USER/ISO2_FLYWAY_PASSWORD`
como válvulas de escape. Diagnóstico en vivo: `GET /api/persistencia/diagnostico` con token `admin` (solo
ADMINISTRADOR) devuelve SGBD, esquema y conteos por tabla.

**Cierre de jornada 09/11/2026 (fin de la sesión 17):** **Iter0-Iter4 fusionadas y etiquetadas** — 156 h de
las 612 h con IA del plan ≈ **25,5 % del esfuerzo** (5 de 22 iteraciones) — y **cerradas del todo** (con TC
humanos e issue cerrada a mano) solo **Iter0/CFG-00 e Iter1/CFG-01**: 62 h ≈ **10,1 %**. `develop` =
`3601fd6`, tag `cfg-iter01` reanotado sobre `df09bca`, wiki al día con la Iter1 rehecha con D33, 12 ramas
fusionadas borradas y repo limpio para arrancar la D34 y la Iter5.

**Cierre de jornada 11/11/2026 (fin de la sesión 18):** **D34 implementada** — MySQL 8.4 + phpMyAdmin en
Docker (host 3307/8081), Flyway dual `h2`/`mysql` por perfil con usuarios DDL/DML separados, capa **DAO +
Agente** nueva en `iso2-server-app` (JPA sigue siendo el acceso primario del dominio) y puerta Testcontainers
contra `mysql:8.4` real: `mvn -B -ntp clean verify` = **356 tests, 0 saltados** con JaCoCo y javadoc estricto
en verde. `develop` = `737b932`; el PR de la D34 espera fusión de Ismael (al fusionar cierra **#34** a mano y
aprovecha para fusionar **#39**), **#40** abierta con el prompt de Iter5/CDU12 (objetivo 20/11). Siguen
pendientes la extensión del método D33 a Iter2/CDU17 e Iter3/CDU13 y los TC humanos de CDU17/13/06.


**Cierre de jornada 11/11/2026 (fin de la sesión 19, Iter5/CDU12 en curso):** la rama `feature/iter05-cdu12-citas` queda **pusheada** sobre `develop` = `6feabdf` con 5 commits: `a7b88e8` (los 4 `.puml` + `.png` de CDU12 antes de escribir código, D31), `0a58892` (re-renderizado del PNG de CDU06 que PlantUML recortaba), `107e1d4` (`CitaFacade` 1.1.0 con estado y hueco + estado `CITADA` en `iso2-solicitudes` + migraciones `V1.5.0` H2 y MySQL + poms `0.1.5-SNAPSHOT` y JaCoCo 0,80 en `iso2-citas`), `3aee742` (el modelo fija "sin agenda = `FUERA_DE_FRANJA`" y "solicitud apta solo en creada") y `99696e5` (implementación completa de `iso2-citas`: dominio JPA `cita`/`cita_hueco`/`cita_auditoria`, repositorios, `CitaServicio` con anti-concurrencia y auditoría encadenada, `CitaControlador` + `CitaManejadorErrores`, y el andamiaje de pruebas del módulo).

* Comprobado **ejecutando**: Flyway aplica las 5 migraciones con `V1.5.0` y `iso2-server-app-0.1.5-SNAPSHOT.jar` arranca; `POST /api/auth/login` con `admin/iso2-admin` devuelve token; `GET /api/citas` sigue respondiendo **401 `AUTENTICACION_REQUERIDA`** incluso con token válido porque el RBAC de citas **aún no está cableado** - es el siguiente trabajo, no un fallo.
* Pendiente para reanudar, en este orden: (1) pruebas del módulo - `CitaServicioTest`, `ConcurrenciaCitasTest` (2 hilos reales sobre el mismo hueco, más los TC de rollback de cita + hueco + auditoría), `MaquinaEstadosCitasTest`, `CitaControladorTest` y `ArquitecturaCitasTest`; (2) `mvn -B -ntp -pl iso2-citas verify` hasta que pase la puerta JaCoCo de línea (0,80); (3) RBAC `cita:leer` / `cita:escribir` en `Permiso` + `MatrizPermisos` + `ConfigSeguridad` y `CitasApiTest` en `iso2-server-app`, y las vistas de recepcionista del `iso2-client-app` (`ApiClienteCitas`, `VistaGestionCitas`, `VistaConsultaCitas` y sus predicates en `NavegacionPorRol`) con `VERSION` a `0.1.5-SNAPSHOT`; (4) método D33 de CDU12 - spec en `docs/test/iter05/tdop-cdu12-espec.json`, CSVs generados en `iso2-citas/src/test/resources/tdop/`, bloque gestionado de `docs/tc/cdu12-tc-humanos.md` y `Cdu12MatrizTdopTest`, lo que exige dos ajustes genéricos en `tools/tc_plantillas.py` (derivar el paquete `tdop` del módulo desde la spec y admitir un mapa `operaciones` por CDU); (5) página de wiki `Iter5-CDU12-citas.md` nombrando la clase probada; (6) `mvn -B -ntp clean verify`, PR a `develop` con `Ref: #40` sin `Closes`, tag `cfg-iter05` y cierre manual de **#40** (D29).
* Decisión nueva registrada como **PROPUESTA** y a la espera del visto bueno de Ismael: **D36** (arriba), que es la que fija cómo se cierra el duelo de concurrencia y qué códigos de error salen.
* Datos para el equipo nuevo: `tools/plantuml.jar` **no** está versionado (`*.jar` en `.gitignore`) y hay que volver a bajarlo para re-renderizar, siempre con `java "-DPLANTUML_LIMIT_SIZE=16384" -jar tools/plantuml.jar -tpng -Playout=smetana` porque el límite por defecto (4096 px) recorta los PNG en silencio. El H2 en fichero de `./data` y el `ISO2_JWT_SECRET` del arrancado suelto del jar son desechables: en la máquina nueva `./data` se regenera con Flyway y sus semillas.


* **CI del push `e44f7c1` (run 43044676645) en rojo por una sola causa, ya diagnosticada**: la puerta JaCoCo de `iso2-citas` (`lines covered ratio is 0.00, but expected minimum is 0.80`) porque el módulo todavía solo tiene el smoke del contrato. Compilación, Javadoc estricto y los otros 13 módulos van en verde. Está dicho también en un comentario de la PR #42.
* **El issue #40 amaneció cerrado** (10:06 UTC del 11/11, sin PR enlazada) cuando CFG-05 está a mitad de alcance. Reabierto con el visto bueno de Ismael y con un comentario que enlaza la PR #42 y resume el punto de continuación, para que siga siendo el ancla del iter.
* **Estado de la máquina al cerrar**: árbol de trabajo limpio, `HEAD` = `origin/feature/iter05-cdu12-citas` = `e44f7c1`, PR **#42** abierta como borrador hacia `develop`, issue **#40** abierto, ningún proceso `java` suelto ni el 8080 ocupado. `data/iso2-server.mv.db` es el H2 de la prueba de arranque: desechable y fuera de Git.
