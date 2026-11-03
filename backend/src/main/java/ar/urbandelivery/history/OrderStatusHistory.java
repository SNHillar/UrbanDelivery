package ar.urbandelivery.history;

import java.time.Instant;

import ar.urbandelivery.user.User;
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
import jakarta.persistence.Table;

/**
 * Una fila por transición. Los estados se guardan como texto porque la tabla registra
 * tanto la máquina del pedido como la de la entrega.
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id", nullable = false)
	private Long orderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private HistoryEntity entity;

	@Column(name = "from_status", length = 20)
	private String fromStatus;

	@Column(name = "to_status", nullable = false, length = 20)
	private String toStatus;

	/** Null cuando el cambio lo hace el sistema. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_user_id")
	private User actor;

	private String reason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected OrderStatusHistory() {
	}

	OrderStatusHistory(Long orderId, HistoryEntity entity, String fromStatus, String toStatus, User actor,
			String reason, Instant createdAt) {
		this.orderId = orderId;
		this.entity = entity;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.actor = actor;
		this.reason = reason;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public HistoryEntity getEntity() {
		return entity;
	}

	public String getFromStatus() {
		return fromStatus;
	}

	public String getToStatus() {
		return toStatus;
	}

	public User getActor() {
		return actor;
	}

	public String getReason() {
		return reason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
