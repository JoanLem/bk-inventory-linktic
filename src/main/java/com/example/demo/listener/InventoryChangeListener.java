package com.example.demo.listener;

import com.example.demo.event.InventoryChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InventoryChangeListener {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryChangeListener.class);
    
    @Async
    @EventListener
    public void handleInventoryChange(InventoryChangeEvent event) {
        logger.info("=== EVENTO DE CAMBIO DE INVENTARIO ===");
        logger.info("Tipo de operación: {}", event.getOperationType());
        logger.info("Producto ID: {}", event.getInventory().getProductId());
        logger.info("Cantidad anterior: {}", event.getPreviousQuantity());
        logger.info("Cantidad nueva: {}", event.getInventory().getQuantity());
        logger.info("=====================================");
        
    }
}

