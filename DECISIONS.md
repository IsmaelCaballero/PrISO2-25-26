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
- **Segunda desviación detectada el 08/11 (sesión 16) y su cierre:** `origin/main` está en `d12388b`, merge de `develop` → `main` a las 19:01 (+0100) con padres `e165622` y `c8fe2d7`: entró trabajo de `develop` en `main` **sin** rama `release/RN` ni tag, así que incumple esta decisión. Ismael decide **dejarlo y documentarlo**: `main` queda como espejo de `develop` hasta R1, que se sacará como rama `release/R1` desde el tag `cfg-iter04` con su PR y su tag `v0.1.1`. No se toca la historia de `main`.
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

## D33. Diseño de pruebas: clases de equivalencia + valores límite + conjetura de errores, compuestas con *pairwise*, y los mismos casos en JUnit y en los TC — ACEPTADA (08/11/2026, Ismael)

- **Contexto:** hasta Iter4 las pruebas las iba escribiendo el agente de forma ad hoc (un test por caso que
  parecía razonable) y las plantillas de TC humanos de D28 se escribían **a mano, por separado**, de forma que
  JUnit y TC podían divergir y de hecho divergían (p. ej. en CDU16 el test de cliente esperaba `CODIGO_EN_USO`
  y el servidor emite `CODIGO_DUPLICADO`). Ismael pide el 08/11 cambiar el método: las pruebas se desarrollan
  **sobre la base de clases de equivalencia, el teorema de los valores límite y la conjetura de errores**, y
  encima se pasa un filtro **pairwise** para reducir la combinatoria de parámetros.
- **Decisión (método obligatorio a partir de ahora, hacia atrás y hacia delante):**
  1. **Clases de equivalencia**: para cada entrada y cada estado relevante del dominio se listan sus clases
     válidas y no válidas (una fila por clase), justificadas con la regla de negocio o la restricción de la
     RF/`iso2-common` que las origina.
  2. **Teorema del valor límite**: de cada clase ordenada se prueban los cuatro puntos del borde
     (`min-1`, `min`, `max`, `max+1`) en la cota donde exista; los límites salen de una única fuente (los
     `record` de `iso2-common`, las anotaciones JPA y la migración Flyway), nunca se inventan.
  3. **Conjetura de errores**: se añaden casos escogidos por defecto plausible (valores nulos/vacíos,
     unicode, campos largos, estados imposibles, reenvío de peticiones, concurrencia sobre el mismo recurso)
     porque la experiencia dice que ahí están los fallos.
  4. **Composición pairwise**: cuando un caso depende de varios factores, no se prueban todas las
     combinaciones (comb exploding); se construye la tabla de factores × niveles y se cubre **todo par de
     niveles**, que es lo que detecta casi todos los fallos por interacción. Se documenta la tabla y los pares
     cubiertos.
  5. **Un solo conjunto de casos, dos proyecciones**: **esos mismos casos** son los que se ejecutan con
     **JUnit en Maven** y los que van en las **plantillas de TC humanos**. Cada caso tiene un id
     (`TC16-01`, `TC17-03`, `TC13-05`, `TC06-02`…) y ese id aparece en el `@DisplayName` del test y en la
     columna «Caso de prueba» de la plantilla de TC. **Una plantilla de TC pasa a ser la proyección legible de
     la matriz**, ya no se escribe a mano.
  6. **JavaDoc en los ficheros de test**: cada clase de test se documenta con JavaDoc indicando **la clase que
     se prueba**, sus colaboradores (qué está simulado y qué es real) y la propiedad que se comprueba; los
     métodos se documentan con el caso que implementan. El JavaDoc entra en el agregado de Pages (D32).
  7. **Evidencia versionada**: la matriz (factores, niveles, clases de equivalencia, puntos límite, pares
     pairwise) se versiona en **`docs/test/iterXX/`** junto al `.puml` de la iteración, de forma que el diseño
     de las pruebas sea un ítem de configuración más, igual que el modelo (D31).
  8. **Wiki**: la sección *Testing* de cada página de iteración identifica **claramente la clase probada** en
     cada fila (no solo el número de tests por módulo).
