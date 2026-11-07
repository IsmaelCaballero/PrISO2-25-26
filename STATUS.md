# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-07 (sesión 6)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | 🟡 En curso — esqueleto Maven de código construido y verde en local; esperando merge del PR #7 |
| Elaboración (Iter1–6) | ⏳ Pendiente — arranque previsto 09/11/2026 |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** Iter0 (CFG-00) — código terminado en `feature/iter00-cfg00`; pendientes el
merge (Ismael) y los tags `cfg-iter00` + `v0.1.0`.
**Siguiente iteración:** Iter1 · CDU16 (catálogo de tipos de prueba) — bloqueada por dos entradas
del alumno: tabla TC de CDU16 (D16) y decisión D18 (PlantUML vs `.staruml`).

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · Git Flow: `main` + `develop` activas.
- **Fusionados:** PR #1 (plan PUD), PR #2 (calidad+config), PR #4 (`releaseplan`→`develop`) y
  PR #5 (`feature/docs-prompt-log`→`develop`, trae PROMPTS.md + docs de sesión 4 a `develop`).
- **Issue #6** = Iter0 · CFG-00 (quedará cerrado al fusionar el PR de código).
- **PR #7 abierto** (`feature/iter00-cfg00` → `develop`): Iter0 de código — reactor Maven de 14
  módulos, contratos baseline en `iso2-common`, apps 0.1.0 y CI (cierra #6).
- **PR #3 abierto por Ismael** (`develop` → `main`): etiquetará R0 en `main` cuando `develop` esté completo.
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

## Deuda / pendientes inmediatos

1. Confirmar decisión D18: ¿diagramas como fuente PlantUML en repo + exportación manual a StarUML, o genero directamente ficheros `.staruml`? (los .staruml son JSON frágiles: alto consumo de tokens y riesgo de corrupción).
2. ~~Instrucciones de código de Ismael~~ → Iter0 ejecutado con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #7.
3. Para Iter1 (CDU16): tabla TC diseñada a mano por Ismael (regla D16) + issue con prompt + milestone `iter01-cdu16`.
4. Crear GitHub Project (Kanban) y la wiki de GitHub enlazando README/CONTEXT/PLAN/STATUS/DECISIONS/PROMPTS.

## Punto exacto de continuación para la próxima sesión

Orden de fusión restante: **#7** (`feature/iter00-cfg00` → `develop`, cierra issue #6) y después
**#3** (`develop`→`main`, etiqueta R0). Sobre el merge de #7 en `develop`: tags `cfg-iter00` + `v0.1.0`.
Para arrancar Iter1: leer CONTEXT.md → STATUS.md → `PROMPTS.md` (Prompt 09, pendientes sus dos
entradas del alumno: tabla TC de CDU16 y decisión D18) → PLAN.md §4 fila Iter1 (`iso2-catalogo`
**1.1.0**, CDU16, CFG-01) → matriz CFG-01 en `docs/mapa-configuraciones.csv`.
