package com.homework.webgui.dto;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long id;
    private String productName;
    private double price;
}