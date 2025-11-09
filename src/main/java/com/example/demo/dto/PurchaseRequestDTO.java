package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO para realizar una compra de producto")
public class PurchaseRequestDTO {
    
    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "ID del producto a comprar", example = "1")
    private Long productId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Schema(description = "Cantidad de productos a comprar", example = "5")
    private int quantity;
}

