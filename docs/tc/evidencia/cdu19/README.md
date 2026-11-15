# Evidencia de los TC humanos de la CDU19 (Iter6 / CFG-06)

Ejecutando: agente (no Ismael), el **15/11/2026** sobre el servidor real. Esto **no sustituye la
validación humana de D28**: Ismael tiene que revisar y dar el visto bueno de los TC humanos. Lo
que hay aquí es el rastro en bruto de la pasada que hizo el agente para poder documentar el
`Resultado real` de `docs/tc/cdu19-tc-humanos.md`.

## Cómo se generó (reproducible)

```powershell
# 1) arrancar el servidor real (H2 en fichero ./data/iso2-server)
$env:ISO2_JWT_SECRET = "<secreto de pruebas de >= 32 bytes>"
mvn -B -ntp spring-boot:run -pl iso2-server-app

# 2) login ISO2 (el unico rol que pasa en integracion es ADMINISTRADOR)
#    POST /api/auth/login {"usuario":"admin","password":"iso2-admin"}

# 3) ejecutar los TC y los sondeos contra http://localhost:8080
python tc19_http.py          # TC19-01..TC19-09 -> registro-cdu19.json
python tc19_cita.py          # cita con integracion caida + huecos -> registro-citas-y-huecos.json
python tc19_sonda_rutas.py   # rutas sin mapear (con y sin token) -> sondeo-rutas-sin-mapear.json
python tc19_sso_sonda.py     # SSO con la clave correcta (contrasena) -> sondeo-sso-contrasena.json
python tc19_auditoria.py     # cadena de auditoria entera (TC19-02 y TC19-06) -> auditoria-cdu19.json
```

Cada registro guarda `metodo`, `ruta`, `status`, `ms` (medido en el cliente) y `cuerpo`. En los
registros generales el cuerpo va **cortado a 700 caracteres** para que el rastro quepa en el repo;
en `auditoria-cdu19.json` no va cortado, porque ahí hace falta ver el eslabón último y el
encadenado `hashAnterior`→`hash`. **Los JWT de las respuestas de `/api/auth/login` se quitaron
antes de versionar** (`evidencia_cdu19.py`): el repo no debe guardar tokens de sesión.

## Qué deja el servidor

Al terminar, la simulación vuelve a `NORMAL` en los cuatro sistemas y la cita creada en el hueco
libre del médico (`2026-11-13T09:00`) queda cancelada. Las semillas de integración se reinician
con el servidor porque no hay migración Flyway del CDU19.

## Qué sale de aquí (hallazgos, van a `STATUS.md` como deuda)

| Evidencia | Fichero | Qué se ve |
|---|---|---|
| TC19-07 con HCE caído y copia local | `registro-cdu19.json` | 503 `SISTEMA_NO_DISPONIBLE`: **D-01 confirmada en servidor real** |
| SSO con `password` frente a `contrasena` | `registro-cdu19.json` y `sondeo-sso-contrasena.json` | 400 `DATOS_INVALIDOS` frente a 200/401: **deuda D-08 (contratos distintos en el mismo módulo)** |
| Rutas sin mapear con token válido | `sondeo-rutas-sin-mapear.json` | **401 `AUTENTICACION_REQUERIDA` con cuerpo `ApiError`** en los 7 casos sondeados: los 404/405 sin cuerpo solo ocurren en MockMvc (**deuda D-09**: falta una prueba de integración con la cadena de seguridad) |
| `GET /api/citas/huecos` con `desde` en formato fecha-hora | `sondeo-rutas-sin-mapear.json` | **500 `ERROR_INTERNO`**, igual que afirman TC12-C04/TC12-D03 de la matriz de CDU12 (**deuda D-07**: falta `@ExceptionHandler` de `MethodArgumentTypeMismatchException`) |
| Huecos libres con parámetros correctos | `sondeo-rutas-sin-mapear.json` | 200 con los huecos del médico del 13/11 (08:00-08:30, 09:00-10:00, 10:30-15:00) |
