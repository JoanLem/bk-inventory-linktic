package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDataDTO {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("attributes")
    private ProductAttributesDTO attributes;
}

