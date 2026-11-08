# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-08 (sesión 9 · Iter2/CDU17 seguridad RBAC+JWT)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔ **Completada y etiquetada** — PR #7 fusionado a `develop` (`641e72d`), tag `cfg-iter00`; `v0.1.0` (R0 en `main`) pendiente del PR `develop`→`main` |
| Elaboración (Iter1–6) | 🟡 En curso — Iter1 (CDU16) **fusionada a `develop`** (`df09bca`, tag `cfg-iter01`); Iter2 (CDU17) en **PR #14** a la espera de fusión; quedan TC humanos (D28) y exports `.staruml` (D18) de ambas |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** Iter2 · CDU17 (CFG-02) — código terminado en `feature/iter02-cdu17-seguridad`
(commits `c8213ee`..`2f8090a`) con **PR #14** abierto a `develop`; pendiente de fusión por Ismael +
tag `cfg-iter02` + cierre manual de #13 (D28 humanos: TC de CDU17 y export `.staruml` de iter02).
**Siguiente iteración:** Iter3 · CDU13 agendas (`iso2-agendas` 1.1.0, CFG-03; apps 0.1.3-SNAPSHOT).

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · Git Flow: `main` + `develop` activas.
- **Fusionados:** PR #1 (plan PUD), PR #2 (calidad+config), PR #4 (`releaseplan`→`develop`),
  PR #5 (`feature/docs-prompt-log`), PR #8 (docs Iter0), **PR #7** (Iter0 código, merge `641e72d`),
  **PR #10** (Iter1/CDU16, merge `df09bca`), **PR #11** (docs sync cierre sesión 7) y **PR #12**
  (higiene CFG-00/01, merge `67ed627`). **Abierto y pendiente de fusión:** **PR #14** (Iter2/CDU17
  → `develop`), con issue #13 abierto hasta su cierre manual tras la fusión (D29).
- **Tags de configuración creados 08/11:** `cfg-iter00` → `641e72d` (CFG-00) y
  `cfg-iter01` → `df09bca` (CFG-01), anotados y subidos. `v0.1.0` (R0) se creará sobre `main`
  cuando se abra el PR nuevo `develop`→`main` (el PR #3 se cerró sin fusionar; `main` sigue en `1245138`).
- **Issues cerrados 08/11 a mano (D29):** #6 (Iter0/CFG-00) y #9 (Iter1/CDU16) — los `Closes #N`
  no operan con merges a `develop`.
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

## Deuda / pendientes inmediatos

1. ~~Confirmar decisión D18~~ → **Aceptada**: fuentes PlantUML versionadas en `docs/modelo/iterXX/` + export manual a `.staruml` (pasos en `docs/modelo/iter01/README.md`; el export lo hace Ismael).
2. ~~Instrucciones de código de Ismael~~ → Iter0/Iter1 ejecutados con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #7 o en el de Iter1.
3. **TC humanos de CDU16** (D28, sustituye al orden de D16): tabla de casos de prueba diseñada por Ismael sobre el código ya fusionado en `develop` (`df09bca` = tag `cfg-iter01`); son criterio de cierre de Iter1/CFG-01.
4. Exportar `docs/modelo/iter01/staruml/iso2-iter01.staruml` en StarUML (manual, D18) y subirlo como artefacto de CFG-01.
5. Crear GitHub Project (Kanban) y la wiki de GitHub enlazando README/CONTEXT/PLAN/STATUS/DECISIONS/PROMPTS.
6. ~~Tags `cfg-iter00`/`cfg-iter01` y cierre manual de #6/#9~~ → **Hecho 08/11** (ver Estado del repositorio). Queda `v0.1.0` para cuando `develop`→`main` publique R0.
7. **TC humanos de CDU17** (D28): tabla de casos de prueba diseñada por Ismael sobre el código del PR #14 tras su fusión; son criterio de cierre de Iter2/CFG-02.
8. Exportar `docs/modelo/iter02/staruml/iso2-iter02.staruml` en StarUML (manual, D18) y subirlo como artefacto de CFG-02. Pasos en `docs/modelo/iter02/README.md`.

## Punto exacto de continuación para la próxima sesión

**Iter2 · CDU17 terminada 08/11 (sesión 9):** rama `feature/iter02-cdu17-seguridad` subida a
remoto (commits `c8213ee`..`2f8090a` + docs sync, base `develop` `67ed627`) con **PR #14** abierto
a `develop` y CI local verde (`mvn -B -ntp verify javadoc:javadoc` 14/14). Nada pendiente del lado
del agente hasta la fusión.

Pasos de Ismael al cerrar Iter2 (D29/D28/D18): **fusionar PR #14**, crear el tag anotado
`cfg-iter02` sobre el merge, cerrar **#13** a mano, diseñar los **TC humanos de CDU17** y exportar
`docs/modelo/iter02/staruml/iso2-iter02.staruml`. Con eso CFG-02 queda cerrada en
`docs/mapa-configuraciones.csv`. Sigue adeudado de CFG-01: TC humanos de CDU16 + export
`iso2-iter01.staruml`.

Para arrancar **Iter3 (CDU13 agendas, `iso2-agendas` 1.1.0, CFG-03**; apps 0.1.3-SNAPSHOT, tag
`cfg-iter03`, puertas "TC solapes/festivos; frontera ArchUnit"): rama nueva desde `develop` tras
la fusión de #14; escribir **Prompt 11** en PROMPTS.md + issue Iter3; leer CONTEXT.md → STATUS.md →
PLAN.md §4 → matriz CFG-03 de `docs/mapa-configuraciones.csv`.

**Cierre de jornada 08/11/2026 (fin sesión 9):** repo estable y todo subido. Iter1 fusionada y
etiquetada; Iter2 con PR #14 esperando fusión. Retomar ahí.
