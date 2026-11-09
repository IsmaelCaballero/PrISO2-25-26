# Evidencia TDOP de CDU16 (iter01 · CFG-01)

Metodo: Clases de equivalencia + teorema de valores limite + conjetura de errores, compuestas con pairwise (todo par de niveles de factores distintos queda cubierto).

Generador: `python tools/pairwise.py docs/test/iter01/tdop-cdu16-espec.json --resources iso2-catalogo/src/test/resources/tdop` (semilla 20261108).
Las mismas filas las ejecutan los JUnit parametrizados de `es.uclm.iso2.catalogo.tdop` y las tablas de `docs/tc/cdu16-tc-humanos.md`: un ID vale para las dos.

## Leyenda de tokens

| Token | Significado |
|---|---|
| `AUT` | valor autocoadyuvante: el que se autogenera el ensayo, siempre dentro de clase |
| `VACIO` | cadena vacia "" (o, en cuerpo, ausencia de campos obligatorios) |
| `ESPACIOS` | texto con espacios delante y detras: el contrato no recorta |
| `REP:<n>:<c>` | el caracter <c> repetido n veces |
| `LARGO` | nombre de 121 caracteres (REP:121:N) |
| `NO_ADMITIDA` | categoria fuera de la lista blanca (USG) |
| `AUSENTE` | campo numerico no enviado: el servicio pone el defecto de la norma |
| `JUSTO` | justo la cota del servicio (1440/720/8760/100/1000) |
| `CERO` | 0, minimo admitido por @PositiveOrZero |
| `NEGATIVA` | -1, pasa la conversion JSON y la rechaza la cota del DTO |
| `EXCESIVA` | un punto sobre la cota (101 / 1001 / valores muy altos) |
| `COMA` | numero con coma decimal local (36,5): Jackson la parte como separador |
| `USG` | texto fuera de la lista blanca de categorias |
| `-` | no aplica |

## Estados esperados (nombre simbolico -> HTTP)

| Estado | HTTP | Codigo de error | Que significa |
|---|---|---|---|
| `CREADO` | 201 | `-` | recurso creado (POST /api/catalogo/pruebas) |
| `OK` | 200 | `-` | peticion atendida con cuerpo |
| `BORRADO` | 204 | `-` | recurso borrado logicamente o recurso ya en el estado pedido (baja idempotente): sin cuerpo |
| `INVALIDOS` | 400 | `DATOS_INVALIDOS` | entrada fuera de clase, parametro mal tipado que pasa la conversion o regla de negocio violada |
| `SOLICITUD_MALFORMADA` | 400 | `DATOS_INVALIDOS` | el cuerpo o el parametro ni siquiera se pueden leer como la peticion promete: el consejo global convierte la conversion fallida en 400 con cuerpo ApiError DATOS_INVALIDOS, igual que una entrada fuera de clase (HALLAZGO: el cliente no puede distinguer ilegible de fuera-de-clase) |
| `NO_ENCONTRADA` | 404 | `PRUEBA_NO_ENCONTRADA` | la ruta no apunta a ninguna fila de la tabla de pruebas |
| `RUTA_INEXISTENTE` | 404 | `-` | no hay ninguna mano del controlador que atienda la ruta pedida: 404 de Spring Boot, sin cuerpo ApiError |
| `CONFLICTO` | 409 | `CODIGO_DUPLICADO` | el codigo de prueba ya esta en uso, este activo o no |
| `NO_ACEPTABLE` | 415 | `CONTENIDO_NO_ACEPTADO` | Content-Type que la API no admite |

## Reparto de casos

| Tabla | Operacion | Modo | Filas |
|---|---|---|---|
| A | Alta de prueba (POST /api/catalogo/pruebas) | pairwise | 33 |
| B | Modificacion de prueba (PUT /api/catalogo/pruebas/{codigo}) | pairwise | 15 |
| C | Listado del catalogo (GET /api/catalogo/pruebas) | pairwise | 36 |
| D | Estados del recurso (GET/DELETE /{codigo} y PUT /{codigo}/reactivar) | filas | 9 |
| E | Conjetura de errores (casos explicitos, sin combinacion) | conjetura | 22 |
| **Total** |  |  | **115** |

## Tabla A · Alta de prueba (POST /api/catalogo/pruebas)

