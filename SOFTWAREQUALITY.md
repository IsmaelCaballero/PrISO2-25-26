# Plan de calidad (ISO/IEC 25010) — impacto por componente y por caso de uso

**Proyecto:** Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM
**Versión:** 1.0.0 (07/11/2026) · **Documentos vinculados:** `docs/planificacion-PUD+Calidad.md`,
`docs/componentes-versionado+Calidad.md`, `docs/mapa-configuraciones.md`, `DECISIONS.md` (D24-D27)

---

## 1. Modelo de calidad aplicado

Del modelo ISO/IEC 25010 seleccionamos **5 características** y, dentro de cada una, las
subcaracterísticas que **maximizamos** (D24). El resto quedan cubiertas de forma básica.
Al ser un sistema clínico (RGPD/LOPDGDD), las tres primeras son no negociables; las dos
últimas son las que sostienen el desarrollo con agentes IA sin acumular deuda técnica.

| Característica | Subcaracterísticas maximizadas | Qué significa en este proyecto | Cómo se mide |
|---|---|---|---|
| **Adecuación funcional** | completitud · corrección · pertinencia | cada RF está cubierto **entero** (no a medias), los cálculos de dominio (huecos, recurrencias, colas, estados) son exactos y la función solo aparece donde el rol/actor la necesita | TC humanos por RF (1 feliz + ≥2 error + 1 permisos), cobertura de requisitos 100 %, índice de requisitos con defecto abierto = 0 |
| **Usabilidad** | operabilidad · protección contra errores | el operador clínico hace su tarea con pocos pasos y sin poder provocar un daño por un despiste | revisión heurística por rol en Iter21, TC de validación/confirmación en cliente, tasa de errores de usuario recuperados sin datos corruptos |
| **Fiabilidad** | disponibilidad · tolerancia a fallos · recuperabilidad | la agenda y la cola de exploración no pueden caerse en jornada; si un sistema externo falla, el negocio sigue; los datos se recuperan | TC de concurrencia y de fallo de adaptador, transaccionalidad verificada, prueba de copia/restore en Iter21 |
| **Seguridad** | confidencialidad · integridad · **no repudio** · **responsabilidad** | dato clínico accesible solo por el rol autorizado, no alterable fuera de reglas, y **cada acción queda atribuida a una persona identificada** | TC RBAC por endpoint (deny-by-default), registro de auditoría con hash encadenado, prueba de que toda mutación lleva autor |
| **Mantenibilidad** | **modularidad (MVC + SOLID)** · analizabilidad · testabilidad | el agente IA puede modificar un módulo sin romper los demás, y el humano puede auditar lo generado | reglas ArchUnit de capas y de dependencias, cobertura JaCoCo, complejidad cognitiva (Checkstyle/SpotBugs), Javadoc 100 % en contratos |

**Definición de hecha +calidad** (añadida al *definition of done* de cada iteración):

1. TC diseñados por el humano **antes** de pedir el código al agente (la corrección funcional se diseña, no se deduce del código).
2. `mvn verify` en verde: Surefire 0 fallos, JaCoCo ≥ umbral de la fase, Checkstyle/SpotBugs sin bloqueantes.
3. **ArchUnit**: 0 violaciones de capas MVC ni de dependencias entre módulos (los raíles SOLID).
4. Toda operación que modifica datos lleva `@Auditable` (no repudio + responsabilidad) y tiene TC que lo comprueba.
5. Permisos: TC que demuestra que el rol **no** autorizado recibe 403 (deny-by-default).
6. Cliente JavaFX: TC de controlador para cada campo obligatorio y cada operación destructiva (protección contra errores).
7. Diagramas PlantUML→StarUML del CDU (CU, secuencia, clases de análisis y de diseño) + Javadoc.
8. Incremento de versión SemVer + `CHANGELOG` + tag de configuración `cfg-iterNN` (ver `docs/mapa-configuraciones.md`).

---

## 2. Impacto de las características en cada componente

Cada submódulo Maven es un **ítem de configuración** versionado SemVer; las exigencias de
calidad se garantizan **dentro del módulo** y en su frontera (contratos de `iso2-common`).
Rasgo común de los diez módulos de dominio: **MVC + puertos** (DIP) y reglas SOLID:

