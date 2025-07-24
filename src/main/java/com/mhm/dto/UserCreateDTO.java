package com.mhm.dto;

import com.mhm.entities.AccountType;
import lombok.Data;

@Data
public class UserCreateDTO {
    // Explicit getters and setters to ensure compilation
    private AccountType accountType;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private String country;
    private CompanyCreateDTO company;

}