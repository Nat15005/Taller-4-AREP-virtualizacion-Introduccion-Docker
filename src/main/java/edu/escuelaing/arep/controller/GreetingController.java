package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.annotations.GetMapping;
import edu.escuelaing.arep.annotations.RequestParam;
import edu.escuelaing.arep.annotations.RestController;

/**
 * REST controller for handling greeting requests.
 * This class provides an endpoint to greet users with a customizable name.
 */
@RestController
public class GreetingController {

    /**
     * Returns a greeting message.
     * This method handles GET requests to the "/greeting" endpoint and accepts an optional query parameter "name".
     * If no name is provided, it defaults to "World".
     *
     * @param name The name of the person to greet. Defaults to "World" if not provided.
     * @return A greeting message in the format "Hola, {name}!".
     */
    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola, " + name + "!";
    }
}