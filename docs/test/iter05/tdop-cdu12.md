# Evidencia TDOP de CDU12 (iter05 · CFG-05)

Metodo: Clases de equivalencia + teorema de valores limite + conjetura de errores, compuestas con pairwise (todo par de niveles de factores distintos queda cubierto).

Generador: `python tools/pairwise.py docs/test/iter05/tdop-cdu12-espec.json --resources iso2-citas/src/test/resources/tdop` (semilla 20261114).
Las mismas filas las ejecutan los JUnit parametrizados de `es.uclm.iso2.citas.tdop` y las tablas de `docs/tc/cdu12-tc-humanos.md`: un ID vale para las dos.

## Leyenda de tokens

| Token | Significado |
|---|---|
| `AUT` | valor autocoadyuvante: dentro de la franja falsa (api-medico, 2026-11-13T09:00), siempre dentro de clase |
| `VACIO` | cadena vacia "" en un campo obligatorio |
| `ESPACIOS` | texto con espacios delante y detras (profesionalId '  api-medico  ') |
| `AUSENTE` | campo no enviado: el cuerpo JSON no trae la clave |
| `JSON_MALFORMADO` | cuerpo que no es JSON legible |
| `JSON_VACIO` | cuerpo {} : JSON legitimo pero sin el campo obligatorio |
| `JSON_NULO` | cuerpo literal null |
| `TIPO_ERRONEO` | JSON legitimo con campos de tipo imposible (inicio 'no-fecha', duracion 'treinta') |
| `TEXT_PLANO` | Content-Type text/plain con cuerpo de texto (la API solo consume JSON) |
| `JUSTO_CIERRE` | valor limite: inicio 14:30, la cita de 30 min acaba justo al cerrar la franja |
| `APERTURA_JUSTA` | valor limite: inicio 08:00, exactamente al abrir la franja |
| `PASADA_CIERRE` | 14:45: la cita de 30 min reventaria el cierre (15:15) |
| `ANTERIOR_APERTURA` | 07:00: antes de la apertura de la franja |
| `FESTIVO` | 2026-11-10: dia festivo para la agenda falsa (sin franja) |
| `NEGATIVA` | -5 minutos: pasa la conversion JSON y la rechaza el servicio |
| `EXCESIVA` | 9999 minutos: no cabe en ninguna franja |
| `LIBRE` | el hueco pedido no esta ocupado antes de la peticion |
| `OCUPADO_EXACTO` | hueco sembrado que empieza justo en el inicio pedido |
| `OCUPADO_SOLAPE` | hueco sembrado que solapa el inicio pedido (empezaria 15 min despues) |
| `COHERENTE` | rango de dias normal 2026-11-12..2026-11-14 |
| `IGUAL_DIA` | desde == hasta (2026-11-13): franja de un solo dia |
| `INVERTIDO` | desde despues de hasta: regla de negocio, no error de sintaxis |
| `SOLO_FESTIVO` | rango 2026-11-10..2026-11-10: solo el festivo, la agenda responde sin franjas |
| `TEXTO_ILEGAL` | fecha con texto ('hola'): ni siquiera se puede convertir al tipo del parametro |
| `FECHA_CORTA` | '2026-11': fecha truncada que no cabe en LocalDate/LocalDateTime segun la puerta |
| `SEM-HUECO-SOL:EXACTO|SOLAPE` | sembrar cita_hueco relativa al inicio del nivel de la propia fila (tabla A) |
| `SEM-HUECO-DEST:EXACTO|SOLAPE` | sembrar cita_hueco relativa al nuevo inicio del nivel de la propia fila (tabla B) |
| `SEM-CITA:<EST>` | sembrar una cita en estado <EST> (inicio 2026-11-13T09:00, solicitud api-sol-creada, hueco si esta activa) |
| `SEM-CITA-CERCANO` | la cita sembrada pasa a inicio 2026-11-11T09:00: dentro de las 24 h del plazo de cancelacion |
| `SEM-CITA-VENCIDA:<EST>` | cita con inicio 2026-11-12T09:00 (antes del corte 2026-11-13T00:00) en estado <EST> |
| `SEM-SOL:<EST>` | solicitud de CDU06 con estado <EST> (las api-* vienen presembradas en SolicitudesFalsas) |
| `SEM-HUECO-FIJO` | hueco hueso 2026-11-13T10:00-10:30 que parte el hueco libre de la agenda |
| `CERCANO` | plazo: la cita cae dentro de las 24 h de antelacion exigidas por la politica de cancelacion |
| `LEJOS` | plazo: la cita queda fuera del plazo (cancelar es libre) |
| `NO_ENCUENTRA` | la ruta apunta a un id de cita que no existe |
| `-` | no aplica |

## Estados esperados (nombre simbolico -> HTTP)

