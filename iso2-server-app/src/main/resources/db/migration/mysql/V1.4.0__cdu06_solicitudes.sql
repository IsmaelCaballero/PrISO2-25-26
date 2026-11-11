-- V1.4.0 CDU06 (Iter4/CFG-04): solicitudes de prueba prescritas desde Atencion Primaria,
-- con no repudio sellado por huella SHA-256 (RF06.2). El esquema sigue siendo
-- autoritativo (spring.jpa.hibernate.ddl-auto = none).
-- Variante MySQL (D34): AUTO_INCREMENT y DATETIME(6) (la conexion JDBC fuerza UTC).

CREATE TABLE solicitud (
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    solicitud_id       VARCHAR(36)  NOT NULL,
    paciente_id        VARCHAR(40)  NOT NULL,
    codigo_prueba      VARCHAR(40)  NOT NULL,
    indicacion_clinica VARCHAR(500) NOT NULL,
    prioridad          VARCHAR(20)  NOT NULL,
    origen             VARCHAR(40)  NOT NULL,
    medico_id          VARCHAR(60)  NOT NULL,
    creada_en          DATETIME(6)  NOT NULL,
    estado             VARCHAR(20)  NOT NULL,
    huella             VARCHAR(64)  NOT NULL,
    CONSTRAINT uk_solicitud_solicitud_id UNIQUE (solicitud_id)
);

-- Indices de las consultas del CDU06: historial del medico (listarPorMedico, el mas
-- frecuente), trazas del paciente y de la prueba. El anti-duplicado (RF06.4) NO es un
-- unique aqui: lo aplica el servicio sobre los estados activos, porque re-prescribir
-- tras una CANCELADA es legitimo y el estado cambia con la vida de la solicitud.
CREATE INDEX idx_solicitud_medico ON solicitud (medico_id, creada_en);
CREATE INDEX idx_solicitud_paciente ON solicitud (paciente_id);
CREATE INDEX idx_solicitud_codigo_prueba ON solicitud (codigo_prueba);

-- Datos semilla de DESARROLLO: pruebas ACTIVAS del catalogo (la V1.1.0 no sembraba
-- ninguna), para poder prescribir sin dar de alta el catalogo a mano. Codigos propios
-- SEM-* que no chocan con las pruebas que crean los tests de la API (SMOKE-*).
INSERT INTO catalogo_prueba (codigo, nombre, categoria, requiere_cita, duracion_minutos, activa,
                             protocolo_preparacion, protocolo_ayuno_horas, protocolo_contraindicaciones,
                             politica_plazo_cancelacion_horas, politica_max_cancelaciones) VALUES
    ('SEM-RX-TORAX',   'Radiografia de torax',            'RX', FALSE, 10, TRUE,
     'sin preparacion especifica', 0, 'embarazo (radiacion)', 24, 3),
    ('SEM-RM-RODILLA', 'Resonancia magnetica de rodilla', 'RM', TRUE,  30, TRUE,
     'retirar objetos metalicos',  0, 'marcapasos o clips metalicos', 48, 2);
