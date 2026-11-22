# Notas de trabajo del ejecutor TDOP de CDU14

Volcados **derivados** de `../tdop-cdu14-espec.json` y de los CSV de `../`, usados para escribir
`iso2-citas/src/test/java/es/uclm/iso2/citas/tdop/Cdu14MatrizTdopTest.java` sin releer la spec
entera (105 KB) cada vez. **No son fuente de verdad**: la fuente es la spec JSON y los CSV, y solo
se regeran con `python tools/pairwise.py docs/test/iter07/tdop-cdu14-espec.json --resources
iso2-citas/src/test/resources/tdop` (**D33**). Si algún fichero de esta carpeta contradice a la
spec, gana la spec.

| Fichero | Contenido |
| --- | --- |
| `deg.txt` | Las tablas D (27 filas de reubicación), E (12 de secuencias), G (12 de auditoría) y C (30 de operaciones) en formato `clave:valor`, columna por columna |
| `F64.txt` | Las 64 filas de la tabla F (*pairwise* de `asignarDesdeCola`) compactas: `esperaId`, `estadoEntrada`, `profesional`, `inicio`, `duracion`, `hueco`, `solicitud`, `setup` y los dos esperados |
| `specF_reglas.txt` | Las 18 reglas de la tabla F, en el orden en que las valida el código, con estado y código esperados y el porqué |
| `specF_fac.txt` | Los niveles de los factores F (`profesional`, `inicio`, `duracion`, `hueco`, `solicitud`) con su literal real y su `setupPor` |
| `specH.txt` | La tabla H (13 filas de conjetura de errores) con la leyenda de tokens y las `_notas` |
| `specG.txt` | La tabla G con `leyendaTokens` y las `_notas` de la auditoría encadenada |
