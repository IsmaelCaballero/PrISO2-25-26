# CONTEXT.md — Reglas permanentes del agente IA en PrISO2-25-26

> Fichero de configuración persistente del desarrollo (SDD). Todo agente que trabaje en este
> repositorio DEBE cumplir estas reglas. Las decisiones de fondo están en `DECISIONS.md`;
> el plan en `PLAN.md`; el estado en `STATUS.md`.

## 1. Sistema: dos aplicaciones

1. **Aplicación servidora** (`iso2-server-app` + módulos de dominio): lógica de negocio,
   persistencia y API REST/JSON. Toda funcionalidad de un CDU vive PRIMERO en el servidor.
2. **Aplicación cliente** (`iso2-client-app`): una sola app cuya **interfaz varía según el rol**
   del usuario autenticado (paciente, médico, administrativo, administrador).
3. Cada CDU de la planificación se **desglosa en parte servidora y parte cliente**
   (ver columna correspondiente en `PLAN.md`).
4. **Gestión de usuarios y credenciales**: en v1 una persona solo desempeña **un rol**
   (supuesto de simplificación registrado en DECISIONS D10). Login local en `iso2-seguridad`
   (tabla de usuarios, BCrypt, token de sesión); el SSO corporativo queda en CDU19.

## 2. Tecnología (fija, no negociable sin ADR nuevo)

- Lenguaje: **Java** (JDK 21).
- Construcción: **Maven multi-módulo** (un módulo por componente; `iso2-parent` agregador).
- Servidor: Spring Boot (REST/JSON) + H2 embebido + JPA. Cliente: JavaFX.
- Pruebas: **JUnit 5** (los casos de prueba se DISEÑAN a mano por los alumnos y el agente los implementa).
- Informes de testing: **Surefire + JaCoCo** (`mvn verify` → `target/site/jacoco`).
- Documentación: **Javadoc en TODAS las clases y paquetes** (`package-info.java` incluido);
  `mvn javadoc:javadoc` debe pasar sin errores en cada iteración.
- Versionado de artefactos: **Semantic Versioning 2.0** en el `<version>` de cada módulo + tag Git.

## 3. Git Flow y trazabilidad

- Rama `main`: solo releases (tags `vX.Y.Z`). **Prohibido commitear directamente en `main` o `develop`.**
- Ramas de trabajo: `feature/iterXX-CDUyy-<servidor|cliente|tests|docs>`.
- **Solo Ismael aprueba y fusiona Pull Requests.** El agente crea la rama, commita, hace push
  y abre el PR con descripción de tareas; NUNCA hace merge.
- **Formato de commit obligatorio**: título con módulo abordado + cuerpo con resumen de cada tarea.
  ```
  feat(iso2-citas · Iter5/CDU12): motor de asignación de citas (parte servidora)

  - Interfaz AgendaGateway y CitaService (diseño alumno) + implementación (agente)
  - Tests JUnit: 14 casos (TC-CITA-01..14), Surefire 14/14 en verde
  - JaCoCo: cobertura módulo 84% · Javadoc sin errores
  ```
- Cada tarea tiene su **issue** con: objetivos, disciplinas PUD, contratos/interfaces involucrados,
  criterios de aceptación (DoD), estimación de horas (con y sin agente) y un **prompt listo para
  ser simulado** por el asignatario (plantilla en `PLAN.md`).
- **Configuración (D25–D26)**: cada merge de iteración a `develop` va etiquetado `cfg-iterNN`
  (configuración mínima de esa iteración, lista para desplegar y para rollback). Etiquetas de
  release `v0.2.0-alpha / v0.3.0-alpha / v0.4.0-beta / v0.9.0-rc / v1.0.0` en `main` vía rama
  `release/Rn`; **ninguna etiqueta de release contiene `SNAPSHOT`**. Tras cada merge, el guardián de
  calidad verifica que las versiones de los POM coinciden con `docs/componentes-versiones+Calidad.csv`.

## 4. Modelado por iteración (StarUML)

Por cada iteración se generan y guardan en `docs/modelo/iterXX/`:
1. Diagrama de **casos de uso** (del CDU de la iteración, actualizado al global).
2. Diagramas de **secuencia/interacción** (flujos principal y alternativos del CDU).
3. Diagrama de **clases de análisis** (entidades del dominio).
4. Diagramas de **clases de diseño** más relevantes (interfaces + clases nuevas del módulo).

