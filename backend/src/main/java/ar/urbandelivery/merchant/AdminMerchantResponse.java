package ar.urbandelivery.merchant;

import java.math.BigDecimal;

public record AdminMerchantResponse(Long id, String name, String category, String address, String phone,
		String openingHours, boolean offersPickup, boolean offersDelivery, BigDecimal deliveryFee,
		ApprovalStatus approvalStatus, String ownerFullName, String ownerEmail) {

	public static AdminMerchantResponse from(Merchant m) {
		return new AdminMerchantResponse(m.getId(), m.getName(), m.getCategory(), m.getAddress(), m.getPhone(),
				m.getOpeningHours(), m.isOffersPickup(), m.isOffersDelivery(), m.getDeliveryFee(),
				m.getApprovalStatus(), m.getOwner().getFullName(), m.getOwner().getEmail());
	}

}