**Modo:** `pairwise` · producto de niveles = 1400 · filas generadas = 33 · pares obligatorios cubiertos = 108/108 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF completitud y correccion: toda alta valida crea la prueba y toda alta invalida se rechaza con un codigo de error estable

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `transporte` | `JSON` | valido | - | application/json: el unico tipo que la API consume |
| `transporte` | `TEXT_PLANO` | **fuera de clase** | tipo no soportado | text/plain |
| `estadoRecurso` | `LIBRE` | valido | - | no hay ninguna fila con ese codigo |
| `estadoRecurso` | `MIXTO` | valido | - | el catalogo tiene otras filas, activas e inactivas, pero ninguna con el codigo enviado |
| `estadoRecurso` | `DUPLICA_ACTIVA` | **fuera de clase** | equivalencia de estado de la coleccion | ya existe una prueba ACTIVA con el codigo enviado |
| `estadoRecurso` | `DUPLICA_INACTIVA` | **fuera de clase** | equivalencia de estado de la coleccion | existe una fila con el codigo enviado pero dada de baja: seguiria ocupando el codigo |
| `cuerpo` | `VALIDO` | valido | - | cuerpo JSON completo y dentro de todas las cotas |
| `cuerpo` | `VACIO` | **fuera de clase** | ausencia | POST sin cuerpo alguno |
| `cuerpo` | `JSON_MALFORMADO` | **fuera de clase** | sintaxis | JSON roto que no se puede parsear |
| `cuerpo` | `TIPO_ERRONEO` | **fuera de clase** | tipos del contrato | un numero donde el contrato pide texto (la coma en el numero lo convertiria en lista y daria 400 con ApiError) |
| `cuerpo` | `CONTRATO_INCOMPLETO` | **fuera de clase** | campos obligatorios | faltan campos obligatorios del contrato DTO (o politica nula): si pasa la conversion pero falla @Valid |
| `nombreCategoria` | `AUT` | valido | - | nombre auto y categoria admitida ANALITICA |
| `nombreCategoria` | `VACIO` | **fuera de clase** | obligatoriedad | nombre cadena vacia: pasa la conversion JSON y lo rechaza @NotBlank |
| `nombreCategoria` | `ESPACIOS` | **fuera de clase** | caracteres raros | nombre con espacios: el contrato no recorta, se guarda tal cual |
| `nombreCategoria` | `LARGO` | **fuera de clase** | un punto sobre la cota | nombre de 121 caracteres (cota maxima 120) |
| `nombreCategoria` | `NO_ADMITIDA` | **fuera de clase** | fuera de dominio | categoria fuera de la lista blanca |
| `limites` | `AUT` | valido | - | valores auto dentro de cotas: duracion 60, ayuno 12, plazo 24, maxCancelaciones 3, protocolo 200 |
| `limites` | `AUSENTE` | **fuera de clase** | ausencia (validacion de cotas antes que duplicado) | sin campos numericos: el DTO exige limites y protocolo (@NotNull), la peticion se rechaza con DATOS_INVALIDOS antes de mirar si el codigo esta duplicado |
| `limites` | `JUSTO` | valido | justo la cota | justo la cota: duracion 1440, ayuno 720, plazo 8760, max 100, protocolo y preparacion de 1000 |
| `limites` | `CERO` | **fuera de clase** | minimo (validacion de cotas antes que duplicado) | 0 en duracion, ayuno, plazo y maxCancelaciones: el servicio exige valores estrictamente positivos, el 0 se rechaza con DATOS_INVALIDOS antes de mirar el duplicado |
| `limites` | `NEGATIVA` | **fuera de clase** | bajo el minimo | duracion y ayuno negativos: pasan la conversion y lo rechaza la cota @PositiveOrZero del DTO |
| `limites` | `EXCESIVA` | **fuera de clase** | un punto sobre la cota | maxCancelaciones 101 (cota 100), protocolo y preparacion de 1001 (cota 1000), duracion y ayuno pasados |
| `limites` | `COMA` | **fuera de clase** | formato numerico local | valores con coma: Jackson los parte como separador y da lista [-1] dentro del entero, que se rechaza con ApiError |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `transporte` vale TEXT_PLANO | `NO_ACEPTABLE` | `CONTENIDO_NO_ACEPTADO` |
| 2 | `cuerpo` vale VACIO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 3 | `cuerpo` vale JSON_MALFORMADO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 4 | `cuerpo` vale TIPO_ERRONEO | `SOLICITUD_MALFORMADA` | `DATOS_INVALIDOS` |
| 5 | `cuerpo` vale CONTRATO_INCOMPLETO | `INVALIDOS` | `DATOS_INVALIDOS` |
| 6 | `nombreCategoria` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 7 | `limites` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 8 | `estadoRecurso` vale DUPLICA_INACTIVA | `CONFLICTO` | `CODIGO_DUPLICADO` |
| 9 | `estadoRecurso` vale DUPLICA_ACTIVA | `CONFLICTO` | `CODIGO_DUPLICADO` |
| 10 | resto de casos | `CREADO` | `-` |

### Casos generados

