# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-08 (sesión 16 · Iter4 · CDU06 **fusionada** (PR #31 → merge `a3fe109`) y
cerrada con el tag `cfg-iter04`; nuevas directrices de Ismael: **testing con clases de equivalencia, valores
límite, conjetura de errores y pairwise** (D33) y **persistencia MySQL con Docker + DAO + patrón Agente**
(D34, aceptada y redactada; implementación pendiente). Sesión 15: PRs #31 y #32. Prompts 13 y 14)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔ **Completada, etiquetada y publicada** — PR #7 fusionado a `develop` (`641e72d`), tag `cfg-iter00`; **R0 publicada en `main`**: PR #19 fusionado (merge `e165622`) con el tag anotado **`v0.1.0`** (regla **D30**: release desde `release/R0`) |
| Elaboración (Iter1–6) | 🟡 En curso — Iter1 (CDU16) **cerrada en ramas** (`df09bca`, tag `cfg-iter01`); Iter2 (CDU17) **cerrada en ramas** (merge `870524e`, #13 cerrada, tag `cfg-iter02`); Iter3 (CDU13) **cerrada en ramas** (PR #18 fusionado, merge `b072b34`, #15 cerrada, tag `cfg-iter03`); **Iter4 (CDU06) fusionada** (PR #31 → merge `a3fe109`, CI verde) y **cerrada con tag `cfg-iter04`**; quedan los **TC humanos** de Iter1–Iter4 (D28: plantillas de CDU16/17/13 **y la nueva de CDU06** en `docs/tc/`; Ismael las rellena y ejecuta) y las **evidencias de modelado** ya cubiertas con PlantUML (`.puml` + `.png` versionados, D31; StarUML descartado) |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** **Iter4 · CDU06 solicitudes (CFG-04) cerrada el 08/11** (sesiones 14, 15 y 16): rama
`feature/iter04-cdu06-solicitudes` con 4 commits (diseño PlantUML `cce3af2` → servidor `71ce94b` → fix
doclint `7f85678` → cliente `b4c4e27`) **fusionada por Ismael en `develop` con el PR #31 (merge `a3fe109`)**
y el PR de docs **#32** (merge `3b02718`). Tag anotado **`cfg-iter04`** creado sobre `a3fe109` y página de
wiki `Iter4-CDU06-solicitudes` publicada. La issue **#30 ya la había cerrado Ismael** (19:01, antes del
merge: D29 cumplida por adelantado). **La siguiente es Iter5 · CDU12 citas (CFG-05)**, que arranca desde
`develop` = `3b02718` **con dos cambios de enfoque pedidos por Ismael**: (1) **nuevo método de pruebas**
(D33: clases de equivalencia + valores límite + conjetura de errores, compuesto con **pairwise**, ejecutado
con **JUnit/Maven** y usando **esos mismos casos** en las plantillas de TC; se empieza **revisando y
ampliando las pruebas de la Iter1**); (2) **nueva capa de persistencia MySQL** con Docker (MySQL +
phpMyAdmin) y los patrones **DAO** y **Agente** (`AgenteMySQL`) — **D34** ya aceptada y redactada, con la
implementación pendiente y cinco puntos por concretar con Ismael (ver Deuda 19). Iter2 e Iter3 cerradas también con sus tags (`cfg-iter02` → `870524e`, `cfg-iter03` →
`b072b34`). Pendientes que mantienen abiertas CFG-01/02/03/04: los **TC humanos** (D28) y, en CFG-01/02/03,
la **regeneración de sus casos con el método D33**; las evidencias de modelado quedaron resueltas con
PlantUML (D31).

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
  **PR #32** (`docs/cierre-iter04-sesiones-14-15` → `develop`, merge **`3b02718`**): **tip actual de
  `develop` = `3b02718`**, CI `construir` en verde en ambos. **Rama `feature/iter04-cdu06-solicitudes`
  borrada** (remoto y local) tras el merge.
