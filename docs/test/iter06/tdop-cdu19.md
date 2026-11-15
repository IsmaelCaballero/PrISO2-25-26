# Evidencia TDOP de CDU19 (iter06 · CFG-06)

Metodo: Clases de equivalencia + teorema de valores limite + conjetura de errores, compuestos por parejas (todo par de niveles de factores distintos queda cubierto) y enumeracion escrita a mano para las secuencias.

Generador: `python tools/pairwise.py docs/test/iter06/tdop-cdu19-espec.json --resources iso2-integracion/src/test/resources/tdop` (semilla 20261115).
Las mismas filas las ejecutan los JUnit parametrizados de `es.uclm.iso2.integracion.tdop` y las tablas de `docs/tc/cdu19-tc-humanos.md`: un ID vale para las dos.

## Leyenda de tokens

| Token | Significado |
|---|---|
| `SIM-<sistema>-<modo>-<lat>-<timeout>-<reintentos>` | setup inyectado en el RegistroSimulacion del contexto antes de la fila (no por HTTP); seis partes separadas por guiones, el modo puede llevar guion bajo |
| `NORMAL` | politica inocua: el adaptador responde lo que se pida |
| `CAIDO` | el adaptador falla con SISTEMA_NO_DISPONIBLE sin mirar la peticion (precede a 400/401/404) |
| `LENTO` | duerme la latencia pero dentro del plazo: la llamada acaba bien |
| `LENTO_TIMEOUT` | latencia mayor que el tiempo de espera: SISTEMA_TIEMPO_SUPERADO tras 1 + reintentos intentos |
| `AUT` | valor autocoadyuvante, siempre dentro de clase (api-paciente-ok, ACC-0001, hce, idp-admin) |
| `VACIO` | cadena vacia o solo espacios en un campo obligatorio |
| `AUSENTE` | se omite la clave del cuerpo o el segmento de ruta |
| `RODEADO` | identificador valido con espacios delante y detras: recortar lo deja reconocible |
| `ROJOS` | reservado: las casillas que la fase rojo aun no ha confirmado |

## Estados esperados (nombre simbolico -> HTTP)

| Estado | HTTP | Codigo de error | Que significa |
|---|---|---|---|
| `OK` | 200 | `-` | peticion atendida con cuerpo del contrato (historia, sesion, estado de sistema, estudio o politica) |
| `INVALIDOS` | 400 | `DATOS_INVALIDOS` | identificador en blanco, modo desconocido o cifras ausentes/negativas: los IAE del dominio y de los controladores tienen consejo propio |
| `CUERPO_ILEGIBLE` | 400 | `DATOS_INVALIDOS` | el cuerpo JSON no se puede leer o no se puede convertir: HttpMessageNotReadableException -> 400 con cuerpo ApiError |
| `CREDENCIALES` | 401 | `CREDENCIALES_INVALIDAS` | el IdP simulado rechaza el par usuario/contrasena (solo con credenciales completas: si falta alguna es 400) |
| `NO_ENCONTRADA` | 404 | `HISTORIA_NO_ENCONTRADA` | el paciente no existe en la HCE simulada |
| `ESTUDIO_PERDIDO` | 404 | `ESTUDIO_NO_ENCONTRADO` | el numero de acceso no esta entre ACC-0001 y ACC-0004 del RIS simulado |
| `SISTEMA_DESCONOCIDO` | 404 | `SISTEMA_NO_ENCONTRADO` | el codigo de la ruta o del cuerpo de la simulacion no es sso, hce, ris ni pacs |
| `RUTA_INEXISTENTE` | 404 | `-` | Spring resuelve la ruta antes de llamar al controlador: el consejo del modulo no interviene y no hay cuerpo ApiError |
| `CAIDO` | 503 | `SISTEMA_NO_DISPONIBLE` | el sistema externo esta en modo CAIDO: el adaptador lo comprueba antes de mirar la peticion |
| `LENTO_TIMEOUT` | 503 | `SISTEMA_TIEMPO_SUPERADO` | la latencia simulada desborda el tiempo de espera y la llamada real jamas se ejecuta |
| `TIPO_ERRONEO` | 400 | `DATOS_INVALIDOS` | la clave numerica llega con un valor que no se puede convertir a long/int: la conversacion la absorbe HttpMessageNotReadableException |
| `CONTENIDO` | 415 | `CONTENIDO_NO_ACEPTADO` | Content-Type no JSON en un endpoint con @RequestBody: HttpMediaTypeNotSupportedException -> 415 con cuerpo ApiError |
| `METODO` | 405 | `-` | metodo no mapeado o ruta heredada del contrato que el CDU19 no implementa: lo resuelve Spring y no pasa por el consejo del modulo |

## Reparto de casos

| Tabla | Operacion | Modo | Filas |
|---|---|---|---|
| A | Sincronizar historia clinica externa (POST /api/integracion/historias/{pacienteExternoId}/sincronizar) | pairwise | 21 |
| B | Idempotencia de la sincronizacion (secuencias de POST sobre el mismo paciente) | filas | 7 |
| C | Login SSO contra el IdP simulado (POST /api/integracion/sso/login) | pairwise | 15 |
| D | Importar estudio del RIS (GET /api/integracion/estudios/{accessionNumber}) | pairwise | 12 |
| E | Ping, listado de sistemas y cadena de auditoria (monitorizacion del spike) | filas | 17 |
| F | Control de la simulacion (POST /api/integracion/simulacion) | pairwise | 40 |
| G | Conjetura de errores y rincones del contrato de integracion | conjetura | 14 |
| **Total** |  |  | **126** |

## Tabla A · Sincronizar historia clinica externa (POST /api/integracion/historias/{pacienteExternoId}/sincronizar)

**Modo:** `pairwise` · producto de niveles = 21 · filas generadas = 21 · pares obligatorios cubiertos = 21/21 (100%)

