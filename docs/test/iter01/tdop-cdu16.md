# Evidencia TDOP de CDU16 (iter01 · CFG-01)

Metodo: Clases de equivalencia + teorema de valores limite + conjetura de errores, compuestas con pairwise (todo par de niveles de factores distintos queda cubierto).

Generador: `python tools/pairwise.py docs/test/iter01/tdop-cdu16-espec.json --resources iso2-catalogo/src/test/resources/tdop` (semilla 20261108).
Las mismas filas las ejecutan los JUnit parametrizados de `es.uclm.iso2.catalogo.tdop` y las tablas de `docs/tc/cdu16-tc-humanos.md`: un ID vale para las dos.

## Leyenda de tokens

| Token | Significado |
|---|---|
| `EMPTY` | cadena vacia |
| `SPACE` | un solo espacio |
| `VACIO` | parametro ausente (null) |
| `REP:<n>:<c>` | el caracter <c> repetido n veces |
| `-` | no aplica |

## Estados esperados (nombre simbolico -> HTTP)

| Estado | HTTP |
|---|---|
| `CREADO` | 201 |
| `OK` | 200 |
| `INVALIDOS` | 400 |
| `NO_ENCONTRADA` | 404 |
| `CONFLICTO` | 409 |
| `NO_ACEPTABLE` | 415 |

## Reparto de casos

| Tabla | Operacion | Modo | Filas |
|---|---|---|---|
| A | Alta de prueba (POST /api/catalogo/pruebas) | pairwise | 58 |
| B | Modificacion de prueba (PUT /api/catalogo/pruebas/{codigo}) | full | 60 |
| C | Listado del catalogo (GET /api/catalogo/pruebas) | pairwise | 33 |
| D | Estados del recurso (GET/DELETE /{codigo} y PUT /{codigo}/reactivar) | full | 9 |
| E | Conjetura de errores (casos explicitos, sin combinacion) | conjetura | 18 |
| **Total** |  |  | **178** |

## Tabla A · Alta de prueba (POST /api/catalogo/pruebas)

