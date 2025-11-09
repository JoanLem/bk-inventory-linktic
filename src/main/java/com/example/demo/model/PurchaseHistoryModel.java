package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo que representa el historial de operaciones de inventario")
public class PurchaseHistoryModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del registro de historial", example = "1")
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    @Schema(description = "ID del producto", example = "1")
    private Long productId;
    
    @Column(name = "quantity", nullable = false)
    @Schema(description = "Cantidad involucrada en la operación", example = "50")
    private Integer quantity;
    
    @Column(name = "operation_type", nullable = false)
    @Schema(description = "Tipo de operación: PURCHASE, SALE, ADJUSTMENT, CREATE, UPDATE", example = "UPDATE")
    private String operationType;
    
    @Column(name = "operation_date", nullable = false)
    @Schema(description = "Fecha y hora de la operación", example = "2024-01-15T10:30:00")
    private LocalDateTime operationDate;
    
    @Column(name = "previous_quantity")
    @Schema(description = "Cantidad anterior antes de la operación", example = "100")
    private Integer previousQuantity;
    
    @Column(name = "new_quantity")
    @Schema(description = "Cantidad nueva después de la operación", example = "150")
    private Integer newQuantity;
    
    @PrePersist
    protected void onCreate() {
        operationDate = LocalDateTime.now();
    }
}

