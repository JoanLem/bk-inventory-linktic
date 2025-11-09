package com.example.demo.client;

import com.example.demo.dto.ProductDTO;
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
		this.webClient = webClientBuilder.baseUrl(productServiceUrl).build();
	}

	public ProductDTO getProductById(Long productId) {
		try {
			String url = productServiceUrl + "/" + productId;
			log.info("Consultando producto con ID: {} desde: {}{}", productId, productServiceUrl, url);

			ProductDTO productResponse = webClient.get().uri(url).retrieve().bodyToMono(ProductDTO.class)
					.timeout(Duration.ofSeconds(10)).block();

			if (productResponse != null) {
				log.info("Producto encontrado: ID={}, Name={}", productResponse.getId(), productResponse.getName());
				return productResponse;
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
