# Spike de integración CDU19 (CFG-06, Iter6) — informe y decisión técnica

**Referencia:** issue #44 (CFG-06 / CDU19, spike de las integraciones críticas).
**Rama:** `feature/iter06-cdu19-integracion`. **Fecha:** 2026-11-15.
**Alcance:** módulo `iso2-integracion` (1.1.0-SNAPSHOT) + fachada en `iso2-common` 1.1.0-SNAPSHOT.
**Ejecuta:** agente de desarrollo; TC humanos pendientes de Ismael (21/11).

## 1. Qué se construyó

Un módulo de hexagonales con cuatro puertos y cuatro adaptadores **simulados**, de modo que
mañana el adaptador real se enchufa sin tocar el servicio:

| Pieza | Fichero | Nota |
|---|---|---|
| Fachada compartida | `iso2-common/.../integracion/InteropFacade.java` | 6 métodos + los DTO de transporte anidados |
| Puertos | `iso2-integracion/.../puerto/{Sso,Hce,Ris,SistemaImagen}Port.java` | `PacsPort`/`RisPort` comparten `SistemaImagenPort` (ping + estado) |
| Adaptadores simulados | `.../adaptador/{Sso,Hce,Ris,Pacs}AdaptadorSimulado.java` | todos hijos de `AdaptadorSimulado` (politica + latencia + reintentos) |
| Servicio | `.../servicio/IntegracionServicio.java` | idempotencia por huella SHA-256 + ping degradado |
| Dominio | `.../dominio/*` | `ModoSimulacion`, `PoliticaSimulacion`, `RegistroSimulacion`, `AuditoriaIntegracion`, `EslabonAuditoria`, `HuellaContenido` |
| Web | `.../web/{IntegracionControlador,SimulacionControlador,IntegracionManejadorErrores}.java` | solo `ADMINISTRADOR` |
| Arranque | `.../configuracion/{IntegracionConfiguracion,IntegracionProperties}.java` | prefijo `iso2.integracion` |
| Puerta de arquitectura | `src/test/.../ArquitecturaIntegracionTest.java` | 6 reglas ArchUnit |

Endpoints publicados (todos bajo `/api/integracion`, rol `ADMINISTRADOR`):

| Método | Ruta | Uso |
|---|---|---|
| POST | `/sso/login` | login delegado contra el IdP simulado |
| GET | `/sistemas` | listado de sistemas |
| GET | `/sistemas/{sistema}/ping` | monitorización **degradada** (nunca 5xx) |
| POST | `/historias/{id}/sincronizar` | sincronización de la historia externa |
| GET | `/estudios/{accessionNumber}` | importación del estudio del RIS |
| GET | `/auditoria` | cadena de auditoría + `cadenaIntegra` |
| GET/POST | `/simulacion` | leer / instalar politicas de fallo **en caliente** |

No hay Flyway nuevo: el spike **no persiste**, todo es memoria, tal y como fijaba la issue.

## 2. Decisión adoptada: FHIR en JSON sobre REST, no HL7v2

**Decisión:** la integración con la HCE y el RIS se modela sobre **recursos FHIR en JSON
(STU3, perfil mínimo) transportados por la REST ya existente**, con la huella SHA-256 de la
forma canónica del recurso como clave de idempotencia. **Se descarta HL7v2 (MLLP)** para este
producto y **se aplaza DICOM** al visor de Iter16.

**Por qué FHIR**

1. El canal ya existe: los adaptadores hablan con Spring MVC y Jackson. Un recurso FHIR es JSON
   anidado, y los DTO actuales (`RegistroHistoriaDTO` con `codigo`/`fecha`/`detalle`,
   `EstudioDTO` con `accessionNumber`/`pacienteId`/`fechaEstudio`/`numeroSeries`) se leen hoy
   como `Observation`/`ServiceRequest` simplificados sin añadir dependencias. HL7v2 exige MLLP
   (socket largo, *keepalive*, ACK) y un parser de segmentos `|^~\&` que no aporta nada al caso
   de uso del CDU19: consultar una historia y un estudio, no intercambiar altas/urgencias.
