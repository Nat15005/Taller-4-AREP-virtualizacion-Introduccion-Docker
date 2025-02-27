package edu.escuelaing.arep.model;

/**
 * Represents a book with a title and an author.
 * This class provides methods to access the book's details and a string representation in JSON format.
 */
public class Book {

    private String title;

    private String author;

    /**
     * Constructs a new Book with the specified title and author.
     *
     * @param title  The title of the book.
     * @param author The author of the book.
     */
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    /**
     * Retrieves the title of the book.
     *
     * @return The title of the book.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the author of the book.
     *
     * @return The author of the book.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns a string representation of the book in JSON format.
     *
     * @return A JSON string representing the book, e.g., {"title":"Title", "author":"Author"}.
     */
    @Override
    public String toString() {
        return "{\"title\":\"" + title + "\", \"author\":\"" + author + "\"}";
    }
}