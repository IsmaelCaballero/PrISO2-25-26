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
   Los hitos de release salen siempre de una rama `release/RN` creada desde el tag de la CFG hito → PR a
   `main` → merge commit (`--no-ff`) → tag `v<versión>`; nunca un PR `feature/…` → `main` (D30).
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

### Prompt 10 — Iter2 · CDU17 seguridad RBAC+JWT (`iso2-seguridad` 1.1.0, CFG-02) [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026): arrancar la Iteración 2 (CDU17, CFG-02) con el prompt
> ejecutable del issue #13: `iso2-seguridad` 1.1.0 con dominio de cuentas (BCrypt + bloqueo por
> intentos), 5 roles SESCAM y matriz RBAC; JWT stateless con secreto solo por `ISO2_JWT_SECRET`;
> `POST /api/auth/login` + filtro; server-app 0.1.2 con la matriz por endpoints y Flyway de
> cuentas semilla; client-app 0.1.2 con login por rol y vista de solo lectura; diagramas PlantUML
> en `docs/modelo/iter02/`. Puertas CFG-02: RBAC en el 100 % de endpoints, `JwtUtils` ≥ 90 %,
> secretos fuera del repo.
>
> Ejecución: rama `feature/iter02-cdu17-seguridad` (base: tip de `develop` `67ed627`). Versions
> CFG-02: `iso2.version` 1.1.0-SNAPSHOT (módulo nuevo incluido), apps 0.1.2-SNAPSHOT, jjwt 0.12.6
> en el `dependencyManagement` del parent. El agente escribe solo pruebas técnicas (D28); los TC
> humanos de CDU17 los diseñará Ismael tras la fusión.

- Resultado (08/11/2026, rama `feature/iter02-cdu17-seguridad`, commits `c8213ee`..`2f8090a` + docs sync):
  - `iso2-seguridad` 1.1.0-SNAPSHOT (módulo nuevo): dominio `Usuario` JPA (tabla `seguridad_usuario`,
    hash BCrypt, contador de intentos y `bloqueadoHasta`; bloqueo a los 5 intentos), `Rol`
    (ADMINISTRADOR/MEDICO/TECNICO/CELADOR/RECEPCIONISTA), `Permiso` (`catalogo:leer`,
    `catalogo:escribir`, `usuario:gestionar`) y `MatrizPermisos` como fuente autoritativa única;
    `SeguridadServicio` implementa la fachada congelada `AutenticacionFacade`; `JwtUtils` HS256 con
    claim `rol`, caducidad corta y secreto solo por `ISO2_JWT_SECRET` (aborta el arranque si falta);
    `ControladorAutenticacion` (`POST /api/auth/login`, advice 401/401/400 con `ApiError`) y
    `JwtAutenticacionFiltro` stateless. 36/36 tests; JwtUtils 100 % de líneas (gate ≥ 90 %).
  - Fix clave: `@Transactional(noRollbackFor = CredencialesInvalidasException)` en el login — sin él
    el rollback dejaba el contador de intentos a 0 y la cuenta nunca se bloqueaba; lo destapó el
    test de integración de server-app (los unitarios con mock no podían verlo).
  - `iso2-server-app` 0.1.2-SNAPSHOT: `ConfigSeguridad` stateless con la matriz RBAC por endpoint
    (login público; GET `/api/catalogo/**` para cualquier rol autenticado; escritura solo
    `ADMINISTRADOR`; h2-console solo ADMIN vía JWT), 401/403 con `ApiError` JSON y Flyway
    `V1.2.0__cdu17_seguridad.sql` con 6 cuentas semilla BCrypt. 23/23 tests: `SeguridadApiTest`
    (lectura para los 5 roles, 16 combinaciones 403, 401 anónimo/manipulado/otra clave/caducado,
    bloqueo real tras 5 intentos → `CUENTA_BLOQUEADA`, 400 `DATOS_INVALIDOS`) + smoke catálogo
    Bearer con el contrato real de Iter1. Evidencia de la puerta "RBAC 100 % endpoints".
  - `iso2-client-app` 0.1.2-SNAPSHOT: `ApiClienteAutenticacion` (traduce `ApiError` a `ApiException`),
    `ApiClienteCatalogo` pasa de Basic a Bearer, `NavegacionPorRol.catalogoEditablePara(rol)` (regla
    de menú testeable fuera de JavaFX), `VistaInicioSesion` + `VistaConsultaCatalogo` (solo lectura)
    y arranque login-first (fuera `-Diso2.usuario/-Diso2.password`). 16/16 tests.
  - `docs/modelo/iter02/`: `cdu17-casos-uso.puml`, `cdu17-secuencia.puml`,
    `cdu17-clases-analisis.puml`, `cdu17-clases-diseno.puml` + README (D18). Los 4 renderizados a
    SVG con PlantUML 1.2025.2 (`-Playout smetana`), exit 0, sin errores sintácticos.
  - Reactor completo: `mvn -B -ntp verify javadoc:javadoc` BUILD SUCCESS 14/14, ArchUnit sin
    violaciones, puertas JaCoCo OK.
  - **PR #14 → `develop` abierto** (https://github.com/IsmaelCaballero/PrISO2-25-26/pull/14). Falta
    de Ismael: fusionar PR #14, tag anotado `cfg-iter02`, cerrar #13 a mano (D29), **TC humanos de
    CDU17** (D28) y export `docs/modelo/iter02/staruml/iso2-iter02.staruml` (D18). Sigue pendiente
    también el cierre de CFG-01 (TC de CDU16 + export iter01).

### Prompt 11 — Iter3 · CDU13 gestionar agendas (`iso2-agendas` 1.1.0, CFG-03) [✔ ejecutado · parte servidora; cliente queda para el 11b]

> Prompt lanzado por Ismael (08/11/2026): arrancar la Iteración 3 (CDU13, CFG-03) con el prompt
> ejecutable del issue #15: `iso2-agendas` 1.1.0 con franjas de disponibilidad y festivos (reglas
> de solape y "el festivo manda"), auditoría encadenada y REST `/api/agendas`; extensión MINOR de
> `AgendaFacade` sin tocar el baseline congelado; RBAC de agenda en `iso2-seguridad`; server-app
> 0.1.3 con Flyway de tablas de agenda y semilla dev; diagramas PlantUML en `docs/modelo/iter03/`.
> Puertas CFG-03: TC de solapes/festivos, frontera ArchUnit, JaCoCo ≥ 60 % en `iso2-agendas`.
>
> Ejecución: por ser iteración de alta complejidad se divide en dos sesiones (CONTEXT.md §7):
> esta (11) hace diagramas + módulo servidor + pruebas del servidor; la siguiente (11b) hará
> client-app 0.1.3 + tests de integración + apertura del PR. Iter2 se dio por cerrada antes de
> arrancar: Ismael fusionó **PR #14** (merge `870524e` = tip de `develop`) y cerró #13; el tag
> `cfg-iter02` queda aún pendiente.

