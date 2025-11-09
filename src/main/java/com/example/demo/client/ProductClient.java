package com.example.demo.client;

import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
@Slf4j
public class ProductClient {

	private final WebClient webClient;

	@Value("${product.service.url}")
	private String productServiceUrl;

	public ProductClient(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl(productServiceUrl).build();
	}

	public ProductDTO getProductById(Long productId) {
		try {
			String url = productServiceUrl + "/" + productId;
			log.info("Consultando producto con ID: {} desde: {}{}", productId, productServiceUrl, url);

			ProductDTO productResponse = webClient
					.get()
					.uri(url)
					.retrieve()
					.bodyToMono(ProductDTO.class)
					.timeout(Duration.ofSeconds(10))
					.block();

			if (productResponse != null) {
				log.info("Producto encontrado: ID={}, Name={}", productResponse.getId(), productResponse.getName());
				return productResponse;
			} else {
				log.warn("Producto no encontrado con ID: {}", productId);
				throw new ProductNotFoundException(productId);
			}
		} catch (WebClientResponseException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				log.warn("Producto no encontrado con ID: {} (404)", productId);
				throw new ProductNotFoundException(productId);
			} else {
				log.error("Error al consultar el producto con ID: {}. Status: {}, Error: {}", 
						productId, e.getStatusCode(), e.getMessage());
				throw new RuntimeException("Error al consultar el microservicio de productos: " + e.getMessage(), e);
			}
		} catch (ProductNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al consultar el producto con ID: {}. Error: {}", productId, e.getMessage());
			throw new RuntimeException("Error inesperado al consultar el producto: " + e.getMessage(), e);
		}
	}
}
