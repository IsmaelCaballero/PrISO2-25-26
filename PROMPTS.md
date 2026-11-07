# PROMPTS.md — Registro cronológico de prompts del agente

Reconstrucción, en orden y listos para copiar/pegar, de los prompts que el alumno escribe al
agente de IA en cada sesión del proyecto. Sirven como **trazabilidad del proceso de desarrollo
asistido con IA** (para el informe de la práctica) y como **receta reproducible**: otro alumno
podría ejecutar el proyecto de principio a fin pegando estos prompts en sesiones nuevas.

## Convenciones de uso

1. Cada bloque `### Prompt NN` es **una sesión nueva del agente** (contexto limpio).
2. **Preámbulo standing** — pegar siempre al inicio de cada prompt (ahorra repetición y fija las
   reglas de CONTEXT.md/DECISIONS.md):
   > Antes de nada, lee CONTEXT.md, DECISIONS.md, PLAN.md y STATUS.md. Trabaja solo en la rama que
   > te indique; nunca toques `main` ni `develop` directamente; formato de commit
   > `tipo(módulo · IterX/CDUyy): resumen` con una línea por tarea en el cuerpo (CONTEXT.md §3);
   > no hagas merge de PRs: solo Ismael aprueba y fusiona. Si el contexto amenaza con agotarse,
   > avisa, guarda STATUS.md/PLAN.md y cierra la sesión.
3. A partir de Iter0 de código: cada prompt se **registra aquí ANTES de ejecutarlo** y se enlaza
   con su issue de GitHub (título del issue = iteración/CDU; cuerpo = prompt ejecutable). Flujo:
   issue → prompt (este fichero) → rama `feature/iterXX-cduyy-…` → PR → merge de Ismael → tag `cfg-iterXX`.
4. Leyenda: **[✔ ejecutado]** · **[● preparado, listo para lanzar]** · **[○ pendiente: requiere
   entrada del alumno]**.

---

## Etapa 1 — Planificación (Teoría 1) — prompts 1–7 [✔ ejecutados]

### Prompt 01 — Análisis del enunciado y planificación PUD [✔]

> Arrancamos el proyecto de la práctica de ISO2: "Sistema Integral de Gestión de Radiodiagnóstico
> e Imagen Médica del SESCAM". El enunciado está en `enunciadoPracticaESPv1.15.1.txt`. Léelo y
> propón la planificación según el Proceso Unificado de Desarrollo:
> (1) extrae los requisitos funcionales y mapea RF→CU 1:1 y CU→iteración 1:1 (simplificación que
> admite el enunciado);
> (2) planifica las fases con iteración 0 (Inicio, coste fijo 1.000 €) y 21 iteraciones de CU,
> terminando en la iteración 21 (Transición, coste fijo 2.000 €), con releases R0..R5;
> (3) estima esfuerzo y coste con tarifa humana 25 €/h y semana de 40 h, en escenario con agentes
> (Baja/Media/Alta = 14/22/36 h) y sin ellos (24/40/64 h), con contingencia del 10 % y equipo de
> 6 personas organizadas en 3 parejas;
> (4) calendario real: Elaboración 09–20/11, Construcción 23/11–11/12, Transición 07–14/12,
> entrega 15/12/2026; prioriza Elaboración por mitigación de riesgos arquitectónicos.
> Genera `docs/planificacion-PUD.md`, `docs/planificacion-iteraciones.csv` y `DECISIONS.md` con
> los supuestos como ADRs (D1..D9). Trabaja en `feature/docs-plan-inicial`.

### Prompt 02 — Reglas del equipo, plan operativo y repositorio [✔]

