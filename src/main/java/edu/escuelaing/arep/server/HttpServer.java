package edu.escuelaing.arep.server;

import edu.escuelaing.arep.controller.BookController;
import edu.escuelaing.arep.controller.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implements a concurrent HTTP server using a thread pool.
 * This class listens for incoming client connections and processes requests concurrently.
 */
public class HttpServer {

    private static final int PORT = 6100; // Port on which the server listens
    private static final int THREAD_POOL_SIZE = 10; // Size of the thread pool
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Pool de threads
    private static boolean isRunning = true; // Flag to control the server loop

    /**
     * Starts the HTTP server.
     *
     * @throws IOException If an error occurs while creating the server or accepting client connections.
     */
    public static void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        WebFramework.registerControllers(new BookController());
        System.out.println("Server listening on port " + PORT);

        // Shutdown hook to safely shut down the server

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the server...");
            isRunning = false; // Stop the server loop
            threadPool.shutdown(); // Shut down the thread pool
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow(); // Force shutdown if not terminated in 60 seconds
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
            System.out.println("Server closed.");
        }));

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept(); // Aceptar una nueva conexión
                System.out.println("New connection accepted: " + clientSocket.getInetAddress());

                // Submit the request to the thread pool for processing
                threadPool.submit(() -> {
                    try {
                        RequestHandler.handleClient(clientSocket);
                    } catch (IOException e) {
                        System.err.println("Error handling request: " + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Stops the server.
     */
    public static void stop() {
        isRunning = false;
    }
}