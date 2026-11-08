# TC humanos CDU06 · Solitud de pruebas (CFG-04 / Iter4)

**Código bajo prueba:** `iso2-solicitudes` 1.1.0 + `iso2-server-app` 0.1.5 + `iso2-client-app`
0.1.5 (base: `develop` con el merge `a3fe109`; el estado exacto de CFG-04 es el del tag
`cfg-iter04`). Regla de mínimos D24: 1 feliz + ≥2 error + 1 permisos.

**Puntos de entrada:** `POST /api/solicitudes` (prescribir), `GET /api/solicitudes/mias`
(historial del médico autenticado, más reciente primero), `GET /api/solicitudes/{id}` (detalle por
UUID), `GET /api/solicitudes/{id}/integridad` (traza de no repudio). Códigos: 201 · 200 ·
400 `DATOS_INVALIDOS` · 403 `FORBIDDEN` · 404 `SOLICITUD_NO_ENCONTRADA` ·
409 `CONFLICTO_SOLICITUD_DUPLICADA` / `CONFLICTO_PRUEBA_NO_DISPONIBLE` · 500 `ERROR_INTERNO`.
Alternativa en el cliente javaFX: secciones **Solicitudes (médico)** y
**Consultar solicitudes (recepcionista)**.

**RF06.1 (prescribir exige médico, paciente, prueba e indicación clínica) se comprueba en dos
sitios.** En el **cliente**, `ApiClienteSolicitudes` envía **5 campos** —`pacienteId`,
`codigoPrueba`, `indicacionClinica`, `prioridad`, `origen`— y **nunca el autor**: la sección de
prescripción solo se abre habiendo médico en `SesiónActual` y la vista valida que paciente/prueba
no estén vacíos. En el **servidor** está la regla de verdad: `SolicitudPrescripcionDTO` rechaza
cualquier campo en blanco (400) y **el autor se toma del claim `sub` del JWT**
(`SolicitudControlador#prescribir`), de modo que inventar un `medicoId` en el JSON no sirve para
suplantar a otro médico (RF06.2, no repudio). Por eso los TC06-02 y TC06-08 hay que ejecutarlos en
**ambos lados**.

**Preparación:** arrancar el servidor con `$env:ISO2_JWT_SECRET` (ver `docs/tc/README.md`) y hacer
login en `POST /api/auth/login` con `medico` / `iso2-medico` (o `recepcionista` / `iso2-rec`,
`admin` / `iso2-admin`). La migración `V1.4.0` siembra dos pruebas **activas** del catálogo para
poder prescribir sin dar de alta a mano: `SEM-RX-TORAX` y `SEM-RM-RODILLA`. No siembra
solicitudes: la primera prescripción de cada caso crea su propia fila.

