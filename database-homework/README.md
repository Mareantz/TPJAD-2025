# Java Database Homework: Multi-Database E-Commerce Application

**Student:** Ciotir Marian-Augustin 
**Group:** 244

---

## 1. Introduction

This document describes a Java application built to fulfill the requirements of the database homework. The primary goal is to demonstrate the ability to connect a single Java application ecosystem to three different database systems (PostgreSQL, OracleDB, and Microsoft SQL Server), with each database holding different, but related, tables.

The application must implement all four **CRUD** (Create, Read, Update, Delete) actions and provide a graphical user interface (GUI) to interact with the data.

---

## 2. System Architecture

To meet the requirements in a modern, robust, and scalable way, a **Microservice Architecture** was adopted. This solution is superior to a single monolithic application as it decouples each database's logic, preventing complex configuration conflicts and allowing each service to be managed independently.

The system consists of four distinct Spring Boot applications:

1.  **`users-api` (Backend Service):**
    * **Port:** `8081`
    * **Database:** PostgreSQL
    * **Responsibility:** Manages the `CUSTOMERS` table. Handles all CRUD operations for user data.

2.  **`products-api` (Backend Service):**
    * **Port:** `8082`
    * **Database:** OracleDB
    * **Responsibility:** Manages the `PRODUCTS` table. Handles all CRUD operations for product inventory.

3.  **`sales-api` (Backend Service):**
    * **Port:** `8083`
    * **Database:** Microsoft SQL Server
    * **Responsibility:** Manages the `SALES_ORDERS` table. This table provides the "link" by storing foreign keys (as plain numbers) to the customer and product tables.

4.  **`web-gui` (Frontend Application):**
    * **Port:** `8080`
    * **Database:** None.
    * **Responsibility:** Acts as the main GUI and "Orchestrator" (or Backend-for-Frontend). It presents the Thymeleaf HTML pages to the user. It does not connect to any database directly; instead, it calls the other three APIs to fetch, create, update, or delete data.

### 2.1. Processing Sequence (High-Level)

The user only interacts with the `web-gui`. The `web-gui`'s `WebController` is responsible for all "business logic," including the crucial "application-level join" of data.



### 2.2. Technology Stack

* **Java:** 17+
* **Backend Framework:** Spring Boot 3.x
* **Data Access:** Spring Data JPA (Hibernate)
* **GUI:** Thymeleaf
* **Build Tool:** Maven
* **Databases:**
    * PostgreSQL
    * OracleDB 21c (Express Edition)
    * Microsoft SQL Server 2022 (Express Edition)
* **API Client:** Spring `RestTemplate`
* **Utilities:** Lombok, Spring Boot DevTools

### 2.3. Database Schema

* **`user_db` (PostgreSQL)**
    * `CUSTOMERS` table (`id` [PK], `full_name`, `email`)
* **`product_admin` schema (OracleDB @ XEPDB1)**
    * `PRODUCTS` table (`id` [PK], `product_name`, `price`)
    * `PRODUCT_ID_SEQ` (Sequence for the primary key)
* **`sales_db` (MS SQL Server)**
    * `SALES_ORDERS` table (`id` [PK], `order_date`, `customer_id_link`, `product_id_link`)

The "link" is logical. The `sales_orders` table's `customer_id_link` column corresponds to an `id` in the `customers` table, but no database-level foreign key exists. This link is enforced and managed by the `web-gui` application.

---

## 3. Alternative Solutions

An alternative solution was initially considered: a **single monolithic application** running on one port.

This design would involve a single Spring Boot project with all three database drivers. Instead of the simple `application.properties` used in the microservices, this monolith would require three complex, manually-defined Java configuration classes (e.g., `UserDbConfig`, `ProductDbConfig`, `SalesDbConfig`).

Each class would have to define its own:
1.  `DataSource`
2.  `LocalContainerEntityManagerFactoryBean`
3.  `PlatformTransactionManager`

This approach was abandoned due to its high complexity. It created conflicts in Spring's auto-configuration, particularly with bean instantiation order. The microservice architecture is far cleaner, simpler to configure, and more aligned with modern development practices.

---

## 4. Component Description (API Services)

This section describes the main objects and method prototypes for the three backend API services.

### 4.1. `users-api` (PostgreSQL)

* **`Customer` (Entity)**
    * A JPA Entity bean representing a row in the `customers` table.
    * **Fields:** `id` (Long, PK), `fullName` (String), `email` (String).
* **`CustomerRepository` (Interface)**
    * `public interface CustomerRepository extends JpaRepository<Customer, Long>`
    * A Spring Data JPA repository that automatically provides all necessary CRUD methods (`save`, `findById`, `findAll`, `deleteById`).