**Puerta que cubre:** SEG-INT-01 sincronizacion de la historia externa: la politica del sistema manda sobre la peticion (503 > 400 > 404) y la ruta vacia escapa al controlador

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `paciente` | `AUT` | valido | - | - |
| `paciente` | `ROTATIVO` | valido | - | - |
| `paciente` | `RODEADO` | valido | - | - |
| `paciente` | `VACIO` | **fuera de clase** | - | id de solo espacios: tras recortar queda vacio y el adaptador exige identificador |
| `paciente` | `AUSENTE` | **fuera de clase** | - | segmento de ruta suprimido: la ruta queda en /historias/sincronizar y ningun handler la atiende |
| `paciente` | `DESCONOCIDO` | **fuera de clase** | - | el paciente no esta registrado en el HCE simulado |
| `paciente` | `LARGO` | **fuera de clase** | - | identificador de 80 caracteres |
| `politica` | `NORMAL` | valido | - | - |
| `politica` | `CAIDO` | valido | - | - |
| `politica` | `LENTO_TIMEOUT` | valido | latencia 250 supera el plazo de 100 | - |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `paciente` vale AUSENTE | `RUTA_INEXISTENTE` | `-` |
| 2 | `politica` vale CAIDO | `CAIDO` | `SISTEMA_NO_DISPONIBLE` |
| 3 | `politica` vale LENTO_TIMEOUT | `LENTO_TIMEOUT` | `SISTEMA_TIEMPO_SUPERADO` |
| 4 | `paciente` vale VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `paciente` vale DESCONOCIDO | `NO_ENCONTRADA` | `HISTORIA_NO_ENCONTRADA` |
| 6 | `paciente` vale LARGO | `NO_ENCONTRADA` | `HISTORIA_NO_ENCONTRADA` |
| 7 | resto de casos | `OK` | `-` |

### Casos generados

| ID | paciente | politica | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|
| `TC19-A01` | `AUT` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | OK | historia sincronizada con su huella sha256 y sus registros, la segunda vez sobre el mismo contenido seria reutilizado=true (lo comprueba la tabla B) |
| `TC19-A02` | `AUT` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase |
| `TC19-A03` | `AUT` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| valores limite: politica=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-A04` | `ROTATIVO` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | OK | historia sincronizada con su huella sha256 y sus registros, la segunda vez sobre el mismo contenido seria reutilizado=true (lo comprueba la tabla B) |
| `TC19-A05` | `ROTATIVO` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase |
| `TC19-A06` | `ROTATIVO` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| valores limite: politica=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-A07` | `RODEADO` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | OK | historia sincronizada con su huella sha256 y sus registros, la segunda vez sobre el mismo contenido seria reutilizado=true (lo comprueba la tabla B) |
| `TC19-A08` | `RODEADO` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase |
| `TC19-A09` | `RODEADO` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| valores limite: politica=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-A10` | `VACIO` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | INVALIDOS `DATOS_INVALIDOS` | recortar deja el id vacio y el adaptador lo exige antes de buscar: 400 por delante del 404 \| paciente: id de solo espacios: tras recortar queda vacio y el adaptador exige identificador |
| `TC19-A11` | `VACIO` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase \| paciente: id de solo espacios: tras recortar queda vacio y el adaptador exige identificador |
| `TC19-A12` | `VACIO` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| paciente: id de solo espacios: tras recortar queda vacio y el adaptador exige identificador |
| `TC19-A13` | `AUSENTE` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | RUTA_INEXISTENTE | el segmento de ruta suprimido deja la peticion en /historias/sincronizar, que no coincide con /historias/{pacienteExternoId}/sincronizar: el 404 de Spring sin ApiError gana incluso a un HCE caido, porque la politica jamas llega a consultarse \| paciente: segmento de ruta suprimido: la ruta queda en /historias/sincronizar y ningun handler la atiende |
| `TC19-A14` | `AUSENTE` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | RUTA_INEXISTENTE | el segmento de ruta suprimido deja la peticion en /historias/sincronizar, que no coincide con /historias/{pacienteExternoId}/sincronizar: el 404 de Spring sin ApiError gana incluso a un HCE caido, porque la politica jamas llega a consultarse \| paciente: segmento de ruta suprimido: la ruta queda en /historias/sincronizar y ningun handler la atiende |
| `TC19-A15` | `AUSENTE` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | RUTA_INEXISTENTE | el segmento de ruta suprimido deja la peticion en /historias/sincronizar, que no coincide con /historias/{pacienteExternoId}/sincronizar: el 404 de Spring sin ApiError gana incluso a un HCE caido, porque la politica jamas llega a consultarse \| paciente: segmento de ruta suprimido: la ruta queda en /historias/sincronizar y ningun handler la atiende |
| `TC19-A16` | `DESCONOCIDO` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | NO_ENCONTRADA `HISTORIA_NO_ENCONTRADA` | el HCE simulado solo conoce api-paciente-ok y api-paciente-rotativo \| paciente: el paciente no esta registrado en el HCE simulado |
| `TC19-A17` | `DESCONOCIDO` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase \| paciente: el paciente no esta registrado en el HCE simulado |
| `TC19-A18` | `DESCONOCIDO` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| paciente: el paciente no esta registrado en el HCE simulado |
| `TC19-A19` | `LARGO` | `NORMAL` | `SIM-HCE-NORMAL-5-300-2` | NO_ENCONTRADA `HISTORIA_NO_ENCONTRADA` | no hay cota de longitud en la fachada: el identificador largo se busca igual y no esta \| paciente: identificador de 80 caracteres |
| `TC19-A20` | `LARGO` | `CAIDO` | `SIM-HCE-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() lee la politica antes de mirar el identificador: el fallo del sistema gana a la entrada fuera de clase \| paciente: identificador de 80 caracteres |
| `TC19-A21` | `LARGO` | `LENTO_TIMEOUT` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la llamada real nunca se ejecuta: 503 en lugar del 400 o 404 que daria la peticion \| paciente: identificador de 80 caracteres |

## Tabla B · Idempotencia de la sincronizacion (secuencias de POST sobre el mismo paciente)

**Modo:** `filas` · filas escritas a mano = 7

**Puerta que cubre:** SEG-INT-01 reutilizacion de la historia ya sincronizada: la instantanea vive en la fachada, no en el sistema externo

### Casos generados