| ID | transporte | estadoRecurso | cuerpo | nombreCategoria | limites | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|
| `TC16-A01` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `AUT` | `NINGUNO` | CREADO | alta admitida: 201 con la prueba creada y asiento de auditoria ALTA |
| `TC16-A02` | `TEXT_PLANO` | `MIXTO` | `VALIDO` | `AUT` | `JUSTO` | `COD_AUTO` | NO_ACEPTABLE `CONTENIDO_NO_ACEPTADO` | el controlador solo consume application/json: HttpMediaTypeNotSupportedException (lo traduce CatalogoManejadorErrores desde D35) \| transporte: text/plain |
| `TC16-A03` | `JSON` | `DUPLICA_ACTIVA` | `VALIDO` | `AUT` | `JUSTO` | `CREA_COD_ACTIVA,COD_AUTO` | CONFLICTO `CODIGO_DUPLICADO` | el codigo ya esta en uso por una prueba activa \| estadoRecurso: ya existe una prueba ACTIVA con el codigo enviado |
| `TC16-A04` | `JSON` | `DUPLICA_INACTIVA` | `VALIDO` | `AUT` | `AUT` | `CREA_COD_INACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | los codigos de prueba no se reutilizan: hay fila aunque este inactiva \| estadoRecurso: existe una fila con el codigo enviado pero dada de baja: seguiria ocupando el codigo |
| `TC16-A05` | `JSON` | `MIXTO` | `VACIO` | `AUT` | `AUT` | `NINGUNO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | POST sin cuerpo: HttpMessageNotReadableException, que no pasa por @Valid y no deja cuerpo ApiError \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: POST sin cuerpo alguno |
| `TC16-A06` | `JSON` | `LIBRE` | `JSON_MALFORMADO` | `AUT` | `JUSTO` | `COD_AUTO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | JSON malformado: HttpMessageNotReadableException sin cuerpo ApiError \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: JSON roto que no se puede parsear |
| `TC16-A07` | `JSON` | `LIBRE` | `TIPO_ERRONEO` | `AUT` | `AUT` | `NINGUNO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | un numero donde va el codigo no se puede leer como String (o un boolean donde va un objeto): HttpMessageNotReadableException, la coma en el numero si convierte en lista y cae a DATOS_INVALIDOS \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: un numero donde el contrato pide texto (la coma en el numero lo convertiria en lista y daria 400 con ApiError) |
| `TC16-A08` | `JSON` | `LIBRE` | `CONTRATO_INCOMPLETO` | `AUT` | `AUT` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | falta un campo obligatorio del contrato, o la politica viene nula: MethodArgumentNotValidException en PruebaCatalogoFacade \| cuerpo: faltan campos obligatorios del contrato DTO (o politica nula): si pasa la conversion pero falla @Valid |
| `TC16-A09` | `JSON` | `LIBRE` | `VALIDO` | `VACIO` | `AUT` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre cadena vacia: pasa la conversion JSON y lo rechaza @NotBlank |
| `TC16-A10` | `JSON` | `LIBRE` | `VALIDO` | `ESPACIOS` | `AUT` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre con espacios: el contrato no recorta, se guarda tal cual |
| `TC16-A11` | `JSON` | `LIBRE` | `VALIDO` | `LARGO` | `AUT` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre de 121 caracteres (cota maxima 120) |
| `TC16-A12` | `JSON` | `LIBRE` | `VALIDO` | `NO_ADMITIDA` | `AUT` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: categoria fuera de la lista blanca |
| `TC16-A13` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `AUSENTE` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: sin campos numericos: el DTO exige limites y protocolo (@NotNull), la peticion se rechaza con DATOS_INVALIDOS antes de mirar si el codigo esta duplicado |
| `TC16-A14` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `CERO` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: 0 en duracion, ayuno, plazo y maxCancelaciones: el servicio exige valores estrictamente positivos, el 0 se rechaza con DATOS_INVALIDOS antes de mirar el duplicado |
| `TC16-A15` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `NEGATIVA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: duracion y ayuno negativos: pasan la conversion y lo rechaza la cota @PositiveOrZero del DTO |
| `TC16-A16` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `EXCESIVA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: maxCancelaciones 101 (cota 100), protocolo y preparacion de 1001 (cota 1000), duracion y ayuno pasados |
| `TC16-A17` | `JSON` | `LIBRE` | `VALIDO` | `AUT` | `COMA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: valores con coma: Jackson los parte como separador y da lista [-1] dentro del entero, que se rechaza con ApiError |
| `TC16-A18` | `TEXT_PLANO` | `LIBRE` | `VALIDO` | `AUT` | `AUT` | `NINGUNO` | NO_ACEPTABLE `CONTENIDO_NO_ACEPTADO` | el controlador solo consume application/json: HttpMediaTypeNotSupportedException (lo traduce CatalogoManejadorErrores desde D35) \| transporte: text/plain |
| `TC16-A19` | `JSON` | `LIBRE` | `VACIO` | `AUT` | `JUSTO` | `COD_AUTO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | POST sin cuerpo: HttpMessageNotReadableException, que no pasa por @Valid y no deja cuerpo ApiError \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: POST sin cuerpo alguno |
| `TC16-A20` | `JSON` | `MIXTO` | `JSON_MALFORMADO` | `AUT` | `AUT` | `NINGUNO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | JSON malformado: HttpMessageNotReadableException sin cuerpo ApiError \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: JSON roto que no se puede parsear |
| `TC16-A21` | `JSON` | `MIXTO` | `TIPO_ERRONEO` | `AUT` | `JUSTO` | `COD_AUTO` | SOLICITUD_MALFORMADA `DATOS_INVALIDOS` | un numero donde va el codigo no se puede leer como String (o un boolean donde va un objeto): HttpMessageNotReadableException, la coma en el numero si convierte en lista y cae a DATOS_INVALIDOS \| HALLAZGO: el consejo convierte la conversion fallida en DATOS_INVALIDOS, el cliente no distingue ilegible de fuera de clase \| cuerpo: un numero donde el contrato pide texto (la coma en el numero lo convertiria en lista y daria 400 con ApiError) |
| `TC16-A22` | `JSON` | `MIXTO` | `CONTRATO_INCOMPLETO` | `AUT` | `JUSTO` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | falta un campo obligatorio del contrato, o la politica viene nula: MethodArgumentNotValidException en PruebaCatalogoFacade \| cuerpo: faltan campos obligatorios del contrato DTO (o politica nula): si pasa la conversion pero falla @Valid |
| `TC16-A23` | `JSON` | `MIXTO` | `VALIDO` | `VACIO` | `JUSTO` | `COD_AUTO,COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre cadena vacia: pasa la conversion JSON y lo rechaza @NotBlank |
| `TC16-A24` | `JSON` | `MIXTO` | `VALIDO` | `ESPACIOS` | `JUSTO` | `COD_AUTO,COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre con espacios: el contrato no recorta, se guarda tal cual |
| `TC16-A25` | `JSON` | `MIXTO` | `VALIDO` | `LARGO` | `JUSTO` | `COD_AUTO,COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: nombre de 121 caracteres (cota maxima 120) |
| `TC16-A26` | `JSON` | `MIXTO` | `VALIDO` | `NO_ADMITIDA` | `JUSTO` | `COD_AUTO,COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | el nombre pasa la cota de LongitudRequerida(120) o la categoria se sale de la lista blanca \| nombreCategoria: categoria fuera de la lista blanca |
| `TC16-A27` | `JSON` | `DUPLICA_ACTIVA` | `VALIDO` | `AUT` | `AUT` | `CREA_COD_ACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | el codigo ya esta en uso por una prueba activa \| estadoRecurso: ya existe una prueba ACTIVA con el codigo enviado |
| `TC16-A28` | `JSON` | `DUPLICA_INACTIVA` | `VALIDO` | `AUT` | `JUSTO` | `CREA_COD_INACTIVA,COD_AUTO` | CONFLICTO `CODIGO_DUPLICADO` | los codigos de prueba no se reutilizan: hay fila aunque este inactiva \| estadoRecurso: existe una fila con el codigo enviado pero dada de baja: seguiria ocupando el codigo |
| `TC16-A29` | `JSON` | `MIXTO` | `VALIDO` | `AUT` | `AUSENTE` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: sin campos numericos: el DTO exige limites y protocolo (@NotNull), la peticion se rechaza con DATOS_INVALIDOS antes de mirar si el codigo esta duplicado |
| `TC16-A30` | `JSON` | `MIXTO` | `VALIDO` | `AUT` | `CERO` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: 0 en duracion, ayuno, plazo y maxCancelaciones: el servicio exige valores estrictamente positivos, el 0 se rechaza con DATOS_INVALIDOS antes de mirar el duplicado |
| `TC16-A31` | `JSON` | `MIXTO` | `VALIDO` | `AUT` | `NEGATIVA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: duracion y ayuno negativos: pasan la conversion y lo rechaza la cota @PositiveOrZero del DTO |
| `TC16-A32` | `JSON` | `MIXTO` | `VALIDO` | `AUT` | `EXCESIVA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: maxCancelaciones 101 (cota 100), protocolo y preparacion de 1001 (cota 1000), duracion y ayuno pasados |
| `TC16-A33` | `JSON` | `MIXTO` | `VALIDO` | `AUT` | `COMA` | `COD_AUTO` | INVALIDOS `DATOS_INVALIDOS` | un limite de la politica o de la preparacion pasa la cota del servicio (se mira antes de mirar duplicados) \| limites: valores con coma: Jackson los parte como separador y da lista [-1] dentro del entero, que se rechaza con ApiError |

