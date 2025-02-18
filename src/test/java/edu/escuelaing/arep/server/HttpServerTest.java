package edu.escuelaing.arep.server;

import org.junit.Test;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class HttpServerTest {

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    Socket socket = new Socket("localhost", 35000);
                    String request = "GET /getBooks HTTP/1.1\r\nHost: localhost\r\n\r\n";
                    socket.getOutputStream().write(request.getBytes());
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    assertTrue(response.contains("HTTP/1.1 200 OK"));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void testConcurrentPostAndDeleteRequests() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 5; i++) {
            // Solicitudes POST para añadir libros
            executorService.submit(() -> {
                try {
                    Socket socket = new Socket("localhost", 35000);
                    String request = "POST /addBook HTTP/1.1\r\nHost: localhost\r\nContent-Length: 36\r\n\r\nbookTitle=Libro" + Thread.currentThread().getId() + "&bookAuthor=Autor";
                    socket.getOutputStream().write(request.getBytes());
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    assertTrue(response.contains("HTTP/1.1 200 OK"));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Solicitudes DELETE para eliminar libros
            executorService.submit(() -> {
                try {
                    Socket socket = new Socket("localhost", 35000);
                    String request = "DELETE /deleteBook HTTP/1.1\r\nHost: localhost\r\nContent-Length: 19\r\n\r\nbookTitle=Libro" + Thread.currentThread().getId();
                    socket.getOutputStream().write(request.getBytes());
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    assertTrue(response.contains("HTTP/1.1 200 OK"));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }
}
