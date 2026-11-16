# Evidencia TDOP de CDU14 (iter07 · CFG-07)

Metodo: Clases de equivalencia + teorema de valores limite + conjetura de errores, compuestas por parejas (todo par de niveles de factores distintos queda cubierto) y enumeracion escrita a mano para transiciones, secuencias de doble clic y auditoria.

Generador: `python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources iso2-citas/src/test/resources/tdop` (semilla 20261116).
Las mismas filas las ejecutan los JUnit parametrizados de `es.uclm.iso2.citas.tdop` y las tablas de `docs/tc/cdu14-tc-humanos.md`: un ID vale para las dos.

## Leyenda de tokens

| Token | Significado |
|---|---|
| `SEMILLA-VACIA` | banco de pruebas sin ninguna entrada en la cola (y sin solicitudes ni citas) |
| `SEMILLA-MIXTA` | cinco entradas ACTIVA de la prueba api-prueba con las tres prioridades y marcas de tiempo distintas (A,B,C,D,E) |
| `SEMILLA-EMPALTE-FECHA` | dos entradas ACTIVA de prioridades distintas con la MISMA marca de tiempo: la prioridad decide y la fecha no aporta nada |
| `SEMILLA-EMPALTE-TODO` | dos entradas ACTIVA con prioridad y marca de tiempo identicas: solo esperaId desempata, y por eso el orden es unico entre dos lecturas consecutivas |
| `SEMILLA-TERMINALES` | dos entradas ACTIVA y dos terminales (ASIGNADA con cita en 09:00 y RETIRADA con motivoFin): sin filtro se sirven las cuatro, el filtro estado deja fuera las terminales |
| `SEMILLA-SOLO-TERMINALES` | una ASIGNADA y una RETIRADA, ninguna ACTIVA: el filtro estado=ACTIVA debe dar 200 con cola vacia |
| `SEMILLA-COLA-TRES` | cola canonicA de tres entradas ACTIVA etiquetadas A (URGENTE, t1), B (PREFERENTE, t2) y C (ORDINARIA, t3), las tres de api-paciente con api-prueba y marcas de tiempo ordenadas |
| `SEMILLA-COLA-EMPALTE-BC` | como SEMILLA-COLA-TRES pero B y C comparten marca de tiempo gemela: el desempate por esperaId es lo unico que rompe el empate |
| `SEMILLA-COLA-CON-RETIRO` | A RETIRADA mas B PREFERENTE y C ORDINARIA activas: la cola activa se renumera porque la posicion se calcula |
| `SEMILLA-COLA-TERMINALES` | A ACTIVA URGENTE, B ASIGNADA (cita en 09:00) PREFERENTE y C RETIRADA ORDINARIA: montura de los rechazos por estado terminal |
| `SEMILLA-COLA-SOLO` | una sola entrada C ORDINARIA ACTIVA: reubicar en una cola unitaria y comprobar que la posicion 1 sigue saliendo en el JSON |
| `SEMILLA-DUP` | entrada ACTIVA de api-paciente con api-prueba sembrada ANTES del alta que se va a pedir: el alta es repetido (doble clic de alta) |
| `SEMILLA-DUP-RETIRADA` | igual que SEMILLA-DUP pero la entrada previa esta RETIRADA: su clave anti-duplicado paso a NULL y el alta nueva es legal |
| `SEMILLA-DUP-ASIGNADA` | igual que SEMILLA-DUP pero la entrada previa esta ASIGNADA: tambien es terminal y libera la clave anti-duplicado |
| `SEMILLA-OTRO-PACIENTE` | entrada ACTIVA de OTRO paciente para la misma prueba: demuestra que la clave anti-duplicado es el par paciente|prueba |
| `SEMILLA-OTRA-PRUEBA` | entrada ACTIVA del mismo paciente para OTRA prueba (api-prueba-dup): idem por la otra pata de la clave |
| `SEMILLA-SOL:<tipo>` | solicitud sembrada del tipo indicado (CREADA, CITADA con cita detras, PRESCRITA o inexistente) para el campo solicitudId del alta |
| `SEM-ESTADO:<estado>` | una unica entrada etiquetada OBJ en el estado indicado (ACTIVA, ASIGNADA o RETIRADA), prioridad PREFERENTE: es la entrada sobre la que se dirige la operacion de la fila |
| `SEM-PRI:SUBIR` | moldeador de peticion: el cuerpo de la reubicacion pide el escalon inmediatamente superior al que tiene la entrada sembrada |
| `REDUNDANTE` | moldeador de peticion: el cuerpo de la reubicacion repite el escalon que ya tiene la entrada sembrada (doble clic de reubicacion) |
| `ENLAZE:<enlace>` | fija la solicitudId de la entrada OBJ: CREADA, CITADA (con cita detras), PRESCRITA, NINGUNA (solicitudId nula) o NO_ENCONTRADA |
| `SEM-HUECO-DEST:LIBRE` | el hueco que va a pedir la fila esta libre en la agenda falsa del profesional |
| `SEM-HUECO-DEST:EXACTO` | hueco sembrado que empieza justo en el inicio que pide la fila: choque exacto |
| `SEM-HUECO-DEST:SOLAPE` | hueco sembrado que empieza quince minutos antes y cruza el inicio pedido: choque por solape, no por coincidencia exacta |
| `SEM-HUECO-DEST:FANTASMA` | hueco ocupado sin cita detras (hueco hueso): solo lo protege la restriccion unica, no la maquina de estados de las citas |
| `SEM-CITA-OCUPA` | cita ya creada por CDU12 en 2026-11-13T09:00 con su hueco y su eslabon en cita_auditoria: sirve para comprobar que las dos cadenas de auditoria no se pisan |
| `AUT` | valor autocoadyuvante: el esperaId o el inicio que la propia montura acaba de sembrar (esperaId de OBJ, o inicio 2026-11-13T13:00 dentro de la franja 08:00-15:00) |
| `INEXISTENTE` | esperaId bien formado (UUID valido) que no existe en la tabla |
| `MAL_FORMATADO` | segmento de ruta que no es un UUID: la conversion del path variable falla antes de entrar en el controlador |
| `VACIO` | cadena vacia o solo espacios en un campo o parametro obligatorio |
| `AUSENTE` | se omite la clave del cuerpo o el parametro de consulta |
| `TEXTO` | texto de motivo corriente, dentro de clase |
| `MAX` | valor de 40 caracteres: ultima longitud que cabe en paciente_id y en codigo_prueba |
| `LARGO` | valor de 41 caracteres: primera longitud que no cabe en paciente_id o codigo_prueba |
| `MAX300` | motivo de 300 caracteres: ultima longitud que cabe en motivo_fin y en el eslabon de auditoria |
| `LARGO301` | motivo de 301 caracteres: primera longitud que no cabe (se corta en el servicio, no en la base de datos) |
| `ESPACIOS` | valor valido con espacios delante y detras: recortar lo deja reconocible (leccion de CDU12) |
| `ILEGAL` | instante con formato invalido donde se espera una fecha-hora ISO |
| `ANTERIOR_APERTURA` | 2026-11-13T07:00: antes de la apertura de la franja falsa |
| `APERTURA_JUSTA` | 2026-11-13T08:00: borde inferior exacto de la franja |
| `CIERRE_LIMITRO` | 2026-11-13T14:30: ultimo inicio que cabe con la duracion de 30 minutos |
| `PASADA_CIERRE` | 2026-11-13T15:00: primera hora que desborda el cierre de la franja |
| `FESTIVO` | 2026-11-10T09:00: dia sin franja en la agenda falsa |
| `DOMINGO` | 2026-11-15T13:00: domingo; la agenda falsa tiene franja 08:00-15:00 TODOS los dias salvo el festivo, asi que el domingo es clase valida dentro de franja y solo se usa para montar el hueco fantasma |

## Estados esperados (nombre simbolico -> HTTP)