| ID | paciente | llamadas | setupEntre | esperadoReutilizado | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|
| `TC19-B01` | `AUT` | `sincronizar,sincronizar` | `-` | `SI` | `-` | OK | la primera sincronizacion crea la instantanea y la segunda la reutiliza: mismo huella sha256 y mismos registros |
| `TC19-B02` | `AUT` | `sincronizar,sincronizar,sincronizar` | `-` | `SI` | `-` | OK | la tercera llamada sobre contenido estable sigue reutilizando: la instantanea no se degrada |
| `TC19-B03` | `ROTATIVO` | `sincronizar,sincronizar` | `-` | `NO` | `-` | OK | el HCE alternante cambia el contenido cada llamada: la huella nueva obliga a registrar una instantanea distinta |
| `TC19-B04` | `AUT` | `sincronizar,sincronizar` | `SIM-HCE-CAIDO-5-300-0` | `-` | `-` | CAIDO `SISTEMA_NO_DISPONIBLE` | HALLAZGO: la fachada consulta al adaptador antes que a su propia instantanea, asi que con la HCE caida no se sirve la copia local (503 en la segunda llamada) |
| `TC19-B05` | `AUT` | `sincronizar,sincronizar` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | `-` | `-` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | igual con el tiempo de espera superado: la reutilizacion no protege del fallo del sistema externo |
| `TC19-B06` | `AUT` | `sincronizar,sincronizar` | `SIM-HCE-LENTO-20-300-1` | `SI` | `-` | OK | HCE lenta dentro del plazo: la reutilizacion se mantiene (teorema de valores limite con latencia 20 y plazo 300) |
| `TC19-B07` | `DESCONOCIDO` | `sincronizar` | `-` | `-` | `-` | NO_ENCONTRADA `HISTORIA_NO_ENCONTRADA` | sin instantanea previa tampoco se puede reutilizar, pero la celda va en guion porque el 404 responde con ApiError, que no tiene campo reutilizado, el equivalente con cuerpo de contrato (reutilizado=false) lo cubre la fila de ROTATIVO |

## Tabla C · Login SSO contra el IdP simulado (POST /api/integracion/sso/login)

**Modo:** `pairwise` · producto de niveles = 40 · filas generadas = 15 · pares obligatorios cubiertos = 26/26 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** SEG-INT-02 login contra el IdP externo simulado: la politica del IdP manda sobre la credencial (503 > 401 > 400)

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `usuario` | `AUT` | valido | - | - |
| `usuario` | `MALO` | **fuera de clase** | - | el usuario no esta en el padron del IdP simulado |
| `usuario` | `VACIO` | **fuera de clase** | - | usuario vacio: la guarda del adaptador lo exige |
| `usuario` | `ESPACIOS` | **fuera de clase** | - | usuario de solo espacios: recortar lo deja vacio |
| `usuario` | `LARGO` | **fuera de clase** | - | usuario de 80 caracteres |
| `contrasena` | `AUT` | valido | - | - |
| `contrasena` | `MALA` | **fuera de clase** | - | la contrasena no coincide con la del padron |
| `contrasena` | `VACIA` | **fuera de clase** | - | contrasena en blanco |
| `contrasena` | `AUSENTE` | **fuera de clase** | - | se omite la clave contrasena del cuerpo: llega null |
| `idp` | `NORMAL` | valido | - | - |
| `idp` | `CAIDO` | valido | - | - |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `idp` vale CAIDO | `CAIDO` | `SISTEMA_NO_DISPONIBLE` |
| 2 | `usuario` vale VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `usuario` vale ESPACIOS | `INVALIDOS` | `DATOS_INVALIDOS` |
| 4 | `contrasena` vale VACIA | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `contrasena` vale AUSENTE | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `contrasena` vale MALA | `CREDENCIALES` | `CREDENCIALES_INVALIDAS` |
| 7 | `usuario` vale MALO | `CREDENCIALES` | `CREDENCIALES_INVALIDAS` |
| 8 | `usuario` vale LARGO | `CREDENCIALES` | `CREDENCIALES_INVALIDAS` |
| 9 | resto de casos | `OK` | `-` |

### Casos generados

| ID | usuario | contrasena | idp | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC19-C01` | `AUT` | `AUT` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | OK | sesion SSO con sujeto, rol externo radiologia y caducidad, la sesion es informativa y no sustituye al JWT de CDU17 (hallazgo del spike) |
| `TC19-C02` | `MALO` | `AUT` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| usuario: el usuario no esta en el padron del IdP simulado |
| `TC19-C03` | `VACIO` | `AUT` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | INVALIDOS `DATOS_INVALIDOS` | la guarda del adaptador exige usuario y contrasena antes de buscar en el padron \| usuario: usuario vacio: la guarda del adaptador lo exige |
| `TC19-C04` | `ESPACIOS` | `AUT` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | INVALIDOS `DATOS_INVALIDOS` | recortar deja el usuario vacio: misma salida que la anterior \| usuario: usuario de solo espacios: recortar lo deja vacio |
| `TC19-C05` | `LARGO` | `AUT` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | CREDENCIALES `CREDENCIALES_INVALIDAS` | el identificador largo no esta en el padron: no hay cota de longitud que lo rechace antes \| usuario: usuario de 80 caracteres |
| `TC19-C06` | `AUT` | `MALA` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| contrasena: la contrasena no coincide con la del padron |
| `TC19-C07` | `AUT` | `VACIA` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | INVALIDOS `DATOS_INVALIDOS` | contrasena en blanco: la misma guarda \| contrasena: contrasena en blanco |
| `TC19-C08` | `AUT` | `AUSENTE` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | INVALIDOS `DATOS_INVALIDOS` | la clave ausente llega como null y la guarda cubre null y vacio \| contrasena: se omite la clave contrasena del cuerpo: llega null |
| `TC19-C09` | `MALO` | `AUT` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | CREDENCIALES `CREDENCIALES_INVALIDAS` | el usuario no esta en el padron: mismo 401, sin revelar si existe o no \| usuario: el usuario no esta en el padron del IdP simulado |
| `TC19-C10` | `AUT` | `MALA` | `NORMAL` | `SIM-SSO-NORMAL-5-300-2` | CREDENCIALES `CREDENCIALES_INVALIDAS` | credenciales completas pero erroneas: el IdP simulado responde CredencialesInvalidasException -> 401 \| contrasena: la contrasena no coincide con la del padron |
| `TC19-C11` | `VACIO` | `AUT` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| usuario: usuario vacio: la guarda del adaptador lo exige |
| `TC19-C12` | `ESPACIOS` | `AUT` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| usuario: usuario de solo espacios: recortar lo deja vacio |
| `TC19-C13` | `LARGO` | `AUT` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| usuario: usuario de 80 caracteres |
| `TC19-C14` | `AUT` | `VACIA` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| contrasena: contrasena en blanco |
| `TC19-C15` | `AUT` | `AUSENTE` | `CAIDO` | `SIM-SSO-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() del SsoAdaptadorSimulado lee la politica antes de la guarda de credenciales: el fallo del IdP gana a un 400 o un 401 \| contrasena: se omite la clave contrasena del cuerpo: llega null |

