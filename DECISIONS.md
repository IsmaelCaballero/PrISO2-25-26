# DECISIONS.md — Decisiones y supuestos del proyecto

Registro de decisiones (estilo ADR) que usan los agentes como configuración persistente.
Responsable final: los 6 miembros del grupo. La IA propone; el humano decide y audita.

## D1. Mapeo 1RF : 1CDU y 1CDU : 1Iteración
- **Fecha:** 19/10/2026 · **Estado:** aceptada
- **Motivo:** simplificación sugerida en el enunciado. Se preserva la incrementalidad vía evolución semver de componentes.
- **Alternativa descartada:** agrupar CUs por módulo en una misma iteración (más PUD puro, pero peor trazabilidad 1:1 exigida en la evaluación).

## D2. Iteración 0 = 1.000 € · Iteración 21 (Transición) = 2.000 €
- Fijado por el enunciado. Equivalencias: 40 h y 80 h humanas a 25 €/h.

## D3. Tarifa humana = 25 €/h; semana = 40 h; equipo = 6 personas (3 parejas)
- **Motivo:** perfil junior; el paralelismo máximo del plan es 3 iteraciones simultáneas (6/semana en Construcción).

## D4. Esfuerzo por CDU con agentes: Baja 14 h · Media 22 h · Alta 36 h (sin agentes: 24/40/64 h)
- Incluye análisis, diseño de interfaces (humano), generación (agente) y revisión (humano).
- Contingencia del 10% sobre iteraciones de CU para contrarrestar el sesgo de optimismo de la IA.

## D5. Lenguaje Java + Maven multi-módulo + SemVer + Git Flow
- Preferencia explícita del claustro; un módulo Maven por componente; ramas `feature/cduXX` → PR → `develop`; nunca commits en `main`.

## D6. Herramientas IA: GitHub Copilot (licencia campus UCLM, 0 €) + OpenCode con modelos gratuitos
- Criterio de selección de modelo: mejor tasa de acierto en Java/Maven (menos rondas de revisión), no tamaño del modelo.
- Coste nominal de mercado de referencia: ~430 €/proyecto (no incurrido).

## D7. Orden de iteraciones por riesgos (Elaboración = CDU16, 17, 13, 06, 12, 19-spike)
- Se mitigan primero los riesgos arquitectónicos: catálogo (dominio), RBAC, agendas, citas, solicitudes e integraciones FHIR/SSO.

## D8. Reglas de arquitectura como guardarailes de los agentes
1. Comunicación entre módulos solo por interfaces de `iso2-common` (programar orientado a interfaces).
2. Capas MVC/servicio-puerto-adaptador dentro de cada módulo.
3. Los contratos publicados no se rompen: SemVer estricto.
4. Acceso a datos clínicos siempre a través de `iso2-seguridad` (auditoría RGPD).
5. DICOM/HL7/FHIR solo tras fachadas de `iso2-integracion`.

## D9. Calendario real
- Iter0: oct (hecha) · Elaboración: 09–20/11 · Construcción: 23/11–11/12 · Transición: 07–14/12 · Entrega: 15/12/2026.

--- Decisiones sesión 2 (07/11/2026, reglas de Ismael) ---

## D10. Dos aplicaciones: servidora + cliente con interfaz por rol
- **Estado:** aceptada. El cliente presenta vistas distintas según rol (paciente/médico/administrativo/adminSistema).
- **v1 simplificada:** una persona = un solo rol = una sola zona de la app (registro en usuarios).
- Cada CDU se desglosa en parte servidora y parte cliente (tabla en PLAN.md §4).

## D11. Login y credenciales locales en `iso2-seguridad`
- Tabla de usuarios con rol único + hash BCrypt + token de sesión (JWT). El SSO corporativo real se deja para CDU19 (integraciones). Justificación: requisito de gestión de usuarios del cliente sin infraestructura externa en el aula.

## D12. Servidor: Spring Boot (REST/JSON) + H2 embebido + JPA
- Justificación: despliegue cero-infraestructura en evaluación; enorme conocimiento del stack por los agentes (menos rondas de revisión = menos tokens y horas de revisión).

## D13. Cliente: JavaFX de escritorio (multiplataforma)
- Encaja con "distintos sistemas operativos de escritorio" del enunciado; web/móvil quedan habilitados en el futuro porque todo pasa por la API REST. Alternativa descartada para v1: cliente web (más infra, fuera de alcance del bloque 1).

## D14. Git Flow estricto + aprobación exclusiva de Ismael en PRs
- Ramas: `main` (solo releases/tags) · `develop` (solo merges de PR aprobados) · `feature/iterXX-CDUyy-…`.
- El agente nunca hace merge ni toca `main`/`develop` directamente.

## D15. Formato de commit: módulo exacto + resumen de tareas
- Título: `tipo(módulo · IterX/CDUyy): descripción`; cuerpo: una línea por tarea realizada (con métricas de tests). Ejemplo canónico en CONTEXT.md §3.