| Estado | HTTP | Codigo de error | Que significa |
|---|---|---|---|
| `CREADO` | 201 | `-` | entrada dada de alta en la cola: 201 con la entrada, su posicion calculada y su estado ACTIVA |
| `OK` | 200 | `-` | lectura o accion correcta: 200 con el cuerpo del contrato (cola, detalle, reubicacion, retirada con su motivo y clave liberada, o auditoria) |
| `ASIGNADO` | 201 | `-` | la entrada pasa a ASIGNADA y nace la cita con su hueco reservado: 201 con la cita creada reutilizando CitaServicio |
| `INVALIDOS` | 400 | `DATOS_INVALIDOS` | campo obligatorio ausente o en blanco, prioridad desconocida, longitud fuera de clase, motivo ausente o esperaId mal formado |
| `FILTRO_INVALIDO` | 400 | `DATOS_INVALIDOS` | filtro de cola presente y vacio, o pruebaCodigo fuera del catalogo: se rechaza en lugar de servir una cola vacia que parezca legitima |
| `CUERPO_ILEGIBLE` | 400 | `DATOS_INVALIDOS` | el cuerpo JSON no se puede leer o no se puede convertir al DTO del contrato |
| `PRUEBA_NO_ENCONTRADA` | 404 | `PRUEBA_NO_ENCONTRADA` | la prueba pedida no esta en el catalogo: la cola se ordena por prueba, asi que sin prueba no hay cola |
| `SOLICITUD_NO_ENCONTRADA` | 404 | `SOLICITUD_NO_ENCONTRADA` | la solicitud referenciada en el alta no existe: la cola no admite referencias colgantes |
| `NO_ENCONTRADA` | 404 | `LISTA_ESPERA_NO_ENCONTRADA` | no hay ninguna entrada de cola con ese esperaId: 404 propio del modulo, no el 404 de ruta de Spring |
| `SOLICITUD_NO_APTA` | 409 | `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud enlazada no admite cita (ya citada, o la entrada no tiene solicitud): se reutiliza el codigo de CDU12, no uno nuevo |
| `ALTA_DUPLICADA` | 409 | `CONFLICTO_ALTA_DUPLICADA` | primera capa del doble clic: ya existe una entrada ACTIVA del mismo paciente para la misma prueba |
| `NO_ACTIVA` | 409 | `CONFLICTO_ENTRADA_NO_ACTIVA` | tercera capa del doble clic y salida de estado terminal: solo una entrada ACTIVA se reubica, se asigna o se retira |
| `REDUNDANTE` | 409 | `CONFLICTO_REUBICACION_REDUNDANTE` | segunda capa del doble clic: la prioridad pedida es la que ya tiene, no se mueve nada y no se escribe un segundo eslabon |
| `HUECO_OCUPADO` | 409 | `CONFLICTO_HUECO_OCUPADO` | el hueco pedido ya esta reservado: lo decide la base de datos por UNIQUE (profesional_id, inicio) y el @Version |
| `FUERA_DE_FRANJA` | 409 | `CONFLICTO_FUERA_DE_FRANJA` | la cita no cabe entera en una franja del profesional (antes de abrir, despues de cerrar o en festivo) |
| `ERROR_INTERNO` | 500 | `ERROR_INTERNO` | el profesional no tiene agenda: comportamiento heredado de CDU12/CDU13, AgendaNoEncontradoException no tiene mapeo propio y cae en el manejador global (HALLAZGO a cerrar en otra iteracion) |
| `UN_OK_Y_OTRO_409` | 201 | `-` | dos pulsaciones concurrentes: exactamente una termina en exito y la otra en conflicto (409 o 500 por optimista). El TC comprueba el multiset de codigos, no cual gano |
| `CONTENIDO` | 415 | `-` | Content-Type no JSON en un endpoint con @RequestBody: lo resuelve Spring y no pasa por el consejo del modulo, asi que no hay cuerpo ApiError |
| `METODO` | 405 | `-` | metodo no mapeado sobre una ruta si existe: lo resuelve Spring sin cuerpo ApiError |
| `RUTA_INEXISTENTE` | 404 | `-` | ruta fuera del mapa del modulo: el 404 lo resuelve Spring y tampoco lleva cuerpo ApiError |

## Reparto de casos

| Tabla | Operacion | Modo | Filas |
|---|---|---|---|
| A | lectura de la cola: orden determinista y filtros | pairwise | 40 |
| B | alta en la cola: clases de equivalencia de la entrada y primera capa del doble clic | pairwise | 55 |
| C | operaciones sobre una entrada: maquina de estados y precedencias entre codigos | filas | 30 |
| D | reubicacion por cambio de prioridad: cola esperada explicita y doble clic de reubicacion | filas | 27 |
| E | secuencias de doble clic y concurrencia: el segundo golpe no escribe nada | filas | 12 |
| F | asignar cita desde la cola: franja horaria, proteccion del hueco y aptitud de la solicitud | pairwise | 64 |
| G | auditoria de la cola: cadena propia con eslabon REUBICACION y verificacion anti-manipulacion | filas | 12 |
| H | conjetura de errores sobre la cola: entrada ilegitima, colision de ruta, dobles intentos y metodos no declarados | conjetura | 13 |
| **Total** |  |  | **253** |

## Tabla A · lectura de la cola: orden determinista y filtros

**Modo:** `pairwise` · producto de niveles = 1050 · filas generadas = 40 · pares obligatorios cubiertos = 90/90 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF completitud y correccion: la misma cola se sirve siempre en el mismo orden y todo filtro mal formado se rechaza con un codigo estable; COMP: la cola llega filtrable por prueba, profesional y estado con su posicion calculada

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `cola` | `MIXTA` | valido | clase valida tipica de contenido | cinco entradas ACTIVA con las tres prioridades y marcas de tiempo distintas |
| `cola` | `EMPALTE_FECHA` | valido | valor limite: la fecha empata, decide la prioridad | dos entradas ACTIVA con la MISMA marca de tiempo y prioridad distinta |
| `cola` | `EMPALTE_TODO` | valido | valor limite: solo esperaId desempata y por eso el orden es unico entre lecturas | dos entradas ACTIVA con prioridad y marca de tiempo identicas |
| `cola` | `TERMINALES` | valido | clase valida mixta: las terminales tambien se sirven sin filtro | dos ACTIVA mas una ASIGNADA y una RETIRADA |
| `cola` | `SOLO_TERMINALES` | valido | valor limite: filtrar ACTIVA sobre mundo sin activas debe dar cola vacia con 200 | una ASIGNADA y una RETIRADA, ninguna ACTIVA |
| `cola` | `VACIA` | valido | valor limite: cola vacia legitima, no es un error | banco de pruebas sin ninguna entrada |
| `prueba` | `NINGUNO` | valido | clase valida: filtro ausente | sin parametro pruebaCodigo: todas las pruebas |
| `prueba` | `CONOCIDA` | valido | clase valida tipica | pruebaCodigo=api-prueba, la que mas entradas tiene en la semilla |
| `prueba` | `OTRA` | valido | clase valida: filtro que recorta la cola | pruebaCodigo=api-prueba-dup, existente pero con pocas entradas |
| `prueba` | `NO_EXISTE` | valido | valor limite: codigo sintacticamente valido que el catalogo no conoce | pruebaCodigo=api-prueba-que-no-existe: bien formado pero fuera del catalogo |
| `prueba` | `VACIO` | **fuera de clase** | clase invalida: filtro presente sin valor | pruebaCodigo= (parametro presente y vacio) |
| `profesional` | `NINGUNO` | valido | clase valida: filtro ausente | sin parametro profesionalId: todos los profesionales |
| `profesional` | `MEDICO` | valido | clase valida tipica | profesionalId=api-medico, el de la agenda falsa |
| `profesional` | `SIN_AGENDA` | valido | valor limite: id bien formado sin agenda, la cola no depende de la agenda | profesionalId=api-sin-agenda, id valido sin agenda en CDU13 |
| `profesional` | `NO_EXISTE` | valido | valor limite: filtro bien formado que no casa con nadie devuelve 200 con cola vacia | profesionalId=api-profesional-que-no-existe: formato valido, nadie lo conoce |
| `profesional` | `VACIO` | **fuera de clase** | clase invalida: filtro presente sin valor | profesionalId= (parametro presente y vacio) |
| `estado` | `NINGUNO` | valido | clase valida: filtro ausente | sin parametro estado: se sirven activas y terminales |
| `estado` | `ACTIVA` | valido | clase valida tipica | estado=ACTIVA |
| `estado` | `ASIGNADA` | valido | clase valida: estado terminal filtrable | estado=ASIGNADA (terminal con cita detras) |
| `estado` | `RETIRADA` | valido | clase valida: estado terminal filtrable | estado=RETIRADA (terminal con motivo de fin) |
| `estado` | `MINUSCULA` | valido | valor limite: el canon de CDU06 acepta el nombre recortado y en minusculas | estado=activa en minusculas |
| `estado` | `INVENTADO` | **fuera de clase** | clase invalida: enumerado fuera de clase | estado=esperando: no hay ese estado en la maquina |
| `estado` | `VACIO` | **fuera de clase** | clase invalida: filtro presente sin valor | estado= (parametro presente y vacio) |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `prueba` está fuera de clase  | `FILTRO_INVALIDO` | `DATOS_INVALIDOS` |
| 2 | `profesional` está fuera de clase  | `FILTRO_INVALIDO` | `DATOS_INVALIDOS` |
| 3 | `estado` está fuera de clase  | `FILTRO_INVALIDO` | `DATOS_INVALIDOS` |
| 4 | `prueba` vale NO_EXISTE | `FILTRO_INVALIDO` | `DATOS_INVALIDOS` |
| 5 | resto de casos | `OK` | `-` |

### Casos generados

| ID | cola | prueba | profesional | estado | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|
| `TC14-A01` | `MIXTA` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A02` | `EMPALTE_FECHA` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-EMPALTE-FECHA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=EMPALTE_FECHA (valor limite: la fecha empata, decide la prioridad), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A03` | `EMPALTE_TODO` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-EMPALTE-TODO` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=EMPALTE_TODO (valor limite: solo esperaId desempata y por eso el orden es unico entre lecturas), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A04` | `TERMINALES` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-TERMINALES` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=TERMINALES (clase valida mixta: las terminales tambien se sirven sin filtro), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A05` | `SOLO_TERMINALES` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-SOLO-TERMINALES` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=SOLO_TERMINALES (valor limite: filtrar ACTIVA sobre mundo sin activas debe dar cola vacia con 200), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A06` | `VACIA` | `NINGUNO` | `NINGUNO` | `NINGUNO` | `SEMILLA-VACIA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=VACIA (valor limite: cola vacia legitima, no es un error), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A07` | `EMPALTE_TODO` | `NINGUNO` | `NINGUNO` | `ACTIVA` | `SEMILLA-EMPALTE-TODO` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=EMPALTE_TODO (valor limite: solo esperaId desempata y por eso el orden es unico entre lecturas), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=ACTIVA (clase valida tipica) |
| `TC14-A08` | `SOLO_TERMINALES` | `NINGUNO` | `NINGUNO` | `ACTIVA` | `SEMILLA-SOLO-TERMINALES` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=SOLO_TERMINALES (valor limite: filtrar ACTIVA sobre mundo sin activas debe dar cola vacia con 200), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=ACTIVA (clase valida tipica) |
| `TC14-A09` | `MIXTA` | `VACIO` | `NINGUNO` | `ASIGNADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | un filtro presente y vacio es un error de sintaxis de la consulta: la ausencia del parametro es lo unico que significa sin filtro \| prueba: pruebaCodigo= (parametro presente y vacio) |
| `TC14-A10` | `MIXTA` | `NINGUNO` | `VACIO` | `ASIGNADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | profesionalId en blanco: misma guarda que los filtros de CDU12 \| profesional: profesionalId= (parametro presente y vacio) |
| `TC14-A11` | `MIXTA` | `CONOCIDA` | `NINGUNO` | `INVENTADO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado=esperando: no hay ese estado en la maquina |
| `TC14-A12` | `MIXTA` | `OTRA` | `NINGUNO` | `VACIO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado= (parametro presente y vacio) |
| `TC14-A13` | `MIXTA` | `NINGUNO` | `MEDICO` | `INVENTADO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado=esperando: no hay ese estado en la maquina |
| `TC14-A14` | `MIXTA` | `NINGUNO` | `SIN_AGENDA` | `VACIO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado= (parametro presente y vacio) |
| `TC14-A15` | `MIXTA` | `NINGUNO` | `NO_EXISTE` | `RETIRADA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NINGUNO (clase valida: filtro ausente), profesional=NO_EXISTE (valor limite: filtro bien formado que no casa con nadie devuelve 200 con cola vacia), estado=RETIRADA (clase valida: estado terminal filtrable) |
| `TC14-A16` | `MIXTA` | `NO_EXISTE` | `NINGUNO` | `RETIRADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | la cola se ordena por prueba, asi que un codigo fuera del catalogo se responde 400 en lugar de una cola vacia que el Administrativo leeria como 'no hay nadie' \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NO_EXISTE (valor limite: codigo sintacticamente valido que el catalogo no conoce), profesional=NINGUNO (clase valida: filtro ausente), estado=RETIRADA (clase valida: estado terminal filtrable) |
| `TC14-A17` | `MIXTA` | `VACIO` | `MEDICO` | `RETIRADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | un filtro presente y vacio es un error de sintaxis de la consulta: la ausencia del parametro es lo unico que significa sin filtro \| prueba: pruebaCodigo= (parametro presente y vacio) |
| `TC14-A18` | `MIXTA` | `CONOCIDA` | `VACIO` | `RETIRADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | profesionalId en blanco: misma guarda que los filtros de CDU12 \| profesional: profesionalId= (parametro presente y vacio) |
| `TC14-A19` | `MIXTA` | `CONOCIDA` | `MEDICO` | `VACIO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado= (parametro presente y vacio) |
| `TC14-A20` | `MIXTA` | `CONOCIDA` | `SIN_AGENDA` | `ASIGNADA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=CONOCIDA (clase valida tipica), profesional=SIN_AGENDA (valor limite: id bien formado sin agenda, la cola no depende de la agenda), estado=ASIGNADA (clase valida: estado terminal filtrable) |
| `TC14-A21` | `MIXTA` | `OTRA` | `MEDICO` | `ASIGNADA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=OTRA (clase valida: filtro que recorta la cola), profesional=MEDICO (clase valida tipica), estado=ASIGNADA (clase valida: estado terminal filtrable) |
| `TC14-A22` | `MIXTA` | `OTRA` | `SIN_AGENDA` | `RETIRADA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=OTRA (clase valida: filtro que recorta la cola), profesional=SIN_AGENDA (valor limite: id bien formado sin agenda, la cola no depende de la agenda), estado=RETIRADA (clase valida: estado terminal filtrable) |
| `TC14-A23` | `MIXTA` | `OTRA` | `NO_EXISTE` | `INVENTADO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado=esperando: no hay ese estado en la maquina |
| `TC14-A24` | `MIXTA` | `NO_EXISTE` | `SIN_AGENDA` | `INVENTADO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado=esperando: no hay ese estado en la maquina |
| `TC14-A25` | `MIXTA` | `OTRA` | `VACIO` | `NINGUNO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | profesionalId en blanco: misma guarda que los filtros de CDU12 \| profesional: profesionalId= (parametro presente y vacio) |
| `TC14-A26` | `MIXTA` | `VACIO` | `SIN_AGENDA` | `NINGUNO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | un filtro presente y vacio es un error de sintaxis de la consulta: la ausencia del parametro es lo unico que significa sin filtro \| prueba: pruebaCodigo= (parametro presente y vacio) |
| `TC14-A27` | `MIXTA` | `CONOCIDA` | `NO_EXISTE` | `NINGUNO` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=CONOCIDA (clase valida tipica), profesional=NO_EXISTE (valor limite: filtro bien formado que no casa con nadie devuelve 200 con cola vacia), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A28` | `MIXTA` | `NO_EXISTE` | `MEDICO` | `NINGUNO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | la cola se ordena por prueba, asi que un codigo fuera del catalogo se responde 400 en lugar de una cola vacia que el Administrativo leeria como 'no hay nadie' \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NO_EXISTE (valor limite: codigo sintacticamente valido que el catalogo no conoce), profesional=MEDICO (clase valida tipica), estado=NINGUNO (clase valida: filtro ausente) |
| `TC14-A29` | `MIXTA` | `NO_EXISTE` | `NO_EXISTE` | `ASIGNADA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | la cola se ordena por prueba, asi que un codigo fuera del catalogo se responde 400 en lugar de una cola vacia que el Administrativo leeria como 'no hay nadie' \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NO_EXISTE (valor limite: codigo sintacticamente valido que el catalogo no conoce), profesional=NO_EXISTE (valor limite: filtro bien formado que no casa con nadie devuelve 200 con cola vacia), estado=ASIGNADA (clase valida: estado terminal filtrable) |
| `TC14-A30` | `MIXTA` | `NO_EXISTE` | `VACIO` | `ACTIVA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | profesionalId en blanco: misma guarda que los filtros de CDU12 \| profesional: profesionalId= (parametro presente y vacio) |
| `TC14-A31` | `MIXTA` | `VACIO` | `NO_EXISTE` | `ACTIVA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | un filtro presente y vacio es un error de sintaxis de la consulta: la ausencia del parametro es lo unico que significa sin filtro \| prueba: pruebaCodigo= (parametro presente y vacio) |
| `TC14-A32` | `MIXTA` | `NO_EXISTE` | `NO_EXISTE` | `VACIO` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | estado que no pertenece a la EnumSet prometida: se rechaza antes de consultar \| estado: estado= (parametro presente y vacio) |
| `TC14-A33` | `MIXTA` | `CONOCIDA` | `MEDICO` | `ACTIVA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=CONOCIDA (clase valida tipica), profesional=MEDICO (clase valida tipica), estado=ACTIVA (clase valida tipica) |
| `TC14-A34` | `MIXTA` | `OTRA` | `SIN_AGENDA` | `ACTIVA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=OTRA (clase valida: filtro que recorta la cola), profesional=SIN_AGENDA (valor limite: id bien formado sin agenda, la cola no depende de la agenda), estado=ACTIVA (clase valida tipica) |
| `TC14-A35` | `MIXTA` | `NINGUNO` | `NINGUNO` | `MINUSCULA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NINGUNO (clase valida: filtro ausente), profesional=NINGUNO (clase valida: filtro ausente), estado=MINUSCULA (valor limite: el canon de CDU06 acepta el nombre recortado y en minusculas) |
| `TC14-A36` | `MIXTA` | `CONOCIDA` | `MEDICO` | `MINUSCULA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=CONOCIDA (clase valida tipica), profesional=MEDICO (clase valida tipica), estado=MINUSCULA (valor limite: el canon de CDU06 acepta el nombre recortado y en minusculas) |
| `TC14-A37` | `MIXTA` | `OTRA` | `SIN_AGENDA` | `MINUSCULA` | `SEMILLA-MIXTA` | OK | cola servida elemento a elemento en el orden del comparador total y con posicion 1..n: la proyeccion HTTP del invariante de determinismo \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=OTRA (clase valida: filtro que recorta la cola), profesional=SIN_AGENDA (valor limite: id bien formado sin agenda, la cola no depende de la agenda), estado=MINUSCULA (valor limite: el canon de CDU06 acepta el nombre recortado y en minusculas) |
| `TC14-A38` | `MIXTA` | `NO_EXISTE` | `NO_EXISTE` | `MINUSCULA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | la cola se ordena por prueba, asi que un codigo fuera del catalogo se responde 400 en lugar de una cola vacia que el Administrativo leeria como 'no hay nadie' \| valores limite: cola=MIXTA (clase valida tipica de contenido), prueba=NO_EXISTE (valor limite: codigo sintacticamente valido que el catalogo no conoce), profesional=NO_EXISTE (valor limite: filtro bien formado que no casa con nadie devuelve 200 con cola vacia), estado=MINUSCULA (valor limite: el canon de CDU06 acepta el nombre recortado y en minusculas) |
| `TC14-A39` | `MIXTA` | `VACIO` | `NINGUNO` | `MINUSCULA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | un filtro presente y vacio es un error de sintaxis de la consulta: la ausencia del parametro es lo unico que significa sin filtro \| prueba: pruebaCodigo= (parametro presente y vacio) |
| `TC14-A40` | `MIXTA` | `NINGUNO` | `VACIO` | `MINUSCULA` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | profesionalId en blanco: misma guarda que los filtros de CDU12 \| profesional: profesionalId= (parametro presente y vacio) |

