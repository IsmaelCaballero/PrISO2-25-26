# Diseño de los conjuntos de casos de prueba · CDU14 «Gestionar listas de espera» (CFG-07 / Iter7)

**Qué es este documento.** Informe detallado (con valor pedagógico) del diseño de los
conjuntos de casos de prueba de la Iter7: cómo se aplicaron **clases de equivalencia**,
**conjetura de errores**, la **teoría de valores límite** y la composición **pairwise**
sobre la superficie nueva de `iso2-citas` 1.2.0, y por qué el resultado es el que es.
Escrito el 15/11 al cerrar el diseño y **actualizado el 18/11 al cerrar la iteración**,
con los datos de ejecución reales (los apartados §5, §7, §9, §10 y §12 recogen lo que la
matriz desmintió o confirmó desde entonces).
El producto del diseño (la matriz ejecutable) está en
`docs/test/iter07/tdop-cdu14-espec.json` (única fuente de verdad, D33: de ella salen los
CSV de `docs/test/iter07/` y de `iso2-citas/src/test/resources/tdop/`, y el bloque
TDOP de `docs/tc/cdu14-tc-humanos.md`; nadie edita las proyecciones a mano). Contrato
vinculante: issue #47. Regla de mínimos D24: 1 feliz + ≥2 error + 1 permisos.

**Superficie bajo prueba (8 puntos de entrada):** `POST /api/listas-espera` (alta),
`GET /api/listas-espera` (cola con filtros `pruebaCodigo`/`profesionalId`/`estado`),
`GET /api/listas-espera/{esperaId}`, `POST /api/listas-espera/{esperaId}/prioridad`
(reubicación), `POST /api/listas-espera/{esperaId}/asignar`,
`POST /api/listas-espera/{esperaId}/retirar`, `GET /api/listas-espera/auditoria-verificable`
y `GET /api/listas-espera/{esperaId}/auditoria`.

## 1. Los dos hechos del diseño que condicionan toda la matriz

Todo el diseño de casos gira alrededor de dos decisiones de diseño (D31 trajo los
diagramas antes que el código; la matriz las prueba desde HTTP):

1. **La posición no se almacena.** El orden de la cola lo calcula un comparador total,
   `ListaEspera.porOrdenDeEspera()`: prioridad descendente → `fechaSolicitud`
   ascendente → `esperaId` ascendente como último desempate. Consecuencia de diseño de
   prueba: *cada fila que termina en estado de éxito reafirma el invariante* — la cola
   servida por `GET` coincide elemento a elemento con el orden del comparador aplicado
   al repositorio, y `posicion` vale 1..n. Es la puerta propia de la iteración
   («reordenación determinista probada») afirmada desde la capa HTTP. Como el último
   desempate es un id, **el orden es único**: dos lecturas seguidas del mismo contenido
   no pueden diferir; el determinismo no depende del azar del motor de BD.
2. **El doble clic se corta en tres capas**, y cada capa tiene su tabla y su código:
   alta repetida → 409 `CONFLICTO_ALTA_DUPLICADA` (clave anti-duplicado
   `pacienteId|pruebaCodigo` mientras la entrada está ACTIVA, liberada al estado
   terminal); reubicación con el escalón que ya se tiene → 409
   `CONFLICTO_REUBICACION_REDUNDANTE` (y ningún eslabón `REUBICACION` nuevo en la
   auditoría); asignación repetida sobre la MISMA entrada → 409
   `CONFLICTO_ENTRADA_NO_ACTIVA` (la máquina de estados gana a la reserva de hueco).
   La red física, para entradas DISTINTAS que apuntan al mismo hueco, es el
   `UNIQUE (profesional_id, inicio)` de `cita_hueco` + `@Version` heredados de CDU12
   (409 `CONFLICTO_HUECO_OCUPADO`).

Reubicar está modelado como **cambio de escalón de prioridad**, no como «mover a la
posición N»: con posiciones arbitrarias el contrato no sería determinista y la puerta
de la iteración no sería demostrable. Bajar de escalón no siempre cede el paso: en
empate de marca de tiempo gana la entrada anterior (es lo que afirman las filas
`esperadoCola` de la tabla D).

## 2. Metodología: el espejo TDOP y cómo encajan las cuatro técnicas

Se mantiene el espejo ya estabilizado en CDU12 y CDU19: **factores → niveles → reglas
de precedencia → estado esperado**, con los niveles inválidos marcados `valido: false`
a propósito. Sobre ese esqueleto, las cuatro técnicas clásicas intervienen así:

- **Clases de equivalencia** aportan los factores y sus niveles: cada factor define
  particiones válidas e inválidas (tabla §3). La regla de oro de la composición: un
  caso por clase, y una clase representada por un nivel; el resto de la fila queda en
  base válida.
