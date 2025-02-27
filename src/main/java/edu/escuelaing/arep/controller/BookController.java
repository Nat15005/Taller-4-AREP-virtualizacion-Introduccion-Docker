package edu.escuelaing.arep.controller;

import edu.escuelaing.arep.annotations.*;
import edu.escuelaing.arep.model.Book;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for managing books.
 * This class handles CRUD (Create, Read, Delete) operations for books.
 */
@RestController
public class BookController {
    private static final ConcurrentHashMap<String, Book> books = new ConcurrentHashMap<>();

    /**
     * Retrieves the list of all books in JSON format.
     *
     * @return A JSON string containing the list of books.
     */
    @GetMapping("/getBooks")
    public String getBooks() {
        StringBuilder json = new StringBuilder("{ \"books\": [");
        books.values().forEach(book -> {
            json.append("{\"title\": \"").append(book.getTitle())
                    .append("\", \"author\": \"").append(book.getAuthor()).append("\"},");
        });
        if (!books.isEmpty()) {
            json.deleteCharAt(json.length() - 1); // Eliminar la última coma
        }
        json.append("] }");
        return json.toString();
    }

    /**
     * Adds a new book to the collection.
     *
     * @param title The title of the book.
     * @param author The author of the book.
     * @return A JSON message indicating whether the book was added successfully or if an error occurred.
     */
    @PostMapping("/addBook")
    public String addBook(@RequestParam(value = "bookTitle") String title,
                          @RequestParam(value = "bookAuthor") String author) {
        if (title.isEmpty() || author.isEmpty()) {
            return "{\"error\": \"El título y el autor no pueden estar vacíos.\"}";
        }

        String key = title.toLowerCase() + "|" + author.toLowerCase();

        // Verificar si el libro ya existe
        if (books.containsKey(key)) {
            return "{\"error\": \"El libro ya existe.\"}";
        }

        // Añadir el libro si no existe
        books.put(key, new Book(title, author));
        return "{\"message\": \"Libro añadido: " + title + " por " + author + "\"}";
    }

    /**
     * Deletes a book from the collection by its title.
     *
     * @param title The title of the book to delete.
     * @return A JSON message indicating whether the book was deleted successfully or if it was not found.
     */
    @DeleteMapping("/deleteBook")
    public String deleteBook(@RequestParam("bookTitle") String title) {
        if (title.isEmpty()) {
            return "{\"error\": \"El título no puede estar vacío.\"}";
        }
        // Buscar y eliminar el libro por título
        boolean removed = books.entrySet().removeIf(entry -> entry.getValue().getTitle().equalsIgnoreCase(title));
        return removed ? "{\"message\": \"Libro eliminado: " + title + "\"}" : "{\"error\": \"Libro no encontrado\"}";
    }
}