## D16. Pruebas por iteración: casos diseñados por humanos + JUnit 5; informes Surefire + JaCoCo
- ~~Los casos de prueba (tabla TC) los diseña el alumno ANTES de implementar~~ → **orden revisado por D28** (el alumno los diseña sobre el código ya funcional); el agente los traduce a JUnit. `mvn verify` genera Surefire + JaCoCo; umbrales de cobertura por release en PLAN.md.

## D17. SemVer 2.0 en todos los artefactos + Javadoc completo
- `<version>` del módulo en pom + tags Git `vX.Y.Z`. Javadoc obligatorio en todas las clases y paquetes (`package-info.java`); `mvn javadoc:javadoc` sin errores como criterio de aceptación.

## D18. Modelado: diagramas por iteración en StarUML — ACEPTADA (07/11/2026, Ismael)
- Por iteración: casos de uso, secuencia/interacción, clases de análisis y clases de diseño relevantes.
- **Decisión:** fuente **PlantUML** en `docs/modelo/iterXX/` (versionable en Git, barata en tokens, diferenciable entre iteraciones) + **exportación manual a StarUML** por el equipo para entregar los `.staruml`. Los `.puml` son el ítem de configuración (D25); el `.staruml` es artefacto de entrega de cada release.

## D19. Presupuesto de tokens por iteración y partida en 2 si Alta
- Baja 50–70k · Media 90–130k · Alta 150–220k tokens (6 CUs Altos se dividen en 2 sesiones: servidor | cliente+tests). Si el contexto amenaza con agotarse: avisar, cerrar lo en curso, guardar STATUS.md/PLAN.md y abrir sesión nueva (regla en CONTEXT.md §7).

## D20. Plan de despliegue con canales alfa/beta/final
- alfa 0.1.0→0.2.0-alpha (R1) · beta 0.4.0-beta (R3) y 0.9.0-RC (R4) · final 1.0.0 (R5, 15/12). Detalle y criterios de paso en PLAN.md §3.

## D21. Repo del proyecto: github.com/IsmaelCaballero/PrISO2-25-26
- Todo el trabajo (docs, código, modelo, informes) vive aquí; la wiki enlazará README + ficheros vivos.

--- Decisiones sesión 3 (07/11/2026, planificación de componentes) ---

## D22. Esquema de evolución de componentes: baseline 1.0.0 + incrementos 1.y.0 por riesgo decreciente
- Cada componente = **submódulo Maven**. `v1.0.0` = baseline de contratos publicado en Iter0
  (interfaces publicables, sin funcionalidad). A partir de ahí, cada `v1.y.0` es un **incremento**
  (conjunto de artefactos que lleva de la versión inmediatamente inferior a la superior).
- Orden de los incrementos dentro de cada componente: **riesgo de desarrollo de mayor a menor**
  (T1→T2→T3→T4, con desempate por prioridad de negocio/dependencias). `v1.y.z` = fixes intra-iteración.
- Los 20 CDU se priorizan por riesgo en 4 niveles (T1: 19,12,13,06,17,16 · T2: 07,14,02,11,09,01,08,18 ·
  T3: 03,05 · T4: 10,15,20,04). Criterios: incertidumbre técnica, impacto arquitectónico,
  dependencias y dificultad de prueba.
- Las dos aplicaciones (`iso2-server-app`, `iso2-client-app`) y `iso2-common` siguen el **tren de
  versiones de release** (0.x→1.0.0; common 1.0.0→1.5.0), no el esquema de incrementos por riesgo.
- Documento completo: `docs/componentes-versionado.md` (+ CSV `docs/componentes-versiones.csv`).
- Criterio de aceptación de cada iteración = la versión objetivo de sus componentes funcionando
  con sus tests (negrita de la matriz §5 del documento).

## D23. Inversión Iter7↔9 (CDU14 antes que CDU01)
- Para que los incrementos de `iso2-citas` respeten riesgo decreciente (1.2.0=CDU14 T2-alto antes
  de 1.3.0=CDU01 T2-bajo), se invierten las iteraciones 7 y 9 del plan de sesión 1.
- No altera fechas, pareja responsable ni coste total; solo el orden CDU01↔CDU14.

## D24. Modelo de calidad ISO/IEC 25010 acotado a 5 características + puertas por release
- **Fecha:** 07/11/2026 · **Estado:** aceptada (pendiente de sellado en PR de `releaseplan`)
- Se maximizan: **Adecuación funcional** (completitud, corrección, pertinencia), **Usabilidad**
  (operabilidad + protección contra errores), **Fiabilidad** (disponibilidad, tolerancia a fallos,
  recuperabilidad), **Seguridad** (confidencialidad, integridad, no repudio, responsabilidad) y
  **Mantenibilidad** (modularidad MVC+SOLID, analizabilidad, testabilidad). El resto de características
  de ISO 25010 se cubren de forma básica (proyecto de 5 semanas, S13 del PUD +Calidad).
