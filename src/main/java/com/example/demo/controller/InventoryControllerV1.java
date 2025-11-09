package com.example.demo.controller;

import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.UpdateInventoryRequestDTO;
import com.example.demo.model.InventoryModel;
import com.example.demo.model.PurchaseHistoryModel;
import com.example.demo.service.InventoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Management", description = "API para gestión de inventario de productos")
public class InventoryControllerV1 {

    private final InventoryService inventoryService;

    public InventoryControllerV1(InventoryService inventoryService) {
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
    @PutMapping("/products/{productId}/quantity")
    public ResponseEntity<InventoryModel> updateProductQuantity(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long productId,
            @Valid @RequestBody UpdateInventoryRequestDTO request) {
        InventoryModel updatedInventory = inventoryService.updateProductQuantity(
                productId, request.getQuantity());
        return ResponseEntity.ok(updatedInventory);
    }

    @Operation(
            summary = "Obtener historial de operaciones de un producto",
            description = "Obtiene el historial completo de operaciones (compras, ventas, ajustes) " +
                    "de un producto específico, ordenado por fecha descendente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PurchaseHistoryModel.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/products/{productId}/history")
    public ResponseEntity<List<PurchaseHistoryModel>> getPurchaseHistory(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long productId) {
        List<PurchaseHistoryModel> history = inventoryService.getPurchaseHistory(productId);
        return ResponseEntity.ok(history);
    }

    @Operation(
            summary = "Obtener todos los inventarios",
            description = "Obtiene una lista de todos los registros de inventario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de inventarios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = InventoryModel.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<InventoryModel>> getAllInventories() {
        List<InventoryModel> inventories = inventoryService.getAllInventories();
        return ResponseEntity.ok(inventories);
    }
}
