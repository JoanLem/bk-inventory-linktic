package com.example.demo.controller;

import com.example.demo.dto.InventoryResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.UpdateInventoryRequestDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.model.InventoryModel;
import com.example.demo.service.InventoryServiceV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryControllerV1.class, 
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@TestPropertySource(properties = {
        "product.service.url=http://localhost:8080/api/v1/products",
        "product.service.api-key=test-api-key",
        "product.service.timeout-seconds=10"
})
@DisplayName("Tests para InventoryControllerV1")
class InventoryControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryServiceV1 inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long productId;
    private InventoryResponseDTO inventoryResponse;
    private InventoryModel inventoryModel;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productId = 1L;

        productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Producto Test");
        productDTO.setDescription("Descripción del producto");
        productDTO.setPrice(new BigDecimal("15000.00"));

        inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setProductId(productId);
        inventoryResponse.setAvailableQuantity(100);
        inventoryResponse.setProduct(productDTO);

        inventoryModel = new InventoryModel();
        inventoryModel.setId(1L);
        inventoryModel.setProductId(productId);
        inventoryModel.setQuantity(100);
    }

    @Test
    @DisplayName("Debería retornar 200 en el health check")
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("API bk-inventory is working"));
    }

    @Test
    @DisplayName("Debería obtener la cantidad disponible de un producto exitosamente")
    void testGetProductQuantity_Success() throws Exception {
        // Given
        when(inventoryService.getProductQuantity(productId)).thenReturn(inventoryResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/products/{productId}/quantity", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.product.id").value(productId))
                .andExpect(jsonPath("$.product.name").value("Producto Test"));

        verify(inventoryService, times(1)).getProductQuantity(productId);
    }

    @Test
    @DisplayName("Debería retornar 404 cuando el producto no existe")
    void testGetProductQuantity_ProductNotFound() throws Exception {
        // Given
        when(inventoryService.getProductQuantity(productId))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/products/{productId}/quantity", productId))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getProductQuantity(productId);
    }

    @Test
    @DisplayName("Debería actualizar la cantidad de inventario exitosamente")
    void testUpdateProductQuantity_Success() throws Exception {
        // Given
        UpdateInventoryRequestDTO request = new UpdateInventoryRequestDTO();
        request.setProductId(productId);
        request.setQuantity(150);

        when(inventoryService.updateProductQuantity(eq(productId), eq(150)))
                .thenReturn(inventoryModel);

        // When & Then
        mockMvc.perform(put("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(inventoryService, times(1)).updateProductQuantity(eq(productId), eq(150));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando la cantidad es negativa")
    void testUpdateProductQuantity_NegativeQuantity() throws Exception {
        // Given
        UpdateInventoryRequestDTO request = new UpdateInventoryRequestDTO();
        request.setProductId(productId);
        request.setQuantity(-10);

        // When & Then
        mockMvc.perform(put("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(inventoryService, never()).updateProductQuantity(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el request es inválido")
    void testUpdateProductQuantity_InvalidRequest() throws Exception {
        // Given
        String invalidJson = "{\"productId\": null, \"quantity\": null}";

        // When & Then
        mockMvc.perform(put("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).updateProductQuantity(any(), any());
    }
}

