package com.homework.webgui.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDetails {
    private Long id;
    private LocalDateTime orderDate;

    private Long customerId;
    private String customerName;

    private Long productId;
    private String productName;
}