package edu.escuelaing.arep.server;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;

/**
 * Handles the reading and serving of files in the HTTP server.
 * This class serves static files from a base directory and determines their MIME type.
 */
public class FileHandler {
    /**
     * Serves static files from the folder configured in WebFramework.
     *
     * @param resource The resource (file) requested by the client.
     * @param out The output stream where the requested file will be sent.
     * @throws IOException If an error occurs while reading the file or writing to the output stream.
     */
    public static void serveFile(String resource, OutputStream out) throws IOException {
        if (resource.equals("/")) {
            resource = "/index.html";
        }

        // Get the base path for static files
        String staticFolder = WebFramework.getStaticFolder();
        Path filePath = Path.of(staticFolder + resource);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            // If the file exists, send the headers and content
            String contentType = getContentType(resource);
            byte[] fileBytes = Files.readAllBytes(filePath);

            String responseHeader = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" + // Permite solicitudes desde cualquier origen
                    "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" + // Métodos permitidos
                    "Access-Control-Allow-Headers: Content-Type\r\n" + // Encabezados permitidos
                    "Content-Length: " + fileBytes.length + "\r\n" +
                    "\r\n";

            out.write(responseHeader.getBytes());
            out.write(fileBytes);
        } else {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" +
                    "Access-Control-Allow-Headers: Content-Type\r\n" +
                    "\r\n" +
                    "404 Not Found";
            out.write(response.getBytes());
        }
    }

    /**
     * Sends a 404 Not Found response to the client.
     *
     * @param out The output stream to send the response.
     * @throws IOException If an error occurs while writing to the output stream.
     */
    private static void sendNotFound(OutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "404 Not Found";
        out.write(response.getBytes());
    }

    /**
     * Determines the MIME type file based on its extension.
     *
     * @param fileName The name of the file.
     * @return The MIME type of the file, or "application/octet-stream" if the extension is unknown.
     */
    static String getContentType(String fileName) {
        HashMap<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }
}