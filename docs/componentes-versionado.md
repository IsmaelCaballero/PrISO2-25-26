# Componentes del sistema: arquitectura, mapeo CDU→componente y evolución por versiones

> Documento operativo de planificación (vinculado desde PLAN.md). Define los **componentes**
> (cada uno = submódulo Maven), el **mapeo caso de uso → componente**, la **priorización por
> riesgo de desarrollo** y la **evolución versionada** de cada componente: cada `v1.y.0` es un
> **incremento** = conjunto de artefactos que nos llevan de la versión inmediatamente anterior.
> Convención: `v1.1.0` = CDU de MAYOR riesgo; `v1.2.0` = siguiente nivel de riesgo/prioridad; etc.

## 1. Arquitectura del sistema: 14 módulos Maven

Un agregador + un módulo transversal + diez componentes de dominio (servidor) + dos aplicaciones (tren de versiones):

```
iso2-parent (agregador · tren de versiones de release)
│  TRANSVERSALES
├── iso2-common        · contratos de dominio, DTO, interfaces de puerto (bus común servidor↔cliente)
│  DOMINIO (servidor)
├── iso2-catalogo      · catálogo de tipos de prueba y políticas
├── iso2-seguridad     · identidad, credenciales, RBAC (1 rol/persona v1), auditoría
├── iso2-agendas       · agendas de médico/técnico/sala/equipo y huecos disponibles
├── iso2-solicitudes   · prescripción, estado, seguimiento periódico
├── iso2-citas         · ciclo de vida de citas: asignación, self-service, listas de espera
├── iso2-pacientes     · altas/modificaciones/bajas e historial de pruebas
├── iso2-comunicacion  · notificaciones, recordatorios, incidencias, encuestas
├── iso2-informes      · informes clínicos e imágenes (visor)
├── iso2-integracion   · fachadas SSO/HCE/Admisión/RIS-PACS (HL7/FHIR/DICOM)
├── iso2-reporting     · estadísticas y reportes (médico, operativo, global)
│  APLICACIONES (tren de versiones de release)
├── iso2-server-app    · ensamblado Spring Boot: publica la API REST (0.x hasta v1.0.0 final)
└── iso2-client-app    · cliente JavaFX con vistas por rol (4 incrementos 1.1→1.4 + pulido 1.5)
```

**Reglas de versionado SemVer 2.0 aplicadas** (D22):
- Componente de dominio: `v1.0.0` = baseline de contratos publicado en Iter0 (interfaz vacía
  publicable); `v1.y.0` = incrementos por riesgo creciente decreciente (cada CDU del componente
  en su nivel); `v1.y.z` = fixes intra-iteración; sin `v2.x` antes de la Iter21.
- `iso2-common`: sube minor por release (R1→1.1.0 … R5→1.5.0) porque cada CDU añade contratos.
- Aplicaciones: tren de versiones de release (`0.1.0`→…→`0.9.0-RC`→`1.0.0`), no del esquema 1.y.0.

## 2. Niveles de riesgo de desarrollo de los CDU (orden de los incrementos)

Criterios: incertidumbre técnica, impacto arquitectónico, dependencias y dificultad de prueba.

| Nivel | CDU | Justificación del riesgo |
|---|---|---|
| **T1 (Alto)** | CDU19, CDU12, CDU13, CDU06, CDU17, CDU16 | 19: protocolos sanitarios externos (FHIR/DICOM/SSO), lo más incierto. 12: motor de asignación cita↔hueco↔recurso. 13: modelo de agenda multi-recurso (base de todo). 06: núcleo del dominio clínico. 17: seguridad/credenciales transversal y crítico de RGPD. 16: catálogo = espina dorsal del modelo de dominio. |
| **T2 (Medio-alto)** | CDU07, CDU14, CDU02, CDU11, CDU09*, CDU01, CDU08, CDU18 | 07: motor de recurrencias. 14: reubicación de listas de espera. 02: motor de recordatorios multi-canal. 11: CRUD + fachada Admisión. 09: visor DICOM (el spike de Iter6 mitiga el riesgo, baja a T2). 01: self-service sobre motor ya construido. 08: consultas. 18: registro de auditoría. |
| **T3 (Medio-bajo)** | CDU03, CDU05 | Presentación/consulta sobre datos ya gestionados. |
| **T4 (Bajo)** | CDU10, CDU15, CDU20, CDU04 | Reportes sobre datos estables y formularios simples. |

