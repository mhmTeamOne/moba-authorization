package com.mhm.models;

import lombok.Data;

@Data
public class CredentialModel {
    protected boolean temporary;
    protected String type;
    protected String value;
}
