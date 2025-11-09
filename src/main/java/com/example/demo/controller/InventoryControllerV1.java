package com.example.demo.controller;

import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.UpdateInventoryRequestDTO;
import com.example.demo.model.InventoryModel;
import com.example.demo.service.InventoryServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Management", description = "API para gestión de inventario de productos")
public class InventoryControllerV1 {

    private final InventoryServiceV1 inventoryService;

    public InventoryControllerV1(InventoryServiceV1 inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(
            summary = "Health check",
            description = "Endpoint para verificar que la API está funcionando correctamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "API funcionando correctamente",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API bk-inventory is working");
    }

    @Operation(
            summary = "Consultar cantidad disponible de un producto",
            description = "Obtiene la cantidad disponible de un producto específico por ID, " +
                    "incluyendo la información del producto desde el microservicio de productos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información del inventario obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = InventoryResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @GetMapping("/products/{productId}/quantity")
    public ResponseEntity<InventoryResponseDTO> getProductQuantity(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long productId) {
        InventoryResponseDTO response = inventoryService.getProductQuantity(productId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar cantidad disponible de un producto",
            description = "Actualiza la cantidad disponible de un producto. Si el producto no existe en inventario, " +
                    "se crea un nuevo registro. Esta operación registra el historial y emite un evento de cambio."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventoryModel.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida (cantidad negativa)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @PutMapping("/products")
    public ResponseEntity<InventoryModel> updateProductQuantity(
            @Valid @RequestBody UpdateInventoryRequestDTO request) {
        InventoryModel updatedInventory = inventoryService.updateProductQuantity(
                request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedInventory);
    }
   
}
