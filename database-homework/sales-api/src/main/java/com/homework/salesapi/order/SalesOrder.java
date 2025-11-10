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

    private Long customerIdLink;

    private Long productIdLink;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
}