- **Release R0 publicada 08/11:** PR #19 (`release/R0` → `main`) fusionado por Ismael con merge commit
  `e165622` (padres `1245138` + `641e72d`) y tag anotado **`v0.1.0`** creado sobre ese merge: CFG-00 ya
  tiene las dos etiquetas de su fila del mapa (`v0.1.0;cfg-iter00`). Issue #20 **cerrada a mano por
  Ismael** (D29). `main` vuelve a contener solo contenido de release etiquetada y no aporta nada que no
  esté en `develop`: `git diff --stat origin/develop...origin/main` queda vacío, así que la publicación de
  R1 no arrastrará restos del PR #17. Rama `release/R0` conservada en remoto (como las demás ramas
  fusionadas).
- **Open / en curso:** **no hay PRs abiertos** tras fusionarse **#31** (código de CDU06, merge `a3fe109`) y
  **#32** (docs de las sesiones 14-15, merge `3b02718`). Tampoco hay issues abiertas. En curso de trabajo:
  la rama `docs/cierre-iter04-material` (esta actualización de `STATUS.md`/`PROMPTS.md`/`DECISIONS.md` + la
  plantilla nueva `docs/tc/cdu06-tc-humanos.md`).
- **Javadoc publicado en GitHub Pages (D32, sesión 14):** workflow nuevo `.github/workflows/javadoc.yml`
  (push a `develop` + `workflow_dispatch`) que ejecuta `mvn -B -ntp javadoc:aggregate` y publica
  `target/reports/apidocs`. URL base **`https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/`**; solo la
  última compilación, sin instantáneas por tag. Issue #27 **cerrada a mano por Ismael** (19:48). También se
  corrigió en `ci.yml` el path del artefacto de Javadoc (`target/site/apidocs` → `target/reports/apidocs`).
- **Wiki del proyecto ampliada (sesión 14):** merge `90d5b26` en la rama `master` del repo wiki añade a las
  cuatro páginas por CFG la **ficha Javadoc enlazada de cada clase** y una **sección Testing**.
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
- **Nueva desviación en `main` (08/11 19:01, pendiente de decidir):** el **PR #29** (`develop` → `main`) se
  fusionó con merge commit **`d12388b`** (padres `e165622` = `v0.1.0` + `c8fe2d7`). Contradice **D30** (`main`
  solo recibe ramas `release/RN`) y **no lleva etiqueta de release**: `main` quedó por delante de `v0.1.0`
  con contenido de `develop` sin CFG cerrada por release. Opciones planteadas a Ismael: (a) etiquetar
  `d12388b` (p. ej. `v0.1.1`) normalizando lo ya hecho, (b) crear la rama `release/R1` sobre `cfg-iter04` y
  publicarla al cerrar CFG-05 volviendo a D30, o (c) revertir `main` a `e165622`. **El agente no toca `main`**
  (D30) y espera indicación.
- **Issues cerradas a mano (D29):** #6 (Iter0/CFG-00), #9 (Iter1/CDU16), #13 (Iter2/CDU17),
  #15 (Iter3/CDU13), #20 (publicar R0, tras el merge `e165622` + tag `v0.1.0`), **#27 (publicar el Javadoc
  en Pages, 19:48, tras el merge `c8fe2d7` del PR #28)** y **#30 (Iter4/CDU06, 19:01)**.
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

## Deuda / pendientes inmediatos