- **Orden de aplicación:** se empieza **revisando con más rigurosidad las pruebas de la Iteración 1**
  (`iso2-catalogo`, CDU16: dominio `TipoPrueba`/`Protocolo`/`Politica` + `AuditoriaCatalogo`, servicio, REST y
  ArchUnit), se regenera su plantilla `docs/tc/cdu16-tc-humanos.md` con los casos resultantes y se actualiza su
  página de wiki. Después se extiende a Iter2 (CDU17) e Iter3 (CDU13), y **Iter5 (CDU12) nace ya con el
  método**.
- **Efectos:** los huecos que el método destapa en CDU16 se convierten en casos nuevos o en decisiones de
  diseño: un `codigo` de catálogo de más de 40 caracteres hoy revienta contra la BD (500) en vez de devolver
  400; `pagina` no numérico en `GET /api/catalogo/pruebas` da 500 en vez de 400; y la discrepancia
  `CODIGO_EN_USO` (test de cliente) vs `CODIGO_DUPLICADO` (servidor). Si alguno obliga a cambiar una invariante
  o una firma, **primero se edita el `.puml` de `docs/modelo/iter01/`** (D31) y luego el código. D28 **no**
  cambia: los TC humanos los rellena y ejecuta Ismael; el agente genera los casos y las plantillas, pero no las
  ejecuta. Las puertas de cobertura de JaCoCo y de ArchUnit se mantienen como están.
- **Aplicación y huecos de CDU16 (decidido el 08/11, sesión 16):** se abre **una issue y una rama por cada trabajo**: **#35** → `feature/iter01-pruebas-d33` (D33 sobre Iter1: CDU16; después CDU17 y CDU13) y **#34** → `feature/iter05-persistencia-mysql` (D34). Los huecos que destapa la matriz de valores límite **se arreglan**: las cotas de longitud (`codigo` 40, `nombre` 120, `categoria` 20, `preparacion` y `contraindicaciones` 1000) se validan en el servicio y `pagina`/`tamano` no numéricos se mapean a **400 `DATOS_INVALIDOS`** en lugar del 500 actual; `categoria` se valida contra la lista del contrato. Como son invariantes nuevas, **primero se editan los `.puml` de `docs/modelo/iter01/`** (D31). Ver D35.

## D34. Persistencia: MySQL en Docker, acceso con patrón DAO y conexión con patrón Agente (`AgenteMySQL`) — ACEPTADA (08/11/2026, Ismael) · IMPLEMENTADA (11/11/2026, sesión 18, rama `feature/iter05-persistencia-mysql`)

- **Contexto:** desde Iter1 la persistencia es **H2 en fichero** con Spring Data JPA y Flyway
  (`V1.0.0`…`V1.4.0`), sin infraestructura de despliegue. Ismael decide el 08/11 que el trabajo siga sobre
  **MySQL** y que se monte la infraestructura en contenedor.
- **Decisión:**
  - **Docker Compose** con cuatro servicios: **MySQL 8** (base de datos `iso2`), **phpMyAdmin** para
    inspeccionar la base, y el **servidor** y el **cliente** de la aplicación.
  - Cuentas de la instancia: `root` con password **`Iso2Curso25-26`** y el usuario de aplicación
    **`gestoroperaciones` / `Iso2Curso25-26`**, que es el que maneja las **operaciones de entrada y salida**
    (DML) de la aplicación.
  - **Patrón DAO** para el acceso a los datos: el dominio deja de depender de `Repository` de Spring Data y se
    introduce una interfaz `DAO` por agregado con su implementación sobre la base de datos.
  - **Patrón Agente** para la conexión: una abstracción `Agente` que encapsula el conector del SGBD y se
    particulariza en **`AgenteMySQL`**, de forma que se pueda **conectar a varios SGBD** añadiendo otro Agente
    sin tocar el dominio ni el DAO.
