package ar.urbandelivery.order;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.catalog.Product;
import ar.urbandelivery.catalog.ProductRepository;
import ar.urbandelivery.common.ApiException;
import ar.urbandelivery.history.HistoryEntity;
import ar.urbandelivery.history.StatusHistoryService;
import ar.urbandelivery.merchant.Merchant;
import ar.urbandelivery.merchant.MerchantService;
import ar.urbandelivery.order.OrderDtos.CreateOrderRequest;
import ar.urbandelivery.order.OrderDtos.OrderDetailResponse;
import ar.urbandelivery.order.OrderDtos.OrderResponse;
import ar.urbandelivery.user.Role;
import ar.urbandelivery.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del pedido. Cada transición cambia el estado y registra el historial
 * en la misma transacción (invariante #11) y verifica quién la ejecuta (invariante #12).
 */
@Service
public class OrderService {

	private final OrderRepository orders;
	private final ProductRepository products;
	private final UserRepository users;
	private final MerchantService merchantService;
	private final StatusHistoryService history;
	private final Clock clock;

	public OrderService(OrderRepository orders, ProductRepository products, UserRepository users,
			MerchantService merchantService, StatusHistoryService history, Clock clock) {
		this.orders = orders;
		this.products = products;
		this.users = users;
		this.merchantService = merchantService;
		this.history = history;
		this.clock = clock;
	}

	@Transactional
	public OrderDetailResponse create(AuthenticatedUser client, CreateOrderRequest request) {
		if (request.fulfillmentType() == FulfillmentType.DELIVERY) {
			// El envío con cadete depende de validar el actor COURIER (3.ª devolución)
			throw ApiException.businessRule("DELIVERY_NOT_AVAILABLE",
					"El envío todavía no está disponible. Por ahora los pedidos son con retiro en el local");
		}
		Merchant merchant = merchantService.findApproved(request.merchantId());
		if (!merchant.isOffersPickup()) {
			throw ApiException.businessRule("PICKUP_NOT_OFFERED", "El comercio no ofrece retiro en el local");
		}

		Map<Long, Integer> quantities = request.items().stream()
			.collect(Collectors.toMap(CreateOrderRequest.Item::productId, CreateOrderRequest.Item::quantity,
					Integer::sum, LinkedHashMap::new));
		Map<Long, Product> found = products.findAllById(quantities.keySet()).stream()
			.collect(Collectors.toMap(Product::getId, Function.identity()));

		Order order = Order.pickup(users.getReferenceById(client.id()), merchant, blankToNull(request.notes()),
				clock.instant());
		quantities.forEach((productId, quantity) -> {
			Product product = found.get(productId);
			// Invariante #4: todos los ítems del mismo comercio
			if (product == null || !product.belongsTo(merchant)) {
				throw ApiException.businessRule("PRODUCT_NOT_IN_MERCHANT",
						"El producto " + productId + " no pertenece a " + merchant.getName());
			}
			// Invariante #5: solo productos activos y disponibles
			if (!product.isOrderable()) {
				throw ApiException.businessRule("PRODUCT_UNAVAILABLE", product.getName() + " no está disponible");
			}
			order.addItem(product, quantity);
		});

		orders.save(order);
		history.record(order.getId(), HistoryEntity.ORDER, null, order.getStatus(), client.id(), null);
		return detail(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> listForClient(AuthenticatedUser client) {
		return orders.findByClientIdOrderByCreatedAtDesc(client.id()).stream().map(OrderResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> listForMerchant(AuthenticatedUser owner, List<OrderStatus> statuses) {
		Merchant merchant = merchantService.findOwnedBy(owner.id());
		List<Order> result = statuses == null || statuses.isEmpty()
				? orders.findByMerchantIdOrderByCreatedAtDesc(merchant.getId())
				: orders.findByMerchantIdAndStatusInOrderByCreatedAtAsc(merchant.getId(), statuses);
		return result.stream().map(OrderResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public OrderDetailResponse get(AuthenticatedUser current, Long orderId) {
		Order order = load(orderId);
		boolean allowed = switch (current.role()) {
			case CLIENT -> order.isFrom(current.id());
			case MERCHANT -> order.getMerchant().isOwnedBy(current.id());
			case ADMIN -> true;
			case COURIER -> false;
		};
		if (!allowed) {
			throw ApiException.forbidden("No tenés acceso a este pedido");
		}
		return detail(order);
	}

	@Transactional
	public OrderDetailResponse accept(AuthenticatedUser owner, Long orderId) {
		Order order = loadForMerchant(owner, orderId);
		OrderStatus from = order.getStatus();
		order.accept(clock.instant());
		return recordAndDetail(order, from, owner, null);
	}

	@Transactional
	public OrderDetailResponse reject(AuthenticatedUser owner, Long orderId, String reason) {
		Order order = loadForMerchant(owner, orderId);
		String required = requireReason(reason, "Indicá el motivo del rechazo");
		OrderStatus from = order.getStatus();
		order.reject(required);
		return recordAndDetail(order, from, owner, required);
	}

	@Transactional
	public OrderDetailResponse markReady(AuthenticatedUser owner, Long orderId) {
		Order order = loadForMerchant(owner, orderId);
		OrderStatus from = order.getStatus();
		order.markReady(clock.instant());
		return recordAndDetail(order, from, owner, null);
	}

	@Transactional
	public OrderDetailResponse completePickup(AuthenticatedUser owner, Long orderId) {
		Order order = loadForMerchant(owner, orderId);
		OrderStatus from = order.getStatus();
		order.completePickup(clock.instant());
		return recordAndDetail(order, from, owner, null);
	}

	/**
	 * El cliente cancela solo mientras el comercio no aceptó, sin motivo obligatorio.
	 * Después de aceptado cancela solo el comercio, con motivo. Un pedido pendiente el comercio lo rechaza.
	 */
	@Transactional
	public OrderDetailResponse cancel(AuthenticatedUser current, Long orderId, String reason) {
		Order order = load(orderId);
		OrderStatus from = order.getStatus();
		String finalReason;
		if (current.is(Role.CLIENT)) {
			if (!order.isFrom(current.id())) {
				throw ApiException.forbidden("No tenés acceso a este pedido");
			}
			if (from != OrderStatus.PENDING && from.canTransitionTo(OrderStatus.CANCELLED)) {
				throw ApiException.conflict("CLIENT_CANCEL_NOT_ALLOWED",
						"El comercio ya aceptó el pedido. Para cancelarlo comunicate con el comercio");
			}
			finalReason = blankToNull(reason);
		}
		else if (current.is(Role.MERCHANT)) {
			if (!order.getMerchant().isOwnedBy(current.id())) {
				throw ApiException.forbidden("El pedido no pertenece a tu comercio");
			}
			if (from == OrderStatus.PENDING) {
				throw ApiException.conflict("USE_REJECT", "Un pedido pendiente se rechaza, no se cancela");
			}
			finalReason = requireReason(reason, "Indicá el motivo de la cancelación");
		}
		else {
			throw ApiException.forbidden("No podés cancelar pedidos");
		}
		order.cancel(finalReason);
		return recordAndDetail(order, from, current, finalReason);
	}

	private Order load(Long orderId) {
		return orders.findDetailedById(orderId).orElseThrow(() -> ApiException.notFound("Pedido inexistente"));
	}

	private Order loadForMerchant(AuthenticatedUser owner, Long orderId) {
		Merchant merchant = merchantService.findOwnedBy(owner.id());
		Order order = load(orderId);
		if (!order.isFor(merchant)) {
			throw ApiException.forbidden("El pedido no pertenece a tu comercio");
		}
		return order;
	}

	private OrderDetailResponse recordAndDetail(Order order, OrderStatus from, AuthenticatedUser actor,
			String reason) {
		// flush: si otro actor cambió el pedido a la vez, @Version falla acá y no se registra historial
		orders.saveAndFlush(order);
		history.record(order.getId(), HistoryEntity.ORDER, from, order.getStatus(), actor.id(), reason);
		return detail(order);
	}

	private OrderDetailResponse detail(Order order) {
		return new OrderDetailResponse(OrderResponse.from(order), history.forOrder(order.getId()));
	}

	private static String requireReason(String reason, String message) {
		String value = blankToNull(reason);
		if (value == null) {
			throw ApiException.badRequest("REASON_REQUIRED", message);
		}
		return value;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