## Tabla B · alta en la cola: clases de equivalencia de la entrada y primera capa del doble clic

**Modo:** `pairwise` · producto de niveles = 7560 · filas generadas = 55 · pares obligatorios cubiertos = 186/186 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** AF completitud y correccion: toda alta valida crea la entrada con su posicion calculada y toda alta invalida se rechaza con un codigo estable; SEG: la clave anti-duplicado impide dos entradas activas del mismo paciente para la misma prueba

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `paciente` | `TIPICO` | valido | clase valida tipica | pacienteId=api-paciente |
| `paciente` | `MAX` | valido | valor limite: ultimo valor que cabe | pacienteId de 40 caracteres: la longitud maxima de la columna paciente_id |
| `paciente` | `LARGO` | **fuera de clase** | valor limite: primera longitud que revienta la columna; se rechaza en el servicio para no convertir un 400 en un 500 de base de datos | pacienteId de 41 caracteres |
| `paciente` | `VACIO` | **fuera de clase** | clase invalida: obligatorio en blanco | pacienteId= o solo espacios |
| `paciente` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | el cuerpo no trae la clave pacienteId |
| `prueba` | `CONOCIDA` | valido | clase valida tipica | pruebaCodigo=api-prueba, activa en el catalogo falso |
| `prueba` | `OTRA` | valido | clase valida: sirve para la clave anti-duplicado paciente\|prueba | pruebaCodigo=api-prueba-dup, la segunda prueba del catalogo falso |
| `prueba` | `NO_EXISTE` | valido | valor limite: codigo bien formado que el catalogo no conoce | pruebaCodigo=api-prueba-que-no-existe |
| `prueba` | `LARGO` | **fuera de clase** | valor limite: primera longitud que no cabe | pruebaCodigo de 41 caracteres (la columna admite 40) |
| `prueba` | `VACIO` | **fuera de clase** | clase invalida: obligatorio en blanco | pruebaCodigo= en blanco |
| `prueba` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | el cuerpo no trae pruebaCodigo |
| `prioridad` | `ORDINARIA` | valido | clase valida: borde inferior de la escala | el escalon mas bajo del canon de CDU06 |
| `prioridad` | `PREFERENTE` | valido | clase valida: punto medio de la escala | el escalon intermedio |
| `prioridad` | `URGENTE` | valido | clase valida: borde superior de la escala | el escalon mas alto |
| `prioridad` | `MINUSCULA` | valido | valor limite: Prioridad.desdeNombre recorta y pasa a mayusculas | prioridad=urgente escrito en minusculas |
| `prioridad` | `ESPACIOS` | valido | valor limite: la leccion de CDU12, recortar antes de comparar | prioridad='  urgente  ' con espacios delante y detras |
| `prioridad` | `INVENTADA` | **fuera de clase** | clase invalida: enumerado fuera de clase | prioridad=CRITICA: no existe en el escalon de tres valores |
| `prioridad` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | el cuerpo no trae prioridad |
| `solicitud` | `NINGUNA` | valido | clase valida: opcional ausente | solicitudId omitida: el alta admite entrar en cola sin solicitud (RF14.1 permite cola por prueba) |
| `solicitud` | `CREADA` | valido | clase valida tipica | solicitudId=api-sol-creada |
| `solicitud` | `CITADA` | valido | valor limite: enlace a solicitud ya citada, el alta debe dejar pasar | solicitudId=api-sol-citada: el alta no mira el estado, la aptitud se comprueba al asignar |
| `solicitud` | `PRESCRITA` | valido | valor limite: enlace a solicitud no citable, el alta debe dejar pasar | solicitudId=api-sol-prescrita |
| `solicitud` | `NO_EXISTE` | valido | valor limite: referencia colgante, la FK y el servicio la cortan | solicitudId=api-sol-que-no-existe |
| `solicitud` | `VACIO` | **fuera de clase** | clase invalida: opcional presente y vacio | solicitudId= en blanco |
| `duplicado` | `NO` | valido | clase valida tipica | cola limpia: no hay nada que duplicar |
| `duplicado` | `MISMA` | valido | primera capa del doble clic: el boton duplicado del Administrativo | sembrada una entrada ACTIVA con el mismo paciente y la misma prueba que pide la fila |
| `duplicado` | `TRAS_RETIRADA` | valido | valor limite: reingreso legal despues de una salida terminal | la entrada previa esta RETIRADA: su clave anti-duplicado paso a NULL |
| `duplicado` | `TRAS_ASIGNACION` | valido | valor limite: reingreso legal tras una cita creada | la entrada previa esta ASIGNADA: tambien es terminal y libera la clave |
| `duplicado` | `OTRO_PACIENTE` | valido | valor limite: la clave es paciente\|prueba, otro paciente si puede entrar | la entrada ACTIVA sembrada es de otro paciente para la misma prueba |
| `duplicado` | `OTRA_PRUEBA` | valido | valor limite: la clave es paciente\|prueba, otra prueba si puede entrar | la entrada ACTIVA sembrada es del mismo paciente para otra prueba |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `paciente` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | `prueba` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `prioridad` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 4 | `solicitud` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `prueba` vale NO_EXISTE | `PRUEBA_NO_ENCONTRADA` | `PRUEBA_NO_ENCONTRADA` |
| 6 | `solicitud` vale NO_EXISTE | `SOLICITUD_NO_ENCONTRADA` | `SOLICITUD_NO_ENCONTRADA` |
| 7 | `duplicado` vale MISMA | `ALTA_DUPLICADA` | `CONFLICTO_ALTA_DUPLICADA` |
| 8 | `duplicado` vale TRAS_RETIRADA | `CREADO` | `-` |
| 9 | `duplicado` vale TRAS_ASIGNACION | `CREADO` | `-` |
| 10 | `duplicado` vale OTRO_PACIENTE | `CREADO` | `-` |
| 11 | `duplicado` vale OTRA_PRUEBA | `CREADO` | `-` |
| 12 | resto de casos | `CREADO` | `-` |

### Casos generados