- Resultado (08/11/2026, rama `feature/iter03-cdu13-agendas`, base `870524e`, commits `e02f425`..`0366247`, **subida sin PR a propósito**):
  - `iso2-common` 1.1.0-SNAPSHOT: `AgendaFacade` extendida en MINOR — `franjasLibres`+`FranjaDTO`
    del baseline intactos; añadidos `listarAgendas`/`buscarPorProfesional`/`crearAgenda`/
    `anadirFranja`/`eliminarFranja`/`anadirFestivo`/`eliminarFestivo` con registros `AgendaDTO`,
    `FranjaGestionDTO` y `FestivoDTO`; nuevas `AgendaNoEncontradoException` y
    `AgendaConflictoException` (conflictos `SOLAPE`, `FESTIVO`, `AGENDA_DUPLICADA`).
  - `iso2-agendas` 1.1.0-SNAPSHOT: dominio JPA `Agenda` (una por profesional, duplicada →
    `AGENDA_DUPLICADA`), `Franja` (invariante `inicio < fin`; sin solape el mismo día — los bordes
    que se tocan **no** solapan), `Festivo` (el festivo manda: franja en festivo, festivo con
    franjas y festivo repetido → `FESTIVO`) y auditoría SHA-256 encadenada con GENESIS
    (`ALTA_AGENDA`/`ALTA_FRANJA`/`BAJA_FRANJA`/`ALTA_FESTIVO`/`BAJA_FESTIVO`) con
    `auditoriaVerificable()`. REST `/api/agendas` con mapa 404 `AGENDA_NO_ENCONTRADA` /
    409 `CONFLICTO_SOLAPE|CONFLICTO_FESTIVO|CONFLICTO_AGENDA_EXISTENTE` / 400 `DATOS_INVALIDOS`.
  - Fix notable: Hibernate inserta los hijos en cascada pero con id `@IDENTITY` **no reasigna el id
    en la instancia en memoria** (probado con test de descarte: DTO con `id=null` mientras la fila
    nacía con id) → `FranjaRepositorio`/`FestivoRepositorio` persisten los hijos directamente para
    que el DTO de alta devuelva el id.
  - `iso2-seguridad` 1.1.0-SNAPSHOT: `Permiso` suma `AGENDA_LEER`/`AGENDA_ESCRIBIR`; matriz: ADMIN
    en bloque, MEDICO/TECNICO/RECEPCIONISTA solo lectura, RECEPCIONISTA también escritura, CELADOR
    sin agendas. `MatrizPermisosTest` 5 roles × 5 permisos; módulo 36/36.
  - `iso2-server-app` 0.1.3-SNAPSHOT: monta `iso2-agendas` `${iso2.version}` (resto de módulos sigue
    anclado a 1.0.0); `ConfigSeguridad`: GET `/api/agendas/**` para ADMIN/MEDICO/TECNICO/
    RECEPCIONISTA, escritura solo ADMIN/RECEPCIONISTA; Flyway `V1.3.0__cdu13_agendas.sql` (4 tablas
    + FKs + índices + semilla dev de medico/tecnico con festivo 2026-11-10).
  - Pruebas técnicas (D28): `AgendaServicioTest` 15/15 + `ArquitecturaAgendasTest` 4/4 + baseline
    1/1 (JaCoCo ≥ 60 % en verde); `AgendasApiTest` 16/16 — RBAC completo y reglas de solape/festivo
    por HTTP contra la app servidora con H2. Reactor: `mvn -B -ntp verify` BUILD SUCCESS 14/14 y
    `javadoc:javadoc` OK.
  - `docs/modelo/iter03/`: `cdu13-casos-uso.puml`, `cdu13-secuencia.puml`,
    `cdu13-clases-analisis.puml`, `cdu13-clases-diseno.puml` + README (D18). Los 4 renderizados a
    SVG, exit 0.
  - **Pendiente para el Prompt 11b:** `iso2-client-app` 0.1.3 + tests de integración + apertura del
    PR de Iter3 → `develop` (sin `Closes`, D29; issue #15). De Ismael sigue adeudado: tag
    `cfg-iter02` sobre `870524e`, TC humanos de CDU17 + export `iso2-iter02.staruml`, y los de
    CFG-01 (TC CDU16 + `iso2-iter01.staruml`).

### Prompt 11b — Iter3 · CDU13 cliente JavaFX + integración real + PR (CFG-03) [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026): rematar la Iteración 3 — `iso2-client-app` 0.1.3 con
> cliente HTTP de agendas y vistas JavaFX según el rol (edición para ADMIN/RECEPCIONISTA, solo
> lectura para MEDICO/TECNICO, sin agendas para CELADOR), tests de integración con el cliente y el
> servidor reales, y apertura del PR de Iter3 → `develop` sin `Closes` (issue #15, cierre manual
> tras el merge, D29). Puertas CFG-03 pendientes: TC solapes/festivos por HTTP y ambas apps 0.1.3.

- Resultado (08/11/2026, misma rama `feature/iter03-cdu13-agendas`, commits `ae79d7e`..`56dac60`,
  **PR #18 → `develop` abierto, pendiente de fusión de Ismael**):
  - `iso2-client-app` 0.1.3-SNAPSHOT: `ApiClienteAgendas` contra `/api/agendas` (listar, detalle,
    alta/baja de franjas y festivos, alta de agenda, franjas libres) reutilizando el token del
    login real; los errores del servidor llegan como `ApiException` con `codigo` y **`mensaje()`
    (método nuevo)** para mostrarlos en la UI.
  - Navegación: barra de secciones Catálogo/Agendas en `Iso2ClientApplication` (escena 1100x680);
    reglas nuevas en `NavegacionPorRol` (`agendasVisiblePara`: CELADOR no; `agendasEditablesPara`:
    solo ADMIN/RECEPCIONISTA); `VistaGestionAgendas` (edición) y `VistaConsultaAgendas` (solo
    lectura). Ventana 0.1.3 con la barra y las vistas montadas según el rol en sesión.
  - Detalle del contrato JSON: Jackson del cliente serializa `LocalTime` **con segundos**
    (`"08:00:00"`, comprobado con jshell sobre jsr310 2.19.2) — es lo que Spring espera al
    deserializar; la aserción del humo lo refleja.
  - Pruebas técnicas (D28): `ApiClienteAgendasTest` 11/11 con servidor HTTP local (cuerpos ISO,
    mapa de errores 400/404/409); `NavegacionPorRolTest` ampliado; `ClienteCaparazonTest` a 0.1.3
    — cliente 37/37 en verde.
  - `iso2-server-app`: dependencia **test-scoped** de `iso2-client-app` 0.1.3-SNAPSHOT (versión
    explícita, no `${iso2.version}`; sin ciclo porque el reactor reordena) →
    `AgendasClienteServidorIntegracionTest` 7/7: app servidora en `RANDOM_PORT` consumida por
    `ApiClienteAutenticacion` y `ApiClienteAgendas` reales — flujo completo de la recepcionista con
    `CONFLICTO_SOLAPE` (bordes que se tocan admitidos) y `CONFLICTO_FESTIVO`, festivo que manda en
    ambos sentidos, agenda duplicada 409, 404 con código en el cuerpo, CELADOR 403 `FORBIDDEN` y
    franjas libres de la semilla del 9 al 13/11 con el festivo del 10/11 fuera. ids `it-*` y
    fechas 2027 para no chocar con la semilla ni con los `api-*` de `AgendasApiTest`.
  - fix(agendas) destapado por la integración: `AgendaControlador.detalle` de profesional sin
    agenda respondía 404 **con cuerpo vacío** (el cliente se quedaba sin código ni mensaje para la
    UI) → lanza `AgendaNoEncontradoException` y el advice devuelve `AGENDA_NO_ENCONTRADA` en el
    cuerpo, igual que el resto de rutas.
  - Puertas CFG-03: TC solapes/festivos ✔ (servicio + API + HTTP de integración en ambos
    sentidos), frontera ArchUnit ✔, JaCoCo ≥ 60 % en `iso2-agendas` ✔, ambas apps 0.1.3-SNAPSHOT
    ✔. `mvn -B -ntp verify` BUILD SUCCESS (server-app 46/46, client-app 37/37). Falta el tag
    `cfg-iter03`, que se creará al fusionar.
  - **De Ismael:** fusionar PR #18, cerrar #15 a mano (D29) y crear el tag anotado `cfg-iter03`;
    siguen adeudados el tag `cfg-iter02` sobre `870524e`, los TC humanos y exports `.staruml` de
    CFG-01/CFG-02 y los TC humanos de CDU13 (criterio de cierre de CFG-03).

### Prompt 11c — Cierre de Iter3 en GitHub: tags, reparación de `main` y release R0 (CFG-00) [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026): tras ver las tres opciones que el agente propuso para publicar
> R0 en `main`, «me parecen bien las tres opciones… me quedo con la recomendada, la 1. Prepárala y
> avísame cuando la tengas. Crea por favor también los tags». Opción 1 = rama `release/R0` desde el tag
> `cfg-iter00`, PR a `main`, fusión con merge commit y `v0.1.0` sobre ese merge. Después, al pedir el
> agente visto bueno sobre tres puntos, Ismael autoriza: **force-push** a `main` + abrir el PR,
> **borrar** la rama `feature/iter03-cdu13-agendas` y crear **issues + ADR** de los pendientes.

- Resultado (08/11/2026, sesión 12; rama `docs/cierre-iter03-y-r0` para los ficheros de proceso):
  - **Tags anotados creados y subidos** con el mismo estilo que `cfg-iter00/01` (asunto del merge +
    resumen de la CFG, puertas técnicas y pendientes D28/D18):
    `cfg-iter02` → `870524e` (PR #14, Iter2/CDU17) y `cfg-iter03` → `b072b34` (PR #18, Iter3/CDU13).
    Iter3 queda cerrada en `develop`: CI verde e issue #15 cerrada a mano por Ismael (D29).
  - **Hallazgo sobre `main`:** el PR #17 (`feature/iter03-cdu13-agendas` → `main`, merge `5e0ef29`) se
    abrió por error. Comprobado con `git log --oneline origin/main --not origin/develop` que el **único**
    commit de `main` no alcanzable desde `develop` era el propio merge, y que antes del PR #17 `main`
    estaba justo en el raíz `1245138`: descartarlo no pierde trabajo (los 15 ficheros / 1432 líneas del
    lado cliente-integración-404 ya estaban en `develop`). `main` no tenía protección de rama (404).
  - **Reparación ejecutada con visto bueno explícito:**
    `git push --force-with-lease=refs/heads/main:5e0ef29… origin 1245138:refs/heads/main` →
    `origin/main` = `1245138`. Descartado `git revert -m 1` (dejaría los ficheros revertidos y el futuro
    merge de `develop` no los re-aplicaría) y dejar `main` tal cual (una release parcial sin etiquetar).
  - **Release R0 publicada:** rama `release/R0` creada sobre `cfg-iter00` (`641e72d`, árbol idéntico a
    CFG-00: 14 módulos + apps `0.1.0`) y **PR #19 `release/R0` → `main`** abierto
    (https://github.com/IsmaelCaballero/PrISO2-25-26/pull/19), sin `Closes` (D29). Ismael lo fusionó con
    **merge commit** `e165622` (padres `1245138` + `641e72d`, no squash ni rebase) y el agente creó y subió
    el tag anotado **`v0.1.0`** sobre ese merge: es el hito R0 del mapa
    (`tag_git = v0.1.0;cfg-iter00`) y la primera etiqueta de release del repo. CFG-00 queda publicada.
  - **Limpieza:** rama `feature/iter03-cdu13-agendas` borrada en remoto y local; `develop` local
    actualizado a `b072b34`.
  - **Issues nuevas de seguimiento:** #20 publicar R0 con `v0.1.0`, #21 TC humanos de CDU16/CDU17/CDU13
    (CFG-01/02/03, D28), #22 export `.staruml` de Iter1-3 (D18), #23 Kanban de GitHub Project + wiki.
    Al final de la sesión se comprueba su estado: **#20 cerrada correctamente por Ismael** (R0 publicada) y
    **#21, #22 y #23 reabiertas** — se habían cerrado a las 12:34–12:35 con motivo `completed` sin
    artefactos detrás: ningún commit ni rama nueva en `develop` con las tablas de TC, `git ls-tree -r
    origin/develop` sin ningún `.staruml` y `GET /repos/…/wiki` en 404 (wiki sin páginas). Cada issue lleva
    un comentario con la comprobación; CFG-01/02/03 siguen abiertas en el mapa por D28 y D18. El Kanban de
    GitHub Project no se pudo verificar (el token del agente carece de scope `read:project`).
  - **Nueva decisión D30** en `DECISIONS.md`: `main` solo recibe merges de rama `release/RN` (nunca
    `feature/…`), con la incidencia del PR #17 documentada y la comprobación previa obligatoria
    (`git log --oneline origin/main --not origin/develop`). Actualizado el §4 de
    `docs/mapa-configuraciones.md` (flujo de ramas y etiquetado).
  - **Siguiente sesión:** Iter4 · CDU06 solicitudes (CFG-04) desde `develop` = `b072b34`
    (`iso2-solicitudes` 1.1.0, apps 0.1.4-SNAPSHOT, puertas: cobertura de solicitudes ≥ 85 % y huella
    de contenido). En GitHub solo queda fusionar el PR de docs #24 y retomar #21 (TC humanos), #22
    (`.staruml`) y #23 (Kanban + wiki), todas reabiertas al final de la sesión.

### Prompt 11d — Modelado con PlantUML (D31): PNG versionados y plantillas de TC [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026): «¿Puedes entregarme la plantilla de TC y recordarme qué se
> espera de mí? Y ajusta toda la documentación a PlantUML, quitando las referencias a StarUML; los
> `.puml` se ven fatal en GitHub, así que incrusta los `.png` en los README (reducidos, pero que al
> hacer clic se abran a tamaño completo)».

- Resultado (08/11/2026, sesión 13; rama `docs/plantuml-evidencia` → `develop`):
  - **Comprobación previa:** `tools/plantuml.jar` (30.020.557 B, sin versionar) con Java 25.0.1 renderiza
    de forma reproducible: los PNG de iter01 regenerados salen **idénticos byte a byte**. Y **no existe
    ningún `.staruml`** en el repo, la historia (`git log --all`), la wiki ni el disco: el trabajo de
    modelado se ha hecho íntegramente con PlantUML.
  - **Nueva ADR D31** en `DECISIONS.md` (D18 anotada como revisada): los artefactos de modelado de cada
    CFG son los `.puml` (fuente editable, ítem de configuración D25) **más los `.png` renderizados con
    `tools/plantuml.jar`, ambos versionados** en `docs/modelo/iterXX/`; la herramienta no se versiona
    (`*.jar` en `.gitignore`). La exportación a StarUML queda descartada. `CONTEXT.md` §4 reescrito como
    «Modelado por iteración (PlantUML)».
  - **README de `docs/modelo/iter01|02|03` adaptados:** intro citando D31, sección «Diagramas» con cada
    PNG incrustado `<a href="X.png"><img … width="700|900|980"/></a>` (clic = tamaño completo), comando
    de regeneración `java -jar tools/plantuml.jar -tpng docs/modelo/iterNN/*.puml` y sección «Artefactos
    de entrega (D31)». Los 12 PNG (4 por iteración, ~1,6 MB) quedan versionados. Issue #22: **no procede**
    el export; Ismael la re-cierra y el seguimiento pasa a `STATUS.md`.
  - **Plantillas de TC humanos (D28) creadas en `docs/tc/`:** `README.md` (regla D24, columnas de quién
    rellena qué, arranque con `ISO2_JWT_SECRET`, tabla de usuarios semilla de `V1.2.0` con contraseñas de
    desarrollo) + `cdu16-tc-humanos.md` (6 TC), `cdu17-tc-humanos.md` (6 TC) y `cdu13-tc-humanos.md`
    (8 TC). Tipo y puerta de calidad **prefijados** desde la matriz §5 de `SOFTWAREQUALITY.md`, endpoints
    y códigos de error verificados en el código fusionado (`CONFLICTO_SOLAPE`, `CUENTA_BLOQUEADA`,
    bloqueo 5/15 min…); pasos y resultados quedan en blanco: los diseña y ejecuta **Ismael**.
  - **Wiki:** creada como repo git del repositorio (`PrISO2-25-26.wiki.git`, commit `60e984d`) con una
    única página `Home.md` que enlaza README/CONTEXT/PLAN/STATUS/DECISIONS/PROMPTS/mapa de configuraciones.
  - **GitHub:** #21/#22/#23 re-cerradas por Ismael (12:56-12:57) tras reabrirlas el agente; no se
    reabren, el seguimiento queda en `STATUS.md`. PR #24 fusionado (`c1219a8` = tip de `develop`).
  - **Siguiente sesión:** Iter4 · CDU06 solicitudes (CFG-04) desde `develop` = `c1219a8`, con modelo en
    `docs/modelo/iter04/` ya en formato D31 (`.puml` + `.png` incrustados). Pendiente de Ismael: rellenar
    y ejecutar los TC de `docs/tc/` y revisar el Kanban visualmente.

### Prompt 11e — Wiki con el trabajo de cada iteración por componente

> Prompt lanzado por Ismael (08/11/2026): «Me parece bien. Tareas hechas. Te quería hacer una pregunta:
> ¿podemos crear en la Wiki del Proyecto alguna descripción del trabajo que se va haciendo en cada
> iteración por cada componente, añadiendo los diagramas correspondientes? Si no, vamos a por la Iter4».

- Resultado (08/11/2026, mismo día; wiki = repo `PrISO2-25-26.wiki.git`, merge `50fd48b`):
  - **Sí se puede**, editando el repo wiki por git (`git clone …/PrISO2-25-26.wiki.git`, rama `master`);
    la API REST de wiki da 404, así que no hay otra vía programática.
  - **`Home.md` reindexado:** ficheros vivos del repositorio + tabla «Trabajo por iteración» con los
    diagramas, y una página por iteración ya cerrada.
  - **Cuatro páginas nuevas**, una por CFG cerrada: `Iter0-CFG-00-esqueleto`, `Iter1-CDU16-catalogo`,
    `Iter2-CDU17-seguridad`, `Iter3-CDU13-agendas`. Cada una cabecera con CDU/CFG/rama/PR/tag/issue/fecha,
    una **tabla de componentes con su versión** (`iso2-common`, el módulo de negocio, `iso2-seguridad`,
    `iso2-server-app`, `iso2-client-app`) y el trabajo hecho en cada uno, los **cuatro diagramas** de la
    iteración, y las **puertas** de esa configuración (técnicas verdes + TC humanos pendientes de Ismael).
  - **Imágenes:** los 12 PNG se copian a `images/` del repo wiki y se incrustan con `<img … width>`
    reducidos (620-900 px) + enlace «Ver a tamaño completo» al raw del wiki (mismo criterio D31).
    Verificado sobre el HTML renderizado: las cuatro páginas dan 200, los `<img>` conservan el ancho y
    los enlaces a `raw.githubusercontent.com/wiki/…` responden 200 `image/png`.
  - Iter0 va sin diagramas (CFG-00 no implementa CDU: deja el reactor de 14 módulos y las fachadas).

### Prompt 12 — Javadoc agregado en GitHub Pages (D32) y wiki enlazada a las fichas [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026, **texto reconstruido**: no se guardó el verbatim de la sesión 14):
> en las páginas de la wiki quieres mencionar las clases; ¿podríamos dejarlas enlazadas a su ficha del
> Javadoc? Eso exige tener el Javadoc publicado en algún sitio estable. Y al terminar, preparamos la Iter4.

- Resultado (08/11/2026; rama `ci/javadoc-pages`, commit `00f93e9`, issue **#27**, PR **#28**):
  - Issue **#27** «Publicar el Javadoc agregado en GitHub Pages» con el prompt ejecutable en el cuerpo.
  - Workflow nuevo `.github/workflows/javadoc.yml`: en `push` a `develop` (y `workflow_dispatch`) ejecuta
    `mvn -B -ntp javadoc:aggregate` y publica `target/reports/apidocs` con `upload-pages-artifact` +
    `deploy-pages` (permisos `pages: write`, `id-token: write`, JDK 21). Índice en `docs/javadoc-index.html`.
  - **Fix encontrado de paso:** `ci.yml` subía como artefacto `**/target/site/apidocs/**`, pero el plugin
    escribe en `target/reports/apidocs` → el artefacto de Javadoc del CI estaba vacío desde Iter0.
  - ADR **D32** en `DECISIONS.md`: Pages publicado **desde `develop`** (no por tag), solo la última
    compilación, sin instantáneas históricas; las páginas de la wiki enlazan la ficha de cada clase a
    `https://ismaelcaballero.github.io/PrISO2-25-26/apidocs/…`.
  - **PR #28 → `develop` fusionado por Ismael** (merge `c8fe2d7`, tip actual de `develop`); deploy de Pages
    verificado en la URL anterior. Issue **#27 cerrada a mano** a las 19:48 (D29).
  - **Wiki ampliada** (repo `PrISO2-25-26.wiki.git`, rama `master`, merge `90d5b26`): en las cuatro páginas
    por CFG cada componente pasa a **enlace a su ficha Javadoc** (rutas reales comprobadas contra el
    `apidocs` publicado) y se añade una **sección Testing** con tests por módulo y puertas JaCoCo.
  - Cerrada la sesión se creó la issue **#30** (Iter4 · CDU06, CFG-04) con el prompt de la Iter4 en el
    cuerpo, para arrancar la Iter4 desde `develop` = `c8fe2d7`.

### Prompt 13 — Iter4 · CDU06 solicitar/prescribir pruebas (`iso2-solicitudes` 1.1.0, CFG-04) [✔ ejecutado]

> Prompt lanzado por Ismael (08/11/2026, **texto reconstruido** a partir del cuerpo de la issue #30 y de la
> sesión): ejecutar la Iter4 con el prompt de la issue #30 — `iso2-solicitudes` 1.1.0 con indicación clínica,
> prioridad y **huella SHA-256** de la solicitud; **diseño PlantUML antes de escribir código**; extensión
> MINOR de `SolicitudFacade` sin tocar el baseline; RBAC de solicitudes; server-app 0.1.4 con Flyway
> `V1.4.0` y semilla dev; cliente JavaFX con vista de prescripción y vista de consulta. Puertas CFG-04:
> **cobertura de `iso2-solicitudes` ≥ 85 %**, campos RF obligatorios, **no repudio** y duplicado imposible.

- Resultado (08/11/2026; rama `feature/iter04-cdu06-solicitudes` desde `c8fe2d7`, 4 commits, PR **#31**):
  - **Diseño primero** (`cce3af2`, 19:04): `docs/modelo/iter04/` con 4 `.puml` + 4 `.png` (casos de uso,
    clases de análisis, clases de diseño y secuencia) renderizados con `tools/plantuml.jar -tpng
    -Playout smetana` y README con los PNG incrustados (D31). El diseño fija antes de programar: dominio
    `Solicitud` con huella SHA-256 del canónico, `creadaEn` truncado a `ChronoUnit.MICROS`, anti-duplicado
    en el servicio, Flyway `V1.4.0`, permisos `solicitud:leer/escribir` y puerta `jacoco.line.min=0.85`.
  - **Servidor** (`71ce94b`, 19:34): `iso2.version` e `iso2-solicitudes` a **1.1.0-SNAPSHOT**, apps a
    **0.1.4-SNAPSHOT**. `SolicitudFacade` ampliada en MINOR (`prescribir`/`listarPorMedico`/`detalle`/
    `trazable`) sin tocar el baseline congelado; dominio JPA con los campos RF obligatorios, no repudio
    (autor + timestamp + huella), duplicado imposible, `Clock` inyectable para poder fijar el instante;
    REST `/api/solicitudes` con RBAC y Flyway `V1.4.0__cdu06_solicitudes.sql` (semilla `SEM-RX-TORAX`,
    `SEM-RM-RODILLA`). El **autor no viaja en el cuerpo HTTP**: el cliente manda 5 campos y el servidor lo
    fija (el `medicoId` del DTO solo sirve para validar RF06.1 en el cliente).
  - **Fix doclint** (`7f85678`): en un `record` no vale documentar `@throws` — doclint lo rechaza y rompía
    `javadoc:javadoc`.
  - **Cliente** (`b4c4e27`, 19:51): `ApiClienteSolicitudes` contra `/api/solicitudes`, dos vistas al estilo
    de Iter3 (`VistaSolicitudesMedico` de escritura, `VistaConsultaSolicitudes` de lectura) y las puertas
    testeables `solicitudesVisiblesPara`/`solicitudesPrescribiblesPara` en `NavegacionPorRol`.
  - **Reactor verde** `mvn -B -ntp verify javadoc:javadoc`: **229 tests** (en Iter3 había 172;
    `iso2-solicitudes` 1→24, `iso2-server-app` 46→60, `iso2-client-app` 37→57) y **JaCoCo de
    `iso2-solicitudes` 87,2 %** (109/125 líneas, 32/50 ramas) → puerta ≥ 85 % cumplida.
  - **PR #31 → `develop` abierto** (19:55, sin `Closes`, D29) con la CI `construir` **en verde**
    (run 42512406096, job 2m6s). La issue **#30** ya la había cerrado Ismael a las **19:01**, antes del
    merge: D29 cumplida por adelantado, no se reabre.
  - Pendiente tras el merge (solo Ismael fusiona): tag anotado `cfg-iter04`, borrar la rama feature, publicar
    la página de wiki `Iter4-CDU06-solicitudes` y decidir la plantilla de **TC humanos de CDU06** (D28).
  - En esta misma sesión se abre el PR de docs `docs/cierre-iter04-sesiones-14-15` (este `STATUS.md` +
    `PROMPTS.md`) → `develop`.
  - **Siguiente bloque:** Iter5 · CDU12 citas (CFG-05) — puertas de concurrencia de hueco y rollback.

### Prompt 14 — Cierre de Iter4 en GitHub + nueva estrategia de pruebas (D33) y persistencia MySQL [✔ ejecutado — cierre y wiki; rediseño de pruebas y MySQL pendientes]

> Prompt lanzado por Ismael (08/11/2026, sesión 16, **texto reconstruido** de la conversación): cerrar la
> Iter4 y preparar la Iter5 **cambiando el enfoque de dos cosas**. (1) **Las pruebas**: a partir de ahora hay
> que desarrollarlas **sobre la base de clases de equivalencia, el teorema de los valores límite y la
> conjetura de errores**, y además pasar esos casos por **pairwise** para reducir la combinatoria; esos mismos
> casos se ejecutan **con JUnit en Maven** y son **exactamente los que van en las plantillas de casos de prueba
> de los TC**, tanto las que ya tenemos como las que aparezcan; los **ficheros de test se documentan con
> JavaDoc**; el resultado se lleva a la **wiki** y hay que **identificar claramente la clase que se está
> probando**; hay que **revisar con más rigurosidad los casos que ya tenemos** y **empezar por las pruebas de la
> Iteración 1**; el agente **genera las plantillas y todos los casos**. (2) **La persistencia**: vamos a
> trabajar sobre **MySQL**; desplegar **Docker con una imagen de MySQL y phpMyAdmin**, y también el cliente y el
> servidor; **password de `root` `Iso2Curso25-26`** y un usuario **`gestoroperaciones` / `Iso2Curso25-26`** para
> manejar las operaciones de entrada y salida; el acceso a los datos se hace con el **patrón DAO** y la conexión
> con el **patrón Agente** particularizado en **`AgenteMySQL`** (el Agente es una abstracción que permite
> conectar a varios SGBD). «De momento vamos a hacer esto. Dime si necesitas algo más.»

- Resultado de la parte de **cierre** (08/11/2026; rama `docs/cierre-iter04-material` desde `origin/develop`,
  PR a `develop`):
  - Ismael fusiona **PR #31** (código Iter4/CDU06, merge **`a3fe109`**) y **PR #32** (docs sesiones 14-15, merge
    **`3b02718`**); CI `construir` en verde. Tip de `develop` = `3b02718`.
  - **Tag anotado `cfg-iter04` creado sobre `a3fe109`** y subido. Criterio aplicado: el tag va sobre el **merge
    del código** (la configuración que superó la puerta de cobertura ≥ 85 %), **no** sobre el merge documental
    posterior.
  - Rama `feature/iter04-cdu06-solicitudes` **borrada** (remoto y local) tras el merge.
  - **Wiki** (repo `PrISO2-25-26.wiki.git`, rama `master`, merge `5207bae`): página nueva
    **`Iter4-CDU06-solicitudes.md`** con los 4 diagramas de `docs/modelo/iter04/` subidos como
    `images/iter04-cdu06-*.png`, la tabla de componentes enlazando su ficha Javadoc (D32) y la sección
    **Testing** con la **clase probada** identificada en cada fila; fila nueva en `Home.md`.
  - Plantilla de TC que faltaba, creada: **`docs/tc/cdu06-tc-humanos.md`** con 14 casos (TC06-01…TC06-14)
    derivados de la **superficie real** de la API, comprobada leyendo `SolicitudControlador`:
    `POST /api/solicitudes` (4 campos; `medicoId` **no** viaja en el cuerpo, lo fija el servidor desde el claim
    `sub` del JWT), `GET /api/solicitudes/mias`, `GET /{id}` (propietario o admin), `GET /{id}/integridad`
    (huella + hash canónico), anti-duplicado RF06.3, RBAC y la semilla de `V1.4.0__cdu06_solicitudes.sql`
    (`SEM-RX-TORAX`, `SEM-RM-RODILLA`). **No hay solicitud sembrada**, así que TC06-03 empieza **creando** la
    primera y `pacienteId` se rellena con `medico` porque la semilla no tiene pacientes.
  - **Desviación nueva detectada contra D30:** el **PR #29** fusionó `develop` → `main` (merge **`d12388b`**,
    19:01) **sin rama `release/RN` y sin etiqueta de release**, dejando `main` por delante de `v0.1.0`.
    Planteado a Ismael: etiquetar `d12388b`, volver a D30 con `release/R1` sobre `cfg-iter04`, o revertir
    `main` a `e165622`. **El agente no toca `main`** y espera indicación.
- Lo que **aún no** se ha hecho de este prompt (bloque siguiente, sigue abierto): la matriz de **clases de
  equivalencia + valores límite + conjetura de errores** de la Iter1 (`iso2-catalogo`) y su composición
  **pairwise**, los JUnit con **JavaDoc** y `@DisplayName` por id de caso, la evidencia versionada en
  `docs/test/iter01/`, regenerar `docs/tc/cdu16-tc-humanos.md` con esos mismos casos y actualizar la wiki de la
  Iter1 **por clase probada**; y el **Docker + MySQL + phpMyAdmin + DAO + `Agente`/`AgenteMySQL`** (ADR **D34**
  al implementarse).
- **Decisiones pedidas a Ismael** antes de montar la persistencia: si **H2** sigue como perfil de test o se
  sustituye por **MySQL en contenedor (Testcontainers)**; si Flyway mantiene **migraciones duales** H2/MySQL o
  solo MySQL; si `gestoroperaciones` tiene **solo DML** y hace falta un usuario DDL aparte para Flyway; driver y
  versión de MySQL y volumen persistente; y si el trabajo va en **rama propia** o dentro de Iter5/CDU12.
- **Siguiente bloque:** aplicar D33 a la Iter1 y luego Iter5 · CDU12 citas (CFG-05) con la capa de persistencia
  MySQL (D34).

### Prompt 15 — Aplicar el método D33 a las pruebas de la Iter1/CDU16 (issue #35, TDOP + pairwise + JUnit) [✔ ejecutado]

> Prompt lanzado por Ismael (09/11/2026, sesión 17, **texto reconstruido** a partir del cuerpo de la issue #35
> y de la sesión): aplicar ya el método **D33** a las pruebas de `iso2-catalogo` (CDU16) de la Iter1, empezando
> por ellas como se pactó. Diseñar **clases de equivalencia**, **puntos del teorema del valor límite** y la
> **conjetura de errores**, componerlas con **pairwise** para no explotar la combinatoria, y usar **ese mismo
> conjunto** de casos tanto en los **JUnit ejecutados con Maven** como en las **plantillas de TC humanos**
> (proyección legible de la matriz, regenerada por herramienta, no a mano). Los ficheros de test llevan
> **JavaDoc** (clase probada, colaborador simulado, propiedad verificada) y `@DisplayName` con el id del caso.
> Evidencia versionada en `docs/test/iter01/`, wiki de la Iter1 con la **clase probada** identificada, y los
> huecos de cotas que aparezcan se arreglan **en el servicio** (D35) editando **antes** los `.puml` (D31).

- Resultado (09/11/2026; rama `feature/iter01-pruebas-d33` desde `develop`, 4 commits, PR **#36**):
  - **Dos herramientas nuevas en `tools/`, sin librerías:** **`pairwise.py`** (componedor pairwise sobre la spec
    con restricciones y generadores de valores) y **`tc_plantillas.py`** (proyecta la matriz terminada a la
    plantilla de TC humanos entre `<!-- TDOP:BEGIN -->` y `<!-- TDOP:END -->`; los TC escritos a mano quedan
    **fuera** de ese bloque y no los toca ninguna regeneración). Las tablas TDOP **solo** salen de estas dos
    herramientas.
  - **Spec primero** (`355ade9`): `docs/test/iter01/tdop-cdu16-espec.json` con las clases de equivalencia, los
    puntos límite y la conjetura de errores de CDU16. Matrices generadas: **A33 + B15 + C36 + D9 + E22 = 115
    casos** en `docs/test/iter01/tdop-cdu16.md` y como fixtures JSON en
    `iso2-catalogo/src/test/resources/tdop/`, que consume la clase de test.
  - **Rojo real:** la primera ejecución dejó **16 fallos sobre los 115 casos**, que destaparon los huecos de
    D35 y **dos discrepancias de spec** (el código de error `CODIGO_DUPLICADO` frente al `CODIGO_EN_USO` que
    esperaba el test de cliente, y el 400 por tamaño de página). Criterio aplicado: **se arregla la spec, no el
    test** — en D33 la matriz es la que dice cuál debe ser el contrato.
  - **Cotas y contrato** (`b2f7356`, D35, con los **`.puml` de iter01 actualizados antes** en `3a17a9a`):
    `LímitesCatalogo` valida en el servicio `codigo`/`nombre`/`categoria` y los textos del protocolo
    (400 en vez de reventar contra la BD), se validan `pagina`/`tamano` (antes 500), se rechaza `categoria`
    fuera de la lista del contrato y se documenta el `415`.
  - **Verde** (`f73ac80`): **115/115 TDOP** y **139 tests en `iso2-catalogo`** (24 heredados + 115 nuevos).
    JaCoCo real medido en `jacoco.csv`: **97,5 % líneas (195/200) y 82,5 % ramas (33/40)**, muy por encima de
    la puerta `jacoco.line.min=0.60`.
  - **PR #36 → `develop` fusionado por Ismael** (merge **`f43721c`**), CI `construir` en verde. La wiki de la
    Iter1 se rehízo con la matriz, la **clase probada** (`Cdu16MatrizTdopTest`) y los 16 fallos del rojo
    (push conjunto `5207bae..7b36c5c`, descrito en el Prompt 16).

### Prompt 16 — TC humanos de CDU16 + cierre de CFG-01 + re-anotación del tag + limpieza de ramas [✔ ejecutado]

> Prompt lanzado por Ismael (09/11/2026, sesión 17, **texto reconstruido**): cerrar por fin la Iter1/CFG-01.
> **Excepción puntual a D28:** ejecutar y rellenar **esta vez los TC humanos de CDU16 el agente** contra el
> servidor local, quedando la autoría y la excepción anotadas honestamente en el doc (los TC siguen siendo
> trabajo de Ismael; él los refrenda y puede repetirlos). Después cerrar todo lo demás: **re-anotar el tag
> `cfg-iter01`** (Ismael elige re-anotar **en el mismo commit `df09bca`**, sin repuntar a `f43721c`), dejar la
> issue **#35 cerrada** con un comentario de registro en vez de reabrirla, **borrar las ramas sobrantes** ya
> fusionadas y sincronizar `STATUS.md` + `PROMPTS.md` para la próxima sesión (D34 + Iter5/CDU12).

- Resultado (09/11/2026):
  - **TC humanos de CDU16 ejecutados y rellenados por el agente vía HTTP** contra `localhost:8080` (commit
    `e70bc50`, PR **#37** → merge **`3601fd6`**, tip de `develop`): **TC16-01…TC16-06 en 16 pasos
    TC16H-01…TC16H-16, todos correctos**, con el executor y la nota de la excepción anotados en el doc;
    **5 asientos nuevos de auditoría encadenada** (baja lógica de `TC16-PBA-01` + 4 reactivaciones) y
    `auditoriaVerificable() = true`. El doc se corrigió además: decía «5 TC humanos» y la plantilla tiene **6**.
    La BD H2 local `./data/iso2-server` se **conserva** con las filas `TC16H-*` y los asientos, para que Ismael
    pueda repetirlos él cuando quiera.
  - **Tag `cfg-iter01` reanotado sobre el mismo commit `df09bca`**: objeto del tag `af95aff` → **`429d67f`**,
    empujado con `--force-with-lease`. El mensaje nuevo fija las puertas técnicas de CFG-01 y añade el bloque
    **«Reanotado el 09/11/2026 (mismo commit, mismo árbol)»**: el rediseño D33 y las cotas D35 viven en
    `develop`, los TC de CDU16 ya están ejecutados, el `.staruml` no procede (D31) y queda el hallazgo del
    TC16-03.
  - **Issue #35:** la había cerrado Ismael a mano el 09/11 a las 00:26; **no se reabre** y se añadió un
    comentario de cierre con la nota D33. **Detectado en el cierre:** la issue **#34** se cerró **a la vez y
    sin trabajo de D34 detrás** (no hay Docker, ni MySQL, ni DAO, ni `AgenteMySQL` en el repo) → hace falta
    reabrirla o abrir una issue nueva (es de Ismael, D29).
  - **Higiene de ramas:** borradas **12 ramas remotas y 12 locales** ya fusionadas (`ci/javadoc-pages`,
    `docs/cierre-iter04-material`, `docs/cierre-sesion-7`, `docs/higiene-config-cfg00-01`,
    `feature/docs-estado-iter0`, `feature/docs-plan-inicial`, `feature/docs-prompt-log`, `feature/iter00-cfg00`,
    `feature/iter01-pruebas-d33`, `feature/iter02-cdu17-seguridad`, `release/R0`, `releaseplan`). La convención
    de la sesión 8 de «conservar las ramas fusionadas» queda **revocada**: los puntos de interés los fijan los
    tags `cfg-iterXX` y `v0.1.0`. La rama local `main` se recreó en `origin/main`; **quedan** `main`, `develop`
    y `feature/iter01-cdu16-catalogo` (no fusionada, su único commit `19dff72` es una nota de la jornada del
    07/11 ya obsoleta: se decide si se borra en la próxima sesión).
  - **Wiki rehecha de la Iter1** (repo `PrISO2-25-26.wiki.git`, `master`, push `5207bae..7b36c5c`): la sección
    **Testing** queda dividida en tres bloques — baseline de `cfg-iter01`; **«Rediseño TDOP (D33) — PR #36»**
    con las matrices A33+B15+C36+D9+E22=115, `Cdu16MatrizTdopTest` como clase probada, los 16 fallos del rojo,
    los dos ajustes de spec y las cotas D35; y **«Testeo humano (D28) — ejecutado 09/11/2026»** caso a caso.
    Cifras contrastadas con `jacoco.csv`: 139 tests en `iso2-catalogo`, **JaCoCo 97,5 % líneas / 82,5 % ramas**.
  - **Docs:** sync de `STATUS.md` (cabecera, estado global, avance cuantificado 156 h/612 h ≈ 25,5 % fusionado
    y 62 h ≈ 10,1 % cerrado del todo, higiene de ramas, deuda nº 3/15/18/19 y punto de continuación reescrito)
    y de este fichero en la rama `docs/sync-sesion-17` → PR a `develop`, **lo fusiona Ismael** (D30).
- **Siguiente bloque:** **D34** (MySQL + Docker + DAO + `AgenteMySQL`, con issue que hace falta reabrir o abrir)
  y **Iter5 · CDU12 citas (CFG-05)**, ambos desde `develop` = `3601fd6`. Quedan además los TC humanos de
  CDU17/CDU13/CDU06 y extender el método D33 a Iter2 e Iter3.


> **⚠ Nota sobre los Prompts 17 a 22 (11/11 - 15/11/2026):** el 15/11 un reinicio del equipo vació el
> almacenamiento local de OpenCode (`%USERPROFILE%\.local\share\opencode\storage`) y se perdió el historial de
> las sesiones 18 a 22, así que **el texto literal de estos prompts ya no existe**. El bloque está
> **reconstruido** con la evidencia del repositorio (`git log`, PRs, issues, tags, `STATUS.md`, `TASADEFECTOS.md`
> y `DECISIONS.md`): el **qué** y el **cómo** están respaldados por commits; la **redacción** del encargo de
> Ismael es aproximada. Queda en la memoria del proyecto la regla que salió de ahí: **nada se queda sin subir**
> y el registro del proyecto son `STATUS.md` + `PROMPTS.md`, no la sesión de la herramienta.

### Prompt 17 — Implementar por fin la D34: MySQL en Docker + Flyway dual + capa DAO/Agente [✔ ejecutado]

> Prompt lanzado por Ismael (11/11/2026, sesión 18, **texto reconstruido**): la D34 llevaba aceptada desde el
> 09/11 sin una línea de código; implementarla ya, con **Docker** y Testcontainers, y en una rama propia para no
> mezclarla con el CDU12. La issue **#34** se reabrió para que el trabajo tuviera ancla.

- Resultado (11/11/2026), rama `feature/iter05-persistencia-mysql` desde `develop` = `3601fd6`:
  - `b15d927` **`docker/`** con **`mysql:8.4` en el host 3307** + **phpMyAdmin en 8081** + volumen
    `iso2-mysql-datos`; base `iso2` con usuario **`gestoroperaciones` solo DML** y `iso2_flyway` con **DDL**
    (usuarios separados); `64e92eb` **Flyway dual** `db/migration/h2/` + `db/migration/mysql/` por perfil, con
    el perfil `mysql` y sus variables de entorno como válvulas de escape; `ae40ad7` **el diseño antes que el
    código** (D31); `bba56f8` capa nueva en `iso2-server-app` con **patrón DAO** y **patrón Agente**
    (`AgenteH2`/`AgenteMySQL`) — JPA sigue siendo el acceso primario del dominio, decidió Ismael — más la
    puerta **Testcontainers contra `mysql:8.4` real** (3 tests).
  - **Medido:** reactor verde con **356 tests, 0 saltados** con JaCoCo y Javadoc estricto.
  - **PR #41 → `develop`, fusionado por Ismael** (merge **`d4749e3`**, 11/11 11:06) y **#34 cerrada a mano**
    (D29). Docs de cierre en `a0eaea4`.

### Prompt 18 — Abrir Iter5 · CDU12 citas (CFG-05) con el flujo diseño-primero [✔ ejecutado]

> Prompt lanzado por Ismael (11/11/2026, sesión 19, **texto reconstruido**): arrancar el CDU12 desde `develop`
> ya con la D34 dentro, empezando por los diagramas, y decidir en el modelo lo delicado (hueco, estado de la
> cita, relación con la solicitud). Cada commit con `Ref: #40`.

- Resultado (11/11/2026), rama `feature/iter05-cdu12-citas`:
  - **Primero el modelo** (`a7b88e8`): los 4 `.puml` + `.png` de `docs/modelo/iter05/` antes de escribir código
    (D31); `0a58892` re-renderiza el PNG de CDU06 que PlantUML recortaba en silencio (de ahí el
    `-DPLANTUML_LIMIT_SIZE=16384` fijo).
  - **Contratos y versiones** (`107e1d4`): `CitaFacade` en **1.1.0** con estado y hueco, estado **`CITADA`** en
    `iso2-solicitudes`, migraciones **`V1.5.0` en las dos carpetas** (h2 y mysql, regla D34 dual), apps a
    **0.1.5-SNAPSHOT** y cota nueva **`jacoco.line.min=0.80`** en `iso2-citas`.
  - **Refino del modelo detectado al programar** (`3aee742`): sin agenda → `FUERA_DE_FRANJA` y solicitud apta
    solo en `creada`.
  - **Implementación** (`99696e5`): dominio JPA `cita`/`cita_hueco`/`cita_auditoria`, `CitaServicio` con **hueco
    anti-concurrencia** y **auditoría encadenada**, máquina de estados, `CitaControlador` + manejador de errores.
  - **Comprobado ejecutando:** Flyway aplica las 5 migraciones con `V1.5.0`, el jar arranca, `POST /api/auth/login`
    devuelve token y `GET /api/citas` responde **401 `AUTENTICACION_REQUERIDA`** (el RBAC de citas aún no estaba
    cableado: siguiente trabajo, no un fallo).
  - **D36 propuesta y registrada** (`e44f7c1`, `7abea47`): cómo se cierra el duelo de concurrencia y qué códigos
    de error salen; **Ismael la dio el visto bueno el 14/11** (`9dad271`).
  - **Dos incidencias del día:** el **issue #40 amaneció cerrado** (10:06 UTC, sin PR enlazada) a mitad de
    iteración → **reabierto con el visto bueno de Ismael** y comentario enlazando la PR #42; y la CI del push
    `e44f7c1` en rojo **solo** por la puerta JaCoCo de `iso2-citas` (`0.00` de 0.80) con el módulo aún sin tests
    → **rojo planificado**, no un fallo.

### Prompt 19 — Medir la tasa de defectos por iteración y convertirla en regla [✔ ejecutado]

> Prompt lanzado por Ismael (11/11/2026, sesión 20, **texto reconstruido**): hasta ahora los fallos del proceso
> estaban repartidos por `STATUS.md` sin poder contarse; abrir un libro de defectos medible, decidir la decisión
> nueva y dejar la regla escrita.

- Resultado (11/11/2026), commit `a276e9c`:
  - **`TASADEFECTOS.md`** en la raíz: libro de **13 defectos** con su evidencia (commit/merge/registro de sesión),
    tabla por iteración con el esfuerzo medido como `git rev-list --count` entre etiquetas (**101 commits = 94 en
    `develop` + 7 en la rama de Iter5**) y recetas de recálculo en PowerShell.
  - **D37** nueva en `DECISIONS.md` (aceptada por Ismael al pedirla): **4 familias** — F1 proceso, F2 entorno,
    F3 contrato y **F4 código, que es la única que mide la valía del modelo** —, regla de exclusión de lo que
    pilla el gate local antes de pushear, y umbrales para pedir un modelo más potente (> 2 F4 por familia de CDU,
    1 decisión de diseño que obligue a reescribir código commiteado, o 1 falso verde en CI).
  - **Regla de registro:** en cada cierre de CFG se actualiza `TASADEFECTOS.md` y el registro de sesión de
    `STATUS.md` lleva la línea con los 6 contadores (`commits · F1 · F2 · F3 · F4 · total (fugados)`), plantilla
    en §7 de ese fichero. **Cifra de partida: 12,9 defectos por 100 commits, de los que solo 2,0 son de código.**

### Prompt 20 — Terminar el código de la Iter5: pruebas del módulo, RBAC, cliente y puerta JaCoCo [✔ ejecutado]

> Prompt lanzado por Ismael (14/11/2026, sesiones 20-21, **texto reconstruido**): rematar CFG-05 según el orden
> que quedó anotado el 11/11 — pruebas del módulo hasta pasar la cota, permisos y contrato HTTP, cliente JavaFX,
> y luego la matriz D33.

- Resultado (11→15/11/2026), commits `4b162cb`→`b912554` sobre la misma rama:
  - **`4b162cb`** suite completa del CDU12: **65 pruebas** (servicio, concurrencia real de dos hilos sobre el
    mismo hueco, máquina de estados, controlador, arquitectura) y **puerta JaCoCo de 0,80 superada**.
  - **`8c4a97c`** RBAC **`cita:leer`/`cita:escribir`** en `Permiso`/`MatrizPermisos`/`ConfigSeguridad` +
    `CitasApiTest` en `iso2-server-app` (matriz de códigos D35 en la puerta HTTP).
  - **`9c081e8`** cliente JavaFX de citas: `ApiClienteCitas`, `VistaGestionCitas`, `VistaConsultaCitas` y sus
    predicates en `NavegacionPorRol`, con `VERSION` a **0.1.5-SNAPSHOT**.
  - **`b912554`** (15/11 04:04) **matriz TDOP de CDU12: 172 filas generadas con `tools/pairwise.py`** → ver
    Prompt 21.
  - **Ismael fusionó el PR #42 el 14/11 a las 22:25 (merge `08a7ed8`)** llevándose los 12 commits de código y
    **cerró #40 a mano** (D29). La matriz, pusheada después del merge, quedó fuera de `develop` y viajó en un
    **PR de cierre aparte** — el mismo patrón de la Iter4 (PR #31 de código + PR #32 de docs).

### Prompt 21 — Cerrar CFG-05: método D33 sobre CDU12, wiki y revalidación D37 [✔ ejecutado]

> Prompt lanzado por Ismael (15/11/2026, sesión 21, **texto reconstruido**): cerrar Iter5 aplicando el mismo
> método TDOP de la Iter1 al CDU12, publicar la wiki y actualizar el libro de defectos.

- Resultado (15/11/2026):
  - **D33 sobre CDU12:** spec de seis tablas en `docs/test/iter05/tdop-cdu12-espec.json` → `python
    tools/pairwise.py` → **172 casos** en los CSVs de `iso2-citas/src/test/resources/tdop/` →
    **`Cdu12MatrizTdopTest` 172/172 en verde** (A38 alta · B31 reprogramación · C29 huecos · D27 listado ·
    E34 estado · F13 conjetura) → evidencia `docs/test/iter05/tdop-cdu12.md` y bloque gestionado proyectado
    **tal cual** a `docs/tc/cdu12-tc-humanos.md` con `tools/tc_plantillas.py`. Antes hicieron falta **dos
    ajustes genéricos** en la herramienta: derivar el paquete `tdop` de la spec y admitir un mapa `operaciones`
    por CDU. **Récord D33: CDU16 115 + CDU12 172 = 287 casos pairwise ejecutados.**
  - **Wiki `Iter5-CDU12-citas.md` publicada** en `master` de la wiki (merge **`0809425`**: página + 4 PNG
    `iter05-cdu12-*` + fila en `Home.md`, D30) nombrando la **clase probada** `Cdu12MatrizTdopTest`.
  - **Revalidación D37:** corte `b912554` → **Iter5 cierra sin DEF nuevos** (no hay DEF-14: el modelo ya fijaba
    `reprogramar` como `POST`, así que no hubo desviación de contrato); el único defecto del tramo es el F2 ya
    fugado **DEF-13**. Serie al corte: **107 commits, 13 defectos, 12,1 por 100, solo 1,9 de código**.
  - **Puerta completa:** `mvn -B -ntp clean verify` con Docker Engine arriba = **BUILD SUCCESS, 637 tests,
    0 saltados**, los 3 Testcontainers de `mysql:8.4` corriendo de verdad.
  - **PR de cierre → PR #43, fusionado por Ismael (merge `cbb1901`, 15/11 08:52)**, que es hoy el **tip de
    `develop`** y lleva `b912554` + `TASADEFECTOS.md` + `STATUS.md`.

### Prompt 22 — Iter6 · CDU19 integración (CFG-06): modelo, módulo spike y pruebas [✔ ejecutado, con recovery]

> Prompt lanzado por Ismael (15/11/2026, sesión 21 → **reanudado en la sesión 22 tras el apagón del equipo**,
> **texto reconstruido**): abrir la última iteración de Elaboración con el **spike de viabilidad** del SSO y de la
> sincronización de historias clínicas sobre sistemas externos, **sin tocar los CDU del dominio** y sin
> persistencia. La issue **#44** es el ancla y lleva el DoD.

- Resultado (15/11/2026), rama `feature/iter06-cdu19-integracion` desde `develop` = `cbb1901`, commits
  `db075c0..f5411c1` con `Ref: #44`:
  - **`db075c0`** modelo PlantUML cerrado **antes del código** (D31): `docs/modelo/iter06/` con casos de uso,
    análisis, clases de diseño y secuencia, `.puml` + `.png`.
  - **`687c624`** contracts: puerto **`InteropFacade` en 1.1.0** con SSO, historia externa e idempotencia y ping
    de sistemas; **el baseline 1.0.0 queda congelado** (lo blindó `ContratoBaselineTest` en Iter4 y sigue verde).
  - **`7a03e0c`** módulo **`iso2-integracion` 1.1.0-SNAPSHOT**: adaptadores **simulados** de SSO, historia clínica
    y RIS/PACS, DTOs (`SesionSsoDTO`, `HistoriaSincronizadaDTO`, `EstadoSistemaDTO`, `EslabonAuditoriaDTO`),
    **auditoría encadenada en memoria** y **sin Flyway** — el spike es deliberadamente sin persistencia, así que
    el slot `V1.6.0__…` queda libre.
  - **`9ca712f`** refino del diseño detectado al escribir el código (regla de `9ca712f` sobre la secuencia) y su
    re-render.
  - **`a22e654`** RBAC **`INTEGRACION_LEER`/`INTEGRACION_ESCRIBIR`** + `IntegracionApiTest` + salto de tren de
    release: apps **0.2.0-SNAPSHOT** y modelo `iso2.version`/`common` en **1.1.0**. Es la iteración que trae el
    hito **`v0.2.0-alpha`** (ALFA-1 / **R1, 20/11**). **[Corregido en la sesión 23: ese par de roles nunca llegó a
    existir; el RBAC real de los 7 endpoints de `/api/integracion` es solo el rol `ADMINISTRADOR`, como ya avisaba
    la propia issue al describir el spike.]**
  - **`f5411c1`** **77 pruebas del CDU19** en 9 clases (`AdaptadorSimuladoTest` 15, `IntegracionServicioTest` 20,
    `IntegracionControladorTest` 13, `SimulacionControladorTest` 8, `ArquitecturaIntegracionTest` 6,
    `AuditoriaIntegracionTest` 6, `SetupSimulacionTest` 4, `RegistroSimulacionTest` 4, `ContratoBaselineTest` 1)
    y arreglo del **`@ConfigurationProperties` que faltaba** en `IntegracionProperties` (el contexto del
    `server-app` moría al arrancar). **Este es el único defecto F4 del tramo de Iter6** y va a
    `TASADEFECTOS.md` en la revalidación D37 del cierre.
  - **Medido sobre `f5411c1`:** `mvn -B -ntp test` en el reactor = **BUILD SUCCESS, 713 tests, 0 fallos,
    0 omitidos**; JaCoCo de `iso2-integracion` **88,2 % de líneas** (cota del módulo `0.70`, puerta de CFG-06
    ≥ 60 %). **[Corregido en la sesión 23: esa cifra estaba caducada, porque `report`/`check` están enlazados a la
    fase `verify` del pom padre y el `jacoco.csv` no se reescribe con `mvn test`; la medida real del módulo es
    96,5 % de líneas y 78,4 % de ramas.]**
  - **Suspendido y reanudado por dos incidencias (15/11):** el reinicio del equipo vació el almacenamiento local
    de OpenCode (perdido el historial, no el trabajo) y la otra sesión trabajó en paralelo sobre el **mismo
    árbol y la misma rama en dos olas**: 11:29-11:49 y 12:37-13:54. No hubo conflicto de `git`; la primera ola
    quedó contenida en `f5411c1` y la segunda dejó sin subir el spec TDOP y sus CSV. La sesión 22 **verificó**
    ambos tramos ejecutando en lugar de reescribir. Tras las 13:54:41 la otra sesión no volvió a escribir nada:
    **queda un solo agente**; la regla práctica es **una rama y un árbol por sesión**.
  - **Hecho por esta sesión 22:** comprobar que Ismael ya había fusionado el PR #43 y cerrado #40, poner el
    **tag anotado `cfg-iter05`** (objeto `005476e`) sobre `cbb1901` siguiendo la receta de
    `docs/mapa-configuraciones.md`, y dejar este sync documental. **Queda la parte b del DoD:** `docs/test/iter06/`
    con la spec TDOP y los CSV de `tools/pairwise.py` + **`Cdu19MatrizTdopTest`** que los lea, proyección a
    `docs/tc/cdu19-tc-humanos.md`, **informe de spike** `docs/informes/spike-cdu19.md`, wiki
    `Iter6-CDU19-integracion` y PR a `develop` (lo fusiona y cierra #44 Ismael, D29); sobre esa fusión, el tag
    **`cfg-iter06`** y el hito **`v0.2.0-alpha`**, que se publica materializando **`release/R1` → `main`** (D30).

### Prompt 22b — Tarde del 15/11: forense de la doble sesión, verificación de la matriz TDOP e integridad [✔ ejecutado]

**Prompt (15/11 13:55):** «ya estoy aqui de nuevo; antes de nada, mira a ver que ha pasado con las dos sesiones
paralelas y si la que estaba en paralelo sigue trabajando». **Prompt (15/11 14:05):** «¿puedes asegurarme que el
estado del proyecto es consistente? Sigue tú a partir de ahora y arregla lo necesario para la integridad… y
respóndeme antes de nada al de los dos agentes en paralelo, uno por iteración, con mi máquina a 59,1 tok/s».

**Hecho:**

- **Forense de sesiones:** con el servicio de OpenCode sin API utilizable (auth borrada otra vez), se consultó la
  base `~\.local\share\opencode\opencode.db` en solo lectura. La sesión paralela (524 mensajes) tuvo **dos olas**
  —11:29-11:49 y 12:37-13:54— y cerró idle a las **13:54:41** con resultado bueno tras su último `pairwise.py`;
  **desde entonces no ha vuelto a tocar el repo** (0 commits nuevos después de `a81b85b`).
- **Verificación del trabajo paralelo antes de commitear:** los CSV de la matriz se regeneraron con
  `tools/pairwise.py` en un directorio temporal → **16 ficheros byte a byte idénticos** a los del árbol; la
  herramienta reporta las coberturas declaradas (A 21/21, C 26/26, D 12/12, F 87/87, B 7, E 16, G 14, 13
  estados) y los estados/tokenes casan con `IntegracionManejadorErrores` y `SetupSimulacion`. Se aceptan
  **tal cual, sin reescribir**.
- **Commit `a81b85b`** `test(tdop - Iter6/CFG-06)`: spec + 8 CSV de `docs/test/iter06/` + 8 espejos en
  `iso2-integracion/src/test/resources/tdop/` (17 ficheros, +935), con la **procedencia** de la otra sesión en
  el cuerpo del mensaje. Subido: remoto == local.
- **Corrección del registro:** `STATUS.md` y este fichero decían «desde las 11:49 hay un solo agente», que era
  **falso** (hubo una segunda ola); quedan corregidos con la cronología real y la deuda nº 21 actualizada.
- **Veredicto sobre dos agentes en paralelo (pregunta de Ismael):** máquina a **59,1 tok/s compartidas** → cada
  agente bajaría a ~30 tok/s; la ganancia real no es 2× sino **~30-45 %** de tiempo real, y en este repo dos
  iteraciones completas chocan de forma garantizada en `Permiso.java`, los `pom.xml`, la cadena de versiones
  Flyway, `iso2-common` (Iter6 congela 1.1.0 / Iter7 pide 1.2.0-SNAPSHOT) y los registros de un solo fichero
  (`STATUS.md`, `TASADEFECTOS.md`). **No recomendado** para iteraciones enteras; sí, con `git worktree` aparte,
  para el trabajo puramente aditivo de Iter7 (diagramas `docs/modelo/iter07/` y spec TDOP `docs/test/iter07/`),
  que no roza ningún punto de conflicto ni `~/.m2`.

### Prompt 23 — Iter6 · CDU19 parte b: matriz TDOP verde, TC humanos, informe de spike, wiki y cierre de sesión [✔ ejecutado]

> Prompt lanzado por Ismael (15/11/2026, tarde, **texto resumido**): continuar **en solitario** y cerrar la parte b
> del DoD de la **#44** — «sigue tú a partir de ahora, y arregla lo que sea necesario para asegurar la integridad
> del proyecto» — dejando **siempre evidencia duradera** (commits y push) porque el árbol de trabajo se pierde.

- **Matriz TDOP ejecutada y en verde** (`c104d78`): **`Cdu19MatrizTdopTest`** con **126 casos** leídos de los CSV de
  `iso2-integracion/src/test/resources/tdop/`, `@ParameterizedTest` + `@SpringBootTest(classes =
  Cdu19MatrizTdopTest.ConfigTdopWeb.class)` y `@AutoConfigureMockMvc` sobre un `@SpringBootConfiguration` interno
  con `@ComponentScan("es.uclm.iso2.integracion")` — **sin starter de seguridad, para que no aparezcan 401/403
  falsos**. Los estados→HTTP se leen del CSV `cdu19-esperados.csv`.
- **Criterio E10 corregido y la matriz crece de 125 a 126**: con segmento vacío, `/api/integracion/sistemas//ping`
  da **404 sin `ApiError`** (lo resuelve Spring, no el controlador), así que **E10 pasa a `RUTA_INEXISTENTE`** y se
  añade **E17** (alias `ESPACIOS`, `"   "` → `SISTEMA_NO_ENCONTRADO`). Spec y CSV **regenerados con
  `tools/pairwise.py`** — los ids son posicionales, por eso E17 sale al final — y recompilado el módulo.
- **Trabajo forense**: el «`mvn test` de 90 casos con 7 fallos» de sesiones previas **nunca existió** (no había
  clase ni CSV en Git), pero los hallazgos sí eran reales y se confirmaron leyendo el código:
  `AdaptadorSimulado.proteger()` evalúa la **política antes que la petición** (503 gana a 400/401/404) e
  `IntegracionServicio.sincronizarHistoriaClinica()` llama a `hce.buscarHistoria()` **antes** que a sus
  `snapshots` → con HCE caído no se sirve la copia local (B04/B05 en 503). Son los hallazgos **H1..H15** del informe.
- **Cobertura corregida y metodología fijada**: con `mvn -B -ntp -pl iso2-integracion verify` sobre `2388b7e`,
  **líneas 361/374 = 96,5 %**, **ramas 134/171 = 78,4 %**, instrucciones 94,7 %, puerta `jacoco.line.min=0.70` en
  verde. **El `jacoco.csv` solo lo escribe la fase `verify`** (en el pom padre `report`/`check` están enlazados a
  `verify`); leerlo tras `mvn test` da cifras caducadas — así es como el **88,2 %** se había quedado clavado en
  `STATUS.md` y en este fichero. Sin cubrir: `IntegracionProperties.SistemaOverride` (0/1) y `HuellaContenido`
  (2/4); ramas débiles: `IntegracionProperties` 11/30.
- **Pruebas del módulo y del reactor**: `iso2-integracion` **203 pruebas en 10 clases, 0 fallos, 0 errores, 0
  omitidos**; reactor completo `mvn -B -ntp verify` sobre `2388b7e` = **BUILD SUCCESS, 839 tests** en 14 módulos
  (13 con tests) y **13 puertas JaCoCo verdes**.
- **TC humanos** (`ec7f228`): `docs/tc/cdu19-tc-humanos.md` proyectado **tal cual** con `tools/tc_plantillas.py`
  (TC19-01…09 + bloque de 126 filas). Parches acotados a la herramienta para no romper las plantillas ya
  ejecutadas de iter01/iter05: `OPERACIONES_EXTRA["iso2-integracion"]`, petición derivada de `metodo`+`ruta`,
  `titulo_de` sin prefijo si no hay `operacion`, `previo` ignora `-`, y **solo las tablas sin `factores`** (B y G)
  proyectan sus columnas como pares.
- **Informe del spike** (`2388b7e`, ficha en `docs/informes/spike-cdu19.md`): veredicto por integración, decisión
  **FHIR STU3 en JSON sobre la REST existente** (la huella SHA-256 canónica ya da la idempotencia), **HL7v2/MLLP
  descartado**, PACS encajado hacia **DICOMweb (`qido-rs`/`wado-rs`)** y recomendación de **no escribir un visor
  DICOM propio en Iter16**; **15 hallazgos**, deudas **D-01..D-07** y riesgos para `v0.2.0-alpha`. Además `86f68ff`
  corrigiendo modos y acciones reales en la documentación de arranque.
- **Corrección al registro**: el RBAC real de `/api/integracion` es **solo rol `ADMINISTRADOR`** (no existen
  `INTEGRACION_LEER/ESCRIBIR`) y los modos reales del spike son `NORMAL`/`LENTO`/`LENTO_TIMEOUT`/`CAIDO`; anotado
  en `STATUS.md` y en la entrada del Prompt 22.
- **Regla de trabajo nueva de esta sesión**: **escribir → verificar en disco → `git add` → commit → push sin
  demora**, y guardar las herramientas de recuento fuera del árbol (`reactor_verify.log`, `reactor_tally.py`,
  `cobertura2.py`, `jacoco_integracion.py` en el temp), porque **solo sobrevive lo commiteado y pusheado**.
- **Wiki publicada (D32)**: página nueva **`Iter6-CDU19-integracion`** en el repo `PrISO2-25-26.wiki` (rama `master`,
  merge **`9b7f513`**, 6 ficheros y +204 líneas): componentes y versiones, los 4 diagramas con sus PNG en
  `images/iter06-cdu19-*`, la tabla **API/RBAC real** de los 7 endpoints (solo `ADMINISTRADOR`), las clases enlazadas
  a Javadoc **o al fuente** mientras el #45 no esté fusionado —las fichas de `iso2-integracion` aún dan 404 en
  `develop`—, y la sección **Testing** con la **clase probada `Cdu19MatrizTdopTest`** (126 casos), los 203 del módulo,
  el **96,5 % de líneas / 78,4 % de ramas** medidos con `verify`, el reactor de 839, las puertas, los hallazgos H1/H2
  y las deudas D-01..D-07. `Home.md` gana la fila de Iter6 y la nota de los README llega hasta `docs/modelo/iter06`.
  Antes de escribir hubo que `git fetch` + `git merge --ff-only origin/master`: el clon del temp iba **5 commits por
  detrás** y el wipe se había llevado los 12 PNG de `images/iter01-*`…`iter03-*`; el `merge` los recuperó (apartado
  **Incidencia 3** de `STATUS.md`). En la misma wiki, **`8aa524b`** quita después de la cabecera el conteo de commits
  («14 commits, tip `a470584`»), un dato que envejece solo y que solo tiene sentido leer en la lista del propio PR.
- **Forense de ramas y hashes fantasma**: `git ls-remote --heads origin` deja solo `develop` (`cbb1901`), `main`
  (`d12388b`), `docs/config-opencode` (`2a2f0a8`) y la rama de la Iter6. **`feature/iter05-cdu12-citas` ya no existe**:
  el remoto la borró el **15/11 a las 08:52** (`DeleteEvent`, al fusionar el PR #43) y en local tampoco está, así que
  el cierre de la Deuda 23 queda hecho y el pendiente pasa a **borrar la rama de la Iter6 tras fusionar el #45**. Y
  **`9457972` no es un commit de este repo** (`git cat-file` lo rechaza, la API de GitHub da 422); los dos commits
  reales del cierre de la Iter5 son `b912554` y `5771a38`, contenidos en `develop`. El dato falso venía de un
  `git branch -r` sin `fetch` previo, y en `STATUS.md` se anota la corrección sin reescribir lo que ya se había dicho.
- **Causa del wipe investigada (Deuda nº 25)**: las **snapshots** de OpenCode capturan el árbol en un repo Git interno
  de su directorio de datos y, al revertir, **eliminan el fichero creado por los pasos revertidos si en la frontera no
  existía**; capturan también ficheros no trackeados no ignorados de hasta 2 MiB. Esto explica que desaparezcan just
  o los ficheros nuevos todavía sin `git add`. Se desactiva con `"snapshots": false` en la config global
  (`~/.config/opencode/opencode.jsonc`) o en el `opencode.json` del proyecto; el almacén actual es
  `%USERPROFILE%\.local\share\opencode\snapshot` (16,6 MB). No se ha tocado la config de Ismael: queda anotado como
  decisión suya.
- **Commits de la sesión, todos subidos**: `c104d78` (matriz verde) → `ec7f228` (evidencia TDOP + TC humanos) →
  `86f68ff` (fix de docs/arranque) → `2388b7e` (informe del spike) → `a470584` (cierre de este registro) →
  `e1d418c` (sync del PR #45) → `6606d81` (wiki publicada, cierra la parte b) → `a2fc906` (forense de ramas) →
  **el docs de la causa del wipe**, que además **pone al día el inventario de la rama: 18 commits sobre
  `cbb1901`**. **PR
  borrador a `develop`: #45** *(91 ficheros, `Ref: #44`, **sin `Closes`**)*. **Queda de Ismael**: ejecutar los TC humanos de
  CDU19 (D28), fusionar el PR y cerrar **#44** (D29), poner el tag `cfg-iter06` sobre la fusión y materializar
  `release/R1` → `main` con `v0.2.0-alpha` (D30).
- **Dos decisiones recogidas de Ismael al cierre**: **no se desactivan las snapshots**, de modo que la Deuda nº 25
  queda abierta, el agente no toca la configuración global de la máquina y el wiping se cubre igual con el orden
  escribir → verificar → `git add` → commit → push; y el **agente B de la Iter7** queda acotado a
  `docs/modelo/iter07/` y `docs/test/iter07/` en el *worktree* `C:\ProgIA\PrISO2-iter07`, sin tocar `src/`, los
  `pom.xml` ni `STATUS.md`/`PROMPTS.md`, y sin abrir PR hasta que el #45 esté fusionado. Tras esto se re-sincroniza el
  cuerpo del **#45** con el tip y se comprueba por API: `draft=True`, `head=c78e6ab`, 18 commits.

### Prompt 24 — Iter6/CFG-06 cerrada por el agente (merge, tag, issue y TC humanos por HTTP) + diseño de conjuntos de prueba y arranque de la Iter7 [✔ ejecutado]

> Prompt lanzado por Ismael (15/11/2026, noche, **texto literal**): «iter06/CFG-06 cerrado. **haz tú lo que
> queda**. A partir de ahora, diseña tú (o el agente B) los TC de lo que haga falta, generando los casos de
> pruebas con clases de equivalencias, conjetura de errores y teoría pesada de los valores límite. Usa *pairwise*
> para diseñar los conjuntos de casos de pruebas. Realiza un informe detallado del diseño de los conjuntos de
> casos de pruebas (tiene su valor pedagógico). Y cuando hayas hecho esto, arranca la Iter7».

- **Decisión nueva que esto supone — D38**: el cierre operativo (fusión del PR de la iteración a `develop`, **tag
  anotado `cfg-iterNN` sobre ese merge**, cierre de la issue CFG-NN y ejecución de los **TC humanos contra el
  servidor real con su evidencia HTTP versionada**) pasa al agente **por encargo expreso de cada iteración**. **No
  cambia la D28 en su fondo** —la validación desde el punto de vista del usuario sigue siendo de Ismael— **ni la
  D30** —el hito `v0.2.0-alpha` (`release/R1` → `main`) sigue siendo de Ismael—, y sigue haciendo falta **CI de la
  rama en verde** para fusionar (D29).
- **Hecho del cierre de la Iter6**: **PR #45 fusionado** (merge **`0ef4f1a`**, CI `construir` verde), **rama
  `feature/iter06-cdu19-integracion` borrada** en remoto y local, **9 TC humanos de CDU19 ejecutados** con el
  servidor real en `http://localhost:8080` (H2 de fichero, login real por `POST /api/auth/login`), **tabla manual
  rellena** y sección **`## Ejecución real de estos TC`** añadida **antes** del bloque `<!-- TDOP:BEGIN -->` de
  `docs/tc/cdu19-tc-humanos.md` (regenerar la proyección sigue siendo un no-op, comprobado).
- **Evidencia HTTP versionada**, carpeta nueva **`docs/tc/evidencia/cdu19/`**: `README.md` + 5 scripts Python
  re-ejecutables (`tc19_http.py`, `tc19_cita.py`, `tc19_sonda_rutas.py`, `tc19_sso_sonda.py`, `tc19_auditoria.py`) +
  5 JSON crudos. Dos lecciones prácticas: `POST /api/integracion/sso/login` usa **`contrasena`** (con `password`
  da 400) y **no hay que sanear el JWT en la capa HTTP**, solo al escribir el JSON (ese bug ya obligó a corregir
  `tc19_auditoria.py` a mitad de sesión). Los JSON llevan cuerpos recortados a 700 caracteres salvo
  `auditoria-cdu19.json`, que se guarda entero a propósito: **39 eslabones** tras tres sincronizaciones y **41**
  tras el rotativo de sistemas, `cadenaIntegra: true`, eslabón #1 `SSO_LOGIN` con génesis de 64 ceros y las ramas
  `OK_REUTILIZADA`/`OK_ACTUALIZADA` de la idempotencia.
- **Deuda nueva del cierre, renumerada sin huecos**: **D-08** `iso2-common` al **0 %** de cobertura (0 de 139
  líneas y 0 de 46 ramas) y sin puerta en su `pom.xml`; **D-09** las rutas sin mapear devuelven **401
  `AUTENTICACION_REQUERIDA` con cuerpo `ApiError`** incluso con token válido; **D-10** una matriz aislada de módulo
  puede esperar **un código distinto del de la aplicación montada** (los TC12-C04/C21/D03 esperan 500 por el
  `TypeMismatchException` de `/api/citas/huecos` y montado da **400 `DATOS_INVALIDOS`**, porque el
  `GlobalExceptionHandler` de `iso2-common` sí tiene consejo y la matriz local solo carga `CitaManejadorErrores`).
  **No se toca código en el cierre** ni se abre fix: las tres son deuda de prueba/contrato.
- **Revalidación D37 escrita en `TASADEFECTOS.md`** (§1-§7): libro hasta **DEF-15**; fila Iter6
  `cfg-iter05..cfg-iter06` = **22 commits, 0 F1, 1 F2, 0 F3, 1 F4, 2 intraiter, 9,1 por 100**; total **129 · 11,6**.
  Las **métricas de apoyo se miden** en vez de repetirse: reactor **839 pruebas / 0 saltados / 0 fallos**, JaCoCo
  de reactor **1.744/2.005 líneas (87,0 %) y 512/675 ramas (75,9 %)** con desglose por módulo (peor en ramas
  **`iso2-common` 0 de 46**), LOC **13.343 en `src/main`** y **10.646 en `src/test`** (ratio **0,80**), CI **83
  ejecuciones = 80 `success` + 3 `failure`** (las 3, la puerta JaCoCo planificada de Iter5) y matriz CDU19 contada
  fila a fila: **126**. **Veredicto: no se pide un modelo más potente**, se tapa el hueco con la prueba de arranque
  con YAML (D-02) y la puerta JaCoCo de `iso2-common` (D-08); la decisión de cambiar de modelo en la Iter7 es de
  Ismael. Corrección de datos encontrada al medir: las puertas que caen sobre la Iter7 son las de **R2 (27/11)**
  — línea **≥ 70 %**, rama **≥ 55 %**, RBAC por endpoint nuevo, trazabilidad **12/20** —; el **≥ 85 % en `iso2-citas`**
  es la puerta de **R5 final**.
- **Registros**: deudas **21/22/23 cerradas**, **24** reescrita con **D-01..D-10**, **25** intacta (no se
  desactivan las *snapshots*), **26** actualizada (el #45 ya está fusionado: el *worktree* del agente B puede
  crearse); apartado **2-ter** nuevo en `STATUS.md` con las condiciones de la Iter7 y los 5 primeros pasos.
- **Materialización del cierre en Git**: **un solo commit de docs** en la rama `docs/iter06-cierre` → PR **#46** a
  `develop` → fusión → **tag anotado `cfg-iter06` sobre ese merge de cierre** (igual que `cfg-iter05` sobre
  `cbb1901`) → **cierra la issue #44** con su DoD y los datos D37 → borra la rama. El conte del tramo queda en
  **22 commits**, que es el número ya escrito en la tabla D37.
- **Lo que se pide para delante en pruebas** (y se estrena en la Iter7): los TC los diseña el agente con **clases
  de equivalencia + conjetura de errores + valores límite + *pairwise***, y hay que escribir un **informe
  detallado del diseño** de esos conjuntos porque tiene valor pedagógico → `docs/informes/diseno-conjuntos-prueba.md`.

### Prompt 25 — Cerrar Iter7/CFG-07: matriz CDU14 a verde, wiki, D37 revalidado, PR y tarjeta de tablero [✔ ejecutado, salvo el tablero]

> Lanzado por Ismael el 18/11/2026 en el equipo nuevo. *(No se conserva el texto literal: el encargo era rematar la
> Iter7 — terminar la matriz TDOP de CDU14 y el cierre material de la iteración: wiki, `TASADEFECTOS.md`,
> `STATUS.md`/`PROMPTS.md`, PR a `develop` y la tarjeta del GitHub Project.)*
>
> *(Nota de numeración: la sesión del 16-17/11 —**fila 25** de `STATUS.md`— quedó **sin entrada de prompt** aquel día.
> Esta entrada es la de la continuación del 18/11, que en el registro de sesiones es la **sesión 26**.)*

- **Relevo verificado antes de tocar nada**: el último commit anunciado el 17/11, **`b4edce0`, sí es ancestro de
  `HEAD`** (`git cat-file -t` + `git merge-base --is-ancestor`), así que los cañones D y E escritos aquel día estaban
  en la rama. Se comprueba también que la matriz tenía **95/253** verdes y que los seis handlers que faltaban eran
  C, F, G y H.
- **Matriz TDOP de CDU14 terminada y verde: 253/253**. Añadidos `operacionDesdeMatriz` (C · 30),
  `reubicacionDesdeMatriz` (D · 27), `secuenciaDesdeMatriz` (E · 12), `asignarDesdeMatriz` (F · 64),
  `auditoriaDesdeMatriz` (G · 12) y `conjeturaDesdeMatriz` (H · 13), más las semillas que hacían falta
  (`huecoDestino` con `SEM-HUECO-DEST:EXACTO|SOLAPE|FANTASMA`, `SEM-TAMPER:ENLAZE|BORRADO|REORDEN` sobre la cadena de `espera_auditoria`,
  `SEMILLA-CITA-OCUPA`) para el hueco que ya tenía una cita detrás.
- **Dos aprendizajes de herramienta, caros en tiempo**: (1) **`-Dtest=Cdu14MatrizTdopTest#tdopTablaH` ejecuta cero
  pruebas en silencio** — los ocho cañones son `@ParameterizedTest` + `@MethodSource` y Surefire no ve ese método, así
  que los «0 fallos» que salían no decían nada; (2) **`jacoco.csv` solo lo escribe `verify`**, y en PowerShell los
  nombres de variable **no distinguen mayúsculas** (`$bc` y `$BC` eran la misma y el acumulado de ramas salió falso).
  Regla practicada: **guardar el log** de cada corrida (`$env:TEMP\verify_iter07_final.log`) y medir sobre los ficheros
  de `target/`, nunca de memoria.
- **Los rojos y su vía D33** (se toca código o spec, nunca CSV ni test): el único defecto real de la matriz fue el
  **415 tragado por el consejo *catch-all*** de `ListaEsperaManejadorErrores` → `medioNoSoportado` con
  **415 `CONTENIDO_NO_ACEPTADO`** (`b38c149`, y `21d9a3c` para alinear el código estable con la convención D35 de
  `CatalogoManejadorErrores:94-97`). El resto era spec mal fijada: **E02/E03/E12** pedían 200 en secuencias que
  terminan en un alta que responde **201**, y **H06** asumía **200 con el primer valor** de
  `?pruebaCodigo=api-prueba&pruebaCodigo=api-otra` cuando **Spring junta los parámetros
  repetidos en una sola cadena** (`api-prueba,api-otra`) y responde **400 `DATOS_INVALIDOS`**. Corregido el JSON y
  **regeerados** los 8 CSV, `cdu14-esperados.csv`, la proyección `docs/test/iter07/tdop-cdu14.md` y el bloque
  `<!-- TDOP:BEGIN -->` de la plantilla de TC humanos.
- **Métricas medidas en el reactor** (`mvn -o -ntp verify`): **1092 pruebas / 0 fallos / 0 saltadas**, JaCoCo
  **2059/2376 líneas (86,7 %) y 621/809 ramas (76,8 %)** — `iso2-citas` **93,5 % / 89,5 %**, `iso2-common` sigue en
  **0/164 y 0/56 sin puerta (D-08)** —, LOC **15.454 líneas en 191 ficheros de `src/main`** y **12.377 en 64 de
  `src/test`** (ratio **0,80**, sin cambios), y **57 ficheros nuevos + 16 modificados** en `cfg-iter06..HEAD`. Ojo con
  el *pairwise*: los **715/715 pares** se cumplen **por tabla** (A 90/90, B 186/186, F 439/439) y **159 filas** de las
  37.770 teóricas; C, D, E, G y H van enumeradas, no emparejadas.
- **Cierre material**: wiki **`Iter7-CDU14-listas-espera`** publicada en `master` del repo wiki (**`3dd871d`**, tras
  `fetch` + `merge --ff-only`, porque el clon iba por detrás), con los 4 PNG de `docs/modelo/iter07` copiados a
  `images/iter07-cdu14-*.png`, la fila nueva en `Home.md` y las fichas Javadoc enlazadas (D32: darán 404 hasta que la
  rama se fusione a `develop`); **comentario con el DoD en la #47**; **PR borrador #48** → `develop` con `Ref: #47` y
  **sin `Closes`**, **CI `construir` en verde** (ejecución 44335613731, 3 m 33 s). El cuerpo del PR se **corrigió y se volvió a leer del remoto**
  con `gh pr view`: fuera la cifra errónea de «**33 ficheros nuevos**» (lo medido en `cfg-iter06..HEAD` es **57 nuevos
  + 16 modificados**) y fuera toda métrica heredada de Iter6; el texto vivo se reprobó contra los números medidos.
- **D37 revalidado** en `TASADEFECTOS.md`: **DEF-16** (F4, el 415), **DEF-17** (F4, **falta de `@Version`** en la cola,
  hallado por TC14-E10 → 409 `CONFLICTO_ENTRADA_NO_ACTIVA` + columna `version` en `V1.6.0`) y **DEF-18** (F3,
  **familia** de expectativas de spec mal fijadas: `sin agenda` 409 `FUERA_DE_FRANJA`, `CREADO` en E02/E03/E12 y H06).
  Fila Iter7 **33 commits · 0 F1 · 0 F2 · 1 F3 · 2 F4 · 3 · 9,1/100**; total **162 · 18 · 13 fugados · 11,1/100**.
  Veredicto de §6: **Iter6 e Iter7 cierran sin F1**, y aun así **no se pide un modelo más potente**; se propone un
  **test de ArchUnit que exija `@Version`** en toda entidad (mitigación mecánica de un olvido repetido), a la espera de
  que Ismael la acepte para numerarla como deuda.
- **No hecho a propósito**: fusionar, taguear, cerrar la #47 y los **TC14-01…09** son de Ismael (D28/D29/D30).
  **Y sin hacer por bloqueo real**: la **tarjeta de la Iter7 en el GitHub Project** — el token de `gh` no tiene el
  alcance `project` y `gh auth refresh -h github.com -s project` dio *context deadline exceeded* (el código de
  dispositivo `FD99-0D44` no llegó a autorizarse). Queda anotado en la Deuda nº 27-h para reintentarlo.

### Prompt 26 - Cerrar CFG-07 por orden de Ismael (merge, tag, issue, ramas) y arrancar la Iter8 · CDU11 [✅ ejecutado]

> Lanzado por Ismael el 22/11/2026 en el equipo nuevo. *(No se conserva el texto literal: la pregunta era
> «¿cómo continuamos el proyecto?» y la respuesta, elegida de un abanico de opciones, fue que **el agente cierre
> CFG-07** —fusionar el #48, taguear, cerrar la #47 y borrar ramas, como con la Iter6 en la sesión 24— **y
> arranque la Iter8**. Los **TC humanos TC14-01…09 se quedan con Ismael** (D28): esa opción no se marcó.)*

- **Sincronización antes de nada**: el disco del equipo nuevo venía detrás; `git pull --ff-only` en
  `feature/iter07-cdu14-listas-espera` trajo 9 commits (`815d5d5` → `8fb4ee1`, el turno del otro equipo) y se
  verificó que todo seguía siendo alcanzable. Lección de relevo repetida: **anexar, no reescribir**.
- **Cierre de CFG-07 ejecutado por el agente** (orden expresa, mismo esquema de la sesión 24): **#48 pasado a
  *ready* y fusionado a `develop` con `--merge` (merge `af8fde6`)**, **cierre documental** en
  `docs/iter07-cierre` → `develop`, **tag anotado `cfg-iter07` sobre la fusión del PR de cierre** (no sobre el
  merge de código: el patrón es `cfg-iter06` = `207a506`, fusión del PR #46), **#47 cerrada con su comentario de
  DoD detrás** (D29) y **ramas borradas** en remoto y local. `main` sin tocar (D30).
- **Reconteo D37 con el tag puesto**: la fila Iter7 del 18/11 decía 33 commits (`cfg-iter06..21d9a3c`) porque
  faltaba el tramo de cierre; medido `git rev-list --count cfg-iter06..cfg-iter07` = **39 commits** (los 33 de la
  rama + el merge `af8fde6` + el commit de docs + su fusión), así que la fila pasa a **39 · 7,7/100** (antes 9,1)
  y el total a **168 · 18 defectos · 13 fugados · 10,7/100**; la F4 sobre 168 queda en **3,0/100**. Iter7 cierra
  con **0 fugados**, como Iter6.
- **Datos del mensaje del tag, todos medidos** (nada heredado de Iter6): matriz **253/253**, reactor
  **1092 pruebas / 0 fallos / 0 saltadas**, JaCoCo **86,7 % líneas / 76,8 % ramas**, `iso2-citas` **93,5 % /
  89,5 %**, wiki `Iter7-CDU14-listas-espera` (`3dd871d`), Flyway **V1.6.0 dual** como último slot y **V1.7.0**
  libre para la Iter8, y CI **92 ejecuciones** (88 verdes, 3 rojas históricas, 1 en curso) contadas con
  `gh run list --limit 100 --json conclusion`.
- **Tablero Kanban, segunda vez bloqueado**: la tarjeta de la Iter7 sigue sin poder moverse; el token no tiene
  alcance `project` y `gh auth refresh -s project` volvió a dar *context deadline exceeded*. Anotado en la deuda;
  no bloquea nada.
- **Arranque de la Iter8 · CDU11 administrativo de pacientes (CFG-08)**: fila 8 del plan (22 h, 23–27/11),
  `iso2-pacientes` + `iso2-integracion` a **1.2.0-SNAPSHOT**, puertas *«unicidad verificada»* y
  *«reconciliación con adaptador probada»*, TC sugeridos: duplicado por NIF/id HIS, admisión caída → alta local
  `pendiente_sincronizar`, baja lógica con confirmación. Issue nueva con alcance y DoD, rama
  `feature/iter08-cdu11-pacientes` desde el tip de `develop` y ciclo **diseño-primero** (D31):
  `docs/modelo/iter08/` primero, spec TDOP en `docs/test/iter08/` después, y el ejecutor de matriz generado de la
  spec JSON, nunca a mano.
