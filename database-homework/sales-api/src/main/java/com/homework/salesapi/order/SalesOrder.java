package com.homework.salesapi.order;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    // This is the "link" to the Postgres 'customers' table
    private Long customerIdLink;

    // This is the "link" to the Oracle 'products' table
    private Long productIdLink;

    // This runs automatically when a new order is saved
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
}