## Tabla D · Importar estudio del RIS (GET /api/integracion/estudios/{accessionNumber})

**Modo:** `pairwise` · producto de niveles = 12 · filas generadas = 12 · pares obligatorios cubiertos = 12/12 (100%)

**Puerta que cubre:** SEG-INT-03 importar estudio del RIS por numero de acceso: es el unico metodo congelado del contrato y no lleva sistema en la ruta

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `acceso` | `AUT` | valido | - | - |
| `acceso` | `MINUSCULA` | valido | - | - |
| `acceso` | `RODEADO` | valido | - | - |
| `acceso` | `INEXISTENTE` | **fuera de clase** | - | ACC-9999 no esta entre ACC-0001 y ACC-0004 del RIS simulado |
| `ris` | `NORMAL` | valido | - | - |
| `ris` | `CAIDO` | valido | - | - |
| `ris` | `LENTO_TIMEOUT` | valido | latencia 250 supera el plazo de 100 | - |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `ris` vale CAIDO | `CAIDO` | `SISTEMA_NO_DISPONIBLE` |
| 2 | `ris` vale LENTO_TIMEOUT | `LENTO_TIMEOUT` | `SISTEMA_TIEMPO_SUPERADO` |
| 3 | `acceso` vale INEXISTENTE | `ESTUDIO_PERDIDO` | `ESTUDIO_NO_ENCONTRADO` |
| 4 | resto de casos | `OK` | `-` |

### Casos generados

| ID | acceso | ris | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|
| `TC19-D01` | `AUT` | `NORMAL` | `SIM-RIS-NORMAL-5-300-2` | OK | estudio importado con accession, paciente, fecha y numero de series: es el contrato base heredado |
| `TC19-D02` | `AUT` | `CAIDO` | `SIM-RIS-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() mira la politica antes de mirar el numero de acceso: un RIS caido responde 503 aunque el estudio exista o no |
| `TC19-D03` | `AUT` | `LENTO_TIMEOUT` | `SIM-RIS-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la busqueda jamas llega a ejecutarse \| valores limite: ris=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-D04` | `MINUSCULA` | `NORMAL` | `SIM-RIS-NORMAL-5-300-2` | OK | estudio importado con accession, paciente, fecha y numero de series: es el contrato base heredado |
| `TC19-D05` | `MINUSCULA` | `CAIDO` | `SIM-RIS-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() mira la politica antes de mirar el numero de acceso: un RIS caido responde 503 aunque el estudio exista o no |
| `TC19-D06` | `MINUSCULA` | `LENTO_TIMEOUT` | `SIM-RIS-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la busqueda jamas llega a ejecutarse \| valores limite: ris=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-D07` | `RODEADO` | `NORMAL` | `SIM-RIS-NORMAL-5-300-2` | OK | estudio importado con accession, paciente, fecha y numero de series: es el contrato base heredado |
| `TC19-D08` | `RODEADO` | `CAIDO` | `SIM-RIS-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() mira la politica antes de mirar el numero de acceso: un RIS caido responde 503 aunque el estudio exista o no |
| `TC19-D09` | `RODEADO` | `LENTO_TIMEOUT` | `SIM-RIS-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la busqueda jamas llega a ejecutarse \| valores limite: ris=LENTO_TIMEOUT (latencia 250 supera el plazo de 100) |
| `TC19-D10` | `INEXISTENTE` | `NORMAL` | `SIM-RIS-NORMAL-5-300-2` | ESTUDIO_PERDIDO `ESTUDIO_NO_ENCONTRADO` | el RIS simulado solo tiene cuatro estudios dados de alta \| acceso: ACC-9999 no esta entre ACC-0001 y ACC-0004 del RIS simulado |
| `TC19-D11` | `INEXISTENTE` | `CAIDO` | `SIM-RIS-CAIDO-5-300-0` | CAIDO `SISTEMA_NO_DISPONIBLE` | proteger() mira la politica antes de mirar el numero de acceso: un RIS caido responde 503 aunque el estudio exista o no \| acceso: ACC-9999 no esta entre ACC-0001 y ACC-0004 del RIS simulado |
| `TC19-D12` | `INEXISTENTE` | `LENTO_TIMEOUT` | `SIM-RIS-LENTO_TIMEOUT-250-100-0` | LENTO_TIMEOUT `SISTEMA_TIEMPO_SUPERADO` | la latencia desborda el plazo y la busqueda jamas llega a ejecutarse \| acceso: ACC-9999 no esta entre ACC-0001 y ACC-0004 del RIS simulado |

## Tabla E · Ping, listado de sistemas y cadena de auditoria (monitorizacion del spike)

**Modo:** `filas` · filas escritas a mano = 17

**Puerta que cubre:** SEG-INT-04 monitorizacion: un sistema caido se responde 200 con disponible=false, jamas un 5xx, y la auditoria registra la cadena hash

### Casos generados

