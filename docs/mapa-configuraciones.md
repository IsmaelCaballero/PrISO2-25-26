# Mapa de configuraciones mínimas y plan de liberaciones (alfa · beta · definitiva)

**Versión:** 1.0.0 (07/11/2026) · **Rama:** `releaseplan` · **Decisiones:** D25 (ítems y etiquetado), D26 (mapa CFG), D27 (puertas por release)
**Documentos vinculados:** `docs/componentes-versionado+Calidad.md`, `docs/planificacion-PUD+Calidad.md`, `SOFTWAREQUALITY.md`

---

## 1. Qué llamamos «configuración» en este proyecto

**Configuración = el conjunto exacto de ítems de configuración con su versión que construye y
etiqueta una iteración.** En Maven eso es verificable: el POM agregador fija la versión de cada
módulo, así que una configuración es **reproducible** (`mvn clean verify` sobre el tag devuelve el
mismo sistema).

Ítems de configuración (todo versionado en Git, nada fuera):

| Tipo | Ítem | Naming |
|---|---|---|
| Código | 14 módulos Maven (`iso2-parent` + `iso2-common` + 10 dominio + 2 apps) | SemVer 2.0 en el POM |
| Contratos | interfaces/DTO de `iso2-common` | congelados en Iter0, suben *minor* por release |
| Datos | scripts `db/migration/V<version>__<cdu>.sql` por módulo (H2) | la versión del módulo en el nombre |
| Pruebas | suites JUnit 5 + informes JaCoCo/Surefire | acompañan al módulo |
| Modelo | diagramas PlantUML→StarUML por CDU (CU, secuencia, análisis, diseño) | `docs/modelo/iterNN/` |
| Documentación | planes, `DECISIONS.md`, `PLAN.md`, `STATUS.md`, `SOFTWAREQUALITY.md`, manual | — |
| Build | CI GitHub Actions, reglas Checkstyle/SpotBugs/ArchUnit | `.github/workflows/` |

**Identificador de configuración:** `CFG-NN` (NN = iteración) → **tag Git `cfg-iterNN`** en el merge
de la iteración a `develop`. Es el punto de partida del desplegable y del rollback.

---

## 2. Mapa de configuraciones (una por iteración)

Componentes sin cambios conservan la versión de la configuración anterior (la columna solo lista lo
que **cambia**). Los `SNAPSHOT` indican que la app aún no es etiquetable.

