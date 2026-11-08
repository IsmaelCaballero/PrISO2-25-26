# Planificación del proyecto según el PUD — versión +Calidad (v2.0.0)

**Proyecto:** Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM
**Grupo:** ISO2-2026-27 · **Equipo:** 6 miembros
**Entrega Teoría 1:** 13/11/2026 · **Entrega práctica:** 15/12/2026
**Versión del documento:** 2.0.0 (rev. 07/11/2026) — es la **nueva versión** de
`docs/planificacion-PUD.md` (v1.1.0): conserva íntegros el plan PUD, la estimación y la agenda,
e incorpora los dos planes que faltaban: el **plan de calidad** (§8, D24) y el **plan de gestión
de configuración** (§9, D25–D27), además de etiquetar **alfa / beta / definitiva** las liberaciones (§4).
Documentos de detalle: `SOFTWAREQUALITY.md` (calidad por componente y por CDU),
`docs/mapa-configuraciones.md` (+CSV) y `docs/componentes-versionado+Calidad.md`.
Las versiones anteriores quedan en el historial Git (rama `feature/docs-plan-inicial`, PR #1).

---

## 1. Supuestos del plan (igual que v1.1.0)

| # | Supuesto | Justificación |
|---|----------|---------------|
| S1 | 1 Requisito Funcional = 1 Caso de Uso (1:1) | Recomendación explícita del enunciado para simplificar el mapeo RF↔CU. |
| S2 | 1 Caso de Uso = 1 Iteración (1:1) | Ídem. La incrementalidad se manifiesta en la evolución semver de los componentes Maven. |
| S3 | Iteración 0 (fase Inicio) = 1.000 € | Fijado por el enunciado (40 h humanas a 25 €/h). |
| S4 | Iteración N=21 (fase Transición) = 2.000 € | Fijado por el enunciado (≈80 h: integración, pruebas de sistema, despliegue, manual). |
| S5 | Tarifa humana: 25 €/h | Perfil de ingeniero junior. |
| S6 | Semana = 40 h; equipo = 6 personas (240 h-persona/semana) | Fijado por el enunciado. |
| S7 | Equipo en **3 parejas** ejecutando en paralelo una iteración de CU cada una | Máx. 3 iteraciones simultáneas; 2 iteraciones cortas/semana por pareja. |
| S8 | Calendario: Iter0 19–25/10 · Elaboración 09–20/11 · Construcción 23/11–11/12 · Transición 07–14/12 · Entrega 15/12 | Calendario real del cuatrimestre. |
| S9 | Esfuerzo humano **con agentes**: Baja 14 h · Media 22 h · Alta 36 h | Los agentes eliminan codificación; persiste análisis, diseño de interfaces y revisión. |
| S10 | Sin agentes (comparativa): Baja 24 h · Media 40 h · Alta 64 h | Escenario tradicional. |
| S11 | Contingencia 10% sobre iteraciones de CU | Re-trabajo por revisión de código generado y curva Git/Maven. |
| S12 | IA: Copilot campus UCLM (0 €) + modelos gratuitos vía OpenCode | Se documenta coste nominal de mercado (~430 €). |
| **S13** *(nueva)* | **Modelo de calidad ISO/IEC 25010 acotado a 5 características** (D24) | Un proyecto de 5 semanas no maximiza las 8: se maximiza Adecuación Funcional, Usabilidad, Fiabilidad, Seguridad y Mantenibilidad; el resto se cubre de forma básica. |
| **S14** *(nueva)* | **Cada iteración produce una configuración mínima identificada (CFG-NN) desplegable y reproducible** (D25–D26) | Hace verificable la incrementalidad PUD y permite anticipar con criterios objetivos las versiones alfa/beta/definitiva. |

## 2. Catálogo RF → CDU (mapeo 1:1)

Sin cambios respecto de v1.1.0: 20 RF (5 paciente + 5 médico + 5 administrativo + 5 administrador)
mapeados 1:1 a CDU01–CDU20 con prioridad de negocio y riesgo PUD. Ver `docs/planificacion-PUD.md` §2
(idéntico); la columna de **exigencia de calidad por CDU** se añade en `SOFTWAREQUALITY.md` §3.

**Criterio de selección para Elaboración:** CDU16, CDU13, CDU12, CDU06, CDU17 y CDU19 (spike) adelantan
por mitigación de riesgos arquitectónicos.

## 3. Plan de fases, iteraciones y liberaciones

### 3.1 Agenda de iteraciones (idéntica a v1.1.0 + columnas de configuración y calidad)

Versión máquina-legible: `docs/planificacion-iteraciones+Calidad.csv` (añade `cfg_id`, `tag_configuracion`,
`foco_calidad_iso25010`, `pruebas_calidad`, `puerta_de_calidad`, `etiqueta_release`).

| Iter. | CDU | Semana | P | Cx | h | € | Componente Maven | CFG | Release · etiqueta |
|---|---|---|---|---|---|---|---|---|---|
| **0** | — (visión, RUP, arquitectura) | 19–25/10 | Todos | — | 40 | 1.000 | parent + common 1.0.0 + baseline 1.0.0 de 10 módulos (apps 0.1.0) | CFG-00 | **R0** 25/10 `v0.1.0` |
| **1** | CDU16 Catálogo/parámetros | 09–13/11 | P-A | M | 22 | 550 | `iso2-catalogo` 1.1.0 | CFG-01 | interna |
| **2** | CDU17 Roles y permisos (RBAC) | 09–13/11 | P-B | M | 22 | 550 | `iso2-seguridad` 1.1.0 | CFG-02 | interna |
| **3** | CDU13 Agendas | 09–13/11 | P-C | A | 36 | 900 | `iso2-agendas` 1.1.0 | CFG-03 | interna |
| **4** | CDU06 Solicitud/prescripción | 16–20/11 | P-A | A | 36 | 900 | `iso2-solicitudes` 1.1.0 | CFG-04 | interna |
| **5** | CDU12 Gestión de citas | 16–20/11 | P-B | A | 36 | 900 | `iso2-citas` 1.1.0 | CFG-05 | interna |
| **6** | CDU19 Integraciones (spike) | 16–20/11 | P-C | A | 36 | 900 | `iso2-integracion` 1.1.0 · common 1.1.0 | CFG-06 | **R1 20/11 · v0.2.0-alpha (ALFA 1)** |
| **7** | CDU14 Listas de espera | 23–27/11 | P-A | M | 22 | 550 | `iso2-citas` 1.2.0 | CFG-07 | interna (alfa en curso) |
| **8** | CDU11 CRUD pacientes | 23–27/11 | P-B | M | 22 | 550 | `iso2-pacientes` 1.1.0 · `iso2-integracion` 1.2.0 | CFG-08 | interna |
| **9** | CDU01 Citas portal paciente | 23–27/11 | P-C | M | 22 | 550 | `iso2-citas` 1.3.0 | CFG-09 | interna |
| **10** | CDU02 Notificaciones | 23–27/11 | P-A | M | 22 | 550 | `iso2-comunicacion` 1.1.0 | CFG-10 | interna |
| **11** | CDU07 Seguimiento periódico | 23–27/11 | P-B | A | 36 | 900 | `iso2-solicitudes` 1.2.0 | CFG-11 | interna |
| **12** | CDU08 Estado solicitudes/agendas | 23–27/11 | P-C | M | 22 | 550 | `iso2-solicitudes` 1.3.0 · `iso2-agendas` 1.2.0 · common 1.2.0 | CFG-12 | **R2 27/11 · v0.3.0-alpha (ALFA consolidada)** |
| **13** | CDU03 Historial de pruebas | 30/11–04/12 | P-A | M | 22 | 550 | `iso2-pacientes` 1.2.0 | CFG-13 | interna (beta en curso) |
| **14** | CDU05 Revisiones/seguimiento | 30/11–04/12 | P-B | M | 22 | 550 | `iso2-solicitudes` 1.4.0 | CFG-14 | interna |
| **15** | CDU18 Auditoría de accesos | 30/11–04/12 | P-C | M | 22 | 550 | `iso2-seguridad` 1.2.0 | CFG-15 | interna |
| **16** | CDU09 Informes e imágenes | 30/11–04/12 | P-A | A | 36 | 900 | `iso2-informes` 1.1.0 · `iso2-integracion` 1.3.0 | CFG-16 | interna |
| **17** | CDU10 Estadísticas médico | 30/11–04/12 | P-B | B | 14 | 350 | `iso2-reporting` 1.1.0 | CFG-17 | interna |
| **18** | CDU04 Incidencias/encuestas | 30/11–04/12 | P-C | B | 14 | 350 | `iso2-comunicacion` 1.2.0 · common 1.3.0 | CFG-18 | **R3 04/12 · v0.4.0-beta (BETA)** |
| **19** | CDU15 Informes operativos | 07–11/12 | P-A | B | 14 | 350 | `iso2-reporting` 1.2.0 | CFG-19 | interna (RC en curso) |
| **20** | CDU20 Estadísticas globales | 07–11/12 | P-B | B | 14 | 350 | `iso2-reporting` 1.3.0 · common 1.4.0 | CFG-20 | **R4 11/12 · v0.9.0-rc (CANDIDATA)** |
| **21** | — (integración, pruebas de sistema, despliegue, manual) | 07–14/12 | Todos | — | 80 | 2.000 | common 1.5.0 · server-app **1.0.0** · client-app **1.5.0** | CFG-21 | **R5 15/12 · v1.0.0 (DEFINITIVA)** |

> Regla del enunciado respetada: en cada iteración se trabaja exclusivamente su CDU; la incrementalidad
> está en la evolución semver de los componentes (D1, D22, D23 — la inversión Iter7↔9 ya está aplicada).

### 3.2 Liberaciones etiquetadas (novedad: clasificación alfa/beta/definitiva)

| Release | CFG hito | Fecha | Versión | **Clasificación** | Contenido y criterio cumplido |
|---|---|---|---|---|---|
| R0 | CFG-00 | 25/10 | `0.1.0` | interna (línea base) | Esqueleto multi-módulo + contratos + CI |
| R1 | CFG-06 | 20/11 | `0.2.0-alpha` | 🅐 **ALFA 1** | flujo login→catálogo→agenda→solicitud→cita end-to-end + spike integración; JaCoCo ≥60 % |
| R2 | CFG-12 | 27/11 | `0.3.0-alpha` | 🅐 **ALFA consolidada** | + pacientes, listas de espera, self-service, notificaciones, recurrencias, tracking; 4/5 roles; JaCoCo ≥70 % |
| R3 | CFG-18 | 04/12 | `0.4.0-beta` | 👻 **BETA** | alcance clínico+imagen completo (CDU03/05/18/09/10/04); modo degradado probado; JaCoCo ≥75 %; copia/restore ensayado |
| R4 | CFG-20 | 11/12 | `0.9.0-rc` | 🏁 **CANDIDATA** | 20/20 CDU; congelación de código; trazabilidad RF→CU→TC completa; JaCoCo ≥78 % |
| R5 | CFG-21 | 15/12 | `1.0.0` | ✅ **DEFINITIVA** | pruebas de sistema con las 2 apps; JaCoCo ≥80 %; manual + instalador; 0 defectos críticos |

Cada escalón se declara **por criterios cumplidos, no por fecha** (detalle en `docs/mapa-configuraciones.md` §3).
Si una CFG no los cumple, la etiqueta se retrasa y se registra en `STATUS.md`.

## 4. Estimación de esfuerzo y coste (idéntica a v1.1.0)

- Humano: **612 h con agentes** (40 + 188 + 304 + 80) vs 1.000 h sin agentes (−39 %).
- Coste: 1.000 + 4.700 + 7.600 + 2.000 + contingencia 1.230 = **≈ 16.530 €** (sin IA: ≈ 26.230 €).
- Tokens: ≈ 2,1M en ≈ 30 sesiones de agente (D19: Baja 50–70k · Media 90–130k · Alta 150–220k, partidas en 2).
- La **calidad no es una iteración aparte**: sus actividades están incorporadas en las horas de cada
  iteración (diseño humano de TC, revisión de arquitectura, puertas de CI) y en las 80 h fijas de Iter21
  (pruebas de sistema, usabilidad, copia/restore). Por eso el presupuesto no cambia respecto de v1.1.0.

## 5. Arquitectura y módulos Maven

14 módulos (parent + common + 10 dominio + 2 aplicaciones) — idéntico a v1.1.0 §5 y a
`docs/componentes-versionado.md` §1. Reglas-arquitectura (guardarailes) extendidas en la versión
+Calidad con la verificación automática (§8.4): las 5 reglas se comprueban con **ArchUnit** en CI,
no solo por convención.

## 6. Roles: estudiante vs agente (igual que v1.1.0)

Ver `docs/planificacion-PUD.md` §6. Añadido de calidad (D24): una persona por release hace de
**guardián de calidad** — ejecuta las puertas, revisa que ningún PR se fusione sin CI verde y
publica el informe de métricas en `STATUS.md`. Ningún miembro se auto-fusiona su PR.

## 7. Riesgos del plan y mitigaciones (igual que v1.1.0)

Se mantiene la tabla de v1.1.0 §7. Añadido: el plan de calidad (§8) y las puertas por release son la
mitigación de sistema contra el riesgo "vibe coding / deuda técnica"; el mapa de configuraciones (§9)
es la mitigación del riesgo de regresión y del rollback imposible.

---

## 8. PLAN DE CALIDAD (D24) — nuevo en v2.0.0

### 8.1 Objetivo de calidad y norma de precedencia

Sistema clínico del SESCAM (RGPD/LOPDGDD): **la calidad funcional y la seguridad no negocian**; el
resto de metas se gradúa por release. Regla de precedencia cuando un CDU va justo de tiempo:
*corrección y seguridad* > *protección contra errores* > *resto de usabilidad* > *prestación*.
Un CDU no pasa a "Hecha" si su exigencia de calidad verificable (columna del CSV +Calidad) no está probada.

### 8.2 Modelo de calidad: ISO/IEC 25010 acotado y medible

Características maximizadas (definición, medida y detalle por componente/CDU en `SOFTWAREQUALITY.md`):

| Característica | Subcaracterísticas maximizadas | Instrumento de verificación |
|---|---|---|
| Adecuación funcional | completitud · corrección · pertinencia | TC humanos por RF (1 feliz + ≥2 error + 1 permisos); matriz RF→CU→TC; 0 defectos abiertos por RF |
| Usabilidad | operabilidad · protección contra errores | revisión heurística por rol (Iter21 y por release); TC de validación/confirmación en JavaFX |
| Fiabilidad | disponibilidad · tolerancia a fallos · recuperabilidad | TC de concurrencia (hueco), TC de adaptador externo caído, ensayo de copia/restore H2 |
| Seguridad | confidencialidad · integridad · **no repudio** · **responsabilidad** | TC RBAC deny-by-default por endpoint; `@Auditable` en toda mutación; auditoría con hash encadenado |
| Mantenibilidad | **modularidad (MVC + SOLID)** · analizabilidad · testabilidad | ArchUnit (capas y fronteras de módulo); JaCoCo; Javadoc 100 % en contratos; Checkstyle/SpotBugs |

### 8.3 Actividades de calidad por fase PUD

| Fase | Actividades de calidad RADIT |
|---|---|
| Inicio (Iter0) | congelación de contratos → revisabilidad; reglas ArchUnit escritas **antes** que el código; CI con JaCoCo; definición de puertas por release |
| Elaboración (Iter1–6) | TC diseñados por humanos **antes** del prompt; tests de concurrencia del hueco (CDU12); tests RBAC en todos los endpoints publicados; spike de integración con idempotencia probada; alfa 1 con flujo end-to-end |
| Construcción (Iter7–20) | repetición del ciclo TC→generación→revisión; tests de modo degradado en cada adaptador; auditoría con hash encadenado (CDU18); alfa consolidada y beta con usabilidad revisada por los 5 roles; revisión cruzada de PR entre parejas |
| Transición (Iter21) | pruebas de sistema con `server-app 1.0.0` + `client-app 1.5.0`; OWASP dependency-check; ensayo documentado de copia/restore; UAT simulada con los 5 roles; informe final JaCoCo/Surefire |

### 8.4 Métricas y puertas (gates) por release

| Puerta | R1 α | R2 α | R3 β | R4 RC | R5 final |
|---|---|---|---|---|---|
| JaCoCo línea (global) | ≥60 % | ≥70 % | ≥75 % | ≥78 % | **≥80 %** (≥85 % citas/solicitudes/seguridad) |
| JaCoCo rama | ≥45 % | ≥55 % | ≥65 % | ≥70 % | ≥70 % |
| Surefire fallos · ArchUnit violaciones · Checkstyle/SpotBugs bloqueantes | 0 | 0 | 0 | 0 | 0 |
| TC RBAC por endpoint · `@Auditable` en mutaciones | 100 % | 100 % | 100 % | 100 % | 100 % |
| TC de fallo de adaptador externo | — | — | obligatorio | obligatorio | obligatorio |
| Trazabilidad RF→CU→TC | 6/20 | 12/20 | 18/20 | 20/20 | 20/20 |
| Copia/restore H2 | — | — | ensayo | ensayo | ensayo documentado |
| Revisión de usabilidad por rol | 1 rol | 3 roles | 5 roles | 5 roles | 5 roles + manual |

*Definition of done* +calidad de cada iteración (8 puntos): ver `SOFTWAREQUALITY.md` §1.

## 9. PLAN DE GESTIÓN DE CONFIGURACIÓN (D25–D27) — nuevo en v2.0.0

### 9.1 Ítems de configuración

14 módulos Maven (código, SemVer en POM) + contratos de `iso2-common` + scripts `db/migration/V<versión>__<cdu>.sql`
+ suites JUnit/informes + diagramas PlantUML→StarUML (`docs/modelo/iterNN/`) + documentación viva
(`DECISIONS/PLAN/STATUS/SOFTWAREQUALITY`) + CI/build. Todo en Git; nada fuera del repositorio.

### 9.2 Identificación y etiquetado

- **CFG-NN = configuración mínima de la iteración NN** (el conjunto exacto de componentes con su versión
  que construye esa iteración). Tag anotado **`cfg-iterNN`** en cada merge de iteración a `develop`.
- Etiquetas de release: `v0.1.0` (R0) → `v0.2.0-alpha` (R1) → `v0.3.0-alpha` (R2) → `v0.4.0-beta` (R3)
  → `v0.9.0-rc` (R4) → `v1.0.0` (R5) sobre `main` con merge `--no-ff`; rama `release/Rn` para estabilizar.
- **Ningún `SNAPSHOT` en una CFG etiquetada** (versions-maven-plugin al etiquetar).
- Mapa completo CFG-00…CFG-21 con versiones y clasificación: `docs/mapa-configuraciones.md` (+CSV).

### 9.3 Flujos de control de cambios y de versión

1. Un CDU = rama `feature/iterXX-CDUyy-…` desde `develop`; PR con issue enlazado; revisión cruzada
   (otra pareja) + CI verde; **merge solo lo aprueba Ismael** (D14).
2. Merge a `develop` → tag `cfg-iterNN` → **auditoría de configuración** (el guardián verifica que las
   versiones del POM agregador coinciden con la matriz `componentes-versiones+Calidad.csv`).
3. CFG hito → `release/Rn` → ajustes de versión de apps → tag `v…` → `main` → backport de fixes a `develop`.
4. Hotfix pos-beta: `0.4.x`/`1.0.x` en rama `hotfix/…` + backport (las *major* solo en Iter21, D22).

### 9.4 Ambientes, despliegue y rollback

Ambientes `dev` (SNAPSHOT) → `test` (CFG recién etiquetada) → `alfa` (0.2.0/0.3.0-alpha) →
`beta` (0.4.0-beta) → `demo/producción académica` (0.9.0-rc, 1.0.0); H2 en fichero + copia diaria desde alfa.
**Rollback** = `git checkout cfg-iterNN` + `mvn clean verify` + desplegar ese par server/client + copia H2
asociada. Cada release publica `DEPLOY-<versión>.md` con la lista exacta de componentes (`mvn dependency:list`).

---

## 10. Trazabilidad y entregables de este documento

| Elemento del enunciado | Dónde se cumple |
|---|---|
| Plan PUD (fases, iteraciones, releases, esfuerzo, coste, riesgos) | §1–§7 (idéntico a v1.1.0, releases ahora etiquetadas alfa/beta/definitiva) |
| Plan de gestión de configuración | §9 + `docs/mapa-configuraciones.md`/CSV + `docs/componentes-versionado+Calidad.md` |
| Plan de calidad | §8 + `SOFTWAREQUALITY.md` (modelo ISO 25010, impacto por componente y por CDU, puertas) |
| Anticipar versiones alfa/beta/definitiva sobre las configuraciones mínimas | §3.2 + mapa CFG (criterios por escalón, no solo fecha) |
| Versiones máquina-legibles | `docs/planificacion-iteraciones+Calidad.csv`, `docs/componentes-versiones+Calidad.csv`, `docs/mapa-configuraciones.csv` |