> A partir de ahora rigen estas reglas del equipo (regístralas en `CONTEXT.md` como vinculantes):
> Git Flow estricto con aprobación exclusiva de Ismael en PRs; commits
> `tipo(módulo · IterX/CDUyy): resumen`; pruebas con casos diseñados por humanos ANTES de
> implementar (el agente solo los traduce a JUnit 5) + informes Surefire y JaCoCo; SemVer 2.0 en
> todos los artefactos + Javadoc completo; diagramas por iteración (casos de uso, secuencia,
> clases de análisis y de diseño); presupuesto de tokens por iteración (Baja 50–70k, Media
> 90–130k, Alta 150–220k; las iteraciones Altas se dividen en 2 sesiones servidor|cliente+tests).
> Decisiones de producto: dos aplicaciones (servidora REST + cliente con interfaz por rol; en v1
> una persona = un rol); cada CDU se desglosa en parte servidora y parte cliente; stack propuesto
> servidor Spring Boot + H2 + JPA + JWT/BCrypt y cliente JavaFX (confirma o objetas); login local
> en `iso2-seguridad` y SSO corporativo diferido a CDU19.
> Genera `PLAN.md` (desglose por iteración con parte servidor/cliente + plan de despliegue con
> canales alfa/beta/final + presupuesto de tokens), `STATUS.md` y `README.md`; crea el repo
> `github.com/IsmaelCaballero/PrISO2-25-26` con `main` y `develop` (Git Flow) y deja el repo local
> apuntando a él.

### Prompt 03 — Planificación de la evolución de componentes [✔]

> Planifica la evolución de componentes para las 21 iteraciones: cada componente es un submódulo
> Maven con SemVer estricto. Versión 1.0.0 = línea base de contratos publicada en Iter0
> (interfaces publicables, sin funcionalidad); a partir de ahí cada 1.y.0 es un incremento. Ordena
> los incrementos de cada componente por riesgo de desarrollo decreciente (propón niveles de
> riesgo para los 20 CDU con sus criterios). `iso2-common` y las dos apps siguen el tren de
> versiones de release (common 1.0.0→1.5.0; apps 0.x→1.0.0), no el esquema por riesgo. Si el orden
> actual de iteraciones rompe el riesgo decreciente de algún componente, propón inversiones de
> iteraciones que no alteren fechas, parejas ni coste.
> Genera `docs/componentes-versionado.md` + `docs/componentes-versiones.csv` (matriz
> componente×iteración), registra los ADR correspondientes y alinea `docs/planificacion-PUD.md`,
> `docs/planificacion-iteraciones.csv` y `PLAN.md`. Al terminar, sube todo y abre PR
> `feature/docs-plan-inicial` → `develop` (yo lo reviso y fusiono).

### Prompt 04 — Plan de calidad ISO/IEC 25010 [✔]

> Extiende la planificación con el plan de calidad. Modelo ISO/IEC 25010 priorizando 5
> características (el resto, cobertura básica por ser un proyecto de 5 semanas): adecuación
> funcional, usabilidad, fiabilidad, seguridad (incl. no repudio de la actividad clínica) y
> mantenibilidad (modularidad MVC + SOLID). Instrumentos: TC humanos por RF (1 feliz + ≥2 error +
> 1 de permisos), guardarailes ArchUnit de MVC/SOLID, anotación `@Auditable` en toda mutación con
> auditoría de hash encadenado, prueba de adaptador externo caído y ensayo de copia/restore de H2.
> Puertas por release sin cambiar el presupuesto: JaCoCo línea ≥60 % (R1) → 70 % (R2) → 75 % (R3)
> → 78 % (R4) → 80 % (final, ≥85 % en citas/solicitudes/seguridad); rama ≥45 %→70 %; fallos
> Surefire = 0. Figure de guardián de calidad rotatoria por release y prohibición de
> auto-fusionar el propio PR.
> Genera `SOFTWAREQUALITY.md` (modelo, DoD ampliado, impacto por componente y por CDU, puertas) y
> versiones `+Calidad` de los tres documentos de planificación (ficheros nuevos, no sobreescribas
> los v1). Registra los ADR de calidad.

### Prompt 05 — Plan de gestión de configuración y mapa de liberaciones [✔]

