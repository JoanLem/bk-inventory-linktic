package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo de inventario que representa la cantidad disponible de un producto")
public class InventoryModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del registro de inventario", example = "1")
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    @Schema(description = "ID del producto", example = "1")
    private Long productId;
    
    @Column(name = "quantity", nullable = false)
    @Schema(description = "Cantidad disponible en inventario", example = "100")
    private Integer quantity;
}
