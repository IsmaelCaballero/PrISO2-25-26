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
- **Revisada el 08/11/2026 por D31:** la parte de StarUML queda descartada; los artefactos de entrega de modelado pasan a ser los `.puml` + sus `.png` renderizados con `plantuml.jar`.

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

## D29. Cierre de issues: manual al fusionar a `develop` (lección del 07/11/2026)

- Los `Closes #N` de la descripción de un PR solo cierran el issue cuando el PR se fusiona a la
  rama por defecto del repo (`main`). Como nuestro flujo fusiona las feature a `develop`, los
  issues de tarea (#6, #9) NO se cerraron solos pese al `Closes #N` en los PRs #7 y #10.
- A partir de ahora: al fusionar un PR a `develop`, Ismael cierra a mano el issue de la tarea
  (o se cierra al publicar el release correspondiente en `main`). El agente lo recuerda en cada
  PR nuevo con una línea "Cerrar #N a mano al fusionar" en el cuerpo.

## D30. `main` solo recibe merges de rama `release/RN` (lección del 08/11/2026)
- **Fecha:** 08/11/2026 · **Estado:** aceptada (Ismael, tras la incidencia del PR #17)
- **Qué pasó:** el PR #17 (`feature/iter03-cdu13-agendas` → `main`, merge `5e0ef29`) se abrió y fusionó
  por error. Dejó en `main` solo la mitad servidora de Iter3 (faltaban 15 ficheros y 1432 líneas del
  lado cliente/integración/fix 404) y rompió la correspondencia `main` ↔ releases etiquetadas del §4 del
  mapa. Al revisarlo: ningún fichero de `main` era exclusivo — el único commit no alcanzable desde
  `develop` era el propio merge `5e0ef29`.
- **Decisión:** `main` **no acepta nunca un PR de rama `feature/…`**. Cada hito se publica como está
  dicho en el mapa §4: rama `release/RN` creada desde el tag de configuración del hito (R0 desde
  `cfg-iter00`), PR a `main`, fusión con merge commit (`--no-ff`, nunca squash ni rebase) y tag anotado
  de release (`v0.1.0`, `v0.2.0-alpha`, …) sobre ese commit de merge. Los `cfg-iterNN` siguen en el merge
  de iteración dentro de `develop`; `main` no los lleva.
- **Reparación ejecutada 08/11:** `git push --force-with-lease origin 1245138:main` descarta `5e0ef29`
  (`main` estaba justo en el commit raíz antes del PR #17, así que no se pierde trabajo) + rama `release/R0`
  sobre `641e72d` + PR #19 → `main`. Se descartó revertir (`revert -m 1` impediría que el futuro merge de
  `develop` re-aplicase esos ficheros) y dejar `main` tal cual (dejaría una release parcial sin etiquetar).
- **Resultado verificado:** Ismael fusionó el PR #19 con merge commit `e165622` (dos padres: `1245138` +
  `641e72d`) y se creó sobre él el tag anotado **`v0.1.0`**, primera etiqueta de release del repo. CFG-00
  queda publicada en `main` con las dos etiquetas de su fila del mapa (`v0.1.0;cfg-iter00`).
- **Comprobación previa obligatoria antes de tocar `main`:** `git log --oneline origin/main --not origin/develop`
  (el PR no debe aportar nada que no esté ya en `develop`) y revisar la protección de rama. Si hace falta
  reescribir `main`, solo lo hace Ismael o con su visto bueno explícito.

## D31. Artefactos de modelado: PlantUML (`.puml` + `.png` versionados) — StarUML descartado — ACEPTADA (08/11/2026, Ismael)
- **Qué ha pasado:** la exportación a StarUML de D18 nunca llegó a ejecutarse: no existe ningún proyecto
  `.staruml` guardado (comprobado en las 13 ramas, en toda la historia de git, en la wiki y en el disco).
  El modelado real de Iter1-3 se ha hecho íntegramente con **PlantUML**: los 12 ficheros `.puml` de
  `docs/modelo/iter01-03/` y sus `.png` generados con `plantuml.jar` (descargado a `tools/` el 08/11).
- **Decisión:** por iteración se versionan DOS artefactos: la fuente `.puml` y el `.png` renderizado desde
  ella, incrustados en el `README.md` del directorio (vista reducida con enlace al PNG a tamaño completo).
  La herramienta es `tools/plantuml.jar`, que **no se versiona** (`*.jar` ya está en `.gitignore`; se
  descarga de plantuml.com). Comando estándar desde la raíz:
  `java -jar tools/plantuml.jar -tpng docs/modelo/iterXX/*.puml` (añadir `-Playout smetana` sin Graphviz).
- **Efectos:** la issue #22 (exports `.staruml`) queda cerrada como "no procede"; D18 sigue vigente en su
  parte PlantUML y en que los `.puml` son el ítem de configuración (D25); CONTEXT §4 se renombra a
  "Modelado por iteración (PlantUML)". Los `.png` se regeneran de forma reproducible (verificado byte a byte
  sobre dos diagramas de iter01). Si el PUD exigiera modelo UML nativo, plan B: reimportar los `.puml` en
  StarUML con el plugin PlantUML y guardar; no ejecutado por ahora.

## D32. Javadoc publicado en GitHub Pages desde `develop` (solo la última compilación) — ACEPTADA (08/11/2026, Ismael)
- **Contexto:** las páginas de la wiki por iteración quieren enlazar, para cada clase, su ficha de
  Javadoc generado. Hasta ahora el Javadoc solo se generaba en el CI (`mvn … javadoc:javadoc`) y
  quedaba en un artefacto con caducidad de 90 días: no había una URL estable que enlazar (issue #27).
- **Decisión:**
  - workflow nuevo `.github/workflows/javadoc.yml`: en push a `develop` (y `workflow_dispatch`)
    ejecuta `mvn -B -ntp javadoc:aggregate` y publica el sitio en GitHub Pages con
    `actions/upload-pages-artifact` + `actions/deploy-pages`; Pages se configura con `build_type=workflow`.
  - **Solo la última compilación de `develop`**, sin instantáneas por tag `cfg-iterNN`: el sitio y el
    CI pesarían ~8 MB por iteración y el Javadoc de una CFG concreta se puede regenerar localmente
    desde su tag. Cada página de la wiki indica la versión de módulo a la que corresponde.
  - URL base: `https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/…`; un `index.html` en la raíz
    (plantilla `docs/javadoc-index.html`) redirige al índice de paquetes.
- **Nota técnica:** `javadoc:aggregate` escribe en `<raíz>/target/reports/apidocs`, no en
  `target/site/apidocs`; por eso se corrige el `path` del artefacto `informes-calidad` del `ci.yml`
  a `**/target/reports/apidocs/**` (hasta ahora ese `path` no cogía nada y el artefacto salía con aviso).
- **Efectos:** las tablas de clases de las páginas de la wiki enlazan la ficha Javadoc de cada clase;
  el sitio refleja siempre `develop`, así que los enlaces no se reescriben al cerrar iteraciones.