1. ~~Confirmar decisión D18~~ → D18 **revisada por D31** (08/11): fuentes PlantUML + `.png` renderizados con `tools/plantuml.jar`, ambos versionados e incrustados en los README de `docs/modelo/iterXX/`; la exportación a StarUML queda descartada.
2. ~~Instrucciones de código de Ismael~~ → Iter0/Iter1 ejecutados con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #7 o en el de Iter1.
3. **TC humanos de CDU16** (D28): plantilla creada por el agente en `docs/tc/cdu16-tc-humanos.md`
   (puertas y tipos prefijados desde `SOFTWAREQUALITY.md`); **Ismael rellena pasos/resultados y ejecuta**
   sobre el código fusionado (base `df09bca` = tag `cfg-iter01`, aunque puede ejecutarse contra `develop`).
   Son criterio de cierre de Iter1/CFG-01. ~~issue #21~~ cerrada como contenedor; seguimiento aquí.
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
    `SEM-RX-TORAX` y `SEM-RM-RODILLA`); **Ismael la rellena y ejecuta**. Siguen pendientes las de
    **CDU16/CDU17/CDU13** (#21), que él rellena y ejecuta.
16. ~~Publicar el Javadoc en GitHub Pages (issue #27)~~ → **Hecho 08/11, issue cerrada a mano**: D32,
    workflow `javadoc.yml` desde `develop`, URL `…/apidocs/` verificada y wiki enlazando las fichas.
    Sin instantáneas por tag: si una wiki de iteración futura necesita la ficha exacta de esa CFG, se
    enlaza a `develop` (siempre la última compilación).
17. **Nueva desviación de D30 en `main` (pendiente de decisión de Ismael):** el PR #29 fusionó `develop` →
    `main` (merge `d12388b`, 08/11 19:01) sin rama `release/RN` ni etiqueta. `main` quedó por delante de
    `v0.1.0`. Se le ha planteado: etiquetar `d12388b`, volver a D30 con `release/R1` sobre `cfg-iter04`, o
    revertir `main` a `e165622`. El agente no actúa sobre `main` sin visto bueno.
18. **Rediseño de las pruebas con el método D33 (abierto, empieza por Iter1):** construir la matriz de
    **clases de equivalencia + valores límite + conjetura de errores** de `iso2-catalogo` (Iter1/CDU16),
    composarla con **pairwise**, implementar los JUnit con **JavaDoc** y `@DisplayName` con el id del caso,
    versionar la evidencia en `docs/test/iter01/`, **regenerar `docs/tc/cdu16-tc-humanos.md` con esos mismos
    casos** y actualizar la página de wiki de la Iter1 **con la clase probada**. Después se extiende a
    Iter2 (CDU17) e Iter3 (CDU13). Huecos ya detectados en CDU16 que hay que cubrir o decidir: `codigo` de
    catálogo más largo de 40 (revienta contra la BD en vez de devolver 400), `pagina` no numérico en
    `GET /api/catalogo/pruebas` (500) y la discrepancia entre el test de cliente que espera
    `CODIGO_EN_USO` y el servidor que emite `CODIGO_DUPLICADO`.
19. **Nueva capa de persistencia MySQL — ADR D34 ya redactada, implementación pendiente:** desplegar
    **Docker** con **MySQL + phpMyAdmin** más el servidor y el cliente; base `iso2` con usuario de
    entrada/salida `gestoroperaciones`
    (password `Iso2Curso25-26`); acceso con **patrón DAO** y conexión con **patrón Agente** particularizado
    en **`AgenteMySQL`** (permitir varios SGBD). Falta decidir si H2 sigue como perfil de test o se sustituye
    por MySQL en contenedor (Testcontainers) y si Flyway mantiene migraciones duales.

## Punto exacto de continuación para la próxima sesión

**Iter4 · CDU06 solicitudes (CFG-04) CERRADA el 08/11 (sesiones 15 y 16, Prompt 13):** rama
`feature/iter04-cdu06-solicitudes` desde `develop` = `c8fe2d7` con 4 commits — `cce3af2` (diseño PlantUML
**previo al código**), `71ce94b` (servidor), `7f85678` (fix doclint en un `record`) y `b4c4e27` (cliente) —
**fusionada por Ismael con el PR #31 (merge `a3fe109`)**; el PR de docs **#32** se fusionó con merge
`3b02718` (tip de `develop`). **Tag anotado `cfg-iter04` creado sobre `a3fe109` y subido**; rama feature
borrada en remoto y local; wiki con la página **`Iter4-CDU06-solicitudes`** (merge `5207bae` del repo wiki)
y plantilla nueva **`docs/tc/cdu06-tc-humanos.md`** (14 casos). Issue **#30 cerrada a mano por Ismael a las
19:01**, antes del merge (D29 por adelantado). Build reactor verde (`mvn -B -ntp verify javadoc:javadoc`):
**229 tests** (en Iter3 eran 172; `iso2-solicitudes` 1→24, server-app 46→60, client-app 37→57) y **JaCoCo de
`iso2-solicitudes` 87,2 %** (109/125 líneas, 32/50 ramas) ≥ 85 %. Puertas técnicas CFG-04 cumplidas; CFG-04
queda **abierta solo por los TC humanos de CDU06** (D28), que ejecuta Ismael.

**Hecho también en la sesión 14 (Prompt 12):** ADR **D32** (Javadoc agregado en GitHub Pages desde
`develop`, `javadoc.yml` + fix del path del artefacto en `ci.yml`), **PR #28 fusionado** (commit `00f93e9`,
merge `c8fe2d7`) y URL `https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/` verificada; issue **#27
cerrada a mano** (19:48). La wiki ganó **enlaces a la ficha Javadoc de cada clase** y una **sección
Testing** en las cuatro páginas por CFG (merge `90d5b26`). Al final de la sesión se creó la issue **#30**
con el prompt de Iter4 en el cuerpo. Durante la sesión 13 se fusionaron **PR #25** (merge `1e4e4a6`) y
**PR #26** (merge `d305763`).

**Flujo diseño-primero (sin ADR, queda como convención):** en Iter4 el modelo PlantUML de
`docs/modelo/iter04/` se escribió y commiteó **antes** del código (`cce3af2`, 19:04) y el código se escribió
contra ese diseño; si un test o el compilador cambia una firma o un invariante, se edita primero el `.puml`
y se sube junto al cambio. Está descrito en el cuerpo de #30 y en el PR #31, y no tiene ADR propio.

**Nueva desviación detectada en `main` (08/11 19:01, pendiente de decidir):** el **PR #29** fusionó
`develop` → `main` con merge commit **`d12388b`** (padres `e165622` = `v0.1.0` + `c8fe2d7`) **sin rama
`release/RN` y sin etiqueta de release**, en contra de **D30**. `main` quedó por delante de `v0.1.0` con
contenido de `develop` no cerrado por release. Opciones planteadas a Ismael: (a) etiquetar `d12388b`
(p. ej. `v0.1.1`) normalizando lo hecho, (b) volver a D30 creando `release/R1` sobre `cfg-iter04` y
publicándola al cerrar CFG-05, o (c) revertir `main` a `e165622`. **El agente no toca `main`**: espera
indicación.

**Siguiente sesión — Iter5 (CFG-05 · CDU12 citas)**, desde `develop` = `3b02718` (Iter4 ya fusionada y tag
`cfg-iter04` puesto): rama `feature/iter05-cdu12-citas`, issue nueva con el prompt ejecutable en el cuerpo
(la abre Ismael, D29) y `Ref: #<issue>` en cada commit, `iso2.version` e `iso2-citas` a **1.1.0**, apps
**0.1.5-SNAPSHOT**, migración Flyway `V1.5.0__…` y modelo en `docs/modelo/iter05/` (4 `.puml` + `.png`
incrustados en su README, D31) **antes de escribir código**. Puertas de CFG-05 en
`docs/mapa-configuraciones.csv`: **TC de concurrencia de hueco** (doble reserva sobre el mismo hueco → una
sola cita) y **rollback transaccional**; fecha objetivo 20/11/2026. Antes de nada: leer CONTEXT.md →
DECISIONS.md (D28-D32 y el nuevo **D33**) → STATUS.md → PLAN.md §4 → matriz CFG-05. Después de Iter5 queda
**Iter6 · CDU19 integración (CFG-06)** con el hito **`v0.2.0-alpha`**.

**Antes de programar CDU12 hay que aplicar los dos cambios de enfoque que pidió Ismael el 08/11:**

1. **Nueva estrategia de pruebas (ADR D33):** los casos se diseñan con **clases de equivalencia**, **teorema
del valor límite** y **conjetura de errores**, y se composan con **pairwise** para no explotar la combinatoria
de parámetros. **El mismo conjunto** alimenta los **JUnit que se ejecutan en Maven** y las **plantillas de TC
humanos**: la plantilla deja de ser una lista a mano y pasa a ser la proyección legible de la matriz. Los
ficheros de test llevan **JavaDoc** (clase probada, colaborador simulado, propiedad verificada) y cada método
se identifica con el id del caso (`TC16-xx` en `@DisplayName`). Se empieza **revisando y ampliando las
pruebas de la Iteración 1** (`iso2-catalogo`, CDU16) y luego se extiende a Iter2 (CDU17) e Iter3 (CDU13).
Evidencia versionada en `docs/test/iter01/` (matriz de clases de equivalencia + puntos límite + tabla de
pares) y wiki de iteración **identificando la clase probada** en cada fila.
2. **Nueva capa de persistencia MySQL (ADR D34 al implementarse):** **Docker** con **MySQL** (base `iso2`) +
**phpMyAdmin**, y también servidor y cliente; usuario de operaciones de entrada/salida **`gestoroperaciones`**
/ `Iso2Curso25-26` además del `root` (`Iso2Curso25-26`). Acceso a datos con **patrón DAO** y conexión con
**patrón Agente** particularizado en **`AgenteMySQL`**, dejando la puerta abierta a conectar otros SGBD.

**Huecos de CDU16 que el método D33 destapa** (arreglar o decidir, editando antes el `.puml` de iter01): un
`codigo` de catálogo de más de 40 caracteres revienta contra la BD en lugar de devolver 400; `pagina` no
numérico en `GET /api/catalogo/pruebas` produce 500 en vez de 400; y el test de cliente que espera
`CODIGO_EN_USO` no coincide con el código que emite el servidor, `CODIGO_DUPLICADO`.

**Pasos de Ismael pendientes (D29/D28/D30):** (1) **decidir la desviación de `main`** del PR #29 y si se abre
la release `R1`; (2) **rellenar y ejecutar los TC humanos** de CDU16/CDU17/CDU13 (**#21**) y los de CDU06
(plantilla ya creada, #30 cerrada) — es criterio de cierre de CFG-01/02/03/04; (3) **abrir las issues** del
trabajo de pruebas (rediseño D33 de Iter1/2/3) y de la persistencia MySQL si quiere trazabilidad en GitHub, y
decidir si Iter5 se ejecuta con prompt de issue; (4) **decidir el alcance de MySQL**: si H2 sigue siendo el
perfil de test o se sustituye por MySQL en contenedor (Testcontainers), si Flyway mantiene migraciones duales
H2/MySQL o solo MySQL, y si las entidades JPA se conservan por encima del DAO o el DAO pasa a ser el acceso
primario; (5) revisar visualmente el **Kanban** (GitHub Project, #23: el token del agente no puede verlo).
#21/#22/#23/#27/#30 quedan cerradas: el seguimiento está en este fichero. El agente no fusiona PRs ni toca
`main` sin visto bueno explícito (D30) y **los TC humanos de la CDU los diseña y ejecuta Ismael** (D28) — con
D33 el agente **sí** genera los casos y las plantillas, pero sigue sin ejecutarlas.

**Pasos del agente después del merge de #31 (ya hechos en la sesión 16):** tag anotado `cfg-iter04` **creado
sobre `a3fe109`** (el merge del código CDU06, no el merge documental posterior `3b02718`, para que el tag
apunte a la configuración que superó la puerta de cobertura) y subido; rama `feature/iter04-cdu06-solicitudes`
borrada en remoto y local; y página de wiki **`Iter4-CDU06-solicitudes.md`** publicada en `master` del clon
del repo wiki con los 4 diagramas de `docs/modelo/iter04/`, la tabla de componentes enlazando su ficha
Javadoc de `…/apidocs/` (D32) y la sección Testing (merge `5207bae`, con fila nueva en `Home.md`).

**Cierre de jornada 08/11/2026 (fin sesión 16):** repo estable y todo subido. **Iter1-Iter4 fusionadas y
etiquetadas** (`cfg-iter00`…`cfg-iter04`) y **R0 publicada en `main`** (merge `e165622`, tag `v0.1.0`).
`develop` en `3b02718` (tras los merges `a3fe109` del PR #31 y `3b02718` del PR #32) con CI `construir` en
verde y **229 tests** en el reactor. Issues: ninguna abierta. Pendiente de Ismael: la desviación de `main`
(PR #29) y los **TC humanos** de CDU16/17/13/06. Pages funcionando en `…/apidocs/` y wiki al día hasta
**Iter4**. Reglas de referencia: **D28** (TC humanos los hace Ismael), **D29** (cierre manual de issues),
**D30** (solo `release/RN` → `main`), **D31** (modelado PlantUML: `.puml` + `.png` versionados), **D32**
(Javadoc en Pages desde `develop`), **D33** (pruebas por clases de equivalencia + valores límite + conjetura
de errores compuestas con pairwise, y **los mismos casos** en JUnit y en las plantillas de TC) y el **flujo
diseño-primero** (modelo antes que código). **D34** (MySQL + Docker + DAO + Agente) queda aceptada y redactada,
a la espera de que Ismael concrete los cinco puntos pendientes.
