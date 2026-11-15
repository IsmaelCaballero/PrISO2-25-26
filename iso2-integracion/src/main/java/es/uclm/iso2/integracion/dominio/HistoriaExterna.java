package es.uclm.iso2.integracion.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Historia clinica externa de un paciente tal y como la devuelve la HCE. El spike usa
 * {@link #canonica()} como entrada de la huella SHA-256 que decide si la sincronizacion
 * es repetida ({@code reutilizado=true}) o aporta algo nuevo.
 *
 * @param pacienteExternoId identificador del paciente en el sistema externo
 * @param registros         registros clinicos devueltos
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record HistoriaExterna(String pacienteExternoId, List<RegistroHistoria> registros) {

    /**
     * Crea la historia exigiendo paciente y normalizando la lista de registros.
     */
    public HistoriaExterna {
        if (pacienteExternoId == null || pacienteExternoId.isBlank()) {
            throw new IllegalArgumentException("el paciente externo es obligatorio");
        }
        registros = registros == null ? List.of() : List.copyOf(registros);
    }

    /**
     * Forma canonica del contenido: registros ordenados por codigo y fecha, unidos con
     * salto de linea. Ordenar aqui (y no fiarse del orden que trae el sistema) es lo que
     * hace estable la huella: el mismo contenido produce siempre la misma SHA-256.
     *
     * @return el texto canonico que se pasa a {@link HuellaContenido#sha256Hex(String)}
     */
    public String canonica() {
        List<RegistroHistoria> ordenados = new ArrayList<>(registros);
        ordenados.sort(Comparator.comparing(RegistroHistoria::codigo)
                .thenComparing(RegistroHistoria::fecha));
        StringBuilder sb = new StringBuilder(pacienteExternoId);
        for (RegistroHistoria registro : ordenados) {
            sb.append('\n').append(registro.codigo()).append('|')
                    .append(registro.fecha()).append('|')
                    .append(registro.detalle() == null ? "" : registro.detalle());
        }
        return sb.toString();
    }
}
