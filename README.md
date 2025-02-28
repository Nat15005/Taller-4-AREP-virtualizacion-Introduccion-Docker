# Web Framework Development for REST Services and Static File Management

This framework is a lightweight, concurrent web server built in Java, inspired by Apache, capable of serving HTML pages and PNG images. It includes an Inversion of Control (IoC) mechanism that allows building web applications using Plain Old Java Objects (POJOs). The server supports handling multiple concurrent requests and automatically detects annotated components to expose web services.

As part of the project, a demonstration web application was built to manage books, allowing users to add, delete, and list books through REST services. The application was then containerized using Docker, configured and deployed locally, and pushed to a repository on DockerHub. Finally, an AWS EC2 virtual machine was set up, Docker was installed, and the container was deployed successfully.

## Demo video
📌 [videoDemo](https://acortar.link/rx7zwM)

https://github.com/user-attachments/assets/0c4b8656-0f90-4711-b54d-0dc3c2a95707



## Getting Started

These instructions will guide you to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

To run this project, you need the following software installed on your local machine:

- **Java 21+**: The project is built using Java. 
- **Maven**: Used for dependency management.
- **IDE (optional)**: An Integrated Development Environment like IntelliJ IDEA can be used for development.

### Installing

Follow these steps to get the development environment running:

1. **Clone the repository:**
   ```bash
   git https://github.com/Nat15005/Taller-4-AREP-virtualizacion-Introduccion-Docker.git
   ```
2. **Navigate to the project folder:**
   ```bash
   cd AREP-Taller-4-AREP-virtualizacion-Introduccion-Docker
   ```
3. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

### Running the Server

Once the project is built, you can start the server with the following command:

```bash
java -cp target/classes edu.escuelaing.arep.Application
```

The server will start and listen on port `6100`.

### Accessing the Application

Open your web browser and go to:

```
http://localhost:6100/
```

You should see the main page of the application.

![image](https://github.com/user-attachments/assets/ee733dbf-a387-4ed7-b243-9d8bdeaf2666)

![image](https://github.com/user-attachments/assets/f2382c33-b777-4073-8d8f-9a355a512263)

### Example of GET REST Services
To test the functionality of the new REST framework, try the following endpoints in your browser:

- GET request with query parameters:
  
```  
http://localhost:35000/greeting?name=Natalia
```

This will return: Hello Natalia

![image](https://github.com/user-attachments/assets/c1872f0d-eb3f-46f6-b065-610f5c4ed404)

### Access the REST endpoints:

- GET /getBooks → List all books
- POST /addBook?bookTitle=Title&bookAuthor=Author → Add a new book
- DELETE /deleteBook?bookTitle=Title → Remove a book

### Static File Location Specification

The framework includes a staticfiles() method that allows developers to define where the static files (like images, CSS, and HTML) are located. By default, this method looks for static files in the /static folder. 

Once the staticfiles() method is configured, you can easily access static resources like CSS, PNG, and other files by simply making a request to the corresponding URL. For example, a request to:

```  
http://localhost:35000/index.css

```
![image](https://github.com/user-attachments/assets/b2592241-c83d-42f6-9443-05fb27035a8c)

This will return the index.css file

## Concurrency Improvements

To handle multiple simultaneous requests efficiently, the following enhancements were implemented:

1. Thread Pool for Request Handling
   - Implemented a fixed thread pool (size = 10) using ExecutorService.
   - Each incoming request is submitted to the thread pool for processing, preventing excessive thread creation.

   
 ``` java
private static final int THREAD_POOL_SIZE = 10; 
private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

while (isRunning) {
    try {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Nueva conexión aceptada: " + clientSocket.getInetAddress());

        threadPool.submit(() -> {
            try {
                RequestHandler.handleClient(clientSocket);
            } catch (IOException e) {
                System.err.println("Error al manejar la solicitud: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
                }
            }
        });
    } catch (IOException e) {
        if (isRunning) {
            System.err.println("Error al aceptar la conexión: " + e.getMessage());
        }
    }
} 
``` 
   

3. Graceful Shutdown Mechanism
   - A shutdown hook was added to properly close the thread pool and free resources when the server stops.
   - Ensures that pending tasks finish execution before shutting down the server.
   
 ``` java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    System.out.println("Apagando el servidor...");
    isRunning = false;
    threadPool.shutdown();
    try {
        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            threadPool.shutdownNow();
        }
    } catch (InterruptedException e) {
        threadPool.shutdownNow();
    }
    try {
        serverSocket.close();
    } catch (IOException e) {
        System.err.println("Error al cerrar el socket del servidor: " + e.getMessage());
    }
    System.out.println("Servidor cerrado.");
}));

``` 

4. Concurrent Data Management
   - Replaced the book list with ConcurrentHashMap to avoid race conditions.
   - Ensures that multiple requests can modify the book collection safely.

``` java
private static final ConcurrentHashMap<String, Book> books = new ConcurrentHashMap<>();

``` 

## Dockerization & Deployment

1. Add the Dockerfile to the Root of the Project
2. Build the Docker Image
    ```
   docker build --tag areptallercuatro .
   ```
    ![image](https://github.com/user-attachments/assets/1368e5db-cd88-4f6b-a313-28a2a505705e)


3. Create and Run Docker Containers

    ```
      docker run -d -p 34000:6000 areptallercuatro
      docker run -d -p 34001:6000 areptallercuatro2
      docker run -d -p 34002:6000 areptallercuatro3

      ```

    ![image](https://github.com/user-attachments/assets/5d2f8098-78a5-491d-b8bc-a657e87c6c34)
   
   
4. Verify the Application in the Browser
   - localhost:34000
     ![image](https://github.com/user-attachments/assets/56dc30f2-2fdc-46f6-831a-77352ada4d21)

   - localhost:34001
     ![image](https://github.com/user-attachments/assets/9d483793-b067-4c6b-9a91-83a6ece8e6cc)
   
   - localhost:34002
     ![image](https://github.com/user-attachments/assets/f3c86c58-0f00-4713-b2e9-d77fe358e7c9)

5. Then Upload the image to DockerHub, Creating a Repository on Docker Hub
   ![image](https://github.com/user-attachments/assets/692f4eab-b438-4da7-8036-708a86179584)

6. Tag the Local Docker Image

      ```
      docker tag areptallercuatro nat1505/areptaller4       
      ```
   
7. Push the Image to Docker Hub

   ```
    docker push nat1505/areptaller4:latest
    ```
8. Now is possible to deploy the Docker Container on AWS. First, Connect to your AWS EC2 instance using SSH:
   ```
    ssh -i your-key.pem ec2-user@your-ec2-public-ip
   ```
9. Install Docker on AWS
    ```
    sudo yum update -y
    sudo yum install docker
    ```

10. Start the Docker Service
      ```
      sudo service docker start
      ```
11. Add User to Docker Group
      ```
      sudo service docker start
      ```
12. Run a Container from the Docker Hub Image
      ```
      docker run -d -p 42000:6000 --name areptallercuatro nat1505/areptaller4
      ```
13. Configure AWS Security Group to Allow External Access

   - Go to the AWS EC2 Console.
   - Find your instance and open the Security Group settings.
   - Edit Inbound Rules and add a rule to allow traffic:
      - Type: Custom TCP
      - Port Range: 42000
      - Source: Anywhere (0.0.0.0/0)
14. Verify the Deployment
   ![image](https://github.com/user-attachments/assets/50d55eb4-56ef-4ae7-a810-c2a58cc2d434)


## Running Tests

To run the unit tests, use the following command:

```bash
mvn test
```
![image](https://github.com/user-attachments/assets/d8f2c103-dd3b-49c4-85c1-ccc378860db5)


### BookTest  
- **testGetTitle:** Verifies that the `getTitle` method correctly returns the title of the book.  
- **testGetAuthor:** Ensures that the `getAuthor` method returns the expected author name.  
- **testToString:** Checks that the `toString` method outputs the expected JSON representation of the book.  


### RequestTest

- testGetValues: Verifies that the getValues method correctly retrieves the value of a query parameter (in this case, "name") from the request.
- testGetValuesNotFound: Tests that the getValues method returns an empty string when a non-existent query parameter (in this case, "age") is requested.

### ResponseTest

- testDefaultContentType: Verifies that the default content type of a Response object is "text/plain".
- testSetContentType: Tests that setting a new content type updates the response correctly.
- testSetContentTypeToNull: Checks that setting the content type to null behaves as expected.
- testSetContentTypeToEmptyString: Verifies that setting the content type to an empty string updates the response accordingly.

### BookControllerTest
- testGetBooksInitiallyEmpty: Verifies that the initial book list is empty by checking the JSON response.
- testAddBookSuccessfully: Ensures that adding a book returns a success message and that the book appears in the stored list.
- testAddBookWithEmptyFields: Checks that trying to add a book with empty title and author returns an appropriate error message.
- testDeleteBookSuccessfully: Confirms that deleting an existing book returns a success message.
- testDeleteBookNotFound: Verifies that trying to delete a non-existent book returns an error message.

### GreetingControllerTest
- testGreetingWithName: Ensures that greeting a specific name (e.g., "Carlos") returns the expected greeting message.
- testGreetingWithDefaultName: Verifies that greeting with the default name ("World") returns the correct greeting.

### WebFrameworkTest
- testStaticFilesConfiguration: Ensures that the static file directory is correctly set.
- testGetRouteRegistrationAndExecution: Checks that a GET route can be registered and executed correctly.
- testPostRouteRegistrationAndExecution: Verifies that a POST route can be registered and executed as expected.
- testDeleteRouteRegistrationAndExecution: Ensures that a DELETE route can be registered and executed properly.
- testHandleRequestForRegisteredGetRoute: Confirms that the framework correctly handles requests to a registered GET route.
- testHandleRequestForUnknownRoute: Ensures that requesting an unknown route returns a 404 error.
- testHandleRequestForPostMethod: Checks that a registered POST route is handled correctly.
- testHandleRequestForDeleteMethod: Verifies that a registered DELETE route is handled successfully.
- testHandleRequestForUnsupportedMethod: Ensures that an unsupported HTTP method (e.g., PUT) returns a 405 error.

### HttpServerTest  
- **testConcurrentRequests:** Simulates multiple concurrent GET requests to `/getBooks` and verifies that the server handles them correctly by responding with HTTP 200.  
- **testConcurrentPostAndDeleteRequests:** Sends concurrent POST requests to add books and DELETE requests to remove them, ensuring that the server processes concurrent modifications safely.  


### Project Structure

```
AREP-Taller-3
├───src
│   ├───main
│   │   ├───java
│   │   │   └───edu
│   │   │       └───escuelaing
│   │   │           └───arep
│   │   │               │   Application.java
│   │   │               │
│   │   │               ├───annotations
│   │   │               │       DeleteMapping.java
│   │   │               │       GetMapping.java
│   │   │               │       PostMapping.java
│   │   │               │       RequestParam.java
│   │   │               │       RestController.java
│   │   │               │
│   │   │               ├───controller
│   │   │               │       BookController.java
│   │   │               │       GreetingController.java
│   │   │               │       Request.java
│   │   │               │       RequestHandler.java
│   │   │               │       Response.java
│   │   │               │
│   │   │               ├───model
│   │   │               │       Book.java
│   │   │               │
│   │   │               └───server
│   │   │                       FileHandler.java
│   │   │                       HttpServer.java
│   │   │                       WebFramework.java
│   │   │
│   │   └───resources
│   │       ├───prueba
│   │       │       index.html
│   │       │
│   │       └───static
│   │               fondo.jpg
│   │               index.css
│   │               index.html
│   │               index.js
│   │               pato.png
│   │

```

#### 📚 Book:
Represents a book with a title and an author. Provides methods to access book details and return a JSON representation.

#### 📂 FileHandler:
Handles file reading and serving in the HTTP server. Serves static files from a configured directory and determines MIME types.

#### 🌐 HttpServer:
Implements an HTTP server listening on port 35000. Manages REST requests and serves static files.

#### 📩 Request:
Represents an HTTP request, storing query parameters and providing methods to retrieve their values.

#### 🔄 RequestHandler:
Manages client HTTP requests, including handling books (GET, POST, DELETE) and serving static files.

#### 📤 Response:
Represents an HTTP response, allowing content type configuration.

### Technologies Used

- **Java** - Main programming language
- **Maven** - Dependency management and build tool
- **JUnit** - For unit testing
- **HTML, CSS, JavaScript** - Frontend components

### Author

Developed by **Natalia Rojas** https://github.com/Nat15005.

### Acknowledgments

- Java and Networking Documentation - For offering essential references on socket programming.

- Open Source Community - For tools and resources that helped in the development of this project.