> Añade el plan de gestión de configuración: identifica los ítems de configuración (14 módulos
> Maven + contratos + migraciones `db/migration/V<versión>__<cdu>.sql` + suites de pruebas + docs
> vivos + pipeline CI, todo en Git). Define el mapa de configuraciones CFG-00..CFG-21: CFG-NN es
> la configuración mínima de la iteración NN (conjunto exacto de componentes+versiones); tag
> anotado `cfg-iterNN` en cada merge de iteración; ningún tag `v…` puede contener SNAPSHOT;
> auditoría de configuración tras cada merge contra la matriz de versiones. Hitos declarados por
> criterios cumplidos (no por fecha): CFG-06 `v0.2.0-alpha` (ALFA 1, R1), CFG-12 `v0.3.0-alpha`
> (ALFA consolidada, R2), CFG-18 `v0.4.0-beta` (BETA, R3), CFG-20 `v0.9.0-rc` (CANDIDATA, R4),
> CFG-21 `v1.0.0` (DEFINITIVA, R5). Ambientes dev/test/alfa/beta/demo y plan de rollback con
> `git checkout cfg-iterNN` + build + copia H2.
> Trabaja en una rama nueva `releaseplan` (desde el tip de la rama de docs): genera
> `docs/mapa-configuraciones.md` y `.csv`, las versiones `+Calidad` que falten de la matriz de
> componentes, los ADR de configuración, y abre PR apilado `releaseplan` →
> `feature/docs-plan-inicial` (yo fusiono).

### Prompt 06 — Arreglo del render de los CSV en GitHub [✔]

> En GitHub aparece que `docs/componentes-versiones.csv` tiene un error y no se visualiza como
> tabla: "row 9 should actually have 15 columns, instead of 14 in line 8". Arreglalo y sube la
> versión nueva. (El agente auditó además todos los demás CSV del repo, alineó las filas con
> comas que faltaban en ambos PR abiertos y verificó columnas uniformes.)

### Prompt 07 — Confirmación de PRs y creación de este registro [✔]

> Creo que ya está arreglado lo de los Pull Request en GitHub… lo puedes confirmar? Además
> necesito que crees un fichero nuevo llamado PROMPTS.md, donde metas en orden cronológico todos
> los prompts que un alumno debería ir escribiendo en un agente paso a paso para llegar al punto
> en el que estamos. A partir de ahora, cada uno de los prompts, escríbelos en este fichero.
> Cuando esté listo, vamos a por la iteración 1.

---

## Etapa 2 — Iteraciones de código — prompts 08 en adelante

### Prompt 08 — Iter0 de código: esqueleto Maven multi-módulo (CFG-00) [✔ ejecutado]

> Issue #6. Arranca la iteración 0 de código sobre la rama `feature/iter00-cfg00`:
> crea la estructura Maven multi-módulo en la raíz del repo (`iso2-parent`, pom empaquetador) con
> los 14 submódulos: `iso2-common` y los 10 módulos de dominio a versión **1.0.0** (línea base
> contractual: interfaces de fachada publicables en `iso2-common` + `package-info.java` con
> Javadoc, sin implementación) y `iso2-server-app` / `iso2-client-app` a **0.1.0** (caparazón
> arrancable: Spring Boot + H2 + JPA la servidora; JavaFX la cliente).
> Cableado obligatorio en el parent: `dependencyManagement` con `spring-boot-dependencies`,
> Surefire + JaCoCo (informes en `mvn verify`), ArchUnit con los guardarailes de D8 (fin de
> ciclos entre módulos, capas), Javadoc sin errores, y un workflow de GitHub Actions que ejecute
> `mvn -B verify javadoc:javadoc` en cada PR. Ejecuta `mvn verify` en local antes de subir.
> Abre PR a `develop` asociado al issue; al fusionarse, tags `cfg-iter00` y `v0.1.0` (R0).
>
> Resultado (07/11/2026): commit `e89863b` en `feature/iter00-cfg00` — reactor `iso2-parent` + 14
> submódulos (10 de dominio + common a 1.0.0, apps 0.1.0), contratos baseline con Javadoc, ArchUnit
> (guardarrailes D8), Surefire+JaCoCo, CI con `mvn -B verify javadoc:javadoc`. Build verde en local y
> en CI. PR **#7** abierto a `develop` (número #6 se perdió al cerrar un PR previo con base errónea);
> pendientes de Ismael: fusión + tags `cfg-iter00`/`v0.1.0`.

### Prompt 09 — Iter1 · CDU16 catálogo de parámetros (`iso2-catalogo` 1.1.0, CFG-01) [✔ ejecutado]