| ID | operacion | sistema | previas | esperadoExtra | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|
| `TC19-E01` | `PING` | `HCE_MIN` | `-` | `DISPONIBLE_SI` | `SIM-HCE-NORMAL-5-300-2` | OK | sistema operativo: devuelve disponible=true y la media movil de latencias de los intentos registrados |
| `TC19-E02` | `PING` | `HCE_MIN` | `-` | `DISPONIBLE_NO_CAIDO` | `SIM-HCE-CAIDO-5-300-0` | OK | HCE caida: 200 con disponible=false y motivo CAIDO, AdaptadorSimulado.estado jamas lanza, por eso el ping no puede acabar en 5xx ni llevar codigo de error (el 503 SISTEMA_NO_DISPONIBLE de la misma politica lo da sincronizar, lo comprueba la tabla A) |
| `TC19-E03` | `PING` | `HCE_MIN` | `-` | `DISPONIBLE_NO_TIEMPO_SUPERADO` | `SIM-HCE-LENTO_TIMEOUT-250-100-0` | OK | degradacion por tiempo de espera: el ping informa el fallo con disponible=false y motivo TIEMPO_SUPERADO en lugar de propagar el 503 SISTEMA_TIEMPO_SUPERADO |
| `TC19-E04` | `PING` | `HCE_MIN` | `-` | `DISPONIBLE_SI` | `SIM-HCE-LENTO-20-300-1` | OK | lento dentro del plazo (latencia 20 < plazo 300): sigue disponible, es el limite del teorema de valores limite de la politica |
| `TC19-E05` | `PING` | `PACS_MIN` | `-` | `DISPONIBLE_NO_CAIDO` | `SIM-PACS-CAIDO-5-300-0` | OK | caer el PACS no arrastra a los demas sistemas: cada adaptador tiene su politica. El ping de un sistema caido sigue respondiendo 200 con disponible=false y motivo CAIDO: el degradado se informa, nunca se propaga como 5xx |
| `TC19-E06` | `PING` | `SSO_MIN` | `-` | `DISPONIBLE_NO_CAIDO` | `SIM-SSO-CAIDO-5-300-0` | OK | el IdP caido se ve en el ping aunque la API de ISO2 siga funcionando con su JWT de CDU17 |
| `TC19-E07` | `PING` | `RIS_MIN` | `-` | `DISPONIBLE_SI` | `SIM-RIS-NORMAL-5-300-2` | OK | ping del RIS sin llamadas previas: latencia media sin muestra |
| `TC19-E08` | `PING` | `MAYUSCULAS` | `-` | `DISPONIBLE_SI` | `SIM-HCE-NORMAL-5-300-2` | OK | codigo en mayusculas: SistemaExterno.desdeCodigo las tolera, la API no obliga a la minuscula |
| `TC19-E09` | `PING` | `RODEADO` | `-` | `DISPONIBLE_SI` | `SIM-HCE-NORMAL-5-300-2` | OK | codigo con espacios delante y detras: desdeCodigo recorta antes de buscar |
| `TC19-E10` | `PING` | `VACIO` | `-` | `-` | `SIM-HCE-NORMAL-5-300-2` | RUTA_INEXISTENTE | HALLAZGO del paso rojo: el alias VACIO se representa como segmento vacio, asi que la peticion queda en /api/integracion/sistemas//ping y ningun patron de /sistemas/{sistema}/ping la atiende: el 404 lo resuelve Spring sin cuerpo ApiError, igual que con el segmento suprimido (TC19-E12). La clase 'codigo de sistema en blanco' sigue cubierta por TC19-E17, que usa un segmento no vacio de solo espacios y si llega al controlador |
| `TC19-E11` | `PING` | `DESCONOCIDO` | `-` | `-` | `SIM-HCE-NORMAL-5-300-2` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | nombre de sistema que no es sso, hce, ris ni pacs |
| `TC19-E12` | `PING` | `AUSENTE` | `-` | `-` | `SIM-HCE-NORMAL-5-300-2` | RUTA_INEXISTENTE | segmento de ruta suprimido: la peticion se arma como /sistemas/ping, ninguna mano del controlador la atiende y Spring responde 404 sin cuerpo ApiError. Si PathPattern tratara el tramo final como variable vacia, la respuesta pasaria a 404 con ApiError SISTEMA_NO_ENCONTRADO: el paso rojo sobre la rama decide |
| `TC19-E13` | `SISTEMAS` | `-` | `-` | `TODOS_DISPONIBLES` | `SIM-SSO-NORMAL-5-300-2` | OK | listado de los cuatro sistemas en orden sso, hce, ris, pacs, todos disponibles |
| `TC19-E14` | `SISTEMAS` | `-` | `-` | `MIXTO` | `SIM-HCE-CAIDO-5-300-0` | OK | la politica de un solo sistema no contamina al resto: solo HCE aparece indisponible en el listado |
| `TC19-E15` | `AUDITORIA` | `-` | `sso,sincronizar` | `CADENA_INTEGRA` | `SIM-HCE-NORMAL-5-300-2` | OK | tras un login y una sincronizacion felices la cadena trae eslabones, ultimoHash y cadenaIntegra=true, el ping no se audita |
| `TC19-E16` | `AUDITORIA` | `-` | `-` | `CADENA_VACIA` | `SIM-HCE-NORMAL-5-300-2` | OK | sin llamadas previas: cadena integra y sin registros (el reset del BeforeEach deja la auditoria del subdominio limpia) |
| `TC19-E17` | `PING` | `ESPACIOS` | `-` | `-` | `SIM-HCE-NORMAL-5-300-2` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | codigo de sistema de solo espacios: el segmento existe, la ruta enruta y desdeCodigo lo deja vacio al recortar, asi que el ping responde 404 con ApiError SISTEMA_NO_ENCONTRADO. Es la prueba de que la clase en blanco es observable: el control del simulacion rechaza el mismo valor con 400 porque su guarda mira el blanco antes de resolver el codigo (tabla F) |

## Tabla F · Control de la simulacion (POST /api/integracion/simulacion)