| ID | paciente | prueba | prioridad | solicitud | duplicado | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|
| `TC14-B01` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | CREADO | alta valida: 201 con la entrada en ACTIVA, fechaSolicitud fijada por el reloj de la aplicacion y posicion calculada \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=NO (clase valida tipica) |
| `TC14-B02` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `MISMA` | `SEMILLA-VACIA,SEMILLA-DUP` | ALTA_DUPLICADA `CONFLICTO_ALTA_DUPLICADA` | primera capa del doble clic: la clave UNIQUE condicionada (paciente\|prueba mientras ACTIVA) ya esta ocupada \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=MISMA (primera capa del doble clic: el boton duplicado del Administrativo) |
| `TC14-B03` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `TRAS_RETIRADA` | `SEMILLA-VACIA,SEMILLA-DUP-RETIRADA` | CREADO | la clave anti-duplicado vale NULL en las entradas terminales: retirar y volver a dar alta es el camino legitimo \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=TRAS_RETIRADA (valor limite: reingreso legal despues de una salida terminal) |
| `TC14-B04` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `TRAS_ASIGNACION` | `SEMILLA-VACIA,SEMILLA-DUP-ASIGNADA` | CREADO | la entrada asignada ya no compite por el hueco de la clave condicionada \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=TRAS_ASIGNACION (valor limite: reingreso legal tras una cita creada) |
| `TC14-B05` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `OTRO_PACIENTE` | `SEMILLA-VACIA,SEMILLA-OTRO-PACIENTE` | CREADO | otro paciente para la misma prueba es una entrada distinta: la clave es el par \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=OTRO_PACIENTE (valor limite: la clave es paciente\|prueba, otro paciente si puede entrar) |
| `TC14-B06` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `OTRA_PRUEBA` | `SEMILLA-VACIA,SEMILLA-OTRA-PRUEBA` | CREADO | el mismo paciente esperando otra prueba es otra cola: la clave es el par \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=OTRA_PRUEBA (valor limite: la clave es paciente\|prueba, otra prueba si puede entrar) |
| `TC14-B07` | `MAX` | `CONOCIDA` | `URGENTE` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | CREADO | alta valida: 201 con la entrada en ACTIVA, fechaSolicitud fijada por el reloj de la aplicacion y posicion calculada \| valores limite: paciente=MAX (valor limite: ultimo valor que cabe), prueba=CONOCIDA (clase valida tipica), prioridad=URGENTE (clase valida: borde superior de la escala), solicitud=CREADA (clase valida tipica), duplicado=NO (clase valida tipica) |
| `TC14-B08` | `TIPICO` | `NO_EXISTE` | `ORDINARIA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | PRUEBA_NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | la prueba es el eje de la cola: sin prueba conocida no hay donde encolar a nadie, y se responde antes de mirar la solicitud \| valores limite: paciente=TIPICO (clase valida tipica), prueba=NO_EXISTE (valor limite: codigo bien formado que el catalogo no conoce), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NINGUNA (clase valida: opcional ausente), duplicado=NO (clase valida tipica) |
| `TC14-B09` | `TIPICO` | `CONOCIDA` | `ORDINARIA` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | SOLICITUD_NO_ENCONTRADA `SOLICITUD_NO_ENCONTRADA` | la cola no admite referencias colgantes: la FK es la red de seguridad, el servicio la traduce a 404 \| valores limite: paciente=TIPICO (clase valida tipica), prueba=CONOCIDA (clase valida tipica), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NO_EXISTE (valor limite: referencia colgante, la FK y el servicio la cortan), duplicado=NO (clase valida tipica) |
| `TC14-B10` | `TIPICO` | `NO_EXISTE` | `ORDINARIA` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | PRUEBA_NO_ENCONTRADA `PRUEBA_NO_ENCONTRADA` | la prueba es el eje de la cola: sin prueba conocida no hay donde encolar a nadie, y se responde antes de mirar la solicitud \| valores limite: paciente=TIPICO (clase valida tipica), prueba=NO_EXISTE (valor limite: codigo bien formado que el catalogo no conoce), prioridad=ORDINARIA (clase valida: borde inferior de la escala), solicitud=NO_EXISTE (valor limite: referencia colgante, la FK y el servicio la cortan), duplicado=NO (clase valida tipica) |
| `TC14-B11` | `LARGO` | `CONOCIDA` | `PREFERENTE` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId de 41 caracteres |
| `TC14-B12` | `VACIO` | `OTRA` | `PREFERENTE` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId= o solo espacios |
| `TC14-B13` | `AUSENTE` | `OTRA` | `ORDINARIA` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: el cuerpo no trae la clave pacienteId |
| `TC14-B14` | `TIPICO` | `LARGO` | `URGENTE` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo de 41 caracteres (la columna admite 40) |
| `TC14-B15` | `MAX` | `VACIO` | `MINUSCULA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo= en blanco |
| `TC14-B16` | `TIPICO` | `AUSENTE` | `ESPACIOS` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: el cuerpo no trae pruebaCodigo |
| `TC14-B17` | `TIPICO` | `OTRA` | `INVENTADA` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: prioridad=CRITICA: no existe en el escalon de tres valores |
| `TC14-B18` | `TIPICO` | `CONOCIDA` | `AUSENTE` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: el cuerpo no trae prioridad |
| `TC14-B19` | `MAX` | `OTRA` | `ESPACIOS` | `VACIO` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | solicitudId presente y en blanco: un opcional mal formado es un error del cliente, no una ausencia \| solicitud: solicitudId= en blanco |
| `TC14-B20` | `LARGO` | `OTRA` | `URGENTE` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId de 41 caracteres |
| `TC14-B21` | `LARGO` | `NO_EXISTE` | `MINUSCULA` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId de 41 caracteres |
| `TC14-B22` | `MAX` | `NO_EXISTE` | `INVENTADA` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: prioridad=CRITICA: no existe en el escalon de tres valores |
| `TC14-B23` | `MAX` | `LARGO` | `ORDINARIA` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo de 41 caracteres (la columna admite 40) |
| `TC14-B24` | `MAX` | `AUSENTE` | `PREFERENTE` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: el cuerpo no trae pruebaCodigo |
| `TC14-B25` | `AUSENTE` | `NO_EXISTE` | `PREFERENTE` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: el cuerpo no trae la clave pacienteId |
| `TC14-B26` | `TIPICO` | `CONOCIDA` | `MINUSCULA` | `VACIO` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | solicitudId presente y en blanco: un opcional mal formado es un error del cliente, no una ausencia \| solicitud: solicitudId= en blanco |
| `TC14-B27` | `TIPICO` | `VACIO` | `PREFERENTE` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo= en blanco |
| `TC14-B28` | `AUSENTE` | `CONOCIDA` | `ESPACIOS` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: el cuerpo no trae la clave pacienteId |
| `TC14-B29` | `AUSENTE` | `OTRA` | `MINUSCULA` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: el cuerpo no trae la clave pacienteId |
| `TC14-B30` | `VACIO` | `NO_EXISTE` | `URGENTE` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId= o solo espacios |
| `TC14-B31` | `VACIO` | `CONOCIDA` | `ORDINARIA` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId= o solo espacios |
| `TC14-B32` | `LARGO` | `NO_EXISTE` | `ESPACIOS` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId de 41 caracteres |
| `TC14-B33` | `MAX` | `OTRA` | `AUSENTE` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: el cuerpo no trae prioridad |
| `TC14-B34` | `TIPICO` | `AUSENTE` | `MINUSCULA` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: el cuerpo no trae pruebaCodigo |
| `TC14-B35` | `VACIO` | `CONOCIDA` | `MINUSCULA` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId= o solo espacios |
| `TC14-B36` | `TIPICO` | `AUSENTE` | `URGENTE` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: el cuerpo no trae pruebaCodigo |
| `TC14-B37` | `TIPICO` | `VACIO` | `ESPACIOS` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo= en blanco |
| `TC14-B38` | `TIPICO` | `LARGO` | `ESPACIOS` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo de 41 caracteres (la columna admite 40) |
| `TC14-B39` | `VACIO` | `CONOCIDA` | `ESPACIOS` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId= o solo espacios |
| `TC14-B40` | `TIPICO` | `AUSENTE` | `ORDINARIA` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: el cuerpo no trae pruebaCodigo |
| `TC14-B41` | `LARGO` | `CONOCIDA` | `ORDINARIA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: pacienteId de 41 caracteres |
| `TC14-B42` | `AUSENTE` | `CONOCIDA` | `URGENTE` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | obligatorio en blanco o longitud que no cabe en la columna: la guarda del servicio precede a cualquier consulta \| paciente: el cuerpo no trae la clave pacienteId |
| `TC14-B43` | `TIPICO` | `CONOCIDA` | `INVENTADA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: prioridad=CRITICA: no existe en el escalon de tres valores |
| `TC14-B44` | `TIPICO` | `LARGO` | `MINUSCULA` | `NINGUNA` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo de 41 caracteres (la columna admite 40) |
| `TC14-B45` | `TIPICO` | `LARGO` | `PREFERENTE` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo de 41 caracteres (la columna admite 40) |
| `TC14-B46` | `TIPICO` | `VACIO` | `ORDINARIA` | `PRESCRITA` | `NO` | `SEMILLA-SOL:PRESCRITA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo= en blanco |
| `TC14-B47` | `TIPICO` | `VACIO` | `URGENTE` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | pruebaCodigo en blanco o de longitud imposible: se corta antes de llamar al catalogo \| prueba: pruebaCodigo= en blanco |
| `TC14-B48` | `TIPICO` | `NO_EXISTE` | `ORDINARIA` | `VACIO` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | solicitudId presente y en blanco: un opcional mal formado es un error del cliente, no una ausencia \| solicitud: solicitudId= en blanco |
| `TC14-B49` | `TIPICO` | `NO_EXISTE` | `AUSENTE` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: el cuerpo no trae prioridad |
| `TC14-B50` | `TIPICO` | `CONOCIDA` | `PREFERENTE` | `VACIO` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | solicitudId presente y en blanco: un opcional mal formado es un error del cliente, no una ausencia \| solicitud: solicitudId= en blanco |
| `TC14-B51` | `TIPICO` | `CONOCIDA` | `URGENTE` | `VACIO` | `NO` | `SEMILLA-VACIA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | solicitudId presente y en blanco: un opcional mal formado es un error del cliente, no una ausencia \| solicitud: solicitudId= en blanco |
| `TC14-B52` | `TIPICO` | `CONOCIDA` | `INVENTADA` | `CREADA` | `NO` | `SEMILLA-SOL:CREADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: prioridad=CRITICA: no existe en el escalon de tres valores |
| `TC14-B53` | `TIPICO` | `CONOCIDA` | `INVENTADA` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: prioridad=CRITICA: no existe en el escalon de tres valores |
| `TC14-B54` | `TIPICO` | `CONOCIDA` | `AUSENTE` | `CITADA` | `NO` | `SEMILLA-SOL:CITADA,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: el cuerpo no trae prioridad |
| `TC14-B55` | `TIPICO` | `CONOCIDA` | `AUSENTE` | `NO_EXISTE` | `NO` | `SEMILLA-SOL:INEXISTENTE,SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | prioridad que no esta en el canon de CDU06: la escala de la cola no se inventa en CDU14 \| prioridad: el cuerpo no trae prioridad |

## Tabla C · operaciones sobre una entrada: maquina de estados y precedencias entre codigos

**Modo:** `filas` · filas escritas a mano = 30

**Puerta que cubre:** AF completitud y correccion: ninguna operacion avanza sobre entrada inexistente ni sobre estado terminal, y cuando dos reglas podrian aplicar a la vez el codigo que se responde es el que declara la precedencia fijada en el contrato

### Casos generados

| ID | operacion | estadoEntrada | inicio | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC14-C01` | `DETALLE_ESPERA` | `ACTIVA` | `-` | `SEM-ESTADO:ACTIVA` | OK | lectura de entrada activa con su posicion calculada sobre la cola del mundo sembrado |
| `TC14-C02` | `DETALLE_ESPERA` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA` | OK | las terminales tambien se leen: la trazabilidad no se borra al cerrar la entrada, la cita enlazada vive en 09:00 (INICIO_FIJO) para no pisar AUT (13:00) |
| `TC14-C03` | `DETALLE_ESPERA` | `RETIRADA` | `-` | `SEM-ESTADO:RETIRADA` | OK | la retirada deja motivoFin y estado, nunca borra la fila |
| `TC14-C04` | `DETALLE_ESPERA` | `NINGUNA` | `-` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | esperaId bien formado que no existe: 404 propio, no el 404 de ruta de Spring |
| `TC14-C05` | `AUDITORIA_ESPERA` | `ACTIVA` | `-` | `SEM-ESTADO:ACTIVA` | OK | cadena de la entrada: GENESIS + ALTA |
| `TC14-C06` | `AUDITORIA_ESPERA` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA` | OK | GENESIS + ALTA + ASIGNACION: la asignacion deja su eslabon apuntando a la cita |
| `TC14-C07` | `AUDITORIA_ESPERA` | `RETIRADA` | `-` | `SEM-ESTADO:RETIRADA` | OK | GENESIS + ALTA + RETIRADA con el motivo en el eslabon |
| `TC14-C08` | `AUDITORIA_ESPERA` | `NINGUNA` | `-` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | auditar una entrada inexistente no devuelve cadena vacia: 404 |
| `TC14-C09` | `AUDITORIA_VERIFICABLE` | `ACTIVA` | `-` | `SEM-ESTADO:ACTIVA` | OK | verificacion de la cadena completa de la tabla espera_auditoria: true en mundo limpio (el campo verificable del JSON lo comprueba el TC a mano) |
| `TC14-C10` | `AUDITORIA_VERIFICABLE` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA` | OK | tres eslabones encadenados siguen verificando: cada huella parte de la anterior |
| `TC14-C11` | `PRIORIDAD` | `ACTIVA` | `-` | `SEM-ESTADO:ACTIVA,SEM-PRI:SUBIR` | OK | reubicacion feliz sobre entrada activa (el orden resultante lo afirman las filas de la tabla D, que llevan la cola esperada escrita) |
| `TC14-C12` | `PRIORIDAD` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | reubicar una entrada ya asignada no tiene sentido: su hueco ya esta cogido |
| `TC14-C13` | `PRIORIDAD` | `RETIRADA` | `-` | `SEM-ESTADO:RETIRADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | estado terminal: la maquina manda antes que cualquier otra consideracion |
| `TC14-C14` | `PRIORIDAD` | `NINGUNA` | `-` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | 404 antes que cualquier regla de estado |
| `TC14-C15` | `PRIORIDAD` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA,REDUNDANTE` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | PRECEDENCIA fijada: estado gana a redundancia, si se mirase primero la redundancia el Administrativo recibira un codigo que no explica que la entrada ya esta cerrada |
| `TC14-C16` | `RETIRAR` | `ACTIVA` | `-` | `SEM-ESTADO:ACTIVA` | OK | retirada feliz con motivo en el cuerpo |
| `TC14-C17` | `RETIRAR` | `ASIGNADA` | `-` | `SEM-ESTADO:ASIGNADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | retirar la entrada que ya tiene cita no deshace la cita: se prohibe, no se revierte |
| `TC14-C18` | `RETIRAR` | `RETIRADA` | `-` | `SEM-ESTADO:RETIRADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | segunda retirada sobre la misma entrada |
| `TC14-C19` | `RETIRAR` | `RETIRADA` | `-` | `SEM-ESTADO:RETIRADA,REDUNDANTE` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | PRECEDENCIA: el doble clic de retirar sobre entrada ya retirada devuelve UN solo codigo (ENTRADA_NO_ACTIVA, regla de estado primero), y asi el TC del doble clic es determinista |
| `TC14-C20` | `RETIRAR` | `NINGUNA` | `-` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | 404 antes que estado |
| `TC14-C21` | `ASIGNAR` | `ACTIVA` | `AUT` | `SEM-ESTADO:ACTIVA` | ASIGNADO | asignacion feliz: 201 con la cita, la entrada pasa a ASIGNADA con citaId y deja eslabon ASIGNACION |
| `TC14-C22` | `ASIGNAR` | `ASIGNADA` | `AUT` | `SEM-ESTADO:ASIGNADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | tercera capa del doble clic sobre la maquina: la entrada ya asignada no se reasigna por esta via |
| `TC14-C23` | `ASIGNAR` | `RETIRADA` | `AUT` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:EXACTO` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | PRECEDENCIA: estado gana a hueco ocupado, el hueco esta sembrado a proposito para demostrar que ni se consulta |
| `TC14-C24` | `ASIGNAR` | `NINGUNA` | `AUT` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | 404 antes que estado |
| `TC14-C25` | `ASIGNAR` | `ACTIVA` | `AUT` | `SEM-ESTADO:ACTIVA,ENLAZE:CITADA,SEM-HUECO-DEST:LIBRE` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | la solicitud enlazada ya tiene cita detras: CDU14 aplica su propia guarda fail-fast (apta: CREADA, no aptas: CITADA, PRESCRITA o enlace nulo) antes de consultar la agenda |
| `TC14-C26` | `ASIGNAR` | `ACTIVA` | `AUT` | `SEM-ESTADO:ACTIVA,ENLAZE:CITADA,SEM-HUECO-DEST:EXACTO` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | PRECEDENCIA reordenada tras el informe de factibilidad del agente B: la aptitud de la solicitud se comprueba antes de llamar a huecosDisponibles, con la precedencia antigua esta fila habria dado HUECO_OCUPADO |
| `TC14-C27` | `ASIGNAR` | `ACTIVA` | `AUT` | `SEM-ESTADO:ACTIVA,ENLAZE:NINGUNA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | entrar en cola sin solicitud es legal (RF14.1), pero asignarle una cita a quien no tiene prescripcion no lo es: la guarda incluye solicitudId nula |
| `TC14-C28` | `ASIGNAR` | `ACTIVA` | `AUT` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | hueco sembrado exactamente sobre el inicio que pide la propia fila: CitaConflicto HUECO_OCUPADO propagado por el facade |
| `TC14-C29` | `ASIGNAR` | `ACTIVA` | `PASADA_CIERRE` | `SEM-ESTADO:ACTIVA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 15:00 en punto: el inicio cae fuera del rango [08:00,15:00) de la franja falsa de PruebasCitas y el fin de 30 minutos desborda ancora mas |
| `TC14-C30` | `ASIGNAR` | `ACTIVA` | `PASADA_CIERRE` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | PRECEDENCIA: huecosDisponibles comprueba la franja antes de las colisiones, asi que gana FUERA_DE_FRANJA aunque el hueco este ocupado |

## Tabla D · reubicacion por cambio de prioridad: cola esperada explicita y doble clic de reubicacion

**Modo:** `filas` · filas escritas a mano = 27

**Puerta que cubre:** AF completitud y correccion: subir o bajar un escalon reordena la cola exactamente como promete el comparador total, y el escalon repetido no escribe auditoria; la columna esperadoCola deja el orden esperado escrito en la tabla, no deducido por el test

### Casos generados

| ID | escenario | objetivo | destino | motivo | esperaId | esperadoCola | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|---|
| `TC14-D01` | `TRES` | `A` | `PREFERENTE` | `TEXTO` | `AUT` | `A:PREFERENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | OK | A baja al escalon intermedio: empata con B pero conserva la delantera por marca de tiempo (t1 < t2): bajar de escalon no siempre cede el paso, y eso es lo que promete el comparador |
| `TC14-D02` | `TRES` | `A` | `ORDINARIA` | `TEXTO` | `AUT` | `B:PREFERENTE\|A:ORDINARIA\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | OK | A baja del todo: empate en ORDINARIA con C, desempata la marca de tiempo a favor de A (t1 < t3): el desempate 2 se ve escrito en la cola esperada |
| `TC14-D03` | `TRES` | `A` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | doble clic de reubicacion: A ya esta en URGENTE, sin segundo eslabon de auditoria (lo comprueba el TC) y la cola no se toca |
| `TC14-D04` | `TRES` | `B` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:URGENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | OK | B sube a URGENTE: empate con A en el escalon mas alto y A conserva la delantera por marca de tiempo (t1 < t2): subir no siempre adelanta, y eso es lo que promete el contrato |
| `TC14-D05` | `TRES` | `B` | `PREFERENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | escalon repetido sobre el escalon intermedio: la redundancia no depende de la direccion, solo del valor |
| `TC14-D06` | `TRES` | `B` | `ORDINARIA` | `TEXTO` | `AUT` | `A:URGENTE\|B:ORDINARIA\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | OK | B baja y empata con C en ORDINARIA: gana B por marca de tiempo (t2 < t3) |
| `TC14-D07` | `TRES` | `C` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE\|C:URGENTE\|B:PREFERENTE` | `SEMILLA-COLA-TRES` | OK | C sube del todo: empata con A en URGENTE y no lo adelanta porque llego despues (t3 > t1), B baja a la tercera plaza real |
| `TC14-D08` | `TRES` | `C` | `PREFERENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:PREFERENTE` | `SEMILLA-COLA-TRES` | OK | C sube al intermedio y empata con B: gana B por marca de tiempo (t2 < t3) |
| `TC14-D09` | `TRES` | `C` | `ORDINARIA` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | redundancia con el escalon mas bajo: la tercera variante del boton duplicado de reubicar |
| `TC14-D10` | `EMPALTE_BC` | `B` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:URGENTE\|C:ORDINARIA` | `SEMILLA-COLA-EMPALTE-BC` | OK | B sube a URGENTE y empata con A: pierde por marca de tiempo, C conserva su marca gemela pero sigue en su escalon |
| `TC14-D11` | `EMPALTE_BC` | `C` | `PREFERENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:PREFERENTE` | `SEMILLA-COLA-EMPALTE-BC` | OK | C sube y queda con escalon y marca IDENTICOS a B: decide esperaId ascendente (creada antes), y por eso dos lecturas seguidas dan el mismo orden exacto, el TC afirma el bloque {B,C} y la lectura repetida |
| `TC14-D12` | `CON_RETIRO` | `B` | `URGENTE` | `TEXTO` | `AUT` | `B:URGENTE\|C:ORDINARIA` | `SEMILLA-COLA-CON-RETIRO` | OK | A fue retirada: la cola filtrada por ACTIVA no la incluye (sigue en la tabla) y las activas suben una posicion real porque la posicion se calcula, no se almacena |
| `TC14-D13` | `CON_RETIRO` | `C` | `URGENTE` | `TEXTO` | `AUT` | `C:URGENTE\|B:PREFERENTE` | `SEMILLA-COLA-CON-RETIRO` | OK | C sube por encima de B en un mundo con una terminal fuera de la cola activa: el retiro previo no distorsiona el comparador |
| `TC14-D14` | `TERMINALES` | `B` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE` | `SEMILLA-COLA-TERMINALES` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | B esta ASIGNADA (cita en 09:00): reubicarla se corta antes de tocar nada y la cola ACTIVA sigue siendo solo A |
| `TC14-D15` | `TERMINALES` | `C` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE` | `SEMILLA-COLA-TERMINALES` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | reubicar la RETIRADA: mismo corte y codigo de estado |
| `TC14-D16` | `TERMINALES` | `B` | `PREFERENTE` | `TEXTO` | `AUT` | `A:URGENTE` | `SEMILLA-COLA-TERMINALES,REDUNDANTE` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | PRECEDENCIA: terminal con escalon repetido (B ya era PREFERENTE y se le pide PREFERENTE): gana ENTRADA_NO_ACTIVA sobre REUBICACION_REDUNDANTE (las reglas se aplican en orden de declaracion) y el REDUNDANTE del setup solo sirve para montar el escalon igual, no como motivo |
| `TC14-D17` | `SOLO` | `C` | `URGENTE` | `TEXTO` | `AUT` | `C:URGENTE` | `SEMILLA-COLA-SOLO` | OK | reubicar en una cola de una sola entrada: el orden esperado es banal pero la posicion 1 debe seguir apareciendo en el JSON |
| `TC14-D18` | `SOLO` | `C` | `ORDINARIA` | `TEXTO` | `AUT` | `C:ORDINARIA` | `SEMILLA-COLA-SOLO` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | unica entrada, escalon repetido: 409 y cola intacta |
| `TC14-D19` | `NINGUNA` | `-` | `URGENTE` | `TEXTO` | `AUT` | `-` | `SEMILLA-VACIA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | mundo vacio y esperaId nunca sembrado: 404 antes que cualquier regla de prioridad |
| `TC14-D20` | `TRES` | `-` | `URGENTE` | `TEXTO` | `INEXISTENTE` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | esperaId que no existe en una cola con entradas: el 404 depende de la entrada pedida, no del mundo, la cola queda intacta |
| `TC14-D21` | `TRES` | `-` | `URGENTE` | `TEXTO` | `MAL_FORMATADO` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | esperaId con guiones donde va un UUID: PathVariableTypeMismatch traducido a ApiError por el consejo propio (HALLAZGO 3 de CDU19 aplicado desde el diseno) |
| `TC14-D22` | `TRES` | `B` | `ORDINARIA` | `VACIO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | motivo en blanco en una reubicacion por lo demas valida: el autor no puede quedar sin justificacion (la misma leccion que el motivo de cancelacion de CDU12) |
| `TC14-D23` | `TRES` | `B` | `ORDINARIA` | `AUSENTE` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | motivo ausente del cuerpo: obligatorio |
| `TC14-D24` | `TRES` | `B` | `ORDINARIA` | `MAX300` | `AUT` | `A:URGENTE\|B:ORDINARIA\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | OK | valor limite: 300 caracteres, ultima longitud que cabe en motivo_fin y en el eslabon de auditoria |
| `TC14-D25` | `TRES` | `B` | `ORDINARIA` | `LARGO301` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | valor limite: 301 caracteres, se rechaza en el servicio para no acabar en un error de base de datos por truncado |
| `TC14-D26` | `TRES` | `A` | `URGENTE` | `LARGO301` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | PRECEDENCIA: sintaxis gana a redundancia, el pedido es redundante Y el motivo no cabe, y se responde DATOS_INVALIDOS porque la validacion de entrada corre antes que las reglas de negocio |
| `TC14-D27` | `TRES` | `A` | `URGENTE` | `TEXTO` | `AUT` | `A:URGENTE\|B:PREFERENTE\|C:ORDINARIA` | `SEMILLA-COLA-TRES,ENLAZE:NINGUNA` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | la entrada objetivo no tiene solicitud enlazada: reubicar no exige solicitud, solo la asignacion (A ya esta en URGENTE: redundante) |

