# Componentes: arquitectura, evolución por versiones y foco de calidad ISO 25010 — versión +Calidad (v2.0.0)

> **Nueva versión** de `docs/componentes-versionado.md` (v1.x). Conserva la arquitectura, el mapeo
> CDU→componente, la priorización por riesgo y la evolución semver de incrementos, y añade la dimensión
> de calidad (D24): **foco ISO 25010 por componente**, **criterios de aceptación de calidad por
> incremento**, **umbrales de cobertura objetivo** y **etiquetas de configuración/release (CFG-NN)**
> en la matriz global. Versiones anteriores: historial Git (rama `feature/docs-plan-inicial`, PR #1).
> Calidad por CDU: `SOFTWAREQUALITY.md` §3. Mapa CFG completo: `docs/mapa-configuraciones.md` (+CSV).

## 1. Arquitectura del sistema: 14 módulos Maven

Un agregador + un transversal + diez de dominio + dos aplicaciones:

```
iso2-parent (agregador · tren de versiones de release)
│  TRANSVERSAL
├── iso2-common        · contratos de dominio, DTO, interfaces de puerto (bus común servidor↔cliente)
│  DOMINIO (servidor)
├── iso2-catalogo      · catálogo de tipos de prueba y políticas
├── iso2-seguridad     · identidad, credenciales, RBAC (1 rol/persona v1), auditoría
├── iso2-agendas       · agendas de médico/técnico/sala/equipo y huecos disponibles
├── iso2-solicitudes   · prescripción, estado, seguimiento periódico
├── iso2-citas         · ciclo de vida de citas: asignación, self-service, listas de espera
├── iso2-pacientes     · altas/modificaciones/bajas e historial de pruebas
├── iso2-comunicacion  · notificaciones, recordatorios, incidencias, encuestas
├── iso2-informes      · informes clínicos e imágenes (visor)
├── iso2-integracion   · fachadas SSO/HCE/Admisión/RIS-PACS (HL7/FHIR/DICOM)
├── iso2-reporting     · estadísticas y reportes (médico, operativo, global)
│  APLICACIONES (tren de versiones de release)
├── iso2-server-app    · ensamblado Spring Boot: publica la API REST (0.x hasta v1.0.0 final)
└── iso2-client-app    · cliente JavaFX con vistas por rol (4 incrementos 1.1→1.4 + pulido 1.5)
```

**Reglas de versionado SemVer 2.0** (D22, sin cambios) + **reglas de configuración** (D25, nuevas):
- Componente de dominio: `v1.0.0` = baseline de contratos (Iter0); `v1.y.0` = incremento por CDU
  (orden de riesgo decreciente, D22/D23); `v1.y.z` = fixes intra-iteración; sin `v2.x` antes de Iter21.
- `iso2-common`: minor por release (R1→1.1.0 … R5→1.5.0). Aplicaciones: tren de release.
- **(nuevo)** Cada merge de iteración a `develop` produce la **CFG-NN** etiquetada `cfg-iterNN`:
  el conjunto exacto de versiones de esta matriz. **(nuevo)** Ninguna CFG etiquetada contiene `SNAPSHOT`.
  **(nuevo)** Toda mutación de estado lleva `@Auditable` (no repudio/responsabilidad) y toda capa
  respeta las reglas ArchUnit (modularidad MVC + SOLID) desde el primer commit del incremento.

## 2. Foco de calidad ISO 25010 por componente (D24)

Cada componente maximiza unas características concretas; la puerta de aceptación de cada incremento
incluye sus criterios de calidad verificables. Los umbrales de cobertura son de **línea JaCoCo al
final del incremento** (las ramas suben del 45 % al 70 % del plan, §8.4 del PUD +Calidad).

| Componente | Características ISO 25010 maximizadas | Criterios de calidad por incremento (verificables) | Cobertura obj. final | CFG hitos donde evoluciona |
|---|---|---|---|---|
| iso2-common | Mantenibilidad: modularidad (frontera de contratos), analizabilidad | Javadoc 100 % en puertos/DTO; ArchUnit: nadie importa implementaciones; compatibilidad binaria entre incrementos (sin romper clientes) | ≥60 % (DTO/contratos) | CFG-00 · 06 · 12 · 18 · 20 · 21 |
| iso2-catalogo | Adecuación funcional (completitud del catálogo) · Seguridad (integridad: catálogo como fuente autorizada) | TC de validación de protocolos/políticas; RBAC solo-admin escribe; referencias intactas al borrar tipo en uso | ≥80 % | CFG-01 |
| iso2-seguridad | **Seguridad** (confidencialidad, integridad, no repudio, responsabilidad) · Fiabilidad (disponibilidad del login) | RBAC deny-by-default testado por endpoint; BCrypt+JWT; auditoría con hash encadenado (1.2.0); bloqueo por intentos fallidos | ≥85 % | CFG-02 · CFG-15 |
| iso2-agendas | Fiabilidad (tolerancia a fallos: no doble reserva) · Mantenibilidad | TC de concurrencia sobre el mismo hueco; invariantes de solape de recursos; modelo multi-recurso testeable sin UI | ≥85 % | CFG-03 · CFG-12 |
| iso2-solicitudes | Adecuación funcional (corrección del flujo clínico) · Seguridad (integridad del estado) · Mantenibilidad (máquina de estados testeable) | Transiciones ilegales rechazadas y testadas; recurrencias (1.2.0) con calendario; tracking con trazas de auditoría | ≥85 % | CFG-04 · 11 · 12 · 14 |
| iso2-citas | **Fiabilidad** (tolerancia a fallos, recuperabilidad: asignación atómica) · Usabilidad (protección de errores al cancelar/reubicar) · Seguridad | TC de carrera al asignar hueco; rollback de asignación; self-service solo sobre citas propias (privacidad); reubicación de lista de espera testeable | ≥85 % | CFG-05 · 07 · 09 |
| iso2-pacientes | Seguridad (confidencialidad RGPD: datos propios) · Adecuación funcional (completitud del historial) | TC de acceso cruzado paciente A↔B denegado; borrado de paciente con datos clínicos bloqueado o anonimizado (1.2.0) | ≥80 % | CFG-08 · CFG-13 |
| iso2-comunicacion | Usabilidad (operabilidad: recordatorios a tiempo) · Fiabilidad (entrega reintentable, tolerancia a canal caído) | TC de reintento de notificación con canal fuera; no duplicación de envío (idempotencia); encuesta anónima (1.2.0) | ≥75 % | CFG-10 · CFG-18 |
| iso2-informes | Adecuación funcional (pertinencia: informe correcto para la prueba) · Seguridad (integridad y confidencialidad de imagen DICOM) · Usabilidad (visor operativo) | TC de visor con imagen de prueba; control de acceso por rol al estudio; firma/integridad del informe | ≥75 % | CFG-16 |
| iso2-integracion | **Fiabilidad** (tolerancia a fallos del externo + modo degradado) · Mantenibilidad (puerto/adaptador, sustituible) | TC con adaptador caído/lento (obligatorio desde R3); idempotencia del spike SSO/FHIR; timeouts y reintentos configurables; Trazabilidad: cada adaptador mapea a su CDU | ≥70 % (adaptadores) + TC de fallo obligatorios | CFG-06 · 08 · 16 |
| iso2-reporting | Adecuación funcional (corrección de cifras) · Usabilidad (dashboards legibles por rol) · Analizabilidad | TC de cálculo con dataset fijo conocido (oracle); totales cuadran con origen (no doble conteo) | ≥75 % | CFG-17 · 19 · 20 |
| iso2-server-app | Fiabilidad (arranque y salud de la app) · Mantenibilidad (ensamblado sin acoplamientos) | smoke test de arranque en cada CFG; `/health`; OpenAPI publicado; sin `SNAPSHOT` en CFG etiquetada | smoke + integración | CFG-00 → CFG-21 (0.1.0→1.0.0) |
| iso2-client-app | **Usabilidad** (operabilidad por rol, protección de errores: validación en vista) · Seguridad (sesión, no repudio de acciones UI) | validación de formulario en vista ANTES de llamar al servidor; menú por rol verificado (5 roles); confirmación en acciones destructivas; revisión heurística por release | lógica de presentación testeable (sin lógica de negocio en vistas); UI validada por revisión + TC de sistema | CFG-00 → CFG-21 (0.1.0→1.5.0) |

**Lectura en clave MVC + SOLID** (Mantenibilidad, pilar pedido en el enunciado): dentro de cada
módulo, dominio → servicio(puerto) → adaptador → vista; el agente solo puede escribir dentro del
módulo del CDU de la iteración (más `iso2-common` si hay contrato nuevo). ArchUnit lo verifica en CI
(reglas escritas en Iter0, antes que el código): sin importaciones cruzadas de implementación, sin
capas saltadas, dependencias hacia abstracciones de `iso2-common`.

## 3. Niveles de riesgo de desarrollo (igual que la versión base)

T1 (CDU19,12,13,06,17,16) → T2 (CDU07,14,02,11,09,01,08,18) → T3 (CDU03,05) → T4 (CDU10,15,20,04).
El orden por riesgo decreciente de los incrementos (`v1.1.0` = CDU de mayor riesgo del componente …)
coincide además con el orden de **dificultad de prueba**: los incrementos T1 llevan los tests más
exigentes (concurrencia, RBAC, adaptadores caídos) y por eso sus puertas de cobertura se fijan
cuanto antes (≥85 % en citas/solicitudes/seguridad desde que existen).

## 4. Matriz global: componente × iteración, con CFG y etiqueta de release

Versión máquina-legible: `docs/componentes-versiones+Calidad.csv` (idéntica a la base + columnas
`cfg_id`, `tag_configuracion`, `etiqueta_release`). En la matriz, `·` = versión estable heredada
(sin cambios en esa iteración); las versiones en curso van en `…-SN` hasta el merge que etiqueta CFG.

| Iter (CDU) | catalog | segur | agendas | solicit | citas | pacientes | comunic | informes | integrac | reporting | common | server | client | CFG | Release · etiqueta |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 0 (—) | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | 0.1.0 | 0.1.0 | CFG-00 | R0 `v0.1.0` interna |
| 1 (16) | **1.1.0** | · | · | · | · | · | · | · | · | · | 1.1.0-SN | · | · | CFG-01 | interna |
| 2 (17) | · | **1.1.0** | · | · | · | · | · | · | · | · | 1.1.0-SN | · | · | CFG-02 | interna |
| 3 (13) | · | · | **1.1.0** | · | · | · | · | · | · | · | 1.1.0-SN | · | · | CFG-03 | interna |
| 4 (06) | · | · | · | **1.1.0** | · | · | · | · | · | · | 1.1.0-SN | · | · | CFG-04 | interna |
| 5 (12) | · | · | · | · | **1.1.0** | · | · | · | · | · | 1.1.0-SN | · | · | CFG-05 | interna |
| 6 (19) | · | · | · | · | · | · | · | · | **1.1.0** | · | →R1: **1.1.0** | **0.2.0-a** | **1.1.0** | CFG-06 | **R1 20/11 · ALFA 1 `v0.2.0-alpha`** |
| 7 (14) | · | · | · | · | **1.2.0** | · | · | · | · | · | 1.2.0-SN | · | · | CFG-07 | alfa en curso |
| 8 (11) | · | · | · | · | · | **1.1.0** | · | · | **1.2.0** | · | 1.2.0-SN | · | · | CFG-08 | interna |
| 9 (01) | · | · | · | · | **1.3.0** | · | · | · | · | · | 1.2.0-SN | · | · | CFG-09 | interna |
| 10 (02) | · | · | · | · | · | · | **1.1.0** | · | · | · | 1.2.0-SN | · | · | CFG-10 | interna |
| 11 (07) | · | · | · | **1.2.0** | · | · | · | · | · | · | 1.2.0-SN | · | · | CFG-11 | interna |
| 12 (08) | · | · | **1.2.0** | **1.3.0** | · | · | · | · | · | · | →R2: **1.2.0** | **0.3.0** | **1.2.0** | CFG-12 | **R2 27/11 · ALFA consol. `v0.3.0-alpha`** |
| 13 (03) | · | · | · | · | · | **1.2.0** | · | · | · | · | 1.3.0-SN | · | · | CFG-13 | beta en curso |
| 14 (05) | · | · | · | **1.4.0** | · | · | · | · | · | · | 1.3.0-SN | · | · | CFG-14 | interna |
| 15 (18) | · | **1.2.0** | · | · | · | · | · | · | · | · | 1.3.0-SN | · | · | CFG-15 | interna |
| 16 (09) | · | · | · | · | · | · | · | **1.1.0** | **1.3.0** | · | 1.3.0-SN | 0.4.0-SN | 1.3.0-SN | CFG-16 | interna (beta en curso) |
| 17 (10) | · | · | · | · | · | · | · | · | · | **1.1.0** | 1.3.0-SN | 0.4.0-SN | 1.3.0-SN | CFG-17 | interna |
| 18 (04) | · | · | · | · | · | · | **1.2.0** | · | · | · | →R3: **1.3.0** | **0.4.0-b** | **1.3.0** | CFG-18 | **R3 04/12 · BETA `v0.4.0-beta`** |
| 19 (15) | · | · | · | · | · | · | · | · | · | **1.2.0** | 1.4.0-SN | 0.9.0-SN | 1.4.0-SN | CFG-19 | interna (RC en curso) |
| 20 (20) | · | · | · | · | · | · | · | · | · | **1.3.0** | →R4: **1.4.0** | **0.9.0-rc** | **1.4.0** | CFG-20 | **R4 11/12 · CANDIDATA `v0.9.0-rc`** |
| 21 (—) | · | · | · | · | · | · | · | · | · | · | **1.5.0** | **1.0.0** | **1.5.0** | CFG-21 | **R5 15/12 · DEFINITIVA `v1.0.0`** |

> La columna CFG hace explícito el **plan de gestión de configuración**: cada fila es una
> configuración mínima identificada y etiquetada (`cfg-iterNN`), de la que se puede construir,
> desplegar y hacer rollback (ver `docs/mapa-configuraciones.md` §5). Las 6 CFG hito (00, 06, 12,
> 18, 20, 21) son los releases con versión de aplicación y clasificación alfa/beta/candidata/definitiva (D26).

## 5. Esfuerzo y coste (sin cambios respecto de la versión base)

492 h en los 22 incrementos de componentes + 120 h fijas (Iter0+Iter21) + 10 % contingencia ≈
**16.530 €**. La calidad **no añade iteración ni coste**: sus actividades (diseño humano de TC,
revisión de arquitectura, puertas de CI, copia/restore) ya están dentro de las horas de cada
iteración (desglose A/D/I/T/Dg de la versión base §4) y de las 80 h de Iter21. Lo que cambia en la
versión +Calidad es **qué se considera "terminado"**: el criterio de aceptación de cada incremento
incluye ahora sus criterios de calidad verificables de la tabla §2.

## 6. Traceabilidad componente ↔ calidad ↔ configuración

| Pregunta | Documento |
|---|---|
| ¿Qué CDU desarrolla cada componente y en qué versión? | este doc §4 (= CSV +Calidad) |
| ¿Qué exigencia ISO 25010 tiene cada CDU y cómo se prueba? | `SOFTWAREQUALITY.md` §3 (20 filas CDU) |
| ¿Qué puerta de calidad ha de pasar la CFG en la que estoy? | `docs/mapa-configuraciones.md` §3 + PUD +Calidad §8.4 |
| ¿Qué versiones exactas forman la CFG-NN que despliego? | `docs/mapa-configuraciones.csv` (fila CFG-NN) |