- **Teoría de valores límite** aporta los niveles «borde» dentro de cada clase
  (tabla §4): la última longitud que cabe y la primera que revienta la columna, el
  primer minuto de la franja y el minuto que la desborda, el borde inferior y superior
  de la escala de prioridad, el valor nulo de la clave anti-duplicado y los empates de
  marca de tiempo del desempate. El «teorema del error único» se fuerza con
  `noCombinaInvalidos`: **ninguna fila combina dos niveles inválidos**, así que todo
  rechazo tiene un único motivo atribuible (si una fila con dos faltas devolviera un
  código u otro, no sabríamos cuál de las dos reglas falló).
- **Conjetura de errores** aporta la tabla H (§5): ataques específicos que nacen de
  la experiencia de iteraciones anteriores (HALLAZGOS de CDU19 y lecciones de CDU12),
  donde el generador automático no llega.
- **Pairwise** compone: todo par de niveles de factores DISTINTOS queda cubierto
  (§6). Las tablas de transiciones, secuencias y auditoría (C/D/E/G) se escriben a
  mano porque una secuencia no es una combinación de factores.

Semilla fija del generador: `20261116`. Regenerar dos veces seguidas produce los
mismos CSV (verificable con `python tools/pairwise.py ... --only A,B,F`).

## 3. Clases de equivalencia por tabla compuesta

Marca ✗ = nivel inválido (`valido: false`), que se combina solo con niveles válidos.

### Tabla A · lectura de la cola (`GET /api/listas-espera`) — 40 filas

| factor (tipo) | niveles válidos | niveles inválidos |
|---|---|---|
| `cola` (montaje) | `MIXTA` (5 activas, 3 prioridades, marcas distintas) · `EMPALTE_FECHA` (misma marca, prioridad distinta) · `EMPALTE_TODO` (prioridad y marca idénticas: solo `esperaId` desempata) · `TERMINALES` (2 activas + ASIGNADA + RETIRADA) · `SOLO_TERMINALES` (ninguna activa) · `VACIA` | — |
| `prueba` (petición) | `NINGUNO` (sin parámetro) · `CONOCIDA` · `OTRA` (`api-prueba-dup`, recorta la cola) · `NO_EXISTE` (bien formado, fuera de catálogo) | ✗ `VACIO` (parámetro presente y vacío) |
| `profesional` (petición) | `NINGUNO` · `MEDICO` · `SIN_AGENDA` (id válido sin agenda: la cola no depende de la agenda) · `NO_EXISTE` (200 con cola vacía: un filtro que no casa con nadie no es un error) | ✗ `VACIO` |
| `estado` (petición) | `NINGUNO` · `ACTIVA` · `ASIGNADA` · `RETIRADA` · `MINUSCULA` (canon CDU06: `desdeNombre` recorta y mayusculiza) | ✗ `INVENTADO` (`esperando`) · ✗ `VACIO` |

Principio de contrato que prueba la tabla: **ningún filtro fuera de clase se convierte
en cola vacía**. Devolver 200 con `[]` ante `estado=` vacío o absurdo engañaría al
Administrativo (creería que no hay nadie esperando); se responde 400
`DATOS_INVALIDOS` (estado simbólico `FILTRO_INVALIDO`). En cambio un `profesionalId`
inexistente SÍ es 200 con cola vacía: el filtro es correcto, solo no coincide con
nadie. La sutileza `pruebaCodigo` inexistente = 400 (el catálogo es cerrado y el alta
sí devuelve 404 por prueba) queda fijada como desambiguación deliberada y aparece
también en H.

### Tabla B · alta en la cola (`POST /api/listas-espera`) — 55 filas

| factor | niveles válidos | niveles inválidos |
|---|---|---|
| `paciente` | `TIPICO` · `MAX` (40 = longitud de la columna) | ✗ `LARGO` (41) · ✗ `VACIO` · ✗ `AUSENTE` |
| `prueba` | `CONOCIDA` · `OTRA` (clave anti-duplicado) · `NO_EXISTE` | ✗ `LARGO` (41) · ✗ `VACIO` · ✗ `AUSENTE` |
| `prioridad` | `ORDINARIA` (borde inferior) · `PREFERENTE` · `URGENTE` (borde superior) · `MINUSCULA` · `ESPACIOS` (`'  urgente  '`: recortar antes de comparar, lección de CDU12) | ✗ `INVENTADA` (`CRITICA`) · ✗ `AUSENTE` |
| `solicitud` | `NINGUNA` (opcional: RF14.1 permite cola por prueba sin solicitud) · `CREADA` · `CITADA` y `PRESCRITA` (el alta NO mira el estado: entra; la aptitud se comprueba al asignar) · `NO_EXISTE` (referencia colgante cortada por el servicio) | ✗ `VACIO` |
| `duplicado` (montaje) | `NO` · `MISMA` (activa con mismo paciente+prueba) · `TRAS_RETIRADA` y `TRAS_ASIGNACION` (terminales: la clave pasó a NULL, el reingreso es legal) · `OTRO_PACIENTE` y `OTRA_PRUEBA` (la clave es `paciente|prueba`: sí pueden entrar) | — |