```
es.uclm.iso2.<modulo>
├── dominio/          entidades + reglas puras (sin Spring ni JPA)   ← SRP, testabilidad
├── aplicacion/       puertos (interfaces de caso de uso)             ← ISP, DIP, OCP
├── adaptadores/
│   ├── entrega/      @RestController (la «C» de MVC) + DTOs         ← sustitución de presentación
│   └── datos/        repositorios JPA (adaptadores, no el dominio)   ← LSP (fakes en tests)
└── auditoria/        aspect @Auditable (transversal, no se repite)
```

Cliente `iso2-client-app` con MVC propio: `vistas/` (FXML+CSS) · `controladores/` · `modelo/`
(ViewModels). `iso2-server-app` solo ensambla y configura; la lógica nunca vive en la app.

### 2.1 iso2-common (contratos compartidos servidor ↔ cliente)
- **Adec. funcional:** los DTO/enum y puertos son la *única* fuente de la verdad del dominio: si un campo falta aquí, el CDU nace incompleto (completitud funcional asegurada en Iter0/Iter21).
- **Usabilidad:** DTOs pensados para la vista (estados legibles, texto de dominio) para que el cliente no "traduzca" códigos en la UI.
- **Fiabilidad:** validación declarativa (`jakarta.validation`) compartida: el mismo contrato protege en cliente y servidor.
- **Seguridad:** sin secretos ni datos clínicos en DTOs innecesarios (minimización → confidencialidad); tipos inmutables/records → integridad.
- **Mantenibilidad:** es el pegamento de la modularidad: **prohibido** importar clases de un módulo de dominio desde otro (regla ArchUnit). Cambia de versión solo por *minor*.

### 2.2 iso2-catalogo (CDU16)
- **A.F.:** completitud de tipos de prueba, protocolos y políticas; **pertinencia**: todo el sistema pregunta aquí, no vale un catálogo parcial.
- **Usab.:** protección: una prueba en uso no se borra, se desactiva (baja lógica) → la UI nunca ofrece una opción inválida.
- **Fia.:** alta disponibilidad (se cachea en cliente: el catálogo cambia poco).
- **Seg.:** integridad referencial (no dejar solicitudes con prueba inexistente) y responsabilidad: un cambio de catálogo afecta a datos clínicos ya registrados → auditado con autor y motivo.
- **Man.:** es el lenguaje ubicuo: Javadoc completo y contratos congelados en Iter0 lo hacen analizable de arriba abajo.

### 2.3 iso2-seguridad (CDU17 RBAC + CDU18 auditoría)
- **A.F.:** corrección de la matriz rol↔permiso y pertinencia: la función aparece solo para el rol correcto (los 5 roles del enunciado).
- **Usab.:** protección: bloqueo tras intentos fallidos, mensajes genéricos (no revelan si el usuario existe), recuperación de contraseña con caducidad.
- **Fia.:** la autenticación no puede tumbar la aplicación: validación en filtro, sin dependencias lentas en el camino crítico.
- **Seg.:** es el corazón de las cuatro subcaracterísticas: BCrypt (coste ≥10), JWT HS256 firmado con caducidad, deny-by-default, **no repudio** (todo `subject` llega al dominio) y **responsabilidad** (registro inmutable con hash encadenado: borrar un registro rompe la cadena y se detecta).
- **Man.:** la matriz de permisos es **datos**, no `if` repartidos → un test parametrizado cubre los 5 roles; `JwtUtils` con cobertura ≥90 %.

### 2.4 iso2-agendas (CDU13, CDU08 colaborador)
- **A.F.:** completitud (médico/técnico, sala, equipo, festivos) y corrección (sin solapes, capacidad real).
- **Usab.:** rejilla legible, confirmación al borrar un hueco con cita asignada.
- **Fia.:** `@Version` en hueco: dos administrativos no invalidan el mismo hueco; transacción agenda+hueco+auditoría.
- **Seg.:** responsabilidad: crear/editar una agenda es una decisión asistencial y queda atribuida.
- **Man.:** `AgendaGateway` en `common`: `iso2-citas` nunca toca JPA de agendas → cambiar el modelo de agenda no rompe las citas.

