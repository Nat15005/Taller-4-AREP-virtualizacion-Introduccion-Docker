package edu.escuelaing.arep.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP request.
 * This class contains the query parameters from the URL and provides methods to retrieve their values.
 * It also handles the parsing of the request body for POST requests.
 */
public class Request {

    // Mapa de parámetros de consulta extraídos de la URL
    private final Map<String, String> queryParams;
    private Map<String, String> bodyParams = new HashMap<>();
    private BufferedReader bodyReader;
    private boolean bodyParsed = false;

    /**
     * Constructor for the Request class.
     *
     * @param queryParams A map of query parameters extracted from the URL.
     */
    public Request(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Retrieves the value of a specific query parameter.
     *
     * @param key The name of the query parameter to retrieve.
     * @return The value of the query parameter, or an empty string if the parameter is not found.
     */
    public String getValues(String key) {
        return queryParams.getOrDefault(key, "");
    }

    /**
     * Assigns a BufferedReader to read the body of a POST request.
     * Parses the body and stores the parameters in a map.
     *
     * @param reader The BufferedReader to read the request body.
     */
    public void setBodyReader(BufferedReader reader) {
        if (!bodyParsed) {
            this.bodyParams = parseBody(reader);
            bodyParsed = true;
        }
    }

    /**
     * Retrieves the map of query parameters.
     *
     * @return The map of query parameters.
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * Reads the body of the POST request as a String.
     *
     * @return The body of the request as a String.
     */
    public String getBody() {
        if (bodyReader == null) return "";
        return bodyReader.lines().reduce("", (acc, line) -> acc + line + "\n");
    }

    /**
     * Retrieves the map of body parameters.
     *
     * @return The map of body parameters.
     */
    public Map<String, String> getBodyParams() {
        return bodyParams;
    }

    /**
     * Retrieves the value of a specific body parameter.
     *
     * @param key The name of the body parameter to retrieve.
     * @return The value of the body parameter, or an empty string if the parameter is not found.
     */
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
