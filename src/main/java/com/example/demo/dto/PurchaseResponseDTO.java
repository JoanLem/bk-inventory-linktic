package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO con la información de la compra realizada")
public class PurchaseResponseDTO {
    
    @Schema(description = "ID del producto comprado", example = "1")
    private Long productId;
    
    @Schema(description = "Información del producto", required = true)
    private ProductDTO product;
    
    @Schema(description = "Cantidad comprada", example = "5")
    private Integer quantity;
    
    @Schema(description = "Precio unitario del producto", example = "15000.00")
    private BigDecimal unitPrice;
    
    @Schema(description = "Subtotal de la compra (precio unitario * cantidad)", example = "75000.00")
    private BigDecimal subtotal;
    
    @Schema(description = "Total de la compra (incluye impuestos si aplica)", example = "75000.00")
    private BigDecimal total;
    
    @Schema(description = "Cantidad disponible en inventario después de la compra", example = "45")
    private Integer remainingQuantity;
}

