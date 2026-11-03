package ar.urbandelivery.catalog;

import java.util.List;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.catalog.CatalogDtos.AvailabilityRequest;
import ar.urbandelivery.catalog.CatalogDtos.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

	private final CatalogService catalogService;

	public CatalogController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@GetMapping("/merchants/{merchantId}/products")
	public List<ProductResponse> publicCatalog(@PathVariable Long merchantId) {
		return catalogService.listForMerchant(merchantId);
	}

	@GetMapping("/merchant/products")
	@PreAuthorize("hasRole('MERCHANT')")
	public List<ProductResponse> ownCatalog(@AuthenticationPrincipal Jwt jwt) {
		return catalogService.listOwn(AuthenticatedUser.from(jwt));
	}

	@PatchMapping("/merchant/products/{productId}/availability")
	@PreAuthorize("hasRole('MERCHANT')")
	public ProductResponse changeAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId,
			@Valid @RequestBody AvailabilityRequest request) {
		return catalogService.changeAvailability(AuthenticatedUser.from(jwt), productId, request.available());
	}

}