**Modo:** `pairwise` · producto de niveles = 2000000 · filas generadas = 58 · pares obligatorios cubiertos = 708/708 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF correccion: el alta rechaza las cotas del esquema y del contrato (D35) sin llegar al INSERT

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `codigo` | `EMPTY` | **fuera de clase** | clase de equivalencia vacio | codigo en blanco (contrato) |
| `codigo` | `SPACE` | **fuera de clase** | clase de equivalencia solo espacios | codigo en blanco (contrato) |
| `codigo` | `A` | valido | minimo observado en el contrato | longitud minima usable |
| `codigo` | `REP:40:C` | valido | justo la cota | justo la cota de almacenamiento |
| `codigo` | `REP:41:C` | **fuera de clase** | un punto sobre la cota | pasa la cota de codigo (40) |
| `nombre` | `EMPTY` | **fuera de clase** | clase de equivalencia vacio | nombre en blanco (contrato) |
| `nombre` | `T` | valido | minimo observado en el contrato | longitud minima usable |
| `nombre` | `NOMBRE-TIPO-41` | valido | valor tipico | nombre real de 41 caracteres |
| `nombre` | `REP:120:N` | valido | justo la cota | justo la cota de nombre |
| `nombre` | `REP:121:N` | **fuera de clase** | un punto sobre la cota | pasa la cota de nombre (120) |
| `categoria` | `EMPTY` | **fuera de clase** | clase de equivalencia vacio | categoria en blanco (contrato) |
| `categoria` | `RX` | valido | valor tipico | dentro de la lista blanca |
| `categoria` | `MED-NUC` | valido | valor tipico alto | la categoria admitida mas larga |
| `categoria` | `USG` | **fuera de clase** | fuera de dominio | categoria fuera de la lista blanca |
| `categoria` | `REP:21:C` | **fuera de clase** | un punto sobre la cota | pasa la longitud de categoria (20) |
| `requiereCita` | `true` | valido | valor tipico | con cita |
| `requiereCita` | `false` | valido | valor tipico | sin cita |
| `duracionMinutos` | `0` | **fuera de clase** | bajo el minimo del contrato | duracion no positiva (contrato) |
| `duracionMinutos` | `1` | valido | minimo del contrato | minimo del contrato |
| `duracionMinutos` | `60` | valido | valor tipico | valor tipico |
| `duracionMinutos` | `1440` | valido | justo la cota | justo la cota |
| `duracionMinutos` | `1441` | **fuera de clase** | un punto sobre la cota | pasa la cota de duracion (1440) |
| `preparacion` | `EMPTY` | **fuera de clase** | clase de equivalencia vacio | preparacion en blanco (contrato) |
| `preparacion` | `AYUNAR-4H` | valido | valor tipico | texto tipico |
| `preparacion` | `REP:1000:P` | valido | justo la cota | justo la cota de texto |
| `preparacion` | `REP:1001:P` | **fuera de clase** | un punto sobre la cota | pasa la cota de preparacion (1000) |
| `ayunoHoras` | `-1` | **fuera de clase** | bajo el minimo del contrato | ayuno negativo (contrato) |
| `ayunoHoras` | `0` | valido | minimo del contrato | minimo del contrato |
| `ayunoHoras` | `12` | valido | valor tipico | valor tipico |
| `ayunoHoras` | `720` | valido | justo la cota | justo la cota |
| `ayunoHoras` | `721` | **fuera de clase** | un punto sobre la cota | pasa la cota de ayuno (720) |
| `contraindicaciones` | `EMPTY` | **fuera de clase** | clase de equivalencia vacio | contraindicaciones en blanco (contrato) |
| `contraindicaciones` | `NINGUNA` | valido | valor tipico | valor pactado para sin contraindicaciones |
| `contraindicaciones` | `REP:1000:D` | valido | justo la cota | justo la cota de texto |
| `contraindicaciones` | `REP:1001:D` | **fuera de clase** | un punto sobre la cota | pasa la cota de contraindicaciones (1000) |
| `plazoCancelacionHoras` | `0` | **fuera de clase** | bajo el minimo del contrato | plazo no positivo (contrato) |
| `plazoCancelacionHoras` | `1` | valido | minimo del contrato | minimo del contrato |
| `plazoCancelacionHoras` | `24` | valido | valor tipico | valor tipico |
| `plazoCancelacionHoras` | `8760` | valido | justo la cota | justo la cota |
| `plazoCancelacionHoras` | `8761` | **fuera de clase** | un punto sobre la cota | pasa la cota de plazo (8760) |
| `maxCancelaciones` | `0` | valido | minimo del contrato | minimo del contrato: cancelacion prohibida |
| `maxCancelaciones` | `3` | valido | valor tipico | valor tipico |
| `maxCancelaciones` | `100` | valido | justo la cota | justo la cota |
| `maxCancelaciones` | `101` | **fuera de clase** | un punto sobre la cota | pasa la cota de cancelaciones (100) |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | cualquier nivel fuera de clase valida | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | resto de casos | `CREADO` | `-` |

### Casos generados

