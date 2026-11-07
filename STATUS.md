# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-07 (sesión 5)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔️ Planificada (coste 1.000 €; se ejecutó en octubre: planificación de Teoría 1) |
| Elaboración (Iter1–6) | ⏳ Pendiente — arranque previsto 09/11/2026 |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** ninguna (planificación de Teoría 1 completa: PUD + calidad + configuración).
**Siguiente iteración:** Iter1 · CDU16 (catálogo de tipos de prueba) — al crearla: issue con prompt,
diseño humano de TC, diagramas, módulo `iso2-catalogo` 1.1.0 + vistas admin, CFG-01 etiquetada al mergear.

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · `main` inicializada (README, .gitignore) y `develop` creada (Git Flow).
- **PR #1 fusionado** (planificación PUD + ficheros vivos) y **PR #2 fusionado** (calidad + configuración).
  ⚠ El PR #2 se fusionó en `feature/docs-plan-inicial` *después* de entrar el #1 en `develop`, así que la
  documentación de sesión 4 no había llegado a `develop`: **PR #4 abierto** (`releaseplan` → `develop`) para
  completarlo. Fusionar #4 **antes** que el #3.
- **PR #3 abierto por Ismael** (`develop` → `main`): etiquetará R0 en `main` cuando `develop` esté completo.
- **PR #5 abierto** (`feature/docs-prompt-log` → `develop`): `PROMPTS.md` (registro cronológico de prompts,
  mantener actualizado a partir de ahora) + actualización de STATUS.
- PR #6 (`feature/iter00-cfg00` → `develop`): Iter0 de código, esqueleto Maven de 14 módulos (CFG-00).
- Identidad git del repo: IsmaelCaballero@users.noreply.github.com (cambiar si se prefiere el correo UCLM).

## Registro de sesiones

| Sesión | Fecha | Hecho | Tokens aprox. |
|---|---|---|---|
| 1 | 07/11/2026 | Planificación PUD Teoría 1: RF→CDU (20), 21 iteraciones, releases, coste ≈16.530 € vs 26.230 € sin IA. Ficheros: docs/planificacion-PUD.md, docs/planificacion-iteraciones.csv, DECISIONS.md (D1–D9) | ~15k |
| 2 | 07/11/2026 | Reglas del equipo registradas (CONTEXT.md), PLAN.md operativo con desglose servidor/cliente + plan de despliegue alfa/beta/final + presupuesto de tokens por iteración, STATUS.md. Clonado repo GitHub, main+develop, pendientes D18 (StarUML) y decisiones D10–D20 | ~12k |
| 3 | 07/11/2026 | Planificación de evolución de componentes: docs/componentes-versionado.md (14 submódulos Maven, mapeo CDU→componente, riesgo T1–T4, incrementos 1.y.0, esfuerzo 492 h/12.300 €/≈2,1M tokens, matriz componente×iteración) + docs/componentes-versiones.csv. Alineados PLAN.md y planificacion-iteraciones.csv (baseline 1.0.0 + versiones 1.x.0). ADRs D22–D23 (esquema de versionado + inversión Iter7↔9) | ~14k |
| 4 | 07/11/2026 | Plan de calidad + plan de gestión de configuración y mapa de liberaciones (rama `releaseplan`): SOFTWAREQUALITY.md (modelo ISO 25010 de 5 características, DoD+calidad, impacto por componente y por CDU, puertas), docs/mapa-configuraciones.md+csv (CFG-00…CFG-21, alfa 20/11 y 27/11, beta 04/12, rc 11/12, definitiva 15/12 por criterios, rollback), versiones +Calidad de los tres planes: docs/planificacion-PUD+Calidad.md (v2.0.0), docs/planificacion-iteraciones+Calidad.csv, docs/componentes-versionado+Calidad.md + docs/componentes-versiones+Calidad.csv. ADRs D24–D27. Arreglado encabezado §1 de componentes-versionado.md (14 módulos) | ~35k |
| 5 | 07/11/2026 | Fix de render de CSV en GitHub (comas que faltaban: filas iter7/8/13/20 del base en `feature` y fila iter18 del +Calidad en `releaseplan`; todos los CSV verificados uniformes). Reapertura de PR #1/#2 cerrados sin fusión; Ismael fusiona #1 y #2 y abre #3 (`develop`→`main`). Detección de que el contenido de sesión 4 no llegó a `develop` → **PR #4** (`releaseplan`→`develop`). Nuevo **PROMPTS.md** (registro cronológico de prompts del alumno, PR #5) y arranque de la etapa de código: issue Iter0 + **Prompt 08** preparado en PROMPTS.md (esqueleto Maven CFG-00) | ~30k |

## Deuda / pendientes inmediatos

1. Confirmar decisión D18: ¿diagramas como fuente PlantUML en repo + exportación manual a StarUML, o genero directamente ficheros `.staruml`? (los .staruml son JSON frágiles: alto consumo de tokens y riesgo de corrupción).
2. ~~Instrucciones de código de Ismael~~ → se arranca Iter0 con el stack aceptado D11–D13; si Ismael añade instrucciones de código, se aplican como commit extra en el PR #6.
3. Para Iter1 (CDU16): tabla TC diseñada a mano por Ismael (regla D16) + issue con prompt + milestone `iter01-cdu16`.
4. Crear GitHub Project (Kanban) y la wiki de GitHub enlazando README/CONTEXT/PLAN/STATUS/DECISIONS/PROMPTS.

## Punto exacto de continuación para la próxima sesión

Orden de fusión de PRs: **#4 → #5 → #6** (a `develop`) y después **#3** (`develop`→`main`, etiqueta R0 `v0.1.0`).
Con Iter0 fusionado: tag `cfg-iter00` + `v0.1.0` sobre el merge del PR #6.
Para arrancar Iter1: leer CONTEXT.md → STATUS.md → `PROMPTS.md` (Prompt 09, pendientes sus dos
entradas del alumno) → PLAN.md §4 fila Iter1 (`iso2-catalogo` **1.1.0**, CDU16, CFG-01) →
matriz CFG-01 en `docs/mapa-configuraciones.csv`.
