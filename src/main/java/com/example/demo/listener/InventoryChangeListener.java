package com.example.demo.listener;

import com.example.demo.event.InventoryChangeEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryChangeListener {
        
    @Async
    @EventListener
    public void handleInventoryChange(InventoryChangeEvent event) {
        log.info("=== EVENTO DE CAMBIO DE INVENTARIO ===");
        log.info("Tipo de operación: {}", event.getOperationType());
        log.info("Producto ID: {}", event.getInventory().getProductId());
        log.info("Cantidad anterior: {}", event.getPreviousQuantity());
        log.info("Cantidad nueva: {}", event.getInventory().getQuantity());
        log.info("=====================================");
        
    }
}

