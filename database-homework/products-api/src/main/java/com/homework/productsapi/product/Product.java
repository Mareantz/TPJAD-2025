package com.homework.productsapi.product;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    // This is the standard, correct way to do auto-incrementing in Oracle
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "PRODUCT_ID_SEQ", allocationSize = 1)
    private Long id;

    private String productName;

    private double price;
}