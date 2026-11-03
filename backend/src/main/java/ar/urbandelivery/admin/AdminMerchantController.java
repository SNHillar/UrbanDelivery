package ar.urbandelivery.admin;

import java.util.List;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.merchant.AdminMerchantResponse;
import ar.urbandelivery.merchant.ApprovalStatus;
import ar.urbandelivery.merchant.MerchantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Moderación de comercios (módulo "admin" de docs/05-modulos.md). El ADMIN aprueba o rechaza
 * las altas que quedaron en PENDING; sin esto el comercio no aparece en el listado público.
 */
@RestController
@RequestMapping("/api/admin/merchants")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

	private final MerchantService merchantService;

	public AdminMerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@GetMapping
	public List<AdminMerchantResponse> list(
			@RequestParam(name = "status", required = false, defaultValue = "PENDING") ApprovalStatus status) {
		return merchantService.listByStatus(status);
	}

	@PostMapping("/{id}/approve")
	public AdminMerchantResponse approve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return merchantService.approve(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping("/{id}/reject")
	public AdminMerchantResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return merchantService.reject(AuthenticatedUser.from(jwt), id);
	}

}