### 2.5 iso2-solicitudes (CDU06, 07, 08, 05) — núcleo del flujo clínico
- **A.F.:** completitud de los campos del RF (tipo de prueba, indicación clínica, prioridad, urgencia) y **corrección** de las reglas de prescripción y del motor de recurrencias (el cálculo más propenso a error del proyecto).
- **Usab.:** autocompletado desde catálogo, validación por campo, bloqueo de petición duplicada en la misma sesión (doble clic no duplica una prescripción).
- **Fia.:** generación de recurrencias por lotes **idempotente** (relanzar el lote no duplica citas) + compensación si falla a mitad.
- **Seg.:** integridad (una solicitud no la edita quien no la creó) y no repudio (autor + timestamp + huella del contenido).
- **Man.:** reglas en POJO puros con `Clock` inyectable → TC deterministas; es el módulo con umbral de cobertura más alto (≥85 % de línea).

### 2.6 iso2-citas (CDU12, 14, 01) — crítico para el negocio
- **A.F.:** corrección: **nunca dos citas en el mismo hueco**; pertinencia: el self-service del paciente ofrece solo las opciones que su estado permite (una cita realizada no se cancela).
- **Usab.:** operabilidad (3 clics para confirmar/cambiar), protección (confirmación + motivo obligatorio al cancelar).
- **Fia.:** disponibilidad del auto-servicio 24/7 y tolerancia a fallos: si el hueco se ocupa mientras se edita, se muestra el conflicto con alternativas en vez de perder el trabajo.
- **Seg.:** confidencialidad (el paciente solo ve sus citas), no repudio y responsabilidad (cada cambio de estado, con actor y motivo).
- **Man.:** máquina de estados de la cita como dominio puro, con TC de tabla (estado × acción × rol) → reutilizables en los tres incrementos.

### 2.7 iso2-pacientes (CDU11, CDU03)
- **A.F.:** completitud del alta (NIF, datos clínicos mínimos) y corrección: un paciente = un historial (unicidad por NIF/id HIS), sin duplicados fusionados mal.
- **Usab.:** protección: confirmación en el baja (nunca borrado físico, solo baja lógica) y aviso de paciente ya existente al dar el alta.
- **Fia.:** tolerancia: si el servicio de Admisión (`integracion`) cae, se crea en local con `pendiente_sincronizar` y se reconcilia (recuperabilidad).
- **Seg.:** confidencialidad (aislamiento por centro/rol) e integridad (unicidad); toda consulta de historial es un acceso auditado.
- **Man.:** criterios de búsqueda expresados como predicados componibles → analizables y testables.

### 2.8 iso2-comunicacion (CDU02, CDU04)
- **A.F.:** pertinencia: no notificar lo que no concierne ni duplicar recordatorios; completitud del formulario de incidencia.
- **Usab.:** operabilidad (centro de notificaciones con histórico), protección contra el *spam* y contra el auto-borrado de mensajes.
- **Fia.:** tolerancia y recuperabilidad: el fallo del canal (correo/SMS simulado) no rompe la operación de negocio → cola persistente, reintento con *backoff* y reenvío idempotente.
- **Seg.:** confidencialidad por **minimización** (el aviso no contiene datos clínicos, solo "tiene un resultado disponible"), no repudio (acuse de envío/lectura).
- **Man.:** `NotificationGateway` en `common`: en tests se cambia por un *fake* en memoria → todos los TC son rápidos y deterministas.

### 2.9 iso2-informes (CDU09)
- **A.F.:** completitud del informe (hallazgos, impresión, recomendación) y corrección de la correlación estudio↔imagen (*accession number*, no otra).
- **Usab.:** operabilidad del visor (zoom/ventana-nivel, precarga de la serie habitual) y protección (el visor no permite "guardar" cambios sobre una imagen original → integridad del estudio).
- **Fia.:** tolerancia: si el PACS no responde, modo degradado (informe disponible, imagen reintentable) y caché local de series ya vistas.
- **Seg.:** confidencialidad (si el rol no tiene acceso a imagen, se muestra el estudio sin píxeles), integridad (*checksum* de ficheros DICOM), no repudio (ver una imagen también es un acceso que se registra).
- **Man.:** el código DICOM queda **dentro de la fachada de `iso2-integracion`**; `iso2-informes` solo ve un puerto → se prueba con un stub local sin PACS real.