| ID | codigo | nombre | categoria | requiereCita | duracionMinutos | preparacion | ayunoHoras | contraindicaciones | plazoCancelacionHoras | maxCancelaciones | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `TC16-A01` | `A` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `1` | `0` | `CREA_OTRA_ACTIVA` | CREADO | todos los niveles dentro de las clases validas \| valores limite: codigo=A (minimo observado en el contrato), nombre=T (minimo observado en el contrato), categoria=RX (valor tipico), requiereCita=true (valor tipico), duracionMinutos=1 (minimo del contrato), preparacion=AYUNAR-4H (valor tipico), ayunoHoras=0 (minimo del contrato), contraindicaciones=NINGUNA (valor tipico), plazoCancelacionHoras=1 (minimo del contrato), maxCancelaciones=0 (minimo del contrato) |
| `TC16-A02` | `EMPTY` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `0` | `REP:1000:D` | `24` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A03` | `SPACE` | `REP:120:N` | `RX` | `false` | `1440` | `REP:1000:P` | `12` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A04` | `REP:41:C` | `NOMBRE-TIPO-41` | `RX` | `true` | `60` | `REP:1000:P` | `720` | `NINGUNA` | `1` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: pasa la cota de codigo (40) |
| `TC16-A05` | `REP:40:C` | `EMPTY` | `MED-NUC` | `false` | `1` | `REP:1000:P` | `12` | `REP:1000:D` | `24` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: nombre en blanco (contrato) |
| `TC16-A06` | `REP:40:C` | `REP:121:N` | `MED-NUC` | `false` | `1440` | `REP:1000:P` | `720` | `REP:1000:D` | `1` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: pasa la cota de nombre (120) |
| `TC16-A07` | `A` | `REP:120:N` | `EMPTY` | `true` | `1440` | `REP:1000:P` | `720` | `REP:1000:D` | `24` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria en blanco (contrato) |
| `TC16-A08` | `A` | `NOMBRE-TIPO-41` | `USG` | `true` | `60` | `AYUNAR-4H` | `12` | `REP:1000:D` | `8760` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria fuera de la lista blanca |
| `TC16-A09` | `REP:40:C` | `REP:120:N` | `REP:21:C` | `false` | `1` | `AYUNAR-4H` | `12` | `NINGUNA` | `1` | `3` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: pasa la longitud de categoria (20) |
| `TC16-A10` | `REP:40:C` | `T` | `MED-NUC` | `true` | `0` | `AYUNAR-4H` | `720` | `REP:1000:D` | `8760` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: duracion no positiva (contrato) |
| `TC16-A11` | `A` | `T` | `MED-NUC` | `false` | `1441` | `REP:1000:P` | `0` | `NINGUNA` | `24` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: pasa la cota de duracion (1440) |
| `TC16-A12` | `REP:40:C` | `NOMBRE-TIPO-41` | `RX` | `true` | `1440` | `EMPTY` | `0` | `REP:1000:D` | `24` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: preparacion en blanco (contrato) |
| `TC16-A13` | `A` | `NOMBRE-TIPO-41` | `RX` | `true` | `1` | `REP:1001:P` | `720` | `NINGUNA` | `8760` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: pasa la cota de preparacion (1000) |
| `TC16-A14` | `REP:40:C` | `T` | `RX` | `true` | `60` | `AYUNAR-4H` | `-1` | `NINGUNA` | `24` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: ayuno negativo (contrato) |
| `TC16-A15` | `A` | `T` | `RX` | `true` | `1440` | `AYUNAR-4H` | `721` | `NINGUNA` | `1` | `3` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: pasa la cota de ayuno (720) |
| `TC16-A16` | `A` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `12` | `EMPTY` | `1` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: contraindicaciones en blanco (contrato) |
| `TC16-A17` | `A` | `REP:120:N` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `REP:1001:D` | `8760` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: pasa la cota de contraindicaciones (1000) |
| `TC16-A18` | `A` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `0` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: plazo no positivo (contrato) |
| `TC16-A19` | `A` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `8761` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: pasa la cota de plazo (8760) |
| `TC16-A20` | `A` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `1` | `101` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| maxCancelaciones: pasa la cota de cancelaciones (100) |
| `TC16-A21` | `REP:40:C` | `REP:120:N` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `12` | `REP:1000:D` | `0` | `3` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: plazo no positivo (contrato) |
| `TC16-A22` | `A` | `EMPTY` | `RX` | `true` | `60` | `AYUNAR-4H` | `0` | `NINGUNA` | `1` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: nombre en blanco (contrato) |
| `TC16-A23` | `A` | `REP:121:N` | `RX` | `true` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `24` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: pasa la cota de nombre (120) |
| `TC16-A24` | `A` | `NOMBRE-TIPO-41` | `RX` | `false` | `0` | `REP:1000:P` | `0` | `NINGUNA` | `1` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: duracion no positiva (contrato) |
| `TC16-A25` | `REP:40:C` | `NOMBRE-TIPO-41` | `RX` | `true` | `1441` | `AYUNAR-4H` | `12` | `REP:1000:D` | `1` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: pasa la cota de duracion (1440) |
| `TC16-A26` | `EMPTY` | `T` | `RX` | `true` | `1` | `AYUNAR-4H` | `12` | `NINGUNA` | `1` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A27` | `SPACE` | `T` | `MED-NUC` | `true` | `1` | `AYUNAR-4H` | `0` | `REP:1000:D` | `1` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A28` | `A` | `T` | `REP:21:C` | `true` | `60` | `REP:1000:P` | `0` | `REP:1000:D` | `24` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: pasa la longitud de categoria (20) |
| `TC16-A29` | `A` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `1` | `REP:1000:P` | `-1` | `REP:1000:D` | `1` | `3` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: ayuno negativo (contrato) |
| `TC16-A30` | `A` | `T` | `MED-NUC` | `false` | `1` | `EMPTY` | `12` | `NINGUNA` | `1` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: preparacion en blanco (contrato) |
| `TC16-A31` | `REP:41:C` | `T` | `MED-NUC` | `false` | `1` | `AYUNAR-4H` | `0` | `REP:1000:D` | `24` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: pasa la cota de codigo (40) |
| `TC16-A32` | `REP:40:C` | `T` | `EMPTY` | `false` | `1` | `AYUNAR-4H` | `0` | `NINGUNA` | `1` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria en blanco (contrato) |
| `TC16-A33` | `REP:40:C` | `T` | `USG` | `false` | `1` | `REP:1000:P` | `0` | `NINGUNA` | `1` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria fuera de la lista blanca |
| `TC16-A34` | `REP:40:C` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `1` | `REP:1000:P` | `721` | `REP:1000:D` | `24` | `0` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: pasa la cota de ayuno (720) |
| `TC16-A35` | `REP:40:C` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `0` | `EMPTY` | `24` | `0` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: contraindicaciones en blanco (contrato) |
| `TC16-A36` | `REP:40:C` | `T` | `MED-NUC` | `false` | `60` | `REP:1001:P` | `0` | `REP:1000:D` | `1` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: pasa la cota de preparacion (1000) |
| `TC16-A37` | `REP:40:C` | `T` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `12` | `REP:1001:D` | `1` | `3` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: pasa la cota de contraindicaciones (1000) |
| `TC16-A38` | `REP:40:C` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `12` | `REP:1000:D` | `8761` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: pasa la cota de plazo (8760) |
| `TC16-A39` | `REP:40:C` | `NOMBRE-TIPO-41` | `MED-NUC` | `false` | `60` | `REP:1000:P` | `12` | `REP:1000:D` | `24` | `101` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| maxCancelaciones: pasa la cota de cancelaciones (100) |
| `TC16-A40` | `SPACE` | `NOMBRE-TIPO-41` | `RX` | `true` | `60` | `AYUNAR-4H` | `720` | `NINGUNA` | `24` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A41` | `A` | `NOMBRE-TIPO-41` | `EMPTY` | `true` | `60` | `AYUNAR-4H` | `12` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria en blanco (contrato) |
| `TC16-A42` | `A` | `NOMBRE-TIPO-41` | `REP:21:C` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `8760` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: pasa la longitud de categoria (20) |
| `TC16-A43` | `A` | `NOMBRE-TIPO-41` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `REP:1001:D` | `24` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: pasa la cota de contraindicaciones (1000) |
| `TC16-A44` | `A` | `REP:120:N` | `USG` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `24` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| categoria: categoria fuera de la lista blanca |
| `TC16-A45` | `A` | `REP:120:N` | `RX` | `true` | `1440` | `REP:1001:P` | `12` | `NINGUNA` | `24` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: pasa la cota de preparacion (1000) |
| `TC16-A46` | `REP:41:C` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `12` | `NINGUNA` | `8760` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: pasa la cota de codigo (40) |
| `TC16-A47` | `A` | `REP:120:N` | `RX` | `true` | `60` | `EMPTY` | `720` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| preparacion: preparacion en blanco (contrato) |
| `TC16-A48` | `A` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `EMPTY` | `8760` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| contraindicaciones: contraindicaciones en blanco (contrato) |
| `TC16-A49` | `EMPTY` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| codigo: codigo en blanco (contrato) |
| `TC16-A50` | `A` | `NOMBRE-TIPO-41` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `0` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: plazo no positivo (contrato) |
| `TC16-A51` | `A` | `REP:120:N` | `RX` | `true` | `0` | `AYUNAR-4H` | `12` | `NINGUNA` | `24` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: duracion no positiva (contrato) |
| `TC16-A52` | `A` | `REP:121:N` | `RX` | `true` | `60` | `AYUNAR-4H` | `12` | `NINGUNA` | `8760` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: pasa la cota de nombre (120) |
| `TC16-A53` | `A` | `REP:120:N` | `RX` | `true` | `60` | `AYUNAR-4H` | `721` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: pasa la cota de ayuno (720) |
| `TC16-A54` | `A` | `REP:120:N` | `RX` | `true` | `1441` | `AYUNAR-4H` | `720` | `NINGUNA` | `8760` | `3` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| duracionMinutos: pasa la cota de duracion (1440) |
| `TC16-A55` | `A` | `EMPTY` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `8760` | `100` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| nombre: nombre en blanco (contrato) |
| `TC16-A56` | `A` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `-1` | `NINGUNA` | `8760` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| ayunoHoras: ayuno negativo (contrato) |
| `TC16-A57` | `A` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `8760` | `101` | `CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| maxCancelaciones: pasa la cota de cancelaciones (100) |
| `TC16-A58` | `A` | `REP:120:N` | `RX` | `true` | `1440` | `AYUNAR-4H` | `720` | `NINGUNA` | `8761` | `100` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | hay al menos un nivel fuera de la clase valida \| plazoCancelacionHoras: pasa la cota de plazo (8760) |