## Tabla B · Modificacion de prueba (PUT /api/catalogo/pruebas/{codigo})

**Modo:** `pairwise` · producto de niveles = 45 · filas generadas = 15 · pares obligatorios cubiertos = 39/39 (100%)

**Puerta que cubre:** AF correccion: coherencia ruta/cuerpo, estado del recurso y cotas en la modificacion

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `estadoRecurso` | `ACTIVA` | valido | - | prueba dada de alta |
| `estadoRecurso` | `INACTIVA` | valido | - | prueba dada de baja logica: se puede modificar |
| `estadoRecurso` | `NO_EXISTE` | **fuera de clase** | - | la ruta no apunta a ninguna fila |
| `codigoRuta` | `IGUAL` | valido | - | ruta y cuerpo coinciden |
| `codigoRuta` | `DISTINTO_INVENTADO` | **fuera de clase** | coherencia ruta/cuerpo | la ruta no coincide con el cuerpo (se mira antes de buscar la fila) |
| `codigoRuta` | `DISTINTO_EXISTENTE` | **fuera de clase** | coherencia ruta/cuerpo | la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera |
| `validezCuerpo` | `VALIDO` | valido | - | cuerpo dentro de todas las cotas |
| `validezCuerpo` | `NOMBRE_LARGO121` | **fuera de clase** | un punto sobre la cota | nombre de 121 caracteres |
| `validezCuerpo` | `CATEGORIA_FUERA` | **fuera de clase** | fuera de dominio | categoria fuera de la lista blanca |
| `validezCuerpo` | `MAXCANCEL_101` | **fuera de clase** | un punto sobre la cota | maxCancelaciones 101 |
| `validezCuerpo` | `PROTO_LOMAX1001` | **fuera de clase** | un punto sobre la cota | preparacion de 1001 caracteres |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `codigoRuta` no vale IGUAL | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | `validezCuerpo` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `estadoRecurso` vale NO_EXISTE | `NO_ENCONTRADA` | `PRUEBA_NO_ENCONTRADA` |
| 4 | resto de casos | `OK` | `-` |

