package edu.escuelaing.arep.controller;

/**
 * Represents an HTTP response.
 * This class allows setting and retrieving the content type of the response.
 */
public class Response {

    private String contentType = "text/plain";

    /**
     * Sets the content type of the HTTP response.
     *
     * @param contentType The content type to set.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Retrieves the content type of the HTTP response.
     *
     * @return The content type of the response.
     */
    public String getContentType() {
        return contentType;
    }
}
