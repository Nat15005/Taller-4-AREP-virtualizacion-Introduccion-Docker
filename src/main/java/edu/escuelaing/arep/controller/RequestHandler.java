package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.server.WebFramework;
import edu.escuelaing.arep.model.Book;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;

/**
 * Class responsible for handling HTTP requests from clients.
 * This class manages book-related operations (GET, POST, DELETE) and serves static files.
 */
public class RequestHandler {

    /**
     * Handles a client request.
     * Reads the request, extracts the HTTP method, resource, and query parameters,
     * and then processes the request based on the HTTP method (GET, POST, DELETE).
     *
     * @param clientSocket The client socket making the request.
     * @throws IOException If an I/O error occurs while handling the request.
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
     * Reads the headers of the request.
     *
     * @param in The input stream of the request.
     * @param out The output stream to send the response.
     * @return A map of key-value headers.
     * @throws IOException If an I/O error occurs.
     */
    private static HashMap<String, String> readHeaders(BufferedReader in, OutputStream out) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            } else {
                sendBadRequest(out);
                return null;
            }
        }
        return headers;
    }

    /**
     * Sends an HTTP 400 Bad Request response.
     *
     * @param out The output stream to send the response.
     * @throws IOException If an I/O error occurs.
     */
    private static void sendBadRequest(OutputStream out) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "400 Bad Request";
        out.write(response.getBytes());
    }

}