| Estado | HTTP | Codigo de error | Que significa |
|---|---|---|---|
| `CREADO` | 201 | `-` | cita creada y hueco bloqueado (POST /api/citas) |
| `OK` | 200 | `-` | peticion atendida con cuerpo (consulta, operacion de estado o lote de vencidas) |
| `INVALIDOS` | 400 | `DATOS_INVALIDOS` | entrada fuera de clase o regla de negocio violada en el servicio |
| `SOLICITUD_MALFORMADA` | 400 | `DATOS_INVALIDOS` | el cuerpo ni siquiera se puede leer: HttpMessageNotReadableException tiene consejo propio -> 400 con cuerpo ApiError DATOS_INVALIDOS (HALLAZGO: comparte codigo con INVALIDOS) |
| `NO_ENCONTRADA` | 404 | `CITA_NO_ENCONTRADA` | el id de la ruta no apunta a ninguna cita |
| `RUTA_INEXISTENTE` | 404 | `-` | ninguna mano del controlador atiende la ruta: 404 de Spring sin cuerpo ApiError (alternativa al 500 conjeturado; la rojo decide) |
| `METODO_NO_PERMITIDO` | 405 | `-` | la ruta si esta mapeada para otros metodos pero no para el enviado: Spring resuelve el 405 antes de llamar a cualquier handler, asi que el consejo Exception del controlador no lo alcanza y el cuerpo no es ApiError (la rojo decide) |
| `TRANSICION` | 409 | `CONFLICTO_TRANSICION_INVALIDA` | la operacion no esta permitida desde el estado actual de la cita |
| `FUERA_FRANJA` | 409 | `CONFLICTO_FUERA_DE_FRANJA` | el profesional no tiene agenda, o el dia es festivo, o la cita no cabe en la franja |
| `HUECO_OCUPADO` | 409 | `CONFLICTO_HUECO_OCUPADO` | el hueco ya esta bloqueado por otra cita activa (o la base de datos lo rechazo por la restriccion unica) |
| `SOLICITUD_NO_APTA` | 409 | `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud no existe o no esta en estado creada (D36) |
| `PLAZO` | 409 | `CONFLICTO_PLAZO_CANCELACION` | cancelar fuera de las 24 h de antelacion que fija la politica de la prueba |
| `ERROR_INTERNO` | 500 | `ERROR_INTERNO` | el consejo Exception generico absorbe lo que no tiene handler propio: falta un parametro obligatorio, el tipo del parametro no casa, metodo o ruta sin mapear (HALLAZGO: en CDU12 no se arreglaron estos casos como en D35) |

## Reparto de casos

| Tabla | Operacion | Modo | Filas |
|---|---|---|---|
| A | Alta de cita (POST /api/citas) | pairwise | 38 |
| B | Reprogramacion de cita (POST /api/citas/{id}/reprogramar) | pairwise | 31 |
| C | Huecos libres (GET /api/citas/huecos) | pairwise | 29 |
| D | Listado de citas con filtros (GET /api/citas) | pairwise | 27 |
| E | Operaciones de estado de la cita (confirmar, realizar, reprogramar, cancelar, detalle, lote de expiracion) | filas | 34 |
| F | Conjetura de errores y rincones del contrato | conjetura | 13 |
| **Total** |  |  | **172** |

## Tabla A · Alta de cita (POST /api/citas)

**Modo:** `pairwise` · producto de niveles = 7875 · filas generadas = 38 · pares obligatorios cubiertos = 203/203 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF completitud y correccion: toda alta valida crea la cita y bloquea su hueco, y toda alta invalida se rechaza con un codigo de error estable; SEG: ningun hueco queda doblemente reservado

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `cuerpo` | `VALIDO` | valido | - | JSON completo con los factores de esta misma fila |
| `cuerpo` | `VACIO` | **fuera de clase** | clase invalida: solicitud ilegible | POST sin cuerpo |
| `cuerpo` | `JSON_MALFORMADO` | **fuera de clase** | clase invalida: solicitud ilegible | '{no-json' |
| `cuerpo` | `JSON_NULO` | **fuera de clase** | clase invalida: solicitud ilegible | literal null: tampoco materializa el record |
| `cuerpo` | `TIPO_ERRONEO` | **fuera de clase** | clase invalida: tipos imposibles | inicio 'no-fecha' y duracion 'treinta' con Content-Type JSON |
| `profesionalId` | `MEDICO` | valido | - | api-medico, con agenda 08:00-15:00 salvo festivo |
| `profesionalId` | `VACIO` | **fuera de clase** | clase invalida: obligatorio en blanco | cadena vacia: el servicio la rechaza antes de nada util |
| `profesionalId` | `ESPACIOS` | valido | - | '  api-medico  ': Cita.nueva recorta profesionalId, asi que si encuentra agenda |
| `profesionalId` | `SIN_AGENDA` | valido | - | api-sin-agenda: existe como id pero CDU13 no le da agenda |
| `profesionalId` | `DESCONOCIDO` | valido | - | api-profesional-que-no-existe |
| `inicio` | `AUT` | valido | clase valida tipica | 2026-11-13T09:00 dentro de la franja |
| `inicio` | `APERTURA_JUSTA` | valido | valor limite: borde inferior de la franja | 2026-11-13T08:00: justo la apertura |
| `inicio` | `JUSTO_CIERRE` | valido | valor limite: ultimo inicio que cabe | 2026-11-13T14:30: con 30 min acaba en el cierre exacto |
| `inicio` | `PASADA_CIERRE` | valido | valor limite: primer inicio que revienta el cierre | 2026-11-13T14:45: un minuto despues del ultimo inicio que cabe |
| `inicio` | `ANTERIOR_APERTURA` | valido | valor limite: borde inferior fallando | 2026-11-13T07:00: un hora antes de abrir |
| `inicio` | `FESTIVO` | valido | valor limite: borde del calendario festivo | 2026-11-10T09:00: hora correcta en dia festivo |
| `inicio` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | el JSON no trae la clave inicio |
| `duracion` | `TREINTA` | valido | - | 30 minutos |
| `duracion` | `NEGATIVA` | **fuera de clase** | clase invalida + borde: el entero mas alla de lo minimo | -5: el record primitivo la acepta y el servicio no |
| `duracion` | `EXCESIVA` | valido | valor limite: longitud desbordada | 9999 minutos: pasa el parseo, la franja la rechaza |
| `estadoHueco` | `LIBRE` | valido | - | sin fila en cita_hueco en esa hora |
| `estadoHueco` | `OCUPADO_EXACTO` | valido | valor limite: solape minimo exacto | hueco hueso con inicio == el pedido |
| `estadoHueco` | `OCUPADO_SOLAPE` | valido | valor limite: solape parcial | hueco hueso 15 min despues del inicio pedido |
| `estadoSolicitud` | `CREADA` | valido | - | api-sol-creada: la unica apta para agendar (D36) |
| `estadoSolicitud` | `CITADA` | valido | - | api-sol-citada: ya tenia cita |
| `estadoSolicitud` | `PRESCRITA` | valido | - | api-sol-prescrita: todavia no es apta |
| `estadoSolicitud` | `INEXISTENTE` | valido | - | api-sol-que-no-existe |
| `estadoSolicitud` | `NULA` | valido | - | solicitudId null: cita sin solicitud, CDU06 no se consulta |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `cuerpo` vale VACIO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 2 | `cuerpo` vale JSON_MALFORMADO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 3 | `cuerpo` vale JSON_NULO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 4 | `cuerpo` vale TIPO_ERRONEO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 5 | `profesionalId` vale VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `inicio` vale AUSENTE | `INVALIDOS` | `DATOS_INVALIDOS` |
| 7 | `duracion` vale NEGATIVA | `INVALIDOS` | `DATOS_INVALIDOS` |
| 8 | `estadoSolicitud` vale INEXISTENTE | `SOLICITUD_NO_APTA` | `CONFLICTO_SOLICITUD_NO_APTA` |
| 9 | `estadoSolicitud` vale CITADA | `SOLICITUD_NO_APTA` | `CONFLICTO_SOLICITUD_NO_APTA` |
| 10 | `estadoSolicitud` vale PRESCRITA | `SOLICITUD_NO_APTA` | `CONFLICTO_SOLICITUD_NO_APTA` |
| 11 | `profesionalId` vale SIN_AGENDA | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 12 | `profesionalId` vale DESCONOCIDO | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 13 | `inicio` vale ANTERIOR_APERTURA | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 14 | `inicio` vale PASADA_CIERRE | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 15 | `inicio` vale FESTIVO | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 16 | `duracion` vale EXCESIVA | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 17 | `estadoHueco` vale OCUPADO_EXACTO | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 18 | `estadoHueco` vale OCUPADO_SOLAPE | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 19 | resto de casos | `CREADO` | `-` |

### Casos generados

| ID | cuerpo | profesionalId | inicio | duracion | estadoHueco | estadoSolicitud | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|---|
| `TC12-A01` | `VALIDO` | `MEDICO` | `AUT` | `TREINTA` | `LIBRE` | `CREADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CREADA` | CREADO | alta valida: 201 con la cita y su hueco bloqueado (saveAndFlush contra la unica), con profesionalId ESPACIOS Cita.nueva lo recorta y tambien crea \| valores limite: inicio=AUT (clase valida tipica) |
| `TC12-A02` | `VALIDO` | `MEDICO` | `AUT` | `TREINTA` | `OCUPADO_EXACTO` | `CREADA` | `SEM-HUECO-SOL:EXACTO,SEM-SOL:CREADA` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | exigeHuecoLibre ve un hueco que arranca justo en el inicio pedido \| valores limite: inicio=AUT (clase valida tipica), estadoHueco=OCUPADO_EXACTO (valor limite: solape minimo exacto) |
| `TC12-A03` | `VALIDO` | `MEDICO` | `AUT` | `TREINTA` | `OCUPADO_SOLAPE` | `CREADA` | `SEM-HUECO-SOL:SOLAPE,SEM-SOL:CREADA` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | el solape con otro hueco activo tambien lo rechaza el motor anti-colision \| valores limite: inicio=AUT (clase valida tipica), estadoHueco=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-A04` | `VACIO` | `ESPACIOS` | `APERTURA_JUSTA` | `EXCESIVA` | `LIBRE` | `CITADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CITADA` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | sin cuerpo no hay CitaNuevaDTO: HttpMessageNotReadableException (consejo cuerpoIlegible) \| cuerpo: POST sin cuerpo |
| `TC12-A05` | `JSON_MALFORMADO` | `SIN_AGENDA` | `JUSTO_CIERRE` | `EXCESIVA` | `OCUPADO_EXACTO` | `PRESCRITA` | `SEM-HUECO-SOL:EXACTO,SEM-SOL:PRESCRITA` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | el cuerpo no es JSON legible: HttpMessageNotReadableException \| cuerpo: '{no-json' |
| `TC12-A06` | `JSON_NULO` | `DESCONOCIDO` | `PASADA_CIERRE` | `EXCESIVA` | `OCUPADO_SOLAPE` | `INEXISTENTE` | `SEM-HUECO-SOL:SOLAPE` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | literal null: @RequestBody obligatorio no perdona un cuerpo null y HttpMessageNotReadable lo traduce el consejo cuerpoIlegible \| cuerpo: literal null: tampoco materializa el record |
| `TC12-A07` | `TIPO_ERRONEO` | `ESPACIOS` | `PASADA_CIERRE` | `TREINTA` | `LIBRE` | `PRESCRITA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:PRESCRITA` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | los campos no caben en los tipos del record: la conversion JsonMappingException viene envuelta en HttpMessageNotReadableException \| cuerpo: inicio 'no-fecha' y duracion 'treinta' con Content-Type JSON |
| `TC12-A08` | `VALIDO` | `VACIO` | `JUSTO_CIERRE` | `TREINTA` | `LIBRE` | `INEXISTENTE` | `SEM-HUECO-SOL:LIBRE` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A09` | `VALIDO` | `SIN_AGENDA` | `AUSENTE` | `TREINTA` | `LIBRE` | `NULA` | `SEM-HUECO-SOL:LIBRE` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: IllegalArgumentException del servicio antes de mirar la solicitud \| inicio: el JSON no trae la clave inicio |
| `TC12-A10` | `VALIDO` | `DESCONOCIDO` | `ANTERIOR_APERTURA` | `NEGATIVA` | `OCUPADO_EXACTO` | `CITADA` | `SEM-HUECO-SOL:EXACTO,SEM-SOL:CITADA` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A11` | `VALIDO` | `ESPACIOS` | `JUSTO_CIERRE` | `NEGATIVA` | `OCUPADO_SOLAPE` | `NULA` | `SEM-HUECO-SOL:SOLAPE` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A12` | `VALIDO` | `VACIO` | `FESTIVO` | `EXCESIVA` | `OCUPADO_EXACTO` | `NULA` | `SEM-HUECO-SOL:EXACTO` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A13` | `VALIDO` | `DESCONOCIDO` | `FESTIVO` | `NEGATIVA` | `LIBRE` | `CREADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A14` | `VALIDO` | `SIN_AGENDA` | `FESTIVO` | `TREINTA` | `OCUPADO_SOLAPE` | `CITADA` | `SEM-HUECO-SOL:SOLAPE,SEM-SOL:CITADA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | solo vale agendar sobre una solicitud en estado creada (D36) \| valores limite: inicio=FESTIVO (valor limite: borde del calendario festivo), estadoHueco=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-A15` | `VALIDO` | `SIN_AGENDA` | `APERTURA_JUSTA` | `NEGATIVA` | `OCUPADO_EXACTO` | `INEXISTENTE` | `SEM-HUECO-SOL:EXACTO` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A16` | `VALIDO` | `DESCONOCIDO` | `APERTURA_JUSTA` | `TREINTA` | `OCUPADO_SOLAPE` | `PRESCRITA` | `SEM-HUECO-SOL:SOLAPE,SEM-SOL:PRESCRITA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud prescrita todavia no puede llevar cita \| valores limite: inicio=APERTURA_JUSTA (valor limite: borde inferior de la franja), estadoHueco=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-A17` | `VALIDO` | `VACIO` | `ANTERIOR_APERTURA` | `EXCESIVA` | `OCUPADO_SOLAPE` | `CREADA` | `SEM-HUECO-SOL:SOLAPE,SEM-SOL:CREADA` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A18` | `VALIDO` | `MEDICO` | `PASADA_CIERRE` | `NEGATIVA` | `OCUPADO_EXACTO` | `CITADA` | `SEM-HUECO-SOL:EXACTO,SEM-SOL:CITADA` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A19` | `VALIDO` | `ESPACIOS` | `AUSENTE` | `EXCESIVA` | `OCUPADO_EXACTO` | `CREADA` | `SEM-HUECO-SOL:EXACTO,SEM-SOL:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: IllegalArgumentException del servicio antes de mirar la solicitud \| inicio: el JSON no trae la clave inicio |
| `TC12-A20` | `VALIDO` | `MEDICO` | `AUSENTE` | `EXCESIVA` | `OCUPADO_SOLAPE` | `PRESCRITA` | `SEM-HUECO-SOL:SOLAPE,SEM-SOL:PRESCRITA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: IllegalArgumentException del servicio antes de mirar la solicitud \| inicio: el JSON no trae la clave inicio |
| `TC12-A21` | `VALIDO` | `MEDICO` | `ANTERIOR_APERTURA` | `TREINTA` | `LIBRE` | `INEXISTENTE` | `SEM-HUECO-SOL:LIBRE` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | CDU06 no conoce esa solicitud (D36) \| valores limite: inicio=ANTERIOR_APERTURA (valor limite: borde inferior fallando) |
| `TC12-A22` | `VALIDO` | `ESPACIOS` | `AUT` | `NEGATIVA` | `LIBRE` | `PRESCRITA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:PRESCRITA` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos < 0 se rechaza antes de tocar CDU06 o CDU13 (precedencia leida del servicio) \| duracion: -5: el record primitivo la acepta y el servicio no |
| `TC12-A23` | `VALIDO` | `VACIO` | `AUT` | `EXCESIVA` | `LIBRE` | `CITADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CITADA` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A24` | `VALIDO` | `ESPACIOS` | `FESTIVO` | `TREINTA` | `LIBRE` | `INEXISTENTE` | `SEM-HUECO-SOL:LIBRE` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | CDU06 no conoce esa solicitud (D36) \| valores limite: inicio=FESTIVO (valor limite: borde del calendario festivo) |
| `TC12-A25` | `VALIDO` | `MEDICO` | `APERTURA_JUSTA` | `TREINTA` | `LIBRE` | `NULA` | `SEM-HUECO-SOL:LIBRE` | CREADO | alta valida: 201 con la cita y su hueco bloqueado (saveAndFlush contra la unica), con profesionalId ESPACIOS Cita.nueva lo recorta y tambien crea \| valores limite: inicio=APERTURA_JUSTA (valor limite: borde inferior de la franja) |
| `TC12-A26` | `VALIDO` | `SIN_AGENDA` | `PASADA_CIERRE` | `TREINTA` | `LIBRE` | `CREADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CREADA` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | CDU13 responde AgendaNoEncontrado y el servicio lo envuelve en FUERA_DE_FRANJA (D36: profesional sin agenda no es error 404) \| valores limite: inicio=PASADA_CIERRE (valor limite: primer inicio que revienta el cierre) |
| `TC12-A27` | `VALIDO` | `DESCONOCIDO` | `AUT` | `TREINTA` | `LIBRE` | `NULA` | `SEM-HUECO-SOL:LIBRE` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | sin agenda para ese id, la franja esta vacia \| valores limite: inicio=AUT (clase valida tipica) |
| `TC12-A28` | `VALIDO` | `MEDICO` | `FESTIVO` | `TREINTA` | `LIBRE` | `PRESCRITA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:PRESCRITA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud prescrita todavia no puede llevar cita \| valores limite: inicio=FESTIVO (valor limite: borde del calendario festivo) |
| `TC12-A29` | `VALIDO` | `SIN_AGENDA` | `AUT` | `TREINTA` | `LIBRE` | `INEXISTENTE` | `SEM-HUECO-SOL:LIBRE` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | CDU06 no conoce esa solicitud (D36) \| valores limite: inicio=AUT (clase valida tipica) |
| `TC12-A30` | `VALIDO` | `VACIO` | `APERTURA_JUSTA` | `TREINTA` | `LIBRE` | `CREADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CREADA` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A31` | `VALIDO` | `VACIO` | `PASADA_CIERRE` | `TREINTA` | `LIBRE` | `PRESCRITA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:PRESCRITA` | INVALIDOS `DATOS_INVALIDOS` | el servicio exige profesional no en blanco justo despues del parseo \| profesionalId: cadena vacia: el servicio la rechaza antes de nada util |
| `TC12-A32` | `VALIDO` | `DESCONOCIDO` | `AUSENTE` | `TREINTA` | `LIBRE` | `INEXISTENTE` | `SEM-HUECO-SOL:LIBRE` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: IllegalArgumentException del servicio antes de mirar la solicitud \| inicio: el JSON no trae la clave inicio |
| `TC12-A33` | `VALIDO` | `MEDICO` | `JUSTO_CIERRE` | `TREINTA` | `LIBRE` | `CREADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CREADA` | CREADO | alta valida: 201 con la cita y su hueco bloqueado (saveAndFlush contra la unica), con profesionalId ESPACIOS Cita.nueva lo recorta y tambien crea \| valores limite: inicio=JUSTO_CIERRE (valor limite: ultimo inicio que cabe) |
| `TC12-A34` | `VALIDO` | `DESCONOCIDO` | `JUSTO_CIERRE` | `TREINTA` | `LIBRE` | `CITADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CITADA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | solo vale agendar sobre una solicitud en estado creada (D36) \| valores limite: inicio=JUSTO_CIERRE (valor limite: ultimo inicio que cabe) |
| `TC12-A35` | `VALIDO` | `ESPACIOS` | `ANTERIOR_APERTURA` | `TREINTA` | `LIBRE` | `PRESCRITA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:PRESCRITA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud prescrita todavia no puede llevar cita \| valores limite: inicio=ANTERIOR_APERTURA (valor limite: borde inferior fallando) |
| `TC12-A36` | `VALIDO` | `SIN_AGENDA` | `ANTERIOR_APERTURA` | `TREINTA` | `LIBRE` | `NULA` | `SEM-HUECO-SOL:LIBRE` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | CDU13 responde AgendaNoEncontrado y el servicio lo envuelve en FUERA_DE_FRANJA (D36: profesional sin agenda no es error 404) \| valores limite: inicio=ANTERIOR_APERTURA (valor limite: borde inferior fallando) |
| `TC12-A37` | `VALIDO` | `MEDICO` | `PASADA_CIERRE` | `TREINTA` | `LIBRE` | `NULA` | `SEM-HUECO-SOL:LIBRE` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 14:45 + duracion se pasa del cierre 15:00 \| valores limite: inicio=PASADA_CIERRE (valor limite: primer inicio que revienta el cierre) |
| `TC12-A38` | `VALIDO` | `MEDICO` | `AUSENTE` | `TREINTA` | `LIBRE` | `CITADA` | `SEM-HUECO-SOL:LIBRE,SEM-SOL:CITADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: IllegalArgumentException del servicio antes de mirar la solicitud \| inicio: el JSON no trae la clave inicio |

## Tabla B · Reprogramacion de cita (POST /api/citas/{id}/reprogramar)

**Modo:** `pairwise` · producto de niveles = 150 · filas generadas = 31 · pares obligatorios cubiertos = 30/30 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF correccion: reprogramar mueve el hueco de la propia cita y solo desde estados no terminales; SEG: nunca se pisa el hueco de otra cita

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `cuerpo` | `VALIDO` | valido | - | JSON con el inicio del nivel nuevoInicio de la fila |
| `cuerpo` | `VACIO` | **fuera de clase** | clase invalida: solicitud ilegible | POST sin cuerpo |
| `cuerpo` | `JSON_MALFORMADO` | **fuera de clase** | clase invalida: solicitud ilegible | '{no-json' |
| `cuerpo` | `JSON_VACIO` | valido | valor limite: obligatorio ausente | {} : JSON legitimo pero sin inicio (no es ilegible, es fuera de clase) |
| `cuerpo` | `JSON_NULO` | **fuera de clase** | clase invalida: solicitud ilegible | literal null |
| `estadoCita` | `PROGRAMADA` | valido | - | estado inicial: reprogramar es feliz |
| `estadoCita` | `CONFIRMADA` | valido | - | no terminal: tambien se puede mover |
| `estadoCita` | `CANCELADA` | valido | - | terminal: moverA la rechaza |
| `estadoCita` | `EXPIRADA` | valido | - | terminal: moverA la rechaza |
| `estadoCita` | `REALIZADA` | valido | - | terminal: moverA la rechaza |
| `estadoCita` | `NO_ENCUENTRA` | valido | - | ruta con id api-cita-que-no-existe |
| `nuevoInicio` | `AUT` | valido | clase valida tipica | 2026-11-13T13:00 dentro de franja y hueco libre |
| `nuevoInicio` | `OCUPADO_EXACTO` | valido | valor limite: solape minimo | hueco hueso clavado en el nuevo inicio |
| `nuevoInicio` | `OCUPADO_SOLAPE` | valido | valor limite: solape parcial | hueco hueso 15 min despues |
| `nuevoInicio` | `FUERA_FRANJA` | valido | valor limite: borde de franja fallando | 2026-11-13T19:00: fuera de la franja |
| `nuevoInicio` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | cuerpo legitimo sin la clave inicio |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `cuerpo` vale VACIO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 2 | `cuerpo` vale JSON_MALFORMADO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 3 | `cuerpo` vale JSON_NULO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 4 | `nuevoInicio` vale AUSENTE | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `cuerpo` vale JSON_VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `estadoCita` vale NO_ENCUENTRA | `NO_ENCONTRADA` | `CITA_NO_ENCONTRADA` |
| 7 | `estadoCita` vale CANCELADA | `TRANSICION` | `CONFLICTO_TRANSICION_INVALIDA` |
| 8 | `estadoCita` vale EXPIRADA | `TRANSICION` | `CONFLICTO_TRANSICION_INVALIDA` |
| 9 | `estadoCita` vale REALIZADA | `TRANSICION` | `CONFLICTO_TRANSICION_INVALIDA` |
| 10 | `nuevoInicio` vale FUERA_FRANJA | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 11 | `nuevoInicio` vale OCUPADO_EXACTO | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 12 | `nuevoInicio` vale OCUPADO_SOLAPE | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 13 | resto de casos | `OK` | `-` |

### Casos generados

| ID | cuerpo | estadoCita | nuevoInicio | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC12-B01` | `VALIDO` | `PROGRAMADA` | `AUT` | `SEM-CITA:PROGRAMADA,SEM-HUECO-DEST:LIBRE` | OK | reprogramacion valida: 200 con la cita movida y su hueco reescrito \| valores limite: nuevoInicio=AUT (clase valida tipica) |
| `TC12-B02` | `JSON_VACIO` | `PROGRAMADA` | `AUT` | `SEM-CITA:PROGRAMADA,SEM-HUECO-DEST:LIBRE` | INVALIDOS `DATOS_INVALIDOS` | {} deja inicio nulo: mismo camino IAE que AUSENTE pero con JSON legitimo \| valores limite: cuerpo=JSON_VACIO (valor limite: obligatorio ausente), nuevoInicio=AUT (clase valida tipica) |
| `TC12-B03` | `VACIO` | `PROGRAMADA` | `OCUPADO_EXACTO` | `SEM-CITA:PROGRAMADA,SEM-HUECO-DEST:EXACTO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | POST sin cuerpo: HttpMessageNotReadableException \| cuerpo: POST sin cuerpo |
| `TC12-B04` | `JSON_MALFORMADO` | `PROGRAMADA` | `OCUPADO_SOLAPE` | `SEM-CITA:PROGRAMADA,SEM-HUECO-DEST:SOLAPE` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | cuerpo ilegible: HttpMessageNotReadableException \| cuerpo: '{no-json' |
| `TC12-B05` | `JSON_NULO` | `CONFIRMADA` | `AUT` | `SEM-CITA:CONFIRMADA,SEM-HUECO-DEST:LIBRE` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | null tampoco materializa ReprogramacionRequest \| cuerpo: literal null |
| `TC12-B06` | `VALIDO` | `PROGRAMADA` | `AUSENTE` | `SEM-CITA:PROGRAMADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |
| `TC12-B07` | `VALIDO` | `PROGRAMADA` | `FUERA_FRANJA` | `SEM-CITA:PROGRAMADA` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 19:00 no cabe en la franja 08:00-15:00 \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B08` | `VALIDO` | `CONFIRMADA` | `OCUPADO_EXACTO` | `SEM-CITA:CONFIRMADA,SEM-HUECO-DEST:EXACTO` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | hueco hueso en el nuevo inicio: exigeHuecoLibre excluye la cita propia pero no el resto \| valores limite: nuevoInicio=OCUPADO_EXACTO (valor limite: solape minimo) |
| `TC12-B09` | `VALIDO` | `CONFIRMADA` | `OCUPADO_SOLAPE` | `SEM-CITA:CONFIRMADA,SEM-HUECO-DEST:SOLAPE` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | solape parcial con otro hueco activo \| valores limite: nuevoInicio=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-B10` | `VALIDO` | `CONFIRMADA` | `FUERA_FRANJA` | `SEM-CITA:CONFIRMADA` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 19:00 no cabe en la franja 08:00-15:00 \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B11` | `VALIDO` | `CONFIRMADA` | `AUSENTE` | `SEM-CITA:CONFIRMADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |
| `TC12-B12` | `VALIDO` | `CANCELADA` | `AUT` | `SEM-CITA:CANCELADA,SEM-HUECO-DEST:LIBRE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | moverA rechaza mover una cita terminal antes de mirar franja y hueco \| valores limite: nuevoInicio=AUT (clase valida tipica) |
| `TC12-B13` | `VALIDO` | `CANCELADA` | `OCUPADO_EXACTO` | `SEM-CITA:CANCELADA,SEM-HUECO-DEST:EXACTO` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | moverA rechaza mover una cita terminal antes de mirar franja y hueco \| valores limite: nuevoInicio=OCUPADO_EXACTO (valor limite: solape minimo) |
| `TC12-B14` | `VALIDO` | `CANCELADA` | `OCUPADO_SOLAPE` | `SEM-CITA:CANCELADA,SEM-HUECO-DEST:SOLAPE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | moverA rechaza mover una cita terminal antes de mirar franja y hueco \| valores limite: nuevoInicio=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-B15` | `VALIDO` | `CANCELADA` | `FUERA_FRANJA` | `SEM-CITA:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | moverA rechaza mover una cita terminal antes de mirar franja y hueco \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B16` | `VALIDO` | `CANCELADA` | `AUSENTE` | `SEM-CITA:CANCELADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |
| `TC12-B17` | `VALIDO` | `EXPIRADA` | `AUT` | `SEM-CITA:EXPIRADA,SEM-HUECO-DEST:LIBRE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=AUT (clase valida tipica) |
| `TC12-B18` | `VALIDO` | `EXPIRADA` | `OCUPADO_EXACTO` | `SEM-CITA:EXPIRADA,SEM-HUECO-DEST:EXACTO` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=OCUPADO_EXACTO (valor limite: solape minimo) |
| `TC12-B19` | `VALIDO` | `EXPIRADA` | `OCUPADO_SOLAPE` | `SEM-CITA:EXPIRADA,SEM-HUECO-DEST:SOLAPE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-B20` | `VALIDO` | `EXPIRADA` | `FUERA_FRANJA` | `SEM-CITA:EXPIRADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B21` | `VALIDO` | `EXPIRADA` | `AUSENTE` | `SEM-CITA:EXPIRADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |
| `TC12-B22` | `VALIDO` | `REALIZADA` | `AUT` | `SEM-CITA:REALIZADA,SEM-HUECO-DEST:LIBRE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=AUT (clase valida tipica) |
| `TC12-B23` | `VALIDO` | `REALIZADA` | `OCUPADO_EXACTO` | `SEM-CITA:REALIZADA,SEM-HUECO-DEST:EXACTO` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=OCUPADO_EXACTO (valor limite: solape minimo) |
| `TC12-B24` | `VALIDO` | `REALIZADA` | `OCUPADO_SOLAPE` | `SEM-CITA:REALIZADA,SEM-HUECO-DEST:SOLAPE` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-B25` | `VALIDO` | `REALIZADA` | `FUERA_FRANJA` | `SEM-CITA:REALIZADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal: transicion prohibida \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B26` | `VALIDO` | `REALIZADA` | `AUSENTE` | `SEM-CITA:REALIZADA` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |
| `TC12-B27` | `VALIDO` | `NO_ENCUENTRA` | `AUT` | `SEM-HUECO-DEST:LIBRE` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el id de la ruta no existe (y el cuerpo ya era valido) \| valores limite: nuevoInicio=AUT (clase valida tipica) |
| `TC12-B28` | `VALIDO` | `NO_ENCUENTRA` | `OCUPADO_EXACTO` | `SEM-HUECO-DEST:EXACTO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el id de la ruta no existe (y el cuerpo ya era valido) \| valores limite: nuevoInicio=OCUPADO_EXACTO (valor limite: solape minimo) |
| `TC12-B29` | `VALIDO` | `NO_ENCUENTRA` | `OCUPADO_SOLAPE` | `SEM-HUECO-DEST:SOLAPE` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el id de la ruta no existe (y el cuerpo ya era valido) \| valores limite: nuevoInicio=OCUPADO_SOLAPE (valor limite: solape parcial) |
| `TC12-B30` | `VALIDO` | `NO_ENCUENTRA` | `FUERA_FRANJA` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el id de la ruta no existe (y el cuerpo ya era valido) \| valores limite: nuevoInicio=FUERA_FRANJA (valor limite: borde de franja fallando) |
| `TC12-B31` | `VALIDO` | `NO_ENCUENTRA` | `AUSENTE` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | inicio nulo: el servicio lo comprueba ANTES de buscar la cita (precedencia leida del codigo) \| nuevoInicio: cuerpo legitimo sin la clave inicio |

## Tabla C · Huecos libres (GET /api/citas/huecos)

**Modo:** `pairwise` · producto de niveles = 150 · filas generadas = 29 · pares obligatorios cubiertos = 74/74 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** COMP: la pantalla de recepcion recibe el calendario de huecos prometido; SEG: un hueco sembrado como ocupado nunca aparece libre

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `profesionalId` | `MEDICO` | valido | - | api-medico: unica agenda sembrada |
| `profesionalId` | `VACIO` | **fuera de clase** | clase invalida: obligatorio en blanco | profesionalId= en blanco |
| `profesionalId` | `ESPACIOS` | valido | - | '  api-medico  ': huecosLibres SI recorta el id antes de pedir la agenda |
| `profesionalId` | `SIN_AGENDA` | valido | - | api-sin-agenda: id conocido sin agenda en CDU13 |
| `profesionalId` | `DESCONOCIDO` | valido | - | api-profesional-que-no-existe: tambien sin agenda |
| `rango` | `COHERENTE` | valido | clase valida tipica | desde=2026-11-12 y hasta=2026-11-14 |
| `rango` | `IGUAL_DIA` | valido | valor limite: rango de un solo dia | desde == hasta == 2026-11-13 |
| `rango` | `INVERTIDO` | **fuera de clase** | valor limite: desde justo despues de hasta | desde=2026-11-14 y hasta=2026-11-12 |
| `rango` | `SOLO_FESTIVO` | valido | valor limite: borde del calendario festivo | 2026-11-10..2026-11-10: solo el festivo -> lista vacia |
| `rango` | `TEXTO_ILEGAL` | **fuera de clase** | clase invalida: tipo de parametro imposible | desde=hola: ni siquiera convierte a LocalDate |
| `rango` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente en la query | no se envia hasta (parametro obligatorio) |
| `duracion` | `D30` | valido | - | duracionMinutos=30 |
| `duracion` | `D60` | valido | - | duracionMinutos=60: parte los huecos de la franja |
| `duracion` | `AUSENTE` | valido | valor limite: duracion por defecto del contrato | sin el parametro: usa el hueco entero de la franja |
| `duracion` | `CERO` | **fuera de clase** | valor limite: el minimo mas alla de lo permitido | duracionMinutos=0 |
| `duracion` | `NEGATIVA` | **fuera de clase** | clase invalida: negativa | duracionMinutos=-5 |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `rango` vale TEXTO_ILEGAL | `ERROR_INTERNO` | `ERROR_INTERNO` |
| 2 | `rango` vale AUSENTE | `ERROR_INTERNO` | `ERROR_INTERNO` |
| 3 | `rango` vale INVERTIDO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 4 | `profesionalId` vale VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `duracion` vale CERO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `duracion` vale NEGATIVA | `INVALIDOS` | `DATOS_INVALIDOS` |
| 7 | `profesionalId` vale SIN_AGENDA | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 8 | `profesionalId` vale DESCONOCIDO | `FUERA_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 9 | resto de casos | `OK` | `-` |

