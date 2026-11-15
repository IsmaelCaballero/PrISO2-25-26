package es.uclm.iso2.integracion.dominio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utilidad de huellas SHA-256 en hexadecimal. La usan {@link AuditoriaIntegracion} para
 * encadenar eslabones y el servicio para decidir si una sincronizacion de historia es
 * repetida. El patron es el mismo que la auditoria persistida de citas: aqui en memoria.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public final class HuellaContenido {

    private HuellaContenido() {
    }

    /**
     * SHA-256 en hexadecimal minusculas del texto canonico recibido.
     *
     * @param canonico texto canonico a hashear
     * @return la huella en hexadecimal
     */
    public static String sha256Hex(String canonico) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonico.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
