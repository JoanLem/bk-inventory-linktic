package com.example.demo.service;

import com.example.demo.client.ProductClient;
import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.event.InventoryChangeEvent;
import com.example.demo.model.InventoryModel;
import com.example.demo.model.PurchaseHistoryModel;
import com.example.demo.repository.InventoryRepo;
import com.example.demo.repository.PurchaseHistoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final InventoryRepo inventoryRepo;
    private final PurchaseHistoryRepo purchaseHistoryRepo;
    private final ProductClient productClient;
    private final ApplicationEventPublisher eventPublisher;
    
    public InventoryService(InventoryRepo inventoryRepo, 
                           PurchaseHistoryRepo purchaseHistoryRepo,
                           ProductClient productClient,
                           ApplicationEventPublisher eventPublisher) {
        this.inventoryRepo = inventoryRepo;
        this.purchaseHistoryRepo = purchaseHistoryRepo;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Consulta la cantidad disponible de un producto específico por ID
     * obteniendo la información del producto desde el microservicio de productos
     */
    public InventoryResponseDTO getProductQuantity(Long productId) {
        logger.info("Consultando cantidad disponible para producto ID: {}", productId);
        
        // Obtener información del producto desde el microservicio
        ProductDTO product = productClient.getProductById(productId);
        
        if (product == null) {
            logger.warn("Producto con ID {} no encontrado en el microservicio de productos", productId);
            throw new RuntimeException("Producto no encontrado con ID: " + productId);
        }
        
        // Buscar el inventario del producto
        Optional<InventoryModel> inventoryOpt = inventoryRepo.findByProductId(productId);
        
        Integer availableQuantity = inventoryOpt
            .map(InventoryModel::getQuantity)
            .orElse(0);
        
        logger.info("Cantidad disponible para producto ID {}: {}", productId, availableQuantity);
        
        InventoryResponseDTO response = new InventoryResponseDTO();
        response.setProductId(productId);
        response.setAvailableQuantity(availableQuantity);
        response.setProduct(product);
        
        return response;
    }
    
    /**
     * Actualiza la cantidad disponible de un producto
     */
    @Transactional
    public InventoryModel updateProductQuantity(Long productId, Integer newQuantity) {
        logger.info("Actualizando cantidad para producto ID: {} a cantidad: {}", productId, newQuantity);
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        Optional<InventoryModel> inventoryOpt = inventoryRepo.findByProductId(productId);
        
        InventoryModel inventory;
        Integer previousQuantity = 0;
        String operationType;
        
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
            previousQuantity = inventory.getQuantity();
            inventory.setQuantity(newQuantity);
            operationType = "UPDATE";
            logger.info("Actualizando inventario existente. Cantidad anterior: {}, nueva: {}", 
                      previousQuantity, newQuantity);
        } else {
            // Si no existe, crear nuevo registro
            inventory = new InventoryModel();
            inventory.setProductId(productId);
            inventory.setQuantity(newQuantity);
            operationType = "CREATE";
            logger.info("Creando nuevo registro de inventario para producto ID: {}", productId);
        }
        
        InventoryModel savedInventory = inventoryRepo.save(inventory);
        
        // Registrar historial de compras (opcional)
        registerPurchaseHistory(productId, newQuantity, operationType, previousQuantity, savedInventory.getQuantity());
        
        // Emitir evento cuando el inventario cambie (opcional)
        eventPublisher.publishEvent(new InventoryChangeEvent(this, savedInventory, operationType, previousQuantity));
        
        logger.info("Inventario actualizado exitosamente. ID: {}, Producto ID: {}, Cantidad: {}", 
                   savedInventory.getId(), savedInventory.getProductId(), savedInventory.getQuantity());
        
        return savedInventory;
    }
    
    /**
     * Registra historial de compras (opcional)
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
            logger.info("Historial de compra registrado para producto ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error al registrar historial de compra: {}", e.getMessage());
            // No lanzamos excepción para que no afecte la operación principal
        }
    }
    
    /**
     * Obtiene el historial de compras de un producto
     */
    public java.util.List<PurchaseHistoryModel> getPurchaseHistory(Long productId) {
        logger.info("Consultando historial de compras para producto ID: {}", productId);
        return purchaseHistoryRepo.findByProductIdOrderByOperationDateDesc(productId);
    }
    
    /**
     * Obtiene todos los registros de inventario
     */
    public java.util.List<InventoryModel> getAllInventories() {
        logger.info("Consultando todos los registros de inventario");
        return inventoryRepo.findAll();
    }
}