* **`CustomerController` (REST API)**
    * Handles all HTTP requests for customers at the `/api/customers` base path.
    * **Constructor:** `public CustomerController(CustomerRepository repository)`
    * **Method Prototypes:**
        * `public List<Customer> getAllCustomers()`: (GET /) Returns a JSON list of all customers.
        * `public Customer createCustomer(@RequestBody Customer customer)`: (POST /) Creates a new customer from the JSON body. Returns the saved customer.
        * `public Customer getCustomerById(@PathVariable Long id)`: (GET /{id}) Returns a single customer by their ID.
        * `public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer updatedCustomer)`: (PUT /{id}) Finds a customer by ID and updates their details. Returns the updated customer.
        * `public void deleteCustomer(@PathVariable Long id)`: (DELETE /{id}) Deletes a customer by their ID.

### 4.2. `products-api` (OracleDB)

* **`Product` (Entity)**
    * A JPA Entity bean for the `products` table.
    * **Fields:** `id` (Long, PK, uses `@SequenceGenerator`), `productName` (String), `price` (double).
* **`ProductRepository` (Interface)**
    * `public interface ProductRepository extends JpaRepository<Product, Long>`
    * Provides automatic CRUD methods for Products.
* **`ProductController` (REST API)**
    * Handles requests at `/api/products`.
    * **Method Prototypes:**
        * `public List<Product> getAllProducts()`: (GET /) Returns a JSON list of all products.
        * `public Product createProduct(@RequestBody Product product)`: (POST /) Creates a new product.
        * `public Product getProductById(@PathVariable Long id)`: (GET /{id}) Returns a single product.
        * `public Product updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct)`: (PUT /{id}) Updates a product.
        * `public void deleteProduct(@PathVariable Long id)`: (DELETE /{id}) Deletes a product.

### 4.3. `sales-api` (MS SQL Server)

* **`SalesOrder` (Entity)**
    * A JPA Entity bean for the `sales_orders` table.
    * **Fields:** `id` (Long, PK), `orderDate` (LocalDateTime, set by `@PrePersist`), `customerIdLink` (Long), `productIdLink` (Long).
* **`SalesOrderRepository` (Interface)**
    * `public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>`
    * Provides automatic CRUD methods for SalesOrders.
* **`SalesOrderController` (REST API)**
    * Handles requests at `/api/orders`.
    * **Method Prototypes:**
        * `public List<SalesOrder> getAllOrders()`: (GET /) Returns a JSON list of all orders.
        * `public SalesOrder createOrder(@RequestBody SalesOrder order)`: (POST /) Creates a new order. The body only needs to contain the two link IDs.
        * `public SalesOrder getOrderById(@PathVariable Long id)`: (GET /{id}) Returns a single order.
        * `public SalesOrder updateOrder(@PathVariable Long id, @RequestBody SalesOrder updatedOrder)`: (PUT /{id}) Updates an order's links.
        * `public void deleteOrder(@PathVariable Long id)`: (DELETE /{id}) Deletes an order.

---

## 5. Component Description (GUI Application)

The `web-gui` project acts as the system's coordinator.

### 5.1. DTOs (Data Transfer Objects)
Since this project cannot access the Entity classes from the other APIs, it defines its own "mirror" classes in the `com.homework.webgui.dto` package:
* `Customer`: Mirrors the `users-api` Customer.
* `Product`: Mirrors the `products-api` Product.
* `SalesOrder`: Mirrors the `sales-api` SalesOrder.
* `OrderDetails`: A composite DTO used to hold the "joined" data for the GUI. Contains fields like `customerName` and `productName`.

### 5.2. `WebController` (Orchestrator & GUI Controller)

This `@Controller` handles all browser requests, calls the backend APIs using `RestTemplate`, performs the application-level join, and serves the Thymeleaf HTML pages.

* **Constructor:**
    * `public WebController(RestTemplate restTemplate, @Value(...) String usersApiUrl, ...)`: Injects the `RestTemplate` bean and the API URLs from `application.properties`.
* **Read (R) Method:**
    * `public ModelAndView getHomePage()`: (GET /) This is the main method.
        1.  Calls `GET /api/customers` on `users-api`.
        2.  Calls `GET /api/products` on `products-api`.
        3.  Calls `GET /api/orders` on `sales-api`.
        4.  Performs an "application-level join": It loops through the `SalesOrder` list and uses `Map`s of customers and products to create a `List<OrderDetails>` containing the actual names.
        5.  Returns the `index.html` template, populated with all three lists.
