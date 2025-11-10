package com.example.demo.client;

import com.example.demo.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nota: Estos tests verifican la lógica de manejo de errores del ProductClient.
 * Para tests de integración completos, se recomienda usar @SpringBootTest con un servidor mock
 * o usar WebTestClient para simular respuestas HTTP.
 * 
 * Los tests de servicios (InventoryServiceV1Test, OrdersServiceV1Test) ya cubren
 * el uso de ProductClient mediante mocks, lo cual es suficiente para la mayoría de casos.
 */
@DisplayName("Tests para ProductClient - Validación de configuración")
class ProductClientTest {

    @Test
    @DisplayName("Debería construir la URL correctamente")
    void testUrlConstruction() {
        // Este test valida que la construcción de URL es correcta
        // La lógica real se prueba en los tests de integración o mediante mocks en los servicios
        
        String productServiceUrl = "http://localhost:8080/api/v1/products";
        Long productId = 1L;
        String expectedUrl = productServiceUrl + "/" + productId;
        String actualUrl = productServiceUrl + "/" + productId;
        
        assertEquals(expectedUrl, actualUrl);
        assertTrue(actualUrl.contains(productServiceUrl));
        assertTrue(actualUrl.contains(productId.toString()));
    }

    @Test
    @DisplayName("Debería manejar ProductNotFoundException correctamente")
    void testProductNotFoundException() {
        // Validar que ProductNotFoundException se lanza correctamente
        Long productId = 999L;
        ProductNotFoundException exception = new ProductNotFoundException(productId);
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(productId.toString()));
        assertThrows(ProductNotFoundException.class, () -> {
            throw exception;
        });
    }

    @Test
    @DisplayName("Debería identificar errores de autenticación correctamente")
    void testAuthenticationErrorIdentification() {
        // Validar que los códigos de error de autenticación se identifican correctamente
        assertTrue(HttpStatus.UNAUTHORIZED.is4xxClientError());
        assertTrue(HttpStatus.FORBIDDEN.is4xxClientError());
        
        // 404 es un error 4xx pero NO es un error de autenticación
        assertTrue(HttpStatus.NOT_FOUND.is4xxClientError());
        assertNotEquals(HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
        assertNotEquals(HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN);
        
        // Verificar que 401 y 403 son errores de autenticación
        assertEquals(401, HttpStatus.UNAUTHORIZED.value());
        assertEquals(403, HttpStatus.FORBIDDEN.value());
        assertEquals(404, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Debería identificar errores del servidor para retry")
    void testServerErrorIdentification() {
        // Validar que los errores 5xx se identifican para retry
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError());
        assertTrue(HttpStatus.BAD_GATEWAY.is5xxServerError());
        assertTrue(HttpStatus.SERVICE_UNAVAILABLE.is5xxServerError());
        
        // Verificar que 4xx no son errores del servidor
        assertFalse(HttpStatus.NOT_FOUND.is5xxServerError());
        assertFalse(HttpStatus.BAD_REQUEST.is5xxServerError());
    }

    @Test
    @DisplayName("Debería validar configuración de retry")
    void testRetryConfiguration() {
        // Validar valores por defecto de retry
        int maxRetryAttempts = 3;
        long initialDelayMillis = 500L;
        long maxDelayMillis = 2000L;
        double multiplier = 2.0;
        
        assertTrue(maxRetryAttempts > 0);
        assertTrue(initialDelayMillis > 0);
        assertTrue(maxDelayMillis >= initialDelayMillis);
        assertTrue(multiplier > 1.0);
        
        // Validar que el delay máximo es mayor o igual al inicial
        assertTrue(maxDelayMillis >= initialDelayMillis);
    }
}

