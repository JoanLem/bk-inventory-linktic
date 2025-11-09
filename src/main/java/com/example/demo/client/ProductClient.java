package com.example.demo.client;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductAttributesDTO;
import com.example.demo.dto.ProductResponseDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@Slf4j
public class ProductClient {
    
    
    private final WebClient webClient;
    
    @Value("${product.service.url}")
    private String productServiceUrl;
    
    public ProductClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(productServiceUrl)
                .build();
    }
    
    public ProductDTO getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            log.info("Consultando producto con ID: {} desde: {}{}", productId, productServiceUrl, url);
            
            ProductResponseDTO productResponse = webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(ProductResponseDTO.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            if (productResponse != null && 
                productResponse.getData() != null && 
                productResponse.getData().getAttributes() != null) {
                
                ProductAttributesDTO attributes = productResponse.getData().getAttributes();
                
                // Mapear ProductAttributesDTO a ProductDTO
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(attributes.getId());
                productDTO.setName(attributes.getName());
                productDTO.setPrice(attributes.getPrice());
                productDTO.setDescription(attributes.getDescription());
                
                log.info("Producto encontrado: ID={}, Name={}", productDTO.getId(), productDTO.getName());
                return productDTO;
            } else {
                log.warn("Estructura de respuesta inválida o producto no encontrado con ID: {}", productId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error al consultar el producto con ID: {}. Error: {}", productId, e.getMessage());
            return null;
        }
    }
}

