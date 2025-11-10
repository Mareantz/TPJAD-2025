package com.homework.webgui.dto;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesOrder {
    private Long id;
    private LocalDateTime orderDate;
    private Long customerIdLink;
    private Long productIdLink;
}