## Tabla E · secuencias de doble clic y concurrencia: el segundo golpe no escribe nada

**Modo:** `filas` · filas escritas a mano = 12

**Puerta que cubre:** SEG: el doble clic no duplica entradas ni citas ni eslabones de auditoria; la capa dura (UNIQUE de claveDuplicada y UNIQUE de hueco con @Version) aguanta dos pulsaciones concurrentes

### Casos generados

| ID | secuencia | esperadoEslabones | esperadoCola | esperadoCitas | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|
| `TC14-E01` | `ALTA_ALTA` | `GENESIS,ALTA` | `1 ACTIVA` | `0` | `SEMILLA-VACIA` | ALTA_DUPLICADA `CONFLICTO_ALTA_DUPLICADA` | doble clic de alta secuencial: la segunda pulsacion se rechaza por la clave condicionada y la cadena sigue en dos eslabones |
| `TC14-E02` | `ALTA_RETIRAR_ALTA` | `GENESIS,ALTA,RETIRADA / GENESIS,ALTA` | `1 ACTIVA + 1 RETIRADA` | `0` | `SEMILLA-VACIA` | OK | retirar y volver a dar alta es el camino legitimo: la clave pasa a NULL en la terminal y la nueva entrada abre su propia cadena |
| `TC14-E03` | `ALTA_ASIGNAR_ALTA` | `GENESIS,ALTA,ASIGNACION / GENESIS,ALTA` | `1 ACTIVA + 1 ASIGNADA` | `1` | `SEMILLA-VACIA` | OK | reingreso tras asignar: el alta no consulta el estado de la solicitud ni el de la entrada anterior, y el hueco de la cita sigue bloqueado |
| `TC14-E04` | `ALTA_RETIRAR_RETIRAR` | `GENESIS,ALTA,RETIRADA` | `1 RETIRADA` | `0` | `SEMILLA-VACIA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | doble clic de retirar en secuencia: segundo 409 y cero eslabones nuevos |
| `TC14-E05` | `ALTA_PRIORIDAD_VUELTA` | `GENESIS,ALTA,REUBICACION,REUBICACION` | `1 ACTIVA` | `0` | `SEMILLA-VACIA` | OK | URGENTE, bajar a ORDINARIA y volver a URGENTE: dos movimientos legitimos son dos eslabones, la cola vuelve al estado original |
| `TC14-E06` | `ALTA_PRIORIDAD_IGUAL_IGUAL` | `GENESIS,ALTA,REUBICACION` | `1 ACTIVA` | `0` | `SEMILLA-VACIA` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | alta con escalon por defecto y despues dos pedidos de URGENTE: el primero es legitimo y deja UN eslabon de REUBICACION, el segundo es el doble clic y no anade nada, se comprueba que la cadena tiene exactamente tres eslabones |
| `TC14-E07` | `ALTA_ASIGNAR_ASIGNAR` | `GENESIS,ALTA,ASIGNACION` | `1 ASIGNADA` | `1` | `SEMILLA-VACIA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | doble clic de asignar secuencial: la maquina de la entrada corta la segunda y no se crea ni un segundo hueco |
| `TC14-E08` | `ASIGNAR_CONCURRENTES` | `GENESIS,ALTA,ASIGNACION` | `1 ASIGNADA` | `1` | `SEMILLA-VACIA` | UN_OK_Y_OTRO_409 | dos hilos asignan la misma entrada al mismo hueco: exactamente un 201, el perdedor recibe 409 (ENTRADA_NO_ACTIVA o HUECO_OCUPADO segun gane la maquina o el @Version/UNIQUE del hueco: el codigo exacto no es determinista y por eso la fila no lo fija, si no el invariante: UNA cita y UN hueco ocupado y UNA cadena de tres eslabones) |
| `TC14-E09` | `ALTA_ALTA_CONCURRENTES` | `GENESIS,ALTA` | `1 ACTIVA` | `0` | `SEMILLA-VACIA` | UN_OK_Y_OTRO_409 | dos hilos dan alta al mismo paciente y prueba: la clave UNIQUE condicionada es la capa dura, si el servicio dejara pasar ambos por carrera, la base la corta y el consejo la traduce al mismo 409 |
| `TC14-E10` | `RETIRAR_CONCURRENTES` | `GENESIS,ALTA,RETIRADA` | `1 RETIRADA` | `0` | `SEMILLA-VACIA` | UN_OK_Y_OTRO_409 | dos hilos retiran la misma entrada: un eslabon RETIRADA y la marca de fin unica del ganador |
| `TC14-E11` | `ASIGNAR_MISMO_HUECO_ENTRADAS_DISTINTAS` | `GENESIS,ALTA,ASIGNACION / GENESIS,ALTA` | `1 ASIGNADA + 2 ACTIVA` | `1` | `SEMILLA-COLA-TRES` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | el doble clic no es solo sobre la misma entrada: dos entradas distintas quieren el mismo hueco y la proteccion del hueco de CDU12 (UNIQUE profesional+inicio y @Version) lo mantiene indivisible |
| `TC14-E12` | `ALTA_ALTA_DISTINTOS` | `GENESIS,ALTA / GENESIS,ALTA` | `2 ACTIVA` | `0` | `SEMILLA-VACIA` | OK | control negativo de la capa anti-duplicado: pacientes distintos o pruebas distintas dan dos entradas y dos cadenas |

