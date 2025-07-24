package com.mhm.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "users")
public class UserEntity extends PanacheEntity {
    // Primary key first
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT PRIMARY KEY")
    private Long id;
    
    // Account type
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", columnDefinition = "VARCHAR(20)")
    protected AccountType accountType;
    
    // Personal information
    @Column(name = "first_name", columnDefinition = "VARCHAR(100)")
    protected String firstName;
    
    @Column(name = "last_name", columnDefinition = "VARCHAR(100)")
    protected String lastName;
    
    @Column(name = "username", columnDefinition = "VARCHAR(50)")
    protected String username;
    
    @Column(name = "email", columnDefinition = "VARCHAR(255)")
    protected String email;
    
    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    protected String phoneNumber;
    
    @Column(name = "password_hash", columnDefinition = "VARCHAR(255)")
    protected String passwordHash;
    
    @Column(name = "country", columnDefinition = "VARCHAR(100)")
    protected String country;
    
    // Status fields
    @Column(name = "disabled", columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected boolean disabled;

    // Foreign key relationship
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    protected CompanyEntity company;
    
    // Explicit getters and setters to ensure compilation
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    
    public CompanyEntity getCompany() { return company; }
    public void setCompany(CompanyEntity company) { this.company = company; }
}