package edu.escuelaing.arep.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BookTest {

    @Test
    public void testGetTitle() {
        Book book = new Book("El Quijote", "Miguel de Cervantes");
        assertEquals("El Quijote", book.getTitle());
    }

    @Test
    public void testGetAuthor() {
        Book book = new Book("El Quijote", "Miguel de Cervantes");
        assertEquals("Miguel de Cervantes", book.getAuthor());
    }

    @Test
    public void testToString() {
        Book book = new Book("El Quijote", "Miguel de Cervantes");
        String expected = "{\"title\":\"El Quijote\", \"author\":\"Miguel de Cervantes\"}";
        assertEquals(expected, book.toString());
    }
}
