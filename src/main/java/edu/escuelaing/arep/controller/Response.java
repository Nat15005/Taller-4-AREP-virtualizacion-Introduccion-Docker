package edu.escuelaing.arep.controller;

/**
 * Clase que representa una respuesta HTTP (Response).
 * Permite configurar y obtener el tipo de contenido de la respuesta.
 */
public class Response {

    // Tipo de contenido de la respuesta (por defecto es "text/plain")
    private String contentType = "text/plain";

    /**
     * Establece el tipo de contenido de la respuesta HTTP.
     *
     * @param contentType El tipo de contenido que se desea establecer.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Obtiene el tipo de contenido de la respuesta HTTP.
     *
     * @return El tipo de contenido de la respuesta.
     */
    public String getContentType() {
        return contentType;
    }
}