\* CDU09 aparece como colaborador en `iso2-integracion` (almacén DICOM, T2) y como titular en
`iso2-informes` (visor/informe, T2). El spike de Iter6 (CDU19) es lo que permite tratarlo a T2.

## 3. Matriz CDU → componente (P = titular · C = colaborador)

| CDU | catalog | segur | agendas | solicit | citas | pacientes | comunic | informes | integrac | reporting |
|---|---|---|---|---|---|---|---|---|---|---|
| CDU16 | **P** | C | | | C | | | | | |
| CDU17 | | **P** | C | C | C | C | | | C | |
| CDU13 | C | | **P** | C | C | | | | | |
| CDU06 | C | C | C | **P** | | | | | | |
| CDU12 | C | C | C | C | **P** | | | | | |
| CDU19 | C | C | C | C | C | C | C | C | **P** | |
| CDU14 | | | C | C | **P** | | | | | |
| CDU11 | | C | | | | **P** | | | C | |
| CDU01 | | C | C | | **P** | | | | | |
| CDU02 | C | C | C | C | C | C | **P** | | | |
| CDU07 | | | C | **P** | C | | | | | |
| CDU08 | | | C | **P** | C | | | | | C |
| CDU03 | C | C | | | | **P** | | | | |
| CDU05 | | | | **P** | C | | | | | |
| CDU18 | | **P** | C | C | C | C | | C | | |
| CDU09 | | C | | | | C | | **P** | C | |
| CDU10 | | | | C | C | | | | | **P** |
| CDU04 | | | | | | | **P** | | | |
| CDU15 | C | | C | | C | | | | | **P** |
| CDU20 | C | C | C | C | C | C | C | C | C | **P** |

Todo CDU pasa además por `iso2-client-app` (tajada de vistas del rol) y por `iso2-common` (contratos).

## 4. Evolución de cada componente: incrementos, esfuerzo y tokens

Leyenda esfuerzo (horas humanas con agente, desglose: **A**nálisis · **D**iseño interfaces ·
**I**mplementación agente + revisión · **T**ests diseño+implementación · **Dg** diagramas+Javadoc).
Complejidad: A=Alta 36 h · M=Media 22 h · B=Baja 14 h. Tokens: consumo de agente por incremento
(iteraciones A se parten en 2 sesiones).

### 4.1 iso2-citas — 80 h (2.000 €) — el componente crítico del negocio

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| 1.0.0 | baseline contratos (Cita, AgendaGateway) | — | Iter0 | 0 | — |
| **1.1.0** | CDU12 gestión de citas (motor asignación) | T1 | 36 h (5/7/14/7/3) | 5 | ~170k (2 sesiones) |
| **1.2.0** | CDU14 listas de espera, prioridades, huecos | T2 | 22 h (3/4/9/4/2) | 7 | ~90k |
| **1.3.0** | CDU01 self-service paciente (confirmar/cambiar/cancelar) | T2 (menor) | 22 h (3/4/9/4/2) | 9 | ~85k |

*(D23: se invierten Iter7↔9 del plan para que el orden de incrementos respete riesgo decreciente: 14 antes que 01.)*

### 4.2 iso2-solicitudes — 116 h (2.900 €) — núcleo del flujo clínico

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| 1.0.0 | baseline (Solicitud, Prioridad) | — | Iter0 | 0 | — |
| **1.1.0** | CDU06 solicitud/prescripción con prioridad | T1 | 36 h (5/7/14/7/3) | 4 | ~170k (2 sesiones) |
| **1.2.0** | CDU07 seguimiento periódico (recurrencias) | T2 | 36 h (5/7/14/7/3) | 11 | ~160k (2 sesiones) |
| **1.3.0** | CDU08 estado de solicitudes + agendas disp. (colab. agendas) | T2 (menor) | 22 h | 12 | ~85k |
| **1.4.0** | CDU05 información de revisiones al paciente | T3 | 22 h | 14 | ~80k |

### 4.3 iso2-agendas — 36 h (900 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU13 agendas médico/técnico/día | T1 | 36 h (5/7/14/7/3) | 3 | ~170k (2 sesiones) |
| **1.2.0** | colab. CDU08 (huecos disponibles) | T2 | (incluido en Iter12) | 12 | — |