### 2.10 iso2-integracion (CDU19, colaboradores Admisión/DICOM)
- **A.F.:** completitud del mapeo de campos de SSO/HCE/Admisión/RIS-PACS (HL7/FHIR/DICOM) y corrección contrastada con TC de ida y vuelta.
- **Fia.:** el componente donde más se explota la tolerancia a fallos: *timeout*, reintento con *backoff*, idempotencia (clave de mensaje), cola de reproceso y **modo degradado**: si el sistema externo cae, el resto del sistema sigue funcionando.
- **Seg.:** integridad (HMAC/TLS en los mensajes simulados), confidencialidad (secretos fuera del repositorio, en `application-local.yml` ignorado).
- **Man.:** regla de arquitectura: **ninguna** clase de dominio importa librerías DICOM/HL7 → verificada con ArchUnit; el *spike* de Iter6 se congela como adaptador, no como código repartido.

### 2.11 iso2-reporting (CDU10, CDU15, CDU20)
- **A.F.:** corrección de las fórmulas (ocupación, espera media, cancelaciones) documentadas en el Javadoc de cada métrica; completitud de los periodos pedidos en los RF.
- **Usab.:** exportación (CSV) y lectura de tendencias sin tener que interpretar tablas crudas.
- **Fia.:** las agregaciones no bloquean la escritura (consultas de solo lectura en transacción separada).
- **Seg.:** confidencialidad por diseño: **solo agregados/anónimos** (umbral mínimo de muestra), sin datos identificables en los reportes.
- **Man.:** solo lee a través de puertos de lectura; nunca escribe → imposible que un reporte corrompa el clínica; TC con datos semilla.

### 2.12 iso2-server-app y iso2-client-app (aplicaciones, tren de release)
- **A.F.:** servidor = hilo de integración (que el CDU funcione *de verdad* end-to-end, no solo unit-test); cliente = la tajada de interfaz del CDU según rol.
- **Usab.:** (cliente, es su característica dominante) vistas por rol, navegación ≤3 niveles, mensajes de error en lenguaje de dominio con causa + acción, atajos de teclado para las tareas repetitivas, orden de tabulación coherente.
- **Fia.:** el cliente maneja el fallo de red sin perder los datos tecleados (reintento y borrador local); el servidor arranca con H2 en fichero y copia de seguridad programable.
- **Seg.:** el cliente no guarda el token en claro ni datos clínicos en disco; el servidor centraliza la autenticación (el cliente no decide permisos → el servidor nunca confía en el cliente).
- **Man.:** `server-app` sin lógica de negocio; `client-app` con MVC estricto y sin llamadas HTTP directas desde las vistas (pasan por un `ApiClient` por módulo).

---

## 3. Cómo afecta el plan de calidad a cada caso de uso

Columna "exigencia verificable" = requisito de calidad que entra en el *issue* del CDU y se
verifica con los TC que diseña el estudiante. Columna "verificación" = medio de prueba.

