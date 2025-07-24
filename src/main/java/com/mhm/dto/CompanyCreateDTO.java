package com.mhm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class CompanyCreateDTO {
    // Explicit getters and setters to ensure compilation
    private String companyName;
    private String pibNumber;
    private String phoneNumber;
    private String country;
    private String city;
    private String zipCode;

}