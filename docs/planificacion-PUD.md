# Planificación del proyecto según el Proceso Unificado de Desarrollo (PUD)

**Proyecto:** Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM
**Grupo:** ISO2-2026-27 · **Equipo:** 6 miembros
**Entrega Teoría 1:** 13/11/2026 · **Entrega práctica:** 15/12/2026
**Versión del documento:** 1.1.0 (rev. 07/11/2026: arquitectura servidor+cliente D10 y esquema de
evolución de componentes por riesgo D22/D23; ver §5 y `docs/componentes-versionado.md`)

---

## 1. Supuestos del plan (justificados)

Estos supuestos quedan también reflejados en `DECISIONS.md` y en la wiki del repositorio.

| # | Supuesto | Justificación |
|---|----------|---------------|
| S1 | 1 Requisito Funcional = 1 Caso de Uso (1:1) | Recomendación explícita del enunciado para simplificar el mapeo RF↔CU. |
| S2 | 1 Caso de Uso = 1 Iteración (1:1) | Ídem. Reduce el PUD a un ciclo casi-cascada, pero cada iteración alimenta un **componente/módulo Maven** que evoluciona, preservando la naturaleza incremental. |
| S3 | Iteración 0 (fase Inicio) = 1.000 € | Fijado por el enunciado. Equivale a 40 h humanas a 25 €/h (1 persona-semana), coherente con visión + RUP de alto nivel + arquitectura candidata. |
| S4 | Iteración N=21 (fase Transición) = 2.000 € | Fijado por el enunciado. ≈80 h humanas: integración final, pruebas de sistema, despliegue, manual, wiki. |
| S5 | Tarifa humana: 25 €/h | Perfil de ingeniero junior; todas las iteraciones de CU se valoran con esta tarifa. |
| S6 | Semana humana = 40 h laborables. Equipo = 6 personas → 240 h-persona/semana. | Fijado por el enunciado. |
| S7 | El equipo se organiza en **3 parejas** (roles cruzados) y cada pareja ejecuta en paralelo una iteración de CU. | Máx. 3 iteraciones simultáneas; con agentes trabajando 24x7 y puertas de revisión humana diaria, cada pareja completa 2 iteraciones cortas/semana. |
| S8 | Calendario: Iter 0 ejecutada en octubre (19–25/10). Elaboración: 09–20/11. Construcción: 23/11–04/12. Transición: 07–14/12. Entrega: 15/12. | Coherente con el calendario real del cuatrimestre y con la fecha de entrega. |
| S9 | Complejidad de CU → esfuerzo humano **con agentes**: Baja=14 h, Media=22 h, Alta=36 h (incluye análisis, diseño de interfaces por el estudiante, generación del agente y **revisión/auditoría** humana). | Estimación por juicio de ingeniería: los agentes eliminan gran parte de la codificación, pero el tiempo de revisión e inspección persiste (el enunciado avisa del sesgo de optimismo de la IA). |
| S10 | Complejidad sin agentes (escenario comparativo): Baja=24 h, Media=40 h, Alta=64 h. | Base del esfuerzo tradicional (implementación "a mano") para la comparación con/sin IA. |
| S11 | Reserva de contingencia del 10% sobre las iteraciones de CU. | Cubre re-trabajo por revisiones fallidas de código generado, curva de Git/Maven y reprogramaciones. |
| S12 | Herramientas IA: GitHub Copilot (licencia campus UCLM, coste 0 €) y modelos gratuitos vía OpenCode. Se documenta coste de mercado de referencia. | El coste real es ~0 €, pero se cuantifica el coste nominal para el análisis económico. |

---

## 2. Catálogo de Requisitos Funcionales → Casos de Uso (mapeo 1:1)

Del enunciado se extraen **20 requisitos funcionales** (5 pacientes + 5 médicos + 5 administración + 5 administradores). Cada uno se mapea a un caso de uso homónimo.

