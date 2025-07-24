package com.mhm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class UserUpdateDTO {
    // Explicit getters and setters to ensure compilation
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String country;
    private String password;
    private CompanyUpdateDTO company;

}