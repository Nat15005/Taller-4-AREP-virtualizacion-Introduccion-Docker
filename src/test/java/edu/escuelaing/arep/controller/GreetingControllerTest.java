package edu.escuelaing.arep.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreetingControllerTest {

    private GreetingController greetingController;

    @BeforeEach
    void setUp() {
        greetingController = new GreetingController();
    }

    @Test
    void testGreetingWithName() {
        String response = greetingController.greeting("Carlos");
        assertEquals("Hola, Carlos!", response);
    }

    @Test
    void testGreetingWithDefaultName() {
        String response = greetingController.greeting("World");
        assertEquals("Hola, World!", response);
    }
}