| RF | Caso de Uso (CDU) | Actor principal | Prioridad negocio | Riesgo/Complejidad (PUD) |
|----|-------------------|-----------------|-------------------|--------------------------|
| RF01 | CDU01 Consultar y gestionar mis citas (confirmar, cambiar, cancelar) | Paciente | Alta | Media |
| RF02 | CDU02 Recibir notificaciones y recordatorios | Paciente | Alta | Media (infraestructura de mensajería) |
| RF03 | CDU03 Gestionar historial de pruebas realizadas | Paciente | Media | Media |
| RF04 | CDU04 Comunicar incidencias y responder encuestas | Paciente | Baja | Baja |
| RF05 | CDU05 Consultar pruebas de seguimiento y revisiones periódicas | Paciente | Media | Media |
| RF06 | CDU06 Solicitar/prescribir pruebas con indicación clínica y prioridad | Médico | **Crítica** | **Alta** (núcleo del dominio) |
| RF07 | CDU07 Prescribir pruebas de seguimiento con periodicidad (genera nuevas citas) | Médico | Alta | Alta (motor de recurrencias) |
| RF08 | CDU08 Consultar estado de solicitudes y agendas disponibles | Médico | Alta | Media |
| RF09 | CDU09 Consultar informes e imágenes (visor DICOM) | Médico | Alta | **Alta** (PACS/DICOM) |
| RF10 | CDU10 Consultar estadísticas de sus solicitudes | Médico | Baja | Baja |
| RF11 | CDU11 Alta/modificación/baja de pacientes | Administrativo | Alta | Media (integra con Admisión) |
| RF12 | CDU12 Gestionar citas: asignar, reprogramar, cancelar | Administrativo | **Crítica** | **Alta** (motor de asignación de huecos) |
| RF13 | CDU13 Gestionar agendas de médicos y técnicos | Administrativo | **Crítica** | **Alta** (base sobre la que se asignan citas) |
| RF14 | CDU14 Gestionar listas de espera, prioridades y huecos | Administrativo | Alta | Media |
| RF15 | CDU15 Generar informes operativos (ocupación, esperas, cancelaciones) | Administrativo | Media | Baja |
| RF16 | CDU16 Configurar parámetros: tipos de prueba, protocolos, políticas | Administrador | **Crítica** | Media (columna vertebral del dominio: catálogo de pruebas) |
| RF17 | CDU17 Gestionar roles y permisos (RBAC + SSO) | Administrador | **Crítica** | Media (transversal de seguridad) |
| RF18 | CDU18 Auditar actividad y accesos a datos clínicos | Administrador | Alta | Media (trazabilidad LOPDGDD/RGPD) |
| RF19 | CDU19 Integrar con sistemas externos (SSO, HCE, Admisión, RIS/PACS, HL7/FHIR/DICOM) | Administrador | Alta | **Alta** (máximo riesgo técnico del proyecto) |
| RF20 | CDU20 Generar estadísticas globales y reportes de uso | Administrador | Baja | Baja |

**Criterio de selección para Elaboración (mitigación de riesgos):** se adelantan los CUs que sostienen la arquitectura y concentran el riesgo técnico: CDU16 (catálogo = modelo de dominio), CDU13 + CDU12 (motores de agenda/cita = núcleo transaccional), CDU06 (flujo clínico origen de todo), CDU17 (seguridad transversal) y CDU19 como *spike* de máxima incertidumbre (FHIR/SSO). El resto va a Construcción.

---

## 3. Plan de fases, iteraciones y liberaciones (PUD)

**Cadencia:** las iteraciones de Elaboración duran 1 semana (3 en paralelo, una por pareja). En Construcción, cada pareja encadena 2 iteraciones cortas/semana (los agentes generan de madrugada; el humano revisa y aprueba por la mañana). Máximo 6 iteraciones/semana.

