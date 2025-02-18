package edu.escuelaing.arep.server;

import edu.escuelaing.arep.controller.BookController;
import edu.escuelaing.arep.controller.RequestHandler;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpServerTest {

    private static final int PORT = 35000;
    private static ExecutorService executor;

    @BeforeAll
    public void setUp() {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                HttpServer.start();
            } catch (IOException e) {
                fail("No se pudo iniciar el servidor: " + e.getMessage());
            }
        });

        // Esperar un poco para que el servidor inicie
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    public void tearDown() {
        HttpServer.stop();
        executor.shutdown();
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numThreads = 5;
        ExecutorService requestExecutor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        String requestUrl = "http://localhost:" + PORT + "/getBooks";

        for (int i = 0; i < numThreads; i++) {
            requestExecutor.submit(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    assertEquals(200, responseCode, "El servidor debe responder con 200 OK");
                } catch (IOException e) {
                    fail("Error en la solicitud HTTP: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        requestExecutor.shutdown();
    }
    @Test
    public void testConcurrentPostAndDeleteRequests() throws InterruptedException {
        int numThreads = 5;
        ExecutorService requestExecutor = Executors.newFixedThreadPool(numThreads * 2);
        CountDownLatch latch = new CountDownLatch(numThreads * 2);
        String postUrl = "http://localhost:" + PORT + "/addBook";
        String deleteUrl = "http://localhost:" + PORT + "/deleteBook";

        for (int i = 0; i < numThreads; i++) {
            int bookId = i;
            requestExecutor.submit(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(postUrl).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        String params = "bookTitle=Libro" + bookId + "&bookAuthor=Autor" + bookId;
                        os.write(params.getBytes());
                        os.flush();
                    }
                    int responseCode = connection.getResponseCode();
                    assertEquals(200, responseCode, "POST debe responder con 200 OK");
                } catch (IOException e) {
                    fail("Error en la solicitud POST: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            requestExecutor.submit(() -> {
                try {
                    Thread.sleep(100); // Dar un poco de tiempo para que los libros se agreguen antes de eliminarlos
                    HttpURLConnection connection = (HttpURLConnection) new URL(deleteUrl).openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        String params = "bookTitle=Libro" + bookId;
                        os.write(params.getBytes());
                        os.flush();
                    }
                    int responseCode = connection.getResponseCode();
                    assertEquals(200, responseCode, "DELETE debe responder con 200 OK");
                } catch (IOException | InterruptedException e) {
                    fail("Error en la solicitud DELETE: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        requestExecutor.shutdown();
    }

}
