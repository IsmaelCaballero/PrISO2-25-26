# Modelo de Iter1 / CFG-01 (CDU16)

Diagramas de la CDU16 "Configurar parametros: tipos de prueba, protocolos, politicas"
(Iteracion 1, configuracion CFG-01). Igual que en el resto de iteraciones, las fuentes
editables del modelo son los ficheros PlantUML (`.puml`) de este directorio; los
diagramas `.staruml` se generan a mano a partir de ellos (decision D18).

## Fuentes PlantUML (lo que versiona el proyecto)

| Fichero | Diagrama | Que cubre |
|---|---|---|
| `cdu16-casos-uso.puml` | Casos de uso | Administrador + roles de rol (solo consulta): UC16.1-UC16.6 dentro del sistema y UC7 de auditoria encadenada fuera de la frontera, con `<<include>>` desde los UC de escritura |
| `cdu16-secuencia.puml` | Secuencia | Alta de prueba: flujo principal + alt por conflicto de codigo (409) |
| `cdu16-clases-analisis.puml` | Clases de analisis | Conceptos del dominio: prueba, protocolo, politica, auditoria encadenada |
| `cdu16-clases-diseno.puml` | Clases de diseno | Implementacion real 1.1.0: fachada + DTOs, entidad JPA + embebidos, repositorios, servicio, controlador, advice, cliente JavaFX |

Convencion de las fuentes: ASCII en los comentarios (evitar acentos/emes en lineas
`'`) para que no haya ambiguedad de codificacion; UTF-8 para el resto.

## Visualizar / exportar a imagen

Con PlantUML local (o la extension de VS Code/IntelliJ):

```text
java -jar plantuml.jar -tpng docs/modelo/iter01/*.puml
```

La sintaxis es la estandar de PlantUML (`@startuml`/`@enduml`); no usa macros ni
includes entre ficheros, asi que cada diagrama se procesa por separado.

## Exportacion a StarUML (artefactos de entrega, D18)

La entrega pide modelos en StarUML (`.staruml`). Procedimiento acordado:

1. Abrir StarUML 6 (o 5) con el complemento **PlantUML** instalado
   (StarUML > Extension Manager > `staruml-plantuml` / PlantUML for StarUML).
2. Para cada `.puml` de este directorio: en StarUML, `File > PlantUML > Import`
   (o pegar el fuente) y dejar que genere el diagrama dentro del modelo `iso2-iter01`.
3. Revisar el diagrama importado (el plugin reposiciona elementos: ordenar y
   renombrar si hace falta) y anadirlo al modelo de la iteracion.
4. Guardar el resultado en `docs/modelo/iter01/staruml/iso2-iter01.staruml`
   (un unico modelo StarUML por iteracion, con los 4 diagramas como subdiagramas).
5. Subir ese fichero al repositorio como artefacto de entrega de CFG-01.

Este paso es manual y le corresponde a Ismael (D18): el agente deja las fuentes
`.puml` revisables en GitHub (los `.puml` se ven como texto y las imagenes se pueden
pegar en la wiki/PR si hace falta).

## Relacion con el codigo

El diagrama de diseno describe el codigo ya implementado en Iter1:

- `iso2-common` 1.1.0-SNAPSHOT: `es.uclm.iso2.common.catalogo.PruebaCatalogoFacade`
  (DTOs anidados con validacion en el constructor) y `es.uclm.iso2.common.shared`
  (`Paginacion`, `ApiError`).
- `iso2-catalogo` 1.1.0-SNAPSHOT: dominio (`TipoPrueba`, `Protocolo`, `Politica`,
  `AuditoriaCatalogo`), repositorios, `CatalogoServicio` y capa web
  (`CatalogoControlador`, `CatalogoManejadorErrores`).
- `iso2-server-app` 0.1.1-SNAPSHOT: ensamblado + `ConfigSeguridad` (RBAC transitorio).
- `iso2-client-app` 0.1.1-SNAPSHOT: `ApiClienteCatalogo`/`ApiException` +
  `VistaAdminCatalogo`.

Si el codigo cambia tras los TC humanos de CDU16 (D28), actualizar primero el `.puml`
y volver a exportar el `.staruml`.