## Tabla B · Modificacion de prueba (PUT /api/catalogo/pruebas/{codigo})

**Modo:** `full` · producto de niveles = 60 · filas generadas = 60

**Puerta que cubre:** AF correccion: coherencia ruta/cuerpo, estado del recurso y cotas en la modificacion

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `estadoRecurso` | `ACTIVA` | valido | - | prueba dada de alta |
| `estadoRecurso` | `INACTIVA` | valido | - | prueba dada de baja logica: se puede modificar |
| `estadoRecurso` | `NO_EXISTE` | **fuera de clase** | - | la ruta no apunta a ninguna fila |
| `codigoRuta` | `IGUAL` | valido | - | ruta y cuerpo coinciden |
| `codigoRuta` | `DISTINTO_INVENTADO` | **fuera de clase** | - | la ruta no coincide con el cuerpo |
| `codigoRuta` | `DISTINTO_EXISTENTE` | **fuera de clase** | - | la ruta apunta a otra prueba existente |
| `codigoRuta` | `LARGO41` | **fuera de clase** | un punto sobre la cota | ruta y cuerpo de 41 caracteres: pasa la cota de codigo |
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
| `TC16-B01` | `ACTIVA` | `IGUAL` | `VALIDO` | `-` | `-` | `CREA_COD_ACTIVA` | OK | modificacion admitida |
| `TC16-B02` | `ACTIVA` | `IGUAL` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: nombre de 121 caracteres |
| `TC16-B03` | `ACTIVA` | `IGUAL` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B04` | `ACTIVA` | `IGUAL` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: maxCancelaciones 101 |
| `TC16-B05` | `ACTIVA` | `IGUAL` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B06` | `ACTIVA` | `DISTINTO_INVENTADO` | `VALIDO` | `-` | `-` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo |
| `TC16-B07` | `ACTIVA` | `DISTINTO_INVENTADO` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B08` | `ACTIVA` | `DISTINTO_INVENTADO` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B09` | `ACTIVA` | `DISTINTO_INVENTADO` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B10` | `ACTIVA` | `DISTINTO_INVENTADO` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B11` | `ACTIVA` | `DISTINTO_EXISTENTE` | `VALIDO` | `-` | `-` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente |
| `TC16-B12` | `ACTIVA` | `DISTINTO_EXISTENTE` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: nombre de 121 caracteres |
| `TC16-B13` | `ACTIVA` | `DISTINTO_EXISTENTE` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B14` | `ACTIVA` | `DISTINTO_EXISTENTE` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: maxCancelaciones 101 |
| `TC16-B15` | `ACTIVA` | `DISTINTO_EXISTENTE` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_ACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B16` | `ACTIVA` | `LARGO41` | `VALIDO` | `-` | `-` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo |
| `TC16-B17` | `ACTIVA` | `LARGO41` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B18` | `ACTIVA` | `LARGO41` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B19` | `ACTIVA` | `LARGO41` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B20` | `ACTIVA` | `LARGO41` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B21` | `INACTIVA` | `IGUAL` | `VALIDO` | `-` | `-` | `CREA_COD_INACTIVA` | OK | modificacion admitida |
| `TC16-B22` | `INACTIVA` | `IGUAL` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: nombre de 121 caracteres |
| `TC16-B23` | `INACTIVA` | `IGUAL` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B24` | `INACTIVA` | `IGUAL` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: maxCancelaciones 101 |
| `TC16-B25` | `INACTIVA` | `IGUAL` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B26` | `INACTIVA` | `DISTINTO_INVENTADO` | `VALIDO` | `-` | `-` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo |
| `TC16-B27` | `INACTIVA` | `DISTINTO_INVENTADO` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B28` | `INACTIVA` | `DISTINTO_INVENTADO` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B29` | `INACTIVA` | `DISTINTO_INVENTADO` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B30` | `INACTIVA` | `DISTINTO_INVENTADO` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B31` | `INACTIVA` | `DISTINTO_EXISTENTE` | `VALIDO` | `-` | `-` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente |
| `TC16-B32` | `INACTIVA` | `DISTINTO_EXISTENTE` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: nombre de 121 caracteres |
| `TC16-B33` | `INACTIVA` | `DISTINTO_EXISTENTE` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B34` | `INACTIVA` | `DISTINTO_EXISTENTE` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: maxCancelaciones 101 |
| `TC16-B35` | `INACTIVA` | `DISTINTO_EXISTENTE` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_INACTIVA,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B36` | `INACTIVA` | `LARGO41` | `VALIDO` | `-` | `-` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo |
| `TC16-B37` | `INACTIVA` | `LARGO41` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B38` | `INACTIVA` | `LARGO41` | `CATEGORIA_FUERA` | `categoria` | `USG` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B39` | `INACTIVA` | `LARGO41` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B40` | `INACTIVA` | `LARGO41` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `CREA_COD_INACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B41` | `NO_EXISTE` | `IGUAL` | `VALIDO` | `-` | `-` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | no hay fila que modificar \| estadoRecurso: la ruta no apunta a ninguna fila |
| `TC16-B42` | `NO_EXISTE` | `IGUAL` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| estadoRecurso: la ruta no apunta a ninguna fila + validezCuerpo: nombre de 121 caracteres |
| `TC16-B43` | `NO_EXISTE` | `IGUAL` | `CATEGORIA_FUERA` | `categoria` | `USG` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| estadoRecurso: la ruta no apunta a ninguna fila + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B44` | `NO_EXISTE` | `IGUAL` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| estadoRecurso: la ruta no apunta a ninguna fila + validezCuerpo: maxCancelaciones 101 |
| `TC16-B45` | `NO_EXISTE` | `IGUAL` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el cuerpo pasa una cota del servicio \| estadoRecurso: la ruta no apunta a ninguna fila + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B46` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `VALIDO` | `-` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo |
| `TC16-B47` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B48` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `CATEGORIA_FUERA` | `categoria` | `USG` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B49` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B50` | `NO_EXISTE` | `DISTINTO_INVENTADO` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta no coincide con el cuerpo + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B51` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `VALIDO` | `-` | `-` | `NINGUNO,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente |
| `TC16-B52` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `NINGUNO,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: nombre de 121 caracteres |
| `TC16-B53` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `CATEGORIA_FUERA` | `categoria` | `USG` | `NINGUNO,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B54` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `NINGUNO,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: maxCancelaciones 101 |
| `TC16-B55` | `NO_EXISTE` | `DISTINTO_EXISTENTE` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `NINGUNO,CREA_OTRA_ACTIVA` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: la ruta apunta a otra prueba existente + validezCuerpo: preparacion de 1001 caracteres |
| `TC16-B56` | `NO_EXISTE` | `LARGO41` | `VALIDO` | `-` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo |
| `TC16-B57` | `NO_EXISTE` | `LARGO41` | `NOMBRE_LARGO121` | `nombre` | `REP:121:N` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: nombre de 121 caracteres |
| `TC16-B58` | `NO_EXISTE` | `LARGO41` | `CATEGORIA_FUERA` | `categoria` | `USG` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: categoria fuera de la lista blanca |
| `TC16-B59` | `NO_EXISTE` | `LARGO41` | `MAXCANCEL_101` | `politica.maxCancelaciones` | `101` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: maxCancelaciones 101 |
| `TC16-B60` | `NO_EXISTE` | `LARGO41` | `PROTO_LOMAX1001` | `protocolo.preparacion` | `REP:1001:P` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | el codigo de la ruta no coincide con el del cuerpo (se mira antes de buscar la fila) \| estadoRecurso: la ruta no apunta a ninguna fila + codigoRuta: ruta y cuerpo de 41 caracteres: pasa la cota de codigo + validezCuerpo: preparacion de 1001 caracteres |

