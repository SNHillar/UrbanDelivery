package ar.urbandelivery.catalog;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public final class CatalogDtos {

	private CatalogDtos() {
	}

	public record ProductResponse(Long id, Long merchantId, String name, String description, BigDecimal price,
			boolean available) {

		static ProductResponse from(Product p) {
			return new ProductResponse(p.getId(), p.getMerchant().getId(), p.getName(), p.getDescription(),
					p.getPrice(), p.isAvailable());
		}

	}

	public record AvailabilityRequest(@NotNull Boolean available) {
	}

}