| CFG | Iter · CDU | Componentes que cambian (nueva versión) | Configuración resultante (componentes · versiones) | Apps | Etiqueta / tag | Clasificación |
|---|---|---|---|---|---|---|
| **CFG-00** | 0 · (arquitectura) | parent, common, apps + baseline 1.0.0 de los 10 módulos | 10 dominio 1.0.0 · common 1.0.0 | 0.1.0 | `v0.1.0` `cfg-iter00` | interna (R0, línea base) |
| **CFG-01** | 1 · CDU16 | iso2-catalogo **1.1.0** | catalogo 1.1.0 · resto 1.0.0 | 0.1.1-SN | `cfg-iter01` | interna |
| **CFG-02** | 2 · CDU17 | iso2-seguridad **1.1.0** | + seguridad 1.1.0 | 0.1.2-SN | `cfg-iter02` | interna |
| **CFG-03** | 3 · CDU13 | iso2-agendas **1.1.0** | + agendas 1.1.0 | 0.1.3-SN | `cfg-iter03` | interna |
| **CFG-04** | 4 · CDU06 | iso2-solicitudes **1.1.0** | + solicitudes 1.1.0 | 0.1.4-SN | `cfg-iter04` | interna |
| **CFG-05** | 5 · CDU12 | iso2-citas **1.1.0** | + citas 1.1.0 | 0.1.5-SN | `cfg-iter05` | interna |
| **CFG-06** | 6 · CDU19 | iso2-integracion **1.1.0** · common **1.1.0** | catalogo 1.1.0 · seguridad 1.1.0 · agendas 1.1.0 · solicitudes 1.1.0 · citas 1.1.0 · integracion 1.1.0 | **0.2.0-alpha** · client 1.1.0 | `v0.2.0-alpha` `cfg-iter06` | 🅐 **ALFA 1** (R1, 20/11) |
| **CFG-07** | 7 · CDU14 | iso2-citas **1.2.0** | citas 1.2.0 | 0.3.0-SN | `cfg-iter07` | interna (alfa en curso) |
| **CFG-08** | 8 · CDU11 | iso2-pacientes **1.1.0** · iso2-integracion **1.2.0** | + pacientes 1.1.0 · integracion 1.2.0 | 0.3.0-SN | `cfg-iter08` | interna (alfa en curso) |
| **CFG-09** | 9 · CDU01 | iso2-citas **1.3.0** | citas 1.3.0 | 0.3.0-SN | `cfg-iter09` | interna (alfa en curso) |
| **CFG-10** | 10 · CDU02 | iso2-comunicacion **1.1.0** | + comunicacion 1.1.0 | 0.3.0-SN | `cfg-iter10` | interna (alfa en curso) |
| **CFG-11** | 11 · CDU07 | iso2-solicitudes **1.2.0** | solicitudes 1.2.0 | 0.3.0-SN | `cfg-iter11` | interna (alfa en curso) |
| **CFG-12** | 12 · CDU08 | iso2-solicitudes **1.3.0** · iso2-agendas **1.2.0** · common **1.2.0** | solicitudes 1.3.0 · agendas 1.2.0 | **0.3.0-alpha** · client 1.2.0 | `v0.3.0-alpha` `cfg-iter12` | 🅐 **ALFA 2 / alfa consolidada** (R2, 27/11) |
| **CFG-13** | 13 · CDU03 | iso2-pacientes **1.2.0** | pacientes 1.2.0 | 0.4.0-SN | `cfg-iter13` | interna (beta en curso) |
| **CFG-14** | 14 · CDU05 | iso2-solicitudes **1.4.0** | solicitudes 1.4.0 | 0.4.0-SN | `cfg-iter14` | interna (beta en curso) |
| **CFG-15** | 15 · CDU18 | iso2-seguridad **1.2.0** | + auditoría 1.2.0 | 0.4.0-SN | `cfg-iter15` | interna (beta en curso) |
| **CFG-16** | 16 · CDU09 | iso2-informes **1.1.0** · iso2-integracion **1.3.0** | + informes 1.1.0 · integracion 1.3.0 (DICOM) | 0.4.0-SN | `cfg-iter16` | interna (beta en curso) |
| **CFG-17** | 17 · CDU10 | iso2-reporting **1.1.0** | + reporting 1.1.0 | 0.4.0-SN | `cfg-iter17` | interna (beta en curso) |
| **CFG-18** | 18 · CDU04 | iso2-comunicacion **1.2.0** · common **1.3.0** | comunicacion 1.2.0 | **0.4.0-beta** · client 1.3.0 | `v0.4.0-beta` `cfg-iter18` | 👻 **BETA** (R3, 04/12) |
| **CFG-19** | 19 · CDU15 | iso2-reporting **1.2.0** | reporting 1.2.0 | 0.9.0-SN | `cfg-iter19` | interna (RC en curso) |
| **CFG-20** | 20 · CDU20 | iso2-reporting **1.3.0** · common **1.4.0** | reporting 1.3.0 | **0.9.0-rc** · client 1.4.0 | `v0.9.0-rc` `cfg-iter20` | 🏁 **CANDIDATA** (R4, 11/12) |
| **CFG-21** | 21 · (transición) | common **1.5.0** · integración y endurecimiento | todos los componentes anteriores congelados | **1.0.0** · client **1.5.0** | `v1.0.0` `cfg-iter21` + `RELEASE-1.0.0` | ✅ **DEFINITIVA** (R5, 15/12) |

Lectura del mapa: **la primera configuración alfa existe el 20/11 (CFG-06), la primera beta el
04/12 (CFG-18) y la definitiva el 15/12 (CFG-21)**. Entre medias, cada CFG es desplegable y
ensayable (todo merge a `develop` deja el sistema construido y probado), pero no es etiquetable
como release mientras no cumpla los criterios de la sección 3.

Máquinas legibles: `docs/componentes-versiones+Calidad.csv` (matriz componente × iteración con
CFG y etiqueta) y `docs/mapa-configuraciones.csv` (CFG → tag, clasificación y puerta de calidad).

---

## 3. Criterios para anticipar alfa, beta y definitiva

Cada escalón se declara **por criterios cumplidos**, no por fecha; la fecha prevista es la que da
la agenda. Si una CFG no cumple los criterios, la etiqueta se retrasa y se registra en `STATUS.md`.

### 🅐 Alfa (CFG-06 = 20/11 y CFG-12 = 27/11)
*Objetivo: que el riesgo de arquitectura y el flujo transaccional estén validados ejecutando de punta a punta.*

- CFG-06 (**alfa 1, núcleo**): login con rol (CDU17) → catálogo (CDU16) → agenda (CDU13) →
  solicitud (CDU06) → cita en hueco (CDU12) con spike de integración (CDU19) funcionando contra
  un sistema externo simulado. Server+client reales, H2 en fichero.
- CFG-12 (**alfa consolidada**): añade alta de paciente (CDU11), listas de espera (CDU14),
  self-service del paciente (CDU01), notificaciones (CDU02), recurrencias (CDU07) y tracking
  (CDU08) ⇒ el **4 de 5 roles** operan de punta a punta su tarea principal.
- Puertas: JaCoCo línea ≥60 % (≥70 % en la 2ª alfa), 0 fallos Surefire, TC RBAC en el 100 % de
  endpoints, TC de concurrencia del hueco en verde, ArchUnit 0 violaciones, `@Auditable` en toda
  mutación, diagramas y Javadoc del CDU.

