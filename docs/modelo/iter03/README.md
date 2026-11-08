# Modelo de Iter3 / CFG-03 (CDU13)

Diagramas de la CDU13 "Gestion de agendas de medicos y tecnicos" (Iteracion 3,
configuracion CFG-03: agendas con franjas de disponibilidad sin solapes, festivos
que mandan sobre las franjas, RBAC ampliado con `agenda:leer`/`agenda:escribir` y
auditoria encadenada). Igual que en el resto de iteraciones, las fuentes editables
del modelo son los ficheros PlantUML (`.puml`) de este directorio; los diagramas
`.staruml` se generan a mano a partir de ellos (decision D18).

## Fuentes PlantUML (lo que versiona el proyecto)

| Fichero | Diagrama | Que cubre |
|---|---|---|
| `cdu13-casos-uso.puml` | Casos de uso | Consulta de agenda y huecos libres (UC13.1-2), alta/baja de franjas y festivos (UC13.3-7), `<<include>>` de las comprobaciones de solape y festivo (UC13.8-9) y de la auditoria encadenada (UC13.10), todo `<<include>>` en la autenticacion/autorizacion CDU17 |
| `cdu13-secuencia.puml` | Secuencia | `GET /api/agendas/{pid}/libres` (200), alta de franja con sus ramas 404/409 SOLAPE/409 FESTIVO/400, alta de festivo con "el festivo manda" (409 en ambos sentidos) y el 403 de la matriz sin llegar al servicio |
| `cdu13-clases-analisis.puml` | Clases de analisis | Conceptos del dominio: agenda de profesional, franja de disponibilidad, festivo, hueco libre, conflicto (SOLAPE/FESTIVO/AGENDA_DUPLICADA), registro de auditoria y permiso de agenda; invariantes del CDU |
| `cdu13-clases-diseno.puml` | Clases de diseno | Implementacion real: contrato `AgendaFacade` 1.1.0 (baseline congelado + records nuevos), `iso2-agendas` 1.1.0-SNAPSHOT (dominio/repositorio/servicio/web), RBAC ampliado en `iso2-seguridad`, `ConfigSeguridad` 0.1.3 y la parte de cliente pendiente de Prompt 11b |

Convencion de las fuentes: ASCII puro en comentarios y etiquetas de PlantUML (sin
tildes ni emes quenas acentuadas: se escriben "contrasena", "Queda", etc. con
`'`) para que no haya ambiguedad de codificacion; UTF-8 para el resto.

## Visualizar / exportar a imagen

Con PlantUML local (o la extension de VS Code/IntelliJ):

```text
java -jar plantuml.jar -tpng docs/modelo/iter03/*.puml
```

(En PlantUML >= 1.2025 sin Graphviz instalado, anadir `-Playout smetana`.)
La sintaxis es la estandar de PlantUML (`@startuml`/`@enduml`); no usa macros ni
includes entre ficheros, asi que cada diagrama se procesa por separado.
Los cuatro fuentes de este directorio se comprobaron renderizando a SVG sin errores
(PlantUML + smetana, prompt 11).

## Exportacion a StarUML (artefactos de entrega, D18)

La entrega pide modelos en StarUML (`.staruml`). Procedimiento acordado:

1. Abrir StarUML 6 (o 5) con el complemento **PlantUML** instalado
   (StarUML > Extension Manager > `staruml-plantuml` / PlantUML for StarUML).
2. Para cada `.puml` de este directorio: en StarUML, `File > PlantUML > Import`
   (o pegar el fuente) y dejar que genere el diagrama dentro del modelo `iso2-iter03`.
3. Revisar el diagrama importado (el plugin reposiciona elementos: ordenar y
   renombrar si hace falta) y anadirlo al modelo de la iteracion.
4. Guardar el resultado en `docs/modelo/iter03/staruml/iso2-iter03.staruml`
   (un unico modelo StarUML por iteracion, con los 4 diagramas como subdiagramas).
5. Subir ese fichero al repositorio como artefacto de entrega de CFG-03.

Este paso es manual y le corresponde a Ismael (D18): el agente deja las fuentes
`.puml` revisables en GitHub (los `.puml` se ven como texto y las imagenes se pueden
pegar en la wiki/PR si hace falta).

## Relacion con el codigo

El diagrama de diseno describe el codigo ya implementado en la parte servidora
de Iter3:

- `iso2-common` 1.1.0-SNAPSHOT: `es.uclm.iso2.common.agendas.AgendaFacade`
  (baseline congelado `franjasLibres`/`FranjaDTO` + metodos MINOR de gestion con
  `AgendaDTO`/`FranjaGestionDTO`/`FestivoDTO`) y las excepciones
  `AgendaNoEncontradoException` y `AgendaConflictoException` (enum `Conflicto`).
- `iso2-agendas` 1.1.0-SNAPSHOT (elevado de baseline a CDU en CFG-03): dominio
  (`Agenda`, `Franja` con `solapaCon`, `Festivo`, `AuditoriaAgendas` con cadena
  SHA-256), repositorios, `AgendaServicio` (reglas de solape/festivo/inicio<fin,
  semilla de auditoria GENESIS y `auditoriaVerificable()`) y capa web
  (`AgendaControlador` en `/api/agendas` + `AgendaManejadorErrores` con codigos
  404/409/400 estables).
- `iso2-seguridad` 1.1.0-SNAPSHOT (ampliado): `Permiso` anade `AGENDA_LEER` y
  `AGENDA_ESCRIBIR`; `MatrizPermisos` concede lectura a MEDICO/TECNICO/RECEPCIONISTA
  y escritura a RECEPCIONISTA (ADMINISTRADOR tiene todo por `allOf`).
- `iso2-server-app` 0.1.3-SNAPSHOT: `ConfigSeguridad` con las reglas de
  `/api/agendas/**` derivadas de la matriz, `V1.3.0__cdu13_agendas.sql` (5 tablas
  `agenda_*` + indices + semilla de desarrollo para medico y tecnico) y
  `AgendasApiTest` (RBAC + 409 solape/festivo/duplicada por HTTP).
- `iso2-client-app` 0.1.3-SNAPSHOT (pendiente, Prompt 11b): `ApiClienteAgendas` y
  `VistaGestionAgendas` con Bearer; el diagrama de diseno las marca en gris.

Tests tecnicos de esta CDU: `AgendaServicioTest` (14 casos: reglas, conflictos,
auditoria verificable y rotura por manipulacion), `ArquitecturaAgendasTest`
(frontera ArchUnit de CFG-03) y `AgendasApiTest` (gate HTTP). Los TC funcionales
humanos de CDU13 los ejecuta Ismael (D28) contra la semilla de desarrollo.

Si el codigo cambia tras los TC humanos de CDU13 (D28), actualizar primero el
`.puml` y volver a exportar el `.staruml`.
