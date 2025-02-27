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
 * A simple web framework that handles HTTP GET, POST, and DELETE requests and serves static files.
 * It allows registering routes to handle requests and specifying the location of static files.
 */
public class WebFramework {

    private static String staticFolder;

    // Maps storing registered GET, POST, and DELETE routes with their respective handlers
    static final Map<String, BiFunction<Request, Response, String>> getRoutes = new HashMap<>();
    static final Map<String, BiFunction<Request, Response, String>> postRoutes = new HashMap<>();
    static final Map<String, BiFunction<Request, Response, String>> deleteRoutes = new HashMap<>();

    /**
     * Configures the location of static files.
     * If the application is running in Docker, it uses a specific path.
     * Otherwise, it uses the local path.
     *
     * @param folder The directory where static files are stored in the local environment.
     */
    public static void staticfiles(String folder) {
        String dockerEnv = System.getenv("DOCKER_ENV");
        if (dockerEnv != null && dockerEnv.equals("true")) {
            // Path inside the Docker container
            staticFolder = "/usrapp/bin/" + folder;
        } else {
            staticFolder = "src/main/resources/" + folder;
        }
    }

    /**
     * Registers a new GET endpoint in the framework.
     *
     * @param path    The API route.
     * @param handler Lambda function that handles the request.
     */
    public static void get(String path, BiFunction<Request, Response, String> handler) {
        getRoutes.put(path, handler);
    }

    /**
     * Registers a new POST endpoint in the framework.
     *
     * @param path    The API route.
     * @param handler Lambda function that handles the request.
     */
    public static void post(String path, BiFunction<Request, Response, String> handler) {
        postRoutes.put(path, handler);
    }

    /**
     * Registers a new DELETE endpoint in the framework.
     *
     * @param path    The API route.
     * @param handler Lambda function that handles the request.
     */
    public static void delete(String path, BiFunction<Request, Response, String> handler) {
        deleteRoutes.put(path, handler);
    }

    /**
     * Handles incoming requests based on the HTTP method and requested resource.
     * If the request is a GET, POST, or DELETE and the route is registered, it executes the corresponding handler.
     * Otherwise, it attempts to serve a static file.
     *
     * @param method      The HTTP method (e.g., "GET", "POST", "DELETE").
     * @param resource    The requested resource path.
     * @param queryParams Query parameters from the URL.
     * @param body        The request body, if applicable.
     * @param out         The output stream where the response will be sent.
     * @throws IOException If an error occurs while writing to the output stream.
     */
    public static void handleRequest(String method, String resource, Map<String, String> queryParams, String body, OutputStream out) throws IOException {
        Request req = new Request(queryParams);

        // Convert the body into a BufferedReader before assigning it
        if (body != null && !body.isEmpty()) {
            req.setBodyReader(new BufferedReader(new StringReader(body)));
        }
        Response res = new Response();
        String responseBody;

        // Map the request to the corresponding controller
        BiFunction<Request, Response, String> handler = null;

        if ("GET".equalsIgnoreCase(method)) {
            if (getRoutes.containsKey(resource)) {
                handler = getRoutes.get(resource);
                responseBody = handler.apply(req, res);
            } else {
                FileHandler.serveFile(resource, out);
                return;
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
     * Gets the configured static files folder path.
     *
     * @return The path of the static files' directory.
     */
    public static String getStaticFolder() {
        return staticFolder;
    }

    /**
     * Registers controllers that are annotated with @RestController.
     * Scans methods annotated with @GetMapping and @PostMapping to map them to their respective routes.
     *
     * @param controllers The controllers to be registered.
     */
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
     * Extracts the arguments for an annotated method based on request parameters.
     *
     * @param method The method to extract arguments for.
     * @param req    The request object containing query parameters.
     * @return An array of extracted method arguments.
     */
    private static Object[] extractMethodArguments(Method method, Request req) {
        Object[] args = new Object[method.getParameterCount()];
        int index = 0;

        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(edu.escuelaing.arep.annotations.RequestParam.class)) {
                edu.escuelaing.arep.annotations.RequestParam requestParam = parameter.getAnnotation(edu.escuelaing.arep.annotations.RequestParam.class);
                String paramName = requestParam.value();
                String defaultValue = requestParam.defaultValue();
                String value = req.getQueryParams().getOrDefault(paramName, defaultValue);
                args[index] = value;
            }
            index++;
        }
        return args;
    }

}