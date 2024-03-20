# Core Banking Application

[![Build & Test](https://github.com/ktenman/core-banking/actions/workflows/ci.yml/badge.svg)](https://github.com/ktenman/core-banking/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/ktenman/core-banking/main/graph/badge.svg)](https://codecov.io/gh/ktenman/core-banking)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ktenman_core-banking&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=ktenman_core-banking)

## Introduction

This Core Banking application provides fundamental banking operations such as account management and transaction
processing, leveraging modern technologies and best practices for a robust and scalable solution.

## Prerequisites

Before you begin, ensure your system meets the following requirements:

- Java: v21.0.2
- Gradle: v8.5
- Docker: v25.0.2
- Docker Compose: v2.24.3

## Technical Stack

- **Backend**:
    - Spring Boot v3.2
    - MyBatis v3.0.3
    - MyBatis-Plus v3.5.5
- **API Documentation**: SpringDoc OpenAPI v2.4.0
- **Database**: PostgreSQL for data persistence and Flyway for database migration management.
- **Caching and Message Queues**:
    - Redis for caching.
    - RabbitMQ for message queues.
- **Testing**: A combination of MockMvc, Testcontainers, Mockito, AssertJ, and JUnit for robust testing coverage.

## Database Design

The application utilizes three main tables to manage accounts, balances, and transactions. Below is an overview of each
table and their relationships:

### Tables Overview

- **`account`**: Stores information about bank accounts.
    - **Columns**:
        - `id`: The primary key.
        - `reference`: Unique reference for the account.
        - `country_code`: Country code associated with the account.
        - `customer_id`: ID of the customer owning the account.
        - `created_at` & `updated_at`: Timestamps for record creation and updates.

- **`balance`**: Contains balance information for each account, with support for multiple currencies.
    - **Columns**:
        - `id`: The primary key.
        - `account_id`: Reference to the associated account.
        - `currency`: Currency of the balance.
        - `available_amount`: Available balance amount.
        - `created_at` & `updated_at`: Timestamps for record creation and updates.

- **`transaction`**: Stores transaction details for each account.
    - **Columns**:
        - `id`: The primary key.
        - `account_id`: Reference to the associated account.
        - `balance_id`: Reference to the associated balance.
        - `amount`: Transaction amount.
        - `direction`: Transaction direction (IN or OUT).
        - `description`: Description of the transaction.
        - `balance_after_transaction`: Balance after the transaction.
        - `currency`: Currency of the transaction.
        - `reference`: Unique reference for the transaction.
        - `created_at` & `updated_at`: Timestamps for record creation and updates.

### Relationships

- An **account** can have multiple **balances**, each in a different currency. This is a one-to-many relationship.
- An **account** can have multiple **transactions**, establishing a one-to-many relationship.
- Each **transaction** is associated with a specific **balance**, representing a many-to-one relationship.

## API Documentation

The application provides comprehensive API documentation using OpenAPI 3.0. You can access this documentation at:

- When running the application with Docker
  Compose: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Setup and Running Instructions

### Docker Containers Setup

Initialize necessary Docker containers with Docker Compose to ensure the database, Redis, and RabbitMQ services are up
before proceeding:

```bash
docker-compose -f compose.yaml up -d
```

This step is crucial, especially before moving to production build, to ensure all dependent services are available.

### Application Setup

Navigate to the project directory and compile the Java application using Gradle:

```bash
./gradlew clean build
./gradlew bootRun
```

### Running in Production

To run the application in production, you can use Docker Compose. This simplifies the deployment process by
orchestrating the startup of your application and its dependencies with a single command.

### Steps:

1. **Prepare the Environment:** Ensure all configuration files are correctly set up for production. This may include
   adjusting `application.yml` or `docker-compose.yml` as needed.
2. **Build and Run with Docker Compose:** Navigate to the root directory of your project where the `docker-compose.yml`
   file is located. Run the following command:

```bash
docker-compose -f docker-compose.yml up -d
```

This command will build the application Docker image and start all services defined in your `docker-compose.yml` file in
detached mode, running in the background.

3. **Verify the Deployment:** Ensure all services are up and running. You can check the status of your Docker containers
   by executing:

```bash
docker ps
```

For logs and troubleshooting, use:

```bash
docker-compose -f docker-compose.yml logs -f <service_name>
```

Replace `<service_name>` with the name of the service you want to inspect.

### Updating the Application

To update the application or its services after making changes:

1. **Rebuild the services:**

```bash
docker-compose -f docker-compose.yml build
```

2. **Restart the services for the changes to take effect:**

```bash
docker-compose -f docker-compose.yml up -d
```

## Continuous Integration and Deployment

- A CI pipeline via GitHub Actions in the `.github` folder automates unit and integration tests.
- **Dependabot** keeps Gradle and GitHub Actions versions up-to-date, automating dependency management.
- **Codecov** is used for code coverage analysis, ensuring that the application maintains a high level of test coverage.
- **SonarQube** is integrated for continuous code quality inspection, helping identify and address code smells,
  vulnerabilities, and maintainability issues.
- A **quality gate** is set to enforce a minimum of **60% test coverage** on new code, ensuring that new functionality
  is adequately tested before merging into the main branch.

## Note on Development Tools

- **Project Lombok** minimizes boilerplate code, improving code clarity and maintainability.
- This project is utilizing **Docker Compose support** introduced in Spring Boot 3.1.
- The `@Loggable` annotation is used to automatically log method invocations and their durations, enhancing
  observability and troubleshooting capabilities.

## Key Features

- **Account Management:** Create and retrieve accounts with associated balances.
- **Transaction Processing:** Perform deposit and withdrawal transactions on accounts.
- **Distributed Locking:** Implements Redis-based distributed locking for concurrency control.

## Performance Estimates

Performance testing was conducted using Locust (version 2.24.0) on a **MacBook Pro 16-inch 2021** with an Apple **M1 Max** 
chip and 64 GB memory, the account application can handle approximately **300 requests per second** with logging and
locking enabled. If logging and locking are removed from the create transaction endpoint, the application can handle
around 600 requests per second. However, when the load is increased to 700 requests per second, a small percentage 
(around 0.07%) of requests start failing.

## Explanation of Important Choices

In the provided solution, several important choices were made to ensure the application's efficiency, scalability, and
maintainability. Here are some key points:

1. **Architecture:** The application follows a layered architecture, separating concerns into controllers, services, and
   data access layers. This promotes modularity, reusability, and easier testing.
2. **Database:** PostgreSQL was chosen as the database for its robustness, reliability, and strong support for ACID
   transactions. It ensures data integrity and provides a solid foundation for the banking application.
3. **MyBatis:** MyBatis was selected as the persistence framework for its simplicity, performance, and ease of use. It
   provides a clean separation between SQL queries and Java code, making the codebase more readable and maintainable.
4. **Redis:** Redis is used for distributed locking to ensure data consistency and prevent race conditions in a
   concurrent environment. It offers high performance and scalability, making it suitable for handling a large number of
   concurrent transactions.
5. **RabbitMQ:** RabbitMQ is employed as the message broker to publish account and transaction events. This enables
   loose coupling between services and facilitates asynchronous communication, allowing for better scalability and fault
   tolerance.
6. **Testing:** The application places a strong emphasis on testing, with a combination of unit tests and integration
   tests. Testcontainers is used to spin up Docker containers for integration testing, providing a realistic testing
   environment. MockMvc is utilized for testing RESTful endpoints, ensuring API correctness.
7. **Logging:** Logging is implemented using the @Loggable annotation, which automatically logs method invocations and
   their durations. This enhances observability and aids in troubleshooting and performance monitoring.
8. **Exception Handling:** A global exception handler is implemented to provide consistent and informative error
   responses to API clients. It handles various exception scenarios and returns appropriate HTTP status codes and error
   messages.
9. **API Documentation:** The application leverages SpringDoc OpenAPI to generate comprehensive API documentation. This
   allows developers to easily understand and interact with the API endpoints.
10. **Docker:** Docker is used to containerize the application, database, and RabbitMQ, enabling easy deployment and
    portability across different environments. Docker Compose is employed to orchestrate the containers and simplify the
    setup process.

## Considerations for Horizontal Scaling

To support horizontal scaling and handle increased traffic, the application can be scaled in several ways:

1. **Load Balancing:** Implement a load balancer to distribute incoming requests evenly across multiple instances of the
   account application. This ensures optimal resource utilization and prevents any single instance from becoming a
   bottleneck.
2. **Stateless Design:** Ensure that the application is stateless, meaning that each request can be processed
   independently without relying on server-side session state. This allows for easier horizontal scaling and enables the
   load balancer to route requests to any available instance.
3. **Database Scalability:**

   3.1. **Read Replicas:** Utilize multiple read replica databases to handle read-heavy workloads. Read replicas are
   separate database instances that receive real-time updates from the primary database and can serve read queries. This
   reduces the load on the primary database and improves read performance.

   3.2 **Database Sharding:** Consider implementing database sharding, which involves partitioning the data across
   multiple database instances based on a sharding key (e.g., account ID). Each shard holds a subset of the data,
   allowing for distributed data storage and processing. Sharding can significantly improve write performance and
   overall scalability.

4. **Distributed Caching:**

   4.1. **Redis Cluster:** Implement a distributed Redis caching solution using Redis Cluster. Redis Cluster allows you
   to distribute the caching load across multiple Redis instances, providing improved performance and fault tolerance.
   It automatically handles data sharding and replication, ensuring high availability and scalability.

   4.2. **Caching Strategies:** Employ appropriate caching strategies, such as read-through, write-through, or
   write-behind caching, depending on the application's read and write patterns. These strategies help in reducing the
   load on the database and improving response times.

5. **Asynchronous Processing:** Utilize asynchronous processing techniques, such as message queues (e.g., RabbitMQ), to
   decouple time-consuming tasks from the main request-response cycle. This allows the application to handle more
   concurrent requests and improves overall throughput.
6. **Monitoring and Alerting:** Implement robust monitoring and alerting systems to track application performance,
   resource utilization, and error rates. This enables proactive identification and resolution of performance
   bottlenecks and ensures the application remains healthy and responsive.
7. **Auto-scaling:** Leverage auto-scaling mechanisms provided by cloud platforms or container orchestration tools like
   Kubernetes. Auto-scaling allows the application to automatically adjust the number of instances based on predefined
   metrics, such as CPU utilization or request rate, ensuring optimal performance under varying load conditions.
8. **Efficient Resource Utilization:** Optimize the application's resource utilization by carefully tuning parameters
   such as thread pools, connection pools, and memory settings. Efficient resource management helps in handling a higher
   number of concurrent transactions without overloading the system.
9. **Distributed Tracing:** Implement distributed tracing to gain visibility into the flow of requests across multiple
   services. This helps in identifying performance bottlenecks, latency issues, and optimizing the overall system
   performance.
10. **Continuous Performance Testing:** Regularly conduct performance tests to assess the application's scalability
    and identify any performance degradation over time. This allows for proactive optimization and ensures the
    application can handle the expected transaction volume.

By leveraging read replica databases, distributed Redis caching with multiple instances, and implementing the other
scalability strategies mentioned above, the account application can be effectively scaled horizontally to handle a
higher volume of transactions. These approaches help in distributing the load, improving performance, and ensuring high
availability and fault tolerance.

---

This README aims to guide developers through setting up, running, and understanding the core functionalities and
technical aspects of the Core Banking application.
