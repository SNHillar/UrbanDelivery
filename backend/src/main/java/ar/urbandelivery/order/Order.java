package ar.urbandelivery.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.urbandelivery.catalog.Product;
import ar.urbandelivery.common.ApiException;
import ar.urbandelivery.merchant.Merchant;
import ar.urbandelivery.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Pedido. Las transiciones de estado viven acá para que ninguna capa pueda saltearse
 * la máquina de {@link OrderStatus}; quién puede ejecutarlas lo decide {@link OrderService}.
 */
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_id")
	private User client;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	@Enumerated(EnumType.STRING)
	@Column(name = "fulfillment_type", nullable = false, length = 10)
	private FulfillmentType fulfillmentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "delivery_address")
	private String deliveryAddress;

	@Column(name = "delivery_reference")
	private String deliveryReference;

	private String notes;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal deliveryFee = BigDecimal.ZERO;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@Column(name = "status_reason")
	private String statusReason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "accepted_at")
	private Instant acceptedAt;

	@Column(name = "ready_at")
	private Instant readyAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<OrderItem> items = new ArrayList<>();

	@Version
	private Long version;

	protected Order() {
	}

	/** Pedido con retiro en el local: sin dirección y sin costo de envío (invariante #9). */
	public static Order pickup(User client, Merchant merchant, String notes, Instant now) {
		Order order = new Order();
		order.client = client;
		order.merchant = merchant;
		order.fulfillmentType = FulfillmentType.PICKUP;
		order.status = OrderStatus.PENDING;
		order.notes = notes;
		order.createdAt = now;
		return order;
	}

	/** Copia nombre y precio del producto: el pedido no cambia si después cambia el catálogo (invariante #8). */
	public void addItem(Product product, int quantity) {
		OrderItem item = new OrderItem(this, product, quantity);
		items.add(item);
		subtotal = subtotal.add(item.getLineTotal());
		total = subtotal.add(deliveryFee);
	}

	public void accept(Instant now) {
		moveTo(OrderStatus.PREPARING);
		acceptedAt = now;
	}

	public void reject(String reason) {
		moveTo(OrderStatus.REJECTED);
		statusReason = reason;
	}

	public void markReady(Instant now) {
		moveTo(OrderStatus.READY);
		readyAt = now;
	}

	/** El cliente retiró en el local. Los pedidos con envío se completan al entregar. */
	public void completePickup(Instant now) {
		if (fulfillmentType != FulfillmentType.PICKUP) {
			throw ApiException.conflict("NOT_PICKUP", "Solo los pedidos con retiro se marcan como retirados");
		}
		moveTo(OrderStatus.COMPLETED);
		completedAt = now;
	}

	public void cancel(String reason) {
		moveTo(OrderStatus.CANCELLED);
		statusReason = reason;
	}

	private void moveTo(OrderStatus target) {
		if (!status.canTransitionTo(target)) {
			throw ApiException.conflict("INVALID_TRANSITION",
					"El pedido está en " + status + " y no puede pasar a " + target);
		}
		status = target;
	}

	public boolean isFrom(Long clientId) {
		return client.getId().equals(clientId);
	}

	public boolean isFor(Merchant other) {
		return merchant.getId().equals(other.getId());
	}

	public Long getId() {
		return id;
	}

	public User getClient() {
		return client;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public FulfillmentType getFulfillmentType() {
		return fulfillmentType;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public String getDeliveryReference() {
		return deliveryReference;
	}

	public String getNotes() {
		return notes;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public Instant getReadyAt() {
		return readyAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public List<OrderItem> getItems() {
		return items;
	}

}