| Iter. | CDU asociado | Semana (fechas) | Pareja | Cplx | Esfuerzo (h c/IA) | Coste € | Componente Maven generado | Release |
|-------|--------------|-----------------|--------|------|-------------------|---------|---------------------------|---------|
| **0** | — (visión, RUP preliminar, arquitectura candidata) | S0: 19–25/10 | Todos | — | 40 h (fijo) | **1.000** | `iso2-parent` + esqueleto multi-módulo + `iso2-common` 1.0.0 + **baseline contractual 1.0.0** de los 10 componentes de dominio (apps: 0.1.0) | **R0** 25/10 (v0.1.0) |
| **1** | CDU16 Catálogo/parámetros | S1: 09–13/11 | P-A | M | 22 | 550 | `iso2-catalogo` 1.1.0 | |
| **2** | CDU17 Roles y permisos (RBAC+SSO) | S1: 09–13/11 | P-B | M | 22 | 550 | `iso2-seguridad` 1.1.0 | |
| **3** | CDU13 Agendas médicos/técnicos | S1: 09–13/11 | P-C | A | 36 | 900 | `iso2-agendas` 1.1.0 | |
| **4** | CDU06 Solicitud/prescripción de pruebas | S2: 16–20/11 | P-A | A | 36 | 900 | `iso2-solicitudes` 1.1.0 | |
| **5** | CDU12 Gestión de citas (asignación) | S2: 16–20/11 | P-B | A | 36 | 900 | `iso2-citas` 1.1.0 | |
| **6** | CDU19 Integraciones (spike SSO+HCE/FHIR+DICOM) | S2: 16–20/11 | P-C | A | 36 | 900 | `iso2-integracion` 1.1.0 | **R1** 20/11 (v0.2.0) — *línea base de arquitectura* |
| **7** | CDU14 Listas de espera y huecos | S3: 23–27/11 | P-A | M | 22 | 550 | `iso2-citas` 1.2.0 | |
| **8** | CDU11 CRUD de pacientes | S3: 23–27/11 | P-B | M | 22 | 550 | `iso2-pacientes` 1.1.0; `iso2-integracion` 1.2.0 (Admisión) | |
| **9** | CDU01 Citas del portal paciente | S3: 23–27/11 | P-C | M | 22 | 550 | `iso2-citas` 1.3.0 (self-service paciente) | |
| **10** | CDU02 Notificaciones y recordatorios | S3: 23–27/11 | P-A | M | 22 | 550 | `iso2-comunicacion` 1.1.0 | |
| **11** | CDU07 Seguimiento periódico (recurrencias) | S3: 23–27/11 | P-B | A | 36 | 900 | `iso2-solicitudes` 1.2.0 | |
| **12** | CDU08 Estado de solicitudes y agendas | S3: 23–27/11 | P-C | M | 22 | 550 | `iso2-solicitudes` 1.3.0; `iso2-agendas` 1.2.0 | |
| **13** | CDU03 Historial de pruebas del paciente | S4: 30/11–04/12 | P-A | M | 22 | 550 | `iso2-pacientes` 1.2.0 | |
| **14** | CDU05 Información de seguimiento/revisiones | S4: 30/11–04/12 | P-B | M | 22 | 550 | `iso2-solicitudes` 1.4.0 | |
| **15** | CDU18 Auditoría de accesos | S4: 30/11–04/12 | P-C | M | 22 | 550 | `iso2-seguridad` 1.2.0 | |
| **16** | CDU09 Informes e imágenes (visor DICOM) | S4: 30/11–04/12 | P-A | A | 36 | 900 | `iso2-informes` 1.1.0; `iso2-integracion` 1.3.0 (DICOM) | |
| **17** | CDU10 Estadísticas del médico | S4: 30/11–04/12 | P-B | B | 14 | 350 | `iso2-reporting` 1.1.0 | |
| **18** | CDU04 Incidencias y encuestas | S4: 30/11–04/12 | P-C | B | 14 | 350 | `iso2-comunicacion` 1.2.0 | |
| **19** | CDU15 Informes operativos | S5: 07–11/12 | P-A | B | 14 | 350 | `iso2-reporting` 1.2.0 | |
| **20** | CDU20 Estadísticas globales | S5: 07–11/12 | P-B | B | 14 | 350 | `iso2-reporting` 1.3.0 | **R4** 11/12 (v0.9.0-RC) |
| **21** | — (integración final, pruebas de sistema, despliegue, manual, wiki) | S6: 07–14/12 | Todos | — | 80 h (fijo) | **2.000** | Integración → `iso2-server-app` 1.0.0 + `iso2-client-app` 1.5.0 | **R5** 15/12 (v1.0.0) |

> **Regla del enunciado respetada:** en cada iteración se trabaja exclusivamente en su CDU. La incrementalidad se manifiesta en la **evolución semver de los componentes**: un CDU posterior no rompe el módulo anterior, le añade una *minor* a través de interfaces (programación orientada a interfaces, MVC, SOLID).

### Liberaciones (releases)

