package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.server.WebFramework;
import edu.escuelaing.arep.model.Book;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;

/**
 * Clase encargada de manejar las solicitudes HTTP de los clientes, incluyendo
 * la gestión de libros (GET, POST, DELETE) y la respuesta con archivos estáticos.
 */
public class RequestHandler {

    /** Lista de libros almacenados en memoria. */
    private static final List<Book> books = new ArrayList<>();

    /**
     * Maneja una solicitud de un cliente.
     * Lee la solicitud, extrae el metodo, el recurso y los parámetros de la consulta,
     * y luego maneja la solicitud según el metodo HTTP (GET, POST, DELETE).
     *
     * @param clientSocket Socket del cliente que hace la solicitud.
     * @throws IOException Si ocurre un error de entrada o salida al manejar la solicitud.
     */
    public static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            sendBadRequest(out);
            clientSocket.close();
            return;
        }

        System.out.println("Solicitud recibida: " + requestLine);
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            sendBadRequest(out);
            clientSocket.close();
            return;
        }

        String method = requestParts[0];  // GET, POST, DELETE, etc.
        String fullResource = requestParts[1]; // /App/hello?name=Pedro

        String resource = fullResource.split("\\?")[0];
        Map<String, String> queryParams = new HashMap<>();
        if (fullResource.contains("?")) {
            String queryString = fullResource.split("\\?")[1];
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(URLDecoder.decode(keyValue[0], "UTF-8"), URLDecoder.decode(keyValue[1], "UTF-8"));
                }
            }
        }

        HashMap<String, String> headers = readHeaders(in, out);
        if (headers == null) {
            clientSocket.close();
            return;
        }

        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        StringBuilder body = new StringBuilder();
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            body.append(buffer);
        }

        WebFramework.handleRequest(method, resource, queryParams, body.toString(), out);

        out.close();
        in.close();
        clientSocket.close();
    }


    /**
     * Envía la lista de libros en formato JSON como respuesta a una solicitud GET.
     *
     * @param out Flujo de salida para enviar la respuesta.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    private static void serveBooksJson(OutputStream out) throws IOException {
        StringBuilder json = new StringBuilder("{ \"books\": [");
        for (int i = 0; i < books.size(); i++) {
            json.append(books.get(i).toString());
            if (i < books.size() - 1) json.append(", ");
        }
        json.append("] }");

        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + json.length() + "\r\n" +
                "\r\n";

        out.write(responseHeader.getBytes());
        out.write(json.toString().getBytes());
    }

    /**
     * Maneja una solicitud POST para agregar un nuevo libro a la lista.
     *
     * @param headers Encabezados de la solicitud.
     * @param in Flujo de entrada de la solicitud.
     * @param out Flujo de salida para enviar la respuesta.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    private static void handlePost(HashMap<String, String> headers, BufferedReader in, OutputStream out) throws IOException {
        // Leer el cuerpo de la solicitud
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        String bodyContent = new String(body);

        // Limpiar y separar el cuerpo en partes
        bodyContent = bodyContent.trim().replace("{", "").replace("}", "").replace("\"", "");
        String[] parts = bodyContent.split(",");

        String title = null;
        String author = null;

        // Extraer título y autor de las partes
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if (key.equals("title")) {
                    title = value;
                } else if (key.equals("author")) {
                    author = value;
                }
            }
        }

        // Verificar si ambos campos están presentes y no vacíos
        if (title != null && author != null && !title.isEmpty() && !author.isEmpty()) {
            books.add(new Book(title, author));  // Añadir el libro
        }

        String response = "HTTP/1.1 201 Created\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Libro añadido: " + (title != null ? title : "Desconocido") + " por " + (author != null ? author : "Desconocido");

        out.write(response.getBytes());
    }

    /**
     * Maneja una solicitud DELETE para eliminar un libro de la lista por su título.
     *
     * @param headers Encabezados de la solicitud.
     * @param in Flujo de entrada de la solicitud.
     * @param out Flujo de salida para enviar la respuesta.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    private static void handleDelete(HashMap<String, String> headers, BufferedReader in, OutputStream out) throws IOException {
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        if (contentLength == 0) {
            sendBadRequest(out);
            return;
        }

        // Leer el cuerpo de la solicitud
        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        String bodyContent = new String(body);

        // Extraer el nombre del libro
        String title = bodyContent.replace("{\"title\":\"", "").replace("\"}", "").trim();

        // Eliminar libro de la lista
        boolean removed = books.removeIf(book -> book.getTitle().equals(title));

        // Construir la respuesta HTTP
        String response = "HTTP/1.1 204 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                (removed ? "Libro eliminado: " + title : "Libro no encontrado");

        out.write(response.getBytes());
    }

    /**
     * Lee los encabezados de la solicitud.
     *
     * @param in Flujo de entrada de la solicitud.
     * @param out Flujo de salida para enviar la respuesta.
     * @return Un mapa de encabezados clave-valor.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    private static HashMap<String, String> readHeaders(BufferedReader in, OutputStream out) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            } else {
                // Si un encabezado no está bien formado, respondemos con Bad Request
                sendBadRequest(out);
                return null;  // Devuelvo null si hay un error en los encabezados
            }
        }
        return headers;
    }

    /**
     * Envía una respuesta HTTP 400 Bad Request.
     *
     * @param out Flujo de salida para enviar la respuesta.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    private static void sendBadRequest(OutputStream out) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "400 Bad Request";
        out.write(response.getBytes());
    }

}