## Tabla C · Listado del catalogo (GET /api/catalogo/pruebas)

**Modo:** `pairwise` · producto de niveles = 180 · filas generadas = 33 · pares obligatorios cubiertos = 81/81 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

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
| `TC16-C02` | `-1` | `25` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C03` | `REP:12:9` | `200` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C04` | `abc` | `1` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C05` | `2` | `0` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C06` | `200` | `201` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C07` | `0` | `REP:12:9` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |
| `TC16-C08` | `2` | `1` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| orden: campo fuera de la lista blanca |
| `TC16-C09` | `-1` | `1` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C10` | `REP:12:9` | `1` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C11` | `-1` | `200` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C12` | `REP:12:9` | `25` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C13` | `200` | `0` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C14` | `2` | `201` | `codigo` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C15` | `0` | `0` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C16` | `0` | `201` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C17` | `0` | `25` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| orden: campo fuera de la lista blanca |
| `TC16-C18` | `200` | `200` | `INVENTADO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| orden: campo fuera de la lista blanca |
| `TC16-C19` | `2` | `25` | `categoria` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=2 (valor tipico), tamano=25 (valor tipico), orden=categoria (valor tipico) |
| `TC16-C20` | `2` | `200` | `duracionMinutos` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=2 (valor tipico), tamano=200 (justo la cota), orden=duracionMinutos (valor tipico) |
| `TC16-C21` | `200` | `25` | `duracionMinutos` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=200 (limite de paginacion), tamano=25 (valor tipico), orden=duracionMinutos (valor tipico) |
| `TC16-C22` | `200` | `REP:12:9` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |
| `TC16-C23` | `abc` | `200` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C24` | `2` | `REP:12:9` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |
| `TC16-C25` | `abc` | `25` | `VACIO` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C26` | `0` | `200` | `VACIO` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=0 (minimo del contrato), tamano=200 (justo la cota), orden=VACIO (ausencia) |
| `TC16-C27` | `200` | `1` | `VACIO` | `NINGUNO` | OK | listado admitido \| valores limite: pagina=200 (limite de paginacion), tamano=1 (minimo del contrato), orden=VACIO (ausencia) |
| `TC16-C28` | `REP:12:9` | `1` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no cabe en un entero de la peticion |
| `TC16-C29` | `0` | `201` | `categoria` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano sobre la cota (contrato) |
| `TC16-C30` | `-1` | `1` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: pagina negativa (contrato) |
| `TC16-C31` | `abc` | `1` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| pagina: no es un numero |
| `TC16-C32` | `0` | `0` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: tamano bajo el minimo (contrato) |
| `TC16-C33` | `0` | `REP:12:9` | `duracionMinutos` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | un parametro de consulta esta fuera de clase o mal tipado \| tamano: no cabe en un entero de la peticion |

