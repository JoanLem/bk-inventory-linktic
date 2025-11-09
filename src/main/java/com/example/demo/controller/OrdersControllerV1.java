package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PurchaseRequestDTO;
import com.example.demo.dto.PurchaseResponseDTO;
import com.example.demo.service.OrdersServiceV1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Purchase Management", description = "API para gestión de las ordenes de ventas")
public class OrdersControllerV1 {

	private final OrdersServiceV1 ordersService;

	public OrdersControllerV1(OrdersServiceV1 ordersService) {
		this.ordersService = ordersService;
	}

	@Operation(summary = "Realizar compra de producto", description = "Procesa una compra de producto verificando disponibilidad en inventario, "
			+ "actualizando la cantidad disponible y retornando información detallada de la compra "
			+ "(producto, cantidad, subtotal, total y cantidad restante)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Compra realizada exitosamente", content = @Content(schema = @Schema(implementation = PurchaseResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Solicitud inválida (cantidad inválida o stock insuficiente)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content) })
	@PostMapping()
	public ResponseEntity<PurchaseResponseDTO> purchaseProduct(
			@Parameter(description = "ID del producto a comprar", required = true, example = "1")
			@Valid @RequestBody PurchaseRequestDTO request) {
		PurchaseResponseDTO purchaseResponse = ordersService.processPurchase(request.getProductId(), request.getQuantity());
		return ResponseEntity.ok(purchaseResponse);
	}
}
