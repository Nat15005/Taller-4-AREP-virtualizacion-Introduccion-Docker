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
 * Clase que implementa un servidor HTTP concurrente usando un pool de threads.
 */
public class HttpServer {

    private static final int PORT = 35000; // Puerto en el que escucha el servidor
    private static final int THREAD_POOL_SIZE = 10; // Tamaño del pool de threads
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Pool de threads
    private static boolean isRunning = true; // Bandera para controlar el bucle del servidor

    /**
     * Inicia el servidor HTTP.
     *
     * @throws IOException Si ocurre un error al crear el servidor o aceptar las conexiones de los clientes.
     */
    public static void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        WebFramework.registerControllers(new BookController());
        System.out.println("Servidor escuchando en el puerto " + PORT);

        // shutdown hook para cerrar el servidor de manera segura
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando el servidor...");
            isRunning = false; // Detener el bucle del servidor
            threadPool.shutdown(); // Apagar el pool de threads
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow(); // Forzar el cierre si no termina en 60 segundos
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            try {
                serverSocket.close(); // Cerrar el socket del servidor
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket del servidor: " + e.getMessage());
            }
            System.out.println("Servidor cerrado.");
        }));

        // Bucle principal del servidor
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept(); // Aceptar una nueva conexión
                System.out.println("Nueva conexión aceptada: " + clientSocket.getInetAddress());

                // Enviar la solicitud al pool de threads para su procesamiento
                threadPool.submit(() -> {
                    try {
                        RequestHandler.handleClient(clientSocket); // Manejar la solicitud
                    } catch (IOException e) {
                        System.err.println("Error al manejar la solicitud: " + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close(); // Cerrar el socket del cliente
                        } catch (IOException e) {
                            System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error al aceptar la conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Detiene el servidor.
     */
    public static void stop() {
        isRunning = false; // Detener el bucle del servidor
    }
}