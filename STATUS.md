# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-07 (sesión 7)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔ Código construido y verde en local; a la espera del merge del PR #7 y de los tags `cfg-iter00`/`v0.1.0` |
| Elaboración (Iter1–6) | 🟡 En curso — Iter1 (CDU16) con código terminado en su rama; esperando fusión y TC humanos |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** Iter1 · CDU16 (CFG-01) — código y diagramas terminados en
`feature/iter01-cdu16-catalogo` (PR a `develop` al terminar la sesión); quedan los TC humanos (D28),
la exportación `.staruml` (D18) y las fusiones. Iter0 pendiente solo de merge (PR #7) + tags.
**Siguiente iteración:** Iter2 · CDU17 roles y permisos RBAC+SSO (`iso2-seguridad` 1.1.0, CFG-02).

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · Git Flow: `main` + `develop` activas.
- **Fusionados:** PR #1 (plan PUD), PR #2 (calidad+config), PR #4 (`releaseplan`→`develop`) y
  PR #5 (`feature/docs-prompt-log`→`develop`, trae PROMPTS.md + docs de sesión 4 a `develop`).
- **Issue #6** = Iter0 · CFG-00 (quedará cerrado al fusionar el PR de código).
- **PR #7 abierto** (`feature/iter00-cfg00` → `develop`): Iter0 de código — reactor Maven de 14
  módulos, contratos baseline en `iso2-common`, apps 0.1.0 y CI (cierra #6).
- **PR #3 abierto por Ismael** (`develop` → `main`): etiquetará R0 en `main` cuando `develop` esté completo.
- **Issue #9** = Iter1 · CDU16 (cierra con su PR de código). Rama `feature/iter01-cdu16-catalogo`
  (base `e89863b`): 7 commits de CDU16; PR a `develop` al terminar la sesión 7. Debe fusionarse
  **después** del #7 (`develop` aún no tiene el código Iter0 que necesita como base).
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
| 7 | 07/11/2026 | **Iter1 · CDU16 (Prompt 09 ejecutado, issue #9):** D18 aceptada (fuentes PlantUML + export manual a StarUML) y nueva **D28** (los TC humanos se diseñan sobre código funcional; el agente solo escribe pruebas técnicas de humo). Rama `feature/iter01-cdu16-catalogo`: `common`+`catalogo` a 1.1.0-SNAPSHOT y apps a 0.1.1-SNAPSHOT, parent con `-parameters` en el compiler (sin eso `@RequestParam`/`@PathVariable` dan 400). `iso2-catalogo` 1.1.0: dominio `TipoPrueba` (+`Protocolo`/`Politica` embebidos), auditoría SHA-256 encadenada con `auditoriaVerificable()`, baja lógica idempotente y códigos no reutilizables, REST `/api/catalogo/pruebas` (201/200/204 + reactivación 200, errores 400/404/409 con `ApiError`) — 24/24 tests, JaCoCo 96,5 %. `iso2-server-app` 0.1.1: Flyway `V1.1.0__cdu16_catalogo.sql`, `ConfigSeguridad` Basic BCrypt admin/celador (RBAC transitorio; JWT en CDU17), smoke 401/403/200/201/400 — 6/6, 91,7 %. `iso2-client-app` 0.1.1: `ApiClienteCatalogo`/`ApiException` + `VistaAdminCatalogo` — 6/6, capa `api` 79,2 % (UI JavaFX excluida del gate). `docs/modelo/iter01/`: 4 diagramas PlantUML + README de exportación StarUML, validados con PlantUML 1.2025.2 y revisados a mano; el de casos de uso lleva actores de rol (Médico/Técnico/Celador/Recepcionista) solo a UC16.5 y UC7 de auditoría fuera de la frontera. Commits `bb337f5`..`ad99b21` en la rama; PR a `develop` (`Closes #9`). Pendientes de Ismael: TC humanos (D28), export `.staruml` y orden de fusión **#7 → tags → #3 → PR Iter1** | ~55k |

## Deuda / pendientes inmediatos

1. ~~Confirmar decisión D18~~ → **Aceptada**: fuentes PlantUML versionadas en `docs/modelo/iterXX/` + export manual a `.staruml` (pasos en `docs/modelo/iter01/README.md`; el export lo hace Ismael).
2. ~~Instrucciones de código de Ismael~~ → Iter0/Iter1 ejecutados con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #7 o en el de Iter1.
3. **TC humanos de CDU16** (D28, sustituye al orden de D16): tabla de casos de prueba diseñada por Ismael sobre el código funcional de `feature/iter01-cdu16-catalogo`; son criterio de cierre de Iter1/CFG-01.
4. Exportar `docs/modelo/iter01/staruml/iso2-iter01.staruml` en StarUML (manual, D18) y subirlo como artefacto de CFG-01.
5. Crear GitHub Project (Kanban) y la wiki de GitHub enlazando README/CONTEXT/PLAN/STATUS/DECISIONS/PROMPTS.

## Punto exacto de continuación para la próxima sesión

Orden de fusión (solo Ismael): **#7** (`feature/iter00-cfg00`→`develop`, cierra #6) → tags
`cfg-iter00` + `v0.1.0` sobre ese merge → **#3** (`develop`→`main`, R0) → **PR de Iter1**
(`feature/iter01-cdu16-catalogo`→`develop`, cierra #9) → tag `cfg-iter01`.
Después: TC humanos de CDU16 sobre el código fusionado (D28) → cerrar CFG-01 en
`docs/mapa-configuraciones.csv`. Para arrancar Iter2 (CDU17, `iso2-seguridad` 1.1.0, CFG-02):
leer CONTEXT.md → STATUS.md → PROMPTS.md (Prompt 10 por escribir) → PLAN.md §4 → matriz CFG-02.