## Tabla F · asignar cita desde la cola: franja horaria, proteccion del hueco y aptitud de la solicitud

**Modo:** `pairwise` · producto de niveles = 29160 · filas generadas = 64 · pares obligatorios cubiertos = 439/439 (100%) · una fila no acumula dos niveles fuera de clase (el rechazo tiene un unico motivo)

**Puerta que cubre:** SEG ninguna cita doble ocupa un hueco + AF correccion: la asignacion delega en CDU12 sin debilitar sus guardas, y la precedencia franja-antes-que-hueco y solicitud-antes-que-franja queda probada fila a fila

### Niveles y criterios

| Factor | Nivel | Clase valida | Criterio elegido | Motivo |
|---|---|---|---|---|
| `esperaId` | `AUT` | valido | clase valida tipica | id de la entrada sembrada por el escenario |
| `esperaId` | `INEXISTENTE` | valido | valor limite: formato correcto, inexistencia real | UUID bien formado que nunca se sembro |
| `esperaId` | `MAL_FORMATADO` | **fuera de clase** | clase invalida: el tipado de la ruta lo corta antes del servicio | no-es-un-uuid en una ruta tipada a UUID |
| `estadoEntrada` | `ACTIVA` | valido | clase valida tipica | entrada en la cola |
| `estadoEntrada` | `ASIGNADA` | valido | clase valida de estado terminal: se rechaza con codigo de estado | terminal con cita en 09:00 |
| `estadoEntrada` | `RETIRADA` | valido | clase valida de estado terminal | terminal con motivo de fin |
| `profesional` | `MEDICO` | valido | clase valida tipica | api-medico, con franja 08:00-15:00 todos los dias salvo festivo |
| `profesional` | `SIN_AGENDA` | valido | valor limite: heredado de CDU12, se responde 500 ERROR_INTERNO (deuda conocida del canon, CDU14 no la agranda ni la arregla a medias) | api-sin-agenda existe como id pero no tiene agenda: AgendaNoEncontradoException sin mapear |
| `profesional` | `DESCONOCIDO` | valido | valor limite: mismo camino que SIN_AGENDA porque nadie valida el id contra un directorio | api-profesional-que-no-existe |
| `profesional` | `VACIO` | **fuera de clase** | clase invalida: CitaServicio lanza IAE la cita necesita un profesional | profesionalId= en el cuerpo |
| `profesional` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | sin clave profesionalId en el cuerpo |
| `inicio` | `AUT` | valido | clase valida tipica | INICIO_TARDE 13:00 de PruebasCitas, dentro de franja y libre salvo que el setup diga lo contrario |
| `inicio` | `APERTURA_JUSTA` | valido | valor limite: borde inferior de la franja | 08:00 en punto, el primer minuto de la franja |
| `inicio` | `CIERRE_LIMITRO` | valido | valor limite: borde superior con duracion justa | 14:30, ultima cita de 30 minutos que cabe antes del cierre |
| `inicio` | `PASADA_CIERRE` | valido | valor limite: un minuto mas alla del borde superior | 15:00 en punto, coincide con el cierre |
| `inicio` | `ANTERIOR_APERTURA` | valido | valor limite: un minuto antes del borde inferior... mas alla: franja entera por debajo | 07:00, antes de la apertura |
| `inicio` | `FESTIVO` | valido | clase valida que se vuelve fuera de franja por calendario | 09:00 del 2026-11-10, festivo declarado en la agenda falsa |
| `inicio` | `DOMINGO` | valido | valor limite: no hay franja de fin de semana en el banco, domingo es clase valida | 13:00 de un domingo: la franja falsa cubre todos los dias |
| `inicio` | `MAL_FORMATADO` | **fuera de clase** | clase invalida: no parsea como OffsetDateTime | inicio=manana al mediodia |
| `inicio` | `AUSENTE` | **fuera de clase** | clase invalida: obligatorio ausente | sin clave inicio en el cuerpo |
| `duracion` | `AUSENTE` | valido | clase valida: opcional ausente | sin clave duracionMinutos en el cuerpo: CitaServicio la pide al catalogo (api-prueba fija 30) |
| `duracion` | `D30` | valido | clase valida tipica | 30 minutos explicitos |
| `duracion` | `D1` | valido | valor limite: minimo positivo | 1 minuto: la cita mas corta que se puede agendar |
| `duracion` | `D0` | valido | valor limite: borde inferior no negativo; CDU14 espeja el canon sin redefinirlo | 0 minutos: el canon de CDU12 lo trata como no fijada y usa la del catalogo |
| `duracion` | `EXCESIVA` | valido | valor limite: desborda el cierre aunque el inicio este dentro | 600 minutos desde cualquier inicio de la franja |
| `duracion` | `D_NEGATIVA` | **fuera de clase** | clase invalida: IAE la duracion no puede ser negativa | -5 minutos |
| `hueco` | `LIBRE` | valido | clase valida tipica | nadie ocupa el tramo pedido |
| `hueco` | `EXACTO` | valido | clase de colision: interseccion maxima | otra cita ocupa exactamente el mismo inicio y duracion del pedido |
| `hueco` | `SOLAPE` | valido | clase de colision: interseccion minima (un minuto de solape basta) | otra cita sembrada respecto al inicio de la propia fila que solapa quince minutos |
| `hueco` | `FANTASMA_EXACTO` | valido | valor limite: ocupacion que ya no cuenta, el hueco vuelve a estar libre | la cita que ocupaba ese inicio fue CANCELADA y su hueco liberado |
| `solicitud` | `CREADA` | valido | clase valida tipica | la entrada enlaza una solicitud CREADA sin cita |
| `solicitud` | `CITADA` | valido | valor limite: aptitud falla por el estado de la solicitud, no por la cola | la solicitud enlazada ya tiene cita detras |
| `solicitud` | `NINGUNA` | valido | valor limite: alta sin solicitud es legal y asignarla no | la entrada no tiene solicitudId |

### Reglas de precedencia (dan el resultado esperado)

