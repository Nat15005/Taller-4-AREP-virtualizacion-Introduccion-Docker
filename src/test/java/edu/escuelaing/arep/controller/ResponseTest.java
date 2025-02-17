package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.controller.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testDefaultContentType() {
        Response response = new Response();
        // Verificar que el tipo de contenido por defecto sea "text/plain"
        assertEquals("text/plain", response.getContentType(), "El tipo de contenido por defecto debería ser 'text/plain'");
    }

    @Test
    void testSetContentType() {
        Response response = new Response();
        // Establecer un nuevo tipo de contenido
        response.setContentType("application/json");
        // Verificar que el tipo de contenido se haya actualizado correctamente
        assertEquals("application/json", response.getContentType(), "El tipo de contenido debería ser 'application/json'");
    }

    @Test
    void testSetContentTypeToNull() {
        Response response = new Response();
        // Establecer el tipo de contenido a null
        response.setContentType(null);
        // Verificar que el tipo de contenido se haya actualizado a null
        assertNull(response.getContentType(), "El tipo de contenido debería ser null");
    }

    @Test
    void testSetContentTypeToEmptyString() {
        Response response = new Response();
        // Establecer el tipo de contenido a una cadena vacía
        response.setContentType("");
        // Verificar que el tipo de contenido se haya actualizado a una cadena vacía
        assertEquals("", response.getContentType(), "El tipo de contenido debería ser una cadena vacía");
    }
}