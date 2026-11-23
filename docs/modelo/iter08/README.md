# Modelo de Iter8 / CDU11 (Dar de alta, modificar y dar de baja pacientes)

Modelo de la Iteracion 8: **CDU11 · Dar de alta, modificar y dar de baja pacientes**
(issue #50, CFG-08). Cuatro diagramas `cdu11-*.puml` (casos de uso, clases de analisis, clases
de diseno y secuencia), commiteados **antes** de escribir codigo Java de CDU11
(design-primero, D31).

El CDU11 llena el unico modulo hueco del arbol: `iso2-pacientes` estaba en baseline 1.0.0 con
dos archivos y nada detras. Anade al sistema el **alta demografica del paciente** con
unicidad por tres vias (identificador, NIF e historia clinica), la **modificacion parcial**
de datos, la **baja logica terminal** (motivo + confirmacion) y la **reconciliacion** de la
historia clinica contra el HCE externo, consumiendo el contrato `InteropFacade` que la
Iter6 (`iso2-integracion`, CFG-06) dejo publicado en `iso2-common`.

Lo delicado del CDU11 no es el CRUD (facil): es **la unicidad bajo dos hilos** (la
comprobacion "soft" del servicio no cierra la carrera; el UNIQUE de la base de datos si),
**que el alta nunca dependa del sistema externo** (HCE caido = 201 con
`PENDIENTE_SINCRONIZAR`, jamas 5xx: leccion DEF-05 de la Iter6) y **que la baja sea de
verdad terminal** (modificar, rebajar o reconciliar un dado de baja es 409, consultar es
200 con estado `BAJA_LOGICA`).

## Como ver los diagramas

Cuatro fuentes `.puml` renderizadas a `.png` con PlantUML local:

```
java -DPLANTUML_LIMIT_SIZE=16384 -jar tools/plantuml.jar -tpng docs/modelo/iter08/cdu11-*.puml
```

(Graphviz no esta instalado: si un diagrama de clases no layoutea, anadir `-Playout smetana`.)

| Archivo | Que cuenta |
|---|---|
| `cdu11-casos-uso.puml` | Los siete casos de uso con sus roles (leer: ADMIN/MEDICO/RECEPCIONISTA; escribir: ADMIN/RECEPCIONISTA), el include de autenticacion y los tres puntos criticos anotados: unicidad, baja logica y reconciliacion idempotente. |
| `cdu11-clases-analisis.puml` | Conceptos (Paciente, RegistroAuditoria, ConciliacionHCE) y las seis reglas del dominio R1-R6 que la matriz TDOP ataca una por una. |
| `cdu11-clases-diseno.puml` | Paquetes y firmas: el contrato nuevo `GestionPacienteFacade` en `iso2-common` junto a la baseline `PacienteFacade` **intacta** (D22), la entidad con `@Version` desde el primer commit (DEF-17), el advice con sus codigos estables y el hueco Flyway V1.7.0. |
| `cdu11-secuencia.puml` | Las cuatro escenas que pueden fallar y como fallan bien: alta con HCE caido, carrera de alta por doble clic, baja + terminalidad y edicion concurrente por `@Version`. |

## Las tres capas anti-duplicado (lo que prueba el titulo de la iteracion)

1. **Soft check** en `PacienteServicio` (`existsByNif`/`existsByHistoriaClinica`): da el
   error bonito (409 `CONFLICTO_NIF_DUPLICADO`...) pero **no** cierra la carrera.
2. **UNIQUE de la base de datos** sobre `paciente_id`, `nif` e `historia_clinica`: la
   garantia real. Cuando dos hilos pasan el soft check, el perdedor del INSERT recibe
   `DataIntegrityViolationException` -> 409 generico `CONFLICTO_PACIENTE_DUPLICADO`.
3. **`@Version` en `Paciente` desde el primer commit** (leccion DEF-17: en CDU14 hubo que
   anadirlo despues a mano): dos ediciones concurrentes de la misma ficha ->
   `ObjectOptimisticLockingFailureException` -> 409 `CONFLICTO_MODIFICACION_CONCURRENT`.

La matriz TDOP (tablas A-G de `docs/test/iter08/tdop-cdu11-espec.json`) ataca las tres
capas desde HTTP con dos hilos reales.

## Decisiones de diseno que fija este modelo

- **`iso2-integracion` no se toca** (decision del issue): CDU11 solo consume
  `InteropFacade.sincronizarHistoriaClinica` desde `iso2-common`. En el TDOP del modulo el
  bean es un **falso** del test con tokens `SEM-HCE-NORMAL`/`SEM-HCE-CAIDO`; en server-app
  lo aporta `iso2-integracion` de verdad y el modo se conmuta con `SIM-*` (Iter6).
- **Baseline sin romper** (D22): `PacienteFacade`/`PacienteDTO` quedan intactos;
  `PacienteServicio` los **implementa** (buscar por HCE + upsert desde HIS), que es justo la
  prueba del contrato que faltaba. El contrato nuevo es `GestionPacienteFacade` (extension
  MINOR, D23).
- **Baja logica**: la fila sigue en la BD; `GET /api/pacientes/{id}` responde 200 con
  estado `BAJA_LOGICA`, fecha y motivo. No existe DELETE fisico.
- **POST para todo lo que es accion** (`/datos`, `/baja`, `/reconciliar`): sin PUT/PATCH,
  mismo criterio que CDU14.
- **Auditoria con genesis propio** en `paciente_auditoria` (patron hash-chained duplicado
  por modulo, como `espera_auditoria`), canon `accion|pacienteId|antes->despues|usuario|motivo|instante|hashAnterior`
  y semillas con hash precalculado (`tools/cdu11_seed_hashes.py`, espejo de la herramienta de CDU14).
- **RBAC desde el primer endpoint** (leccion TC14-08): `paciente:leer`/`paciente:escribir`
  en la matriz + reglas en `ConfigSeguridad` + **test RBAC automatizado en server-app**
  (`PacientesApiTest`, patron `CitasApiTest`). En CDU14 esto se demoro y quedo como deuda
  TC14-08; aqui es DoD.

## Trazabilidad

Issue [#50](https://github.com/IsmaelCaballero/PrISO2-25-26/issues/50). Este modelo es el
DoD parcial "modelo commiteado antes del codigo" (D31). despues vendran: spec TDOP
(`docs/test/iter08/`) -> CSV generados por `tools/pairwise.py` (D33, intocables a mano) ->
ejecutor `Cdu11MatrizTdopTest` -> codigo Java -> proyeccion de TC humanos `docs/tc/cdu11-tc-humanos.md`.