**Modo:** `pairwise` · producto de niveles = 240 · filas generadas = 40 · pares obligatorios cubiertos = 87/87 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** SEG-INT-05 cambiar el modo de fallo en caliente: la guarda del controlador (400 por sistema en blanco) manda sobre la resolucion del sistema (404), y el record PoliticaSimulacion rechaza las cifras negativas

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `sistema` | `HCE_MIN` | valido | - | - |
| `sistema` | `RIS_MIN` | valido | - | - |
| `sistema` | `VACIO` | **fuera de clase** | - | sistema vacio: la guarda del controlador lo exige |
| `sistema` | `ESPACIOS` | **fuera de clase** | - | sistema de solo espacios |
| `sistema` | `DESCONOCIDO` | **fuera de clase** | - | sap no es un sistema del CDU19 |
| `modo` | `NORMAL_MINUS` | valido | - | - |
| `modo` | `NORMAL` | valido | - | - |
| `modo` | `LENTO` | valido | - | - |
| `modo` | `CAIDO` | valido | - | - |
| `modo` | `DESCONOCIDO` | **fuera de clase** | - | FUERA_SERVICIO no existe en ModoSimulacion |
| `modo` | `AUSENTE` | **fuera de clase** | - | se omite la clave modo del cuerpo: llega null |
| `cifras` | `COMPLETAS` | valido | - | - |
| `cifras` | `CERO` | valido | los tres valores en su cota inferior | - |
| `cifras` | `MAX` | valido | longe/int en su cota superior | - |
| `cifras` | `NEG_LAT` | **fuera de clase** | - | latencia negativa |
| `cifras` | `NEG_TIMEOUT` | **fuera de clase** | - | tiempo de espera negativo |
| `cifras` | `NEG_REINT` | **fuera de clase** | - | reintentos negativo |
| `cifras` | `AUSENTE` | **fuera de clase** | - | se omiten las tres cifras: los envoltorios llegan null |
| `cifras` | `TIPO_ERRONEO` | **fuera de clase** | - | latencia con un valor de texto no convertible a long |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `sistema` vale VACIO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | `sistema` vale ESPACIOS | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `sistema` vale DESCONOCIDO | `SISTEMA_DESCONOCIDO` | `SISTEMA_NO_ENCONTRADO` |
| 4 | `modo` vale DESCONOCIDO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `modo` vale AUSENTE | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `cifras` vale AUSENTE | `INVALIDOS` | `DATOS_INVALIDOS` |
| 7 | `cifras` vale NEG_LAT | `INVALIDOS` | `DATOS_INVALIDOS` |
| 8 | `cifras` vale NEG_TIMEOUT | `INVALIDOS` | `DATOS_INVALIDOS` |
| 9 | `cifras` vale NEG_REINT | `INVALIDOS` | `DATOS_INVALIDOS` |
| 10 | `cifras` vale TIPO_ERRONEO | `TIPO_ERRONEO` | `DATOS_INVALIDOS` |
| 11 | resto de casos | `OK` | `-` |

### Casos generados

