package com.mhm.mappers;

import com.mhm.dto.*;
import com.mhm.entities.CompanyEntity;
import com.mhm.entities.UserEntity;
import com.mhm.services.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserMapper {
    
    @Inject
    PasswordService passwordService;
    
    public UserEntity toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        
        UserEntity entity = new UserEntity();
        entity.setAccountType(dto.getAccountType());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setCountry(dto.getCountry());
        entity.setDisabled(false); // Default value
        
        // Hash password
        if (dto.getPassword() != null) {
            entity.setPasswordHash(passwordService.hashPassword(dto.getPassword()));
        }
        
        // Map company if present
        if (dto.getCompany() != null) {
            entity.setCompany(toCompanyEntity(dto.getCompany()));
        }
        
        return entity;
    }
    
    public UserResponseDTO toResponseDTO(UserEntity entity) {
        if (entity == null) return null;
        
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setAccountType(entity.getAccountType());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setCountry(entity.getCountry());
        dto.setDisabled(entity.isDisabled());
        
        // Map company if present
        if (entity.getCompany() != null) {
            dto.setCompany(toCompanyResponseDTO(entity.getCompany()));
        }
        
        return dto;
    }
    
    public void updateEntityFromDTO(UserEntity entity, UserUpdateDTO dto) {
        if (dto == null || entity == null) return;
        
        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getCountry() != null) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.getPassword() != null) {
            entity.setPasswordHash(passwordService.hashPassword(dto.getPassword()));
        }
        
        // Update company if present
        if (dto.getCompany() != null && entity.getCompany() != null) {
            updateCompanyEntityFromDTO(entity.getCompany(), dto.getCompany());
        }
    }
    
    private CompanyEntity toCompanyEntity(CompanyCreateDTO dto) {
        if (dto == null) return null;
        
        CompanyEntity entity = new CompanyEntity();
        entity.setCompanyName(dto.getCompanyName());
        entity.setPibNumber(dto.getPibNumber());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setCountry(dto.getCountry());
        entity.setCity(dto.getCity());
        entity.setZipCode(dto.getZipCode());
        
        return entity;
    }
    
    private CompanyResponseDTO toCompanyResponseDTO(CompanyEntity entity) {
        if (entity == null) return null;
        
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setPibNumber(entity.getPibNumber());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setCountry(entity.getCountry());
        dto.setCity(entity.getCity());
        dto.setZipCode(entity.getZipCode());
        
        return dto;
    }
    
    private void updateCompanyEntityFromDTO(CompanyEntity entity, CompanyUpdateDTO dto) {
        if (dto == null || entity == null) return;
        
        if (dto.getCompanyName() != null) {
            entity.setCompanyName(dto.getCompanyName());
        }
        if (dto.getPibNumber() != null) {
            entity.setPibNumber(dto.getPibNumber());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getCountry() != null) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.getCity() != null) {
            entity.setCity(dto.getCity());
        }
        if (dto.getZipCode() != null) {
            entity.setZipCode(dto.getZipCode());
        }
    }
} 