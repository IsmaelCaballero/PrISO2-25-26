# Modelo de Iter2 / CFG-02 (CDU17)

Diagramas de la CDU17 "Gestion de seguridad y permisos" (Iteracion 2, configuracion
CFG-02: RBAC con 5 roles + sesion JWT + bloqueo de cuentas). Igual que en el resto de
iteraciones, las fuentes editables del modelo son los ficheros PlantUML (`.puml`) de
este directorio; los diagramas `.staruml` se generan a mano a partir de ellos
(decision D18).

## Fuentes PlantUML (lo que versiona el proyecto)

| Fichero | Diagrama | Que cubre |
|---|---|---|
| `cdu17-casos-uso.puml` | Casos de uso | Login (UC17.1) con `<<include>>` de verificacion BCrypt y emision JWT, `<<extend>>` de bloqueo por intentos, y autenticacion/autorizacion silenciosa del filtro (UC17.5-UC17.6) encadenada a los UC16 del catalogo |
| `cdu17-secuencia.puml` | Secuencia | Inicio de sesion (flujo principal + credenciales invalidas + 5. intento con bloqueo) y despues una peticion con Bearer a traves de `JwtAutenticacionFiltro` (200 / 401 / 403 con `ApiError`) |
| `cdu17-clases-analisis.puml` | Clases de analisis | Conceptos del dominio: cuenta con bloqueo temporal, rol, permiso, matriz de permisos, sesion, operacion protegida |
| `cdu17-clases-diseno.puml` | Clases de diseno | Implementacion real: `iso2-seguridad` 1.1.0-SNAPSHOT (dominio/repositorio/servicio/web), `ConfigSeguridad` del server-app y capa API+UI del client-app 0.1.2-SNAPSHOT |

Convencion de las fuentes: ASCII puro en comentarios y etiquetas de PlantUML (sin
tildes ni emes quenas acentuadas: se escriben "contrasena", "Queda", etc. con
`'`) para que no haya ambiguedad de codificacion; UTF-8 para el resto.

## Visualizar / exportar a imagen

Con PlantUML local (o la extension de VS Code/IntelliJ):

```text
java -jar plantuml.jar -tpng docs/modelo/iter02/*.puml
```

(En PlantUML >= 1.2025 sin Graphviz instalado, anadir `-Playout smetana`.)
La sintaxis es la estandar de PlantUML (`@startuml`/`@enduml`); no usa macros ni
includes entre ficheros, asi que cada diagrama se procesa por separado.
Los cuatro fuentes de este directorio se comprobaron renderizando a SVG sin errores.

## Exportacion a StarUML (artefactos de entrega, D18)

La entrega pide modelos en StarUML (`.staruml`). Procedimiento acordado:

1. Abrir StarUML 6 (o 5) con el complemento **PlantUML** instalado
   (StarUML > Extension Manager > `staruml-plantuml` / PlantUML for StarUML).
2. Para cada `.puml` de este directorio: en StarUML, `File > PlantUML > Import`
   (o pegar el fuente) y dejar que genere el diagrama dentro del modelo `iso2-iter02`.
3. Revisar el diagrama importado (el plugin reposiciona elementos: ordenar y
   renombrar si hace falta) y anadirlo al modelo de la iteracion.
4. Guardar el resultado en `docs/modelo/iter02/staruml/iso2-iter02.staruml`
   (un unico modelo StarUML por iteracion, con los 4 diagramas como subdiagramas).
5. Subir ese fichero al repositorio como artefacto de entrega de CFG-02.

Este paso es manual y le corresponde a Ismael (D18): el agente deja las fuentes
`.puml` revisables en GitHub (los `.puml` se ven como texto y las imagenes se pueden
pegar en la wiki/PR si hace falta).

## Relacion con el codigo

El diagrama de diseno describe el codigo ya implementado en Iter2:

- `iso2-common` 1.1.0-SNAPSHOT: `es.uclm.iso2.common.seguridad.AutenticacionFacade`
  (fachada congelada + `CredencialesDTO`/`SesionDTO`) y `shared.ApiError`.
- `iso2-seguridad` 1.1.0-SNAPSHOT (modulo nuevo de CFG-02): dominio (`Rol`, `Permiso`,
  `MatrizPermisos`, `Usuario` JPA con bloqueo, excepciones), `UsuarioRepositorio`,
  `SeguridadServicio` (con `noRollbackFor` para que el bloqueo persista), `JwtUtils`
  y capa web (`ControladorAutenticacion`, `JwtAutenticacionFiltro`).
- `iso2-server-app` 0.1.2-SNAPSHOT: `ConfigSeguridad` (chain stateless + matriz RBAC
  por endpoint + 401/403 con `ApiError`), `V1.2.0__cdu17_seguridad.sql` (6 cuentas
  BCrypt sembradas) y `application.yml` con `ISO2_JWT_SECRET` por entorno.
- `iso2-client-app` 0.1.2-SNAPSHOT: `ApiClienteAutenticacion`, `ApiClienteCatalogo`
  (Bearer), `NavegacionPorRol`, `VistaInicioSesion`, `VistaConsultaCatalogo` y
  arranque reencadenado en `Iso2ClientApplication`.

Si el codigo cambia tras los TC humanos de CDU17 (D28), actualizar primero el `.puml`
y volver a exportar el `.staruml`.