- **Cinco puntos pendientes, resueltos por Ismael el 08/11/2026 (sesión 16):** (a) las pruebas técnicas usan **Testcontainers con MySQL 8**, así que el SGBD de las pruebas es el mismo de producción (Docker pasa a ser requisito para `mvn verify`); (b) Flyway con **migraciones duales** en `db/migration/h2` y `db/migration/mysql`, misma numeración `V1.x` y carpeta elegida por perfil (`spring.flyway.locations`), porque las migraciones actuales están escritas en dialecto H2 (`GENERATED BY DEFAULT AS IDENTITY`, `TIMESTAMP(6) WITH TIME ZONE`); (c) `gestoroperaciones` queda con **solo DML** y se crea un **usuario de DDL aparte** para Flyway; (d) driver `com.mysql:mysql-connector-j` contra **MySQL 8.4** con volumen persistente nombrado en el Compose; (e) va en **rama propia** `feature/iter05-persistencia-mysql`, separada del trabajo de pruebas.
- **Realización (11/11/2026, sesión 18), con tres matices respecto al texto de la decisión:**
  - **El dominio conserva Spring Data JPA como acceso primario** (decisión de Ismael del 11/11: «JPA arriba,
    DAO + Agente como capa nueva»). El **patrón DAO** y el **patrón Agente** se materializan en un paquete
    **nuevo**, `es.uclm.iso2.server.persistencia` de `iso2-server-app`: interfaz `Agente` + `AgenteH2`
    (`SELECT CURRENT_SCHEMA()`) y `AgenteMySQL` (`SELECT DATABASE()`) elegidos en `ConfigPersistencia` por
    `DatabaseMetaData.getDatabaseProductName()`, y un DAO JDBC de ejemplo (`DiagnosticoPersistenciaDao`) que
    alimenta `GET /api/persistencia/diagnostico` (solo `ADMINISTRADOR`). No se sustituye ningún `Repository`
    de los módulos de dominio.
  - El Compose lleva **dos servicios** (`mysql:8.4` en puerto host **3307** + **phpMyAdmin** en **8081**,
    volumen nombrado `iso2-mysql-datos`), no cuatro: el servidor Spring y el cliente JavaFX siguen siendo
    procesos locales (`mvn spring-boot:run`); su contenedorización queda para la fase de despliegue.
    La contraseña de `root` del Compose es **`Iso2Curso25-26-Root`** (distinta de la de aplicación para no
    mezclar; `gestoroperaciones` mantiene `Iso2Curso25-26` y el usuario DDL es `iso2_flyway` / `Iso2Flyway25-26`).
  - Las carpetas de Flyway se eligen **por perfil** (`spring.flyway.locations: classpath:db/migration/h2`
    o `…/mysql`), **no** con el placeholder `{vendor}`: Flyway 11 lo resuelve mal y escanea ambas subcarpetas,
    produciendo versiones duplicadas.
- **Hecho de entorno que condiciona la puerta Testcontainers:** Docker Engine 29 rechaza clientes con API
  menor de 1.40; Testcontainers **1.21.3** (la que gestiona el BOM de Boot 3.5.4) habla 1.32, recibía un
  `400` del daemon y los tests **se saltaban en silencio**. Corrección: importar **`testcontainers-bom`
  1.21.4** en el `<dependencyManagement>` del padre **antes** de `spring-boot-dependencies` (sobrescribir la
  propiedad `testcontainers.version` no funciona cuando el BOM se importa y no se hereda de
  `spring-boot-starter-parent`) y subir JaCoCo del padre **0.8.12 → 0.8.15** (el Jackson sombreado de
  Testcontainers no compilaba con Java 25). Sin Docker instalado, los tests de MySQL se autodesactivan
  (`@Testcontainers(disabledWithoutDocker = true)`) y el resto del reactor sigue verde.
- **Seguimiento:** issue **#34** (reabierta tras detectarse su cierre del 09/11 sin trabajo detrás; implementada
  en `feature/iter05-persistencia-mysql` con commits `b15d927` (docker), `64e92eb` (Flyway dual + poms),
  `ae40ad7` (modelo D31) y `bba56f8` (código DAO/Agente + tests); PR a `develop` pendiente de fusión y cierre
  manual de la issue, D29).
- **Efectos previstas:** hay que tocar los `pom.xml` (driver MySQL), la configuración de `iso2-server-app`
  (`application*.yml`), las migraciones y los tests de integración que hoy dependen de H2; y el modelo de
  `docs/modelo/iterXX` de la iteración que lo incorpore gana el diagrama de la capa DAO/Agente (D31). El
  despliegue en contenedor no cambia las puertas de calidad existentes.

## D35. Las cotas del contrato se validan en el servicio (400), no se delegan a la base de datos (500) — ACEPTADA (08/11/2026, Ismael)

