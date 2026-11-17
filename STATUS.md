# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-15 (sesión 24 · **Iter6 · CDU19 / CFG-06 CERRADA por el agente, por orden
de Ismael**: «iter06/CFG-06 cerrado. Haz tú lo que queda»). Ya está **fusionado el PR #45**
(`feature/iter06-cdu19-integracion` → `develop`, merge **`0ef4f1a`**, check `construir` en verde) y su rama
esta **borrada** en remoto y en local. Los **TC humanos de CDU19 se han ejecutado contra el servidor real**
(puerto 8080, H2 en fichero) y su **evidencia HTTP esta versionada** en `docs/tc/evidencia/cdu19/` — 5 scripts
repetibles y 5 registros JSON con los JWT redactados—; la tabla manual de
`docs/tc/cdu19-tc-humanos.md` esta **rellena** (9 filas TC19-01..09) con su seccion de ejecucion real, y el
bloque `<!-- TDOP:BEGIN/END -->` sigue intacto (regenerar es no-op). **D37 revalidado y corregido**:
`TASADEFECTOS.md` trae la fila de Iter6 (**22 commits · 0 F1 · 1 F2 · 0 F3 · 1 F4 · 2 totales · 9,1/100**) con
**DEF-14** (el `@ConfigurationProperties` ausente, nacido en `7a03e0c` y arreglado en `f5411c1`, misma rama) y
**DEF-15** (el vaciado de `STATUS.md`/`PROMPTS.md`, F2, el dano nunca llego al repo); los hashes `00c6194`
que figuraban eran incorrectos y se han sustituido. Queda pendiente de fusion el **PR de cierre** de esta
misma rama `docs/iter06-cierre` → `develop`, y sobre su fusion ira el **tag anotado `cfg-iter06`**; la
**issue #44 se cierra con la evidencia detras** y aqui queda constancia de que los TC humanos los ha ejecutado
el agente, **sin sustituir la validacion D28 de Ismael**. Registro cronologico en `PROMPTS.md` (Prompts 17 a 24).

**Sesion 23 (15/11, tarde), conservada:** **Iter6 parte b TERMINADA — wiki incluida**: matriz TDOP ejecutada
y en verde, TC humanos proyectados, informe de spike, evidencia D33 y **pagina de wiki
`Iter6-CDU19-integracion` publicada** (`9b7f513` en `master` del repo wiki). La sesion 21 quedo cortada por un
reset del equipo y su registro local se perdio, pero **todo su trabajo estaba subido**; **Iter5 / CFG-05 cerro
del todo** con el **PR #43 → merge `cbb1901`** en `develop` y el tag anotado **`cfg-iter05`**.
**Iter6 · CDU19 (CFG-06)** fue en la rama `feature/iter06-cdu19-integracion` sobre la **issue #44**: modelo
PlantUML (D31) + módulo `iso2-integracion` 1.1.0 con `InteropFacade` y adaptadores simulados + **203 pruebas en
el módulo** (10 clases, con el contrato del baseline 1.0.0 blindado), de ellas **126 de la matriz TDOP
`Cdu19MatrizTdopTest`, en verde** (la spec creció a **126 casos / 8 tablas A-H** al corregir la E10). Reactor
revalidado sobre `2388b7e`: **BUILD SUCCESS, 839 tests, 0 fallos, 0 errores, 0 omitidos** y 13 puertas JaCoCo
verdes; `iso2-integracion` en **JaCoCo 96,5 % de líneas (361/374) y 78,4 % de ramas**, puerta `0.70` superada.
Aviso metodológico: **el CSV de JaCoCo solo lo escribe la fase `verify`**; leerlo tras un `mvn test` da cifras
caducadas (de ahí salía el 88,2 % que venía repitiendo este fichero). Así cerró la sesión 23: la wiki
**`Iter6-CDU19-integracion` quedó publicada** y el **PR borrador quedó abierto como #45** a `develop`; a la CDU19
le faltaban entonces los **TC humanos (D28)** y la **fusión con su tag (D29/D30)**, y la rama
`feature/iter06-cdu19-integracion` estaba **pusheada hasta el último docs de esa sesión**.

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔ **Completada, etiquetada y publicada** — PR #7 fusionado a `develop` (`641e72d`), tag `cfg-iter00`; **R0 publicada en `main`**: PR #19 fusionado (merge `e165622`) con el tag anotado **`v0.1.0`** (regla **D30**: release desde `release/R0`) |
| Elaboración (Iter1–6) | 🟡 En curso — **Iter5 (CDU12 / CFG-05) CERRADA EL 15/11**: código fusionado por Ismael con el **PR #42** (merge `08a7ed8`) y matriz D33 de 172 casos con el **PR #43** (merge `cbb1901`), **tip actual de `develop` = `0ef4f1a`** tras el **PR #45** de Iter6. tag anotado **`cfg-iter05` → `cbb1901`** y **#40 cerrada a mano** (D29). **Iter6 (CDU19 / CFG-06) CERRADA EL 15/11 (sesión 24, cierre asumido por el agente por orden de Ismael)**: código en `feature/iter06-cdu19-integracion` (issue **#44**) **fusionado por el agente con el PR #45** (merge **`0ef4f1a`**, check `construir` en verde, rama **borrada** en remoto y local): modelo + módulo `iso2-integracion` + **203 pruebas (10 clases)** con la **matriz TDOP de 126 casos en verde** (`Cdu19MatrizTdopTest`, spec de 8 tablas A-H subida en `c104d78`), **TC humanos proyectados y ejecutados** (`docs/tc/cdu19-tc-humanos.md` + evidencia HTTP en `docs/tc/evidencia/cdu19/`), **informe de spike** (`docs/informes/spike-cdu19.md`, en `2388b7e`) y **reactor revalidado en 839 tests**; la wiki **`Iter6-CDU19-integracion` publicada** (`9b7f513` en `master` del repo wiki). Lo que cierra la sesión 24: **evidencia HTTP versionada**, **tabla manual de TC humanos rellena**, **D37 revalidado con DEF-14/DEF-15** y **PR de cierre** de `docs/iter06-cierre` → `develop` con el **tag `cfg-iter06` sobre su fusión** y la **#44 cerrada con artefacto detrás**. Ojo: los **TC humanos de CDU19 los ejecutó el agente**, así que la **validación D28 de Ismael sigue pendiente** en esa CDU. **Iter1 (CDU16 / CFG-01) CERRADA DEL TODO el 09/11**: puertas técnicas (tag `cfg-iter01`, reanotado sobre `df09bca`), **D33 aplicado** (PR #36 → merge `f43721c`) y **TC humanos de CDU16 hechos** (PR #37 → merge `3601fd6`). Iter2 (CDU17) y Iter3 (CDU13) cerradas en ramas y tagueadas (`cfg-iter02` → `870524e`, `cfg-iter03` → `b072b34`) pero **aún sin TC humanos**; Iter4 (CDU06) fusionada (PR #31 → merge `a3fe109`, tag `cfg-iter04`) y **sin TC humanos** (plantilla `docs/tc/cdu06-tc-humanos.md` creada, 14 casos). Lo que queda vivo: los **TC humanos de CDU12/CDU17/CDU13/CDU06** —y la **validación D28 de los de CDU19**, que los ejecutó el agente— y **extender el método D33 a Iter2/Iter3**. Las evidencias de modelado quedan resueltas con PlantUML (`.puml` + `.png` versionados, D31; StarUML descartado) |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Avance cuantificado (15/11/2026), sobre el plan de `docs/planificacion-iteraciones+Calidad.csv`:** el total
planificado son **612 h con IA** repartidas en 22 iteraciones (Iter0 40 h; Elaboración Iter1-6 188 h;
Construcción Iter7-20 304 h; Transición Iter21 80 h). **Fusionadas y tagueadas van Iter0-Iter6 = 228 h ≈ 37,3 %
del esfuerzo** (7 de 22 iteraciones, con los tags `cfg-iter00`..`cfg-iter05` puestos y **`cfg-iter06` sobre la
fusión de cierre de esta sesión**). **Cerradas del todo, con TC humanos hechos e issue cerrada, Iter0 (CFG-00),
Iter1 (CFG-01) e Iter6 (CFG-06) = 98 h ≈ 16,0 %**, con el matiz de que **los TC humanos de CDU19 los ejecutó el
agente** y a Ismael le queda la validación D28; a Iter2/3/4/5 les siguen faltando los TC humanos (D28). En
calendario vamos por delante: el plan ponía Iter4-6 del 16/11 al 20/11 y la Iter6 ya estaba fusionada el 15/11.