Formato de trabajo: fuente **PlantUML** en el repo (económico en tokens y diferenciable en Git)
+ integración/exportación a **StarUML** (.staruml) por el equipo humano. *(Confirmar con Ismael: D18.)*

## 5. Ficheros vivos (obligatorio actualizarlos al final de cada iteración/sesión)

| Fichero | Misión |
|---|---|
| `DECISIONS.md` | Decisiones y supuestos (ADRs). Toda decisión relevante = nueva entrada. |
| `PLAN.md` | Plan completo del proyecto (fases, iteraciones, despliegue, releases). |
| `STATUS.md` | Estado real: iteración en curso, commits/PRs, métricas, siguiente acción, registro de sesión. |

## 6. Disciplina de iteraciones PUD

- En cada iteración se trabaja **exclusivamente el CDU planificado** (nada de "todo a la vez").
- Un CDU toca su módulo servidor (nueva versión minor o módulo nuevo) y la tajada de vistas
  correspondiente en el cliente: nunca se rompen contratos publicados (SemVer estricto).
- Reglas de arquitectura (guardarailes): comunicación entre módulos solo por interfaces de
  `iso2-common`; capas dominio/servicio-adaptador/vista; datos clínicos siempre tras `iso2-seguridad`;
  DICOM/HL7/FHIR solo tras fachadas de `iso2-integracion`. **Estas reglas se verifican con ArchUnit
  en CI** (reglas escritas en Iter0, antes que el código): violación = PR no fusionable.

## 6bis. Puertas de calidad (D24 — detalle en SOFTWAREQUALITY.md y PUD +Calidad §8)

- Modelo ISO/IEC 25010 acotado: adecuación funcional, usabilidad (operabilidad + protección de
  errores), fiabilidad (disponibilidad, tolerancia a fallos, recuperabilidad), seguridad
  (confidencialidad, integridad, **no repudio**, responsabilidad) y mantenibilidad (**MVC+SOLID**,
  analizabilidad, testabilidad).
- En cada iteración: TC humanos (≥1 camino feliz + ≥2 error + 1 permisos por RF), RBAC
  deny-by-default testeado en cada endpoint, **`@Auditable` en toda mutación de estado** (auditoría
  con hash encadenado), y umbrales JaCoCo del componente (≥85 % en citas/solicitudes/seguridad).
- Puertas por release (α 60 % → α 70 % → β 75 % → RC 78 % → final 80 % línea; Surefire/ArchUnit = 0).
- **Un CDU no se marca "Hecha" si su exigencia de calidad verificable
  (`docs/planificacion-iteraciones+Calidad.csv`) no está probada.** Prioridad si va justo de tiempo:
  corrección y seguridad > protección contra errores > resto de usabilidad > prestación.
- Guardián de calidad rotatorio por release: ejecuta puertas, audita configuración, publica métricas
  en `STATUS.md`. Nadie se auto-fusiona su PR.

## 7. Gestión de contexto del agente (tokens)

- Presupuesto por iteración (ver tabla en `PLAN.md`): Baja ≈ 50–70k tokens · Media ≈ 90–130k · Alta ≈ 150–220k.
- Iteraciones de **complejidad Alta**: se parten en dos sesiones/semi-iteraciones:
  (a) análisis + diagramas + módulo servidor + tests servidor; (b) cliente + tests integración + informes.
- Si el contexto de sesión se acerca al límite: **avisar a Ismael**, terminar lo en curso solo si no
  excede el límite, **guardar `STATUS.md` y `PLAN.md`** (con punto exacto de continuación) y abrir sesión nueva.
- Toda sesión nueva arranca leyendo `CONTEXT.md` + `STATUS.md` + `PLAN.md` + `DECISIONS.md`.

## 8. Plan de despliegue (resumen; detalle en PLAN.md)

| Canal | Versión | Criterio |
|---|---|---|
| **alfa** | 0.1.0 (R0) → 0.2.0-alpha (R1) | Arquitectura interna, solo equipo |
| **beta** | 0.4.0-beta (R3) → 0.9.0-RC (R4) | Funcionalidad completa, pruebas de sistema/UAT |
| **final** | 1.0.0 (R5, 15/12/2026) | Integrada, probada, desplegada, con manual |
