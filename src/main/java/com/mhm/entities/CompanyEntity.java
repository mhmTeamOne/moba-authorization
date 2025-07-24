package com.mhm.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "company")
public class CompanyEntity {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Company identification
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "pib_number")
    private String pibNumber;
    
    // Contact information
    @Column(name = "phone_number")
    private String phoneNumber;
    
    // Address information
    private String country;
    private String city;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    // Explicit getters and setters to ensure compilation
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getPibNumber() { return pibNumber; }
    public void setPibNumber(String pibNumber) { this.pibNumber = pibNumber; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
} 