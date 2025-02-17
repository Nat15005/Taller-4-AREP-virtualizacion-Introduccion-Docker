package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.controller.Request;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestTest {

    @Test
    public void testGetValues() {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("name", "Pedro");
        Request request = new Request(queryParams);

        String value = request.getValues("name");
        assertEquals("Pedro", value, "El valor del parámetro 'name' debería ser 'Pedro'");
    }

    @Test
    public void testGetValuesNotFound() {
        HashMap<String, String> queryParams = new HashMap<>();
        Request request = new Request(queryParams);

        String value = request.getValues("age");
        assertEquals("", value, "El valor del parámetro 'age' debería ser una cadena vacía");
    }
}