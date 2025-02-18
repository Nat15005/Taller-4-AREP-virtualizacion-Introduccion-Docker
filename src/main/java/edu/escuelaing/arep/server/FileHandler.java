package edu.escuelaing.arep.server;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;

/**
 * Clase encargada de manejar la lectura y envío de archivos en el servidor HTTP.
 * Permite servir archivos estáticos desde un directorio base y determinar su tipo MIME.
 */
public class FileHandler {
    /**
     * Sirve archivos estáticos desde la carpeta configurada en WebFramework.
     *
     * @param resource El recurso (archivo) solicitado por el cliente.
     * @param out El flujo de salida donde se enviará el archivo solicitado.
     * @throws IOException Si ocurre un error al leer el archivo o escribir en el flujo de salida.
     */
    public static void serveFile(String resource, OutputStream out) throws IOException {
        if (resource.equals("/")) {
            resource = "/index.html"; // Redirigir a index.html si se accede a "/"
        }

        // Obtén la ruta base de los archivos estáticos
        String staticFolder = WebFramework.getStaticFolder();
        Path filePath = Path.of(staticFolder + resource);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            // Si el archivo existe, envía los encabezados y el contenido
            String contentType = getContentType(resource);
            byte[] fileBytes = Files.readAllBytes(filePath);

            // Construye los encabezados HTTP
            String responseHeader = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" + // Permite solicitudes desde cualquier origen
                    "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" + // Métodos permitidos
                    "Access-Control-Allow-Headers: Content-Type\r\n" + // Encabezados permitidos
                    "Content-Length: " + fileBytes.length + "\r\n" +
                    "\r\n";

            // Envía los encabezados y el contenido del archivo
            out.write(responseHeader.getBytes());
            out.write(fileBytes);
        } else {
            // Si el archivo no existe, envía una respuesta 404
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" +
                    "Access-Control-Allow-Headers: Content-Type\r\n" +
                    "\r\n" +
                    "404 Not Found";
            out.write(response.getBytes());
        }
    }

    private static void sendNotFound(OutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "404 Not Found";
        out.write(response.getBytes());
    }

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