### Casos generados

| ID | estadoRecurso | codigoRuta | validezCuerpo | campoInvalido | valorInvalido | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|
| `TC16-B01` | `ACTIVA` | `IGUAL` | `VALIDO` | `-` | `-` | `CREA_COD_ACTIVA` | OK | modificacion admitida: 200 con la prueba modificada y asiento de auditoria MODIFICACION |
| `TC16-B02` | `ACTIVA` | `DISTINTO_INVENTADO` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta no coincide con el cuerpo (se mira antes de buscar la fila) + validezCuerpo: nombre de 121 caracteres |
| `TC16-B03` | `ACTIVA` | `DISTINTO_EXISTENTE` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B04` | `INACTIVA` | `IGUAL` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: nombre de 121 caracteres |
| `TC16-B05` | `NO_EXISTE` | `IGUAL` | `CATEGORIA_FUERA` | `categoria` | `USG` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| estadoRecurso: la ruta no apunta a ninguna fila + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B06` | `INACTIVA` | `DISTINTO_INVENTADO` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta no coincide con el cuerpo (se mira antes de buscar la fila) + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B07` | `INACTIVA` | `DISTINTO_EXISTENTE` | `VALIDO` | `-` | `-` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera |
| `TC16-B08` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `VALIDO` | `-` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo (se mira antes de buscar la fila) |
| `TC16-B09` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera + validezCuerpo: nombre de 121 caracteres |
| `TC16-B10` | `ACTIVA` | `IGUAL` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: maxCancelaciones 101 |
| `TC16-B11` | `INACTIVA` | `DISTINTO_INVENTADO` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta no coincide con el cuerpo (se mira antes de buscar la fila) + validezCuerpo: maxCancelaciones 101 |
| `TC16-B12` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera + validezCuerpo: maxCancelaciones 101 |
| `TC16-B13` | `ACTIVA` | `IGUAL` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B14` | `INACTIVA` | `DISTINTO_INVENTADO` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| codigoRuta: la ruta no coincide con el cuerpo (se mira antes de buscar la fila) + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B15` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo: CatalogoServicio.actualizar lo mira ANTES de buscar la fila \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente y el cuerpo apunta a una tercera + validezCuerpo: preparacion de 1001 caracteres |

## Tabla C · Listado del catalogo (GET /api/catalogo/pruebas)

**Modo:** `pairwise` · producto de niveles = 180 · filas generadas = 36 · pares obligatorios cubiertos = 96/96 (100%)

**Puerta que cubre:** AF completitud: paginacion y orden del listado de gestion, incluyendo parametros mal tipados

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `pagina` | `-1` | **fuera de clase** | bajo el minimo del contrato | pagina negativa (contrato) |
| `pagina` | `0` | valido | minimo del contrato | minimo del contrato y valor por defecto |
| `pagina` | `2` | valido | valor tipico | pagina dentro del catalogo |
| `pagina` | `200` | valido | limite de paginacion | pagina mas alla del catalogo: pagina vacia |
| `pagina` | `REP:12:9` | **fuera de clase** | parametro mal tipado | no cabe en un entero de la peticion |
| `pagina` | `abc` | **fuera de clase** | parametro mal tipado | no es un numero |
| `tamano` | `0` | **fuera de clase** | bajo el minimo del contrato | tamano bajo el minimo (contrato) |
| `tamano` | `1` | valido | minimo del contrato | minimo del contrato |
| `tamano` | `25` | valido | valor tipico | valor tipico |
| `tamano` | `200` | valido | justo la cota | justo la cota del contrato |
| `tamano` | `201` | **fuera de clase** | un punto sobre la cota | tamano sobre la cota (contrato) |
| `tamano` | `REP:12:9` | **fuera de clase** | parametro mal tipado | no cabe en un entero de la peticion |
| `orden` | `VACIO` | valido | ausencia | sin parametro: orden por defecto |
| `orden` | `codigo` | valido | valor tipico | campo admitido (es el defecto) |
| `orden` | `categoria` | valido | valor tipico | campo admitido |
| `orden` | `duracionMinutos` | valido | valor tipico | campo admitido |
| `orden` | `INVENTADO` | **fuera de clase** | fuera de dominio | campo fuera de la lista blanca |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | cualquier nivel fuera de clase valida | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | resto de casos | `OK` | `-` |

### Casos generados

| ID | pagina | tamano | orden | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC16-C01` | `0` | `1` | `VACIO` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=0 (minimo del contrato), tamano=1 (minimo del contrato), orden=VACIO (ausencia) |
| `TC16-C02` | `-1` | `1` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C03` | `2` | `1` | `categoria` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=2 (valor tipico), tamano=1 (minimo del contrato), orden=categoria (valor tipico) |
| `TC16-C04` | `200` | `1` | `duracionMinutos` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=200 (limite de paginacion), tamano=1 (minimo del contrato), orden=duracionMinutos (valor tipico) |
| `TC16-C05` | `REP:12:9` | `1` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion + orden: campo fuera de la lista blanca |
| `TC16-C06` | `-1` | `0` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) + tamano: tamano bajo el minimo (contrato) |
| `TC16-C07` | `-1` | `25` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C08` | `-1` | `200` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C09` | `-1` | `201` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) + tamano: tamano sobre la cota (contrato) + orden: campo fuera de la lista blanca |
| `TC16-C10` | `0` | `0` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C11` | `0` | `25` | `duracionMinutos` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=0 (minimo del contrato), tamano=25 (valor tipico), orden=duracionMinutos (valor tipico) |
| `TC16-C12` | `0` | `200` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| orden: campo fuera de la lista blanca |
| `TC16-C13` | `0` | `201` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C14` | `2` | `0` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C15` | `200` | `0` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) + orden: campo fuera de la lista blanca |
| `TC16-C16` | `2` | `25` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| orden: campo fuera de la lista blanca |
| `TC16-C17` | `REP:12:9` | `0` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion + tamano: tamano bajo el minimo (contrato) |
| `TC16-C18` | `2` | `200` | `codigo` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=2 (valor tipico), tamano=200 (justo la cota), orden=codigo (valor tipico) |
| `TC16-C19` | `200` | `25` | `codigo` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=200 (limite de paginacion), tamano=25 (valor tipico), orden=codigo (valor tipico) |
| `TC16-C20` | `2` | `201` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C21` | `REP:12:9` | `25` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C22` | `200` | `200` | `VACIO` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=200 (limite de paginacion), tamano=200 (justo la cota), orden=VACIO (ausencia) |
| `TC16-C23` | `200` | `201` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C24` | `REP:12:9` | `200` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C25` | `REP:12:9` | `201` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion + tamano: tamano sobre la cota (contrato) |
| `TC16-C26` | `abc` | `REP:12:9` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero + tamano: no cabe en un entero de la peticion |
| `TC16-C27` | `-1` | `REP:12:9` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) + tamano: no cabe en un entero de la peticion |
| `TC16-C28` | `0` | `REP:12:9` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |
| `TC16-C29` | `2` | `REP:12:9` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |
| `TC16-C30` | `200` | `REP:12:9` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion + orden: campo fuera de la lista blanca |
| `TC16-C31` | `abc` | `0` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero + tamano: tamano bajo el minimo (contrato) |
| `TC16-C32` | `abc` | `1` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C33` | `abc` | `25` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C34` | `abc` | `200` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero + orden: campo fuera de la lista blanca |
| `TC16-C35` | `REP:12:9` | `REP:12:9` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion + tamano: no cabe en un entero de la peticion |
| `TC16-C36` | `abc` | `201` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero + tamano: tamano sobre la cota (contrato) |