- **Contexto:** la matriz de clases de equivalencia y valores límite de CDU16 (D33) deja ver que el contrato publicado (Javadoc de `PruebaCatalogoFacade`) habla de «400 validación», pero en la implementación solo se validan los mínimos (`duracionMinutos > 0`, `ayunoHoras ≥ 0`, textos no vacíos). Las cotas máximas viven **solo** en `@Column(length = …)` de `TipoPrueba` y en `V1.1.0__cdu16_catalogo.sql`; al pasarlas por HTTP, un `codigo` de 41 caracteres termina en un fallo de insert de H2/MySQL que el `@ExceptionHandler(Exception.class)` de `CatalogoManejadorErrores` traduce a **500 `ERROR_INTERNO`**. Lo mismo con `?pagina=abc`: `MethodArgumentTypeMismatchException` cae en el mismo capturador genérico.
- **Decisión:** las cotas son **reglas de negocio del servicio**, no efectos secundarios del esquema. En `iso2-catalogo`:
  1. una única fuente de cotas (`LimitesCatalogo`, que refleja `@Column` y la migración) y validación de longitudes en `CatalogoServicio.crear`/`actualizar` → `IllegalArgumentException` → **400 `DATOS_INVALIDOS`**;
  2. `categoria` validada contra la lista que ya documenta el contrato (`RX`, `TAC`, `RM`, `ECO`, `MED-NUC`, `DENS`, `OTRO`);
  3. capturador específico para los parámetros de consulta mal tipados en `CatalogoManejadorErrores` (**400**, no 500), manteniendo el capturador genérico como red de seguridad con su 500 y su log.
- **Por qué no Bean Validation:** `iso2-catalogo` no usa JSR-380 y meter `spring-boot-starter-validation` ahora abriría un segundo camino de errores (`MethodArgumentNotValidException`) con cuerpo distinto del `ApiError` del contrato. Se valora para Iter5 si crece la superficie escrita.
- **Modelo primero (D31):** las invariantes nuevas de longitud y de dominio de `categoria` se añaden a los `.puml` de `docs/modelo/iter01/` **antes** de tocar el código, y se regeneran los `.png`.
- **Efectos:** el contrato firmado en CFG-00 **no cambia de firma** (sigue siendo MINOR), solo se ciñe su semántica de errores; los tests de `iso2-server-app` y de cliente que usan categorías y códigos válidos no se ven afectados; se corrige el stub de `ApiClienteCatalogoTest` que simula `CODIGO_EN_USO` cuando el servidor emite `CODIGO_DUPLICADO`.


## D36. El hueco se protege con un UNIQUE en la tabla de ocupación y el duelo lo decide la base de datos - PROPUESTA (11/11/2026, agente; pendiente de visto bueno de Ismael)

- **Contexto:** CFG-05 exige tres puertas que un `synchronized` casero no satisface: *TC 2 hilos reservando el mismo hueco -> 1 éxito*, *TC rollback de cita + hueco + auditoría* y `@Version` en el hueco (`SOFTWAREQUALITY.md`, apartado CDU12). Y hay que poder reprogramar y cancelar sin dejar huecos huérfanos ni citas sin hueco.
- **Decisión (ya implementada en `iso2-citas` 1.1.0):**
  1. Tabla de ocupación **`cita_hueco`**: una fila por **cita activa** (`cita_id` único), con `@Version int version` y `UNIQUE (profesional_id, inicio)`. Como cancelar, realizar y expirar borran la fila (`deleteByCitaId`), en la tabla solo viven las citas activas, así que su sola presencia ya significa "hueco ocupado".
  2. El servicio hace dos pre-chequeos para dar mensajes claros (inicio exacto con `findByProfesionalIdAndInicio`, y solape con `findByProfesionalIdAndInicioBetween` en una ventana de +/-1 día filtrada por `CitaHueco.solapaCon`) y **deja que el duelo real lo decida la base de datos** en `huecos.saveAndFlush(...)`: el perdedor recibe el rechazo del índice único.
  3. Ese rechazo **no se captura dentro de la transacción** porque la envenenaría y el resultado sería un 500: `DataIntegrityViolationException` y `ObjectOptimisticLockingFailureException` se mapean en `CitaManejadorErrores` a **409 `CONFLICTO_HUECO_OCUPADO`**, el mismo código que `CitaConflictoException(HUECO_OCUPADO)`, de modo que el cliente solo ve un código pase lo que pase.
  4. `reprogramar` **mueve** la fila existente (`CitaHueco.moverA` + `saveAndFlush`) en lugar de borrarla e insertar otra, para que el `@Version` que exige `SOFTWAREQUALITY.md` proteja también dos reprogramaciones concurrentes.
