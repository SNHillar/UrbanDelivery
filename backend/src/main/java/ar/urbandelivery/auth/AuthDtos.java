package ar.urbandelivery.auth;

import java.time.Instant;

import ar.urbandelivery.user.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record LoginResponse(String token, Instant expiresAt, UserInfo user) {
	}

	/**
	 * Alta propia. {@code role} solo admite CLIENT o MERCHANT (se valida en el servicio);
	 * {@code merchant} es obligatorio únicamente cuando {@code role = MERCHANT}.
	 */
	public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password,
			@NotBlank String fullName, String phone, @NotNull Role role, @Valid MerchantRegistration merchant) {
	}

	/** Entrega final V1 solo PICKUP (docs/06): sin envío, sin costo de envío. */
	public record MerchantRegistration(@NotBlank String name, String category, @NotBlank String address,
			String phone, String openingHours) {
	}

	/** {@code merchantId} solo viene para usuarios MERCHANT que ya tienen comercio. */
	public record UserInfo(Long id, String email, String fullName, Role role, Long merchantId) {
	}

}