2. Trazabilidad y depuración: un recurso JSON entra en el log y en la auditoría tal cual; un
   segmento HL7v2 exige volcarlo para poder leerlo.
3. Idempotencia: sobre un recurso canónico la huella SHA-256 es determinista (TC19-B01 y
   TC19-A01, `reutilizado=true`). Sobre un mensaje HL7v2 el `MessageControlId` cambia en cada
   envio y hay que normalizar el mensaje antes de poder comparar, que es exactamente el trabajo
   que FHIR ya hace con el `id` lógico + `meta`.
4. El RIS se modela con `Accession`/`Worklist`: el identificador de acceso (`ACC-0001` del
   adaptador) es literalmente el `accessionNumber` de FHIR `ServiceRequest`.

**Riesgos asumidos y su mitigación**

| Riesgo | Mitigación |
|---|---|
| El servidor FHIR real no expone el perfil mínimo que asumimos | el puerto `HcePort` es la costura: el mapeo recurso → DTO vive en el adaptador |
| FHIR es ancho (140+ recursos) y puede arrastrar dependencias (HAPI FHIR) | **no** se añade HAPI: se usan DTO propios en `iso2-common`. Si el adaptador real lo necesita, la dependencia entra en `iso2-integracion` y `iso2-common` sigue siendo contratos puros, que es lo que vigila la regla ArchUnit 6 (`es.uclm.iso2.common..` no puede mirar a `es.uclm.iso2.integracion..`) |
| HL7v2 puede volver por Admisión (CDU14/CDU20) | la fachada es la misma (`InteropFacade`); un adaptador HL7v2 futuro comparte puertos y auditoría |

**Visor DICOM para Iter16 (CDU17, recomendación para Ismael):** no escribir un visor DICOM
propio. El spike deja el PACS monitorizable (`PacsAdaptadorSimulado.ping()`, 200 con
`disponible=false` cuando está caído) y recomienda **DICOMweb** como contrato: `qido-rs` para
buscar el estudio y `wado-rs` para bajar los frames, y un visor de código abierto empaquetado en
el cliente (la lista comparativa va en la wiki `Integracion CDU19`). El motivo de calidad:
`iso2-informes` exige "integridad y confidencialidad de imagen DICOM" con umbral ≥75 %
(`docs/componentes-versionado+Calidad.md`), y un visor propio consume el iterativo entero.

## 3. Viabilidad de cada integracion

| Integración | Veredicto | Evidencia |
|---|---|---|
| SSO delegado (IdP) | **Viable con una laguna**: el login delegado funciona y audita, pero la sesión SSO no se canjea por el JWT de CDU17 ni el rol externo se mapea a `Permiso` | TC19-01, TC19-C01..C15 |
| HCE / sincronización | **Viable**: feliz, caído, lento/timeout, idempotencia por huella | TC19-A01..A21, TC19-B01..B07 |
| RIS / importar estudio | **Viable**: feliz + degradado | TC19-D01..D12, TC19-03 |
| PACS | **Solo monitorización**: no hay importación de estudio ni eslabón `PACS_IMPORTAR`; caído, su ping sigue 200 con `disponible=false` y no arrastra a los demás | TC19-E05, TC19-E14 |
| Modo degradado (resto de la app) | **Viabilidad demostrada a mano, sin automatizar**: RIS/PACS caídos no tocan login/catálogo/agenda/cita | TC19-04 (manual); deuda D-05 |

## 4. Evidencia de la parte b

Medido el 2026-11-15 sobre `86f68ff` con `mvn -B -ntp -pl iso2-integracion verify` (el informe de
JaCoCo solo lo escribe la fase `verify`: leer el CSV tras un `mvn test` da cifras **caducadas**).

