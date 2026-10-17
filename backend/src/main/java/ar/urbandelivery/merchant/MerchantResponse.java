package ar.urbandelivery.merchant;

import java.math.BigDecimal;

public record MerchantResponse(Long id, String name, String category, String address, String phone,
		String openingHours, boolean offersPickup, boolean offersDelivery, BigDecimal deliveryFee) {

	public static MerchantResponse from(Merchant m) {
		return new MerchantResponse(m.getId(), m.getName(), m.getCategory(), m.getAddress(), m.getPhone(),
				m.getOpeningHours(), m.isOffersPickup(), m.isOffersDelivery(), m.getDeliveryFee());
	}

}