### Casos generados

| ID | profesionalId | rango | duracion | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC12-C01` | `MEDICO` | `COHERENTE` | `D30` | `SEM-HUECO-FIJO` | OK | calendario de huecos en JSON (200), con ESPACIOS el servicio SI recorta profesionalId (huecosLibres llama a franjasDelProfesional con el id recortado) \| valores limite: rango=COHERENTE (clase valida tipica) |
| `TC12-C02` | `VACIO` | `COHERENTE` | `D60` | `BODY:VACIO` | INVALIDOS `DATOS_INVALIDOS` | profesional en blanco: es la primera guarda del servicio \| profesionalId: profesionalId= en blanco |
| `TC12-C03` | `MEDICO` | `INVERTIDO` | `D60` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: regla de negocio del servicio (IAE -> 400), se comprueba antes de tocar la agenda \| rango: desde=2026-11-14 y hasta=2026-11-12 |
| `TC12-C04` | `ESPACIOS` | `TEXTO_ILEGAL` | `D30` | `BODY:ESPACIOS` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: el TypeMismatch del @RequestParam LocalDate cae al consejo Exception generico (no hay handler propio como en D35) \| rango: desde=hola: ni siquiera convierte a LocalDate |
| `TC12-C05` | `MEDICO` | `AUSENTE` | `AUSENTE` | `SEM-HUECO-FIJO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: MissingServletRequestParameterException de los @RequestParam obligatorios tampoco tiene consejo propio -> 500 \| rango: no se envia hasta (parametro obligatorio) |
| `TC12-C06` | `ESPACIOS` | `COHERENTE` | `CERO` | `BODY:ESPACIOS` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos <= 0 la rechaza huecosLibres (no aplica el defecto del catalogo: aqui no hay solicitud) \| duracion: duracionMinutos=0 |
| `TC12-C07` | `MEDICO` | `IGUAL_DIA` | `NEGATIVA` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | igual que CERO: debe ser positiva \| duracion: duracionMinutos=-5 |
| `TC12-C08` | `MEDICO` | `SOLO_FESTIVO` | `CERO` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos <= 0 la rechaza huecosLibres (no aplica el defecto del catalogo: aqui no hay solicitud) \| duracion: duracionMinutos=0 |
| `TC12-C09` | `SIN_AGENDA` | `COHERENTE` | `NEGATIVA` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | igual que CERO: debe ser positiva \| duracion: duracionMinutos=-5 |
| `TC12-C10` | `ESPACIOS` | `SOLO_FESTIVO` | `NEGATIVA` | `BODY:ESPACIOS` | INVALIDOS `DATOS_INVALIDOS` | igual que CERO: debe ser positiva \| duracion: duracionMinutos=-5 |
| `TC12-C11` | `VACIO` | `SOLO_FESTIVO` | `D30` | `BODY:VACIO` | INVALIDOS `DATOS_INVALIDOS` | profesional en blanco: es la primera guarda del servicio \| profesionalId: profesionalId= en blanco |
| `TC12-C12` | `VACIO` | `IGUAL_DIA` | `AUSENTE` | `BODY:VACIO` | INVALIDOS `DATOS_INVALIDOS` | profesional en blanco: es la primera guarda del servicio \| profesionalId: profesionalId= en blanco |
| `TC12-C13` | `DESCONOCIDO` | `COHERENTE` | `AUSENTE` | `BODY:DESCONOCIDO` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | el id no tiene agenda en CDU13: agenda falsa desconocida -> AgendaNoEncontrado -> FUERA_DE_FRANJA, igual que SIN_AGENDA \| valores limite: rango=COHERENTE (clase valida tipica), duracion=AUSENTE (valor limite: duracion por defecto del contrato) |
| `TC12-C14` | `SIN_AGENDA` | `IGUAL_DIA` | `CERO` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos <= 0 la rechaza huecosLibres (no aplica el defecto del catalogo: aqui no hay solicitud) \| duracion: duracionMinutos=0 |
| `TC12-C15` | `ESPACIOS` | `IGUAL_DIA` | `D60` | `BODY:ESPACIOS` | OK | calendario de huecos en JSON (200), con ESPACIOS el servicio SI recorta profesionalId (huecosLibres llama a franjasDelProfesional con el id recortado) \| valores limite: rango=IGUAL_DIA (valor limite: rango de un solo dia) |
| `TC12-C16` | `DESCONOCIDO` | `IGUAL_DIA` | `D30` | `BODY:DESCONOCIDO` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | el id no tiene agenda en CDU13: agenda falsa desconocida -> AgendaNoEncontrado -> FUERA_DE_FRANJA, igual que SIN_AGENDA \| valores limite: rango=IGUAL_DIA (valor limite: rango de un solo dia) |
| `TC12-C17` | `ESPACIOS` | `INVERTIDO` | `AUSENTE` | `BODY:ESPACIOS` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: regla de negocio del servicio (IAE -> 400), se comprueba antes de tocar la agenda \| rango: desde=2026-11-14 y hasta=2026-11-12 |
| `TC12-C18` | `SIN_AGENDA` | `INVERTIDO` | `D30` | `SEM-HUECO-FIJO` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: regla de negocio del servicio (IAE -> 400), se comprueba antes de tocar la agenda \| rango: desde=2026-11-14 y hasta=2026-11-12 |
| `TC12-C19` | `SIN_AGENDA` | `SOLO_FESTIVO` | `AUSENTE` | `SEM-HUECO-FIJO` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | CDU13 lanza AgendaNoEncontrado y el servicio lo envuelve en FUERA_DE_FRANJA (D36) \| valores limite: rango=SOLO_FESTIVO (valor limite: borde del calendario festivo), duracion=AUSENTE (valor limite: duracion por defecto del contrato) |
| `TC12-C20` | `DESCONOCIDO` | `SOLO_FESTIVO` | `D60` | `BODY:DESCONOCIDO` | FUERA_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | el id no tiene agenda en CDU13: agenda falsa desconocida -> AgendaNoEncontrado -> FUERA_DE_FRANJA, igual que SIN_AGENDA \| valores limite: rango=SOLO_FESTIVO (valor limite: borde del calendario festivo) |
| `TC12-C21` | `SIN_AGENDA` | `TEXTO_ILEGAL` | `D60` | `SEM-HUECO-FIJO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: el TypeMismatch del @RequestParam LocalDate cae al consejo Exception generico (no hay handler propio como en D35) \| rango: desde=hola: ni siquiera convierte a LocalDate |
| `TC12-C22` | `MEDICO` | `TEXTO_ILEGAL` | `AUSENTE` | `SEM-HUECO-FIJO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: el TypeMismatch del @RequestParam LocalDate cae al consejo Exception generico (no hay handler propio como en D35) \| rango: desde=hola: ni siquiera convierte a LocalDate |
| `TC12-C23` | `ESPACIOS` | `AUSENTE` | `D30` | `BODY:ESPACIOS` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: MissingServletRequestParameterException de los @RequestParam obligatorios tampoco tiene consejo propio -> 500 \| rango: no se envia hasta (parametro obligatorio) |
| `TC12-C24` | `SIN_AGENDA` | `AUSENTE` | `D60` | `SEM-HUECO-FIJO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: MissingServletRequestParameterException de los @RequestParam obligatorios tampoco tiene consejo propio -> 500 \| rango: no se envia hasta (parametro obligatorio) |
| `TC12-C25` | `DESCONOCIDO` | `INVERTIDO` | `D30` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: regla de negocio del servicio (IAE -> 400), se comprueba antes de tocar la agenda \| rango: desde=2026-11-14 y hasta=2026-11-12 |
| `TC12-C26` | `DESCONOCIDO` | `TEXTO_ILEGAL` | `D30` | `BODY:DESCONOCIDO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: el TypeMismatch del @RequestParam LocalDate cae al consejo Exception generico (no hay handler propio como en D35) \| rango: desde=hola: ni siquiera convierte a LocalDate |
| `TC12-C27` | `DESCONOCIDO` | `AUSENTE` | `D30` | `BODY:DESCONOCIDO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO: MissingServletRequestParameterException de los @RequestParam obligatorios tampoco tiene consejo propio -> 500 \| rango: no se envia hasta (parametro obligatorio) |
| `TC12-C28` | `DESCONOCIDO` | `COHERENTE` | `CERO` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | duracionMinutos <= 0 la rechaza huecosLibres (no aplica el defecto del catalogo: aqui no hay solicitud) \| duracion: duracionMinutos=0 |
| `TC12-C29` | `DESCONOCIDO` | `COHERENTE` | `NEGATIVA` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | igual que CERO: debe ser positiva \| duracion: duracionMinutos=-5 |

## Tabla D · Listado de citas con filtros (GET /api/citas)

**Modo:** `pairwise` · producto de niveles = 100 · filas generadas = 27 · pares obligatorios cubiertos = 63/63 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** COMP: el listado respeta los filtros del contrato (fecha, profesional, estado) y los fechas mal formadas se rechazan

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `rango` | `COHERENTE` | valido | clase valida tipica | desde=2026-11-12T00:00 hasta=2026-11-14T23:59 |
| `rango` | `IGUAL_DIA` | valido | valor limite: un solo dia | desde=2026-11-13T00:00 hasta=2026-11-13T23:59 |
| `rango` | `INVERTIDO` | **fuera de clase** | valor limite: rango invertido | desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `rango` | `FECHA_CORTA` | **fuera de clase** | clase invalida: formato imposible | desde=2026-11 (truncada) |
| `rango` | `AUSENTE` | valido | valor limite: sin filtro | sin filtros de fecha: todos los dias |
| `estado` | `AUSENTE` | valido | - | sin filtro de estado |
| `estado` | `PROGRAMADA` | valido | - | estado programada (minusculas en la query) |
| `estado` | `CONFIRMADA` | valido | - | no hay citas confirmadas sembradas -> lista vacia legitima |
| `estado` | `DESCONOCIDO` | **fuera de clase** | clase invalida: valor imposible | estado=volando: fuera del enum |
| `profFiltro` | `API_MEDICO` | valido | - | filtra por la agenda con citas |
| `profFiltro` | `AUSENTE` | valido | valor limite: sin filtro | sin filtro de profesional |
| `profFiltro` | `VACIO` | valido | valor limite: vacio equivale a sin filtro (no hay guarda que lo rechace) | profesionalId= en blanco: listar lo trata como sin filtro |
| `profFiltro` | `ESPACIOS` | valido | - | '  api-medico  ': listar recorta el filtro antes de ir al repositorio |
| `profFiltro` | `DESCONOCIDO` | valido | - | profesional sin citas -> lista vacia |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `rango` vale FECHA_CORTA | `ERROR_INTERNO` | `ERROR_INTERNO` |
| 2 | `rango` vale INVERTIDO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `estado` vale DESCONOCIDO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 4 | resto de casos | `OK` | `-` |

### Casos generados

| ID | rango | estado | profFiltro | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC12-D01` | `COHERENTE` | `AUSENTE` | `API_MEDICO` | `SEM-CITA:PROGRAMADA` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=COHERENTE (clase valida tipica) |
| `TC12-D02` | `INVERTIDO` | `AUSENTE` | `AUSENTE` | `SEM-CITA:PROGRAMADA` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: guardia del listado igual que en huecos \| rango: desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `TC12-D03` | `FECHA_CORTA` | `AUSENTE` | `VACIO` | `BODY:VACIO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 2: '2026-11' no casa con el ISO.DATE_TIME del @RequestParam -> TypeMismatchException sin consejo propio, lo absorbe Exception -> 500 \| rango: desde=2026-11 (truncada) |
| `TC12-D04` | `IGUAL_DIA` | `DESCONOCIDO` | `API_MEDICO` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | estado fuera del enum: no se puede convertir \| estado: estado=volando: fuera del enum |
| `TC12-D05` | `IGUAL_DIA` | `AUSENTE` | `ESPACIOS` | `BODY:ESPACIOS` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=IGUAL_DIA (valor limite: un solo dia) |
| `TC12-D06` | `INVERTIDO` | `PROGRAMADA` | `API_MEDICO` | `SEM-CITA:PROGRAMADA` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: guardia del listado igual que en huecos \| rango: desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `TC12-D07` | `FECHA_CORTA` | `CONFIRMADA` | `API_MEDICO` | `SEM-CITA:PROGRAMADA` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 2: '2026-11' no casa con el ISO.DATE_TIME del @RequestParam -> TypeMismatchException sin consejo propio, lo absorbe Exception -> 500 \| rango: desde=2026-11 (truncada) |
| `TC12-D08` | `INVERTIDO` | `CONFIRMADA` | `VACIO` | `BODY:VACIO` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: guardia del listado igual que en huecos \| rango: desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `TC12-D09` | `IGUAL_DIA` | `PROGRAMADA` | `VACIO` | `BODY:VACIO` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=IGUAL_DIA (valor limite: un solo dia), profFiltro=VACIO (valor limite: vacio equivale a sin filtro (no hay guarda que lo rechace)) |
| `TC12-D10` | `IGUAL_DIA` | `CONFIRMADA` | `AUSENTE` | `SEM-CITA:PROGRAMADA` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=IGUAL_DIA (valor limite: un solo dia), profFiltro=AUSENTE (valor limite: sin filtro) |
| `TC12-D11` | `FECHA_CORTA` | `PROGRAMADA` | `AUSENTE` | `SEM-CITA:PROGRAMADA` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 2: '2026-11' no casa con el ISO.DATE_TIME del @RequestParam -> TypeMismatchException sin consejo propio, lo absorbe Exception -> 500 \| rango: desde=2026-11 (truncada) |
| `TC12-D12` | `COHERENTE` | `DESCONOCIDO` | `AUSENTE` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | estado fuera del enum: no se puede convertir \| estado: estado=volando: fuera del enum |
| `TC12-D13` | `AUSENTE` | `DESCONOCIDO` | `VACIO` | `BODY:DESCONOCIDO,BODY:VACIO` | INVALIDOS `DATOS_INVALIDOS` | estado fuera del enum: no se puede convertir \| estado: estado=volando: fuera del enum |
| `TC12-D14` | `COHERENTE` | `PROGRAMADA` | `ESPACIOS` | `BODY:ESPACIOS` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=COHERENTE (clase valida tipica) |
| `TC12-D15` | `COHERENTE` | `CONFIRMADA` | `DESCONOCIDO` | `BODY:DESCONOCIDO` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=COHERENTE (clase valida tipica) |
| `TC12-D16` | `AUSENTE` | `CONFIRMADA` | `ESPACIOS` | `BODY:ESPACIOS` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=AUSENTE (valor limite: sin filtro) |
| `TC12-D17` | `AUSENTE` | `AUSENTE` | `DESCONOCIDO` | `BODY:DESCONOCIDO` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=AUSENTE (valor limite: sin filtro) |
| `TC12-D18` | `AUSENTE` | `PROGRAMADA` | `API_MEDICO` | `SEM-CITA:PROGRAMADA` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=AUSENTE (valor limite: sin filtro) |
| `TC12-D19` | `IGUAL_DIA` | `PROGRAMADA` | `DESCONOCIDO` | `BODY:DESCONOCIDO` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=IGUAL_DIA (valor limite: un solo dia) |
| `TC12-D20` | `COHERENTE` | `AUSENTE` | `VACIO` | `BODY:VACIO` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=COHERENTE (clase valida tipica), profFiltro=VACIO (valor limite: vacio equivale a sin filtro (no hay guarda que lo rechace)) |
| `TC12-D21` | `AUSENTE` | `AUSENTE` | `AUSENTE` | `SEM-CITA:PROGRAMADA` | OK | 200 con la lista filtrada (puede ir vacia), un filtro profesionalId en blanco vale como sin filtro y ESPACIOS se recorta en el servicio \| valores limite: rango=AUSENTE (valor limite: sin filtro), profFiltro=AUSENTE (valor limite: sin filtro) |
| `TC12-D22` | `INVERTIDO` | `AUSENTE` | `ESPACIOS` | `BODY:ESPACIOS` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: guardia del listado igual que en huecos \| rango: desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `TC12-D23` | `INVERTIDO` | `AUSENTE` | `DESCONOCIDO` | `BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | desde > hasta: guardia del listado igual que en huecos \| rango: desde=2026-11-14T00:00 hasta=2026-11-12T00:00 |
| `TC12-D24` | `FECHA_CORTA` | `AUSENTE` | `ESPACIOS` | `BODY:ESPACIOS` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 2: '2026-11' no casa con el ISO.DATE_TIME del @RequestParam -> TypeMismatchException sin consejo propio, lo absorbe Exception -> 500 \| rango: desde=2026-11 (truncada) |
| `TC12-D25` | `FECHA_CORTA` | `AUSENTE` | `DESCONOCIDO` | `BODY:DESCONOCIDO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 2: '2026-11' no casa con el ISO.DATE_TIME del @RequestParam -> TypeMismatchException sin consejo propio, lo absorbe Exception -> 500 \| rango: desde=2026-11 (truncada) |
| `TC12-D26` | `COHERENTE` | `DESCONOCIDO` | `ESPACIOS` | `BODY:DESCONOCIDO,BODY:ESPACIOS` | INVALIDOS `DATOS_INVALIDOS` | estado fuera del enum: no se puede convertir \| estado: estado=volando: fuera del enum |
| `TC12-D27` | `COHERENTE` | `DESCONOCIDO` | `DESCONOCIDO` | `BODY:DESCONOCIDO,BODY:DESCONOCIDO` | INVALIDOS `DATOS_INVALIDOS` | estado fuera del enum: no se puede convertir \| estado: estado=volando: fuera del enum |

## Tabla E · Operaciones de estado de la cita (confirmar, realizar, reprogramar, cancelar, detalle, lote de expiracion)

**Modo:** `filas` · filas escritas a mano = 34

**Puerta que cubre:** SEG integridad: ninguna operacion permite salir de un estado terminal, el plazo de cancelacion manda sobre el hueco pero no sobre la transicion (D36), y el lote de vencidas solo toca citas activas

### Casos generados

| ID | operacion | estadoCita | plazo | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC12-E01` | `CONFIRMAR` | `PROGRAMADA` | `LEJOS` | `SEM-CITA:PROGRAMADA` | OK | transicion feliz programada -> confirmada |
| `TC12-E02` | `CONFIRMAR` | `CONFIRMADA` | `LEJOS` | `SEM-CITA:CONFIRMADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | confirmar solo admite programada: no es idempotente |
| `TC12-E03` | `CONFIRMAR` | `CANCELADA` | `LEJOS` | `SEM-CITA:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal |
| `TC12-E04` | `CONFIRMAR` | `EXPIRADA` | `LEJOS` | `SEM-CITA:EXPIRADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal |
| `TC12-E05` | `CONFIRMAR` | `REALIZADA` | `LEJOS` | `SEM-CITA:REALIZADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal |
| `TC12-E06` | `CONFIRMAR` | `NO_ENCUENTRA` | `-` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el id de la ruta no existe |
| `TC12-E07` | `REALIZAR` | `PROGRAMADA` | `LEJOS` | `SEM-CITA:PROGRAMADA` | OK | realizar admite programada y confirmada |
| `TC12-E08` | `REALIZAR` | `CONFIRMADA` | `LEJOS` | `SEM-CITA:CONFIRMADA` | OK | transicion feliz |
| `TC12-E09` | `REALIZAR` | `CANCELADA` | `LEJOS` | `SEM-CITA:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | no se puede realizar una cita cancelada |
| `TC12-E10` | `REALIZAR` | `EXPIRADA` | `LEJOS` | `SEM-CITA:EXPIRADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | estado terminal |
| `TC12-E11` | `REALIZAR` | `REALIZADA` | `LEJOS` | `SEM-CITA:REALIZADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | realizar no es idempotente |
| `TC12-E12` | `REALIZAR` | `NO_ENCUENTRA` | `-` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | id inexistente |
| `TC12-E13` | `REPROGRAMAR` | `PROGRAMADA` | `LEJOS` | `SEM-CITA:PROGRAMADA` | OK | cuerpo con inicio 2026-11-13T13:00 libre y en franja |
| `TC12-E14` | `REPROGRAMAR` | `CONFIRMADA` | `LEJOS` | `SEM-CITA:CONFIRMADA` | OK | no terminal: tambien se mueve |
| `TC12-E15` | `REPROGRAMAR` | `CANCELADA` | `LEJOS` | `SEM-CITA:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | moverA rechaza el terminal antes de mirar franja y hueco |
| `TC12-E16` | `REPROGRAMAR` | `EXPIRADA` | `LEJOS` | `SEM-CITA:EXPIRADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | terminal |
| `TC12-E17` | `REPROGRAMAR` | `REALIZADA` | `LEJOS` | `SEM-CITA:REALIZADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | terminal |
| `TC12-E18` | `REPROGRAMAR` | `NO_ENCUENTRA` | `-` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | cuerpo valido pero id inexistente |
| `TC12-E19` | `CANCELAR` | `PROGRAMADA` | `LEJOS` | `SEM-CITA:PROGRAMADA` | OK | cuerpo {motivo:...}: cancelacion libre fuera de plazo |
| `TC12-E20` | `CANCELAR` | `CONFIRMADA` | `LEJOS` | `SEM-CITA:CONFIRMADA` | OK | cancelar admite programada y confirmada |
| `TC12-E21` | `CANCELAR` | `CANCELADA` | `LEJOS` | `SEM-CITA:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | cancelar no es idempotente |
| `TC12-E22` | `CANCELAR` | `EXPIRADA` | `LEJOS` | `SEM-CITA:EXPIRADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | terminal |
| `TC12-E23` | `CANCELAR` | `REALIZADA` | `LEJOS` | `SEM-CITA:REALIZADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | terminal |
| `TC12-E24` | `CANCELAR` | `NO_ENCUENTRA` | `-` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | el motivo valido no evita el 404 |
| `TC12-E25` | `CANCELAR` | `PROGRAMADA` | `CERCANO` | `SEM-CITA-CERCANO:PROGRAMADA` | PLAZO `CONFLICTO_PLAZO_CANCELACION` | inicio 2026-11-11T09:00: el reloj 2026-11-11T08:00 pasa del limite inicio-24h |
| `TC12-E26` | `CANCELAR` | `CONFIRMADA` | `CERCANO` | `SEM-CITA-CERCANO:CONFIRMADA` | PLAZO `CONFLICTO_PLAZO_CANCELACION` | el plazo se exige tras cancelar en el dominio (cita ya cancelada en memoria pero la transaccion revierte) |
| `TC12-E27` | `CANCELAR` | `CANCELADA` | `CERCANO` | `SEM-CITA-CERCANO:CANCELADA` | TRANSICION `CONFLICTO_TRANSICION_INVALIDA` | D36: en cancelar gana la TRANSICION sobre el PLAZO |
| `TC12-E28` | `DETALLE` | `PROGRAMADA` | `LEJOS` | `SEM-CITA:PROGRAMADA` | OK | GET /api/citas/{id} con cuerpo CitaDTO |
| `TC12-E29` | `DETALLE` | `NO_ENCUENTRA` | `-` | `NINGUNO` | NO_ENCONTRADA `CITA_NO_ENCONTRADA` | 404 con cuerpo ApiError |
| `TC12-E30` | `EXPIRAR_LOTE` | `PROGRAMADA` | `LEJOS` | `SEM-CITA-VENCIDA:PROGRAMADA` | OK | cuerpo {corte:2026-11-13T00:00} y fin 2026-11-12T09:30 antes del corte: se expira y libera el hueco |
| `TC12-E31` | `EXPIRAR_LOTE` | `CONFIRMADA` | `LEJOS` | `SEM-CITA-VENCIDA:CONFIRMADA` | OK | confirmada tambien es activa: entra en el lote |
| `TC12-E32` | `EXPIRAR_LOTE` | `CANCELADA` | `LEJOS` | `SEM-CITA-VENCIDA:CANCELADA` | OK | no esta en ESTADOS_ACTIVOS: lote vacio 200 |
| `TC12-E33` | `EXPIRAR_LOTE` | `EXPIRADA` | `LEJOS` | `SEM-CITA-VENCIDA:EXPIRADA` | OK | terminal: no vuelve a expirarse |
| `TC12-E34` | `EXPIRAR_LOTE` | `REALIZADA` | `LEJOS` | `SEM-CITA-VENCIDA:REALIZADA` | OK | realizada no es activa: lote vacio |

## Tabla F · Conjetura de errores y rincones del contrato

**Modo:** `conjetura` · filas escritas a mano = 13

**Puerta que cubre:** AF rincones: cuerpos degenerados, tipos imposibles, rutas sin mapear y asimetrias leidas del codigo; el paso rojo sobre la rama confirma o corrige cada esperado

### Casos generados

| ID | operacion | cuerpo | rutaExtra | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC12-F01` | `AGENDAR` | `VACIO` | `-` | `NINGUNO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | @RequestBody obligatorio sin cuerpo: HttpMessageNotReadable con consejo propio (no es el 415/400 sin cuerpo de otras CDU) |
| `TC12-F02` | `AGENDAR` | `JSON_NULO` | `-` | `SEM-SOL:CREADA` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | literal null: Jackson materializa null y el consejo cuerpoIlegible responde 400 |
| `TC12-F03` | `AGENDAR` | `JSON_SIN_DURACION` | `-` | `SEM-SOL:CREADA` | CREADO | int primitivo sin clave = 0, y el servicio usa el defecto del catalogo (30 min para api-prueba): 201 (HALLAZGO 8) |
| `TC12-F04` | `AGENDAR` | `JSON_DUR_CERO` | `-` | `SEM-SOL:CREADA` | CREADO | duracionMinutos:0 explicito cae al mismo defecto del catalogo: 201 |
| `TC12-F05` | `AGENDAR` | `JSON_DUR_NEGATIVA` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | -5 pasa el record pero la guarda duracion<0 del servicio no (borde bajo) |
| `TC12-F06` | `AGENDAR` | `JSON_SIN_SOL` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | sin solicitud no hay defecto de catalogo que aplicar: duracion 0 sin fuente -> 400 |
| `TC12-F07` | `AGENDAR` | `JSON_PROF_ESPACIOS` | `inicio 2026-11-13T12:00` | `NINGUNO` | CREADO | leido del dominio: Cita.nueva SI recorta profesionalId, asi que '  api-medico  ' encuentra agenda y la alta es valida (misma clase que huecosLibres, sin asimetria) |
| `TC12-F08` | `AGENDAR` | `JSON_SOL_ESPACIOS` | `inicio 2026-11-13T12:00` | `NINGUNO` | CREADO | HALLAZGO 4: solicitudId ' ' se convierte en null: la cita se crea sin solicitud y CDU06 no se consulta |
| `TC12-F09` | `REPROGRAMAR` | `PROPIO_HUECO` | `inicio = el actual de la cita` | `SEM-CITA:PROGRAMADA` | OK | exigeHuecoLibre excluye la cita propia: moverse al mismo hueco es legal |
| `TC12-F10` | `CANCELAR` | `JSON_MOTIVO_VACIO` | `id inexistente` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el motivo en blanco se exige ANTES de buscar la cita: 400 gana a 404 |
| `TC12-F11` | `AGENDAR` | `TEXT_PLANO` | `Content-Type text/plain` | `NINGUNO` | ERROR_INTERNO `ERROR_INTERNO` | HALLAZGO 1 predicho: no hay consejo HttpMediaTypeNotSupportedException en CDU12 -> lo absorbe Exception y devuelve 500 (rojo confirma) |
| `TC12-F12` | `AUDITORIA` | `NINGUNO` | `GET /api/citas/{id}/auditoria` | `SEM-CITA:PROGRAMADA` | RUTA_INEXISTENTE | HALLAZGO 6: no hay subrecurso de historial (TC12-05 sin endpoint): 404 de Spring sin cuerpo ApiError, si cayera al consejo generico seria 500 ERROR_INTERNO |
| `TC12-F13` | `BAJA` | `NINGUNO` | `DELETE /api/citas/{id}` | `SEM-CITA:PROGRAMADA` | METODO_NO_PERMITIDO | no hay DELETE de citas, pero /api/citas/{id} si esta mapeado para GET: Spring resuelve 405 antes de entrar al handler, asi que el consejo Exception no lo ve y el cuerpo no es ApiError (HALLAZGO 6 confirmado por la rojo) |

## Como se ejecutan

```
mvn -B -ntp -pl iso2-citas -am test
```

Los CSV de trabajo estan en `docs/test/iter05/` y su copia en el classpath de pruebas (`iso2-citas/src/test/resources/tdop/`) es identica: el generador las escribe a la vez.

