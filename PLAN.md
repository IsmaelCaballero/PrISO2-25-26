# PLAN.md — Plan de proyecto PUD (operativo)

Proyecto: Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica (SESCAM)
Equipo: 6 miembros · Tarifa 25 €/h · Iter0=1.000 € · Iter21=2.000 € · Entrega final: 15/12/2026
(Detalle económico completo en `docs/planificacion-PUD.md` — este fichero es la visión operativa.)

## 1. Arquitectura: dos aplicaciones

```
iso2-parent (agregador Maven)
├── iso2-common        · contratos de dominio, DTO e interfaces compartidas (servidor ↔ cliente)
├── iso2-catalogo      · CDU16                 [servidor]
├── iso2-seguridad     · CDU17 + login/credenciales (1 rol/persona) → +CDU18   [servidor+cliente login]
├── iso2-agendas       · CDU13 → +CDU08        [servidor]
├── iso2-solicitudes   · CDU06 → +CDU07 → +CDU08 → +CDU05   [servidor]
├── iso2-citas         · CDU12 → +CDU14 → +CDU01            [servidor] (D23)
├── iso2-integracion   · CDU19 → +CDU11 → +CDU09 (DICOM)    [servidor]
├── iso2-pacientes     · CDU11 → +CDU03        [servidor]
├── iso2-comunicacion  · CDU02 → +CDU04        [servidor]
├── iso2-informes      · CDU09                 [servidor]
├── iso2-reporting     · CDU10 → +CDU15 → +CDU20            [servidor]
├── iso2-server-app    · API REST Spring Boot que ensambla los módulos   [servidor]
└── iso2-client-app    · cliente JavaFX con vistas por rol  [cliente: todas las tajadas de CDU]
```

**Regla de desglose por CDU**: cada iteración entrega (a) el servicio/entidades del CDU en su módulo
servidor con API REST y (b) la tajada de vistas del CDU en `iso2-client-app` para el rol afectado.
Cada parte versiona su módulo con SemVer 2.0.

**Evolución de componentes (D22)**: cada componente submódulo Maven parte de un baseline `1.0.0`
(contractos publicados en Iter0) y crece en incrementos `1.y.0` ordenados por **riesgo de desarrollo
decreciente** (v1.1.0 = CDU de mayor riesgo del componente, v1.2.0 = siguiente nivel, etc.).
La matriz completa CDU→componente, incrementos, esfuerzo y tokens está en
[`docs/componentes-versionado.md`](docs/componentes-versionado.md) y en CSV en
[`docs/componentes-versiones.csv`](docs/componentes-versiones.csv). La **versión +Calidad** de estos
documentes (foco ISO 25010 por componente, cobertura objetivo y etiquetas CFG/release) está en
[`docs/componentes-versionado+Calidad.md`](docs/componentes-versionado+Calidad.md) y
[`docs/componentes-versiones+Calidad.csv`](docs/componentes-versiones+Calidad.csv); el mapa completo
de configuraciones CFG-00…CFG-21 en [`docs/mapa-configuraciones.md`](docs/mapa-configuraciones.md).

## 2. Calendario y liberaciones

| Hito | Fecha | Release | Contenido |
|---|---|---|---|
| Iter0 (Inicio) | 19–25/10 (hecha) | R0 v0.1.0 (CFG-00) | Esqueleto multi-módulo + esqueletos `server-app`/`client-app` con login vacío |
| Elaboración | 09–20/11 | **R1 v0.2.0-alpha · ALFA 1** (CFG-06, 20/11) | CDU16, 17, 13, 06, 12, 19-spike → línea base arquitectura |
| Construcción A | 23–27/11 | **R2 v0.3.0-alpha · ALFA consol.** (CFG-12, 27/11) | CDU01, 11, 14, 02, 07, 08 |
| Construcción B | 30/11–04/12 | **R3 v0.4.0-beta · BETA** (CFG-18, 04/12) | CDU03, 05, 18, 09, 10, 04 |
| Construcción C | 07–11/12 | R4 v0.9.0-rc · CANDIDATA (CFG-20, 11/12) | CDU15, 20 + estabilización |
| Transición (Iter21) | 07–14/12 | **R5 v1.0.0 final · DEFINITIVA** (CFG-21, 15/12) | Integración, pruebas de sistema, despliegue, manual |

Cada merge de iteración a `develop` etiqueta su **CFG-NN** (`cfg-iterNN`): configuración mínima
desplegable y reproducible. Mapa completo: `docs/mapa-configuraciones.md` (+CSV) y D25–D26.

## 3. Plan de despliegue (alfa / beta / final)