## Tabla D · Estados del recurso (GET/DELETE /{codigo} y PUT /{codigo}/reactivar)

**Modo:** `full` · producto de niveles = 9 · filas generadas = 9

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
| 2 | resto de casos | `OK` | `-` |

### Casos generados

| ID | operacion | estadoRecurso | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|
| `TC16-D01` | `DETALLE` | `ACTIVA` | `CREA_COD_ACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D02` | `DETALLE` | `INACTIVA` | `CREA_COD_INACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D03` | `DETALLE` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | la ruta no apunta a ninguna fila \| estadoRecurso: codigo inexistente |
| `TC16-D04` | `BAJA` | `ACTIVA` | `CREA_COD_ACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D05` | `BAJA` | `INACTIVA` | `CREA_COD_INACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D06` | `BAJA` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | la ruta no apunta a ninguna fila \| estadoRecurso: codigo inexistente |
| `TC16-D07` | `REACTIVAR` | `ACTIVA` | `CREA_COD_ACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D08` | `REACTIVAR` | `INACTIVA` | `CREA_COD_INACTIVA` | OK | operacion de estado admitida (baja y reactivacion son idempotentes) |
| `TC16-D09` | `REACTIVAR` | `NO_EXISTE` | `NINGUNO` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | la ruta no apunta a ninguna fila \| estadoRecurso: codigo inexistente |