**Iteración cerrada (y siguiente):** **Iter6 · CDU19 integrar con sistemas externos (CFG-06) — CERRADA el
15/11 en la sesión 24**. La issue **#44** (abierta por Ismael el 15/11) fijaba el alcance: spike de viabilidad del
SSO y de la sincronización de historias en el módulo **`iso2-integracion` 1.1.0**, con el puerto
`InteropFacade` y adaptadores simulados (CDU del dominio **sin tocar**), `server-app` **0.2.0-SNAPSHOT** y
el hito **`v0.2.0-alpha` (R1 ALFA, 20/11)**. Lo que quedó en `feature/iter06-cdu19-integracion`
(commits `db075c0..2388b7e` sobre `cbb1901`, **hoy fusionados en `0ef4f1a`**): **modelo PlantUML D31** (`docs/modelo/iter06/`, 4 diagramas con
sus `.png`), **puerto + adaptadores + DTOs + auditoría encadenada en memoria**, `SimulacionControlador` con
`SetupSimulacion` (token `SIM-*`, **sin persistencia**: no hay slot Flyway que llenar) — ojo: el RBAC real es
**solo rol `ADMINISTRADOR`** en los 7 endpoints, la pareja `INTEGRACION_LEER/ESCRIBIR` que anunciaba la issue
no existe — y **203 pruebas en el módulo** repartidas en 10 clases: `AdaptadorSimuladoTest` 15,
`IntegracionServicioTest` 20, `IntegracionControladorTest` 13, `SimulacionControladorTest` 8,
`ArquitecturaIntegracionTest` 6, `AuditoriaIntegracionTest` 6, `SetupSimulacionTest` 4,
`RegistroSimulacionTest` 4, `ContratoBaselineTest` 1 (fija que el contrato de `iso2-integracion` 1.0.0 del
baseline no se rompe) y la nueva **`Cdu19MatrizTdopTest` con los 126 casos de la matriz D33 leídos de CSV**.
Puertas medidas sobre `2388b7e` con `mvn -B -ntp verify`: **BUILD SUCCESS con 839 tests, 0 fallos, 0 errores,
0 omitidos** (203 de ellos de CDU19) y `iso2-integracion` en **JaCoCo 96,5 % de líneas (361/374) y 78,4 % de
ramas**, por encima de la cota del módulo (70 %). El **informe del spike ya está escrito** en
`docs/informes/spike-cdu19.md`: veredicto de viabilidad por integración, decisión **FHIR/JSON sobre REST**
(descartado HL7v2/MLLP y descartado escribir un visor DICOM propio en Iter16, con DICOMweb como contrato),
**15 hallazgos** del spike con su evidencia en la matriz y **deuda D-01..D-07** para Iter7 (hoy ya **D-01..D-10**,
ver el apartado de Deuda). **Cierre material hecho por el agente en la sesión 24:** **PR #45 fusionado** (merge
`0ef4f1a`), **rama `feature/iter06-cdu19-integracion` borrada**, **TC humanos de CDU19 ejecutados contra el
servidor real** con su evidencia en `docs/tc/evidencia/cdu19/`, **tabla manual rellena** en
`docs/tc/cdu19-tc-humanos.md`, **D37 revalidado** en `TASADEFECTOS.md` y **PR de cierre** de `docs/iter06-cierre`
→ `develop` con el **tag `cfg-iter06` sobre su fusión** y la **#44 cerrada con artefacto detrás**. **Iter7 ·
CDU14 listas de espera (CFG-07)** es la siguiente iteración (inversión **D23**, `iso2-citas` **1.2.0**, fila 7 de
`docs/planificacion-iteraciones+Calidad.csv`: calidad **Adec. Funcional (corrección) · Usabilidad (operabilidad
y protección) · Seguridad (no repudio y responsabilidad)**, puerta **reordenación determinista probada +
auditoría de reubicaciones**, TC sugeridos: **comparador de reordenación con tabla de casos, doble clic no
duplica hueco, traza de reubicación con autor y motivo**). **Iter5 · CDU12 (CFG-05)
queda CERRADA**: código por **PR #42** (`08a7ed8`), matriz D33 y docs por **PR #43** (merge `cbb1901`,
15/11 08:52, **tip de `develop`**), **tag `cfg-iter05` → `cbb1901`** y **#40 cerrada a mano**; solo les siguen
faltando los **TC humanos de CDU12** (D28, pendiente 20). Antes, el 09/11 (sesión 17), se cerró **del todo la
Iter1 · CDU16 / CFG-01**, que es la primera que completa el ciclo entero: diseño → código → puertas técnicas →
**D33** → **TC humanos** → issue cerrada a mano → wiki al día, y en la sesión 16 se cerró **Iter4 · CDU06
(CFG-04)** (PR #31 → merge `a3fe109`, tag `cfg-iter04`, wiki y **#30 cerrada a mano** por Ismael).

**Dos cambios de enfoque pedidos por Ismael el 08/11:** (1) el **método D33** de pruebas — clases de
equivalencia + teorema del valor límite + conjetura de errores compuestos con **pairwise**, ejecutados con
**JUnit/Maven** y proyectados tales cuales a las **plantillas de TC humanos**, con JavaDoc en los ficheros de
test y la **clase probada** identificada en la wiki; **ya aplicado a la Iter1/CDU16 y a la Iter5/CDU12**
(evidencia en `docs/test/iter01/` y `docs/test/iter05/`), pendiente de extender a Iter2/CDU17 e Iter3/CDU13. (2) La **capa de persistencia MySQL**
con Docker (MySQL + phpMyAdmin) y los patrones **DAO** y **Agente** (`AgenteMySQL`) — **D34 aceptada y
redactada**, con los cinco puntos ya decididos por Ismael (Testcontainers con MySQL 8, Flyway dual
`db/migration/h2` + `db/migration/mysql`, `gestoroperaciones` solo DML con usuario DDL aparte para Flyway,
`com.mysql:mysql-connector-j` sobre MySQL 8.4 con volumen, y rama propia `feature/iter05-persistencia-mysql`) y
**la implementación ya fusionada a `develop` el 11/11** (rama `feature/iter05-persistencia-mysql`, PR #41 →
merge `d4749e3`, issue **#34** cerrada). Iter2 e Iter3 cerradas también con sus tags (`cfg-iter02` →
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
- **Open / en curso (15/11, sesión 23):** al empezar la sesión no había **ningún PR abierto** (el de cierre de CFG-05,
  **PR #43**, se fusionó el 15/11 08:52 → `cbb1901`). Al cerrarla sí lo hay: **PR borrador #45** de la Iter6
  (`feature/iter06-cdu19-integracion` → `develop`, con `Ref: #44` y **sin `Closes`**): lo fusiona y cierra Ismael
  (D29). **Issue abierta: la #44** (Iter6 · CDU19 / CFG-06), abierta por Ismael el 15/11 y
  **en curso** en `feature/iter06-cdu19-integracion`; es la que lleva el DoD de esta iteración y solo Ismael la
  cerrará (D29). Histórico de la sesión 17: **#35** (pruebas D33) la cerró Ismael a mano el **09/11 00:26** antes
  de que llegara el trabajo (PRs **#36** y **#37**) y **no se reabre**; **#34** (persistencia D34) se cerró **a la
  vez sin trabajo detrás**, se reabrió el 11/11 y **quedó implementada y cerrada** esa misma semana (PR #41 →
  merge `d4749e3`).
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
  `cfg-iter02` → `870524e`, `cfg-iter03` → `b072b34`, `cfg-iter04` → `a3fe109`,
  **`cfg-iter05` → `cbb1901`** (los seis anotados y subidos) y la etiqueta de release **`v0.1.0` → `e165622`**
  (merge del PR #19 en `main`, anotada y subida). El tag `cfg-iter05` es el objeto anotado `005476e`, puesto el
  15/11 (sesión 22) sobre la fusión del **PR #43** siguiendo la receta de `docs/mapa-configuraciones.md` y el
  formato del mensaje de `cfg-iter04`: fija el cierre de CFG-05 (código + matriz D33 de 172 casos + docs),
  recuerda que **no hay release en `main` para la Iter5** (D30) y que los **TC humanos de CDU12 siguen
  pendientes** (D28).
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
- **Estado de ramas e instantáneas a 15/11 (sesión 23):** `origin/develop` = **`cbb1901`** (merge del PR #43),
  `origin/main` = `d12388b` (la desviación ya documentada en D30, sigue sin tocar), `origin/docs/config-opencode`
  = `2a2f0a8` y `origin/feature/iter06-cdu19-integracion` = **el último docs de la sesión 23** (tras `a470584`
  van `e1d418c` del sync del PR #45, `6606d81` de la wiki, `a2fc906` del forense de ramas y el docs de la causa del
  wipe), con el árbol local idéntico
  al remoto y **nada sin subir** (**18** commits de Iter6 sobre `cbb1901`: `db075c0` modelo, `687c624` contrato
  `InteropFacade`, `7a03e0c` módulo `iso2-integracion`, `9ca712f` refino del diseño, `a22e654` RBAC y ensamblado,
  `f5411c1` 77 pruebas y arreglo del `@ConfigurationProperties`, `a81b85b` spec TDOP, `a86d1f4` + `a532992` registro de
  la sesión 22, `c104d78` matriz verde, `ec7f228` evidencia + TC humanos, `86f68ff` fixes de docs, `2388b7e` informe
  del spike, `a470584` + `e1d418c` + `6606d81` cierre documental de la sesión 23, `a2fc906` el forense y el docs de la
  causa del wipe). Tags en el
  remoto: `cfg-iter00`…`cfg-iter05` y `v0.1.0`. La rama `feature/iter05-cdu12-citas` **ya no existe**: el remoto la
  borró el **15/11 a las 08:52** (`DeleteEvent`, justo al
  fusionar el PR #43) y en local tampoco está; sus dos commits del cierre, `b912554` y `5771a38`, están contenidos en
  `develop`. [Corregido en la sesión 23: aquí ponía «sigue viva en remoto con `b912554` y `9457972`», y los dos datos
  eran falsos —la rama llevaba borrada esa misma mañana y **`9457972` no es un commit de este repo**: `git cat-file`
  lo rechaza y la API de GitHub lo devuelve 422—. El dato venía de un `git branch -r` sin `fetch` previo.] **PR
  borrador de la Iter6 abierto: #45**
  (`feature/iter06-cdu19-integracion` → `develop`, 91 ficheros, `Ref: #44` y **sin `Closes`** → lo fusiona y cierra
  Ismael, D29). **Repo wiki** `PrISO2-25-26.wiki`, rama `master` = **`8aa524b`**: página nueva
  **`Iter6-CDU19-integracion`** + sus 4 PNG en `images/iter06-cdu19-*` y `Home.md` con la fila de Iter6
  (publicada en **`9b7f513`** y corregida en **`8aa524b`**, que quita de la cabecera el conteo de commits que
  envejece).
- **Incidencia 1 — reinicio del equipo a media sesión 21 (15/11):** el apagón dejó vacío el almacenamiento
  local de OpenCode (`%USERPROFILE%\.local\share\opencode\storage`), así que **se perdió el historial de la
  conversación pero no el trabajo**, que estaba subido. La sesión 22 se reconstruyó leyendo `git reflog`,
  `STATUS.md` y GitHub, y ahí apareció que Ismael ya había **fusionado el PR #43 y cerrado #40**: solo quedaba
  el tag `cfg-iter05` y este sync. Regla aplicada desde entonces: **nada se queda sin subir** y el registro son
  `STATUS.md` + `PROMPTS.md`, no la sesión de la herramienta.
- **Incidencia 2 — dos agentes escribiendo a la vez sobre el mismo árbol y la misma rama (15/11, en dos
  olas):** tras el reinicio, la otra sesión se reanimó **dos veces**: entre **11:29 y 11:49** y otra vez entre
  **12:37 y 13:54**. No hubo conflicto de `git` (un solo HEAD y un solo índice), pero sí dos tramos de trabajo:
  la primera ola quedó contenida en **`f5411c1`** (las 77 pruebas del CDU19) y esta sesión lo **verificó ejecu-
  tando** en lugar de reescribirlo: compila y pasa las **713 pruebas** del reactor. La segunda ola dejó en el
  árbol, **sin commitear**, el spec TDOP y los 16 CSV de `docs/test/iter06/` (mtimes 12:37-13:53): esta sesión
  los regeneró con `tools/pairwise.py` — **los 16 ficheros salen byte a byte iguales** a los del árbol — los
  validó contra `IntegracionManejadorErrores` y `SetupSimulacion`, y los subió tal cual en **`a81b85b`**,
  dejando la procedencia en el cuerpo del mensaje. Su última actividad registrada fue **13:54:41** y desde
  entonces no ha vuelto a tocar nada: **el único agente activo es esta sesión**, y ya no hay dos. Regla
  práctica: **una rama y un árbol por sesión**, y comprobar `HEAD` + `git status` antes de escribir.
- **Incidencia 3 — clon de la wiki desactualizado y PNG de iter01-03 borrados (15/11, sesión 23):** el clon del
  repo wiki del temp estaba **5 commits por detrás** de `origin/master` (Ismael había añadido
  `Iter5-CDU12-citas`, `Requisitos`, el PDF del enunciado `v1.15.3` y los PNG de iter05) y el **wipe de ficheros
  no trackeados** se había llevado por delante los 12 PNG de `images/iter01-*`…`iter03-*`. Se resolvió con
  `git checkout -f master` + `git merge --ff-only origin/master` → HEAD `df2c140`, que **recupera los PNG perdidos**
  desde el remoto; la página nueva y los PNG de Iter6 se escribieron **después** de ese `merge`, y antes de
  commitear se verificó el índice con `git diff --cached --name-only`. Regla: **en la wiki hay que `fetch` +
  `merge --ff-only origin/master` antes de escribir**, porque el remoto puede ir por delante del clon.
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
| 25 | 16-17/11/2026 | **Iter7 · CDU14 listas de espera (CFG-07, issue #47) — implementación servidora completa; ejecutor TDOP aún sin cerrar:** rama `feature/iter07-cdu14-listas-espera` (subida hasta `47a6601`). Commits: `d9c0766` inventario de la base, `410bf15` diagramas D31, `080fd09` **diseño TDOP de 253 filas** (8 tablas A-H en `docs/test/iter07/tdop-cdu14-espec.json`, semilla 20261116, CSVs regenerados y verificados byte a byte) + plantilla `docs/tc/cdu14-tc-humanos.md`, `9fd5eff`+`18a5b78` alineación del modelo (Agente B), `741fdfe` tren de versiones 1.2.0-SNAPSHOT + `tools/cdu14_smoke.py`, `6168156` contratos en `iso2-common`, `1fda042` dominio (comparador determinista prioridad↓/fechaSolicitud↑/esperaId↑ + auditoría `REUBICACION` encadenada propia en `espera_auditoria` con génesis virtual), `f6dda51`+`23627f7` repositorios y asignación con profesional, `dfe87aa` servicio + auditoría, `2c7ea4e` capa web con consejos de error, `99578f9` RBAC `lista_espera:leer/escribir` + Flyway **V1.6.0 dual h2+mysql** + semillas, `7891e61` notas del ejecutor al doc TDOP, `d8f95fe` **arreglo del hallazgo TC14-E10**: control optimista `@Version` en `ListaEspera` → 409 `CONFLICTO_ENTRADA_NO_ACTIVA` + columna `version` en V1.6.0 (modelo D31 alineado), `47a6601` doble `PRUEBA_DUP` y borrador del ejecutor guardado en `docs/test/iter07/borrador-ejecutor/`. **Estado honesto:** reactor `iso2-citas`+`iso2-server-app` en verde (`mvn -B -pl iso2-citas,iso2-server-app -am test`: **238 tests** en citas —los de siempre, **ninguna clase de test nueva de CDU14 compila todavía**— y **90** en server-app con 3 saltados), así que la **puerta de la matriz CFG-07 sigue ABIERTA**: `Cdu14MatrizTdopTest` (253 casos 1:1) no existe aún. Ensayo **TC14-H05 en verde contra el servidor real** (5 ALTAs 201, tres lecturas de cola idénticas, `auditoria/verificar` → verificable=true con 8 eslabones, retirar 200). Hallazgo anotado: `AgendaNoEncontradoException` del asignar no está mapeada en el consejo de citas → 500 `ERROR_INTERNO` (afecta a F16/F17/F48/F50) | — |

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
20. **TC humanos de CDU12 (D28) — pendientes de Ismael, ya sin bloqueo de PR**: el PR de cierre de CFG-05 se
    fusionó el **15/11** (**PR #43** → merge `cbb1901`), así que la plantilla está en `develop`. Está en
    `docs/tc/cdu12-tc-humanos.md` con los **172 casos TDOP generados** por el método D33 (tablas A–F, los
    mismos que ejecuta `Cdu12MatrizTdopTest`) más la preparación del entorno. Al ejecutarlos sobre el
    sistema en marcha, anotar el resultado y cerrarlos como se hizo con los de CDU16 (PR #37).
21. **Iter6 · CDU19 (CFG-06) — CERRADA EL 15/11 (sesión 24; issue #44)**:
    (a1) **hecho** — evidencia TDOP en `docs/test/iter06/` (`tdop-cdu19-espec.json` de **126 casos en 8 tablas
    A-H** + los **17 CSV**, también versionados en `iso2-integracion/src/test/resources/tdop/`, generados con
    `tools/pairwise.py`) subida en `a81b85b` y corregida en `c104d78`;
    (a2) **hecho** — **`Cdu19MatrizTdopTest`: 126 casos leídos de CSV, en verde**, y `docs/test/iter06/tdop-cdu19.md`
    como evidencia autogenerada (`c104d78`);
    (a3) **hecho** — **TC humanos proyectados** a `docs/tc/cdu19-tc-humanos.md` con `tools/tc_plantillas.py`
    (`ec7f228`); (b) **hecho** — **informe de spike** en `docs/informes/spike-cdu19.md` con el veredicto de
    viabilidad por integración, la decisión FHIR/JSON y **15 hallazgos** (`2388b7e`); (d) **hecho** — **PR #45**
    abierto y **fusionado por el agente** a `develop` (merge **`0ef4f1a`**, check `construir` en verde, 91 ficheros,
    +8232/−82); (e) **hecho** — wiki **`Iter6-CDU19-integracion`** publicada en `master` del repo wiki con la
    **clase probada** `Cdu19MatrizTdopTest`, los 4 diagramas, la API/RBAC reales y las cifras de JaCoCo medidas con
    `verify` (merge **`9b7f513`**, y `Home.md` con la fila de Iter6); (f) **hecho el 15/11 por el agente** —
    **TC humanos de CDU19 ejecutados contra el servidor real** (9 TC en 30 pasos, todos con su asiento en
    `docs/tc/cdu19-tc-humanos.md`) y su **evidencia HTTP versionada** en `docs/tc/evidencia/cdu19/`; esto **no
    sustituye la validación D28 de Ismael**, que sigue pudiendo repetirlos sobre la H2 local; (g) **hecho** —
    **rama `feature/iter06-cdu19-integracion` borrada** en remoto y local tras la fusión; (h) **hecho** —
    **#44 cerrada con la evidencia detrás** y **tag anotado `cfg-iter06` sobre la fusión del PR de cierre** de
    `docs/iter06-cierre`. El hito **`v0.2.0-alpha` (R1)** sigue siendo de `release/R1` hacia `main`, no de esta rama.
22. **Revalidación D37 al cerrar la Iter6 — HECHA el 15/11**: `TASADEFECTOS.md` queda con la **fila de Iter6**
    (corte `cfg-iter05..cfg-iter06` = **22 commits** · 0 F1 · 1 F2 · 0 F3 · 1 F4 · **2 totales · 9,1/100**) y el
    libro con **DEF-14** (F4: `IntegracionProperties` sin `@ConfigurationProperties(prefix = "iso2.integracion")`,
    nacido en `7a03e0c` y arreglado en `f5411c1` dentro de la misma rama) y **DEF-15** (F2: el vaciado de
    `STATUS.md`/`PROMPTS.md`, imputado al tramo aunque el daño nunca llegó al repositorio). Los hashes `00c6194`
    que figuraban en el fichero **no existían en el repo** y se han sustituido por los reales. Total histórico
    corregido: **129 commits · 15 defectos · 13 fugados · 11,6/100**. Métricas de §4 y veredicto de §6 rehechos con
    datos medidos (839 tests, reactor JaCoCo 87,0 % líneas / 75,9 % ramas, 83 ejecuciones de CI con 3 rojas).
23. **Higiene de ramas (Ismael) — CERRADA el 15/11**: `feature/iter05-cdu12-citas` y
    `feature/iter06-cdu19-integracion` están **borradas del remoto y de local** (la segunda tras fusionar el
    PR #45). La única rama de trabajo abierta es `docs/iter06-cierre`, que se borra al fusionar su PR de cierre.
24. **Deuda que deja la CDU19 (D-01..D-07 del informe `docs/informes/spike-cdu19.md` §6, más D-08 y D-09 del
    cierre), para Iter7:** **D-01** servir la instantánea guardada cuando el externo cae (hoy `IntegracionServicio` consulta HCE
    antes que sus `snapshots` → los casos B04/B05 de la matriz cierran en 503) o documentar que el degradado no
    incluye caché; **D-02** probar `IntegracionProperties` (11/30 ramas cubiertas) y decidir si un modo mal escrito
    es *aviso + default* (promete el DoD) o *fail-fast* (hace el código); **D-03** canjear la sesión SSO por el JWT
    de CDU17 y mapear el rol externo a `Permiso`; **D-04** cliente HTTP real (timeout que corta, reintentos con
    backoff, `Idempotency-Key`) e importación **DICOMweb** del PACS para Iter16; **D-05** regresión del **modo
    degradado** en `iso2-server-app` (hoy solo el TC19-04 manual) y el **403 por rol no `ADMINISTRADOR`**
    automatizado, ambos pedidos por el DoD y hoy inexistentes: ningún test de `iso2-server-app` toca
    `/api/integracion`; **D-06** auditoría de integración persistente (implica reabrir la puerta a Flyway, que la
    issue cerraba); **D-07** `HuellaContenido` (2/4 líneas) y el record `IntegracionProperties.SistemaOverride`
    (0/1) sin cubrir; **D-08** `iso2-common` tiene **0 % de cobertura (0 de 139 líneas, 0 de 46 ramas)** y su
    `pom.xml` no fija puerta, así que hereda el `0.00` de la raíz y CI no se entera: o se le ponen pruebas (el
    `DigestSha256` de la D33 vive ahí) o se le pone cota; **D-09** las rutas sin mapear devuelven **401
    `AUTENTICACION_REQUERIDA` con cuerpo `ApiError` incluso con token válido** (lo comprobó TC19-09 con
    `/api/citas/huecosx`, `/api/no-existe` y `/api/integracion/ping`): Spring Security decide antes de que el
    enrutador distinga 404 de 401; **D-10** una matriz de módulo puede esperar un código distinto del de la
    aplicación montada: TC12-C04, TC12-C21 y TC12-D03 esperan 500 por el `TypeMismatchException` del
    `@RequestParam LocalDate` en `/api/citas/huecos` y el servidor real contestó **400 `DATOS_INVALIDOS`**,
    porque el `GlobalExceptionHandler` de `iso2-common` sí tiene consejo para ese caso y la matriz aislada solo
    carga el consejo local `CitaManejadorErrores`. No es un defecto colado (la spec de CDU12 ya contemplaba la
    alternativa), pero obliga a documentar la divergencia o a montar la matriz sobre el contexto real. Las tres
    nacieron en el cierre de la sesión 24, con evidencia versionada en
    `docs/tc/evidencia/cdu19/sondeo-rutas-sin-mapear.json`. Y una nota práctica de este cierre: **los scripts de
    evidencia no deben sanear el JWT en la capa HTTP** (ese bug ya obligó a corregir `tc19_auditoria.py`, que ahora
    solo sanea al escribir el JSON).
25. **Configurar el entorno OpenCode para cortar el wipe (decisión de Ismael; la causa ya está investigada)**: el
    borrado de ficheros **no trackeados** que ha venido sufriendo el árbol coincide literalmente con cómo funciona
    el `undo`/`Revert` de las **snapshots** de OpenCode: el rollback restaura las rutas atribuidas a los pasos del
    asistente posteriores a la frontera elegida y **elimina un fichero creado por esos pasos si antes no existía**;
    además captura ficheros no trackeados no ignorados de hasta 2 MiB. El almacén está en
    `%USERPROFILE%\.local\share\opencode\snapshot` (ahora 16,6 MB de un solo proyecto) y **no** es lo mismo que el
    apagón del 15/11, que vació el almacenamiento de sesiones. La pieza que lo desactiva es `"snapshots": false` en
    `~/.config/opencode/opencode.jsonc` (o en el `opencode.json` del proyecto): deja de capturar estado de ficheros,
    el rollback solo deshace conversación y las instantáneas ya guardadas no se borran. Contrapartida: se pierde el
    `/undo` de ficheros, algo aceptable mientras el agente trabaje con **commit + push inmediato**. No se ha
    aplicado: cambia la configuración global de la máquina y le corresponde a Ismael (el agente puede hacerlo si se
    lo pide). **Decisión tomada el 15/11: se queda como está, sin tocar la config**; así que el wiping no se corta y
    la única defensa sigue siendo el orden **escribir → verificar en disco → `git add` → commit → push inmediato**.
26. **Alcance del agente B de la Iter7 (decidido por Ismael el 15/11)**: se autoriza un segundo agente en un
    *worktree* aparte (`C:\ProgIA\PrISO2-iter07`, creado desde `develop`) **solo sobre `docs/modelo/iter07/` y
    `docs/test/iter07/`**: diagramas `.puml` + sus PNG y la especificación/matriz TDOP con sus CSV. **No toca `src/`,
    ni `pom.xml`, ni `STATUS.md`/`PROMPTS.md`, ni `main`/`develop`**, y abre su propio PR a `develop`. El PR
    borrador #45 **ya quedó fusionado el 15/11 en la sesión 24** (merge `0ef4f1a`), así que ese bloqueo está
    levantado: el *worktree* puede crearse desde el tip actual de `develop`.

## Punto exacto de continuación para la próxima sesión

**La Iter5 / CFG-05 está CERRADA del lado del repo** (15/11, sesiones 21-22): los 12 commits de código entraron
con el **PR #42** (merge `08a7ed8`, 14/11 22:25) y la **matriz D33 de 172 casos** con el **PR #43** (merge
**`cbb1901`**); **#40 la cerró Ismael a mano** (D29) y el tag anotado **`cfg-iter05`** (`005476e`) apunta a
`cbb1901`. La rama fusionada **ya está borrada** (Deuda nº 23 cerrada en la sesión 24); solo le quedan los
**TC humanos de CDU12** (D28, Deuda nº 20).

**La Iter6 / CDU19 (CFG-06) está CERRADA EL 15/11 en la sesión 24**, por orden de Ismael («haz tú lo que
queda», D38). Ya estaba hecho el cuerpo de la iteración (sesión 23: modelo, módulo, 203 pruebas, matriz TDOP de
126 casos en verde por `Cdu19MatrizTdopTest`, informe de spike y wiki). Hecho en el cierre: **PR #45 fusionado**
a `develop` (merge **`0ef4f1a`**, CI `construir` en verde), **rama `feature/iter06-cdu19-integracion` borrada**
en remoto y local, **TC humanos de CDU19 ejecutados por el agente contra el servidor real** con su evidencia
HTTP versionada en `docs/tc/evidencia/cdu19/` (scripts y JSON crudos, JWT redactado), **tabla manual rellena**
en `docs/tc/cdu19-tc-humanos.md`, **revalidación D37 escrita** en `TASADEFECTOS.md` y **deuda nueva D-08..D-10**
anotada. Quedan tres gestos materiales, y también los hace el agente: **commit + push de los docs de cierre**
desde la rama `docs/iter06-cierre` con su PR a `develop`, **tag anotado `cfg-iter06` sobre ese merge de cierre**
(patrones `cfg-iter05` y `cfg-iter04`) y **cerrar la issue #44** con su DoD. Siguen siendo de Ismael el hito
**`v0.2.0-alpha`** (ALFA-1 / R1), que se materializa publicando **`release/R1` → `main`** (D30), y el **refrendo
de los TC humanos** que ejecutó el agente (D28): esa ejecución es evidencia útil, no sustituye su validación.

**La Iter7 / CFG-07 · CDU14 «listas de espera» está EN CURSO desde el 16/11 (sesión 25)** sobre `iso2-citas`
**1.2.0-SNAPSHOT** (inversión D23: CDU14 antes que CDU01), en la rama `feature/iter07-cdu14-listas-espera`
(subida hasta `47a6601`). Ventana del plan **23-27/11/2026** (vamos por delante), fase Construcción, actor
**Administrativo**, pareja responsable P-A, **22 h con IA**. Foco de calidad ISO 25010: **Adec. Funcional (corrección)**, **Usabilidad
(operabilidad y protección)** y **Seguridad (no repudio y responsabilidad)**. Puertas de CFG-07 en
`docs/planificacion-iteraciones+Calidad.csv`: **reordenación determinista probada** y **auditoría de
reubicaciones**. Sus condiciones de trabajo están en el apartado **2-ter**.

**Hecho ya en la Iter7** (ver sesión 25 del registro): inventario (`d9c0766`), diagramas D31 (`410bf15`), diseño
TDOP de 253 filas con spec canonica + plantilla de TC humanos (`080fd09`), modelo alineado del Agente B
(`9fd5eff`+`18a5b78`), tren de versiones 1.2.0 + smoke (`741fdfe`), contratos en `iso2-common` (`6168156`),
dominio con cola determinista y auditoría `REUBICACION` (`1fda042`), repositorios (`f6dda51`+`23627f7`), servicio
(`dfe87aa`), capa web (`2c7ea4e`), RBAC + Flyway V1.6.0 dual (`99578f9`), notas del ejecutor (`7891e61`) y control
optimista `@Version` del hallazgo TC14-E10 (`d8f95fe`). **La puerta grande sigue abierta: el ejecutor
`Cdu14MatrizTdopTest` de los 253 casos no existe todavía** (solo el borrador en
`docs/test/iter07/borrador-ejecutor/`), y no hay ninguna clase de test nueva de CDU14 compilando. Pasos
siguientes, en orden:

1. **Cerrar la matriz TDOP (prioridad nº 1):** escribir `Cdu14MatrizTdopTest` en
   `iso2-citas/src/test/java/es/uclm/iso2/citas/tdop/` siguiendo el patrón de `Cdu12MatrizTdopTest`: lectura de
   los 9 CSVs, tabla de esperados, ayuda `aplicarSetup` (semillas `SEMILLA-COLA-*` por API y `SEM-TAMPER:*` con
   los hooks `setHashParaPruebas`/`setMotivoParaPruebas`), y las 8 tablas A-H. El sembrado usa **un único
   paciente** (`api-paciente`) y la colisión de clave se dispara con `api-prueba-dup` (`PRUEBA_DUP` en
   `PruebasCitas`). Regla D33: si un caso sale rojo, se corrige el código o la spec JSON y se regeneran los
   CSVs con `python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources
   iso2-citas/src/test/resources/tdop`; nunca editar CSVs ni el ejecutor a mano.
2. **Hallazgo que destapará la tabla F (F16/F17/F48/F50):** `AgendaNoEncontradoException` del asignar no está
   mapeada en `ListaEsperaManejadorErrores` y cae a 500 `ERROR_INTERNO`. Decidir (con Ismael si hace falta) si
   se mapea a 404/409 o la spec acepta `ERROR_INTERNO`, y dejarlo consignado.
3. **TC humanos:** la plantilla `docs/tc/cdu14-tc-humanos.md` ya proyecta las 8 tablas; el ensayo del flujo
   TC14-H05 contra el servidor real ya salió verde (evidencia en el registro sesión 25). Rellenado y refrendo,
   de Ismael (D28).
4. **Wiki (D32):** página `Iter7-CDU14-listas-espera.md` (espejo de `Iter6-CDU19-integracion.md`), PNGs a
   `images/iter07-cdu14-*.png` y fila de Iter7 en `Home.md`.
5. **Cierre CFG-07 (D29, solo por orden de Ismael):** PR `feature/iter07-cdu14-listas-espera` → `develop`
   (sin `Closes`), fusión y cierre de **#47** a mano, tag anotado `cfg-iter07` sobre el merge y refresco de
   `TASADEFECTOS.md` (D37).

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

### 2) Iter6 · CDU19 integración (CFG-06) — ~~en curso~~ → **ejecutada y CERRADA el 15/11** (sesión 24)

Rama `feature/iter06-cdu19-integracion`, abierta **desde `develop` = `cbb1901`** (ya con la Iter5 dentro) y con
`Ref: #44` en cada commit. Versiones: `iso2-integracion` **1.1.0-SNAPSHOT** (su baseline **1.0.0 queda
congelado**: `ContratoBaselineTest` es el test que lo blindó en Iter4 y sigue verde), `common` **1.1.0-SNAPSHOT**
(se congela en 1.1.0 al publicar R1) y las apps **0.2.0-SNAPSHOT** (es el primer salto de tren de release).
**Sin migración Flyway**: el spike es deliberadamente **sin persistencia** (la auditoría va encadenada en
memoria), así que el slot `V1.6.0__…` queda libre para el CDU de Construcción que lo necesite. Puertas de
CFG-06 en `docs/mapa-configuraciones.csv`: **flujo rol+catálogo+agenda+solicitud+cita end-to-end** y
**JaCoCo ≥ 60 %**; el módulo puso su propia cota más alta, **`jacoco.line.min = 0.70`**, y está en **96,5 % de
líneas y 78,4 % de ramas** (medido con `mvn -B -ntp -pl iso2-integracion verify` sobre `2388b7e`; **el CSV de
JaCoCo solo lo escribe la fase `verify`**, así que leerlo tras un `mvn test` da cifras caducadas — era el caso del
88,2 % que venía escrito aquí).
El trabajo va **después de los diagramas** (D31, diseño-primero: los 4 `.puml` + `.png` de `docs/modelo/iter06/`
se commitearon antes que el código, en `db075c0`). Fecha objetivo **20/11/2026**, porque la Iter6 es la que
trae el hito **`v0.2.0-alpha`** (ALFA-1 / R1). Orden de lectura al llegar: `CONTEXT.md` → `DECISIONS.md`
(D28-D37) → `STATUS.md` → `PLAN.md` §4 → fila CFG-06 del mapa. Al cerrar: **revalidación D37** en
`TASADEFECTOS.md` (Deuda nº 22) y wiki con la **clase probada** nombrada (D33).

### 2-ter) Iter7 · CDU14 listas de espera (CFG-07) — la que sigue: las condiciones del trabajo

Rama `feature/iter07-cdu14-listas-espera`, abierta **desde el tip de `develop` tras el cierre de la Iter6** y con
`Ref: #NN` (la issue CFG-07, que hay que abrir antes) en cada commit. Versión esperada: `iso2-citas` **1.2.0**
(su baseline **1.0.0 → 1.1.0** ya quedó blindado en Iter5); las apps siguen en **0.2.0-SNAPSHOT** y los demás
módulos no se tocan. Fase **Construcción**, fecha objetivo **27/11/2026**, **22 h con IA**, actor
**Administrativo**, pareja responsable **P-A**, complejidad **Media**.

- **Flyway dual**: el slot libre es **`V1.6.0__cdu14_listas_espera.sql`**, que hay que escribir en
  `iso2-server-app/src/main/resources/db/migration/h2/` **y** en `db/migration/mysql/` (D34: si solo se pone en
  `h2/`, el perfil `mysql` se queda en V1.5.0 y revientan los tests Testcontainers). Las franjas y citas semilla
  de `V1.5.0__cdu12_citas.sql` son la base sobre la que se prueba la cola de espera.
- **Modelo primero** (D31): los 4 `.puml` + `.png` de `docs/modelo/iter07/` se commitean **antes** del código.
  Ahí y en `docs/test/iter07/` es donde puede trabajar el agente B (Deuda nº 26), en su *worktree*.
- **Puerta 1 de CFG-07 · reordenación determinista probada**: un comparador de reordenación de la cola con su
  **tabla de casos** (prioridad, urgencia, fecha de entrada, antigüedad y empates). La exigencia de
  `SOFTWAREQUALITY.md:161` es literal: reordenación **estable y determinista**; o sea, dos ejecuciones con el
  mismo estado dan el mismo orden y los empates se rompen por un criterio fijo y documentado.
- **Puerta 2 de CFG-07 · auditoría de reubicaciones**: cada movimiento deja **autor, motivo y fecha** y se
  encadena con el hash de `AuditoriaCitas` de CDU12, añadiendo una acción **`REUBICACION`** al vocabulario de
  esa auditoría (asiento inmutable, como el resto).
- **Calidad que se juega en Iter7** (`SOFTWAREQUALITY.md:161` y puertas de R2 en `:178-189`): **Adec. Funcional
  (corrección)**, **Usabilidad (operabilidad y protección: el doble clic no puede duplicar un hueco)** y
  **Seguridad (no repudio y responsabilidad)**. Puertas de la release **R2 alfa (27/11)**: línea JaCoCo **≥ 70 %**,
  rama **≥ 55 %**, **un TC RBAC por cada endpoint nuevo** y trazabilidad **12/20**. El **≥ 85 % en `iso2-citas`**
  es la puerta de **R5 final** (hoy el módulo está en 95,8 % de líneas y 90,8 % de ramas, con cota propia 0,80).
- **Método D33 sobre CDU14** (ya probado en CDU16 · 115, CDU12 · 172 y CDU19 · 126 casos): spec JSON en
  `docs/test/iter07/tdop-cdu14-espec.json` → `python tools/pairwise.py` genera los CSV en
  `iso2-citas/src/test/resources/tdop/` → `python tools/tc_plantillas.py` proyecta
  `docs/test/iter07/tdop-cdu14.md` y la plantilla `docs/tc/cdu14-tc-humanos.md` → la matriz se ejecuta en verde
  en la **clase probada `Cdu14MatrizTdopTest`** → wiki `Iter7-CDU14-listas-espera` nombrando esa clase (D32).
  Nunca se editan a mano los CSV ni el bloque `<!-- TDOP:BEGIN -->`/`<!-- TDOP:END -->`.
- **Informe del diseño de los conjuntos de prueba** (nuevo pedido de Ismael del 15/11, con valor pedagógico):
  documentar **cómo se diseñan** las cuatro familias —clases de equivalencia, conjetura de errores, valores
  límite y *pairwise*—, no solo el resultado. Sitio: `docs/informes/diseno-conjuntos-prueba.md`.
- **Deuda heredada a repartir**: `D-01..D-07` del spike (`docs/informes/spike-cdu19.md`) más las tres nacidas en
  el cierre de la Iter6 — **D-08** (`iso2-common` al 0 % de cobertura y sin puerta en su `pom.xml`), **D-09**
  (rutas sin mapear → 401 con cuerpo `ApiError` incluso con token válido) y **D-10** (la matriz aislada espera un
  código distinto del de la aplicación montada). Son deuda de prueba/contrato: caben en Iter7 sin ahogar los
  22 h de CDU14, pero hay que decirlas en la issue para no repartirlas a ojo.
### 2-bis) Iter5 · CDU12 (CFG-05) — ~~siguiente iteración~~ → ejecutada y cerrada el 15/11

~~Receta tal como se escribió, cerrada con el PR #42 (`08a7ed8`), el PR #43 (`cbb1901`) y el tag `cfg-iter05`.
Se deja por si sirve de patrón para la Iter6:~~

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
400, nunca contra la BD → 500), **D37** (tasa de defectos por iteración: en **cada cierre de CFG** se
actualiza `TASADEFECTOS.md` —libro de defectos con evidencia y tabla por tramo de etiquetas— y el registro de
sesión de `STATUS.md` lleva la línea con los 6 contadores `commits · F1 · F2 · F3 · F4 · total (fugados)`,
plantilla en §7 de ese fichero) y el **flujo diseño-primero** (editar el `.puml` antes que el código cuando
cambia un invariante).

**Datos de defectos al 15/11/2026 (D37, corte de cierre de CFG-05):** 107 commits (94 en `develop` + 13 en
la rama de Iter5 `6feabdf..b912554`) → **13 defectos** = 6 F1 proceso + 3 F2 entorno + 2 F3 contrato +
**2 F4 código** · 11 fugados · 12,1 por 100 commits (F4 solo 1,9). Iter0 2, Iter1 3, Iter2 0, Iter3 3,
Iter4 2, tramo D33/D34 2, Iter5 1 (DEF-13, F2 fugado; **Iter5 no añade ningún DEF nuevo**, ver §2 de
`TASADEFECTOS.md`). CI en el PR #42: 72 ejecuciones, 69 verdes; los 3 rojos son la puerta JaCoCo
planificada de `iso2-citas` en commits intermedios sin tests (ya superada: 238 tests, 95,8 %). Detalle y
receta de recálculo en `TASADEFECTOS.md`.

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


**Trabajo de la sesión 20 (11/11/2026, métrica de calidad - D37):** creado **`TASADEFECTOS.md`** en la raíz:
libro de 13 defectos con su evidencia (commits/merge/registros de sesión), tabla por iteración con el esfuerzo
medido como `git rev-list --count` entre etiquetas (101 commits = 94 en `develop` + 7 en la rama de Iter5) y
recetas de recálculo en PowerShell. Nueva **D37** en `DECISIONS.md` (aceptada por Ismael al pedirla): 4 familias
de defecto (F1 proceso, F2 entorno, F3 contrato, F4 código - solo F4 mide la valía del modelo), regla de
exclusión de lo que pilla el gate local antes de pushear, y umbrales para pedir un modelo más potente (> 2 F4
por familia de CDU, 1 decisión de diseño que obligue a reescribir código commiteado, o 1 falso verde en CI).
Desde ahora, en cada cierre de CFG se actualiza `TASADEFECTOS.md` y el registro de sesión de `STATUS.md` lleva
la línea de 6 contadores (plantilla en §7 de `TASADEFECTOS.md`). Cifra de partida: 12,9 defectos por 100
commits, de los que solo 2,0 son de código; los 2 rojos de CI son la puerta JaCoCo planificada de `iso2-citas`.
Nota: en el punto anterior `HEAD` figura como `e44f7c1`, pero el punta real de la rama es `7abea47`.

**Trabajo de la sesión 21 (15/11/2026, cierre de Iter5/CFG-05: D33 de CDU12 + wiki + D37 - Prompt 21):**
primera mitad de la sesión, cerrar el **método D33 sobre CDU12**: spec TDOP de seis tablas en
`docs/test/iter05/tdop-cdu12-espec.json` → `python tools/pairwise.py` → **172 casos** en los CSVs de
`iso2-citas/src/test/resources/tdop/` → **`Cdu12MatrizTdopTest` 172/172 en verde** (A38 alta, B31
reprogramación, C29 huecos, D27 listado, E34 estado, F13 conjetura) → evidencia `docs/test/iter05/tdop-cdu12.md`
y bloque gestionado proyectado **tal cual** a `docs/tc/cdu12-tc-humanos.md` con `tools/tc_plantillas.py`
(dos ajustes genéricos previos en la herramienta: paquete `tdop` derivado de la spec y mapa `operaciones`
por CDU). Récord D33: **CDU16 115 casos + CDU12 172 casos = 287 casos pairwise ejecutados**. Segunda
mitad, cerrar CFG-05: **D34 en la puerta** (puerta JaCoCo de `iso2-citas` superada), wiki
**`Iter5-CDU12-citas.md` publicada** en `master` de la wiki (merge **`0809425`**: página + 4 PNG
`iter05-cdu12-*` + fila en `Home.md`, D30) nombrando clase probada `Cdu12MatrizTdopTest`; **revalidación
D37** en `TASADEFECTOS.md` (corte `b912554`: **Iter5 cierra sin DEF nuevos**, no hay DEF-14 porque el
modelo ya fijaba `reprogramar` como `POST` y nunca prometió PUT/PATCH → sin desviación de contrato; el
único defecto del tramo es el F2 ya fugado DEF-13); `mvn -B -ntp clean verify` con Docker Engine arriba
= **BUILD SUCCESS, 637 tests, 0 saltados** (los 3 Testcontainers de `mysql:8.4` corriendo de verdad); y
**PR #42 fusionado por Ismael (merge `08a7ed8`, 14/11 22:25)** con los 12 commits de código y **#40 cerrada
a mano** (14/11 22:24, D29); la matriz D33 `b912554`, pusheada justo después del merge, sube con estos docs
en un **PR de cierre aparte** y sobre su fusión irá el tag `cfg-iter05`.

**Datos de defectos Iter5/CFG-05:** commits `6feabdf..b912554` = 13 · F1 proceso 0 · F2 entorno 1 ·
F3 contrato 0 · F4 código 0 · total 1 (fugados 1) · **7,7 por 100 commits** · detalle en
`TASADEFECTOS.md` (DEF-13). Serie total al corte: **107 commits, 13 defectos, 12,1 por 100, solo 1,9 de
código**. Queda de Ismael (D29): fusionar el PR de cierre (lleva `b912554` + estos docs), poner el tag
`cfg-iter05` sobre esa fusión y borrar la rama; luego TC humanos de CDU12
(D28) e **Iter6 · CDU19 (CFG-06)** con el hito `v0.2.0-alpha`.


**Trabajo de la sesión 22 (15/11/2026, recovery tras el reset del equipo + apertura de Iter6/CFG-06 —
Prompts 21 y 22):** la sesión 21 se cortó de golpe por un reinicio del equipo y el almacenamiento local de
OpenCode (`%USERPROFILE%\.local\share\opencode\storage`) quedó vacío, así que **se perdió el registro de la
conversación**. No se perdió trabajo: la sesión 22 se reconstruyó leyendo `git reflog`, `STATUS.md`,
`TASADEFECTOS.md` y GitHub, y apareció que Ismael ya había **fusionado el PR #43 (merge `cbb1901`, 15/11 08:52,
hoy tip de `develop`)** y **cerrado #40 a mano**. Con eso:

* **Cerrada Iter5 / CFG-05 del lado del repo** y puesto el **tag anotado `cfg-iter05`** (objeto `005476e`) sobre
  `cbb1901`, siguiendo la receta de `docs/mapa-configuraciones.md` y el formato del mensaje de `cfg-iter04`. El
  tag recuerda que **no hay release en `main` para la Iter5** (D30) y que los **TC humanos de CDU12 siguen
  pendientes** (D28).
* **Arrancada la Iter6 · CDU19 (CFG-06)** en `feature/iter06-cdu19-integracion`, abierta desde `develop` =
  `cbb1901` sobre la issue **#44**, con el flujo diseño-primero (D31) y `Ref: #44` en cada commit:
  `db075c0` (`docs/modelo/iter06/` con los 4 `.puml` de casos de uso, análisis, diseño y secuencia, re-renderizados
  con `-tpng` y `PLANTUML_LIMIT_SIZE=16384`) → `687c624` (puerto `InteropFacade` en **1.1.0**, baseline 1.0.0
  congelado) → `7a03e0c` (módulo `iso2-integracion` 1.1.0-SNAPSHOT: adaptadores simulados SSO / historia clínica /
  RIS-PACS, DTOs, auditoría encadenada en memoria y smoke del contrato) → `9ca712f` (re-render del diagrama de
  secuencia tras el arreglo del diseño) → `a22e654` (RBAC `INTEGRACION_LEER/ESCRIBIR` + `IntegracionApiTest` +
  salto de tren de release: apps **0.2.0-SNAPSHOT**, modelo `iso2.version`/`common` **1.1.0**) → `f5411c1`
  (**77 pruebas del CDU19** y arreglo del `@ConfigurationProperties` que faltaba en `IntegracionProperties`).
  **[Corregido en la sesión 23: el RBAC real de los 7 endpoints es solo el rol `ADMINISTRADOR`; la pareja
  `INTEGRACION_LEER/ESCRIBIR` que anunciaba la issue #44 nunca existió en el código.]**
* **Medido de nuevo con esta sesión, ejecutando**: `mvn -B -ntp test` en el reactor completo sobre `f5411c1` =
  **BUILD SUCCESS con 713 tests, 0 fallos, 0 errores, 0 omitidos** y **JaCoCo de `iso2-integracion` en 88,2 % de
  líneas** (cota propia `jacoco.line.min=0.70`, puerta de CFG-06 ≥ 60 %). **[Corregido en la sesión 23: esa cifra
  estaba caducada porque el CSV de JaCoCo se leyó tras `mvn test`, y la fase `report`/`check` está enlazada a
  `verify`; la medida real del módulo, con `mvn -B -ntp -pl iso2-integracion verify`, es 96,5 % de líneas y 78,4 %
  de ramas.]** Reparto de los 77 del CDU19:
  `AdaptadorSimuladoTest` 15 · `IntegracionServicioTest` 20 · `IntegracionControladorTest` 13 ·
  `SimulacionControladorTest` 8 · `ArquitecturaIntegracionTest` 6 · `AuditoriaIntegracionTest` 6 ·
  `SetupSimulacionTest` 4 · `RegistroSimulacionTest` 4 · `ContratoBaselineTest` 1. **Todavía no hay matriz D33
  leída de CSV ni `Cdu19MatrizTdopTest`**: eso es la parte b, con `tools/pairwise.py`.
* **Incidencia registrada (dos agentes a la vez, en dos olas):** entre las 11:29 y las 11:49 del 15/11, y otra
  vez entre las 12:37 y las 13:54, trabajó en paralelo otra sesión sobre el **mismo árbol y la misma rama** con
  la intención de hacer la matriz TDOP. No hubo conflicto de `git` (un solo HEAD, un solo índice). La primera
  ola quedó contenida en `f5411c1` y esta sesión lo **verificó ejecutando** en lugar de reescribirlo; la segunda
  dejó el spec TDOP y sus 16 CSV **sin commitear** en el árbol, y tras regenerarlos y comprobar que salían
  **byte a byte iguales** se subieron en `a81b85b` con su procedencia en el mensaje del commit. Desde las
  13:54:41 esa sesión no ha vuelto a escribir nada: **queda un solo agente**, y la regla práctica que se saca
  es **una rama y un árbol por sesión**, comprobando `HEAD` y `git status` antes de escribir.
* **Deuda nueva de esta sesión (nº 21, 22 y 23)** y revalidación **D37** aplazada al cierre de CFG-06: el tramo ya
  arrastra **1 defecto F4 de código fugado** (`IntegracionProperties` sin `@ConfigurationProperties` pese a que el
  `.puml` de la iteración sí lo prometía → el contexto del `server-app` moría al arrancar; arreglado en `f5411c1`).
* **Cierre de jornada 15/11/2026 (mañana):** `develop` = `cbb1901` con **cfg-iter05 puesto**, `main` = `d12388b` sin tocar
  (D30), rama de Iter6 en `f5411c1` con el árbol local idéntico al remoto y **nada sin subir**, ningún proceso
  `java`/`mvn` suelto y ningún PR abierto. Avance del plan: **Iter0-Iter5 = 192 h de 612 h ≈ 31,4 %** (6 de 22
  iteraciones fusionadas y tagueadas). Regla que se queda: **nada se queda sin subir** y el registro del proyecto
  son `STATUS.md` + `PROMPTS.md`, no la sesión de la herramienta.

* **Continuación de tarde (sesión 22):** la otra sesión volvió a trabajar entre 12:37 y 13:54 (ver Incidencia 2) y
  dejó el spec TDOP y los CSV sin subir. Esta sesión **verificó** ese trabajo antes de tocar nada — regen
  `pairwise.py` byte a byte igual, 125 casos en 7 tablas con 13 estados, coherentes con el código real— y lo subió
  tal cual en **`a81b85b`**. Con eso el árbol vuelve a estar idéntico al remoto y **la parte b queda lanzada**: a
  continuación `Cdu19MatrizTdopTest`, proyección de TC humanos, informe de spike, wiki y PR borrador.

**Trabajo de la sesión 23 (15/11/2026, tarde-noche · Iter6/CFG-06 parte b terminada salvo wiki y PR — Prompt 23):**
Ismael autorizó a seguir en solitario («arregla lo que sea necesario para asegurar la integridad del proyecto»), así
que esta sesión cierra la parte b del DoD de la **#44** y deja la fusión, el tag y el cierre para Ismael (D29).

* **Matriz TDOP en verde**: `Cdu19MatrizTdopTest` (patrón `Cdu12MatrizTdopTest`) lee los CSV de
  `iso2-integracion/src/test/resources/tdop/` con `@SpringBootTest(classes = …ConfigTdopWeb.class)` +
  `@AutoConfigureMockMvc` sobre un `@SpringBootConfiguration` interno con `@ComponentScan` del paquete
  `es.uclm.iso2.integracion` (sin starter de seguridad: así no aparecen 401/403 falsos). **126 casos, 0 fallos**, y
  el módulo entero **203 pruebas en 10 clases, 0 fallos, 0 errores, 0 omitidos**. Commit `c104d78`.
* **Crecida de 125 a 126 casos (criterio E10)**: con segmento vacío, `/api/integracion/sistemas//ping` devuelve
  **404 sin `ApiError`** (lo resuelve Spring, no el controlador), así que E10 se reclasifica a `RUTA_INEXISTENTE` y
  se añade la fila nueva **E17** (alias `ESPACIOS`, `"   "` → `SISTEMA_NO_ENCONTRADO`). La spec y los CSV se
  **regeneran con `tools/pairwise.py`**, no a mano: los ids son posicionales, por eso E17 queda al final. Subido en
  `c104d78`.
* **Trabajo forense sobre las sesiones anteriores**: el «`mvn test` de 90 casos con 7 fallos» que se citaba **nunca
  existió** (no había `Cdu19MatrizTdopTest` ni CSV en Git). Los hallazgos, en cambio, sí eran reales y se confirmaron
  leyendo el código: `AdaptadorSimulado.proteger()` mira la **política antes que la petición** (503 gana a
  400/401/404) e `IntegracionServicio.sincronizarHistoriaClinica()` llama a `hce.buscarHistoria()` **antes** que a
  sus `snapshots`, así que con HCE caído no se sirve la copia local (B04/B05 cierran en 503). Quedan escritos como
  **H1..H15** en el informe.
* **Cobertura real corregida**: **96,5 % de líneas (361/374) y 78,4 % de ramas**, puerta `jacoco.line.min=0.70` en
  verde, medida con `mvn -B -ntp -pl iso2-integracion verify`. El **88,2 % que venía en `STATUS.md` estaba caducado**:
  `report`/`check` están enlazados a `verify` en el pom padre, así que tras un `mvn test` el `jacoco.csv` no se
  actualiza. Corregido en este fichero y en el informe. Sin cubrir: `IntegracionProperties.SistemaOverride` (0/1) y
  `HuellaContenido` (2/4); ramas débiles: `IntegracionProperties` 11/30.
* **Reactor revalidado**: `mvn -B -ntp verify` sobre `2388b7e` = **BUILD SUCCESS, 839 tests** en 14 módulos (13 con
  tests), **0 fallos, 0 errores, 0 omitidos** y **13 puertas JaCoCo verdes**.
* **TC humanos**: `docs/tc/cdu19-tc-humanos.md` proyectado **tal cual** con `tools/tc_plantillas.py` (9 casos
  TC19-01…09 + el bloque de 126 filas), en `ec7f228`. Para ello se parcheó la herramienta de forma acotada
  (`OPERACIONES_EXTRA["iso2-integracion"]`, petición derivada de `metodo`+`ruta`, `previo` ignora `-`, y solo las
  tablas **sin `factores`** —las B y G de la CDU19— proyectan sus columnas como pares) para **no generar drift** en
  las plantillas ya ejecutadas de iter01 e iter05.
* **Informe de spike** en `docs/informes/spike-cdu19.md` (carpeta nueva), commit `2388b7e`: veredicto por
  integración, decisión **FHIR STU3 en JSON sobre la REST existente** (la huella SHA-256 canónica da la idempotencia),
  **HL7v2/MLLP descartado**, PACS encajado hacia **DICOMweb (`qido-rs`/`wado-rs`)** y recomendación de **no escribir
  un visor DICOM propio en Iter16**; **15 hallazgos**, deudas **D-01..D-07** (Deuda nº 24 de este fichero) y riesgos
  para R1. También `86f68ff` corrigiendo modos y acciones reales en la documentación de arranque.
* **Corrección de fondo a lo que decía la issue #44**: el RBAC real de los 7 endpoints es **solo rol
  `ADMINISTRADOR`**; la pareja `INTEGRACION_LEER/ESCRIBIR` nunca existió. Anotado en el bloque de iteración en curso
  y en la sesión 22.
* **Wiki publicada (D32)**: página nueva **`Iter6-CDU19-integracion`** en el repo `PrISO2-25-26.wiki`, rama `master`,
  merge **`9b7f513`** (6 ficheros, +204): componentes y versiones, los 4 diagramas con sus PNG en
  `images/iter06-cdu19-*`, tabla **API/RBAC real** de los 7 endpoints, clases con enlace a Javadoc **o al fuente**
  mientras el #45 no esté fusionado (las fichas de `iso2-integracion` aún dan 404 en `develop`), sección **Testing**
  con la **clase probada `Cdu19MatrizTdopTest`**, las tablas de la matriz TDOP, las puertas, los hallazgos H1/H2 y
  las deudas D-01..D-07; y `Home.md` con la fila de Iter6. Antes de escribir hubo que
  `merge --ff-only origin/master` porque el clon iba 5 commits por detrás (ver **Incidencia 3**). Justo después,
  **`8aa524b`** quita de la cabecera de la página el conteo de commits («14 commits, tip `a470584`»), un dato que
  envejece solo y que solo tiene sentido leer en la lista del propio PR.
* **Forense del estado de ramas (corrección de `STATUS.md`)**: `git ls-remote --heads origin` deja solo `develop`
  (`cbb1901`), `main` (`d12388b`), `docs/config-opencode` (`2a2f0a8`) y la rama de la Iter6. **`feature/iter05-cdu12-citas`
  ya no existe en el remoto**: la rama se borró el **15/11 a las 08:52** (`DeleteEvent`, al fusionar el PR #43) y
  tampoco está en local, así que su cierre ya estaba hecho y el pendiente de higiene pasa a **borrar la rama de la
  Iter6 después de fusionar el #45** (Deuda 23 reescrita con ese criterio). Además **`9457972` no es un commit de este
  repo**: `git cat-file` lo rechaza y la API de GitHub lo devuelve 422, y los dos commits reales del cierre de la Iter5
  son `b912554` y `5771a38`, ambos contenidos en `develop`. El dato falso venía de un `git branch -r` **sin `fetch`
  previo**, y la corrección se anota sin reescribir lo que ya se había registrado.
* **Riesgo activo y cómo se mitigó**: el árbol se limpia a veces y **solo sobrevive lo commiteado y pusheado**. Se
  cambió el orden de trabajo a **escribir → verificar en disco → `git add` inmediato → commit → push sin demora**, y
  las herramientas de recuento se guardaron en el temp (`reactor_verify.log`, `reactor_tally.py`, `cobertura2.py`,
  `jacoco_integracion.py`). **Causa encontrada en la sesión 23**: son las **snapshots** de OpenCode, que guardan el
  estado del árbol en un repo Git interno (`%USERPROFILE%\.local\share\opencode\snapshot`) y al hacer `undo`/`Revert`
  **borran el fichero creado por los pasos revertidos si en la frontera no existía** —exactamente los ficheros nuevos
  aún sin `git add`. La llave para desactivarlo es `"snapshots": false` en la config; queda como **Deuda nº 25**
  porque es un cambio en la configuración global de la máquina y no lo aplica el agente.
* **Cierre de jornada 15/11/2026 (noche):** rama `feature/iter06-cdu19-integracion` en **`a470584`** = remoto, con los
  14 commits `db075c0`, `687c624`, `7a03e0c`, `9ca712f`, `a22e654`, `f5411c1`, `a81b85b`, `a86d1f4`, `a532992`,
  `c104d78`, `ec7f228`, `86f68ff`, `2388b7e` y `a470584` sobre `cbb1901`. [Aviso de la propia sesión 23: aquí se había
  apuntado `71ce94b`, que en realidad es el commit de Iter4/CDU06 ya presente en `develop` y **no** pertenece a esta
  rama.] `origin/develop` sigue en `cbb1901` y `origin/main` en `d12388b`, **sin tocar**. **PR borrador abierto: #45**
  (borrador, `Ref: #44`, sin `Closes`). [Anotación de la misma sesión 23: **la wiki `Iter6-CDU19-integracion` quedó
  publicada justo después** con el merge `9b7f513` del repo wiki, así que de la parte b ya no queda nada del agente;
  tras `a470584` solo han entrado los docs de registro.] Pendientes: los **TC humanos de CDU19** (Ismael, D28),
  **fusionar el #45** con su tag `cfg-iter06` y la revalidación **D37**.

**Trabajo de la sesión 24 (15/11/2026, noche · cierre material de la Iter6/CFG-06 por el agente, con D38, y
preparación de la Iter7 — Prompt 24):**
Ismael da por cerrada la Iter6 y encarga al agente lo que quedaba —«**haz tú lo que queda**»—, lo que cambia el
reparto de la D29 para el cierre operativo: queda registrado como **D38**. La **D28 no se toca**: la validación
del punto de vista del usuario sigue siendo de Ismael, y el hito `v0.2.0-alpha` (`release/R1` → `main`) también
(D30).

* **Fusión y limpieza**: **PR #45 fusionado** a `develop` (merge **`0ef4f1a`**, CI `construir` en verde) y rama
  `feature/iter06-cdu19-integracion` **borrada** en remoto y local → **Deuda nº 23 cerrada**. Wiki
  `Iter6-CDU19-integracion` publicada (merge `9b7f513`).
* **TC humanos de CDU19 ejecutados por el agente contra el servidor real** (los 9 TC19-01…09): servidor
  levantado con `mvn -B -ntp spring-boot:run -pl iso2-server-app` sobre H2 de fichero y login real por
  `POST /api/auth/login`. Resultados fijados en la sección **`## Ejecución real de estos TC (15/11/2026)`** de
  `docs/tc/cdu19-tc-humanos.md`, puesta **antes** del bloque `<!-- TDOP:BEGIN -->` para que regenerar la
  proyección D33 siga siendo un no-op (comprobado).
* **Evidencia HTTP versionada** (nueva carpeta `docs/tc/evidencia/cdu19/`): `README.md`, 5 scripts Python
  re-ejecutables y 5 JSON crudos con petición/respuesta reales y el **JWT redactado**. Dos hallazgos de montaje
  salen de ahí: `POST /api/integracion/sso/login` espera **`contrasena`** (con `password` da 400), y las rutas sin
  mapear contestan **401 `AUTENTICACION_REQUERIDA` con cuerpo `ApiError` incluso con token válido** (**D-09**).
* **La cadena de auditoría probada de verdad** (TC19-02 y TC19-06): `auditoria-cdu19.json` guarda los eslabones
  completos —**39 tras tres sincronizaciones y 41 tras el rotativo**—, `cadenaIntegra: true`, eslabón #1
  `SSO_LOGIN` con génesis de 64 ceros y las ramas `OK_REUTILIZADA`/`OK_ACTUALIZADA` del idempotente. El
  `/api/citas/huecos?desde=no-es-fecha` montado da **400 `DATOS_INVALIDOS`** mientras la matriz aislada de CDU12
  espera 500 → deuda **D-10**.
* **Métricas de cierre medidas, no repetidas** (scripts en el temp sobre los `target/` del `verify` de `2388b7e`):
  reactor **839 pruebas / 0 saltados / 0 fallos**; JaCoCo de reactor **1.744 de 2.005 líneas (87,0 %) y 512 de 675
  ramas (75,9 %)**; el peor módulo por ramas deja de ser `iso2-integracion` y pasa a ser **`iso2-common`: 0 de 46
  ramas y 0 de 139 líneas, sin puerta en su `pom.xml`** → deuda **D-08**. LOC **13.343 en 181 ficheros de
  `src/main`** y **10.646 en 63 de `src/test`** (ratio **0,80**). CI: **83 ejecuciones = 80 `success` + 3
  `failure`**, y las 3 son la puerta JaCoCo planificada de la Iter5. Matriz CDU19 contada fila a fila: **126**
  (A21 + B7 + C15 + D12 + E17 + F40 + G14).
* **D37 revalidada en `TASADEFECTOS.md`** (§1-§7 completos): libro con **DEF-14** (F4, el
  `@ConfigurationProperties` ausente en `IntegracionProperties`, nacido en `7a03e0c` y corregido en `f5411c1`
  cuatro commits después de la misma rama → **nunca llegó a `develop` con CI verde**, roza el umbral del falso
  verde sin cruzarlo) y **DEF-15** (F2, el vaciado de `STATUS.md`/`PROMPTS.md`, imputado al tramo Iter6 porque el
  daño nunca se commiteó). Fila Iter6: **22 hallazgos · 0 F1 · 1 F2 · 0 F3 · 1 F4 · 2 intraiter · 9,1 por 100
  commits**; total **129 · 11,6**. **Veredicto D37: el modelo no se cambia**; se tapa el hueco con la prueba de
  arranque con YAML (**D-02**) y la puerta JaCoCo de `iso2-common` (**D-08**). Decidir un modelo más potente en la
  Iter7 es decisión de Ismael.
* **Registros al día**: deudas **21, 22 y 23 cerradas**; **24** reescrita con **D-01..D-10**; **25** sigue
  pendiente (Ismael decide no desactivar las *snapshots*); **26** actualizada porque el #45 ya está fusionado y el
  *worktree* del agente B puede crearse. Apartado nuevo **2-ter** con las condiciones de la Iter7 y bloque con los
  **5 primeros pasos** al principio del punto de continuación.
* **Cómo se materializa el cierre**: todo el cierre va en **un solo commit de docs** en la rama
  `docs/iter06-cierre` → PR **#46** a `develop` → fusión → **tag anotado `cfg-iter06` sobre ese merge de cierre**
  (patrón `cfg-iter05` sobre `cbb1901`) → **cerrar la #44** con su DoD y los datos D37 → borrar la rama. Con ese
  orden el conte `cfg-iter05..cfg-iter06` cuadra con los **22** ya escritos en `TASADEFECTOS.md`.
* **Turno siguiente — Iter7 / CFG-07 · CDU14 «listas de espera»** (22 h, ventana 23-27/11). El pedido nuevo de
  Ismael cambia la forma de trabajar en pruebas: **los TC los diseña el agente** con **clases de equivalencia +
  conjetura de errores + teoría pesada de valores límite + *pairwise***, y el diseño se documenta en un **informe
  con valor pedagógico** (`docs/informes/diseno-conjuntos-prueba.md`), no solo el resultado.


---

## Punto de continuación — 17/11/2026: CFG-07 abierta y matriz TDOP de CDU14 a 95/253

*(Escrito al cerrar la sesión del 17/11; el trabajo sigue en otro equipo, así que este bloque es el relevo completo.)*

* **Dónde queda el repo**: rama `feature/iter07-cdu14-listas-espera` en **`f1c3764`**, volcada en `origin` y con el árbol de trabajo **limpio** (no hay nada sin commitear). **CFG-06 cerrada**: el reactor de 14 módulos está verde con **238 pruebas** en `iso2-citas`. **CFG-07 abierta**: de las **253** filas de los 8 CSV de la matriz están ejecutadas y verdes **95** (tabla A · cola: 40, y tabla B · alta: 55). El **#47** sigue abierto y solo lo cierra Ismael (**D29**), igual que la PR.
* **Lo que ya tiene `Cdu14MatrizTdopTest.java`** (624 líneas, en HEAD): montura `@SpringBootTest(classes = ConfigTdopWeb.class)` + `@AutoConfigureMockMvc` + `@DirtiesContext`, mundo limpio en `@BeforeEach`, lectura de los CSV y de `cdu14-esperados.csv`, motor de siembra **por la propia API HTTP** (`alta`, `cambiarPrioridad`, `retirar`, `asignar`, `colaTres`, `semEstado`), verificador del canon de orden **D11** y los dos handlers hechos: `colaDesdeMatriz` y `altaDesdeMatriz`.
* **Lo que falta, en este orden**:
  1. Los seis handlers restantes con su `filasXxx`: `reubicacionDesdeMatriz` (D · 27), `secuenciaDesdeMatriz` (E · 12), `operacionDesdeMatriz` (C · 30), `asignarDesdeMatriz` (F · 64), `auditoriaDesdeMatriz` (G · 12) y `conjeturaDesdeMatriz` (H · 13), más los helpers `objetivoDe`, `esperaIdDe`, `inicioDe`, `duracionDe`, `motivoDe`, `prioridadPedida`, `setupValor`, `cadenaDe`, `multiset`, `verificarColaEsperada`, `verificarVerificable`, la siembra del hueco (`SEM-HUECO-DEST:EXACTO|SOLAPE|FANTASMA`, con la FANTASMA creada y **cancelada** por HTTP) y los ataques `SEM-TAMPER` (hooks `setMotivoParaPruebas` / `setHashParaPruebas` y `AuditoriaEsperaRepositorio`).
  2. Corregir tres semillas del `switch` de setup: `SEMILLA-COLA-EMPALTE-BC` pasa a ser exactamente `colaTres()` (D10 y D11), `SEMILLA-COLA-CON-RETIRO` es `colaTres()` más retirar a `A`, y `SEMILLA-CON-RETIRO` (G03, G10, G11) es **una sola** entrada con alta + reubicación + retiro; falta `SEMILLA-CITA-OCUPA` (G08).
  3. Añadir en `ListaEsperaManejadorErrores` un `@ExceptionHandler` de `HttpMediaTypeNotSupportedException` que devuelva **415** sin cuerpo (**TC14-H02**): hoy se lo lleva el *catch-all* y responde 500. El **405** de H10 y H11 ya lo da Spring y no se toca.
  4. Desajuste conocido de la spec: **E02, E03 y E12** fijan `esperadoStatus: OK` (200) en secuencias que terminan en un alta que responde **201**, así que tocarán rojo. Vía **D33**: se corrige `tdop-cdu14-espec.json` a `CREADO` y se regeran los CSV con `python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources iso2-citas/src/test/resources/tdop`; **nunca** se edita la matriz a mano ni se adapta el test.
  5. Ejecutar por tramos (`-Dtest=Cdu14MatrizTdopTest#reubicacionDesdeMatriz` y compañía, siempre con `-am`), luego `mvn -o -ntp -pl iso2-citas -am test` con las 253 + 238, y al final el reactor. Con `@DirtiesContext` por método, la matriz sola cuesta **20-30 min por ejecución**: limitar a cuatro o cinco corridas completas.
  6. Cierre de la iteración: página **Iter7/CDU14** en la wiki (`PrISO2-25-26.wiki.git`, rama `master`, con los 4 PNG de `docs/modelo/iter07` copiados a `images/`), revalidación de **D37** en `TASADEFECTOS.md`, actualización de `STATUS.md` y `PROMPTS.md`, y tag anotado `cfg-iter07`.
* **Pérdida registrada**: los tests unitarios de cola escritos el 17/11 y una versión larga del ejecutor **nunca llegaron a commit**, y `git fsck` solo devuelve objetos de Iter3/Iter5/Iter6, así que son irrecuperables. La cifra real de pruebas de `iso2-citas` es **238**, no las que anunciaban los apuntes de esa sesión. Queda como regla de trabajo: escribir trozo → comprobar en disco → `git add` → commit → **push inmediato**, y **nunca** `git clean`, `git reset --hard` ni `git restore .`, que son las únicas operaciones que borran el trabajo sin dejar rastro en el reflog.
* **Relevo al equipo nuevo**: en `docs/test/iter07/notas-ejecutor/` están los volcados derivados de las tablas C, D, E, F, G y H y de los factores y reglas de F, para no releer la spec de 105 KB; en `docs/test/iter07/borrador-ejecutor/` quedan las dos partes del borrador del ejecutor. El equipo nuevo necesita su **`~/.m2` relleno**, porque todo se compila y se prueba con `mvn -o` (sin descarga).
