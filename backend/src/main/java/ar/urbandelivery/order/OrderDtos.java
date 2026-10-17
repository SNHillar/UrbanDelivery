package ar.urbandelivery.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import ar.urbandelivery.history.StatusChangeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

	private OrderDtos() {
	}

	public record CreateOrderRequest(@NotNull Long merchantId, @NotNull FulfillmentType fulfillmentType,
			@Size(max = 500) String notes, @NotEmpty @Size(max = 50) List<@Valid @NotNull Item> items) {

		public record Item(@NotNull Long productId, @NotNull @Min(1) @Max(99) Integer quantity) {
		}

	}

	/** Motivo de rechazo o cancelación. Obligatorio o no según la transición. */
	public record ReasonRequest(@Size(max = 255) String reason) {
	}

	public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity,
			BigDecimal lineTotal) {

		static OrderItemResponse from(OrderItem i) {
			return new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
					i.getLineTotal());
		}

	}

	public record OrderResponse(Long id, Long merchantId, String merchantName, String merchantAddress,
			String clientName, String clientPhone, FulfillmentType fulfillmentType, OrderStatus status,
			String statusLabel, String statusReason, String notes, BigDecimal subtotal, BigDecimal deliveryFee,
			BigDecimal total, Instant createdAt, Instant acceptedAt, Instant readyAt, Instant completedAt,
			List<OrderItemResponse> items) {

		static OrderResponse from(Order o) {
			return new OrderResponse(o.getId(), o.getMerchant().getId(), o.getMerchant().getName(),
					o.getMerchant().getAddress(), o.getClient().getFullName(), o.getClient().getPhone(),
					o.getFulfillmentType(), o.getStatus(), o.getStatus().clientLabel(o.getFulfillmentType()),
					o.getStatusReason(), o.getNotes(), o.getSubtotal(), o.getDeliveryFee(), o.getTotal(),
					o.getCreatedAt(), o.getAcceptedAt(), o.getReadyAt(), o.getCompletedAt(),
					o.getItems().stream().map(OrderItemResponse::from).toList());
		}

	}

	public record OrderDetailResponse(OrderResponse order, List<StatusChangeResponse> history) {
	}

}
