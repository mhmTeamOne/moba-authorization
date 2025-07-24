package com.mhm.models;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KeycloackUserDTO implements Serializable {
    protected String username;
    protected String email;
    protected String firstName;
    protected String lastName;
    protected AttributeModel attributeModel;
    protected List<CredentialModel> credentials;
    protected boolean emailVerified;
    protected boolean enabled;

}