| ID | Título | Tipo | Regla / puerta que cubre | Pasos | Resultado esperado | Resultado real | Pasa | Fecha/ejecutor |
|---|---|---|---|---|---|---|---|---|
| TC06-01 | Prescribir una prueba como médico | feliz | RF06.1 alta de solicitud | *rellenar* (médico: POST con `pacienteId=PAC-001`, `codigoPrueba=SEM-RX-TORAX`, `indicacionClinica=Dolor toracico`, `prioridad=ORDINARIA`, `origen=AP`) | 201 con `solicitudId` (UUID), `estado=creada` y `fechaSolicitada` del día; en la tabla `solicitud`, `medico_id = medico` y `huella` de 64 hex | | | |
| TC06-02 | Prescribir sin paciente o sin indicación | error (CE vacío) | RF06.1 campos obligatorios | *rellenar* (POST con `pacienteId` vacío; repetir con `indicacionClinica` en blanco; y en el cliente dejar el campo Paciente vacío y pulsar **Prescribir prueba**) | 400 `DATOS_INVALIDOS` con mensaje del campo ausente; el cliente no llega a llamar al servidor | | | |
| TC06-03 | Prioridad fuera de conjunto | error (CE inválida) | RF06.1 dominio de la prioridad | *rellenar* (POST con `prioridad=MAXIMA`, y con `prioridad` ausente; comprobar que el combobox del cliente solo ofrece ORDINARIA / PREFERENTE / URGENTE) | 400 `DATOS_INVALIDOS` («la prioridad … no es valida»); en el cliente no hay forma de elegir otra | | | |
| TC06-04 | Prueba inexistente en el catálogo | error (CE inválida) | integridad CDU06 ↔ CDU16 | *rellenar* (POST con `codigoPrueba=SEM-NO-EXISTE`) | 409 `CONFLICTO_PRUEBA_NO_DISPONIBLE` | | | |
| TC06-05 | Prueba dada de baja lógica en el catálogo | error (CE estado) | integridad: no se prescribe catálogo inactivo | *rellenar* (ADMIN borra con `DELETE /api/catalogo/pruebas/{codigo}` una prueba de catálogo creada a propósito; prescribirla; reactivarla con `PUT …/reactivar` y repetir la prescripción) | 409 `CONFLICTO_PRUEBA_NO_DISPONIBLE` mientras está inactiva; 201 tras reactivarla | | | |
| TC06-06 | Repetir la misma prueba al mismo paciente estando activa | error (anti-duplicado RF06.3) | integridad asistencial: no duplicar solicitudes en curso | *rellenar* (repetir el POST de TC06-01 tal cual; después cancelar la primera a mano en la BD (`estado=CANCELADA`) y volver a prescribir) | segundo POST → 409 `CONFLICTO_SOLICITUD_DUPLICADA`; tras `CANCELADA` → 201 nuevo `solicitudId` | | | |
| TC06-07 | Mismo paciente y prueba pero con otro médico | feliz (límite de la regla) | alcance de RF06.3 (el duplicado se mide **por médico**) | *rellenar* (prescribir lo mismo con otro usuario médico, p. ej. `bloqueable` / `iso2-bloq` si tiene rol médico, o con `admin`) | 201: la regla no cruza médicos | | | |
| TC06-08 | Historial del médico | feliz (consulta) | completitud del historial propio | *rellenar* (tras dos altas distintas, `GET /api/solicitudes/mias` o botón **Recargar historial**) | 200 con las dos solicitudes del médico, más reciente primero, y **sin** las de otros médicos | | | |
| TC06-09 | No repudio: autor, instante y huella | feliz (auditoría / trazabilidad) | RF06.2 responsabilidad | *rellenar* (`GET /api/solicitudes/{id}/integridad`, o botón **Ver no repudio de la seleccion** / **Verificar no repudio** en la vista de consulta) | 200 con `autor=medico`, `creadaEn` con microsegundos (`TIMESTAMP(6)`), `huella` SHA-256 de 64 hex y `integridadOk=true` | | | |
| TC06-10 | Huella que no se repite y no se pisa | feliz (integridad sin competencia) | integridad: dos solicitudes cercanas en el tiempo conservan su huella | *rellenar* (prescribir dos pruebas distintas al mismo paciente en menos de un segundo; consultar ambas integridades) | dos `huella` distintos y ambos con `integridadOk=true` (la marca de tiempo con micros evita la colisión) | | | |
| TC06-11 | Solicitud inexistente | error (CE inexistente) | corrección de la capa REST | *rellenar* (`GET /api/solicitudes/00000000-0000-0000-0000-000000000000`, y también `/integridad`) | 404 `SOLICITUD_NO_ENCONTRADA` | | | |
| TC06-12 | Prescribir sin permiso de escritura | permisos | RBAC `solicitud:escribir` (solo médico y administrador) | *rellenar* (POST con tokens de `recepcionista`, `celador`, `tecnico` y `paciente` si existe; repetir con `medico`) | 403 `FORBIDDEN` en los cuatro intentos; 201 con `medico` | | | |
| TC06-13 | Consultar sin permiso de lectura | permisos | RBAC `solicitud:leer` | *rellenar* (`GET /api/solicitudes/mias` con `tecnico` y `celador`; repetir con `recepcionista`) | 403 para técnico y celador; 200 para recepcionista, que además **no ve la sección de prescribir** en el cliente | | | |
| TC06-14 | Un médico no ve el historial de otro | permisos (privacidad) | confidencialidad del historial | *rellenar* (con `medico2` pedir `GET /api/solicitudes/{id}` de una solicitud de `medico`; comparar con el `GET` del autor) | el detalle por UUID lo responde el servicio sin filtro por autor: anotar lo observado y decidir si se endurece (queda como hallazgo si se considera defecto) | | | |

## Resultado global de la tabla

- [ ] Mínimos D24 cubiertos (1 feliz + ≥2 error + 1 permisos)
- [ ] RF06.1 verificada en **cliente y servidor** (TC06-02 y TC06-03, y el paso de cliente de TC06-08)
- [ ] Todos los TC ejecutados → CFG-04 **cerrada** (marcar en `STATUS.md`; el tag es `cfg-iter04`)

> Lo funcional ya está cubierto automáticamente por los 24 tests de `iso2-solicitudes`, los 13 de
> `SolicitudesApiTest` (HTTP real + H2 + Flyway) y los 11 de `ApiClienteSolicitudesTest`
> (ver la página **Iter4 · CDU06** de la wiki). Esta plantilla es la **puerta humana D28**: se
> ejecuta sobre el sistema en marcha, no sobre mocks.