* **Create (C) Methods:**
    * `public String createCustomer(@RequestParam String fullName, ...)`: (POST /create-customer) Receives form data, creates a `Customer` DTO, and calls `POST /api/customers`. Redirects to `/`.
    * `public String createProduct(...)`: (POST /create-product) Calls `POST /api/products`. Redirects to `/`.
    * `public String createOrder(@RequestParam Long customerIdLink, ...)`: (POST /create-order) The "link" logic. Receives the two IDs and calls `POST /api/orders`. Redirects to `/`.
* **Update (U) Methods:**
    * `public ModelAndView showEditCustomerPage(@PathVariable Long id)`: (GET /edit-customer/{id}) Calls `GET /api/customers/{id}` to fetch a single customer. Returns the `edit-customer.html` template, pre-filled with that customer's data.
    * `public String updateCustomer(@ModelAttribute Customer customer)`: (POST /update-customer) Receives the form data (including the hidden `id`) and calls `PUT /api/customers/{id}`. Redirects to `/`.
    * (Similar methods exist for Product and Order).
* **Delete (D) Methods:**
    * `public String deleteCustomer(@PathVariable Long id)`: (GET /delete-customer/{id}) Calls `DELETE /api/customers/{id}`. Redirects to `/`.
    * (Similar methods exist for Product and Order).

---

## 6. Operational Context & Testing

This section provides the steps to set up and run the entire system.

### 6.1. Database Pre-Configuration

Before running the applications, the three databases must be configured:

1.  **PostgreSQL:**
    * Create a user: `CREATE USER user_admin WITH PASSWORD 'password';`
    * Create a database: `CREATE DATABASE user_db OWNER user_admin;`
2.  **OracleDB:**
    * Connect as `SYS` to the `XEPDB1` pluggable database.
    * Create a user: `CREATE USER product_admin IDENTIFIED BY 'password';`
    * Grant permissions: `GRANT CONNECT, RESOURCE TO product_admin;`
    * Set quota: `ALTER USER product_admin QUOTA UNLIMITED ON USERS;`
3.  **MS SQL Server:**
    * Create a database: `CREATE DATABASE sales_db;`
    * Create a login: `CREATE LOGIN sales_admin WITH PASSWORD = 'password', CHECK_POLICY = OFF;`
    * Map user: `USE sales_db; CREATE USER sales_admin FOR LOGIN sales_admin; ALTER ROLE db_owner ADD MEMBER sales_admin;`
    * **Crucially,** use **SQL Server Configuration Manager** to:
        1.  Enable "TCP/IP" protocol for the SQL Server instance.
        2.  Restart the SQL Server service.
    * **Crucially,** use **SSMS** to:
        1.  Change the server's authentication mode to "SQL Server and Windows Authentication mode."
        2.  Restart the SQL Server service again.

### 6.2. How to Run the System

The applications must be run in the following order:

1.  Ensure all three database services (PostgreSQL, Oracle, SQL Server) are running.
2.  Run `UsersApiApplication.java` (starts on port 8081).
3.  Run `ProductsApiApplication.java` (starts on port 8082).
4.  Run `SalesApiApplication.java` (starts on port 8083).
5.  Run `WebGuiApplication.java` (starts on port 8080).
6.  Open a web browser and navigate to `http://localhost:8080`.

### 6.3. Battery of Tests (Usage Examples)

The following tests can be performed from the GUI at `http://localhost:8080` to validate all functionality.

1.  **Test Create (C):**
    * In the "New Customer" form, create a customer "John Doe".
    * **Expected:** The page reloads, and "John Doe" appears in the "Customers" table.
    * In the "New Product" form, create a product "Laptop".
    * **Expected:** The page reloads, and "Laptop" appears in the "Products" table.

2.  **Test Read (R) and "Link":**
    * Observe the IDs for "John Doe" and "Laptop".
    * In the "New Order" form, enter these two IDs.
    * **Expected:** The page reloads. The "Orders" table shows a new order that, instead of just IDs, displays the text "John Doe" and "Laptop" in the same row. This proves the application-level join is working.

3.  **Test Update (U):**
    * Click "Edit" next to "John Doe".
    * Change the name to "John Smith" and submit.
    * **Expected:** The page reloads. The "Customers" table shows "John Smith". The "Orders" table (which previously showed "John Doe") now *also* shows "John Smith", proving the join logic is dynamic.

4.  **Test Delete (D):**
    * Click "Delete" next to the order you created.
    * **Expected:** The page reloads, and the order is removed from the "Orders" table.
    * Click "Delete" next to "John Smith".
    * **Expected:** The page reloads, and the customer is removed from the "Customers" table. Any orders that linked to them will now show "N/A (ID not found)", demonstrating the broken link.