| Magnitud | Valor |
|---|---|
| Tests del módulo | **203**, 0 fallos, 0 errores, 0 omitidos (10 clases de test) |
| Matriz TDOP `Cdu19MatrizTdopTest` | **126 casos, 0 fallos** (A21 + B7 + C15 + D12 + E17 + F40 + G14) |
| Cobertura JaCoCo del módulo | línea **96,5 %** (361/374), rama **78,4 %** (134/171), instrucción 94,7 % → DoD ≥70 % **cumplido** |
| Puerta JaCoCo del módulo | `jacoco.line.min = 0.70` en `iso2-integracion/pom.xml`: *"All coverage checks have been met"* |
| Reglas ArchUnit | 6, en verde |
| TC humanos | 9 (TC19-01..TC19-09) + bloque TDOP de 126 en `docs/tc/cdu19-tc-humanos.md`, pendientes de ejecutar |
| Espec TDOP | `docs/test/iter06/tdop-cdu19-espec.json` + 7 CSV (par generados por `tools/pairwise.py`) |
| Reactor completo | se revalida con la subida del PR (ver `STATUS.md`) |

**Lo que queda sin cubrir** (registrado para el ciclo de defectos): el record
`IntegracionProperties.SistemaOverride` (0/1 línea) y `HuellaContenido` (2/4). La línea está casi
limpia; **la asignatura pendiente son las ramas**: `IntegracionProperties` tiene **11/30 ramas** y
`AdaptadorSimulado` **19/20**, de modo que los *overrides* por sistema y la configuración corrupta
no están probados (ver H5).

## 5. Hallazgos del spike

Etiqueta `[producto]` = hay que arreglar código; `[contrato]` = la docs/contracto se alineó a lo
que hace el código; `[deuda]` = queda tarea para Iter7.