| Canal | Versión SemVer | Fecha | Despliegue | Criterio de paso |
|---|---|---|---|---|
| **alfa** | 0.1.0 → 0.2.0-alpha → 0.3.0-alpha | 25/10 → 20/11 → 27/11 | local (mvn spring-boot:run + javafx:run, H2 en memoria) | Arquitectura validada, riesgos mitigados, flujo login→catálogo→agenda→solicitud→cita end-to-end; puertas JaCoCo ≥60 % (R1) y ≥70 % (R2) |
| **beta** | 0.4.0-beta | 04/12 | JAR ejecutable servidor + cliente empaquetado (jpackage), H2 en fichero | Todos los CDU clínicos+imagen funcionales, cobertura ≥75 %, Javadoc sin errores, modo degradado probado, copia/restore ensayado |
| **beta-RC** | 0.9.0-rc | 11/12 | ídem + datos de prueba SESCAM simulados | 20/20 CDU, congelación de código, trazabilidad RF→CU→TC completa, ≥78 % |
| **final** | 1.0.0 (tag v1.0.0) | 15/12 | entrega Moodle + repo: manuales, informes JaCoCo/Surefire, wiki | Aprobación de Ismael + UAT simulada superada, ≥80 % (≥85 % núcleo), 0 defectos críticos |

Ramas asociadas (Git Flow): `release/0.2.0` (alfa), `release/0.4.0` (beta), `release/0.9.0` (RC),
`release/1.0.0` + tag `v1.0.0` + merge a `main` (solo lo hace Ismael al aprobar).

## 4. Iteraciones: desglose servidor/cliente y presupuesto de tokens

Complejidad → presupuesto tokens de agente por iteración: **Baja ≈ 50–70k · Media ≈ 90–130k · Alta ≈ 150–220k → partir en 2 sesiones** (a=diagramas+servidor+tests servidor; b=cliente+tests integración+informes). Ventana de trabajo objetivo: ≤180k tokens/sesión.

| Iter | CDU | Complej. | Parte SERVIDOR (módulo · versión) | Parte CLIENTE (vistas del rol) | Tokens est. | ¿Partir? |
|---|---|---|---|---|---|---|
| 0 | — | — | `common`+componentes baseline **1.0.0** · `server-app` 0.1.0 | `client-app` 0.1.0 (shell+login vacío) | (hecha) | — |
| 1 | CDU16 catálogo | M | `iso2-catalogo` **1.1.0**: CRUD tipos prueba, protocolos, políticas | Admin: pant. catálogo/pruebas (alta) | ~100k | no |
| 2 | CDU17 RBAC+credenciales | M | `iso2-seguridad` **1.1.0**: usuarios, roles, BCrypt, sesión JWT | Login + shell con menú por rol | ~95k | no |
| 3 | CDU13 agendas | A | `iso2-agendas` **1.1.0**: agenda médico/técnico/sala, huecos | Admin: pant. agenda día/semana | ~170k | **sí (2)** |
| 4 | CDU06 solicitudes | A | `iso2-solicitudes` **1.1.0**: prescripción con prioridad | Médico: nueva solicitud + listado | ~170k | **sí (2)** |
| 5 | CDU12 citas | A | `iso2-citas` **1.1.0**: motor asignación huecos↔solicitud | Admin: gestión de citas | ~180k | **sí (2)** |
| 6 | CDU19 integraciones (spike) | A | `iso2-integracion` **1.1.0**: fachada FHIR/SSO + ping RIS/PACS | (no vista; informes del spike) | ~150k | **sí (2)** |
| 7 | CDU14 listas de espera | M | `iso2-citas` **1.2.0**: prioridades, huecos, reubicación | Admin: listas de espera | ~90k | no |
| 8 | CDU11 pacientes CRUD | M | `iso2-pacientes` **1.1.0** + `integracion` **1.2.0** (Admisión) | Admin: alta/modif/baja paciente | ~95k | no |
| 9 | CDU01 citas paciente | M | `iso2-citas` **1.3.0**: self-service confirmar/cambiar/cancelar | Paciente: mis citas | ~85k | no |
| 10 | CDU02 notificaciones | M | `iso2-comunicacion` **1.1.0**: recordatorios/preparación/informes | Todos: campana + email simulado | ~95k | no |
| 11 | CDU07 seguimiento periódico | A | `iso2-solicitudes` **1.2.0**: motor recurrencia→nueva necesidad | Médico: prescripción seguimiento | ~160k | **sí (2)** |
| 12 | CDU08 estado solicitudes | M | `iso2-solicitudes` **1.3.0** + `agendas` **1.2.0** | Médico: tracking + huecos | ~85k | no |
| 13 | CDU03 historial paciente | M | `iso2-pacientes` **1.2.0** | Paciente: historial | ~80k | no |
| 14 | CDU05 info seguimiento | M | `iso2-solicitudes` **1.4.0** | Paciente: próximas revisiones | ~80k | no |
| 15 | CDU18 auditoría | M | `iso2-seguridad` **1.2.0**: registro accesos clínicos | AdminSistema: visor auditoría | ~95k | no |
| 16 | CDU09 informes+imágenes | A | `iso2-informes` **1.1.0** + `integracion` **1.3.0** (DICOM store) | Médico: visor informe + imagen | ~180k | **sí (2)** |
| 17 | CDU10 estadíst. médico | B | `iso2-reporting` **1.1.0** | Médico: dashboard esperas | ~55k | no |
| 18 | CDU04 incidencias/encuestas | B | `iso2-comunicacion` **1.2.0** | Paciente: alta incidencia + encuesta | ~55k | no |
| 19 | CDU15 informes operativos | B | `iso2-reporting` **1.2.0** | Admin: informes ocupación | ~55k | no |
| 20 | CDU20 estadíst. globales | B | `iso2-reporting` **1.3.0** | AdminSistema: reportes uso | ~55k | no |
| 21 | — Transición | — | `server-app` **1.0.0** + `common` 1.5.0 | `client-app` **1.5.0** + instalador jpackage | ~120k | no (pero repartir en 2 días) |

