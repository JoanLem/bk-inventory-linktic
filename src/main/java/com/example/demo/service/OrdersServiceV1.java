package com.example.demo.service;

import com.example.demo.client.ProductClient;
import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.PurchaseResponseDTO;
import com.example.demo.model.InventoryModel;
import com.example.demo.model.PurchaseHistoryModel;
import com.example.demo.repository.PurchaseHistoryRepo;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrdersServiceV1 {
   
    private final InventoryServiceV1 inventoryService;
    private final PurchaseHistoryRepo purchaseHistoryRepo;
    private final ProductClient productClient;
    
    public OrdersServiceV1(InventoryServiceV1 inventoryService, 
                           PurchaseHistoryRepo purchaseHistoryRepo,
                           ProductClient productClient) {
        this.inventoryService = inventoryService;
        this.purchaseHistoryRepo = purchaseHistoryRepo;
        this.productClient = productClient;
    } 
   
    
    /**
     * Procesa una compra de producto:
     * - Verifica disponibilidad en inventario
     * - Actualiza la cantidad disponible tras la compra
     * - Retorna información de la compra realizada
     */
    @Transactional
    public PurchaseResponseDTO processPurchase(Long productId, Integer quantity) {
        log.info("Procesando compra: Producto ID={}, Cantidad={}", productId, quantity);
        
        // Validar cantidad
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        // Obtener información del producto desde el microservicio
        ProductDTO product = productClient.getProductById(productId);
        if (product == null) {
            log.warn("Producto con ID {} no encontrado", productId);
            throw new RuntimeException("Producto no encontrado con ID: " + productId);
        }
        
        // Verificar disponibilidad en inventario
        InventoryResponseDTO currentInventory = inventoryService.getProductQuantity(productId);
        Integer availableQuantity = currentInventory.getAvailableQuantity();
        
        if (availableQuantity < quantity) {
            log.warn("Stock insuficiente. Disponible: {}, Solicitado: {}", availableQuantity, quantity);
            throw new IllegalStateException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", availableQuantity, quantity)
            );
        }
        
        // Calcular totales
        BigDecimal unitPrice = product.getPrice();
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal total = subtotal; // para fines practicos es igual al subtotal, pero podriamos implementar algo de impuestos
        
        // Actualizar inventario (reducir cantidad)
        Integer newQuantity = availableQuantity - quantity;
        Integer previousQuantity = availableQuantity;
        
        InventoryModel savedInventory = inventoryService.updateProductQuantity(productId, newQuantity);
        
        // Registrar historial de compra
        registerPurchaseHistory(productId, quantity, "PURCHASE", previousQuantity, savedInventory.getQuantity());
        
        // Construir respuesta
        PurchaseResponseDTO response = new PurchaseResponseDTO();
        response.setProductId(productId);
        response.setProduct(product);
        response.setQuantity(quantity);
        response.setUnitPrice(unitPrice);
        response.setSubtotal(subtotal);
        response.setTotal(total);
        response.setRemainingQuantity(savedInventory.getQuantity());
        
        log.info("Compra procesada exitosamente. Producto ID={}, Cantidad={}, Total={}", 
                   productId, quantity, total);
        
        return response;
    }
    
    /**
     * Registra historial de compras
     */
    private void registerPurchaseHistory(Long productId, Integer quantity, String operationType, 
                                        Integer previousQuantity, Integer newQuantity) {
        try {
            PurchaseHistoryModel history = new PurchaseHistoryModel();
            history.setProductId(productId);
            history.setQuantity(quantity);
            history.setOperationType(operationType);
            history.setPreviousQuantity(previousQuantity);
            history.setNewQuantity(newQuantity);
            
            purchaseHistoryRepo.save(history);
            log.info("Historial de compra registrado para producto ID: {}", productId);
        } catch (Exception e) {
            log.error("Error al registrar historial de compra: {}", e.getMessage());
            // No lanzamos excepción para que no afecte la operación principal
        }
    }
    
    
    
}

