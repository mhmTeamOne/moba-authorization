package com.mhm.dto;

import lombok.Data;

@Data
public class CompanyResponseDTO {

    // Explicit getters and setters to ensure compilation
    private Long id;
    private String companyName;
    private String pibNumber;
    private String phoneNumber;
    private String country;
    private String city;
    private String zipCode;
}