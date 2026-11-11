-- Copia de fe de docker/init/01-usuarios.sql para el contenedor de pruebas
-- (Testcontainers la ejecuta como root antes de arrancar la aplicacion).
-- Misma separacion D34: gestoroperaciones solo DML, iso2_flyway con DDL.
-- Son credenciales SOLO de pruebas, documentadas a proposito igual que las de desarrollo.

CREATE DATABASE IF NOT EXISTS iso2 CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'gestoroperaciones'@'%' IDENTIFIED BY 'Iso2Curso25-26';
GRANT SELECT, INSERT, UPDATE, DELETE ON iso2.* TO 'gestoroperaciones'@'%';

CREATE USER IF NOT EXISTS 'iso2_flyway'@'%' IDENTIFIED BY 'Iso2Flyway25-26';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES
    ON iso2.* TO 'iso2_flyway'@'%';

FLUSH PRIVILEGES;
