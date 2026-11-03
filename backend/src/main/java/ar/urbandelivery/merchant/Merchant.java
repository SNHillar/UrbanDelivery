package ar.urbandelivery.merchant;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchants")
public class Merchant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_user_id", unique = true)
	private User owner;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 60)
	private String category;

	@Column(nullable = false)
	private String address;

	@Column(length = 30)
	private String phone;

	@Column(name = "opening_hours")
	private String openingHours;

	@Column(name = "offers_pickup", nullable = false)
	private boolean offersPickup;

	@Column(name = "offers_delivery", nullable = false)
	private boolean offersDelivery;

	@Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal deliveryFee;

	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status", nullable = false, length = 20)
	private ApprovalStatus approvalStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by")
	private User approvedBy;

	@Column(name = "approved_at")
	private Instant approvedAt;

	protected Merchant() {
	}

	public boolean isOwnedBy(Long userId) {
		return owner.getId().equals(userId);
	}

	public boolean isApproved() {
		return approvalStatus == ApprovalStatus.APPROVED;
	}

	public Long getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	public boolean isOffersPickup() {
		return offersPickup;
	}

	public boolean isOffersDelivery() {
		return offersDelivery;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

}
