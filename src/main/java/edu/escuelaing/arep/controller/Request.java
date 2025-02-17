package edu.escuelaing.arep.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa una solicitud HTTP (Request).
 * Contiene los parámetros de consulta de la URL y proporciona métodos para obtener sus valores.
 */
public class Request {

    // Mapa de parámetros de consulta extraídos de la URL
    private final Map<String, String> queryParams;
    private Map<String, String> bodyParams = new HashMap<>();
    private BufferedReader bodyReader; // 🔹 Agregado aquí
    private boolean bodyParsed = false;
    /**
     * Constructor de la clase Request.
     *
     * @param queryParams Mapa de parámetros de consulta extraídos de la URL.
     */
    public Request(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Obtiene el valor de un parámetro de consulta específico.
     *
     * @param key El nombre del parámetro de consulta que se desea obtener.
     * @return El valor del parámetro de consulta, o una cadena vacía si no se encuentra el parámetro.
     */
    public String getValues(String key) {
        return queryParams.getOrDefault(key, "");
    }
    // Nuevo metodo para asignar el cuerpo del POST
    public void setBodyReader(BufferedReader reader) {
        if (!bodyParsed) {
            this.bodyParams = parseBody(reader);
            bodyParsed = true;
        }
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    // Metodo para leer el cuerpo del POST como String
    public String getBody() {
        if (bodyReader == null) return "";
        return bodyReader.lines().reduce("", (acc, line) -> acc + line + "\n");
    }
    public Map<String, String> getBodyParams() {
        return bodyParams;
    }
    public String getBodyParam(String key) {
        return bodyParams.getOrDefault(key, "");
    }
    private Map<String, String> parseBody(BufferedReader reader) {
        Map<String, String> params = new HashMap<>();
        try {
            StringBuilder bodyContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyContent.append(line);
            }

            String body = bodyContent.toString();
            if (!body.isEmpty()) {
                String[] pairs = body.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        params.put(URLDecoder.decode(keyValue[0], "UTF-8"),
                                URLDecoder.decode(keyValue[1], "UTF-8"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }
}
