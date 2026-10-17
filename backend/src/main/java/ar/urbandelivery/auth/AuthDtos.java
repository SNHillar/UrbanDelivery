package ar.urbandelivery.auth;

import java.time.Instant;

import ar.urbandelivery.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record LoginResponse(String token, Instant expiresAt, UserInfo user) {
	}

	/** {@code merchantId} solo viene para usuarios MERCHANT que ya tienen comercio. */
	public record UserInfo(Long id, String email, String fullName, Role role, Long merchantId) {
	}

}