| Release | Fecha | Contenido | Versión global |
|---------|-------|-----------|----------------|
| R0 | 25/10/2026 | Esqueleto multi-módulo Maven, `iso2-common` con interfaces de dominio, CI, wiki | 0.1.0 |
| R1 | 20/11/2026 | **Hito de Elaboración**: arquitectura validada, 5 riesgos mitigados (catálogo, RBAC, agenda, solicitud, cita, spike de integración) | 0.2.0 |
| R2 | 27/11/2026 | Funcionalidad núcleo ampliada (paciente, listas de espera, notificaciones, recurrencias) | 0.3.0 |
| R3 | 04/12/2026 | Funcionalidad completa restante (historial, auditoría, informes/DICOM, estadísticas básicas) | 0.4.0 |
| R4 | 11/12/2026 | Candidata a release: últimos CUs + estabilización | 0.9.0-RC |
| R5 | 15/12/2026 | **Entrega final**: integrada, probada, desplegada, con manual | 1.0.0 |

---

## 4. Estimación de esfuerzo y coste total

### 4.1 Esfuerzo humano (h) por disciplina y escenario

| Concepto | Sin agentes | Con agentes (S9) | Ahorro |
|----------|-------------|------------------|--------|
| Iter 0 (Inicio) — fijo enunciado | 40 h | 40 h | — |
| Elaboración (iters 1–6) | 336 h | 188 h | −44% |
| Construcción (iters 7–20) | 544 h | 304 h | −44% |
| Iter 21 (Transición) — fijo enunciado | 80 h | 80 h | — |
| **Total humano** | **1.000 h** | **612 h** | **−39%** |

El ahorro (~44% en las iteraciones de CU) procede de: el agente genera esqueletos e implementación a partir del prompt contextual persistente del CDU (que fija requisitos + interfaces diseñadas por el estudiante), y trabaja 24x7. **No** se ahorra el análisis, el diseño de interfaces, la priorización ni la revisión/auditoría del código (el "humano en el bucle"). El 10% de contingencia (S11) contrarresta el sesgo de optimismo de la IA.

### 4.2 Coste total del proyecto (toma de decisiones del equipo)

| Partida | Importe |
|---------|---------|
| Iter 0 (humanos) | 1.000 € |
| Elaboración (188 h × 25 €) | 4.700 € |
| Construcción (304 h × 25 €) | 7.600 € |
| Iter 21 Transición (humanos) | 2.000 € |
| Contingencia 10% sobre iteraciones de CU (492 h) | 1.230 € |
| Licencias IA: Copilot campus UCLM + modelos gratis OpenCode | 0 € (nominal de mercado: ~430 €: 6×19€×2 meses Copilot Business + ~200 € tokens API) |
| **TOTAL proyecto** | **≈ 16.530 €** (escenario sin IA comparable: ≈ 26.230 €) |

**Impacto en la agenda (el punto clave del Tema 1):** sin agentes, con cadencia máxima realista de 3 iteraciones/semana (1 por pareja, esfuerzo 24–64 h), los 20 CDU requerirían ~6,7 semanas: 09/11 + 7 semanas ≈ **fin de diciembre → no se cumpliría la entrega del 15/12**. Con agentes (generación 24x7 + revisión humana en tandas diarias) la cadencia sube a 6 iteraciones/semana y el proyecto **termina el 11/12 con 3 días de colchón**. La IA no solo abarata ~9.700 €: **comprime el camino crítico ~45% y hace viable el plazo**.

**Parámetro de selección de LLM (discusión exigida):** un modelo especializado en código (p. ej. Copilot integrado en VS Code o modelos de código abiertos vía OpenCode) reduce las rondas de revisión-fallo: cada ronda ahorrada en un CDU medio son ~4 h humanas (=100 €) frente a un coste marginal de tokens ≈ 0 € con licencia campus. Conclusión: prima el modelo con mejor tasa de acierto en Java/Maven sobre el más grande y caro; la decisión queda registrada en `DECISIONS.md`.

---

## 5. Arquitectura y módulos Maven (centrado en arquitectura)

Proyecto multi-módulo Maven (cap. 6 de *Maven by Examples*): un POM agregador `iso2-parent`, un módulo de contratos comunes y un módulo por componente, todos con versiones SemVer y comunicación **solo a través de interfaces** (mínimo acoplamiento / máxima cohesión, SOLID, MVC en cada módulo).