| CDU (RF) · iter · componente | Subcaracterísticas críticas | Exigencia de calidad verificable | Verificación |
|---|---|---|---|
| **CDU01** (RF01) · I9 · citas | AF pertinencia · USA protección · SEG no repudio · FIA disponibilidad | El paciente solo actúa sobre sus citas y solo en estados válidas; cancelar exige confirmación y motivo; toda acción se atribuye | TC tabla estado×acción×rol; TC "cita ajena" → 403; TC doble submit; auditoría con actor |
| **CDU02** (RF02) · I10 · comunicacion | FIA tolerancia · AF pertinencia · SEG confidencialidad · USA protección | Ningún fallo de canal rompe la operación de negocio; el mensaje no lleva datos clínicos; sin recordatorios duplicados | TC adaptador caído → encola y reintenta; TC dedupe 24 h; TC contenido sin PII |
| **CDU03** (RF03) · I13 · pacientes | AF completitud · SEG confidencialidad/integridad · MAN analizabilidad | Historial completo y sin duplicados; solo lectura; el acceso queda registrado; criterios de búsqueda legibles | TC con datos semilla multiestudio; TC acceso auditado; TC predicados de búsqueda |
| **CDU04** (RF04) · I18 · comunicacion | AF completitud · USA protección · SEG integridad | Formulario completo y no editable tras el envío; la encuesta solo a pacientes con prueba cerrada | TC validación de campos; TC edición posterior → 409; TC pertinencia de encuesta |
| **CDU05** (RF05) · I14 · solicitudes | AF corrección (recurrencias) · USA operabilidad · MAN testabilidad | Las revisiones periódicas coinciden con las calculadas por el motor de CDU07 (misma regla, sin desviación) | TC compartidos con el motor de recurrencias con `Clock` fijo |
| **CDU06** (RF06) · I4 · solicitudes | AF las tres · USA protección · SEG no repudio/integridad | Todos los campos del RF obligatorios validados; prioridad/urgencia válidas; imposible duplicar una prescripción; autor + huella del contenido | TC por campo; TC duplicado; TC huella SHA-256 del contenido; cobertura ≥85 % |
| **CDU07** (RF07) · I11 · solicitudes | FIA tolerancia/recuperabilidad · AF corrección · MAN testabilidad | El lote de recurrencias es **idempotente**: relanzarlo no duplica citas; si falla a mitad, se compensa | TC relanzamiento ×2; TC interrupción del lote; TC festivos/fin de serie |
| **CDU08** (RF08) · I12 · solicitudes+agendas | AF completitud de estados · USA operabilidad · SEG confidencialidad | Estados reales del ciclo de vida, nunca cacheados; el médico solo ve sus solicitudes; búsqueda rápida | TC ciclo de vida completo; TC aislamiento por profesional; slice `@WebMvcTest` |
| **CDU09** (RF09) · I16 · informes+integracion | FIA tolerancia · SEG confidencialidad/integridad · AF pertinencia | PACS caído ⇒ modo degradado con aviso y reintento; *checksum* DICOM; ver una imagen queda auditado | TC stub DICOM con fallo; TC checksum; TC rol sin acceso a imagen |
| **CDU10** (RF10) · I17 · reporting | AF corrección · SEG confidencialidad (agregados) · MAN modularidad | Las cifras cuadran con los datos; el reporte no expone datos identificables; el módulo solo lee | TC totales con semilla; ArchUnit: `reporting` sin escrituras ni PII |
| **CDU11** (RF11) · I8 · pacientes+integracion | AF corrección (unicidad) · FIA tolerancia · SEG responsabilidad | Un paciente unívoco (NIF/id HIS); si Admisión cae, alta local y reconciliación; el alta/baja se atribuye | TC duplicado por NIF; TC Admisión caído → `pendiente_sincronizar`; TC baja lógica |
| **CDU12** (RF12) · I5 · citas | AF corrección · FIA tolerancia/recuperabilidad · SEG integridad | **Imposible** reservar el mismo hueco dos veces (concurrencia real); cita+hueco+auditoría en una sola transacción | TC 2 hilos mismo hueco → 1 éxito; TC rollback; `@Version` en hueco |
| **CDU13** (RF13) · I3 · agendas | AF completitud/corrección · SEG responsabilidad · MAN modularidad | Agendas multi-recurso sin solapes ni capacidad ficticia; su creación queda atribuida; `citas` no depende de sus tablas | TC solapes/festivos; test parametrizado de capacidad; ArchUnit de frontera |
| **CDU14** (RF14) · I7 · citas | AF corrección · USA operabilidad/protección · SEG no repudio | Reordenación **estable y determinista** por prioridad/urgencia/fecha; cada reubicación con autor y motivo; sin reubicaciones dobles por doble clic | TC comparador con tabla de casos; TC doble clic; TC traza de reubicación |
| **CDU15** (RF15) · I19 · reporting | AF completitud/corrección · SEG confidencialidad · USA operabilidad | Ocupación/esperas/cancelaciones con fórmula documentada y agregados anónimos; exportación | TC métricas con semilla; ArchUnit sin PII en proyecciones |
| **CDU16** (RF16) · I1 · catalogo | AF completitud/pertinencia · SEG integridad/responsabilidad · MAN analizabilidad | Catálogo completo y único (código); una prueba en uso no se borra; todo cambio auditado porque sostiene datos clínicos | TC unicidad de código; TC borrar en uso → prohibido; TC auditoría de cambios |
| **CDU17** (RF17) · I2 · seguridad | SEG las cuatro · AF corrección · USA protección | Deny-by-default en todos los endpoints; BCrypt≥10 y JWT firmado y caducable; bloqueo por intentos; la matriz rol↔permiso es un test | TC 403 por rol no autorizado (uno por endpoint); TC token caducado; test parametrizado de matriz RBAC |
| **CDU18** (RF18) · I15 · seguridad | SEG no repudio/integridad/responsabilidad · MAN analizabilidad | Registro inmutable (sin UPDATE/DELETE) con hash encadenado; consultable por usuario/entidad/periodo; el visor es solo-admin | TC alteración rompe la cadena; TC solo-admin; ArchUnit: toda mutación lleva `@Auditable` |
| **CDU19** (RF19) · I6 · integracion | FIA las tres · MAN modularidad · AF completitud del mapeo | Timeout+reintento+idempotencia+cola de reproceso; si el externo cae el negocio sigue; ningún adaptador DICOM/HL7 se filtra al dominio | TC externo lento/caído; TC mensaje duplicado; ArchUnit de fachada; TC ida y vuelta de mapeo |
| **CDU20** (RF20) · I20 · reporting+seguridad | AF completitud · SEG confidencialidad/responsabilidad | Estadísticas globales cuadran con los totales; sin datos personales; el reporte lleva pie de trazabilidad (quién y cuándo) | TC totales; ArchUnit sin PII; TC pie de reporte |

