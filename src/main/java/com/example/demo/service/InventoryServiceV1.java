package com.example.demo.service;

import com.example.demo.client.ProductClient;
import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.event.InventoryChangeEvent;
import com.example.demo.model.InventoryModel;
import com.example.demo.repository.InventoryRepo;
import com.example.demo.repository.PurchaseHistoryRepo;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class InventoryServiceV1 {
   
    private final InventoryRepo inventoryRepo;
    private final ProductClient productClient;
    private final ApplicationEventPublisher eventPublisher;
    
    public InventoryServiceV1(InventoryRepo inventoryRepo, 
                           PurchaseHistoryRepo purchaseHistoryRepo,
                           ProductClient productClient,
                           ApplicationEventPublisher eventPublisher) {
        this.inventoryRepo = inventoryRepo;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Consulta la cantidad disponible de un producto específico por ID
     * obteniendo la información del producto desde el microservicio de productos
     */
    public InventoryResponseDTO getProductQuantity(Long productId) {
        log.info("Consultando cantidad disponible para producto ID: {}", productId);
        
        // Obtener información del producto desde el microservicio
        ProductDTO product = productClient.getProductById(productId);
        
        if (product == null) {
            log.warn("Producto con ID {} no encontrado en el microservicio de productos", productId);
            throw new RuntimeException("Producto no encontrado con ID: " + productId);
        }
        
        // Buscar el inventario del producto
        Optional<InventoryModel> inventoryOpt = inventoryRepo.findByProductId(productId);
        
        Integer availableQuantity = inventoryOpt
            .map(InventoryModel::getQuantity)
            .orElse(0);
        
        log.info("Cantidad disponible para producto ID {}: {}", productId, availableQuantity);
        
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
        log.info("Actualizando cantidad para producto ID: {} a cantidad: {}", productId, newQuantity);
        
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
            log.info("Actualizando inventario existente. Cantidad anterior: {}, nueva: {}", 
                      previousQuantity, newQuantity);
        } else {
            // Si no existe, crear nuevo registro
            inventory = new InventoryModel();
            inventory.setProductId(productId);
            inventory.setQuantity(newQuantity);
            operationType = "CREATE";
            log.info("Creando nuevo registro de inventario para producto ID: {}", productId);
        }
        
        InventoryModel savedInventory = inventoryRepo.save(inventory);
                
        // Emitir evento cuando el inventario cambie (opcional)
        eventPublisher.publishEvent(new InventoryChangeEvent(this, savedInventory, operationType, previousQuantity));
        
        log.info("Inventario actualizado exitosamente. ID: {}, Producto ID: {}, Cantidad: {}", 
                   savedInventory.getId(), savedInventory.getProductId(), savedInventory.getQuantity());
        
        return savedInventory;
    }
    
}

