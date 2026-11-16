# Borrador del ejecutor de la matriz CDU14 (NO compila, NO esta en el build)

Estos dos ficheros son un borrador a medias de `Cdu14MatrizTdopTest`, el ejecutor 1:1 de
los nueve CSVs de `docs/test/iter07/` (253 filas, spec canonica `tdop-cdu14-espec.json`,
D33). Estan aqui para conservar el trabajo hecho, **no** para compilarse: la puerta CFG-07
de la matriz TDOP sigue **abierta** hasta que el ejecutor este completo y en verde sobre
la rama `feature/iter07-cdu14-listas-espera`.

Lo que hay:

- `Cdu14MatrizTdopTest-parte1.txt`: cabecera, mundo limpio por fila, lectura de matrices
  y de la tabla de esperados.
- `Cdu14MatrizTdopTest-parte2.txt`: esqueleto de las tablas de alta (cdu14-alta.csv) y de
  cola (cdu14-cola.csv).

Lo que falta (y por que no vale la pena terminarlo a medias):

- Ayudas centrales: `aplicarSetup` (semillas `SEMILLA-COLA-*` y `SEM-TAMPER:*` por API),
  `esId`, `verificar` (esperado simbolico -> status + codigo desde `cdu14-esperados.csv`).
- Tablas D (reubicacion), E (doble clic y concurrencia con `@Version`), F (asignar,
  64 filas), G (auditoria y anti-manipulacion) y H (conjetura).
- Revisar el sembrado multi-paciente del borrador: el canon del curso usa un unico
  paciente (`api-paciente`) y la clave duplicada se dispara cambiando el codigo de prueba
  (`api-prueba-dup`, ya anadido a `PruebasCitas`).

Regla D33: si un caso sale rojo, se corrige el codigo o la spec JSON y se regeneran los
CSVs con `python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources
iso2-citas/src/test/resources/tdop`; nunca se editan los CSVs ni el ejecutor a mano.
