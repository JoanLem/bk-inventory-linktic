package com.example.demo.client;

import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class ProductClient {

	private final WebClient webClient;

	@Value("${product.service.url}")
	private String productServiceUrl;

	@Value("${product.service.timeout-seconds:10}")
	private int timeoutSeconds;

	@Value("${product.service.retry.max-attempts:3}")
	private int maxRetryAttempts;

	@Value("${product.service.retry.initial-delay-millis:500}")
	private long initialDelayMillis;

	@Value("${product.service.retry.max-delay-millis:2000}")
	private long maxDelayMillis;

	@Value("${product.service.retry.multiplier:2.0}")
	private double multiplier;

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
					.timeout(Duration.ofSeconds(timeoutSeconds))
					.retryWhen(createRetrySpec())
					.doOnError(error -> {
						if (error instanceof WebClientResponseException) {
							WebClientResponseException ex = (WebClientResponseException) error;
							log.error("Error HTTP al consultar producto ID: {}. Status: {}, Error: {}", 
									productId, ex.getStatusCode(), ex.getMessage());
						} else {
							log.error("Error de conexión/timeout al consultar producto ID: {}. Error: {}", 
									productId, error.getMessage());
						}
					})
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
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Error de autenticación al consultar producto ID: {}. Status: {}", 
						productId, e.getStatusCode());
				throw new RuntimeException("Error de autenticación con el microservicio de productos. " +
						"Verifique la API Key configurada.", e);
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

	/**
	 * Crea la especificación de retry con backoff exponencial
	 * Solo reintenta en errores de conexión/timeout, no en errores HTTP 4xx/5xx
	 */
	private Retry createRetrySpec() {
		return Retry.backoff(maxRetryAttempts, Duration.ofMillis(initialDelayMillis))
				.maxBackoff(Duration.ofMillis(maxDelayMillis))
				.multiplier(multiplier)
				.filter(throwable -> {
					// Solo reintentar en errores de conexión/timeout, no en errores HTTP
					if (throwable instanceof WebClientResponseException) {
						WebClientResponseException ex = (WebClientResponseException) throwable;
						// No reintentar en errores 4xx (excepto 408 Request Timeout)
						if (ex.getStatusCode().is4xxClientError() && 
								ex.getStatusCode() != HttpStatus.REQUEST_TIMEOUT) {
							return false;
						}
						// Reintentar en errores 5xx y 408
						return ex.getStatusCode().is5xxServerError() || 
								ex.getStatusCode() == HttpStatus.REQUEST_TIMEOUT;
					}
					// Reintentar en errores de conexión/timeout
					return true;
				})
				.doBeforeRetry(retrySignal -> {
					log.warn("Reintentando petición al servicio de productos. Intento: {}/{}", 
							retrySignal.totalRetries() + 1, maxRetryAttempts);
				})
				.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
					log.error("Agotados todos los reintentos ({}) para consultar producto", maxRetryAttempts);
					return retrySignal.failure();
				});
	}
}
