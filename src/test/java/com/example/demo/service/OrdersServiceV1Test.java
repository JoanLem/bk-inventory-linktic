package com.example.demo.service;

import com.example.demo.client.ProductClient;
import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.PurchaseResponseDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.model.InventoryModel;
import com.example.demo.repository.PurchaseHistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para OrdersServiceV1")
class OrdersServiceV1Test {

    @Mock
    private InventoryServiceV1 inventoryService;

    @Mock
    private PurchaseHistoryRepo purchaseHistoryRepo;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrdersServiceV1 ordersService;

    private ProductDTO productDTO;
    private InventoryResponseDTO inventoryResponse;
    private InventoryModel inventoryModel;
    private Long productId;
    private Integer quantity;

    @BeforeEach
    void setUp() {
        productId = 1L;
        quantity = 5;

        productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Producto Test");
        productDTO.setDescription("Descripción del producto");
        productDTO.setPrice(new BigDecimal("15000.00"));

        inventoryModel = new InventoryModel();
        inventoryModel.setId(1L);
        inventoryModel.setProductId(productId);
        inventoryModel.setQuantity(100);

        inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setProductId(productId);
        inventoryResponse.setAvailableQuantity(100);
        inventoryResponse.setProduct(productDTO);
    }

    @Test
    @DisplayName("Debería procesar una compra exitosamente")
    void testProcessPurchase_Success() {
        // Given
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryService.getProductQuantity(productId)).thenReturn(inventoryResponse);
        when(inventoryService.updateProductQuantity(eq(productId), anyInt())).thenReturn(inventoryModel);

        // When
        PurchaseResponseDTO response = ordersService.processPurchase(productId, quantity);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(quantity, response.getQuantity());
        assertEquals(new BigDecimal("15000.00"), response.getUnitPrice());
        assertEquals(new BigDecimal("75000.00"), response.getSubtotal());
        assertEquals(new BigDecimal("75000.00"), response.getTotal());
        assertNotNull(response.getProduct());

        verify(productClient, times(1)).getProductById(productId);
        verify(inventoryService, times(1)).getProductQuantity(productId);
        verify(inventoryService, times(1)).updateProductQuantity(eq(productId), eq(95));
        verify(purchaseHistoryRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando la cantidad es igual a 0")
    void testProcessPurchase_InvalidQuantity_Zero() {
        // Given
        Integer invalidQuantity = 0;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ordersService.processPurchase(productId, invalidQuantity);
        });

        // Verificar el mensaje de la excepción
        assertEquals("La cantidad debe ser mayor a 0", exception.getMessage());

        // Verificar que no se llamó a ningún servicio
        verify(productClient, never()).getProductById(any());
        verify(inventoryService, never()).getProductQuantity(any());
        verify(inventoryService, never()).updateProductQuantity(any(), any());
        verify(purchaseHistoryRepo, never()).save(any());
    }
    
    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando la cantidad es negativa")
    void testProcessPurchase_InvalidQuantity_Negative() {
        // Given
        Integer invalidQuantity = -5;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ordersService.processPurchase(productId, invalidQuantity);
        });

        // Verificar el mensaje de la excepción
        assertEquals("La cantidad debe ser mayor a 0", exception.getMessage());

        // Verificar que no se llamó a ningún servicio
        verify(productClient, never()).getProductById(any());
        verify(inventoryService, never()).getProductQuantity(any());
        verify(inventoryService, never()).updateProductQuantity(any(), any());
        verify(purchaseHistoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el producto no existe")
    void testProcessPurchase_ProductNotFound() {
        // Given
        when(productClient.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            ordersService.processPurchase(productId, quantity);
        });

        verify(inventoryService, never()).getProductQuantity(any());
        verify(inventoryService, never()).updateProductQuantity(any(), any());
    }

    @Test
    @DisplayName("Debería lanzar IllegalStateException cuando hay stock insuficiente")
    void testProcessPurchase_InsufficientStock() {
        // Given
        Integer requestedQuantity = 150;
        inventoryResponse.setAvailableQuantity(100);
        
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryService.getProductQuantity(productId)).thenReturn(inventoryResponse);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ordersService.processPurchase(productId, requestedQuantity);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(inventoryService, never()).updateProductQuantity(any(), any());
    }

    @Test
    @DisplayName("Debería calcular correctamente los totales de la compra")
    void testProcessPurchase_CalculateTotals() {
        // Given
        Integer purchaseQuantity = 3;
        BigDecimal unitPrice = new BigDecimal("20000.00");
        productDTO.setPrice(unitPrice);
        inventoryResponse.setAvailableQuantity(50);
        
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryService.getProductQuantity(productId)).thenReturn(inventoryResponse);
        when(inventoryService.updateProductQuantity(eq(productId), anyInt())).thenReturn(inventoryModel);

        // When
        PurchaseResponseDTO response = ordersService.processPurchase(productId, purchaseQuantity);

        // Then
        assertEquals(unitPrice, response.getUnitPrice());
        assertEquals(new BigDecimal("60000.00"), response.getSubtotal());
        assertEquals(new BigDecimal("60000.00"), response.getTotal());
    }
}

