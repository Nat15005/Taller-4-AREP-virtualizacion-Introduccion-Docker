package edu.escuelaing.arep;

import edu.escuelaing.arep.annotations.*;
import edu.escuelaing.arep.controller.Request;
import edu.escuelaing.arep.server.HttpServer;
import edu.escuelaing.arep.server.WebFramework;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;

/**
 * Main class that starts the web server and automatically loads controllers.
 */
public class Application {

    /**
     * Main method that starts the web server.
     *
     * @param args Command-line arguments. The first argument can be the POJO class to load.
     */
    public static void main(String[] args) {
        try {
            // Configure the static files directory
            WebFramework.staticfiles("static");

            // Load POJO from the command line (if provided)
            if (args.length > 0) {
                String className = args[0];
                System.out.println("Cargando POJO desde la línea de comandos: " + className);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(RestController.class)) {
                    registerController(clazz);
                } else {
                    System.err.println("La clase " + className + " no está anotada con @RestController.");
                }
            } else {
                // Automatically load controllers if no POJO is specified
                System.out.println("Cargando controladores automáticamente...");
                loadControllers("edu.escuelaing.arep.controller");
            }
            HttpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Automatically loads classes annotated with @RestController in the specified package.
     *
     * @param packageName Name of the package where the controllers are located.
     * @throws Exception If an error occurs while loading the classes.
     */
    private static void loadControllers(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        if (resource.getProtocol().equals("file")) {
            // If running outside a JAR, use File
            File directory = new File(resource.toURI());
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    loadClass(packageName, file.getName());
                }
            }
        } else if (resource.getProtocol().equals("jar")) {
            // If running inside a JAR, use the JAR's resource list
            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!")); // Extract the actual JAR path
            try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath)) {
                for (java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                    String entryName = entries.nextElement().getName();
                    if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                        loadClass(packageName, entryName.substring(path.length() + 1));
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("Unsupported protocol: " + resource.getProtocol());
        }
    }

    /**
     * Loads a class dynamically from the specified package.
     *
     * @param packageName The package where the class is located.
     * @param fileName    The name of the class file.
     * @throws Exception If an error occurs while loading the class.
     */
    private static void loadClass(String packageName, String fileName) throws Exception {
        String className = packageName + '.' + fileName.replace(".class", "");
        Class<?> clazz = Class.forName(className);
        if (clazz.isAnnotationPresent(RestController.class)) {
            registerController(clazz);
        }
    }

    /**
     * Registers methods annotated with @GetMapping, @PostMapping, and @DeleteMapping in the framework.
     *
     * @param clazz The controller class.
     * @throws Exception If an error occurs while registering the methods.
     */
    private static void registerController(Class<?> clazz) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                String route = annotation.value();
                WebFramework.get(route, (req, res) -> invokeMethod(method, clazz, req));
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping annotation = method.getAnnotation(PostMapping.class);
                String route = annotation.value();
                WebFramework.post(route, (req, res) -> invokeMethod(method, clazz, req));
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
                String route = annotation.value();
                WebFramework.delete(route, (req, res) -> invokeMethod(method, clazz, req));
            }
        }
    }

    /**
     * Invokes a method from a controller and returns the response.
     *
     * @param method The method to invoke.
     * @param clazz  The controller class.
     * @param req    The HTTP request object.
     * @return The response as a string.
     */
    private static String invokeMethod(Method method, Class<?> clazz, Request req) {
        try {
            Object[] args = new Object[method.getParameterCount()];
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                    RequestParam paramAnnotation = parameters[i].getAnnotation(RequestParam.class);
                    String paramValue = req.getValues(paramAnnotation.value());
                    args[i] = paramValue != null ? paramValue : paramAnnotation.defaultValue();
                }
            }
            return (String) method.invoke(clazz.getDeclaredConstructor().newInstance(), args);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method", e);
        }
    }
}