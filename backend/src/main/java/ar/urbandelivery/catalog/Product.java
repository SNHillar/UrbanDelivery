package ar.urbandelivery.catalog;

import java.math.BigDecimal;

import ar.urbandelivery.merchant.Merchant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	@Column(nullable = false, length = 120)
	private String name;

	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	/** Toggle rápido "hay / no hay". Sin inventario. */
	@Column(nullable = false)
	private boolean available;

	/** Baja lógica: los pedidos históricos siguen referenciando el producto. */
	@Column(nullable = false)
	private boolean active;

	protected Product() {
	}

	public boolean belongsTo(Merchant other) {
		return merchant.getId().equals(other.getId());
	}

	public boolean isOrderable() {
		return active && available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Long getId() {
		return id;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public boolean isAvailable() {
		return available;
	}

	public boolean isActive() {
		return active;
	}

}
