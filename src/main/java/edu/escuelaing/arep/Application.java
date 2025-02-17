package edu.escuelaing.arep;

import edu.escuelaing.arep.annotations.*;
import edu.escuelaing.arep.controller.Request;
import edu.escuelaing.arep.server.HttpServer;
import edu.escuelaing.arep.server.WebFramework;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase principal que inicia el servidor web y carga automáticamente los controladores.
 */
public class Application {

    /**
     * Metodo principal que inicia el servidor web.
     *
     * @param args Argumentos de línea de comandos. El primer argumento puede ser la clase del POJO a cargar.
     */
    public static void main(String[] args) {
        try {
            // Configurar la carpeta de archivos estáticos
            WebFramework.staticfiles("src/main/resources/static");

            // Cargar POJO desde la línea de comandos (si se proporciona)
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
                // Cargar automáticamente los controladores si no se especifica un POJO
                System.out.println("Cargando controladores automáticamente...");
                loadControllers("edu.escuelaing.arep.controller");
            }

            // Iniciar el servidor
            HttpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Carga automáticamente las clases anotadas con @RestController en el paquete especificado.
     *
     * @param packageName Nombre del paquete donde se encuentran los controladores.
     * @throws Exception Si ocurre un error al cargar las clases.
     */
    private static void loadControllers(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File directory = new File(resource.toURI());
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(RestController.class)) {
                    registerController(clazz);
                }
            }
        }
    }

    /**
     * Registra los métodos anotados con @GetMapping, @PostMapping y @DeleteMapping en el framework.
     *
     * @param clazz Clase del controlador.
     * @throws Exception Si ocurre un error al registrar los métodos.
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