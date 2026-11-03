package ar.urbandelivery.merchant;

import java.time.Clock;
import java.util.List;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.common.ApiException;
import ar.urbandelivery.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

	private final MerchantRepository merchants;
	private final UserRepository users;
	private final Clock clock;

	public MerchantService(MerchantRepository merchants, UserRepository users, Clock clock) {
		this.merchants = merchants;
		this.users = users;
		this.clock = clock;
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

	/** Bandeja de moderación del ADMIN (módulo "admin" de docs/05-modulos.md). */
	@Transactional(readOnly = true)
	public List<AdminMerchantResponse> listByStatus(ApprovalStatus status) {
		return merchants.findByApprovalStatusOrderByNameAsc(status).stream()
			.map(AdminMerchantResponse::from)
			.toList();
	}

	@Transactional
	public AdminMerchantResponse approve(AuthenticatedUser admin, Long merchantId) {
		Merchant merchant = findAny(merchantId);
		merchant.approve(users.getReferenceById(admin.id()), clock.instant());
		return AdminMerchantResponse.from(merchant);
	}

	@Transactional
	public AdminMerchantResponse reject(AuthenticatedUser admin, Long merchantId) {
		Merchant merchant = findAny(merchantId);
		merchant.reject(users.getReferenceById(admin.id()), clock.instant());
		return AdminMerchantResponse.from(merchant);
	}

	private Merchant findAny(Long id) {
		return merchants.findById(id).orElseThrow(() -> ApiException.notFound("Comercio inexistente"));
	}

}
