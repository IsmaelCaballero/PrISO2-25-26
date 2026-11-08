# TC humanos CDU17 · Gestion de seguridad y permisos (CFG-02 / Iter2)

**Código bajo prueba:** `iso2-seguridad` 1.1.0 (`develop` actual; base de la iteración:
tag `cfg-iter02` = `870524e`). Regla de mínimos D24: 1 feliz + ≥2 error + 1 permisos.

**Puntos de entrada:** `POST /api/auth/login` (JSON `{"usuario":..., "password":...}`
→ `SesionDTO` con JWT y expiración) y cualquier endpoint protegido con cabecera
`Authorization: Bearer <token>`. Datos fijos del diseño: BCrypt coste 10, JWT HS256
firmado con `ISO2_JWT_SECRET` y caducidad por defecto 60 min
(`iso2.seguridad.jwt.duracion-minutos`), bloqueo temporal tras **5** intentos fallidos
durante **15** min. Tokens de error: 401 `CREDENCIALES_INVALIDAS` / token inválido o
caducado · 401 `CUENTA_BLOQUEADA` (el bloqueo devuelve 401 con ese código) · 403 rol
sin permiso · 400 `DATOS_INVALIDOS`.

**Truco para el TC de token caducado:** arrancar el servidor con
`--iso2.seguridad.jwt.duracion-minutos=1`, hacer login y esperar ~70 s antes de llamar.

**Usuarios semilla:** ver tabla en `docs/tc/README.md` (incluida `bloqueable` /
`iso2-bloq` para el TC de bloqueo).

| ID | Título | Tipo | Puerta que cubre (SQ §5) | Pasos | Resultado esperado | Resultado real | Pasa | Fecha/ejecutor |
|---|---|---|---|---|---|---|---|---|
| TC17-01 | Login correcto y uso del token | feliz | SEG integridad/autenticación + AF corrección | *rellenar* (login `medico`, GET /api/agendas/medico con Bearer) | | | | |
| TC17-02 | Credenciales incorrectas | error | SEG no repudio/autenticación: denegación sin filtrar usuario vs contraseña | *rellenar* (usuario correcto + mal password; y usuario inexistente) | | | | |
| TC17-03 | Bloqueo por intentos fallidos | error | SEG protección: bloqueo por intentos — pista SQ | *rellenar* (6 intentos fallidos con `bloqueable`; esperar levantamiento del bloqueo) | | | | |
| TC17-04 | Token caducado o adulterado | error | SEG protección + JWT firmado y caducable — pista SQ: *TC token caducado* | *rellenar* (token tras expirar; token con un carácter cambiado de sitio; token firmado con otra clave) | | | | |
| TC17-05 | 403 por endpoint: un acceso por rol no autorizado | permisos | SEG protección: deny-by-default en TODOS los endpoints — pista SQ: *TC 403 por endpoint* | *rellenar* (un par por endpoint protegido: p. ej. `celador` → GET /api/agendas; `medico` → POST /api/catalogo/pruebas; `recepcionista` → ¿GET catálogo?) | | | | |
| TC17-06 | Mantiene la matriz rol→permiso | permisos | SEG responsabilidad: la matriz rol→permiso es verificable — pista SQ: *test parametrizado de matriz RBAC* | *rellenar* (recorrer matriz: 5 usuarios × {catálogo lectura, catálogo escritura, agenda lectura, agenda escritura}; anotarlo en la columna; si algún par no coincide con `MatrizPermisos`, marcar `no`) | | | | |

## Resultado global de la tabla

- [ ] Mínimos D24 cubiertos (1 feliz + ≥2 error + 1 permisos)
- [ ] Todos los TC ejecutados → CFG-02 **cerrada** (marcar en `STATUS.md`)
