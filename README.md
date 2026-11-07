# PrISO2-25-26 · Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica (SESCAM)

Ingeniería del Software II — Proyecto de curso.

## Estructura de lectura (wiki → ficheros)

| Fichero | Contenido |
|---|---|
| [CONTEXT.md](CONTEXT.md) | Reglas permanentes de trabajo del agente IA (normas del equipo) |
| [PLAN.md](PLAN.md) | Plan de proyecto PUD operativo: iteraciones, servidor/cliente, despliegue, issues+prompts |
| [STATUS.md](STATUS.md) | Estado vivo del proyecto: qué está hecho, qué sigue, registro de sesiones |
| [DECISIONS.md](DECISIONS.md) | Decisiones y supuestos (estilo ADR) que usan los agentes como contexto |
| [docs/planificacion-PUD.md](docs/planificacion-PUD.md) | Planificación PUD completa (entrega Teoría 1) |
| [docs/planificacion-PUD+Calidad.md](docs/planificacion-PUD+Calidad.md) | **Nueva versión v2.0.0**: plan PUD + plan de calidad (ISO 25010) + plan de gestión de configuración + releases alfa/beta/definitiva |
| [docs/planificacion-iteraciones.csv](docs/planificacion-iteraciones.csv) | Agenda de iteraciones en CSV |
| [docs/planificacion-iteraciones+Calidad.csv](docs/planificacion-iteraciones+Calidad.csv) | Agenda + columnas CFG, foco ISO 25010, pruebas y puerta de calidad, etiqueta release |
| [docs/componentes-versionado.md](docs/componentes-versionado.md) | Componentes (submódulos Maven), mapeo CDU→componente, evolución por versiones/incrementos priorizada por riesgo, esfuerzo |
| [docs/componentes-versionado+Calidad.md](docs/componentes-versionado+Calidad.md) | **Nueva versión +Calidad**: foco ISO 25010 y cobertura objetivo por componente + matriz con CFG y etiquetas de release |
| [docs/componentes-versiones.csv](docs/componentes-versiones.csv) | Matriz componente × iteración (versión que se construye) en CSV |
| [docs/componentes-versiones+Calidad.csv](docs/componentes-versiones+Calidad.csv) | Matriz + columnas cfg_id, tag y etiqueta_release |
| [docs/mapa-configuraciones.md](docs/mapa-configuraciones.md) | Plan de gestión de configuración: CFG-00…CFG-21, ramas/tags, ambientes, rollback, criterios alfa/beta/definitiva |
| [docs/mapa-configuraciones.csv](docs/mapa-configuraciones.csv) | Mapa CFG en CSV (versiones exactas de cada configuración) |
| [SOFTWAREQUALITY.md](SOFTWAREQUALITY.md) | Plan de calidad: modelo ISO/IEC 25010, impacto en cada componente y en cada CDU, puertas de calidad |

## Arquitectura (v1.0)

Dos aplicaciones: **servidor** (Java + Spring Boot + H2 + Maven multi-módulo) y **cliente** (JavaFX,
con interfaz distinta según rol: paciente, médico, administrativo, administrador), comunicadas por REST/JSON.
Una persona = un rol en v1; credenciales locales gestionadas por `iso2-seguridad`.

## Flujo de trabajo

Git Flow estricto: `main` ← solo releases · `develop` ← solo PRs aprobados por Ismael ·
`feature/iterXX-CDUyy-…` para el trabajo. Cada iteración: diagramas (StarUML), casos de prueba
diseñados a mano + JUnit, informes Surefire+JaCoCo, Javadoc completo, versiones SemVer 2.0.
