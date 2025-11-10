package com.homework.webgui.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * A new DTO to hold the "merged" order information for the GUI.
 */
@Data
public class OrderDetails {
    // From SalesOrder
    private Long id;
    private LocalDateTime orderDate;

    // From Customer (the "join")
    private Long customerId;
    private String customerName;

    // From Product (the "join")
    private Long productId;
    private String productName;
}