- Instrumentos: TC humanos por RF (1 feliz + ≥2 error + 1 permisos); ArchUnit para reglas MVC/SOLID;
  `@Auditable` en toda mutación con auditoría de hash encadenado (no repudio); TC de adaptador externo
  caído; ensayo de copia/restore H2. Puertas JaCoCo línea: ≥60 % R1 · ≥70 % R2 · ≥75 % R3 · ≥78 % R4 ·
  **≥80 % final** (≥85 % en citas/solicitudes/seguridad); rama ≥45 %→70 %; fallos Surefire = 0.
- figura de **guardián de calidad** rotatoria por release; ningún miembro se auto-fusiona su PR.
- Detalle: `SOFTWAREQUALITY.md` (por componente y por CDU) y `docs/planificacion-PUD+Calidad.md` §8.

## D25. Ítems de configuración: 14 módulos Maven + contratos + migraciones + docs vivos
- **Fecha:** 07/11/2026 · **Estado:** aceptada
- Ítems de configuración: parent + common + 10 dominio + 2 apps (versión SemVer en POM), scripts
  `db/migration/V<versión>__<cdu>.sql`, suites JUnit e informes, diagramas `docs/modelo/iterXX/`,
  ficheros vivos (DECISIONS/PLAN/STATUS/SOFTWAREQUALITY) y pipeline CI. Todo en Git; nada fuera.
- **CFG-NN = configuración mínima de la iteración NN** (conjunto exacto de componentes+versiones que
  la construyen). Tag anotado `cfg-iterNN` en cada merge de iteración a `develop`. Ninguna etiqueta de
  release (`v…`) puede contener `SNAPSHOT`. Auditoría de configuración tras cada merge: el guardián
  verifica que los POM coinciden con `docs/componentes-versiones+Calidad.csv`.
- Detalle: `docs/planificacion-PUD+Calidad.md` §9 y `docs/mapa-configuraciones.md`.

## D26. Mapa de configuraciones CFG-00…CFG-21 con etiquetas alfa/beta/candidata/definitiva
- **Fecha:** 07/11/2026 · **Estado:** aceptada
- 22 configuraciones mínimas mapeadas a la agenda de iteraciones. CFG hito de release:
  CFG-06 `v0.2.0-alpha` (**ALFA 1**, R1 20/11) · CFG-12 `v0.3.0-alpha` (**ALFA consolidada**, R2 27/11) ·
  CFG-18 `v0.4.0-beta` (**BETA**, R3 04/12) · CFG-20 `v0.9.0-rc` (**CANDIDATA**, R4 11/12) ·
  CFG-21 `v1.0.0` (**DEFINITIVA**, R5 15/12). El resto, CFG internas (pueden llevar `…-SN`).
- Las clasificaciones se declaran **por criterios cumplidos, no por fecha** (flujo end-to-end, nº de
  CDU, puertas de calidad, modo degradado probado). Ambientes: dev → test → alfa → beta → demo.
  Rollback = `git checkout cfg-iterNN` + build + par server/client + copia H2 asociada.
- Detalle y tabla completa: `docs/mapa-configuraciones.md` + `docs/mapa-configuraciones.csv`
  (rama `releaseplan`).

## D27. Puertas de calidad embebidas en el plan (por release, no como iteración aparte)
- **Fecha:** 07/11/2026 · **Estado:** aceptada
- Las actividades de calidad se financian dentro de las horas ya presupuestadas de cada iteración
  (diseño humano de TC, revisión de arquitectura, CI) y de las 80 h fijas de Iter21: **el presupuesto
  no cambia** respecto del plan v1.1.0. La tabla de puertas §8.4 del PUD +Calidad es la referencia
  para el criterio de paso entre releases (trazabilidad RF→CU→TC: 6/12/18/20/20 CDU).

## D28. Diseño humano de TC: sobre el código funcional de la iteración (revisión de D16)
- **Fecha:** 07/11/2026 · **Estado:** aceptada (Ismael, al arrancar Iter1)
- La tabla TC de cada CDU la diseña el alumno **una vez el código funcional está visible** (firmas
  de API, códigos de error y pantallas reales), y el agente la traduce a JUnit 5 antes de cerrar la
  iteración. Justificación: los TC se diseñan mejor contra comportamiento observable y el alumno los
  revisa contra la API real, no contra un boceto.
- Hasta que llegan los TC del alumno, el agente solo escribe **pruebas técnicas de humo/integración**
  para arranque y puertas (JaCoCo, ArchUnit); no sustituyen a los TC humanos, que siguen siendo
  obligatorios por RF (1 feliz + ≥2 error + 1 permisos, D24) y son criterio de cierre de iteración.
- Se mantiene todo lo demás de D16: JUnit 5, informes Surefire + JaCoCo en `mvn verify`, umbrales por
  release. Cada iteración deja una tarea explícita "TC humanos del CDU" en STATUS.md.
