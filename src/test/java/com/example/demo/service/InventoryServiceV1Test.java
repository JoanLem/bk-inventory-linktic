package com.example.demo.service;

import com.example.demo.client.ProductClient;
import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.model.InventoryModel;
import com.example.demo.repository.InventoryRepo;
import com.example.demo.repository.PurchaseHistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para InventoryServiceV1")
class InventoryServiceV1Test {

    @Mock
    private InventoryRepo inventoryRepo;

    @Mock
    private PurchaseHistoryRepo purchaseHistoryRepo;

    @Mock
    private ProductClient productClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceV1 inventoryService;

    private ProductDTO productDTO;
    private InventoryModel inventoryModel;
    private Long productId;

    @BeforeEach
    void setUp() {
        productId = 1L;
        
        productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Producto Test");
        productDTO.setDescription("Descripción del producto");
        productDTO.setPrice(new BigDecimal("15000.00"));

        inventoryModel = new InventoryModel();
        inventoryModel.setId(1L);
        inventoryModel.setProductId(productId);
        inventoryModel.setQuantity(100);
    }

    @Test
    @DisplayName("Debería obtener la cantidad disponible de un producto existente")
    void testGetProductQuantity_ProductExists() {
        // Given
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryRepo.findByProductId(productId)).thenReturn(Optional.of(inventoryModel));

        // When
        InventoryResponseDTO response = inventoryService.getProductQuantity(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(100, response.getAvailableQuantity());
        assertNotNull(response.getProduct());
        assertEquals(productDTO.getId(), response.getProduct().getId());
        
        verify(productClient, times(1)).getProductById(productId);
        verify(inventoryRepo, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("Debería retornar cantidad 0 cuando el producto no tiene inventario")
    void testGetProductQuantity_ProductWithoutInventory() {
        // Given
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryRepo.findByProductId(productId)).thenReturn(Optional.empty());

        // When
        InventoryResponseDTO response = inventoryService.getProductQuantity(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(0, response.getAvailableQuantity());
        assertNotNull(response.getProduct());
    }

    @Test
    @DisplayName("Debería lanzar ProductNotFoundException cuando el producto no existe")
    void testGetProductQuantity_ProductNotFound() {
        // Given
        when(productClient.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            inventoryService.getProductQuantity(productId);
        });

        verify(productClient, times(1)).getProductById(productId);
        verify(inventoryRepo, never()).findByProductId(any());
    }

    @Test
    @DisplayName("Debería actualizar la cantidad de un producto existente")
    void testUpdateProductQuantity_ProductExists() {
        // Given
        Integer newQuantity = 150;
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryRepo.findByProductId(productId)).thenReturn(Optional.of(inventoryModel));
        when(inventoryRepo.save(any(InventoryModel.class))).thenReturn(inventoryModel);

        // When
        InventoryModel result = inventoryService.updateProductQuantity(productId, newQuantity);

        // Then
        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());
        verify(inventoryRepo, times(1)).save(any(InventoryModel.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Debería crear un nuevo registro de inventario cuando no existe")
    void testUpdateProductQuantity_CreateNewInventory() {
        // Given
        Integer newQuantity = 50;
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryRepo.findByProductId(productId)).thenReturn(Optional.empty());
        when(inventoryRepo.save(any(InventoryModel.class))).thenAnswer(invocation -> {
            InventoryModel inv = invocation.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        // When
        InventoryModel result = inventoryService.updateProductQuantity(productId, newQuantity);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(newQuantity, result.getQuantity());
        verify(inventoryRepo, times(1)).save(any(InventoryModel.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando la cantidad es negativa")
    void testUpdateProductQuantity_NegativeQuantity() {
        // Given
        Integer negativeQuantity = -10;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateProductQuantity(productId, negativeQuantity);
        });

        // Verificar el mensaje de la excepción
        assertEquals("La cantidad no puede ser negativa", exception.getMessage());
        
        // Verificar que no se llamó a ningún método del repositorio o cliente
        verify(productClient, never()).getProductById(any());
        verify(inventoryRepo, never()).findByProductId(any());
        verify(inventoryRepo, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Debería permitir cantidad cero al actualizar inventario")
    void testUpdateProductQuantity_ZeroQuantity() {
        // Given
        Integer zeroQuantity = 0;
        when(productClient.getProductById(productId)).thenReturn(productDTO);
        when(inventoryRepo.findByProductId(productId)).thenReturn(Optional.of(inventoryModel));
        when(inventoryRepo.save(any(InventoryModel.class))).thenReturn(inventoryModel);

        // When
        InventoryModel result = inventoryService.updateProductQuantity(productId, zeroQuantity);

        // Then
        assertNotNull(result);
        assertEquals(zeroQuantity, result.getQuantity());
        verify(inventoryRepo, times(1)).save(any(InventoryModel.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el producto no existe al actualizar")
    void testUpdateProductQuantity_ProductNotFound() {
        // Given
        Integer newQuantity = 50;
        when(productClient.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            inventoryService.updateProductQuantity(productId, newQuantity);
        });

        verify(inventoryRepo, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}