| # | Hallazgo | Evidencia | Severidad |
|---|---|---|---|
| H1 | **Precedencia 503 > 400/404.** `AdaptadorSimulado.proteger()` aplica la política **antes** de mirar la petición: con HCE caída, un paciente inexistente o en blanco responde 503 `SISTEMA_NO_DISPONIBLE` en vez de 404/400. El contrato lo acepta (503 > 401 > 400), pero el cliente no puede distinguir "el servicio no está" de "el paciente no existe" | TC19-A11, A17, A20; TC19-B04, B05 | mayor |
| H2 | **Sin copia local cuando el externo cae.** `IntegracionServicio.sincronizarHistoriaClinica()` llama a `hce.buscarHistoria()` (línea 123) antes de consultar `snapshots`, así que la instantánea ya guardada no se sirve con HCE caída: B04/B05 dan 503 aunque existe copia con la misma huella. El "modo degradado" de la issue queda a medias | TC19-B04, TC19-B05 | mayor |
| H3 | **`LENTO` no comprueba el plazo.** `PoliticaSimulacion.excedeTiempoEspera()` solo salta en `LENTO_TIMEOUT`: con `LENTO` y latencia 250 sobre plazo 100 la llamada responde 200 tras el retardo. Es el mecanismo de la prueba de rendimiento, pero leer "latencia > timeout → 503" es mentira | TC19-05 (docs), A03 | medio |
| H4 | **El timeout se pinta como 503, no 408.** `SistemaExternoException.CAIDO` para todos los fallos del externo, incluido `TIEMPO_SUPERADO` | TC19-A03, TC19-05 | medio |
| H5 | **`IntegracionProperties` falla el arranque con un modo desconocido** (`IllegalStateException` desde `politicaDe`). El DoD pedía "arranque con valores por defecto y warning" para configuración corrupta. Además solo se pisan **11 de sus 30 ramas**: los *overrides* por sistema (`iso2.integracion.sistemas.<codigo>.modo`) y la config corrupta no tienen ningún TC | JaCoCo, TC ausentes | mayor `[deuda]` |
| H6 | **Sin `@RequestBody` en `sincronizar`.** `POST /historias/{id}/sincronizar` no tiene cuerpo, así que un `Content-Type: text/plain` pasa y sincroniza de verdad; no hay forma de devolver 415 en ese endpoint | TC19-G03 | medio |
| H7 | **Jackson coerciona tipos.** Un `usuario` numérico (`{"usuario":123}`) se convierte en `"123"` y el resultado es 401 `CREDENCIALES_INVALIDAS`, no 400 | TC19-G07 | menor |
| H8 | **Ruta fantasma.** El borrador de contrato del agente de docs prometía `PATCH /api/integracion/simulacion/reintentos`; no existe (404 `HTTP_404_NOT_FOUND`). No se implementó: la política completa ya se cambia con `POST /simulacion` | TC19-G11 | menor |
| H9 | **404 sin cuerpo `ApiError`** en `GET /sistemas//ping` (segmento vacío): la ruta no la atiende ningún patrón y Spring responde con su error por defecto. Se registró como hallazgo en la matriz | TC19-E10 | menor |
| H10 | **La sesión SSO es decorativa.** `SsoAdaptadorSimulado` fija `idp-admin`/`idp-pass` y el rol externo `radiologia`, con caducidad de una hora; no hay refresh y **no se canjea por el JWT de CDU17**, así que el login delegado no autentica nada en el resto de la aplicación | TC19-C01, TC19-01 | mayor `[deuda]` |
| H11 | **Auditoría en memoria con hash-chain**, mientras citas/agendas/catálogo usan entidades persistentes (`AuditoriaCitas`, `AuditoriaAgendas`, `AuditoriaCatalogo`). En un reinicio la cadena se pierde, y `cadenaIntegra()` no prueba nada frente a un atacante con reinicio | `AuditoriaIntegracion` | medio `[deuda]` |
| H12 | **No existe `@Auditable`** en `iso2-common`, aunque la issue lo citaba como contrato afectado. La auditoría de integración se hace explícita en el servicio (`auditoria.registrar(...)`) | grep global | menor |
| H13 | **Reintentos simulados, no reales.** `AdaptadorSimulado.proteger()` duerme `1 + reintentos` veces, pero no reintenta una llamada real ni hay cliente HTTP ni backoff; `timeoutMs` tampoco corta nada. Son knobs de prueba, no tolerancia a fallos | TC19-A03, TC19-05 | medio `[deuda]` |
| H14 | **PACS sin importar.** No hay operación de importación de imagen ni eslabón `PACS_IMPORTAR` auditado; PACS solo responde ping y estado | `SistemaImagenPort` | medio `[deuda]` |
| H15 | **El RBAC es `ADMINISTRADOR` a secas.** La issue planteaba permisos de lectura/escritura de integración; solo existe el rol global | TC19-08 | menor `[deuda]` |

### Correcciones de docs ya hechas en este ciclo (`86f68ff`)

- `application.yml` listaba el modo **`LENTO_SIN_TIMEOUT`**, que no existe en `ModoSimulacion`.
  Copiarlo hacía `IllegalStateException` en el arranque: nadie arrancaba el servidor con el
  ejemplo de la propia docs.
- TC19-05 esperaba 503 con `LENTO` (ver H3). TC19-09 probaba un `DELETE /sistemas/{s}/ping` que
  no está en ninguna matriz; se cambió por los dos métodos torcidos verificados (`TC19-G02`,
  `TC19-G12`). La tabla de datos semilla metía `ERROR` y `ROTATIVO` como modos; `ROTATIVO` es el
  alias del paciente `api-paciente-rotativo`.
- El javadoc de `EslabonAuditoria` enumeraba `LOGIN_SSO`, `SINCRONIZAR_HISTORIA` e
  `IMPORTAR_ESTUDIO`; el servicio registra `SSO_LOGIN`, `HCE_SINCRONIZAR`, `RIS_IMPORTAR` y
  `SIMULACION`.
