package es.uclm.iso2.common.pacientes;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code pacientes}: Datos demograficos de paciente (CDU11).
 * Solo contrato: la implementacion vive en el modulo iso2-pacientes (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface PacienteFacade {

    /**
     * Busca un paciente por su numero de historia clinica.
     *
     * @param historiaClinica numero de historia clinica
     * @return el paciente si existe, vacio en caso contrario
     */
    Optional<PacienteDTO> buscarPorHistoriaClinica(String historiaClinica);

    /**
     * Crea o actualiza un paciente (alta desde HIS).
     *
     * @param paciente datos del paciente
     * @return paciente persistido
     */
    PacienteDTO upsert(PacienteDTO paciente);

    /**
     * Paciente registrado en el sistema.
     *
     * @param historiaClinica numero de historia clinica
     * @param nombreCompleto  nombre completo
     * @param fechaNacimiento fecha de nacimiento
     * @param centroOrigen    centro de origen (SESCAM)
     */
    record PacienteDTO(String historiaClinica, String nombreCompleto, LocalDate fechaNacimiento, String centroOrigen) {
    }
}
