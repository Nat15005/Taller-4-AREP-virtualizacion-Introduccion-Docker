package edu.escuelaing.arep;

import java.io.*;
import java.net.*;

/**
 * MockSocket simula un socket de red para pruebas. Esta clase hereda de {@link Socket}
 * y permite simular el comportamiento de un cliente que realiza una solicitud HTTP.
 * Es útil para pruebas unitarias donde se necesita simular una conexión de cliente sin
 * necesidad de interactuar con la red real.
 *
 * <p>La clase permite proporcionar una solicitud HTTP simulada al servidor y obtener la
 * respuesta generada por el servidor como si estuviera siendo enviada por un socket real.</p>
 */
public class MockSocket extends Socket {

    /** La solicitud HTTP simulada que se envía al servidor. */
    private final String request;

    /** Flujo de salida donde se almacena la respuesta generada por el servidor. */
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    /**
     * Constructor que recibe una solicitud HTTP simulada.
     *
     * @param request La solicitud HTTP que se enviará al servidor simulado.
     *                El formato esperado es una cadena que representa una solicitud HTTP,
     *                como por ejemplo: "GET / HTTP/1.1".
     */
    public MockSocket(String request) {
        this.request = request + "\r\n\r\n";  // Añadido un par de saltos de línea para imitar un HTTP request completo.
    }

    /**
     * Sobrescribe el metodo {@link Socket#getInputStream()} para proporcionar una entrada simulada.
     * Este metodo devuelve un {@link ByteArrayInputStream} que lee la solicitud HTTP proporcionada.
     *
     * @return Un {@link InputStream} que contiene la solicitud HTTP simulada.
     * @throws IOException Si ocurre un error al crear el flujo de entrada.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(request.getBytes());
    }

    /**
     * Sobrescribe el metodo {@link Socket#getOutputStream()} para proporcionar un flujo de salida simulado.
     * Este flujo se usa para capturar la respuesta generada por el servidor.
     *
     * @return Un {@link OutputStream} donde se almacena la respuesta del servidor.
     * @throws IOException Si ocurre un error al crear el flujo de salida.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    /**
     * Obtiene la respuesta simulada generada por el servidor y almacenada en el flujo de salida.
     *
     * @return La respuesta generada por el servidor como una cadena de texto.
     */
    public String getResponse() {
        return outputStream.toString();
    }
}
