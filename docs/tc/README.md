# TC humanos de las CDU (D24 y D28)

Aquí se diseñan y documentan los **casos de prueba funcionales** que exige el PUD para
cada CDU. Por **D28** la ejecución la hace **Ismael** sobre el código ya fusionado (firmas
de API, códigos de error y pantallas reales). Desde el 08/11 rige también **D33**: el agente
**sí diseña y genera** los casos y las plantillas, a partir de **clases de equivalencia**,
**teorema del valor límite** y **conjetura de errores**, compuestos con **pairwise**; esos
**mismos casos** son los que se ejecutan con **JUnit en Maven** y los que aparecen aquí, con
el mismo ID. Los tests técnicos del agente (JUnit/Surefire/JaCoCo/ArchUnit) NO sustituyen a
estos TC: unos y otros comparten casos y conviven como ejecuciones distintas.

## Regla de mínimos por CDU (D24)

Cada CDU necesita como mínimo: **1 TC feliz + 2 TC de error + 1 TC de permisos**.
La traza RF ↔ CU ↔ TC es criterio de cierre de cada configuración (CFG-01/02/03).

## De dónde salen las filas (D33)

Cada plantilla se deriva de una matriz de diseño, no de una lista improvisada:

1. **Clases de equivalencia**: una fila por clase válida y por cada clase no válida de cada
   entrada y estado del dominio (la justifica la regla de negocio o la restricción de
   `iso2-common`/la BD que la origina).
2. **Valor límite**: de cada clase ordenada se prueban los cuatro puntos del borde
   (`min-1`, `min`, `max`, `max+1`). Las cotas salen de una única fuente: los `record` de
   `iso2-common`, las anotaciones JPA y la migración Flyway.
3. **Conjetura de errores**: casos escogidos por defecto plausible (nulos/vacíos, unicode,
   campos largos, estados imposibles, reenvío de peticiones, concurrencia sobre el mismo recurso).
4. **Pairwise**: cuando un caso depende de varios factores se cubre **todo par de niveles**
   de los factores, no todas las combinaciones.

La matriz (factores, niveles, puntos límite y pares cubiertos) se versiona en
`docs/test/iterXX/`; el ID `TCnn-xx` de la plantilla es el mismo que aparece en el
`@DisplayName` del test JUnit que implementa ese caso.

## Cómo rellenar las plantillas

Una fila = un caso de prueba. Columnas:

| Columna | Quién la rellena | Qué se espera |
|---|---|---|
| ID | prefijado | `TCnn-xx` (nn = CDU). Si añades filas, añade IDs nuevos. |
| Título | prefijado con foco | título corto del caso; edítalo si lo afinas. |
| Tipo | prefijado | `feliz` · `error` · `permisos` (añade `auditoría` si separas ese TC). |
| Puerta que cubre | prefijado | fila de la matriz de puertas de `SOFTWAREQUALITY.md` §5. Es la trazabilidad RF→TC. |
| Pasos | **Ismael** | pasos concretos y reproducibles ( endpoint + método + payload, o clics en el cliente JavaFX ). |
| Resultado esperado | **Ismael** | status HTTP + código de error + estado observable (BD/H2, auditoría, pantalla). |
| Resultado real | **Ismael** | lo que obtuvo al ejecutar. Vacío = no ejecutado aún. |
| Pasa | **Ismael** | `sí` / `no` / `–`. |
| Fecha/ejecutor | **Ismael** | `AAAA-MM-DD` + quién lo ejecutó. |

## Sobre qué código se ejecutan

Los tres CDU ya están fusionados en `develop`, así que pueden ejecutarse todos contra
la misma instalación (o contra el código de su iteración si se prefiere, usando los
tags `cfg-iter01` = `df09bca`, `cfg-iter02` = `870524e`, `cfg-iter03` = `b072b34`).

Arranque local desde la raíz del repositorio:

```text
mvn -B verify javadoc:javadoc                # suite técnica del agente (no es este TC humano)
mvn spring-boot:run -pl iso2-server-app      # servidor en http://localhost:8080 (H2 en ./data)
mvn javafx:run -pl iso2-client-app           # cliente JavaFX
```

Console H2 (solo rol ADMINISTRADOR): `http://localhost:8080/h2-console`.

### Usuarios semilla de desarrollo (migración `V1.2.0__cdu17_seguridad.sql`)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `iso2-admin` | ADMINISTRADOR |
| `medico` | `iso2-medico` | MEDICO |
| `tecnico` | `iso2-tecnico` | TECNICO |
| `recepcionista` | `iso2-rec` | RECEPCIONISTA |
| `celador` | `iso2-ses` | CELADOR |
| `bloqueable` | `iso2-bloq` | MEDICO (cuenta pensada para el TC de bloqueo por intentos) |

Los TC de permisos se hacen casi siempre combinando estos usuarios (login en
`POST /api/auth/login` y usar el JWT como `Bearer`, o cambiar de usuario en el cliente).

## Ficheros

| CDU | Fichero | Configuración | Estado de la plantilla |
|---|---|---|---|
| CDU16 · catálogo de parámetros | `cdu16-tc-humanos.md` | CFG-01 (Iter1) | escrita a mano; **pendiente de regenerar con D33** (empieza por aquí) |
| CDU17 · seguridad y permisos | `cdu17-tc-humanos.md` | CFG-02 (Iter2) | escrita a mano; pendiente de regenerar con D33 |
| CDU13 · agendas de médicos y técnicos | `cdu13-tc-humanos.md` | CFG-03 (Iter3) | escrita a mano; pendiente de regenerar con D33 |
| CDU06 · solicitar/prescribir pruebas | `cdu06-tc-humanos.md` | CFG-04 (Iter4) | creada 08/11 (14 casos) sobre la superficie real de la API |

Al terminar de rellenar cada fichero, subirlo en un commit
`docs(tc · CDUxx): tabla de TC humanos` y comentarlo en `STATUS.md` (la CFG correspondiente
queda **cerrada** cuando la tabla está ejecutada y aprobada; seguimiento en `STATUS.md`,
issue #21 cerrada como contenedor).
