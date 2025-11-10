package com.homework.webgui.dto;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private Long id;
    private String fullName;
    private String email;
}