> **Actualización (D10, sesión 2):** el sistema se materializa en **dos aplicaciones**: una
> **servidora** (Spring Boot + H2, API REST/JSON) que ensambla los módulos de dominio, y una
> **cliente** (JavaFX) cuya interfaz varía según el **rol** autenticado. En v1 cada persona
> desempeña un único rol (credenciales locales en `iso2-seguridad`). Cada iteración desarrolla
> la parte servidora y la tajada cliente de su CDU (desglose en `PLAN.md`).

```
iso2-parent (agregador)
├── iso2-common        · contratos de dominio, DTO e interfaces compartidas (servidor ↔ cliente)
├── iso2-catalogo      · CDU16
├── iso2-seguridad     · CDU17 (+credenciales/login) → +CDU18
├── iso2-agendas       · CDU13 → +CDU08
├── iso2-solicitudes   · CDU06 → +CDU07 → +CDU08 → +CDU05
├── iso2-citas         · CDU12 → +CDU14 → +CDU01
├── iso2-integracion   · CDU19 → +CDU11 (Admisión) → +CDU09 (DICOM)
├── iso2-pacientes     · CDU11 → +CDU03
├── iso2-comunicacion  · CDU02 → +CDU04
├── iso2-informes      · CDU09
├── iso2-reporting     · CDU10 → +CDU15 → +CDU20
├── iso2-server-app    · ensamblado API REST Spring Boot — creado en Iter 0
└── iso2-client-app    · cliente JavaFX con vistas por rol — creado en Iter 0 (integración en Iter 21)
```

**Reglas-arquitectura (guardarailes de los agentes)** — se publican en `CONTEXT.md`/`DECISIONS.md` del SDD:
1. Prohibido acoplamiento directo entre módulos: solo interfaces de `iso2-common`.
2. Capas dentro de cada módulo: dominio / servicio (puerto) / adaptador / vista.
3. Nuevas funcionalidades = nuevas implementaciones; no se modifican contratos publicados (SemVer: *patch* arreglos, *minor* compatibilidad, *major* solo en Transición).
4. Datos clínicos: toda persistencia pasa por el control de acceso de `iso2-seguridad` (auditoría RGPD).
5. Integraciones externas solo a través de fachadas de `iso2-integracion` (DICOM/HL7/FHIR).

---

## 6. Roles: qué se delega al estudiante y qué al agente

| Actividad | Estudiantes (ingenieros) | Agentes IA |
|-----------|--------------------------|------------|
| Identificación y priorización de RF/CDU | ✅ Deciden y firman | Asesora |
| Planificación PUD, agenda, releases | ✅ | Compara/estimula |
| Diseño de interfaces y contratos (arquitectura) | ✅ "El ingeniero diseña las interfaces" | Revisa y sugiere |
| Redacción de prompts contextuales persistentes por CDU | ✅ | Ejecuta |
| Implementación (esqueleto y código) | Audita | ✅ Genera |
| Diseño de casos de prueba (unitarias/integración) | ✅ | ✅ Traduce a código de test |
| Ejecución de pruebas y CI | Supervisa fallos | ✅ |
| Commits, PR, issues, Kanban | ✅ (autor humano responsable) | Prepara borradores |

Trazabilidad exigida: cada iteración → hito en GitHub Projects → issues con plantilla rigurosa (objetivos, disciplinas RADIT, contratos involucrados, *definition of done*, estimación en horas con/agente) → commits/PR en ramas Git Flow → Insights. La wiki enlaza: este plan, `DECISIONS.md`, actas de reunión, especificaciones de CDU y ficheros Visual Paradigm.

---

## 7. Riesgos del plan y mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Sesgo de optimismo de la IA (subestimar revisión) | Retraso iteraciones | Contingencia 10%; puertas de revisión diarias; métricas de *rework* en Insights |
| Complejidad DICOM/PACS (CDU09, CDU19) | Sobre_coste | *Spike* anticipado en Elaboración (Iter 6); visor de código abierto como base |
| Curva de aprendizaje Git/Maven | Commits en main, rupturas | Ramas + PR obligatorios desde R0; mini-prácticas fuera del proyecto |
| Vibe coding / alucinaciones | Deuda técnica | Prompts persistentes por CDU + reglas de arquitectura (sección 5) + CI con checkstyle/spotbugs |
| Conflicto de calendario académico (examenes dic) | Paralelismo cae en S5–S6 | CUs de baja complejidad al final (iters 19–20) + 3 días de colchón |
```

