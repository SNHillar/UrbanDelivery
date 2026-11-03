package ar.urbandelivery.auth;

import ar.urbandelivery.user.Role;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quién hace la petición, tomado del JWT ya validado por Spring Security.
 */
public record AuthenticatedUser(Long id, Role role) {

	public static AuthenticatedUser from(Jwt jwt) {
		return new AuthenticatedUser(Long.valueOf(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString(JwtService.ROLE_CLAIM)));
	}

	public boolean is(Role expected) {
		return role == expected;
	}

}
