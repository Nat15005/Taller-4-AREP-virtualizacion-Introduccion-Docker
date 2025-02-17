document.addEventListener("DOMContentLoaded", function () {
    const bookList = document.getElementById("book-list");
    const addBookForm = document.getElementById("add-book-form");
    const bookTitleInput = document.getElementById("book-title");
    const bookAuthorInput = document.getElementById("book-author");

    /**
     * Carga los libros almacenados en el servidor y los muestra en la tabla.
     * Realiza una solicitud GET a "/getBooks" y actualiza la lista de libros.
     */
    function loadBooks() {
        fetch("/getBooks")
            .then(response => response.json())
            .then(data => {
                bookList.innerHTML = ""; // Limpiar la lista antes de agregar libros
                if (data.books.length === 0) return; // Evitar agregar elementos vacíos

                data.books.forEach(book => {
                    if (book.title.trim() === "" || book.author.trim() === "") return; // Evitar agregar entradas vacías
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td><span class="book-title">${book.title}</span></td>
                        <td>${book.author}</td>
                        <td><button class="delete-btn">Delete</button></td>
                    `;
                    bookList.appendChild(tr);
                });
            })
            .catch(error => console.error("Error al cargar los libros:", error));
    }

    // Llamar a la función al cargar la página para obtener los libros existentes
    loadBooks();

    /**
     * Maneja la eliminación de un libro cuando se hace clic en el botón "Delete".
     * Realiza una solicitud DELETE a "/deleteBook" con el título del libro.
     * @param {Event} event - Evento de clic en la lista de libros.
     */
    function deleteBook(event) {
        // Verificar que el evento fue disparado por un botón de eliminar
        if (event.target.classList.contains("delete-btn")) {
            const bookName = event.target.parentElement.parentElement.querySelector(".book-title").textContent.trim();

            const url = `/deleteBook?bookTitle=${encodeURIComponent(bookName)}`;

                    fetch(url, {
                        method: "DELETE",
                        headers: { "Content-Type": "application/json" }
                    })
                    .then(response => {
                        if (!response.ok) throw new Error("Error al eliminar el libro.");
                        return response.text();
                    })
                    .then(data => {
                        console.log("Respuesta del servidor:", data);
                        loadBooks(); // Recargar la lista después de eliminar
                    })
                    .catch(error => console.error("Error al eliminar el libro:", error));
                }
            }

    // Agregar evento de clic a la lista de libros para manejar eliminaciones
    bookList.addEventListener("click", deleteBook);

    /**
     * Maneja el envío del formulario para añadir un nuevo libro.
     * Realiza una solicitud POST a "/addBook" con los datos del nuevo libro.
     * @param {Event} event - Evento de envío del formulario.
     */
    addBookForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Evitar que el formulario recargue la página

        const bookTitle = bookTitleInput.value.trim(); // Obtener el título del libro
        const bookAuthor = bookAuthorInput.value.trim(); // Obtener el autor del libro

        // Validar que los campos no estén vacíos
        if (bookTitle === "" || bookAuthor === "") {
            alert("El título y el autor del libro no pueden estar vacíos.");
            return;
        }

        // Enviar el nuevo libro al servidor mediante POST
        fetch(`/addBook?bookTitle=${encodeURIComponent(bookTitle)}&bookAuthor=${encodeURIComponent(bookAuthor)}`, {
            method: "POST"
        })
        .then(response => response.text())
        .then(data => {
            console.log("Respuesta del servidor:", data);
            loadBooks(); // Recargar la lista después de añadir
            bookTitleInput.value = ""; // Limpiar el campo del título
            bookAuthorInput.value = ""; // Limpiar el campo del autor
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Ocurrió un error al añadir el libro.");
        });

    });
});