## Tabla D · Estados del recurso (GET/DELETE /{codigo} y PUT /{codigo}/reactivar)

**Modo:** `filas` · filas escritas a mano = 9

**Puerta que cubre:** SEG integridad: baja logica, reactivacion idempotente y codigos que no se reutilizan

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `operacion` | `DETALLE` | valido | - | GET /api/catalogo/pruebas/{codigo} |
| `operacion` | `BAJA` | valido | - | DELETE /api/catalogo/pruebas/{codigo} |
| `operacion` | `REACTIVAR` | valido | - | PUT /api/catalogo/pruebas/{codigo}/reactivar |
| `estadoRecurso` | `ACTIVA` | valido | - | prueba activa |
| `estadoRecurso` | `INACTIVA` | valido | - | prueba desactivada: detalle y reactivacion la ven |
| `estadoRecurso` | `NO_EXISTE` | **fuera de clase** | - | codigo inexistente |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `estadoRecurso` vale NO_EXISTE | `NO_ENCONTRADA` | `PRUEBA_NO_ENCONTRADA` |
| 2 | `operacion` vale BAJA | `BORRADO` | `-` |
| 3 | resto de casos | `OK` | `-` |

### Casos generados

| ID | operacion | estadoRecurso | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|
| `TC16-D01` | `DETALLE` | `ACTIVA` | `CREA_COD_ACTIVA` | OK | la consulta de detalle ve una prueba activa |
| `TC16-D02` | `DETALLE` | `INACTIVA` | `CREA_COD_INACTIVA` | OK | HALLAZGO COMP: el detalle ve la prueba inactiva (baja logica) - confirmar con el jefe de estudio |
| `TC16-D03` | `DETALLE` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | codigo inexistente |
| `TC16-D04` | `BAJA` | `ACTIVA` | `CREA_COD_ACTIVA` | BORRADO | baja logica: 204 sin cuerpo, la fila sigue existiendo con activa=false y emite asiento BAJA |
| `TC16-D05` | `BAJA` | `INACTIVA` | `CREA_COD_INACTIVA` | BORRADO | dar de baja algo ya bajado es idempotente: repite 204 sin asiento BAJA nuevo (queda anotado en la spec) |
| `TC16-D06` | `BAJA` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | codigo inexistente |
| `TC16-D07` | `REACTIVAR` | `ACTIVA` | `CREA_COD_ACTIVA` | OK | reactivar algo ya activo es idempotente: 200 y sigue activa, sin asiento REACTIVACION nuevo |
| `TC16-D08` | `REACTIVAR` | `INACTIVA` | `CREA_COD_INACTIVA` | OK | reactivacion: 200 con activa=true y asiento REACTIVACION (sobre una fila que ya tenia ALTA+BAJA) |
| `TC16-D09` | `REACTIVAR` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | codigo inactivado no encontrado (el PUD lo pide explicito) |

