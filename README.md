# User Aggregator

## Overview

The **User Aggregator** is a Spring Boot application designed to aggregate user data from multiple databases (PostgreSQL and MySQL). It provides RESTful APIs to fetch and add users across these databases. The application uses Flyway for database migrations and Testcontainers for integration testing.

### Key Features:
- Fetch users from PostgreSQL and MySQL databases.
- Add users to multiple databases simultaneously.
- Swagger UI for API documentation and testing.
- Docker Compose support for easy setup and deployment.

---

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17**
- **Maven**
- **Docker**
- **Docker Compose**

---

## Running the Application with Docker Compose

To run the application using Docker Compose, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/user-aggregator.git
   
   cd user-aggregator

   
2. **Change .env file**

    Environment Variables:
   - POSTGRES_URL: JDBC URL for PostgreSQL.
   - POSTGRES_USER: Username for PostgreSQL.
   - POSTGRES_PASSWORD: Password for PostgreSQL.
   - MYSQL_URL: JDBC URL for MySQL.
   - MYSQL_USER: Username for MySQL.
   - MYSQL_PASSWORD: Password for MySQL.

3. **Build the application:**
   ```bash
   mvn clean package

4. **Run Docker Compose:**

    ```bash
    docker-compose up --build
    ```
This command will:
   - Start a PostgreSQL container.
   - Start a MySQL container.
   - Build and start the user-aggregator Spring Boot application.

5. **Access the application:**
   - The application will be available at http://localhost:8080.
   - Swagger UI will be available at http://localhost:8080/swagger-ui.html.

## Running Tests
To run the tests, execute the following command:
   ```bash
   mvn test
```
  The tests use Testcontainers to spin up PostgreSQL and MySQL containers for integration testing.

## API Documentation (Swagger UI)
The application includes Swagger UI for API documentation and testing. After starting the application, you can access Swagger UI at:
   ```bash
     http://localhost:8080/swagger-ui.html
```

## Example API Endpoints:

- **GET `/api/v1/users`**: Fetch all users from both databases.
- **POST `/api/v1/users/add`**: Add a new user to all databases.


