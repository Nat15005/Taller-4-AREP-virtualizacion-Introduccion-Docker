package edu.escuelaing.arep.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookControllerTest {

    private BookController bookController;

    @BeforeEach
    void setUp() {
        bookController = new BookController();
    }

    @Test
    void testGetBooksInitiallyEmpty() {
        String result = bookController.getBooks();
        assertEquals("{ \"books\": [] }", result);
    }

    @Test
    void testAddBookSuccessfully() {
        String response = bookController.addBook("1984", "George Orwell");
        assertEquals("{\"message\": \"Libro añadido: 1984 por George Orwell\"}", response);

        String booksJson = bookController.getBooks();
        assertTrue(booksJson.contains("1984"));
        assertTrue(booksJson.contains("George Orwell"));
    }

    @Test
    void testAddBookWithEmptyFields() {
        String response = bookController.addBook("", "");
        assertEquals("{\"error\": \"El título y el autor no pueden estar vacíos.\"}", response);
    }

    @Test
    void testDeleteBookSuccessfully() {
        bookController.addBook("Cien años de soledad", "Gabriel García Márquez");
        String response = bookController.deleteBook("Cien años de soledad");
        assertEquals("{\"message\": \"Libro eliminado: Cien años de soledad\"}", response);
    }

    @Test
    void testDeleteBookNotFound() {
        String response = bookController.deleteBook("Libro inexistente");
        assertEquals("{\"error\": \"Libro no encontrado\"}", response);
    }
}

