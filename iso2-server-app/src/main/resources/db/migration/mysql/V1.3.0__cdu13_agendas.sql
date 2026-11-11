-- V1.3.0 CDU13 (Iter3/CFG-03): agendas de medicos y tecnicos con franjas, festivos
-- y auditoria de hash encadenado. El esquema sigue siendo autoritativo
-- (spring.jpa.hibernate.ddl-auto = none).
-- Variante MySQL (D34): ademas de los cambios de dialecto habituales
-- (AUTO_INCREMENT, DATETIME(6)), las literales estandar DATE '...' / TIME '...'
-- del fichero h2 pasan a cadenas ISO ('2026-11-09', '08:00:00'), que MySQL acepta
-- para columnas DATE y TIME.

CREATE TABLE agenda_profesional (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profesional_id  VARCHAR(60) NOT NULL,
    CONSTRAINT uk_agenda_profesional UNIQUE (profesional_id)
);

CREATE TABLE agenda_franja (
    id         BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agenda_id  BIGINT  NOT NULL,
    dia        DATE    NOT NULL,
    inicio     TIME    NOT NULL,
    fin        TIME    NOT NULL,
    CONSTRAINT fk_agenda_franja_agenda FOREIGN KEY (agenda_id) REFERENCES agenda_profesional (id)
);

CREATE INDEX idx_agenda_franja_agenda_dia ON agenda_franja (agenda_id, dia);

CREATE TABLE agenda_festivo (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agenda_id  BIGINT       NOT NULL,
    dia        DATE         NOT NULL,
    motivo     VARCHAR(200) NOT NULL,
    CONSTRAINT fk_agenda_festivo_agenda FOREIGN KEY (agenda_id) REFERENCES agenda_profesional (id)
);

CREATE INDEX idx_agenda_festivo_agenda_dia ON agenda_festivo (agenda_id, dia);

CREATE TABLE agenda_auditoria (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    accion          VARCHAR(20)  NOT NULL,
    profesional_id  VARCHAR(60)  NOT NULL,
    usuario         VARCHAR(100) NOT NULL,
    instante        DATETIME(6)  NOT NULL,
    hash            VARCHAR(64)  NOT NULL,
    hash_anterior   VARCHAR(64)
);

CREATE INDEX idx_agenda_auditoria_profesional ON agenda_auditoria (profesional_id);

-- Datos semilla de DESARROLLO: agenda con franjas del medico y del tecnico (usuarios de
-- la migracion V1.2.0) para poder probar la consulta de huecos libres sin dar de alta
-- nada a mano. El festivo marca dias sin disponibilidad: franjasLibres los descarta
-- (la regla del CDU es que el festivo manda sobre las franjas del dia).
-- profesional_id = usuario de seguridad en la v1.
INSERT INTO agenda_profesional (profesional_id) VALUES ('medico'), ('tecnico');

-- agenda 1 = medico, agenda 2 = tecnico (identidades en orden de insercion).
INSERT INTO agenda_franja (agenda_id, dia, inicio, fin) VALUES
    (1, '2026-11-09', '08:00:00', '15:00:00'),   -- lunes manana+tarde
    (1, '2026-11-11', '08:00:00', '15:00:00'),   -- miercoles
    (1, '2026-11-13', '08:00:00', '15:00:00'),   -- viernes
    (2, '2026-11-10', '09:00:00', '14:00:00'),   -- tecnico martes manana
    (2, '2026-11-12', '09:00:00', '14:00:00');   -- tecnico jueves manana

INSERT INTO agenda_festivo (agenda_id, dia, motivo) VALUES
    (1, '2026-11-10', 'dia natural del centro');