### 4.4 iso2-integracion — 36 h (900 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU19 spike SSO+HCE/FHIR+ping PACS | T1 | 36 h (5/7/14/7/3) | 6 | ~150k (2 sesiones) |
| **1.2.0** | CDU11 fachada Admisión (adaptador) | T2 | (incluido en Iter8) | 8 | — |
| **1.3.0** | CDU09 almacén DICOM (store/scp) | T2 (mitigado por spike) | (incluido en Iter16) | 16 | — |

### 4.5 iso2-seguridad — 44 h (1.100 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU17 RBAC + usuarios/credenciales (BCrypt+JWT, 1 rol/persona) | T1 | 22 h (3/4/9/4/2) | 2 | ~95k |
| **1.2.0** | CDU18 auditoría de accesos clínicos | T2 | 22 h | 15 | ~95k |

### 4.6 iso2-catalogo — 22 h (550 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU16 catálogo de tipos de prueba, protocolos, políticas | T1 | 22 h (3/4/9/4/2) | 1 | ~100k |

### 4.7 iso2-pacientes — 44 h (1.100 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU11 alta/modif/baja de pacientes | T2 | 22 h | 8 | ~95k |
| **1.2.0** | CDU03 historial de pruebas | T3 | 22 h | 13 | ~80k |

### 4.8 iso2-comunicacion — 36 h (900 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU02 notificaciones y recordatorios | T2 | 22 h | 10 | ~95k |
| **1.2.0** | CDU04 incidencias y encuestas | T4 | 14 h (2/3/5/3/1) | 18 | ~55k |

### 4.9 iso2-informes — 36 h (900 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU09 informes + imágenes (visor DICOM médico) | T2 | 36 h (5/7/14/7/3) | 16 | ~180k (2 sesiones) |

### 4.10 iso2-reporting — 42 h (1.050 €)

| Versión | Incremento (CDU) | Riesgo | Esfuerzo | Iter | Tokens |
|---|---|---|---|---|---|
| **1.1.0** | CDU10 estadísticas del médico | T4 (mayor del nivel) | 14 h (2/3/5/3/1) | 17 | ~55k |
| **1.2.0** | CDU15 informes operativos | T4 | 14 h | 19 | ~55k |
| **1.3.0** | CDU20 estadísticas globales | T4 (menor) | 14 h | 20 | ~55k |

### 4.11 iso2-client-app — incrementos de vistas por riesgo (esfuerzo incluido en cada iteración)

| Versión | Incremento | Iter | Contenido |
|---|---|---|---|
| 0.1.0 | baseline | 0 | shell + login vacío (tren de release, 0.x) |
| **1.1.0** | vistas T1 | 1–6 | login+menú por rol, catálogo, agenda (rejilla), nueva solicitud, gestión de citas |
| **1.2.0** | vistas T2 | 7–12 | mis citas (paciente), CRUD pacientes, listas de espera, notificaciones, recurrencias, tracking |
| **1.3.0** | vistas T3 + visor | 13–16 | historial, revisiones, visor de auditoría, visor de informes/imágenes |
| **1.4.0** | vistas T4 | 17–20 | dashboards de estadísticas, incidencias/encuestas, informes operativos |
| **1.5.0** | pulido+instalador | 21 | usabilidad por rol, empaquetado jpackage, manual |

Reparto de esfuerzo por iteración: **servidor ≈60% / cliente ≈40%** (integrado en las horas de la tabla).

### 4.12 iso2-common y iso2-server-app (transversales, tren de release)

| Versión | common | server-app | Qué entra |
|---|---|---|---|
| 1.0.0 / 0.1.0 | contratos baseline | esqueleto multi-módulo + API vacía | Iter0 (R0) |
| 1.1.0 / 0.2.0-alpha | contratos T1 | ensambla catalogo1.1+seguridad1.1+agendas1.1+solicitudes1.1+citas1.1+integracion1.1 | Iter1–6 (R1) |
| 1.2.0 / 0.3.0 | contratos T2a | + pacientes1.1, citas1.2, comunicacion1.1, solicitudes1.2 | Iter7–12 (R2) |
| 1.3.0 / 0.4.0-beta | contratos T2b/T3 | + informes1.1, solicitudes1.3/1.4, seguridad1.2, citas1.3 | Iter13–16 (R3) |
| 1.4.0 / 0.9.0-RC | contratos T4 | + reporting1.1→1.3, comunicacion1.2 | Iter17–20 (R4) |
| 1.5.0 / **1.0.0** | contratos congelados | integración + pruebas de sistema | Iter21 (R5, 15/12) |

