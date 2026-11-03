package ar.urbandelivery.catalog;

import java.util.List;

import ar.urbandelivery.auth.AuthenticatedUser;
import ar.urbandelivery.catalog.CatalogDtos.ProductResponse;
import ar.urbandelivery.common.ApiException;
import ar.urbandelivery.merchant.Merchant;
import ar.urbandelivery.merchant.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

	private final ProductRepository products;
	private final MerchantService merchantService;

	public CatalogService(ProductRepository products, MerchantService merchantService) {
		this.products = products;
		this.merchantService = merchantService;
	}

	/** Catálogo público: productos activos, marcando cuáles no están disponibles hoy. */
	@Transactional(readOnly = true)
	public List<ProductResponse> listForMerchant(Long merchantId) {
		Merchant merchant = merchantService.findApproved(merchantId);
		return products.findByMerchantIdAndActiveTrueOrderByNameAsc(merchant.getId()).stream()
			.map(ProductResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> listOwn(AuthenticatedUser current) {
		Merchant merchant = merchantService.findOwnedBy(current.id());
		return products.findByMerchantIdAndActiveTrueOrderByNameAsc(merchant.getId()).stream()
			.map(ProductResponse::from)
			.toList();
	}

	@Transactional
	public ProductResponse changeAvailability(AuthenticatedUser current, Long productId, boolean available) {
		Merchant merchant = merchantService.findOwnedBy(current.id());
		Product product = products.findById(productId)
			.filter(Product::isActive)
			.orElseThrow(() -> ApiException.notFound("Producto inexistente"));
		if (!product.belongsTo(merchant)) {
			throw ApiException.forbidden("El producto no pertenece a tu comercio");
		}
		product.setAvailable(available);
		return ProductResponse.from(product);
	}

}
