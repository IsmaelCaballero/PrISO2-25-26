# STATUS.md — Estado vivo del proyecto

**Última actualización:** 2026-11-07 (sesión 2)

## Estado global

| Fase | Estado |
|---|---|
| Inicio (Iter0) | ✔️ Planificada (coste 1.000 €; se ejecutó en octubre: planificación de Teoría 1) |
| Elaboración (Iter1–6) | ⏳ Pendiente — arranque previsto 09/11/2026 |
| Construcción (Iter7–20) | ⏳ Pendiente |
| Transición (Iter21) | ⏳ Pendiente — cierre 14/12, entrega 15/12 |

**Iteración en curso:** ninguna (preparación del repo y reglas de trabajo completadas).
**Siguiente iteración:** Iter1 · CDU16 (catálogo de tipos de prueba) — al crearla: issue con prompt,
diseño humano de TC, diagramas, módulo `iso2-catalogo` 1.0.0 + vistas admin.

## Estado del repositorio

- Repo: https://github.com/IsmaelCaballero/PrISO2-25-26 · rama `main` inicializada (README, .gitignore).
- Rama `develop` creada (Git Flow). Pendiente: PR `feature/docs-plan-inicial` → **aprobación de Ismael**.
- Estructura Maven: **aún no creada** (se hará al aprobarse la rama de docs, con Ismael dando las
  instrucciones de código pendientes).

## Registro de sesiones

| Sesión | Fecha | Hecho | Tokens aprox. |
|---|---|---|---|
| 1 | 07/11/2026 | Planificación PUD Teoría 1: RF→CDU (20), 21 iteraciones, releases, coste ≈16.530 € vs 26.230 € sin IA. Ficheros: docs/planificacion-PUD.md, docs/planificacion-iteraciones.csv, DECISIONS.md (D1–D9) | ~15k |
| 2 | 07/11/2026 | Reglas del equipo registradas (CONTEXT.md), PLAN.md operativo con desglose servidor/cliente + plan de despliegue alfa/beta/final + presupuesto de tokens por iteración, STATUS.md. Clonado repo GitHub, main+develop, pendientes D18 (StarUML) y decisiones D10–D20 | ~12k |

## Deuda / pendientes inmediatos

1. Confirmar decisión D18: ¿diagramas como fuente PlantUML en repo + exportación manual a StarUML, o genero directamente ficheros `.staruml`? (los .staruml son JSON frágiles: alto consumo de tokens y riesgo de corrupción).
2. Instrucciones de código de Ismael (aún no dadas) → crear `iso2-parent` multi-módulo.
3. Crear GitHub Project (Kanban) + milestone `iter01-cdu16` + issues cuando arranque Iter1.
4. Crear fichero de la wiki de GitHub que enlace README/CONTEXT/PLAN/STATUS/DECISIONS.

## Punto exacto de continuación para la próxima sesión

Leer CONTEXT.md → este fichero → PLAN.md sección 4 fila Iter1. Esperar instrucciones de código;
mientras tanto queda pendiente resolver D18 y aprobar el PR de docs.
