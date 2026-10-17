package ar.urbandelivery.merchant;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@GetMapping
	public List<MerchantResponse> list() {
		return merchantService.listApproved();
	}

	@GetMapping("/{id}")
	public MerchantResponse get(@PathVariable Long id) {
		return merchantService.getApproved(id);
	}

}