| Orden | Condicion | Estado esperado | Codigo de error |
|---|---|---|---|
| 1 | `profesional` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 2 | `inicio` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 3 | `duracion` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 4 | `esperaId` está fuera de clase  | `INVALIDOS` | `DATOS_INVALIDOS` |
| 5 | `esperaId` vale INEXISTENTE | `NO_ENCONTRADA` | `LISTA_ESPERA_NO_ENCONTRADA` |
| 6 | `estadoEntrada` vale ASIGNADA | `NO_ACTIVA` | `CONFLICTO_ENTRADA_NO_ACTIVA` |
| 7 | `estadoEntrada` vale RETIRADA | `NO_ACTIVA` | `CONFLICTO_ENTRADA_NO_ACTIVA` |
| 8 | `solicitud` vale CITADA | `SOLICITUD_NO_APTA` | `CONFLICTO_SOLICITUD_NO_APTA` |
| 9 | `solicitud` vale NINGUNA | `SOLICITUD_NO_APTA` | `CONFLICTO_SOLICITUD_NO_APTA` |
| 10 | `profesional` vale SIN_AGENDA | `ERROR_INTERNO` | `ERROR_INTERNO` |
| 11 | `profesional` vale DESCONOCIDO | `ERROR_INTERNO` | `ERROR_INTERNO` |
| 12 | `inicio` vale FESTIVO | `FUERA_DE_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 13 | `inicio` vale PASADA_CIERRE | `FUERA_DE_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 14 | `inicio` vale ANTERIOR_APERTURA | `FUERA_DE_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 15 | `duracion` vale EXCESIVA | `FUERA_DE_FRANJA` | `CONFLICTO_FUERA_DE_FRANJA` |
| 16 | `hueco` vale EXACTO | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 17 | `hueco` vale SOLAPE | `HUECO_OCUPADO` | `CONFLICTO_HUECO_OCUPADO` |
| 18 | resto de casos | `ASIGNADO` | `-` |

### Casos generados

| ID | esperaId | estadoEntrada | profesional | inicio | duracion | hueco | solicitud | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|---|---|---|---|
| `TC14-F01` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `AUSENTE` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ASIGNADO | asignacion feliz: 201 con la cita creada por CitaServicio, entrada ASIGNADA con citaId y eslabon ASIGNACION en la auditoria de la cola \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=AUSENTE (clase valida: opcional ausente), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F02` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ASIGNADO | asignacion feliz: 201 con la cita creada por CitaServicio, entrada ASIGNADA con citaId y eslabon ASIGNACION en la auditoria de la cola \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F03` | `AUT` | `ACTIVA` | `MEDICO` | `APERTURA_JUSTA` | `D30` | `EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:CREADA` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | colision exacta: CDU12 mantiene su proteccion del hueco bajo la asignacion de CDU14 \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=APERTURA_JUSTA (valor limite: borde inferior de la franja), duracion=D30 (clase valida tipica), hueco=EXACTO (clase de colision: interseccion maxima), solicitud=CREADA (clase valida tipica) |
| `TC14-F04` | `AUT` | `ACTIVA` | `MEDICO` | `PASADA_CIERRE` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 15:00 coincide con el cierre: el inicio queda fuera de [08:00, 15:00) \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=PASADA_CIERRE (valor limite: un minuto mas alla del borde superior), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F05` | `AUT` | `ACTIVA` | `MEDICO` | `PASADA_CIERRE` | `D30` | `EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 15:00 coincide con el cierre: el inicio queda fuera de [08:00, 15:00) \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=PASADA_CIERRE (valor limite: un minuto mas alla del borde superior), duracion=D30 (clase valida tipica), hueco=EXACTO (clase de colision: interseccion maxima), solicitud=CREADA (clase valida tipica) |
| `TC14-F06` | `AUT` | `ACTIVA` | `MEDICO` | `CIERRE_LIMITRO` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 600 minutos desbordan el cierre desde cualquier inicio de la franja: el fin de la cita tambien debe caber \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=CIERRE_LIMITRO (valor limite: borde superior con duracion justa), duracion=EXCESIVA (valor limite: desborda el cierre aunque el inicio este dentro), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F07` | `AUT` | `ACTIVA` | `MEDICO` | `CIERRE_LIMITRO` | `D1` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ASIGNADO | asignacion feliz: 201 con la cita creada por CitaServicio, entrada ASIGNADA con citaId y eslabon ASIGNACION en la auditoria de la cola \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=CIERRE_LIMITRO (valor limite: borde superior con duracion justa), duracion=D1 (valor limite: minimo positivo), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F08` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `SOLAPE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CREADA` | HUECO_OCUPADO `CONFLICTO_HUECO_OCUPADO` | un minuto de solape basta para el rechazo: la interseccion se calcula por tramos, no por inicio exacto \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=SOLAPE (clase de colision: interseccion minima (un minuto de solape basta)), solicitud=CREADA (clase valida tipica) |
| `TC14-F09` | `AUT` | `ACTIVA` | `MEDICO` | `FESTIVO` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | el 2026-11-10 esta marcado como festivo en la agenda falsa \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=FESTIVO (clase valida que se vuelve fuera de franja por calendario), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F10` | `AUT` | `ACTIVA` | `MEDICO` | `ANTERIOR_APERTURA` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 07:00 esta antes de la apertura \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=ANTERIOR_APERTURA (valor limite: un minuto antes del borde inferior... mas alla: franja entera por debajo), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F11` | `AUT` | `ASIGNADA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | la maquina de la cola manda sobre la franja y el hueco: la entrada ya tiene su cita \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ASIGNADA (clase valida de estado terminal: se rechaza con codigo de estado), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F12` | `AUT` | `RETIRADA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | retirada: nadie puede ser asignado desde fuera de la cola \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=RETIRADA (clase valida de estado terminal), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F13` | `INEXISTENTE` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | NO_ENCONTRADA `LISTA_ESPERA_NO_ENCONTRADA` | no hay entrada que asignar: 404 antes de mirar estados ni huecos \| valores limite: esperaId=INEXISTENTE (valor limite: formato correcto, inexistencia real), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F14` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `CITADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CITADA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | guarda propia fail-fast: una solicitud con cita no admite otra, y esto se comprueba antes de consultar huecos \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CITADA (valor limite: aptitud falla por el estado de la solicitud, no por la cola) |
| `TC14-F15` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `LIBRE` | `NINGUNA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:NINGUNA` | SOLICITUD_NO_APTA `CONFLICTO_SOLICITUD_NO_APTA` | sin prescripcion no hay cita que crear: la guarda incluye solicitudId nula \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=NINGUNA (valor limite: alta sin solicitud es legal y asignarla no) |
| `TC14-F16` | `AUT` | `ACTIVA` | `SIN_AGENDA` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ERROR_INTERNO `ERROR_INTERNO` | AgendaNoEncontradoException no tiene mapeo propio desde CDU12: 500 heredado y documentado, CDU14 no introduce un mapeo a medias solo para su endpoint \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=SIN_AGENDA (valor limite: heredado de CDU12, se responde 500 ERROR_INTERNO (deuda conocida del canon, CDU14 no la agranda ni la arregla a medias)), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F17` | `AUT` | `ACTIVA` | `DESCONOCIDO` | `AUT` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ERROR_INTERNO `ERROR_INTERNO` | mismo camino: el id del profesional no se valida contra un directorio en CDU12 ni en CDU14 \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=DESCONOCIDO (valor limite: mismo camino que SIN_AGENDA porque nadie valida el id contra un directorio), inicio=AUT (clase valida tipica), duracion=D30 (clase valida tipica), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F18` | `AUT` | `ACTIVA` | `MEDICO` | `DOMINGO` | `D0` | `FANTASMA_EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CREADA` | ASIGNADO | asignacion feliz: 201 con la cita creada por CitaServicio, entrada ASIGNADA con citaId y eslabon ASIGNACION en la auditoria de la cola \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=DOMINGO (valor limite: no hay franja de fin de semana en el banco, domingo es clase valida), duracion=D0 (valor limite: borde inferior no negativo, CDU14 espeja el canon sin redefinirlo), hueco=FANTASMA_EXACTO (valor limite: ocupacion que ya no cuenta, el hueco vuelve a estar libre), solicitud=CREADA (clase valida tipica) |
| `TC14-F19` | `MAL_FORMATADO` | `ASIGNADA` | `SIN_AGENDA` | `APERTURA_JUSTA` | `AUSENTE` | `SOLAPE` | `CITADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F20` | `INEXISTENTE` | `RETIRADA` | `VACIO` | `APERTURA_JUSTA` | `D0` | `EXACTO` | `CITADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:EXACTO,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F21` | `INEXISTENTE` | `RETIRADA` | `AUSENTE` | `DOMINGO` | `D1` | `SOLAPE` | `NINGUNA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:SOLAPE,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F22` | `INEXISTENTE` | `ASIGNADA` | `DESCONOCIDO` | `MAL_FORMATADO` | `D0` | `FANTASMA_EXACTO` | `NINGUNA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:FANTASMA,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: inicio=manana al mediodia |
| `TC14-F23` | `INEXISTENTE` | `RETIRADA` | `SIN_AGENDA` | `AUSENTE` | `AUSENTE` | `EXACTO` | `NINGUNA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:EXACTO,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: sin clave inicio en el cuerpo |
| `TC14-F24` | `AUT` | `ASIGNADA` | `MEDICO` | `CIERRE_LIMITRO` | `D_NEGATIVA` | `EXACTO` | `CREADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:EXACTO,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F25` | `MAL_FORMATADO` | `RETIRADA` | `DESCONOCIDO` | `CIERRE_LIMITRO` | `EXCESIVA` | `FANTASMA_EXACTO` | `CITADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F26` | `INEXISTENTE` | `ASIGNADA` | `VACIO` | `PASADA_CIERRE` | `EXCESIVA` | `SOLAPE` | `NINGUNA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:SOLAPE,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F27` | `AUT` | `ASIGNADA` | `AUSENTE` | `ANTERIOR_APERTURA` | `D1` | `EXACTO` | `CITADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:EXACTO,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F28` | `INEXISTENTE` | `RETIRADA` | `SIN_AGENDA` | `ANTERIOR_APERTURA` | `D_NEGATIVA` | `FANTASMA_EXACTO` | `NINGUNA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:FANTASMA,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F29` | `MAL_FORMATADO` | `ACTIVA` | `DESCONOCIDO` | `FESTIVO` | `D1` | `EXACTO` | `NINGUNA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F30` | `INEXISTENTE` | `ACTIVA` | `VACIO` | `FESTIVO` | `AUSENTE` | `FANTASMA_EXACTO` | `CITADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F31` | `MAL_FORMATADO` | `RETIRADA` | `SIN_AGENDA` | `PASADA_CIERRE` | `D0` | `LIBRE` | `CREADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F32` | `AUT` | `ASIGNADA` | `VACIO` | `DOMINGO` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F33` | `AUT` | `ACTIVA` | `SIN_AGENDA` | `MAL_FORMATADO` | `EXCESIVA` | `EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: inicio=manana al mediodia |
| `TC14-F34` | `AUT` | `ACTIVA` | `DESCONOCIDO` | `PASADA_CIERRE` | `D_NEGATIVA` | `SOLAPE` | `CITADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F35` | `INEXISTENTE` | `ACTIVA` | `AUSENTE` | `CIERRE_LIMITRO` | `D0` | `SOLAPE` | `NINGUNA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F36` | `AUT` | `ACTIVA` | `SIN_AGENDA` | `AUSENTE` | `D1` | `FANTASMA_EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: sin clave inicio en el cuerpo |
| `TC14-F37` | `AUT` | `ACTIVA` | `AUSENTE` | `PASADA_CIERRE` | `AUSENTE` | `FANTASMA_EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F38` | `MAL_FORMATADO` | `ACTIVA` | `DESCONOCIDO` | `DOMINGO` | `AUSENTE` | `EXACTO` | `CITADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F39` | `AUT` | `ACTIVA` | `AUSENTE` | `APERTURA_JUSTA` | `EXCESIVA` | `LIBRE` | `NINGUNA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:NINGUNA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F40` | `AUT` | `RETIRADA` | `MEDICO` | `MAL_FORMATADO` | `D30` | `FANTASMA_EXACTO` | `CITADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: inicio=manana al mediodia |
| `TC14-F41` | `AUT` | `ASIGNADA` | `MEDICO` | `AUSENTE` | `D30` | `LIBRE` | `CITADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CITADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: sin clave inicio en el cuerpo |
| `TC14-F42` | `MAL_FORMATADO` | `ACTIVA` | `MEDICO` | `AUT` | `D30` | `EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:EXACTO,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F43` | `MAL_FORMATADO` | `ACTIVA` | `DESCONOCIDO` | `ANTERIOR_APERTURA` | `AUSENTE` | `SOLAPE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | PathVariableTypeMismatch: el consejo propio traduce el error de tipo de la ruta a ApiError \| esperaId: no-es-un-uuid en una ruta tipada a UUID |
| `TC14-F44` | `AUT` | `ASIGNADA` | `SIN_AGENDA` | `FESTIVO` | `D0` | `SOLAPE` | `CREADA` | `SEM-ESTADO:ASIGNADA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CREADA` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | la maquina de la cola manda sobre la franja y el hueco: la entrada ya tiene su cita \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ASIGNADA (clase valida de estado terminal: se rechaza con codigo de estado), profesional=SIN_AGENDA (valor limite: heredado de CDU12, se responde 500 ERROR_INTERNO (deuda conocida del canon, CDU14 no la agranda ni la arregla a medias)), inicio=FESTIVO (clase valida que se vuelve fuera de franja por calendario), duracion=D0 (valor limite: borde inferior no negativo, CDU14 espeja el canon sin redefinirlo), hueco=SOLAPE (clase de colision: interseccion minima (un minuto de solape basta)), solicitud=CREADA (clase valida tipica) |
| `TC14-F45` | `AUT` | `ACTIVA` | `VACIO` | `AUT` | `D1` | `FANTASMA_EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F46` | `AUT` | `RETIRADA` | `AUSENTE` | `FESTIVO` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:RETIRADA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F47` | `AUT` | `ACTIVA` | `SIN_AGENDA` | `DOMINGO` | `D_NEGATIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F48` | `AUT` | `ACTIVA` | `DESCONOCIDO` | `APERTURA_JUSTA` | `D1` | `FANTASMA_EXACTO` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:FANTASMA,ENLAZE:CREADA` | ERROR_INTERNO `ERROR_INTERNO` | mismo camino: el id del profesional no se valida contra un directorio en CDU12 ni en CDU14 \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=DESCONOCIDO (valor limite: mismo camino que SIN_AGENDA porque nadie valida el id contra un directorio), inicio=APERTURA_JUSTA (valor limite: borde inferior de la franja), duracion=D1 (valor limite: minimo positivo), hueco=FANTASMA_EXACTO (valor limite: ocupacion que ya no cuenta, el hueco vuelve a estar libre), solicitud=CREADA (clase valida tipica) |
| `TC14-F49` | `AUT` | `ACTIVA` | `DESCONOCIDO` | `AUSENTE` | `D0` | `SOLAPE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: sin clave inicio en el cuerpo |
| `TC14-F50` | `AUT` | `ACTIVA` | `SIN_AGENDA` | `CIERRE_LIMITRO` | `AUSENTE` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | ERROR_INTERNO `ERROR_INTERNO` | AgendaNoEncontradoException no tiene mapeo propio desde CDU12: 500 heredado y documentado, CDU14 no introduce un mapeo a medias solo para su endpoint \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=SIN_AGENDA (valor limite: heredado de CDU12, se responde 500 ERROR_INTERNO (deuda conocida del canon, CDU14 no la agranda ni la arregla a medias)), inicio=CIERRE_LIMITRO (valor limite: borde superior con duracion justa), duracion=AUSENTE (clase valida: opcional ausente), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F51` | `AUT` | `ACTIVA` | `VACIO` | `CIERRE_LIMITRO` | `D30` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F52` | `AUT` | `ACTIVA` | `VACIO` | `ANTERIOR_APERTURA` | `D0` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: profesionalId= en el cuerpo |
| `TC14-F53` | `AUT` | `ACTIVA` | `AUSENTE` | `AUT` | `D0` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | profesionalId ausente o en blanco: IAE de CitaServicio, la guarda de sintaxis corre antes que cualquier llamada \| profesional: sin clave profesionalId en el cuerpo |
| `TC14-F54` | `AUT` | `ACTIVA` | `MEDICO` | `MAL_FORMATADO` | `AUSENTE` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: inicio=manana al mediodia |
| `TC14-F55` | `AUT` | `ACTIVA` | `MEDICO` | `MAL_FORMATADO` | `D1` | `SOLAPE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:SOLAPE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: inicio=manana al mediodia |
| `TC14-F56` | `AUT` | `ACTIVA` | `MEDICO` | `PASADA_CIERRE` | `D1` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 15:00 coincide con el cierre: el inicio queda fuera de [08:00, 15:00) \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=PASADA_CIERRE (valor limite: un minuto mas alla del borde superior), duracion=D1 (valor limite: minimo positivo), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F57` | `AUT` | `ACTIVA` | `MEDICO` | `APERTURA_JUSTA` | `D_NEGATIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F58` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `D_NEGATIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F59` | `AUT` | `ACTIVA` | `MEDICO` | `FESTIVO` | `D_NEGATIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | duracion negativa: IAE la duracion no puede ser negativa \| duracion: -5 minutos |
| `TC14-F60` | `AUT` | `ACTIVA` | `MEDICO` | `AUT` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 600 minutos desbordan el cierre desde cualquier inicio de la franja: el fin de la cita tambien debe caber \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=AUT (clase valida tipica), duracion=EXCESIVA (valor limite: desborda el cierre aunque el inicio este dentro), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F61` | `AUT` | `ACTIVA` | `MEDICO` | `ANTERIOR_APERTURA` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 07:00 esta antes de la apertura \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=ANTERIOR_APERTURA (valor limite: un minuto antes del borde inferior... mas alla: franja entera por debajo), duracion=EXCESIVA (valor limite: desborda el cierre aunque el inicio este dentro), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F62` | `AUT` | `ACTIVA` | `MEDICO` | `FESTIVO` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | el 2026-11-10 esta marcado como festivo en la agenda falsa \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=FESTIVO (clase valida que se vuelve fuera de franja por calendario), duracion=EXCESIVA (valor limite: desborda el cierre aunque el inicio este dentro), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F63` | `AUT` | `ACTIVA` | `MEDICO` | `DOMINGO` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | FUERA_DE_FRANJA `CONFLICTO_FUERA_DE_FRANJA` | 600 minutos desbordan el cierre desde cualquier inicio de la franja: el fin de la cita tambien debe caber \| valores limite: esperaId=AUT (clase valida tipica), estadoEntrada=ACTIVA (clase valida tipica), profesional=MEDICO (clase valida tipica), inicio=DOMINGO (valor limite: no hay franja de fin de semana en el banco, domingo es clase valida), duracion=EXCESIVA (valor limite: desborda el cierre aunque el inicio este dentro), hueco=LIBRE (clase valida tipica), solicitud=CREADA (clase valida tipica) |
| `TC14-F64` | `AUT` | `ACTIVA` | `MEDICO` | `AUSENTE` | `EXCESIVA` | `LIBRE` | `CREADA` | `SEM-ESTADO:ACTIVA,SEM-HUECO-DEST:LIBRE,ENLAZE:CREADA` | INVALIDOS `DATOS_INVALIDOS` | inicio ausente o ilegible: la deserializacion o IAE lo cortan con 400 \| inicio: sin clave inicio en el cuerpo |

## Tabla G · auditoria de la cola: cadena propia con eslabon REUBICACION y verificacion anti-manipulacion

**Modo:** `filas` · filas escritas a mano = 12

**Puerta que cubre:** SEG responsabilidad e integridad: cada accion de cola deja su eslabon con hash encadenado y actor, y la verificacion descubre cualquier manipulacion retroactiva sin tocar la cadena de CDU18

### Casos generados

