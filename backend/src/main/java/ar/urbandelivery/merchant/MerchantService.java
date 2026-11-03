package ar.urbandelivery.merchant;

import java.util.List;

import ar.urbandelivery.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

	private final MerchantRepository merchants;

	public MerchantService(MerchantRepository merchants) {
		this.merchants = merchants;
	}

	@Transactional(readOnly = true)
	public List<MerchantResponse> listApproved() {
		return merchants.findByApprovalStatusOrderByNameAsc(ApprovalStatus.APPROVED).stream()
			.map(MerchantResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public MerchantResponse getApproved(Long id) {
		return MerchantResponse.from(findApproved(id));
	}

	/** Los comercios pendientes o rechazados no son visibles para el público. */
	public Merchant findApproved(Long id) {
		return merchants.findById(id)
			.filter(Merchant::isApproved)
			.orElseThrow(() -> ApiException.notFound("Comercio inexistente"));
	}

	public Merchant findOwnedBy(Long userId) {
		return merchants.findByOwnerId(userId)
			.orElseThrow(() -> ApiException.forbidden("El usuario no tiene un comercio asociado"));
	}

}