| ID | sistema | modo | cifras | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC19-F01` | `HCE_MIN` | `NORMAL_MINUS` | `COMPLETAS` | `NINGUNO` | OK | politica registrada en caliente: la respuesta es el record PoliticaSimulacion vigente (no lleva el nombre del sistema: hay que leer el registro vivo) y el registro cambia sin reiniciar |
| `TC19-F02` | `VACIO` | `NORMAL` | `COMPLETAS` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la guarda del controlador exige sistema no vacio y lo lanza como IllegalArgumentException -> 400, antes de resolver el codigo \| sistema: sistema vacio: la guarda del controlador lo exige |
| `TC19-F03` | `ESPACIOS` | `NORMAL_MINUS` | `CERO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | isBlank tambien catches los espacios: mismo 400 \| sistema: sistema de solo espacios |
| `TC19-F04` | `DESCONOCIDO` | `LENTO` | `COMPLETAS` | `NINGUNO` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | el codigo si esta relleno pero no es sso, hce, ris ni pacs: la resolucion responde 404 \| sistema: sap no es un sistema del CDU19 |
| `TC19-F05` | `HCE_MIN` | `DESCONOCIDO` | `CERO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el modo no esta en ModoSimulacion: el controlador lo rechaza con IllegalArgumentException, que su propio advice traduce en 400 DATOS_INVALIDOS \| modo: FUERA_SERVICIO no existe en ModoSimulacion |
| `TC19-F06` | `RIS_MIN` | `AUSENTE` | `COMPLETAS` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | modo null: el controlador lo comprueba antes de construir el record y lanza el mismo IAE -> 400 DATOS_INVALIDOS \| modo: se omite la clave modo del cuerpo: llega null |
| `TC19-F07` | `HCE_MIN` | `NORMAL` | `NEG_LAT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el compact constructor del record prohibe latencias negativas y lanza IllegalArgumentException -> 400 \| cifras: latencia negativa |
| `TC19-F08` | `RIS_MIN` | `NORMAL_MINUS` | `NEG_TIMEOUT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre el tiempo de espera \| cifras: tiempo de espera negativo |
| `TC19-F09` | `HCE_MIN` | `LENTO` | `NEG_REINT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre los reintentos \| cifras: reintentos negativo |
| `TC19-F10` | `HCE_MIN` | `CAIDO` | `AUSENTE` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | las cifras son obligatorias en el controlador: null no se confunde con cero, y de nuevo sale un IAE -> 400 DATOS_INVALIDOS \| cifras: se omiten las tres cifras: los envoltorios llegan null |
| `TC19-F11` | `HCE_MIN` | `NORMAL_MINUS` | `TIPO_ERRONEO` | `NINGUNO` | TIPO_ERRONEO `DATOS_INVALIDOS` | la cifra llega como texto: la conversion de Jackson falla y el consejo de cuerpo ilegible responde 400 con ApiError \| cifras: latencia con un valor de texto no convertible a long |
| `TC19-F12` | `HCE_MIN` | `AUSENTE` | `MAX` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | modo null: el controlador lo comprueba antes de construir el record y lanza el mismo IAE -> 400 DATOS_INVALIDOS \| modo: se omite la clave modo del cuerpo: llega null |
| `TC19-F13` | `ESPACIOS` | `CAIDO` | `COMPLETAS` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | isBlank tambien catches los espacios: mismo 400 \| sistema: sistema de solo espacios |
| `TC19-F14` | `VACIO` | `NORMAL_MINUS` | `MAX` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la guarda del controlador exige sistema no vacio y lo lanza como IllegalArgumentException -> 400, antes de resolver el codigo \| sistema: sistema vacio: la guarda del controlador lo exige |
| `TC19-F15` | `VACIO` | `LENTO` | `CERO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la guarda del controlador exige sistema no vacio y lo lanza como IllegalArgumentException -> 400, antes de resolver el codigo \| sistema: sistema vacio: la guarda del controlador lo exige |
| `TC19-F16` | `ESPACIOS` | `LENTO` | `MAX` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | isBlank tambien catches los espacios: mismo 400 \| sistema: sistema de solo espacios |
| `TC19-F17` | `RIS_MIN` | `DESCONOCIDO` | `MAX` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el modo no esta en ModoSimulacion: el controlador lo rechaza con IllegalArgumentException, que su propio advice traduce en 400 DATOS_INVALIDOS \| modo: FUERA_SERVICIO no existe en ModoSimulacion |
| `TC19-F18` | `DESCONOCIDO` | `NORMAL` | `MAX` | `NINGUNO` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | el codigo si esta relleno pero no es sso, hce, ris ni pacs: la resolucion responde 404 \| sistema: sap no es un sistema del CDU19 |
| `TC19-F19` | `DESCONOCIDO` | `CAIDO` | `CERO` | `NINGUNO` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | el codigo si esta relleno pero no es sso, hce, ris ni pacs: la resolucion responde 404 \| sistema: sap no es un sistema del CDU19 |
| `TC19-F20` | `RIS_MIN` | `LENTO` | `NEG_LAT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el compact constructor del record prohibe latencias negativas y lanza IllegalArgumentException -> 400 \| cifras: latencia negativa |
| `TC19-F21` | `RIS_MIN` | `NORMAL` | `CERO` | `NINGUNO` | OK | politica registrada en caliente: la respuesta es el record PoliticaSimulacion vigente (no lleva el nombre del sistema: hay que leer el registro vivo) y el registro cambia sin reiniciar \| valores limite: cifras=CERO (los tres valores en su cota inferior) |
| `TC19-F22` | `RIS_MIN` | `CAIDO` | `NEG_REINT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre los reintentos \| cifras: reintentos negativo |
| `TC19-F23` | `HCE_MIN` | `NORMAL` | `NEG_TIMEOUT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre el tiempo de espera \| cifras: tiempo de espera negativo |
| `TC19-F24` | `RIS_MIN` | `NORMAL_MINUS` | `AUSENTE` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | las cifras son obligatorias en el controlador: null no se confunde con cero, y de nuevo sale un IAE -> 400 DATOS_INVALIDOS \| cifras: se omiten las tres cifras: los envoltorios llegan null |
| `TC19-F25` | `RIS_MIN` | `NORMAL` | `TIPO_ERRONEO` | `NINGUNO` | TIPO_ERRONEO `DATOS_INVALIDOS` | la cifra llega como texto: la conversion de Jackson falla y el consejo de cuerpo ilegible responde 400 con ApiError \| cifras: latencia con un valor de texto no convertible a long |
| `TC19-F26` | `VACIO` | `CAIDO` | `MAX` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la guarda del controlador exige sistema no vacio y lo lanza como IllegalArgumentException -> 400, antes de resolver el codigo \| sistema: sistema vacio: la guarda del controlador lo exige |
| `TC19-F27` | `HCE_MIN` | `DESCONOCIDO` | `COMPLETAS` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el modo no esta en ModoSimulacion: el controlador lo rechaza con IllegalArgumentException, que su propio advice traduce en 400 DATOS_INVALIDOS \| modo: FUERA_SERVICIO no existe en ModoSimulacion |
| `TC19-F28` | `HCE_MIN` | `AUSENTE` | `CERO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | modo null: el controlador lo comprueba antes de construir el record y lanza el mismo IAE -> 400 DATOS_INVALIDOS \| modo: se omite la clave modo del cuerpo: llega null |
| `TC19-F29` | `ESPACIOS` | `NORMAL` | `COMPLETAS` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | isBlank tambien catches los espacios: mismo 400 \| sistema: sistema de solo espacios |
| `TC19-F30` | `DESCONOCIDO` | `NORMAL_MINUS` | `COMPLETAS` | `NINGUNO` | SISTEMA_DESCONOCIDO `SISTEMA_NO_ENCONTRADO` | el codigo si esta relleno pero no es sso, hce, ris ni pacs: la resolucion responde 404 \| sistema: sap no es un sistema del CDU19 |
| `TC19-F31` | `HCE_MIN` | `NORMAL_MINUS` | `NEG_LAT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el compact constructor del record prohibe latencias negativas y lanza IllegalArgumentException -> 400 \| cifras: latencia negativa |
| `TC19-F32` | `HCE_MIN` | `NORMAL_MINUS` | `NEG_REINT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre los reintentos \| cifras: reintentos negativo |
| `TC19-F33` | `HCE_MIN` | `NORMAL` | `NEG_REINT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre los reintentos \| cifras: reintentos negativo |
| `TC19-F34` | `HCE_MIN` | `NORMAL` | `AUSENTE` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | las cifras son obligatorias en el controlador: null no se confunde con cero, y de nuevo sale un IAE -> 400 DATOS_INVALIDOS \| cifras: se omiten las tres cifras: los envoltorios llegan null |
| `TC19-F35` | `HCE_MIN` | `LENTO` | `AUSENTE` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | las cifras son obligatorias en el controlador: null no se confunde con cero, y de nuevo sale un IAE -> 400 DATOS_INVALIDOS \| cifras: se omiten las tres cifras: los envoltorios llegan null |
| `TC19-F36` | `HCE_MIN` | `LENTO` | `NEG_TIMEOUT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre el tiempo de espera \| cifras: tiempo de espera negativo |
| `TC19-F37` | `HCE_MIN` | `LENTO` | `TIPO_ERRONEO` | `NINGUNO` | TIPO_ERRONEO `DATOS_INVALIDOS` | la cifra llega como texto: la conversion de Jackson falla y el consejo de cuerpo ilegible responde 400 con ApiError \| cifras: latencia con un valor de texto no convertible a long |
| `TC19-F38` | `HCE_MIN` | `CAIDO` | `NEG_LAT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el compact constructor del record prohibe latencias negativas y lanza IllegalArgumentException -> 400 \| cifras: latencia negativa |
| `TC19-F39` | `HCE_MIN` | `CAIDO` | `NEG_TIMEOUT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | misma guarda del record sobre el tiempo de espera \| cifras: tiempo de espera negativo |
| `TC19-F40` | `HCE_MIN` | `CAIDO` | `TIPO_ERRONEO` | `NINGUNO` | TIPO_ERRONEO `DATOS_INVALIDOS` | la cifra llega como texto: la conversion de Jackson falla y el consejo de cuerpo ilegible responde 400 con ApiError \| cifras: latencia con un valor de texto no convertible a long |

## Tabla G · Conjetura de errores y rincones del contrato de integracion

**Modo:** `conjetura` · filas escritas a mano = 14

**Puerta que cubre:** AF rincones: rutas sin mapear, metodos no implementados, content-type y tipos imposibles; el paso rojo sobre la rama confirma o corrige cada esperado

### Casos generados

| ID | grupo | metodo | ruta | cuerpo | contentTipo | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|
| `TC19-G01` | `RUTA` | `GET` | `/api/integracion/no-existe` | `NINGUNO` | `NINGUNO` | `NINGUNO` | RUTA_INEXISTENTE | ruta sin mapear dentro del prefijo del modulo: el 404 lo resuelve Spring y no pasa por el advice del modulo, asi que no hay cuerpo ApiError |
| `TC19-G02` | `METODO` | `POST` | `/api/integracion/sistemas` | `NINGUNO` | `NINGUNO` | `NINGUNO` | METODO | el listado de sistemas solo admite GET: 405 sin cuerpo ApiError por el mismo motivo |
| `TC19-G03` | `CONTENIDO` | `POST` | `/api/integracion/historias/api-paciente-ok/sincronizar` | `NINGUNO` | `TEXT_PLANO` | `SIM-HCE-NORMAL-5-300-2` | OK | HALLAZGO: sincronizar no lleva @RequestBody, asi que un Content-Type text/plain no lo rechaza y la sincronizacion pasa de verdad (el paciente de la ruta existe en la HCE simulada), el 415 solo aparece en los endpoints con cuerpo |
| `TC19-G04` | `CONTENIDO` | `POST` | `/api/integracion/sso/login` | `SSO_COMPLETO` | `TEXT_PLANO` | `NINGUNO` | CONTENIDO `CONTENIDO_NO_ACEPTADO` | text/plain en un endpoint con @RequestBody: HttpMediaTypeNotSupportedException tiene consejo propio -> 415 con cuerpo ApiError |
| `TC19-G05` | `CUERPO` | `POST` | `/api/integracion/sso/login` | `NO_JSON` | `JSON` | `NINGUNO` | CUERPO_ILEGIBLE `DATOS_INVALIDOS` | cuerpo que no es JSON parseable: el consejo de cuerpo ilegible lo traduce a 400 con ApiError |
| `TC19-G06` | `CUERPO` | `POST` | `/api/integracion/sso/login` | `NULL` | `JSON` | `NINGUNO` | CUERPO_ILEGIBLE `DATOS_INVALIDOS` | literal null como cuerpo: @RequestBody lo trata como cuerpo ausente -> 400 DATOS_INVALIDOS, no un 500 por null pointer |
| `TC19-G07` | `COERCION` | `POST` | `/api/integracion/sso/login` | `USUARIO_NUMERICO` | `JSON` | `NINGUNO` | CREDENCIALES `CREDENCIALES_INVALIDAS` | HALLAZGO: Jackson coercea el escalar numerico a la cadena 123 y el IdP lo rechaza con el 401 normal, no hay 400 por tipo en este endpoint |
| `TC19-G08` | `TIPO` | `POST` | `/api/integracion/simulacion` | `CIFRAS_TEXTO` | `JSON` | `NINGUNO` | TIPO_ERRONEO `DATOS_INVALIDOS` | aqui las cifras si son long e int: un valor de texto no convertible revienta en la conversion -> 400 con ApiError |
| `TC19-G09` | `CUERPO` | `POST` | `/api/integracion/simulacion` | `VACIO` | `JSON` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | objeto vacio: sistema null, la guarda del controlador responde 400 y no deja ver el modo ni las cifras |
| `TC19-G10` | `METODO` | `POST` | `/api/integracion/auditoria` | `NINGUNO` | `NINGUNO` | `NINGUNO` | METODO | la auditoria es solo lectura: no hay escritura externa de eslabones en el spike |
| `TC19-G11` | `CONTRATO` | `PATCH` | `/api/integracion/historias/api-paciente-ok/reintentos` | `NINGUNO` | `NINGUNO` | `NINGUNO` | RUTA_INEXISTENTE | HALLAZGO: la ruta aparece en el borrador del contrato comun de integracion y el CDU19 no la implementa. Como ningun metodo la mapea, Spring no llega ni a plantear el 405: responde 404 sin cuerpo ApiError, y el spike no decide el mecanismo de reintentos por API |
| `TC19-G12` | `METODO` | `GET` | `/api/integracion/historias/api-paciente-ok/sincronizar` | `NINGUNO` | `NINGUNO` | `NINGUNO` | METODO | sincronizar solo admite POST: traerla por GET seria una escritura disfrazada de lectura |
| `TC19-G13` | `RUTA` | `GET` | `/api/integracion/estudios/` | `NINGUNO` | `NINGUNO` | `NINGUNO` | RUTA_INEXISTENTE | segmento de ruta vacio en el endpoint de estudios: si Spring lo mapa, la guarda del adaptador responderia 400 DATOS_INVALIDOS |
| `TC19-G14` | `RUTA` | `GET` | `/api/integracion/sistemas/hce/ping/extra` | `NINGUNO` | `NINGUNO` | `NINGUNO` | RUTA_INEXISTENTE | segmento de mas bajo una ruta mapeada: Spring no la atiende y el advice del modulo no interviene |

## Como se ejecutan

```
mvn -B -ntp -pl iso2-integracion -am test
```

Los CSV de trabajo estan en `docs/test/iter06/` y su copia en el classpath de pruebas (`iso2-integracion/src/test/resources/tdop/`) es identica: el generador las escribe a la vez.

