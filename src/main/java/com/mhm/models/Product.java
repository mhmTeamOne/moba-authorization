package com.mhm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("tags")
    private String[] tags;
    
    @JsonProperty("inStock")
    private boolean inStock;
    
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("rating")
    private Double rating;
    
    @JsonProperty("reviewCount")
    private Integer reviewCount;
} 