### 👻 Beta (CFG-18 = 04/12)
*Objetivo: todo el alcance clínico y de imagen visible y usable por los 5 roles; solo quedan reportes de administración.*

- Contenido: + historial del paciente (CDU03), revisiones (CDU05), **auditoría** (CDU18),
  informes + visor DICOM (CDU09), estadísticas del médico (CDU10), incidencias/encuestas (CDU04).
- Puertas: JaCoCo ≥75 %, TC de **fallo de adaptador externo** obligatorio (modo degradado),
  prueba de integridad del registro de auditoría (hash encadenado), ensayo de copia/restore H2,
  revisión de usabilidad de los 5 roles, 0 defectos críticos/altos abiertos.
- Consecuencia: a partir de `v0.4.0-beta` **no se añade funcionalidad nueva** salvo correcciones y
  los CDU15/CDU20 planificados (reporting), y cada corrección se versiona `0.4.x`.

### 🏁 Candidata (CFG-20 = 11/12)
- Reach de los 20 CDU: + informes operativos (CDU15) y estadísticas globales (CDU20).
- **Congelación de código** (solo defectos), JaCoCo ≥78 %, trazabilidad RF→CU→TC 20/20,
  changelog completo, plan y ejecución de pruebas de aceptación, manual en marcha.

### ✅ Definitiva (CFG-21 = 15/12)
- Pruebas de sistema con `iso2-server-app 1.0.0` + `iso2-client-app 1.5.0` reales,
  JaCoCo ≥80 % (≥85 % en citas/solicitudes/seguridad), copia/restore ensayado y documentado,
  OWASP dependency-check sin críticos, usabilidad de los 5 roles revisada, manual y wiki,
  instalador del cliente (`jpackage`) y guía de arranque del servidor.
- SemVer: `1.0.0` = contratos congelados y API estable; las `major` no aparecen antes de Iter21 (D22).

---

## 4. Ramas, etiquetado y flujo de una configuración

```
main ──────●───────────●─────────●────────●──────  solo releases etiquetadas
           v0.1.0   v0.2.0-alpha v0.4.0-beta v1.0.0      (merge --no-ff desde develop)
develop ─●───●───●───●───●───●───●───●───●───●──  merges de iteración, tag cfg-iterNN
        c00  c01  c02  c03 ...              c21
feature/CDUxx-<nombre> ──●──●──PR──▶ develop        (1 rama por CDU, Git Flow + PR obligatorio)
hotfix/… ────────────────────────────▶ main + backport a develop   (0.4.1, 1.0.1)
release/Rn ───────────────────────────▶ estabilización y etiquetado de la CFG hito
```

- Cada CDU → rama `feature/CDUxx-…` desde `develop`; PR con el issue enlazado, revisión cruzada
  (otra pareja) y CI verde; **el merge solo lo aprueba el responsable del repo** (regla del curso).
- Merge de iteración → tag **anotado** `cfg-iterNN` (identifica la configuración completa).
- CFG hito → rama temporal `release/Rn`, ajuste de versiones de app en el POM agregador, tag
  `v<versión>` y merge `--no-ff` a `main`; luego backport de los fixes a `develop`.
- **Rollback:** `git checkout cfg-iterNN` + `mvn clean verify` + desplegar ese par de artefactos;
  los datos se recuperan con la copia H2 asociada a la CFG (cada release guarda `backup/`).
- Ningún `SNAPSHOT` en una CFG etiquetada: antes de un tag de release las dependencias del
  agregador apuntan a versiones fijadas (plugin `versions-maven-plugin`, `versions:set`).

---

## 5. Ambientes y plan de despliegue (coherente con las etiquetas)

| Ambiente | Base de datos | Qué configuración se despliega | Quién lo usa |
|---|---|---|---|
| `dev` | H2 en memoria + `data.sql` de datos semilla | CFG en curso (SNAPSHOT) | la pareja que itera |
| `test` | H2 en fichero + scripts `db/migration` | CFG-NN recién etiquetada | CI y revisor de la otra pareja |
| `alfa` | H2 en fichero + copia diaria | `v0.2.0-alpha`, `v0.3.0-alpha` | equipo completo: demo interna y pruebas de aceptación parciales |
| `beta` | H2 en fichero + copia diaria + restore ensayado | `v0.4.0-beta` | simulación de escenario SESCAM con los 5 roles |
| `demo/producción académica` | H2 en fichero + copia + restore verificado | `v0.9.0-rc` y `v1.0.0` | entrega y demostración final (15/12) |

Despliegue: `mvn -Pprod clean verify` en el tag → `java -jar iso2-server-app/target/*.jar`
(profile `prod`: H2 en fichero, JWT con secreto externo, logs en fichero) + cliente empaquetado
`iso2-client-app-1.5.0.msi/jar`. Cada release lleva `DEPLOY-<versión>.md` con la versión exacta
de cada componente de la configuración (salida de `mvn dependency:list`) — es la **lista de
verificación de la configuración** exigible en la memoria.
