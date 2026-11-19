package ar.urbandelivery.auth;

import ar.urbandelivery.auth.AuthDtos.LoginRequest;
import ar.urbandelivery.auth.AuthDtos.LoginResponse;
import ar.urbandelivery.auth.AuthDtos.MerchantRegistration;
import ar.urbandelivery.auth.AuthDtos.RegisterRequest;
import ar.urbandelivery.auth.AuthDtos.UserInfo;
import ar.urbandelivery.common.ApiException;
import ar.urbandelivery.merchant.Merchant;
import ar.urbandelivery.merchant.MerchantRepository;
import ar.urbandelivery.user.Role;
import ar.urbandelivery.user.User;
import ar.urbandelivery.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository users;
	private final MerchantRepository merchants;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository users, MerchantRepository merchants, PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.users = users;
		this.merchants = merchants;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = users.findByEmailIgnoreCase(request.email().trim())
			.filter(User::isActive)
			.filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
			// Mismo mensaje para email inexistente y contraseña incorrecta: no revela qué cuentas existen
			.orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));
		JwtService.IssuedToken token = jwtService.issue(user);
		return new LoginResponse(token.value(), token.expiresAt(), toUserInfo(user));
	}

	/** Alta propia: CLIENT queda operativo al toque; MERCHANT queda PENDING hasta que el ADMIN lo apruebe. */
	@Transactional
	public LoginResponse register(RegisterRequest request) {
		if (request.role() != Role.CLIENT && request.role() != Role.MERCHANT) {
			throw ApiException.badRequest("INVALID_ROLE", "Solo podés registrarte como cliente o comercio");
		}
		String email = request.email().trim();
		if (users.findByEmailIgnoreCase(email).isPresent()) {
			throw ApiException.conflict("EMAIL_TAKEN", "Ya existe una cuenta con ese email");
		}
		if (request.role() == Role.MERCHANT && request.merchant() == null) {
			throw ApiException.badRequest("MERCHANT_DATA_REQUIRED", "Faltan los datos del comercio");
		}

		User user = User.register(email, passwordEncoder.encode(request.password()), request.fullName().trim(),
				blankToNull(request.phone()), request.role());
		users.save(user);

		if (request.role() == Role.MERCHANT) {
			MerchantRegistration m = request.merchant();
			merchants.save(Merchant.register(user, m.name().trim(), blankToNull(m.category()), m.address().trim(),
					blankToNull(m.phone()), blankToNull(m.openingHours())));
		}

		JwtService.IssuedToken token = jwtService.issue(user);
		return new LoginResponse(token.value(), token.expiresAt(), toUserInfo(user));
	}

	@Transactional(readOnly = true)
	public UserInfo me(AuthenticatedUser current) {
		User user = users.findById(current.id()).orElseThrow(() -> ApiException.notFound("Usuario inexistente"));
		return toUserInfo(user);
	}

	private UserInfo toUserInfo(User user) {
		Long merchantId = user.getRole() == Role.MERCHANT
				? merchants.findByOwnerId(user.getId()).map(Merchant::getId).orElse(null)
				: null;
		return new UserInfo(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), merchantId);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