## 5. Matriz global: componente × iteración (versión que se construye)

| Iter (CDU) | catalog | segur | agendas | solicit | citas | pacientes | comunic | informes | integrac | reporting | common | server | client |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 0 (—) | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 0.1.0 | 0.1.0 |
| 1 (16) | **1.1.0** | | | | | | | | | | 1.1.0-SN | | |
| 2 (17) | | **1.1.0** | | | | | | | | | | | |
| 3 (13) | | | **1.1.0** | | | | | | | | | | |
| 4 (06) | | | | **1.1.0** | | | | | | | | | |
| 5 (12) | | | | | **1.1.0** | | | | | | | | |
| 6 (19) | | | | | | | | | **1.1.0** | | →R1: 1.1.0 | 0.2.0-a | 1.1.0 |
| 7 (14) | | | | | **1.2.0** | | | | | | | | |
| 8 (11) | | | | | | **1.1.0** | | | **1.2.0** | | | | |
| 9 (01) | | | | | **1.3.0** | | | | | | | | |
| 10 (02) | | | | | | | **1.1.0** | | | | | | |
| 11 (07) | | | | **1.2.0** | | | | | | | | | |
| 12 (08) | | | **1.2.0** | **1.3.0** | | | | | | | →R2: 1.2.0 | 0.3.0 | 1.2.0 |
| 13 (03) | | | | | | **1.2.0** | | | | | | | |
| 14 (05) | | | | **1.4.0** | | | | | | | | | |
| 15 (18) | | **1.2.0** | | | | | | | | | | | |
| 16 (09) | | | | | | | | **1.1.0** | **1.3.0** | | →R3: 1.3.0 | 0.4.0-b | 1.3.0 |
| 17 (10) | | | | | | | | | | **1.1.0** | | | |
| 18 (04) | | | | | | | **1.2.0** | | | | | | |
| 19 (15) | | | | | | | | | | **1.2.0** | | | |
| 20 (20) | | | | | | | | | | **1.3.0** | →R4: 1.4.0 | 0.9.0-RC | 1.4.0 |
| 21 (—) | | | | | | | | | | | 1.5.0 | **1.0.0** | **1.5.0** |

## 6. Resumen de esfuerzo y tokens por componente (desarrollo, sin Iter0/21)

| Componente | Versiones 1.x | Horas | Coste | Tokens | % esfuerzo |
|---|---|---|---|---|---|
| iso2-solicitudes | 1.1→1.4 | 116 h | 2.900 € | ~495k | 24% |
| iso2-citas | 1.1→1.3 | 80 h | 2.000 € | ~345k | 16% |
| iso2-seguridad | 1.1→1.2 | 44 h | 1.100 € | ~190k | 9% |
| iso2-pacientes | 1.1→1.2 | 44 h | 1.100 € | ~175k | 9% |
| iso2-agendas | 1.1 (colab 1.2) | 36 h | 900 € | ~170k | 7% |
| iso2-integracion | 1.1→1.3 | 36 h | 900 € | ~150k | 7% |
| iso2-comunicacion | 1.1→1.2 | 36 h | 900 € | ~150k | 7% |
| iso2-informes | 1.1 | 36 h | 900 € | ~180k | 7% |
| iso2-reporting | 1.1→1.3 | 42 h | 1.050 € | ~165k | 9% |
| iso2-catalogo | 1.1 | 22 h | 550 € | ~100k | 4% |
| **TOTAL componentes** | 22 incrementos | **492 h** | **12.300 €** | **≈2,1M tokens** (repartidos en ≈30 sesiones) | 100% |

Con Iter0 (1.000 €) e Iter21 (2.000 €): **coste humano total ≈ 15.300 €** + contingencia 10% = **≈16.530 €** (coherente con docs/planificacion-PUD.md).

**Decisión de inversión por iteración** (para el Kanban): cada iteración fija la versión objetivo
de 1–2 componentes (columna en negrita de la matriz §5); el criterio de aceptación de la iteración
es *esa versión funcionando con sus tests* — así la evolución del componente y la agenda coinciden.
