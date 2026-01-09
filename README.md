# GreenRide: Distributed Carpooling Platform

This project demonstrates a robust **Distributed System** architecture comprising two independent microservices communicating via REST APIs, containerized with Docker, and secured with a hybrid stateful/stateless authentication mechanism.

---

## System Architecture

1.  **GreenRide Service (Core)**
    * **Role:** The main application handling user management, ride booking, and the web interface.
    * **Tech:** Spring Boot Web (MVC), Spring Data JPA, Thymeleaf, Spring Security.
    * **Database:** PostgreSQL (Production/Docker) or H2 (Dev).
    * **Security:** Hybrid architecture—Session-based for Web UI, JWT for REST API.

2.  **HUA-NOC Service (External)**
    * **Role:** An independent "Network Operations Center" service that handles SMS notifications and phone number validation.
    * **Tech:** Spring Boot Web, Caffeine Cache, Third-party integrations (Routee/Mock).
    * **Communication:** GreenRide talks to HUA-NOC via HTTP/REST.

---

##Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.5.7
* **Build Tool:** Maven
* **Containerization:** Docker & Docker Compose
* **Database:** PostgreSQL 15 (Docker/Prod)
* **Frontend:** Thymeleaf + Bootstrap
* **API Documentation:** SpringDoc OpenAPI (Swagger UI)

---

## Quick Start (Docker Compose)

The easiest way to run the full system (App + SMS Service + Database) is using Docker.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running.

### Steps
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/gkoulis/DS-Lab-NOC.git](https://github.com/gkoulis/DS-Lab-NOC.git)
    cd distributed
    ```

2.  **Build and Run:**
    ```bash
    docker-compose up --build
    ```
    *This creates the network `greenride-net`, spins up a PostgreSQL container, and launches both Java services.*

3.  **Access the Application:**
    * **GreenRide UI:** [http://localhost:8080](http://localhost:8080)
    * **Swagger API (GreenRide):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    * **Swagger API (NOC):** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

4.  **Stop the System:**
    ```bash
    docker-compose down
    ```