- Nota: en sesiones anteriores se reportó "90 casos TDOP con 7 fallos". **Ese run nunca existió**
  (no había `Cdu19MatrizTdopTest` ni CSV en Git). Los 7 hallazgos que anunciaba sí eran reales:
  4 de ellos son H1, H2, H3 y H5 de esta tabla.

## 6. Deuda que deja la CDU19 (para registrar en `STATUS.md`)

1. **D-01** H2: servir la instantánea guardada cuando el externo cae (cache-first con política de
   obsolescencia explícita), o documentar que el degradado no incluye caché.
2. **D-02** H5: test de `IntegracionProperties` (modos por sistema, overrides, config corrupta) y
   decidir si el modo inválido es *warning + default* (DoD) o *fail-fast*.
3. **D-03** H10: canjear la sesión SSO por el JWT de CDU17 y mapear el rol externo a `Permiso`.
4. **D-04** H13/H14: cliente HTTP real (timeout que corta, reintentos con backoff y cabecera
   idempotency-key) e importación DICOMweb del PACS para Iter16.
5. **D-05** TC de **modo degradado como regresión en `iso2-server-app`** (hoy solo TC19-04 manual)
   y **403 por rol no-Administrador automatizado**: ambos los pide el DoD y hoy no hay ningún test
   de `iso2-server-app` que toque `/api/integracion`.
6. **D-06** H11: auditoría de integración persistente (habría que abrir la puerta a Flyway, que la
   issue cerraba).
7. **D-07** `HuellaContenido` (2/4 líneas) y el record `IntegracionProperties.SistemaOverride` (0/1)
   sin cubrir, y las 19 ramas que faltan de `IntegracionProperties` (solapable con D-02).

## 7. Riesgos para `v0.2.0-alpha` (etiqueta `R1`, fusión a `main`)

| Riesgo | Impacto | Mitigación propuesta |
|---|---|---|
| El spike deja SSO y adaptadores simulados y R1 va a llevar `/api/integracion` publicado | un usuario final puede llamar a endpoints que autentican contra credenciales fijas de juguete | dejar los endpoints tras `ADMINISTRADOR` (ya hecho) y decidir si R1 los expone o se apagan con `iso2.integracion.enabled` en Iter7 |
| H1 + H4: el cliente ve 503 y no distingue "caído" de "no existe" | soporte no puede diagnosticar | documentar la precedencia en la wiki y decidir en Iter7 si se separa en códigos de error |
| H5: un `application.yml` con un modo mal escrito tira el arranque | el despliegue de R1 no arranca por un typo | D-02 antes de R1, o validar el modo con `@ConfigurationProperties` + `@Valid` |
| 126 TC automáticos pero 9 TC humanos sin ejecutar | el DoD de la CDU19 no está cerrado | Ismael ejecuta `docs/tc/cdu19-tc-humanos.md` el 21/11 |

## 8. Cómo se reproduce

```bash
mvn -B -ntp -pl iso2-integracion test                       # los 203 tests, incluido la matriz
mvn -B -ntp -pl iso2-integracion verify                     # puerta JaCoCo (jacoco.line.min = 0,70)
python tools/pairwise.py docs/test/iter06/tdop-cdu19-espec.json \
       --resources iso2-integracion/src/test/resources      # regenera los CSV y su copia de test
python tools/tc_plantillas.py docs/test/iter06/tdop-cdu19-espec.json \
       --tc docs/tc/cdu19-tc-humanos.md                     # evidencia TDOP + bloque de la plantilla
```

La semilla del generador es `20261115` (fija en la espec), así que los CSV, la evidencia
`docs/test/iter06/tdop-cdu19.md` y el bloque generado salen idénticos byte a byte; la plantilla
**no toca** los TC19-01..TC19-09 escritos a mano: solo reescribe lo que hay entre los marcadores
`<!-- TDOP:BEGIN -->` y `<!-- TDOP:END -->`. Tras regenerar hay que volver a lanzar el módulo,
porque los CSV viven también en `iso2-integracion/src/test/resources/tdop/`.