### Tabla F · asignar cita desde la cola (`POST .../asignar`) — 64 filas

| factor | niveles válidos | niveles inválidos |
|---|---|---|
| `esperaId` | `AUT` · `INEXISTENTE` (UUID bien formado nunca sembrado) | ✗ `MAL_FORMATADO` (ruta tipada a UUID: corte antes del servicio) |
| `estadoEntrada` (montaje) | `ACTIVA` · `ASIGNADA` · `RETIRADA` | — |
| `profesional` | `MEDICO` (franja 08:00–15:00) · `SIN_AGENDA` y `DESCONOCIDO` (el `AgendaNoEncontradoException` de CDU13 se traduce aquí a **409 `CONFLICTO_FUERA_DE_FRANJA`**: sin agenda no hay ninguna franja donde quepa el hueco; `DESCONOCIDO` sigue el mismo camino porque nadie valida el id contra un directorio) | ✗ `VACIO` · ✗ `AUSENTE` |
| `inicio` | `AUT` (13:00) · `APERTURA_JUSTA` (08:00) · `CIERRE_LIMITRO` (14:30, última de 30' que cabe) · `PASADA_CIERRE` (15:00) · `ANTERIOR_APERTURA` (07:00) · `FESTIVO` (09:00 del 2026-11-10) · `DOMINGO` (13:00: la franja falsa cubre todos los días; domingo es clase válida) | ✗ `MAL_FORMATADO` (`manana al mediodia`) · ✗ `AUSENTE` |
| `duracion` | `AUSENTE` (la pide el catálogo: `api-prueba` fija 30') · `D30` · `D1` (mínimo positivo) · `D0` (canon CDU12: 0 = no fijada, se usa la del catálogo; CDU14 espeja sin redefinir) · `EXCESIVA` (600' desborda el cierre aunque el inicio esté dentro) | ✗ `D_NEGATIVA` (−5) |
| `hueco` (montaje) | `LIBRE` · `EXACTO` (intersección máxima) · `SOLAPE` (intersección mínima: un minuto basta) · `FANTASMA_EXACTO` (la cita que ocupaba ese inicio fue CANCELADA: cancelar/realizar/expirar borran la fila de `cita_hueco`; el hueco vuelve a estar libre y el asignar debe pasar) | — |
| `solicitud` (montaje) | `CREADA` · `CITADA` (aptitud falla por la solicitud, no por la cola) · `NINGUNA` (alta sin solicitud es legal y asignarle cita no) | — |

Nota sobre `FANTASMA_EXACTO`: el hueco «hueso» no se puede sembrar con la API porque
toda cancelación libera la fila de `cita_hueco`; la montura lo inserta directamente
en el repositorio, y eso también es una prueba: si el servicio diera `HUECO_OCUPADO`
contra un inicio sin fila, estaría consultando la agenda con la lógica equivocada.

### Factores de montaje y `noEmparejar` (A: `cola`; B: `duplicado`)

Ambos factores **montan el mundo, no son la petición**. No generan pares obligatorios
por dos razones: (1) combinar dos rechazos suyos con niveles inválidos de la petición
taparía el motivo real del rechazo (se rompería el error único atribuible), y (2) el
voraz del §6 tendería a ocultarlos tras combinaciones de la petición. Se prueban **uno
por fila sobre base válida**, y el generador verifica que ningún nivel suyo queda sin
ejecutar (si falta, se declara una `filasBase`). Por eso la tabla A necesita además 7
filas base y B 9: son las que garantizan que los seis montajes de `cola` y los seis de
`duplicado` aparecen realmente.

## 4. Valores límite declarados (teoría pesada de valores límite)

Cada nivel borde lleva su `criterio` en la spec, que el proyector vierte en la columna
`nota` de los CSV: la prueba visible de la teoría. Lista completa del contrato nuevo:

| borde | inferior aceptado | primer rechazado | cómo se comprueba |
|---|---|---|---|
| `pacienteId` | 40 caracteres (longitud de `paciente_id`) | 41 | 400 `DATOS_INVALIDOS`, no 500 de BD: la guarda del servicio **espeja las longitudes de la columna** para no convertir un desborde en error de integridad |
| `pruebaCodigo` | 40 | 41 | ídem (columna `codigo_prueba`) |
| `motivo` (reubicar/retirar) | 300 (`MAX300`) | 301 (`LARGO301`) | espejo de `motivo_fin VARCHAR(300)` |
| escala de prioridad | `ORDINARIA` / `PREFERENTE` / `URGENTE` | `CRITICA` (inventada) | los tres escalones como clases de borde; además `MINUSCULA` y `ESPACIOS` prueban el canon `desdeNombre` (trim+toUpperCase) |
| franja de la agenda | `APERTURA_JUSTA` 08:00 · `CIERRE_LIMITRO` 14:30 (última de 30' que cabe) | `ANTERIOR_APERTURA` 07:00 · `PASADA_CIERRE` 15:00 (coincide con el cierre) · `EXCESIVA` desde inicio válido | heredados del banco de `PruebasCitas` de CDU12; `FESTIVO` es clase válida que se vuelve fuera de franja por calendario, y `DOMINGO` sigue siendo válida (no hay franja de fin de semana en el banco) |
| duración | `D1` (mínimo positivo) · `D0` (= no fijada, canon CDU12) | `D_NEGATIVA` (−5) | el 0 es frontera de *significado*, no de tamaño: espeja el canon sin redefinirlo |
| clave anti-duplicado | `NULL` (terminales: reingreso legal `TRAS_RETIRADA`/`TRAS_ASIGNACION`) | fila ACTIVA con `pacienteId\|pruebaCodigo` repetido | UNIQUE **nullable**: la unicidad solo se aplica mientras la entrada está ACTIVA; es el valor límite nulo de la teoría aplicado a una clave compuesta |
| desempate del orden | dos marcas idénticas (`EMPALTE_TODO`: decide `esperaId`) | — | no hay rechazo: el límite aquí es del **invariante de determinismo**, probado por las filas OK de A y D |
| hueco solapado | solape de 1 minuto (`SOLAPE`) · hueco fantasma liberado (`FANTASMA_EXACTO`) | intersección exacta (`EXACTO`) | la frontera es la intersección, no la igualdad de inicio |

## 5. Conjetura de errores (tabla H, 13 filas escritas a mano)

Los ataques salen de la experiencia acumulada, no de la composición:

| ataque | idea y origen |
|---|---|
| `CUERPO-ILEGIBLE` | texto suelto donde va el JSON del alta: 400 con código estable, nunca 500 (lección de CDU12 y CDU19) |
| `CONTENT-TYPE-ERRONEO` | `text/plain` en el alta: se conjeturó 415 «porque Spring lo resuelve solo» y **era falso** — el consejo del módulo, al llevar un *catch-all* de `Exception`, se comía la excepción de tipo de medio y salía **500 `ERROR_INTERNO`** (DEF-16). Arreglado con un manejador propio que devuelve **415 con el código estable `CONTENIDO_NO_ACEPTADO`** que D35/D25 fijaron para el resto de módulos. La lección no es el 415: es que un consejo con `assignableTypes` sigue necesitando un manejador por familia de error |
| `COLISION-RUTA-VARIABLE` | **POST** `/api/listas-espera/auditoria-verificable/prioridad`: como todo en la cola es POST, el método coincide y la colisión cae en el path variable de UUID → 400 `DATOS_INVALIDOS`. Con PUT, Spring daría 405 antes de mirar el UUID, y no se probaría lo que se quiere probar. HALLAZGO 3 de CDU19: `MAL_FORMATADO` es INVALIDOS, no NO_ENCONTRADA (corregido en CDU12) |
| `METODO-NO-DECLARADO` | POST sobre `/{esperaId}/auditoria` con UUID válido no sembrado: la ruta coincide y el método no → 405 sin cuerpo propio (estabilizado en CDU19) |
| `DOBLE-ASIGNAR` | doble clic real: la montura ejecuta dos intentos seguidos y comprueba el **segundo** (el primero debe dar 201). El segundo encuentra la entrada ASIGNADA: por precedencia, la máquina de estados gana a la reserva de hueco → 409 `CONFLICTO_ENTRADA_NO_ACTIVA` sin llegar a consultar la agenda. La red física para entradas distintas la prueba E (`ASIGNAR_MISMO_HUECO_ENTRADAS_DISTINTAS` → 409 `CONFLICTO_HUECO_OCUPADO`). Si el segundo diera 201, la SEG de no duplicar hueco estaría reventada y la fila quedaría en rojo como HALLAZGO |
| `DOBLE-RETIRAR` | segunda retirada → 409 estado (coherente con C y D) |
| `FILTRO-CON-PARAM-REPETIDO` | `?pruebaCodigo=X&pruebaCodigo=Y`: la conjetura de diseno era **200 con el primer valor** (canon de binding que habia que fijar). Resulto ser **falsa**: al atar el parametro repetido a un `String`, Spring **junta** los dos valores (`api-prueba,api-otra`) y esa prueba conjunta no esta en el catalogo → **400 `DATOS_INVALIDOS`**. Corregida en la spec al cerrar la tabla H (DEF-18), confirmada por HTTP contra el servidor montado. Lo que si se sostiene es lo que se queria comprobar: no hay 500 y la cola queda intacta |
| `PRUEBA-FUERA-DE-CATALOGO-EN-FILTRO` | → 400 `FILTRO_INVALIDO` (desambiguación con el 404 del alta, §3-A) |
| `ASIGNAR-CON-PARAMS-EN-QUERY` | `inicio` en query cuando se envía cuerpo → el cuerpo manda y falta `inicio` → 400 (el contrato es el cuerpo) |
| restantes | id de ruta con prefijo de otra entidad, `estado` en mayúsculas/raro, alta con `esperaId` propio en el cuerpo (no se acepta: lo genera el servidor), retirada sin `motivo` |

## 6. Pairwise: composición, cuentas y por qué no «full»

El generador (`tools/pairwise.py`) es un voraz con poda: arranca con las `filasBase`
fijadas a mano, tapa los pares obligatorios eligiendo siempre el nivel que aporta más
pares nuevos y poda filas redundantes. Cifras reales de la composición:

| tabla | tipo | producto de niveles | filas | pares observados / obligatorios | cobertura |
|---|---|---|---|---|---|
| A | pairwise | 1050 | 40 | 124 / 90 | 100 % |
| B | pairwise | 7 560 | 55 | 230 / 186 | 100 % |
| F | pairwise | 29 160 | 64 | 439 / 439 | 100 % |
| C, D, E, G | filas a mano | — | 30+27+12+12 | — | — |
| H | conjetura a mano | — | 13 | — | — |

Los **obligatorios** se calculan como todos los pares entre factores que emparejan
(excluidos los de montaje por `noEmparejar`), menos los pares (inválido, inválido) por
`noCombinaInvalidos`. La aritmética, para que se pueda repetir a lápiz:

- A: factores de petición `prueba`(5) × `profesional`(5) × `estado`(7): 25+35+35 = 95
  pares; menos 5 pares (inv,inv) → **90 obligatorios**. Los 34 pares adicionales
  observados son de `cola` con los demás factores: el voraz los «ve» gratis al completar
  las filas, pero no los exigía.
- B: cuatro factores de petición → 30+35+30+42+36+42 = 215; menos 29 (inv,inv) →
  **186 obligatorios**; observados 230 (44 extras del montaje `duplicado`).
- F: siete factores → Σ pares = 452; menos 13 (inv,inv) → **439 obligatorios** y
  **439 observados**: en F no aparece ni un solo par (inv,inv) de más, lo que confirma
  empíricamente el teorema del error único sobre esta tabla.

Por qué pairwise y no full: el producto de F (29 160) es inviable como casos reales de
HTTP, y el de B (7 560) tampoco aporta: el defecto típico de un contrato REST vive en
**la interacción de dos factores** (hueco ocupado + solicitud citada, filtro absurdo +
mundo vacío), no en quíntuples. Con `esperadoStatus` como proyección de una función
`reglas` declarativa en la spec, pairwise da cobertura de interacciones binaria al
25 % del coste del full de A (40 filas frente a 1050).

Sombras del voraz y `filasBase`: el voraz prioriza pares de la petición, así que los
niveles de los factores de montaje (pocos y caros: cada uno es un `setup` distinto)
pueden no aparecer en ninguna fila. Por eso A declara 7 filas base, B 9 y F 17: son
los mundos que garantizan cada montaje y los casos donde la regla de precedencia
decide (p. ej. en F, `INEXISTENTE` con montaje `SIN_AGENDA`: gana el 404 sobre el 409
de franja).
Además el generador comprueba tras la poda que **todo nivel de todo factor aparece en
alguna fila** («no observa los estados exigidos» es error duro).

## 7. Tablas enumeradas a mano (C, D, E, G): qué prueban y por qué no se componen

- **C · operaciones × estado (30 filas).** Es la matriz de transiciones
  DETALLE/PRIORIDAD/ASIGNAR/RETIRAR/AUDITORIA × {ACTIVA, ASIGNADA, RETIRADA} más las
  entradas inexistentes y los cuerpos inválidos. Una transición es una secuencia de
  montaje; los factores pairwise no la describen. Distribución: 10 OK, 8
  `NO_ACTIVA`, 5 `NO_ENCONTRADA`, 3 `SOLICITUD_NO_APTA`, 2 `FUERA_DE_FRANJA`,
  1 `ASIGNADO`, 1 `HUECO_OCUPADO`. Aquí se afirman las precedencias de §8. La columna
  `operacion` se renombró `DETALLE_ESPERA` en la proyección porque la palabra
  `DETALLE` ya está tomada en el proyector por `GET /api/citas/{id}`.
- **D · reubicación con cola esperada escrita (27 filas).** Cada fila lleva
  `esperadoCola`: la cola EXACTA que debe servir el `GET` tras el cambio de escalón
  (secuencia de ids). Es la prueba directa de la puerta propia: no basta con un 200,
  hay que servir el orden del comparador, incluidos los casos «bajar de escalón no
  cede el paso» (empate → gana marca anterior) y los empates totales. Lleva también
  el doble clic de reubicación (`REDUNDANTE`, 5 filas) con la exigencia de que **no se
  escriba un segundo eslabón `REUBICACION`**. `esperadoCola` aquí = cola filtrada por
  `estado=ACTIVA` (las terminales no interfieren con el orden de la cola viva).
- **E · secuencias de doble clic y concurrencia (12 filas).** `ALTA_ALTA`,
  `ALTA_RETIRAR_ALTA`, `ALTA_ASIGNAR_ASIGNAR`, `ASIGNAR_CONCURRENTES`,
  `ALTA_ALTA_CONCURRENTES`, `RETIRAR_CONCURRENTES`,
  `ASIGNAR_MISMO_HUECO_ENTRADAS_DISTINTAS` y compañía (distribución: 3 `CREADO` · 3
  `UN_OK_Y_OTRO_409` · 2 `NO_ACTIVA` · 1 `ALTA_DUPLICADA` · 1 `HUECO_OCUPADO` · 1 `OK` ·
  1 `REDUNDANTE`). El pseudo-estado `UN_OK_Y_OTRO_409` (3 filas) afirma que de dos golpes
  concurrentes sale un multiset {201, 409}: exactamente uno escribe, el otro recibe un
  código de conflicto. Aquí la red física son la UNIQUE de `cita_hueco` con el `@Version`
  heredado de CDU12 **y el `@Version` propio de `ListaEspera`**, que se añadió tarde: la
  fila `TC14-E10` (`RETIRAR_CONCURRENTES`) estuvo en rojo porque la entrada de cola no
  tenía control optimista y los dos hilos escribían (DEF-17, corregido en `d8f95fe` con
  columna `version` en `V1.6.0` para h2 y mysql). Es el mismo fallo que se cometió en
  Iter5, y por eso la propuesta de un chequeo ArchUnit mecánico de `@Version` está en la
  Deuda nº 28 del STATUS. En `TC14-E11` el esperado es `HUECO_OCUPADO` **porque son
  entradas distintas**: la máquina de estados no puede parar el segundo golpe, para la
  base de datos.
- **G · auditoría de la cola (12 filas).** Cadena propia `espera_auditoria` con
  acciones `ALTA/REUBICACION/ASIGNACION/RETIRADA`, génesis literal `GENESIS` (mismo
  canon que `AuditoriaCitas`), autor y motivo obligatorios en reubicaciones y
  retiradas, y las tres manipulaciones desde el repositorio (`SEM-TAMPER:ENLAZE`,
  `BORRADO`, `REORDEN`) que `auditoria-verificable` debe detectar como rotura. La
  cadena de CDU12 (`cita_auditoria`) no se toca: cada raíz se verifica por separado;
  CDU18 (Iter15) unirá las dos cadenas.

## 8. Precedencias fijadas (el pegamento de las tablas C, F y H)

El contrato no dice solo QUÉ códigos, sino EN QUÉ ORDEN se comprueban; sin esto dos
implementaciones correctas darían códigos distintos y la matriz sería inestable:

- **alta:** 400 sintaxis > 404 `PRUEBA_NO_ENCONTRADA` (la prueba es el eje de la cola,
  se responde antes de mirar la solicitud) > 404 `SOLICITUD_NO_ENCONTRADA` > 409
  `CONFLICTO_ALTA_DUPLICADA`.
- **reubicar / retirar:** 400 (incluye el motivo: obligatorio y ≤300 pertenece a la
  sintaxis, así que un motivo en blanco se responde 400 aunque el cambio también
  fuera redundante — la guarda precede a la búsqueda, como en CDU12) > 404 > 409
  estado > 409 `REDUNDANTE`.
- **asignar:** 400 > 404 > 409 estado > 409 `SOLICITUD_NO_APTA` (incluida solicitud
  nula; apta solo `CREADA`) > 409 `FUERA_DE_FRANJA` (aquí cae también el profesional sin
  agenda: la excepcion de agenda de CDU13 se traduce a conflicto de franja, sin 500)
  > 409 `HUECO_OCUPADO`. La aptitud se comprueba **antes de tocar la agenda**: es una
  guarda propia de la cola, más barata y más explicativa.

## 9. Desviaciones y exclusiones documentadas

- **`GET /api/listas-espera/{esperaId}/auditoria` no estaba en la tabla de endpoints
  de #47** (que lista 7). La verificación global `auditoria-verificable` sí estaba. El
  octavo endpoint sale del diseño (diagramas D31) y de la matriz; es una **desviación
  anotada del contrato**, no una omisión: sin él, la auditoría por entrada de la
  reubicación (pista SQ «reordenar cola por prioridad») no se puede observar.
- **RBAC fuera de la matriz TDOP:** el módulo `iso2-citas` se prueba con su montura de
  pruebas, sin filtro de seguridad. La cadena real de `ConfigSeguridad` se comprueba
  en `iso2-server-app` (TC14-08 recorre los 8 endpoints con `admin`/`recepcionista`/
  `medico`/`celador`, y las pruebas de integración existentes). Puerta R2: TC de
  permisos para el 100 % de los endpoints nuevos. Lectura (`GET`×4):
  ADMINISTRADOR+MEDICO+RECEPCIONISTA; escritura (`POST`×4): ADMINISTRADOR+RECEPCIONISTA.
- **UI Vaadin fuera de CFG-07** (el mapa de configuraciones no sube `iso2-client-app`):
  la protección de doble clic se prueba donde el duplicado puede existir, en el
  servidor.
- **El `profesionalId` sin agenda no da 500: da 409 con codigo `CONFLICTO_FUERA_DE_FRANJA`.**
  La spec lo habia fijado como 500 `ERROR_INTERNO` en cuatro filas F, pero el codigo
  resuelve antes: sin agenda no hay ninguna franja donde quepa el hueco, asi que la cola
  responde 409 con codigo propio. **Se corrigio la spec, no el codigo** (`9c8510e`: TC14-F16,
  F17, F48 y F50 pasan de `ERROR_INTERNO` a `FUERA_DE_FRANJA`). Va en DEF-18 como
  expectativa falsa, no como defecto.
- **Se distinguen dos estados de enrutamiento, y solo uno es observable en la montura
  aislada.** `METODO` = ruta que si existe con un metodo no declarado → **405 sin cuerpo
  `ApiError`** (lo resuelve Spring); `RUTA_INEXISTENTE` = ruta fuera del mapa del modulo →
  **404 sin cuerpo `ApiError`**. En la montura de pruebas de `iso2-citas` no hay filtro de
  seguridad y los dos se ven tal cual; en la app montada, las rutas sin mapear contestan
  **401 `AUTENTICACION_REQUERIDA`**, que es la deuda abierta **D-09** y no se toca en esta
  iteracion.
- **Sin prueba automatizada de 401/403 sobre los 8 endpoints de cola.** La matriz TDOP
  corre sin filtro de seguridad, y las pruebas de `iso2-server-app` no cubren la cola.
  Por eso el **TC14-08 es puerta R2 obligatoria** (D30: no se fusiona sin TC humanos), y
  por eso queda anotado como hueco: el dia que se monte la matriz sobre la app real
  (deuda D-10) esos 401/403 pasan a ser filas de matriz.
- **Cambiar expectativas sobre la marcha solo esta justificado alineandolas con
  comportamiento confirmado.** Las unicas cinco expectativas de spec modificadas entre el
  relevo del 17/11 (`b4edce0`) y el cierre son E02/E03/E12 (`OK` → `CREADO`), H02
  (`esperadoCodigo` `-` → `CONTENIDO_NO_ACEPTADO`, aqui si cambio el codigo: DEF-16) y H06
  (`OK` → `FILTRO_INVALIDO`). Auditado fila a fila contra el JSON de `b4edce0`; D33 lo
  permite en las filas de conjetura y en las de spec erronea, y es lo unico que se hizo.

## 10. Recuento final y verificaciones de sanidad

**253 filas** (A 40, B 55, C 30, D 27, E 12, F 64, G 12, H 13) sobre un mapa canónico de
**19 estados simbólicos** (`tdop-cdu14-esperados.csv`, con su `status` + `codigo`) que el
JUnit traduce a status+codigo. De esos 19, la matriz usa **18**: el que no aparece en
ninguna fila es `RUTA_INEXISTENTE` (404 de Spring fuera del mapa del módulo), porque el
ataque de prefijo de otra entidad cae en una ruta que **sí** existe con un UUID mal
formateado → `INVALIDOS`. El 404 de ruta inexistente solo se observa en la app montada,
donde además se lo come el filtro de seguridad (deuda D-09). Distribucion real por estado,
leida de los CSV generados:

| tabla | distribución |
|---|---|
| A | 22 FILTRO_INVALIDO · 18 OK |
| B | 45 INVALIDOS · 6 CREADO · 2 PRUEBA_NO_ENCONTRADA · 1 ALTA_DUPLICADA · 1 SOLICITUD_NO_ENCONTRADA |
| C | 10 OK · 8 NO_ACTIVA · 5 NO_ENCONTRADA · 3 SOLICITUD_NO_APTA · 2 FUERA_DE_FRANJA · 1 ASIGNADO · 1 HUECO_OCUPADO |
| D | 12 OK · 5 INVALIDOS · 5 REDUNDANTE · 3 NO_ACTIVA · 2 NO_ENCONTRADA |
| E | 3 CREADO · 3 UN_OK_Y_OTRO_409 · 2 NO_ACTIVA · 1 ALTA_DUPLICADA · 1 HUECO_OCUPADO · 1 OK · 1 REDUNDANTE |
| F | 38 INVALIDOS · 14 FUERA_DE_FRANJA · 4 ASIGNADO · 3 NO_ACTIVA · 2 HUECO_OCUPADO · 2 SOLICITUD_NO_APTA · 1 NO_ENCONTRADA |
| G | 9 OK · 1 INVALIDOS · 1 NO_ACTIVA · 1 REDUNDANTE |
| H | 4 INVALIDOS · 2 FILTRO_INVALIDO · 2 METODO · 1 CONTENIDO · 1 CREADO · 1 CUERPO_ILEGIBLE · 1 NO_ACTIVA · 1 OK |

Dos lecturas de salud del diseño: **el 27,3 % de las filas (69/253) son salidas de éxito
y el 72,7 % son camino de error**, así que la matriz no se apoya en caminos felices; y
**`ERROR_INTERNO` = 0 filas**, que es la propiedad transversal del diseño (ninguna
entrada, ni cuerpo ilegible, ni colisión de ruta, ni filtro repetido, debe producir 500).
Los dos tropiezos de la iteración fueron justo sobre esta propiedad, y hay que separarlos:
el 415 que salía como 500 era **defecto de código** (DEF-16, `medioNoSoportado` en el
consejo), y las cuatro filas F que llevaban `ERROR_INTERNO` eran **expectativa errónea de
la spec** (DEF-18, arreglado en `9c8510e` sin tocar el código).

Sanidad verificada sobre las proyecciones: **0 filas duplicadas** en las 8 tablas
(misma norma que CDU12: ninguna fila repite valores salvo el id) y regeneración
estable con la misma semilla. Los TC humanos (TC14-01..09) quedan en la cabecera de
`docs/tc/cdu14-tc-humanos.md` y trazan sobre los mismos IDs que el JUnit parametrizado
`Cdu14MatrizTdopTest`.

## 11. Cómo regenerar y verificar

```text
python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources iso2-citas/src/test/resources/tdop
python tools/tc_plantillas.py docs/test/iter07/tdop-cdu14-espec.json --tc docs/tc/cdu14-tc-humanos.md
```

El primero escribe los 9 CSV (8 tablas + mapa de estados) en `docs/test/iter07/` y en
`iso2-citas/src/test/resources/tdop/` y comprueba la cobertura de pares; el segundo
proyecta la evidencia `docs/test/iter07/tdop-cdu14.md` e inserta el bloque
`<!-- TDOP:BEGIN/END -->` de los TC humanos. **Los CSV y el bloque TDOP no se editan a
mano: se regenera el bloque** (D33).

Dos trampas Operativas que cuesta repetir (por eso van escritas):

- **Ejecutar un solo método de la matriz en silencio no ejecuta nada.** Las ocho tablas son
  `@ParameterizedTest` con `@MethodSource`; `mvn -Dtest=Cdu14MatrizTdopTest#tdopTablaH` sin
  `-Dsurefire.failIfNoSpecifiedTests=false -am` termina en verde con **0 pruebas**. La
  matriz completa dura **2 min 14 s**: es más rápido correrla entera.
- **La cobertura solo existe después de `verify`.** `jacoco.csv` no se escribe en `test`, y
  cuatro módulos del reactor (`iso2-comunicacion`, `iso2-informes`, `iso2-pacientes`,
  `iso2-reporting`) tienen `jacoco.csv` con **cero líneas ejecutables**: cualquier script de
  métricas tiene que proteger la división por cero o inventa porcentajes.

## 12. Balance al cerrar la iteración

Estado al cierre: **matriz 253/253 en verde**. Último `mvn -o -ntp verify` completo (sobre
`21d9a3c`, el mismo árbol de código que `HEAD`: lo posterior son documentos): **1092 pruebas
en el reactor, 0 fallos, 0 saltadas**; JaCoCo global **2059/2376 líneas (86,7 %) y 621/809
ramas (76,8 %)**, y en `iso2-citas` **696/744 líneas (93,5 %) y 257/287 ramas (89,5 %)**.

Lo que el diseño rindió, en números de defecto (TASADEFECTOS §3): **3 defectos de 253
filas, 0 fugados** (DEF-16 y DEF-17 de severidad F4, DEF-18 la familia de expectativas
erróneas, F3). De los dos defectos de código, **ninguno lo habría encontrado una prueba
unitaria del servicio**: el 415 se pierde en la capa web y el control optimista solo se
ve con dos hilos contra HTTP. Es el argumento más fuerte a favor del espejo TDOP sobre la
superficie real, y la razón por la que la matriz se ejecuta contra el controlador montado
en vez de llamar al servicio.

Y una corrección honesta de este informe: dos conjeturas de las que aquí se escribieron
como hechos **resultaron falsas** y hubo que reescribirlas al cerrar la tabla H, que es
justo lo que la tabla H existe para descubrir. El `Content-Type` erróneo no daba 415 «sin
pasar por el consejo» (daba 500, DEF-16), y Spring no se queda con el primer valor de un
`@RequestParam` repetido (los junta en una cadena y devuelve 400, §5).
