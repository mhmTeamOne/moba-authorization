package com.mhm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorDTO {
    
    @JsonProperty("field")
    private String field;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("rejected_value")
    private Object rejectedValue;
    
    public ValidationErrorDTO() {
    }
    
    public ValidationErrorDTO(String field, String message, String errorCode) {
        this.field = field;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public ValidationErrorDTO(String field, String message, String errorCode, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.errorCode = errorCode;
        this.rejectedValue = rejectedValue;
    }

}