package es.uclm.iso2.integracion.puerto;

/**
 * Salida hacia el archivo de imagenes (PACS). En el spike CDU19 el PACS solo se
 * monitoriza: el despacho DICOM real llega con CDU11/CDU09.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public interface PacsPort extends SistemaImagenPort {
}
