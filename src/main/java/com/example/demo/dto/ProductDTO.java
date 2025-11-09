package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO con la información del producto obtenida del microservicio de productos")
public class ProductDTO {
    
    @Schema(description = "ID único del producto", example = "1")
    private Long id;
    
    @Schema(description = "Nombre del producto", example = "Laptop Dell")
    private String name;
    
    @Schema(description = "Descripción del producto", example = "Laptop Dell Inspiron 15")
    private String description;
    
    @Schema(description = "Precio del producto", example = "899.99")
    private BigDecimal price;
}

