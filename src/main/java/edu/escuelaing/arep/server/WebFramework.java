package edu.escuelaing.arep.server;

import edu.escuelaing.arep.annotations.GetMapping;
import edu.escuelaing.arep.annotations.PostMapping;
import edu.escuelaing.arep.annotations.RestController;
import edu.escuelaing.arep.controller.Request;
import edu.escuelaing.arep.controller.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Framework web sencillo que permite manejar peticiones HTTP GET, POST y DELETE, y servir archivos estáticos.
 * Permite registrar rutas para manejar peticiones y especificar la ubicación de los archivos estáticos.
 */
public class WebFramework {

    // Ruta por defecto para los archivos estáticos
    private static String staticFolder;

    // Mapa que almacena las rutas GET, POST y DELETE registradas y sus manejadores
    static final Map<String, BiFunction<Request, Response, String>> getRoutes = new HashMap<>();
    static final Map<String, BiFunction<Request, Response, String>> postRoutes = new HashMap<>();
    static final Map<String, BiFunction<Request, Response, String>> deleteRoutes = new HashMap<>();

    /**
     * Permite configurar la ubicación de los archivos estáticos.
     * Si la aplicación se está ejecutando en Docker, usa una ruta específica.
     * Si no, usa la ruta local.
     *
     * @param folder Ruta donde se encuentran los archivos estáticos en el entorno local.
     */
    public static void staticfiles(String folder) {
        String dockerEnv = System.getenv("DOCKER_ENV");
        if (dockerEnv != null && dockerEnv.equals("true")) {
            // Ruta dentro del contenedor Docker
            staticFolder = "/usrapp/bin/" + folder;
        } else {
            // Ruta local
            staticFolder = "src/main/resources/" + folder;
        }
    }

    /**
     * Registra un nuevo endpoint GET en el framework.
     *
     * @param path    Ruta de la API.
     * @param handler Función lambda que maneja la petición.
     */
    public static void get(String path, BiFunction<Request, Response, String> handler) {
        getRoutes.put(path, handler);
    }

    /**
     * Registra un nuevo endpoint POST en el framework.
     *
     * @param path    Ruta de la API.
     * @param handler Función lambda que maneja la petición.
     */
    public static void post(String path, BiFunction<Request, Response, String> handler) {
        postRoutes.put(path, handler);
    }

    /**
     * Registra un nuevo endpoint DELETE en el framework.
     *
     * @param path    Ruta de la API.
     * @param handler Función lambda que maneja la petición.
     */
    public static void delete(String path, BiFunction<Request, Response, String> handler) {
        deleteRoutes.put(path, handler);
    }

    /**
     * Maneja las peticiones entrantes según el metodo y recurso solicitado.
     * Si la petición es un GET, POST o DELETE y la ruta está registrada, ejecuta el manejador correspondiente.
     * Si no, intenta servir un archivo estático.
     *
     * @param method      El metodo HTTP (por ejemplo, "GET", "POST", "DELETE").
     * @param resource    La ruta del recurso solicitado.
     * @param queryParams Parámetros de consulta de la URL.
     * @param out         El flujo de salida donde se enviará la respuesta.
     * @throws IOException Si ocurre un error al escribir en el flujo de salida.
     */

    public static void handleRequest(String method, String resource, Map<String, String> queryParams, String body, OutputStream out) throws IOException {
        Request req = new Request(queryParams);

        // Convertimos el body a un BufferedReader antes de asignarlo
        if (body != null && !body.isEmpty()) {
            req.setBodyReader(new BufferedReader(new StringReader(body)));
        }

        Response res = new Response();
        String responseBody;

        // Mapeamos la solicitud al controlador correspondiente
        BiFunction<Request, Response, String> handler = null;

        if ("GET".equalsIgnoreCase(method)) {
            if (getRoutes.containsKey(resource)) {
                handler = getRoutes.get(resource);
                responseBody = handler.apply(req, res);
            } else {
                // Intenta servir un archivo estático si la ruta no está en getRoutes
                FileHandler.serveFile(resource, out);
                return; // Importante: salir para evitar escribir otra respuesta
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            handler = postRoutes.getOrDefault(resource, (r, s) -> "404 Not Found");
            responseBody = handler.apply(req, res);
        }else if ("DELETE".equalsIgnoreCase(method)) {
                handler = deleteRoutes.getOrDefault(resource, (r, s) -> "{\"error\": \"Ruta no encontrada.\"}");
                responseBody = handler.apply(req, res);
        }
        else {
            responseBody = "405 Method Not Allowed";
        }

        // Construcción de la respuesta HTTP
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + responseBody.getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseBody;

        out.write(response.getBytes());
        out.flush();
    }



    /**
     * Obtiene la ruta de la carpeta de archivos estáticos configurada.
     *
     * @return La ruta de la carpeta de archivos estáticos.
     */
    public static String getStaticFolder() {
        return staticFolder;
    }

    public static void registerControllers(Object... controllers) {
        for (Object controller : controllers) {
            Class<?> clazz = controller.getClass();
            if (clazz.isAnnotationPresent(RestController.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        String path = method.getAnnotation(GetMapping.class).value();
                        get(path, (req, res) -> {
                            try {
                                return (String) method.invoke(controller);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        String path = method.getAnnotation(PostMapping.class).value();
                        post(path, (req, res) -> {
                            try {
                                // Extraer los parámetros de la solicitud
                                Object[] args = extractMethodArguments(method, req);
                                return (String) method.invoke(controller, args);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Extrae los argumentos para un método anotado, basándose en los parámetros de la solicitud.
     */
    private static Object[] extractMethodArguments(Method method, Request req) {
        Object[] args = new Object[method.getParameterCount()];
        int index = 0;

        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(edu.escuelaing.arep.annotations.RequestParam.class)) {
                edu.escuelaing.arep.annotations.RequestParam requestParam = parameter.getAnnotation(edu.escuelaing.arep.annotations.RequestParam.class);
                String paramName = requestParam.value();
                String defaultValue = requestParam.defaultValue();

                // Obtener valor del queryParams o usar el defaultValue si no está presente
                String value = req.getQueryParams().getOrDefault(paramName, defaultValue);
                args[index] = value;
            }
            index++;
        }
        return args;
    }

}