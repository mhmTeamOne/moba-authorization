package com.mhm.dto;

import com.mhm.entities.AccountType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserResponseDTO {

    // Explicit getters and setters to ensure compilation
    private Long id;
    private AccountType accountType;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String country;
    private boolean disabled;
    
    // Company information (only if account type is BUSINESS)
    private CompanyResponseDTO company;

}