package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO con la información del inventario y el producto")
public class InventoryResponseDTO {
    
    @Schema(description = "ID del producto", example = "1")
    private Long productId;
    
    @Schema(description = "Cantidad disponible en inventario", example = "50")
    private Integer availableQuantity;
    
    @Schema(description = "Información detallada del producto")
    private ProductDTO product;
}

