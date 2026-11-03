package ar.urbandelivery.order;

import java.util.List;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.order.OrderDtos.CreateOrderRequest;
import ar.urbandelivery.order.OrderDtos.OrderDetailResponse;
import ar.urbandelivery.order.OrderDtos.OrderResponse;
import ar.urbandelivery.order.OrderDtos.ReasonRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/orders")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('CLIENT')")
	public OrderDetailResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrderRequest request) {
		return orderService.create(AuthenticatedUser.from(jwt), request);
	}

	@GetMapping("/orders/mine")
	@PreAuthorize("hasRole('CLIENT')")
	public List<OrderResponse> mine(@AuthenticationPrincipal Jwt jwt) {
		return orderService.listForClient(AuthenticatedUser.from(jwt));
	}

	@GetMapping("/merchant/orders")
	@PreAuthorize("hasRole('MERCHANT')")
	public List<OrderResponse> merchantOrders(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(name = "status", required = false) List<OrderStatus> statuses) {
		return orderService.listForMerchant(AuthenticatedUser.from(jwt), statuses);
	}

	@GetMapping("/orders/{id}")
	public OrderDetailResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return orderService.get(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping("/orders/{id}/accept")
	@PreAuthorize("hasRole('MERCHANT')")
	public OrderDetailResponse accept(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return orderService.accept(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping("/orders/{id}/reject")
	@PreAuthorize("hasRole('MERCHANT')")
	public OrderDetailResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody ReasonRequest request) {
		return orderService.reject(AuthenticatedUser.from(jwt), id, request.reason());
	}

	@PostMapping("/orders/{id}/ready")
	@PreAuthorize("hasRole('MERCHANT')")
	public OrderDetailResponse ready(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return orderService.markReady(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping("/orders/{id}/complete")
	@PreAuthorize("hasRole('MERCHANT')")
	public OrderDetailResponse complete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return orderService.completePickup(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping("/orders/{id}/cancel")
	@PreAuthorize("hasAnyRole('CLIENT', 'MERCHANT')")
	public OrderDetailResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody(required = false) ReasonRequest request) {
		return orderService.cancel(AuthenticatedUser.from(jwt), id, request == null ? null : request.reason());
	}

}
