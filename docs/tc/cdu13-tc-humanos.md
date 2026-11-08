# TC humanos CDU13 · Gestion de agendas de medicos y tecnicos (CFG-03 / Iter3)

**Código bajo prueba:** `iso2-agendas` 1.1.0 (`develop` actual; base de la iteración:
tag `cfg-iter03` = `b072b34`). Regla de mínimos D24: 1 feliz + ≥2 error + 1 permisos.

**Puntos de entrada (RBAC desde el filtro):**

| Acción | Endpoint | Roles que pasan |
|---|---|---|
| Listar agendas | `GET /api/agendas` | ADMIN, MEDICO, TECNICO, RECEPCIONISTA |
| Alta de agenda | `POST /api/agendas` (body `{profesionalId}`) | ADMIN, RECEPCIONISTA |
| Ver agenda / huecos libres | `GET /api/agendas/{pid}` · `GET /api/agendas/{pid}/libres` | ADMIN, MEDICO, TECNICO, RECEPCIONISTA |
| Alta/baja de franja | `POST /api/agendas/{pid}/franjas` · `DELETE .../franjas/{id}` | ADMIN, RECEPCIONISTA |
| Alta/baja de festivo | `POST /api/agendas/{pid}/festivos` · `DELETE .../festivos/{id}` | ADMIN, RECEPCIONISTA |

Códigos de error observables: 400 rango inválido (inicio ≥ fin, formato) · 404
agenda/profesional inexistente · 409 `CONFLICTO_SOLAPE` · 409 `CONFLICTO_FESTIVO` ·
409 `CONFLICTO_AGENDA_EXISTENTE` · 403 rol sin permiso. Semilla `V1.3.0`: agendas de
`medico` (3 franjas) y `tecnico` con un festivo ya cargado — el TC feliz de huecos
libres puede hacerse sobre `medico` sin preparar nada.

| ID | Título | Tipo | Puerta que cubre (SQ §5) | Pasos | Resultado esperado | Resultado real | Pasa | Fecha/ejecutor |
|---|---|---|---|---|---|---|---|---|
| TC13-01 | Consultar huecos libres de la semilla | feliz | AF completitud/corrección: sin capacidad ficticia | *rellenar* (GET /api/agendas/medico/libres; comprobar que el festivo de la semilla no aparece como hueco) | | | | |
| TC13-02 | Alta de franja válida | feliz | AF corrección + SEG responsabilidad (queda atribuida en auditoría) | *rellenar* (POST franja sin solape como recepcionista; ver la nueva franja en GET) | | | | |
| TC13-03 | Franja solapada con otra | error | AF corrección: imposible solapar — pista SQ: *TC solapes* | *rellenar* (POST franja que pisa una existente; y franja que empieza antes de terminar la anterior) | | | | |
| TC13-04 | "El festivo manda" (en los dos sentidos) | error | AF corrección: festivos sobre las franjas — pista SQ: *TC festivos* | *rellenar* (a) POST franja en día festivo; (b) POST festivo en día con franja ya creada) | | | | |
| TC13-05 | Segunda agenda para el mismo profesional | error | SEG integridad: una agenda por profesional | *rellenar* (POST /api/agendas con `medico`, que ya tiene agenda de la semilla) | | | | |
| TC13-06 | Franja con rango inválido | error | AF corrección: validación | *rellenar* (inicio == fin, inicio > fin, hora mal formada) | | | | |
| TC13-07 | Roles sin permiso de agenda | permisos | MAN modularidad + matriz RBAC (`agenda:leer`/`agenda:escribir`) | *rellenar* (a) `celador` → GET /api/agendas; (b) `medico` → POST /api/agendas/x/franjas; (c) `recepcionista` → ¿POST franja permitida?) | | | | |
| TC13-08 | Auditoría encadenada de la agenda | feliz (auditoría) | SEG responsabilidad: la creación queda atribuida | *rellenar* (tras alta de franja/festivo, revisar `agenda_auditoria`: actor, acción, hash encadenado con la anterior) | | | | |

## Resultado global de la tabla

- [ ] Mínimos D24 cubiertos (1 feliz + ≥2 error + 1 permisos)
- [ ] Todos los TC ejecutados → CFG-03 **cerrada** (marcar en `STATUS.md`)