## Tabla E · Conjetura de errores (casos explicitos, sin combinacion)

**Modo:** `conjetura` · filas escritas a mano = 22

**Puerta que cubre:** SEG integridad y robustez: conjetura de errores sobre el alta, la modificacion y el detalle

### Casos generados

| ID | operacion | estadoRecurso | cuerpo | rutaExtra | asientos | estadoFinal | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|---|
| `TC16-E01` | `ALTA` | `NO_EXISTE` | `CODIGO_21_CARACTERES` | `-` | `-` | `-` | `NINGUNO` | CREADO | HALLAZGO: el PUD promete codigos de 2 a 20, pero el servicio valida contra CODIGO_MAX = 40 (comentado en LimitesCatalogo y en el .puml) |
| `TC16-E02` | `DETALLE` | `NO_EXISTE` | `NINGUNO` | `AUDITORIA` | `-` | `-` | `NINGUNO` | RUTA_INEXISTENTE | GET /{codigo}/auditoria: no hay ninguna mano del controlador que lo atienda -> 404 de Spring sin cuerpo ApiError. TC16-05 auditoria: HALLAZGO de COMP para Iter2 (falta el subrecurso de auditoria) |
| `TC16-E03` | `MODIFICACION` | `ACTIVA` | `CODIGO_PERTENECIENTE` | `OTRA_COD` | `-` | `-` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | PUT con codigo del cuerpo distinto del de la ruta: NO existe CODIGO_INCONSISTENTE, es IllegalArgumentException -> 400 DATOS_INVALIDOS |
| `TC16-E04` | `MODIFICACION` | `INACTIVA` | `VALIDO` | `-` | `3` | `False` | `CREA_COD_INACTIVA` | OK | caracterizacion: modificar una prueba dada de baja se admite (ALTA+BAJA+MODIFICACION) y sigue inactiva |
| `TC16-E05` | `BAJA` | `ACTIVA` | `NINGUNO` | `-` | `2` | `-` | `CREA_COD_ACTIVA` | BORRADO | tramos de auditoria: tras la baja queda ALTA+BAJA |
| `TC16-E06` | `BAJA` | `INACTIVA` | `NINGUNO` | `-` | `2` | `-` | `CREA_COD_INACTIVA` | BORRADO | baja repetida idempotente: los asientos siguen siendo ALTA+BAJA, no se emite un segundo BAJA |
| `TC16-E07` | `REACTIVAR` | `ACTIVA` | `NINGUNO` | `-` | `1` | `True` | `CREA_COD_ACTIVA` | OK | tramos de auditoria: reactivar algo ya activo no emite segundo asiento REACTIVACION y sigue activa |
| `TC16-E08` | `REACTIVAR` | `INACTIVA` | `NINGUNO` | `-` | `3` | `True` | `CREA_COD_INACTIVA` | OK | reaccion en cadena: la secuencia ALTA->BAJA->REACTIVACION queda completa (verificable desde el servicio con auditoriaVerificable) y el estado pasa a activa |
| `TC16-E09` | `REACTIVAR` | `INACTIVA` | `NINGUNO` | `-` | `3` | `True` | `CREA_COD_INACTIVA` | OK | reactivacion en dos tramos: al sentarse la reactivacion la cadena se ve al enumerar auditoria |
| `TC16-E10` | `ALTA` | `NO_EXISTE` | `ACTIVA_FALSA` | `-` | `1` | `True` | `NINGUNO` | CREADO | activa=false en el cuerpo del alta se ignora: el recurso se crea ACTIVO porque el alta fuerza activa=true |
| `TC16-E11` | `ALTA` | `ACTIVA` | `ACTIVA_FALSA` | `-` | `-` | `-` | `CREA_COD_ACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | activa=false en el cuerpo no convierte el alta en reactivacion: sigue siendo conflicto de codigo |
| `TC16-E12` | `MODIFICACION` | `ACTIVA` | `ACTIVA_FALSA` | `-` | `2` | `True` | `CREA_COD_ACTIVA` | OK | PUT con activa=false: el servicio ignora el campo y la prueba sigue activa (invariante de estado) |
| `TC16-E13` | `MODIFICACION` | `INACTIVA` | `ACTIVA_VERDADERA` | `-` | `3` | `False` | `CREA_COD_INACTIVA` | OK | PUT con activa=true en el cuerpo no reactiva: reactivar es su propia operacion |
| `TC16-E14` | `LISTADO` | `MIXTO` | `NINGUNO` | `-` | `-` | `-` | `CREA_MIXTO` | OK | COMP: el listado de gestion incluye las pruebas inactivas junto con las activas |
| `TC16-E15` | `MODIFICACION` | `ACTIVA` | `VALIDO` | `-` | `2` | `-` | `CREA_COD_ACTIVA` | OK | tramos de auditoria: modificar una prueba activa suma un asiento MODIFICACION sobre la misma fila |
| `TC16-E16` | `MODIFICACION` | `ACTIVA` | `VALIDO` | `-` | `-` | `-` | `CREA_COD_ACTIVA` | OK | la modificacion respeta el codigo de la ruta: PUT con el codigo de la fila creada |
| `TC16-E17` | `ALTA` | `NO_EXISTE` | `VALIDO` | `-` | `1` | `True` | `NINGUNO` | CREADO | linea de base de auditoria: el alta crea exactamente un asiento ALTA y el recurso queda activo |
| `TC16-E18` | `ALTA` | `NO_EXISTE` | `DURACION_COMO_TEXTO` | `-` | `-` | `-` | `NINGUNO` | CREADO | Jackson convierte '60' a numero: se acepta y se crea - caracterizacion |
| `TC16-E19` | `ALTA` | `NO_EXISTE` | `CAMPO_DESCONOCIDO` | `-` | `-` | `-` | `NINGUNO` | CREADO | Jackson descarta campos desconocidos: se ignora y se crea - caracterizacion |
| `TC16-E20` | `ALTA` | `NO_EXISTE` | `CATEGORIA_MINUSCULA` | `-` | `-` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la lista blanca de categorias distingue mayusculas |
| `TC16-E21` | `ALTA` | `NO_EXISTE` | `CODIGO_CON_ESPACIOS` | `-` | `-` | `-` | `NINGUNO` | CREADO | no hay regla de recorte: el codigo se guarda con espacios - caracterizacion |
| `TC16-E22` | `ALTA` | `DUPLICA_ACTIVA` | `LIMITES_CERO` | `-` | `-` | `-` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | la validacion de cotas (0 no admitido, limites ausentes) se aplica antes de comprobar el duplicado: codigo repetido con cuerpo fuera de cota da 400 DATOS_INVALIDOS, no 409 (con cuerpo valido el duplicado si da 409: ver las filas CONFLICTO de la tabla A) |

## Como se ejecutan

```
mvn -B -ntp -pl iso2-catalogo -am test
```

Los CSV de trabajo estan en `docs/test/iter01/` y su copia en el classpath de pruebas (`iso2-catalogo/src/test/resources/tdop/`) es identica: el generador las escribe a la vez.