## Tabla E · Conjetura de errores (casos explicitos, sin combinacion)

**Modo:** `conjetura` · filas escritas a mano = 18

**Puerta que cubre:** SEG integridad y robustez: conjetura de errores sobre el alta, la modificacion y el detalle

### Casos generados

| ID | operacion | estadoRecurso | cuerpo | rutaExtra | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|
| `TC16-E01` | `ALTA` | `NO_EXISTE` | `VALIDO` | `-` | `CREA_OTRA_ACTIVA` | CREADO | linea de base: alta valida sobre un catalogo con otra fila |
| `TC16-E02` | `ALTA` | `ACTIVA` | `VALIDO` | `-` | `CREA_COD_ACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | reutilizar el codigo de una prueba activa |
| `TC16-E03` | `ALTA` | `INACTIVA` | `VALIDO` | `-` | `CREA_COD_INACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | los codigos no se reutilizan: dar de alta una fila dada de baja es conflicto, no reactivacion implicita |
| `TC16-E04` | `ALTA` | `ACTIVA` | `ACTIVA_FALSA` | `-` | `CREA_COD_ACTIVA` | CONFLICTO `CODIGO_DUPLICADO` | activa=false en el cuerpo no convierte el alta en reactivacion |
| `TC16-E05` | `MODIFICACION` | `ACTIVA` | `ACTIVA_FALSA` | `-` | `CREA_COD_ACTIVA` | OK | activa=false en el cuerpo se ignora: la prueba sigue activa |
| `TC16-E06` | `MODIFICACION` | `INACTIVA` | `ACTIVA_VERDADERA` | `-` | `CREA_COD_INACTIVA` | OK | activa=true en el cuerpo no reactiva: reactivar es su propia operacion |
| `TC16-E07` | `ALTA` | `NO_EXISTE` | `VACIO_JSON` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | cuerpo {} : faltan todos los campos del contrato |
| `TC16-E08` | `ALTA` | `NO_EXISTE` | `SIN_PROTOCOLO` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | falta el objeto protocolo: el contrato exige anidados no nulos |
| `TC16-E09` | `ALTA` | `NO_EXISTE` | `MALFORMADO` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | JSON malformado: HttpMessageNotReadableException |
| `TC16-E10` | `ALTA` | `NO_EXISTE` | `SIN_CUERPO` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | POST sin cuerpo |
| `TC16-E11` | `ALTA` | `NO_EXISTE` | `TEXTO_PLANO` | `-` | `NINGUNO` | NO_ACEPTABLE `CONTENIDO_NO_ACEPTADO` | Content-Type text/plain: 415 con codigo estable desde D35 (antes caia en el capturador general y devolvia 500) |
| `TC16-E12` | `ALTA` | `NO_EXISTE` | `CAMPO_DESCONOCIDO` | `-` | `NINGUNO` | CREADO | Jackson descarta campos desconocidos: se ignora y se crea - caracterizacion |
| `TC16-E13` | `ALTA` | `NO_EXISTE` | `DURACION_COMO_TEXTO` | `-` | `NINGUNO` | CREADO | Jackson convierte 60 a numero: se acepta - caracterizacion |
| `TC16-E14` | `ALTA` | `NO_EXISTE` | `CATEGORIA_MINUSCULA` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | la lista blanca de categorias distingue mayusculas |
| `TC16-E15` | `ALTA` | `NO_EXISTE` | `CATEGORIA_20_FUERA` | `-` | `NINGUNO` | INVALIDOS `DATOS_INVALIDOS` | categoria de 20 caracteres cabe en el VARCHAR pero esta fuera de la lista blanca |
| `TC16-E16` | `ALTA` | `NO_EXISTE` | `CODIGO_CON_ESPACIOS` | `-` | `NINGUNO` | CREADO | no hay regla de recorte: el codigo se guarda con espacios - caracterizacion |
| `TC16-E17` | `DETALLE` | `ACTIVA` | `NINGUNO` | `COD-MINUS` | `CREA_COD_ACTIVA` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | H2 distingue mayusculas: el detalle en minusculas no encuentra la fila - caracterizacion, revisar con MySQL (collation ci) |
| `TC16-E18` | `DETALLE` | `ACTIVA` | `NINGUNO` | `COD+ESP` | `CREA_COD_ACTIVA` | NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | codigo de ruta con espacio final: no se recorta - caracterizacion |

## Como se ejecutan

```
mvn -B -ntp -pl iso2-catalogo -am test
```

Los CSV de trabajo estan en `docs/test/iter01/` y su copia en el classpath de pruebas (`iso2-catalogo/src/test/resources/tdop/`) es identica: el generador las escribe a la vez.

