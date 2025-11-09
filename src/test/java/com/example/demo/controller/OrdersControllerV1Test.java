package com.example.demo.controller;

import com.example.demo.dto.PurchaseRequestDTO;
import com.example.demo.dto.PurchaseResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.service.OrdersServiceV1;
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

@WebMvcTest(controllers = OrdersControllerV1.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@TestPropertySource(properties = {
        "product.service.url=http://localhost:8080/api/v1/products"
})
@DisplayName("Tests para OrdersControllerV1")
class OrdersControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdersServiceV1 ordersService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long productId;
    private Integer quantity;
    private PurchaseRequestDTO purchaseRequest;
    private PurchaseResponseDTO purchaseResponse;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productId = 1L;
        quantity = 5;

        productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Producto Test");
        productDTO.setDescription("Descripción del producto");
        productDTO.setPrice(new BigDecimal("15000.00"));

        purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setProductId(productId);
        purchaseRequest.setQuantity(quantity);

        purchaseResponse = new PurchaseResponseDTO();
        purchaseResponse.setProductId(productId);
        purchaseResponse.setProduct(productDTO);
        purchaseResponse.setQuantity(quantity);
        purchaseResponse.setUnitPrice(new BigDecimal("15000.00"));
        purchaseResponse.setSubtotal(new BigDecimal("75000.00"));
        purchaseResponse.setTotal(new BigDecimal("75000.00"));
        purchaseResponse.setRemainingQuantity(95);
    }

    @Test
    @DisplayName("Debería procesar una compra exitosamente")
    void testPurchaseProduct_Success() throws Exception {
        // Given
        when(ordersService.processPurchase(eq(productId), eq(quantity)))
                .thenReturn(purchaseResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.quantity").value(quantity))
                .andExpect(jsonPath("$.unitPrice").value(15000.00))
                .andExpect(jsonPath("$.subtotal").value(75000.00))
                .andExpect(jsonPath("$.total").value(75000.00))
                .andExpect(jsonPath("$.remainingQuantity").value(95))
                .andExpect(jsonPath("$.product.id").value(productId));

        verify(ordersService, times(1)).processPurchase(eq(productId), eq(quantity));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando la cantidad es inválida (validación de Spring)")
    void testPurchaseProduct_InvalidQuantity() throws Exception {
        // Given
        purchaseRequest.setQuantity(0);
        // Nota: La validación de @Valid rechaza el request antes de llegar al servicio
        // por lo que processPurchase nunca se invoca

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isBadRequest());

        // La validación de Spring rechaza el request antes de llegar al servicio
        verify(ordersService, never()).processPurchase(any(), any());
    }
    
    @Test
    @DisplayName("Debería retornar 400 cuando la cantidad es negativa (validación de Spring)")
    void testPurchaseProduct_NegativeQuantity() throws Exception {
        // Given
        purchaseRequest.setQuantity(-5);
        // Nota: La validación de @Valid rechaza el request antes de llegar al servicio

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isBadRequest());

        // La validación de Spring rechaza el request antes de llegar al servicio
        verify(ordersService, never()).processPurchase(any(), any());
    }

    @Test
    @DisplayName("Debería retornar 404 cuando el producto no existe")
    void testPurchaseProduct_ProductNotFound() throws Exception {
        // Given
        when(ordersService.processPurchase(eq(productId), eq(quantity)))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isNotFound());

        verify(ordersService, times(1)).processPurchase(eq(productId), eq(quantity));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando hay stock insuficiente")
    void testPurchaseProduct_InsufficientStock() throws Exception {
        // Given
        when(ordersService.processPurchase(eq(productId), eq(quantity)))
                .thenThrow(new IllegalStateException("Stock insuficiente. Disponible: 2, Solicitado: 5"));

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isBadRequest());

        verify(ordersService, times(1)).processPurchase(eq(productId), eq(quantity));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el request es inválido")
    void testPurchaseProduct_InvalidRequest() throws Exception {
        // Given
        String invalidJson = "{\"productId\": null, \"quantity\": -1}";

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(ordersService, never()).processPurchase(any(), any());
    }

    @Test
    @DisplayName("Debería retornar 400 cuando faltan campos requeridos")
    void testPurchaseProduct_MissingFields() throws Exception {
        // Given
        String incompleteJson = "{\"productId\": 1}";

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest());

        verify(ordersService, never()).processPurchase(any(), any());
    }
}

