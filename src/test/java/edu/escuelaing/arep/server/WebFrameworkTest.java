package edu.escuelaing.arep.server;

import edu.escuelaing.arep.controller.Request;
import edu.escuelaing.arep.controller.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class WebFrameworkTest {

    @BeforeEach
    void setUp() {
        WebFramework.getRoutes.clear();
        WebFramework.postRoutes.clear();
        WebFramework.deleteRoutes.clear();
    }


    @Test
    void testGetRouteRegistrationAndExecution() {
        WebFramework.get("/hello", (req, res) -> "Hello World");
        assertTrue(WebFramework.getRoutes.containsKey("/hello"));
        assertEquals("Hello World", WebFramework.getRoutes.get("/hello").apply(new Request(new HashMap<>()), new Response()));
    }

    @Test
    void testPostRouteRegistrationAndExecution() {
        WebFramework.post("/submit", (req, res) -> "Data Saved");
        assertTrue(WebFramework.postRoutes.containsKey("/submit"));
        assertEquals("Data Saved", WebFramework.postRoutes.get("/submit").apply(new Request(new HashMap<>()), new Response()));
    }

    @Test
    void testDeleteRouteRegistrationAndExecution() {
        WebFramework.delete("/remove", (req, res) -> "Deleted Successfully");
        assertTrue(WebFramework.deleteRoutes.containsKey("/remove"));
        assertEquals("Deleted Successfully", WebFramework.deleteRoutes.get("/remove").apply(new Request(new HashMap<>()), new Response()));
    }

    @Test
    void testHandleRequestForRegisteredGetRoute() throws IOException {
        WebFramework.get("/test", (req, res) -> "Test Response");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WebFramework.handleRequest("GET", "/test", new HashMap<>(), null, outputStream);

        String response = outputStream.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Test Response"));
    }

    @Test
    void testHandleRequestForUnknownRoute() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WebFramework.handleRequest("GET", "/unknown", new HashMap<>(), null, outputStream);

        String response = outputStream.toString();
        assertTrue(response.contains("404 Not Found") || response.contains("HTTP/1.1 200 OK")); // Por si intenta servir un archivo
    }

    @Test
    void testHandleRequestForPostMethod() throws IOException {
        WebFramework.post("/submit", (req, res) -> "Post Received");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WebFramework.handleRequest("POST", "/submit", new HashMap<>(), "", outputStream);

        String response = outputStream.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Post Received"));
    }

    @Test
    void testHandleRequestForDeleteMethod() throws IOException {
        WebFramework.delete("/remove", (req, res) -> "Delete Successful");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WebFramework.handleRequest("DELETE", "/remove", new HashMap<>(), "", outputStream);

        String response = outputStream.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Delete Successful"));
    }

    @Test
    void testHandleRequestForUnsupportedMethod() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WebFramework.handleRequest("PUT", "/any", new HashMap<>(), "", outputStream);

        String response = outputStream.toString();
        assertTrue(response.contains("405 Method Not Allowed"));
    }

}
