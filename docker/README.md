# Base de datos de desarrollo (D34 · Iter5)

MySQL 8.4 en Docker con phpMyAdmin, para el perfil `mysql` del servidor
(`iso2-server-app`). La base por defecto del proyecto sigue siendo H2 en fichero
(perfil por defecto); esto es la configuracion MySQL prometida en la D34.

## Arrancar / parar

```powershell
cd docker
docker compose up -d      # levanta MySQL (puerto 3307) y phpMyAdmin (http://localhost:8081)
docker compose ps         # estado + healthcheck
docker compose down       # para; el volumen iso2-mysql-datos conserva los datos
docker compose down -v    # para Y BORRA los datos (vuelve a sembrar Flyway al arrancar)
```

`init/01-usuarios.sql` solo se ejecuta la primera vez que se crea el volumen.
Si se cambia algo en el script, hay que hacer `docker compose down -v` para que
vuelva a correr.

## Usuarios y privilegios (D34)

| Usuario | Contra (desarrollo) | Que puede | Quien lo usa |
| --- | --- | --- | --- |
| `gestoroperaciones` | `Iso2Curso25-26` | solo DML (`SELECT/INSERT/UPDATE/DELETE`) sobre `iso2.*` | la aplicacion (`spring.datasource`) |
| `iso2_flyway` | `Iso2Flyway25-26` | DDL + DML sobre `iso2.*` | Flyway (`spring.flyway.user`) |
| `root` | `Iso2Curso25-26-Root` | todo (solo admin del contenedor) | nadie desde la aplicacion |

La aplicacion no puede crear ni alterar tablas: el esquema lo versiona Flyway
desde `src/main/resources/db/migration/mysql/`.

## Correr el servidor contra MySQL

```powershell
$env:ISO2_JWT_SECRET = "una-frase-larga-de-pista-compartida-segura"
mvn -B -ntp -pl iso2-server-app spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

El perfil `mysql` de `application.yml` apunta a `jdbc:mysql://localhost:3307/iso2`
con `gestoroperaciones`, y hace que Flyway use `iso2_flyway` y la carpeta
`db/migration/mysql`. Las migraciones viven en dos carpetas hermanas
(`db/migration/h2` y `db/migration/mysql`) con las mismas versiones V1.x: la que
se aplica la elige el perfil activo (se probo la sintaxis `{vendor}` de Flyway y
escaneaba las dos carpetas a la vez en classpath, por eso la carpeta se fija por
perfil).

## phpMyAdmin

`http://localhost:8081` — login con `gestoroperaciones` (ver datos) o con
`iso2_flyway` (ver y administrar tablas).

## Pruebas automatizadas contra MySQL

El test `PersistenciaMysqlTestcontainersTest` de `iso2-server-app` levanta su
propio contenedor MySQL 8.4 con Testcontainers (no usa este compose) y se salta
automaticamente si Docker no esta disponible en la maquina.