- **Reglas de semántica derivadas, escritas en los `.puml` antes que en el código (D31) y pendientes de confirmación:** la solicitud apta es **solo** la que está en estado `creada` (el contrato ya decía "no existe, no está en creada o ya está citada" -> 409 `CONFLICTO_SOLICITUD_NO_APTA`, y por eso una solicitud inexistente es 409 y no 404); un profesional **sin agenda** cuenta como `FUERA_DE_FRANJA`, porque sin franja no hay hueco posible; el preaviso de cancelación solo aplica a las citas que vienen de una solicitud con prueba catalogada (una cita directa no tiene política que exigir); y en `cancelar` manda la máquina de estados, así que `TRANSICION_INVALIDA` gana a `PLAZO_CANCELACION`. El flag `requiereCita = false` del catálogo **no** bloquea agendar (la semilla `SEM-RX-TORAX` lo tiene en false y aun así tiene una cita sembrada): el catálogo aporta duración y preaviso, no la decisión de si hace falta cita.
- **Efectos:** el contrato firmado en CFG-00 no cambia de firma. En el módulo hacen falta pruebas con transacción real (`@Transactional(propagation = NOT_SUPPORTED)`) porque `reprogramar` muta la entidad gestionada antes de validar el hueco de destino: con una transacción compartida por la prueba, el cambio se queda en la caché de Hibernate y el TC de rollback no mide nada.


## D37. Medir la tasa de defectos por iteración (4 familias) para decidir cuándo hace falta un modelo más potente - ACEPTADA (11/11/2026, Ismael)

- **Contexto:** el proyecto se está haciendo con un modelo cuantizado (Qwen3.8-Flash-Next `iq3_s`) y no hay
  ningún dato que diga si eso basta o si en ciertos trabajos hace falta un modelo más potente. Las cifras que
  circulaban en conversaciones ("3 defectos de RBAC", "5 fallos por saltarse pasos") no tienen respaldo en
  `STATUS.md` ni en `DECISIONS.md`, así que no son utilizables.
- **Decisión:** se crea **`TASADEFECTOS.md`** en la raíz con (1) un libro de defectos con su evidencia
  (commit/tag/registro de sesión), (2) la tabla por iteración con el esfuerzo medido como
  `git rev-list --count` entre etiquetas, y (3) las métricas de apoyo (CI, tests, líneas). Los defectos se
  clasificarán en 4 familias: **F1 proceso/protocolo** (violar D29/D30, issues sin artefacto), **F2
  entorno/herramienta** (trampas de la cadena de build), **F3 semántica/contrato** (spec o decisión de diseño
  contra lo publicado) y **F4 código** (comportamiento distinto del contrato). **Solo F4 mide la valía del
  modelo de código**; F1/F2/F3 se arreglan con reglas y revisión, no con un modelo más caro.
- **Regla de exclusión (sesgo declarado):** no se cuenta lo que pillan el compilador o `mvn -B -ntp clean
  verify` local antes de pushear. Las cifras quedan sesgadas a la baja por diseño; a cambio son auditables.
  Unidad de conteo: familia de hueco, no caso de prueba (los 16 fallos del rojo D33 de Iter1 son 1 F4 + 2 F3).
- **Registro obligatorio a partir de ahora:** en cada cierre de CFG (D29) se actualiza `TASADEFECTOS.md`
  (libro + tabla) y el registro de sesión de `STATUS.md` lleva la línea con los 6 contadores: commits del
  tramo, F1, F2, F3, F4, total y fugados. Plantilla en §7 de `TASADEFECTOS.md`.
- **Umbrales para pedir modelo más potente** (revisar en cada cierre): una familia de CDU acumula **> 2
  defectos F4** detectados en revisión/TC humanos, o **1 decisión de diseño errónea que obligue a reescribir
  código ya commiteado**, o **1 falso verde colado en CI**. Mientras no salten, se sigue con el flash
  cuantizado + D33 + CI; si saltan, el modelo más potente se pide para ese trabajo concreto (diseño de
  contrato, pruebas de concurrencia/transiciones), no para todo el iterativo.
- **Situación de partida (11/11/2026):** 13 defectos en 101 commits (12,9/100); 6 F1, 3 F2, 2 F3, **2 F4**
  (2,0/100: DEF-05/D35 en Iter1 y `ae79d7e` en Iter3); 11 fugados y 2 detectados en local; 0 roturas llegan a
  CI. Veredicto provisional y lectura de la serie en §6 de `TASADEFECTOS.md`.