**Regla transversal:** todo CDU, además de su propia exigencia, arrastra las obligaciones de
`iso2-seguridad` (autenticación + permiso + auditoría) y de `iso2-client-app` (validación y
confirmación en la vista). Esas obligaciones no se rediseñan en cada CDU: son plantillas del
`issue` (sección de *quality gates* en `docs/planificacion-PUD+Calidad.md`).

---

## 4. Métricas y puertas de calidad por release

| Puerta (gate) | R0 (Iter0) | R1 α (20/11) | R2 α (27/11) | R3 β (04/12) | R4 RC (11/12) | R5 final (15/12) |
|---|---|---|---|---|---|---|
| Cobertura de línea JaCoCo | — | ≥60 % | ≥70 % | ≥75 % | ≥78 % | **≥80 %** (≥85 % en citas/solicitudes/seguridad) |
| Cobertura de rama | — | ≥45 % | ≥55 % | ≥65 % | ≥70 % | ≥70 % |
| Surefire (fallos) | 0 | 0 | 0 | 0 | 0 | 0 |
| ArchUnit (capas/dependencias) | 0 violaciones | 0 | 0 | 0 | 0 | 0 |
| TC RBAC por endpoint nuevo | — | 100 % | 100 % | 100 % | 100 % | 100 % |
| TC de fallo de adaptador externo | — | — | — | obligatorio | obligatorio | obligatorio |
| Checkstyle/SpotBugs bloqueantes | 0 | 0 | 0 | 0 | 0 | 0 |
| Javadoc en contratos públicos | 100 % | 100 % | 100 % | 100 % | 100 % | 100 % |
| Trazabilidad RF→CU→TC | — | 6/20 | 12/20 | 18/20 | 20/20 | 20/20 |
| Prueba de copia/restore H2 | — | — | — | 1 ensayo | 1 ensayo | **ensayo documentado** |
| Revisión de usabilidad por rol (heurística) | — | 1 rol | 3 roles | 5 roles | 5 roles | **5 roles + manual** |

**Roles de calidad:** cada pareja responde de la calidad de sus CDU; una persona por release hace
de *guardián de calidad* (ejecuta las puertas, revisa que ningún PR se fusione sin green CI y
publica el informe de métricas en `STATUS.md`). Ningún miembro se auto-fusiona su PR.

**Herramientas:** JUnit 5 + AssertJ + Mockito · JaCoCo + Surefire (informes en `target/site`) ·
Checkstyle + SpotBugs + **ArchUnit** (raíles MVC/SOLID) · OWASP dependency-check en Iter21 ·
Pruebas de sistema en Iter21 con las dos aplicaciones reales (H2 en fichero + copia/restore).
