# 🎲 Poker Dice

**Multiplayer Web Application for the Poker Dice game**

Poker Dice is a multiplayer web application that enables the creation and management of lobbies where multiple players can participate in real-time Poker Dice matches.

The project was developed with a focus on:

* Modular structuring 
* Clear separation of concerns
* Well-defined request processing pipeline
* Secure authentication and session management
* Reactive communication between backend and frontend
* Consistent transactional organization of business logic

---

# 🏗️ General Architecture

The application is divided into multiple Docker containers:

* **Nginx** – Reverse proxy
* **Backend API** – Kotlin + Spring
* **Frontend** – React + TypeScript

**Nginx** acts as the single entry point, reverse-proxying requests to internal containers, which allows for:

* Clear separation of concerns
* Service isolation
* Future scalability
* Centralized routing configuration

---

# 🖥️ Frontend

## ⚛️ React + TypeScript

The frontend was developed using:

* **React** for component-based interface construction
* **TypeScript** for static typing and greater robustness
* State management driven by the backend game state

---

## ⚡ Vite

We used **Vite** as our development and bundling tool.

Vite was essential for:

* Optimized application bundling
* TypeScript transpilation
* Hot Module Replacement (HMR)
* Production-optimized build
* Extremely fast development server

Practical advantages in the project:

* Significant reduction in reload time
* Simplified build pipeline
* Better modular organization of the frontend

---

# 🧠 Backend

## 🔹 Kotlin + Spring

The backend was developed using:

* **Kotlin**
* **Spring Boot**
* Layer-separated architecture
* Organization by domain, repositories, services, and controllers

---

# 🔄 Request Processing Pipeline

The pipeline defined for each HTTP request follows this sequence:

1. HTTP Server (container)
2. `HttpServlet`
3. Global HTTP Filter (executed before Spring)
4. Spring Interceptors
5. Controllers
6. Services
7. Repositories

This approach made it possible to:

* Separate authentication from business logic
* Validate fields before they reach the services
* Centralize permission checks
* Ensure consistency in error handling

---

# 🔐 Authentication and Session Management

## 🍪 Cookie System

A custom authentication system was implemented based on:

* Manually configured cookies
* Tokens stored in the database
* Automatic expiration after 24 hours
* Token replacement and invalidation system

### Process:

1. Login → token generation
2. Token stored in the database
3. Token hash saved
4. Cookie sent to the browser
5. Interceptor validates the token on every request

---

## 🔑 Cookies vs Tokens (Advantages of the adopted approach)

### Cookies (used in the project)

* Automatic transmission by the browser
* Stronger integration with HTTP mechanisms
* Ability to use `HttpOnly` and `Secure` flags
* Better control within a traditional web context

In this project, combining cookies with persisted tokens provided:

* Full session control
* Manual token revocation
* Guaranteed expiration
* Enhanced security control

---

# 🔐 Encryption and Security

## Hashing with SHA-256

Token encryption was handled using SHA-256:

### Strategy adopted:

* Never store tokens in plain text
* Store only the hash
* Compare the hash of the sent token with the stored hash
* Passwords handled in the same manner

This ensures:

* Reduced impact in the event of a data leak
* Impossibility of directly reconstructing the original token

---

# 🗄️ Data Persistence

## JDBI + Transaction Manager

The persistence layer was implemented using **JDBI**.

### Handle

The `Handle` represents an active connection to the database within a transaction.

Each repository receives the same `Handle`, ensuring:

* Transactional consistency
* Atomic operations
* Context isolation

---

## Transaction Manager

```kotlin
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {

    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
