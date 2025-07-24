package com.mhm.dto;

import lombok.Data;

@Data
public class CompanyUpdateDTO {
    // Explicit getters and setters to ensure compilation
    private String companyName;
    private String pibNumber;
    private String phoneNumber;
    private String country;
    private String city;
    private String zipCode;
}