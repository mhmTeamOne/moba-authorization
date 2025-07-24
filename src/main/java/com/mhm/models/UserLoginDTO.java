package com.mhm.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserLoginDTO {

    private String userName;
    private String password;

}