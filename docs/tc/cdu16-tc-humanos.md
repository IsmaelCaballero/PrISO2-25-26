# TC humanos CDU16 · Configurar parámetros (CFG-01 / Iter1)

**Código bajo prueba:** `iso2-catalogo` 1.1.0 (`develop` actual; base de la iteración:
tag `cfg-iter01` = `df09bca`). Regla de mínimos D24: 1 feliz + ≥2 error + 1 permisos.

**Puntos de entrada:** `POST/GET /api/catalogo/pruebas`, `GET/PUT/DELETE
/api/catalogo/pruebas/{codigo}`, `PUT /api/catalogo/pruebas/{codigo}/reactivar`
(400 validación · 404 inexistente · 409 código duplicado) o la pantalla de catálogo
del cliente JavaFX. **RBAC aplicado desde Iter2 sobre estos endpoints: la escritura
(POST/PUT/DELETE/reactivar) es solo para `ADMINISTRADOR`.** Auditoría encadenada en
`catalogo_auditoria` (actor + acción + hash encadenado), visible en H2.

**Preparación:** arrancar servidor con `$env:ISO2_JWT_SECRET` (ver `docs/tc/README.md`)
y, para los TC con token, login previo en `POST /api/auth/login` con `admin` /
`iso2-admin`.

| ID | Título | Tipo | Puerta que cubre (SQ §5) | Pasos | Resultado esperado | Resultado real | Pasa | Fecha/ejecutor |
|---|---|---|---|---|---|---|---|---|
| TC16-01 | Alta de prueba y lectura en catálogo | feliz | AF completitud/pertinencia: catálogo completo | *rellenar* (alta con payload válido; luego GET lista + GET por código) | | | | |
| TC16-02 | Código de prueba duplicado | error | SEG integridad: catálogo único (código) — pista SQ: *TC unicidad de código* | *rellenar* (POST repitiendo un código ya existente) | | | | |
| TC16-03 | Borrar una prueba en uso | error | SEG integridad: una prueba en uso no se borra — pista SQ: *TC borrar en uso → prohibido* | *rellenar* (DELETE de una prueba con citas/agendas asociadas; comprobar baja lógica y reactivación) | | | | |
| TC16-04 | Payload inválido en alta/edición | error | AF corrección: validación de campos obligatorios | *rellenar* (p. ej. `duracionMinutos: 0` o campos vacíos; body no coincide con la ruta en PUT) | | | | |
| TC16-05 | Auditoría de cada cambio | feliz (auditoría) | SEG responsabilidad: todo cambio queda auditado — pista SQ: *TC auditoría de cambios* | *rellenar* (tras alta/edición/baja, revisar `catalogo_auditoria`: actor, acción, hash encadenado) | | | | |
| TC16-06 | Escritura de catálogo sin ser ADMIN | permisos | SEG protección + matriz RBAC (escritura solo `ADMINISTRADOR`) | *rellenar* (POST/PUT/DELETE con token de `medico` → y repetir con `admin`) | | | | |

## Resultado global de la tabla

- [ ] Mínimos D24 cubiertos (1 feliz + ≥2 error + 1 permisos)
- [ ] Todos los TC ejecutados → CFG-01 **cerrada** (marcar en `STATUS.md`)
