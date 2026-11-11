-- D34 · separacion de privilegios (decidida y aceptada el 08/11/2026):
--   * gestoroperaciones: usuario de la APLICACION, solo DML (la aplicacion jamas
--     puede alterar el esquema; el esquema solo lo toca Flyway).
--   * iso2_flyway: usuario DDL que usa Flyway (spring.flyway.user) para crear y
--     versionar tablas y su propia tabla flyway_schema_history.
-- Solo DESARROLLO: las contrasenas van documentadas a proposito (igual que la
-- semilla BCrypt de V1.2.0). En produccion se usarian secretos fuera del repo.

CREATE DATABASE IF NOT EXISTS iso2 CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Usuario de aplicacion: DML puro sobre iso2.
CREATE USER IF NOT EXISTS 'gestoroperaciones'@'%' IDENTIFIED BY 'Iso2Curso25-26';
GRANT SELECT, INSERT, UPDATE, DELETE ON iso2.* TO 'gestoroperaciones'@'%';

-- Usuario de migraciones: DDL + DML para la tabla de control de Flyway.
CREATE USER IF NOT EXISTS 'iso2_flyway'@'%' IDENTIFIED BY 'Iso2Flyway25-26';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES
    ON iso2.* TO 'iso2_flyway'@'%';

FLUSH PRIVILEGES;
