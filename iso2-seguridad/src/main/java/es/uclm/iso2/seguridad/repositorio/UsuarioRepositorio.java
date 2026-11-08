package es.uclm.iso2.seguridad.repositorio;

import es.uclm.iso2.seguridad.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA de usuarios (CDU17). La busqueda es por nombre de usuario (el
 * identificador de negocio); el {@code id} es tecnico.
 *
 * @since 1.1.0 (CFG-02)
 */
@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de inicio de sesion.
     *
     * @param usuario nombre a buscar
     * @return el usuario si existe; vacio en caso contrario
     */
    Optional<Usuario> findByUsuario(String usuario);
}