> **D23**: se invierten Iter7↔9 del plan original (CDU01 y CDU14) para que los incrementos de
> `iso2-citas` respeten el orden de riesgo decreciente: 1.2.0=CDU14 (T2 alto) antes que 1.3.0=CDU01 (T2 bajo).

**Checklist obligatorio de cada iteración** (definido en CONTEXT.md; DoD+calidad de 8 puntos en
`SOFTWAREQUALITY.md` §1):
1. Issue/s creados con prompt simulable → 2. Casos de prueba DISEÑADOS por humanos (tabla TC, con
≥1 feliz + ≥2 error + 1 permisos) → 3. Diagramas StarUML/PlantUML (CU, secuencia, clases análisis,
clases diseño) → 4. Implementación agente (servidor→cliente) con `@Auditable` en mutaciones y
reglas ArchUnit en verde → 5. Tests JUnit en verde + JaCoCo/Surefire (umbral del componente en
`docs/componentes-versionado+Calidad.md` §2) → 6. Javadoc completo → 7. PR con commits por tarea →
8. Bump SemVer + merge a `develop` con **tag `cfg-iterNN`** y auditoría de configuración +
`STATUS.md`/`PLAN.md`/`DECISIONS.md` actualizados.

## 5. Plantilla de issue (obligatoria, con prompt para simular)

```markdown
## Objetivo · Iter{XX} · CDU{yy} · disciplina PUD: {análisis|diseño|implementación|pruebas}
## Contratos/interfaces involucrados
## Criterios de aceptación (Definition of Done)
- [ ] Tests TC-{xxx}-01..N en verde (Surefire) · cobertura JaCoCo ≥ {umbral}
- [ ] Javadoc sin errores · versión del módulo bump → {semver}
## Estimación: humano {h} h · con agente {h} h · tokens est. {k}
## Prompt para el asignatario (simulación)
> Actúa como implementador del módulo {módulo} de PrISO2-25-26. Contexto: lee CONTEXT.md,
> STATUS.md y docs/modelo/iter{XX}/. Contrato a cumplir: {interfaz}. No modifiques {ficheros}.
> Casos de prueba ya diseñados: {tabla TC}. Entrega: implementación + tests JUnit + javadoc.
## Assignee / Milestone iter{XX}
```

## 6. Tablero Kanban (GitHub Projects)

Columnas: `Backlog (20 CDU)` → `Planificada` → `En curso (máx 3)` → `PR abierto (espera Ismael)` →
`Hecha`. Regla: una tarjeta por iteración/CDU; los issues cuelgan de la tarjeta; revisión de compromisos
en reunión semanal con acta (`docs/actas/`).

## 7. Métricas de control

- Burndown de iteraciones (21) vs plan de la sección 4; desviación >1 iteración → acta de replanificación.
- Puertas de calidad por release (detalle en `docs/planificacion-PUD+Calidad.md` §8.4): JaCoCo línea
 ≥60 % R1 · ≥70 % R2 · ≥75 % R3 · ≥78 % R4 · **≥80 % R5** (≥85 % citas/solicitudes/seguridad); rama
 ≥45 %→70 %; fallos Surefire = violaciones ArchUnit = 0; TC RBAC y `@Auditable` al 100 %.
- Auditoría de configuración en cada merge: ¿coinciden los POM con `docs/componentes-versiones+Calidad.csv`?
- Trazabilidad RF→CU→TC: 6/12/18/20/20 CDU por release. Consumo real de tokens por iteración vs
 presupuesto → alimenta reestimación en DECISIONS.md. El **guardián de calidad** (rotatorio) publica
 las métricas en `STATUS.md` en cada CFG hito.