> Prompt lanzado por Ismael (07/11/2026):
> "Los casos de pruebas los diseñaremos una vez que tengamos el código, y sobre la decisión D18,
> mejor las fuentes de PlantUML en docs/modelo/iter01 + export a StarUML. ... con esto, podemos
> abordar la iteración 01"
>
> Ejecución: issue #9, rama `feature/iter01-cdu16-catalogo` (base: tip de Iter0 `e89863b`).
> Las dos entradas que bloqueaban el prompt quedan resueltas así: (a) D18 **aceptada** con fuentes
> PlantUML en `docs/modelo/iter01/` + export manual a StarUML; (b) los TC de CDU16 los diseñará
> Ismael sobre el código ya funcional (**D28**, revisión del orden de D16) — el agente deja pruebas
> técnicas de humo para las puertas (JaCoCo ≥ 60 % en `iso2-catalogo`) y los TC humanos cierran la
> iteración. Alcance ejecutado: contratos `iso2-common` 1.1.0-SNAPSHOT (CRUD fachada catálogo),
> `iso2-catalogo` 1.1.0 (dominio tipo de prueba + protocolo + política, auditoría hash encadenado,
> REST `/api/catalogo/pruebas`), RBAC transitorio Basic+ADMIN en `iso2-server-app` (JWT llega en
> CDU17) con migración Flyway `V1.1.0__cdu16_catalogo.sql`, pantalla JavaFX Admin·catálogo en
> `iso2-client-app`; versiones CFG-01 (apps 0.1.1-SNAPSHOT).
>
> Resultado (07/11/2026, rama `feature/iter01-cdu16-catalogo`, commits `bb337f5`..`ad99b21`):
> - `iso2-catalogo` 1.1.0: dominio `TipoPrueba` (+`Protocolo`/`Politica` embebidos), auditoría con
>   hash SHA-256 encadenado y `auditoriaVerificable()`, baja lógica idempotente, códigos no reutilizables;
>   REST `/api/catalogo/pruebas` (201/200/204, reactivación 200, errores 400/404/409 con `ApiError`).
>   24/24 tests, JaCoCo 96,5 % líneas.
> - `iso2-server-app` 0.1.1: Flyway `V1.1.0__cdu16_catalogo.sql`, `ConfigSeguridad` Basic+ADMIN
>   (JWT definitivo en CDU17), smoke 401/403/200/201/400. 6/6 tests, 91,7 %.
> - `iso2-client-app` 0.1.1: `ApiClienteCatalogo`/`ApiException` + `VistaAdminCatalogo`. 6/6 tests,
>   capa `api` 79,2 % (UI JavaFX excluida del gate, patrón de `iso2-common`).
> - `docs/modelo/iter01/`: 4 diagramas PlantUML (casos de uso, secuencia, clases análisis/diseño) +
>   README de exportación a StarUML (D18). El de casos de uso lleva actores de rol (Médico, Técnico
>   facultativo, Celador, Recepcionista) con solo consulta a UC16.5 y UC7 de auditoría fuera de la
>   frontera; revalidados los 4 con PlantUML 1.2025.2.
> - Falta de Ismael: fusión del PR #7 (Iter0) + tags, fusión del **PR #10** (Iter1), **TC humanos de CDU16**
>   (D28) y exportación `.staruml`.

### Prompt 09b - Cierre de jornada 7: guardar estado tras las fusiones de Ismael [✔ ejecutado]

> Usuario (07/11 noche): «guarda el estado de todo, que de momento lo vamos a dejar por hoy aqui»

- Contexto de última hora: durante el cierre, Ismael fusionó **PR #7** (merge `641e72d`) y
  **PR #10** (merge `df09bca`, nuevo tip de `develop`) y cerró **sin fusionar** el PR #3
  (`develop`→`main`). Los issues #6/#9 no se cerraron solos: `Closes #N` solo opera al fusionar
  a la rama por defecto (`main`) → registrada como **D29** (cierre manual de issues en merges a `develop`).
- Hecho: rama `docs/cierre-sesion-7` desde `develop` con PR de sincronización: STATUS.md (fila
  sesión 7, punto de continuación y cierre de jornada), DECISIONS.md (D29) y esta entrada en
  PROMPTS.md. Sin cambios de código. El trabajo de Iter1/CDU16 queda fusionado en `develop`.