| ID | caso | esperadoVerificable | esperadoEslabones | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC14-G01` | `ALTA_PURA` | `true` | `GENESIS,ALTA` | `SEMILLA-COLA-TRES` | OK | el alta crea el primer eslabon real y enlaza con el genesis de la cola, igual que el genesis de cita_auditoria en CDU12 |
| `TC14-G02` | `CON_REUBICACION` | `true` | `GENESIS,ALTA,REUBICACION` | `SEMILLA-COLA-EMPALTE-BC` | OK | REUBICACION no existe en AccionAuditoria de CDU12: se anade a la cadena de cola sin romper la tabla de citas (D30) |
| `TC14-G03` | `CON_RETIRADA` | `true` | `GENESIS,ALTA,REUBICACION,RETIRADA` | `SEMILLA-CON-RETIRO` | OK | el eslabon RETIRADA guarda el motivo en el propio hash y deja la entrada terminal, despues de retirar ya no se puede reubicar: la columna de reubicacion final devuelve NO_ACTIVA |
| `TC14-G04` | `MANIPULA-CONTENIDO` | `false` | `-` | `SEM-TAMPER:ENLAZE` | OK | se cambia el motivo de un eslabon ya guardado desde el repositorio: la cadena se rompe en ese eslabon y la verificacion global dice false (conjetura: recompute el hash o corte la cadena) |
| `TC14-G05` | `MANIPULA-BORRADO` | `false` | `-` | `SEM-TAMPER:BORRADO` | OK | se borra un eslabon intermedio: el hash del siguiente queda huerfano y la verificacion debe detectarlo como rotura, no como hueco tolerable |
| `TC14-G06` | `MANIPULA-REORDEN` | `false` | `-` | `SEM-TAMPER:REORDEN` | OK | se invierte el orden de dos eslabones en la tabla: el encadenamiento por hash previo deja de cuadrar |
| `TC14-G07` | `SOLO-GENESIS` | `true` | `GENESIS` | `SEMILLA-SOLO` | OK | mundo con solo genesis: la verificacion global es true (la cadena base existe), y leer la auditoria de una entrada inexistente sigue siendo NO_ENCONTRADA |
| `TC14-G08` | `CADENAS-INDEPENDIENTES` | `true` | `GENESIS,ALTA,ASIGNACION` | `SEMILLA-CITA-OCUPA` | OK | la cita nacida de la cola crea el genesis de su propia cadena en cita_auditoria: cada raiz se verifica por separado y la verificacion de cola no ve la de citas (CDU18 las unira) |
| `TC14-G09` | `SIN-ACCIONES` | `true` | `GENESIS` | `SEMILLA-VACIA` | OK | nadie ha tocado la cola: solo existe el genesis escrito por la migracion y aun asi el balance es verificable |
| `TC14-G10` | `RETIRADA-SIN-MOTIVO` | `true` | `-` | `SEMILLA-CON-RETIRO` | INVALIDOS `DATOS_INVALIDOS` | el motivo es obligatorio al retirar: si no se exige, la auditoria no puede auditar nada (conjetura de auditoria silenciosa) |
| `TC14-G11` | `REUBICAR-TERMINAL` | `true` | `-` | `SEMILLA-CON-RETIRO` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | la entrada ya esta RETIRADA: no se escribe un eslabon REUBICACION sobre una entrada terminal, porque falsearia la regla de longitud de la cadena |
| `TC14-G12` | `PRIORIDAD-REPETIDA` | `true` | `GENESIS,ALTA` | `SEMILLA-COLA-TRES` | REDUNDANTE `CONFLICTO_REUBICACION_REDUNDANTE` | esta es la fila que cierra el doble clic de reubicacion: si se escribiera el eslabon, la cadena creceria con un cambio que no cambio nada |

## Tabla H · conjetura de errores sobre la cola: entrada ilegitima, colision de ruta, dobles intentos y metodos no declarados

**Modo:** `conjetura` · filas escritas a mano = 13

**Puerta que cubre:** AF robustez: ningun ataque ni descuido del cliente convierte la cola en un 500 ni en una respuesta que miente al cliente, y los codigos de Spring (405/415/404 de ruta) se mantienen estables sin cuerpo ApiError

### Casos generados

| ID | ataque | metodo | ruta | Setup | Esperado | Nota del caso |
|---|---|---|---|---|---|---|
| `TC14-H01` | `CUERPO-ILEGIBLE` | `POST` | `/api/listas-espera` | `SEMILLA-VACIA` | CUERPO_ILEGIBLE `DATOS_INVALIDOS` | texto suelto donde va el JSON del alta: 400 con codigo estable, nunca 500 (leccion de CDU12 y CDU19) |
| `TC14-H02` | `CONTENT-TIPO-ERRONEO` | `POST` | `/api/listas-espera` | `SEMILLA-VACIA` | CONTENIDO | enviar el alta como text/plain: Spring responde 415 sin cuerpo ApiError propio, es el canon, no se persigue 400 aqui |
| `TC14-H03` | `QUERY-CON-PARAMS-EN-ALTA` | `POST` | `/api/listas-espera?pruebaCodigo=api-prueba` | `SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | intentar colar los obligatorios por query en lugar de cuerpo: el alta solo lee el cuerpo, asi que faltan y es 400, si alguna vez diera 201, HALLAZGO |
| `TC14-H04` | `PRIORIDAD-INVENTADA-EN-ALTA` | `POST` | `/api/listas-espera` | `SEMILLA-VACIA` | CREADO | prioridad=' URGENTE ' con espacios y minusculas: canon de CDU06, se recorta y normaliza, la cola la sirve como URGENTE en la cuspide |
| `TC14-H05` | `COLISION-RUTA-VARIABLE` | `POST` | `/api/listas-espera/auditoria-verificable/prioridad` | `SEMILLA-VACIA` | INVALIDOS `DATOS_INVALIDOS` | usar la palabra auditoria-verificable como esperaId en la ruta de prioridad: el metodo coincide (todo en cola es POST) y la colision cae en el path variable de UUID, que la rechaza con 400 DATOS_INVALIDOS (HALLAZGO 3 de CDU19, corregido en CDU12: MAL_FORMATADO es INVALIDOS, no NO_ENCONTRADA), con PUT en cambio Spring daria 405 antes de mirar el UUID |
| `TC14-H06` | `FILTRO-CON-PARAM-REPETIDO` | `GET` | `/api/listas-espera?pruebaCodigo=api-prueba&pruebaCodigo=api-otra` | `SEMILLA-MIXTA` | OK | el cliente manda el mismo filtro dos veces con valores distintos: Spring se queda con el primer valor (canon MultiValueMap), se sirve la cola de api-prueba y no hay 500 (es la trampa del diseno: al reves, esta fila estaria en rojo) |
| `TC14-H07` | `PRUEBA-FUERA-DE-CATALOGO-EN-FILTRO` | `GET` | `/api/listas-espera?pruebaCodigo=NO-EXISTE` | `SEMILLA-MIXTA` | FILTRO_INVALIDO `DATOS_INVALIDOS` | filtrar por una prueba que no esta en el catalogo: se prefiere 400 DATOS_INVALIDOS a servir una cola vacia que parece legitima (precedencia fijada en la nota de la tabla A, el 404 se reserva para el alta, donde la prueba si es una referencia que falta) |
| `TC14-H08` | `ESTADO-ABSURDO-EN-FILTRO` | `GET` | `/api/listas-espera?estado=VOLANDO` | `SEMILLA-MIXTA` | INVALIDOS `DATOS_INVALIDOS` | estado=VOLANDO en el filtro: 400, nunca una cola vacia que miente (leccion de CDU12 TC12-07) |
| `TC14-H09` | `COLISION-RUTA-LITERAL` | `GET` | `/api/listas-espera/auditoria-verificable` | `SEMILLA-VACIA` | OK | GET /api/listas-espera/auditoria-verificable: el segmento literal no debe ser secuestrado por la ruta /{esperaId}, Spring da prioridad al literal y responde la verificacion, no un 400 por UUID ilegible (es la trampa del diseno: al reves, esta fila estaria en rojo) |
| `TC14-H10` | `METODO-NO-DECLARADO` | `POST` | `/api/listas-espera/{esperaId}/auditoria` | `SEMILLA-VACIA` | METODO | hacer POST (en lugar de GET) sobre la ruta de auditoria, con un esperaId UUID bien formado pero nunca sembrado: la ruta coincide y el metodo no, asi que Spring da 405 Method Not Allowed sin cuerpo ApiError propio, estabilizado en CDU19 y debe seguir igual |
| `TC14-H11` | `RETIRAR-SIN-ID` | `DELETE` | `/api/listas-espera` | `SEMILLA-VACIA` | METODO | borrar la coleccion entera por descuido: 405, no un 204 que vacie la cola del Servicio de Salud (conjetura de desastre) |
| `TC14-H12` | `ASIGNAR-CON-PARAMS-EN-QUERY` | `POST` | `/api/listas-espera/{esperaId}/asignar?profesionalId=api-medico&inicio=2026-11-13T13:00` | `SEMILLA-COLA-TRES` | INVALIDOS `DATOS_INVALIDOS` | intentar pasar el hueco por query en lugar del cuerpo: el controlador solo lee el cuerpo, asi que faltan obligatorios y es 400, si alguna vez diera 201 habria dos contratos distintos conviviendo (HALLAZGO) |
| `TC14-H13` | `DOBLE-ASIGNAR` | `POST` | `/api/listas-espera/{esperaId}/asignar` | `SEMILLA-COLA-TRES` | NO_ACTIVA `CONFLICTO_ENTRADA_NO_ACTIVA` | el doble clic real sobre el boton de asignar: la montura ejecuta dos intentos seguidos sobre la misma entrada y se comprueba el segundo (el primero debe dar 201). El segundo encuentra la entrada ASIGNADA: por la precedencia fijada la maquina de estados gana a la reserva de hueco, asi que es 409 ENTRADA_NO_ACTIVA sin llegar a consultar la agenda. La red fisica la prueba otra fila (E ASIGNAR_MISMO_HUECO_ENTRADAS_DISTINTAS: ahi si es HUECO_OCUPADO por el UNIQUE de cita_hueco). Si el segundo diera 201, la SEG de no duplicar hueco estaria reventada (D140) y esta fila quedaria en rojo como HALLAZGO |

## Como se ejecutan

```
mvn -B -ntp -pl iso2-citas -am test
```

Los CSV de trabajo estan en `docs/test/iter07/` y su copia en el classpath de pruebas (`iso2-citas/src/test/resources/tdop/`) es identica: el generador las escribe a la vez.

La matriz la ejecuta `Cdu14MatrizTdopTest` en `iso2-citas` (aislado, sin Spring Security, como
`Cdu12MatrizTdopTest`). Notas del ejecutor, para releer la matriz sin sorpresa:

- **Semillas por la puerta de entrada.** Todo lo que se siembra entra por `POST /api/listas-espera`
  (y por `/{esperaId}/prioridad|retirar|asignar`), sin repositorios: la peticion de alta admite
  `fechaSolicitud`, asi que las marcas de tiempo son explicitas y la matriz da el mismo resultado a
  cualquier hora del dia.
- **Pacientes.** `PAC_DUPL` y `PAC_OTRO` no cambian el paciente: el identificador canonico es uno
  solo, y la clave `pacienteId|pruebaCodigo` se dispara por la otra mitad cambiando el codigo de
  prueba (canon del curso: un unico paciente de integracion).
- **Genesis virtual.** No hay fila genesis en `espera_auditoria` (el nacimiento de la cola no es una
  peticion): el primer eslabon real enlaza con el genesis virtual, y las cadenas esperadas se anotan
  por entrada como `GENESIS,ALTA,...`. El ejecutor re-encadena los eslabones de cada entrada con
  `AuditoriaEspera.cadenaIntegra`, la misma comprobacion que hace `auditoria-verificable`.
- **`SEMILLA-SOLO`** (tabla G) es un mundo vacio: con genesis virtual no hay fila que sembrar.
  `SEMILLA-CON-RETIRO` y `SEMILLA-SOLO` son los nombre cortos de `SEMILLA-COLA-CON-RETIRO` y
  `SEMILLA-COLA-SOLO`.
- **Empates y posiciones.** Las filas con empates (A03, D01-D08, F51-F56) no escriben un orden
  literal: el ejecutor ordena el estado real leido del server con el comparador del dominio
  (`ListaEspera.porOrdenDeEspera`) y compara esa secuencia con la servida, ademas de exigir dos
  lecturas identicas y posiciones 1..n. El desempate por `esperaId` (UUID) es determinista dentro de
  cada ejecucion: lo que se promueve a invariante es que el orden sea total y estable.
- **Ataques de manipulacion (`SEM-TAMPER`).** Se sembraria la cadena limpia y despues se rompe a mano
  con los hooks de prueba de `AuditoriaEspera` (`setMotivoParaPruebas`, `setHashParaPruebas`):
  `ENLAZE` reescribe el motivo de un eslabon (su hash ya no encaja con el que firmo el siguiente),
  `REORDEN` invierte dos eslabones de una cadena larga (los enlaces se rompen en medio) y `BORRADO`
  elimina un eslabon central (`espera_auditoria` no tiene FK de estructura: la rotura se descubre por
  encadenado, que es precisamente lo que promete la verificacion).

