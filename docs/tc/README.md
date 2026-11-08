# TC humanos de las CDU (D24 y D28)

Aquí se diseñan y documentan los **casos de prueba funcionales** que exige el PUD para
cada CDU. Por **D28** la tabla la diseña y ejecuta **Ismael** sobre el código ya
fusionado (firmas de API, códigos de error y pantallas reales); el agente solo aporta
esta estructura y las pistas de qué puerta de calidad cubre cada TC, y luego traduce a
JUnit lo que proceda. Los tests técnicos del agente (JUnit/Surefire/JaCoCo/ArchUnit)
NO sustituyen a estos TC: unos y otros conviven.

## Regla de mínimos por CDU (D24)

Cada CDU necesita como mínimo: **1 TC feliz + 2 TC de error + 1 TC de permisos**.
La traza RF ↔ CU ↔ TC es criterio de cierre de cada configuración (CFG-01/02/03).

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

| CDU | Fichero | Configuración |
|---|---|---|
| CDU16 · catálogo de parámetros | `cdu16-tc-humanos.md` | CFG-01 (Iter1) |
| CDU17 · seguridad y permisos | `cdu17-tc-humanos.md` | CFG-02 (Iter2) |
| CDU13 · agendas de médicos y técnicos | `cdu13-tc-humanos.md` | CFG-03 (Iter3) |

Al terminar de rellenar cada fichero, subirlo en un commit
`docs(tc · CDUxx): tabla de TC humanos` y comentarlo en `STATUS.md` (la CFG correspondiente
queda **cerrada** cuando la tabla está ejecutada y aprobada; seguimiento en `STATUS.md`,
issue #21 cerrada como contenedor).
