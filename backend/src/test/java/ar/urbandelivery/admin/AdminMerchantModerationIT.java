package ar.urbandelivery.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.urbandelivery.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Módulo "admin" (docs/05-modulos.md): el ADMIN aprueba o rechaza un comercio PENDING y recién
 * ahí aparece en el listado público. Cada test siembra su propio comercio por JdbcTemplate para
 * no tocar la "Verdulería Don José" de db/demo, de la que depende AuthIT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminMerchantModerationIT {

	private static final String PASSWORD = "demo1234";

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	PasswordEncoder passwordEncoder;

	String admin;
	String client;
	long pendingMerchantId;

	@BeforeEach
	void setUp() throws Exception {
		admin = token("admin@urbandelivery.local");
		client = token("cliente@urbandelivery.local");
		pendingMerchantId = seedPendingMerchant();
	}

	@Test
	void adminApprovesAPendingMerchantAndItBecomesPublic() throws Exception {
		mvc.perform(get("/api/admin/merchants").header("Authorization", "Bearer " + admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == " + pendingMerchantId + ")]").exists());
		// Todavía no es visible para clientes
		mvc.perform(get("/api/merchants"))
			.andExpect(jsonPath("$[?(@.id == " + pendingMerchantId + ")]").isEmpty());

		mvc.perform(post("/api/admin/merchants/" + pendingMerchantId + "/approve").header("Authorization", "Bearer " + admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

		mvc.perform(get("/api/merchants"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == " + pendingMerchantId + ")]").exists());
		mvc.perform(get("/api/admin/merchants").header("Authorization", "Bearer " + admin))
			.andExpect(jsonPath("$[?(@.id == " + pendingMerchantId + ")]").isEmpty());
	}

	@Test
	void adminRejectsAPendingMerchantAndItStaysHidden() throws Exception {
		mvc.perform(post("/api/admin/merchants/" + pendingMerchantId + "/reject").header("Authorization", "Bearer " + admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.approvalStatus").value("REJECTED"));

		mvc.perform(get("/api/merchants"))
			.andExpect(jsonPath("$[?(@.id == " + pendingMerchantId + ")]").isEmpty());
	}

	@Test
	void onlyAdminCanModerate() throws Exception {
		mvc.perform(get("/api/admin/merchants").header("Authorization", "Bearer " + client))
			.andExpect(status().isForbidden());
		mvc.perform(post("/api/admin/merchants/" + pendingMerchantId + "/approve").header("Authorization", "Bearer " + client))
			.andExpect(status().isForbidden());
	}

	@Test
	void approvingAnAlreadyDecidedMerchantConflicts() throws Exception {
		mvc.perform(post("/api/admin/merchants/" + pendingMerchantId + "/approve").header("Authorization", "Bearer " + admin))
			.andExpect(status().isOk());

		mvc.perform(post("/api/admin/merchants/" + pendingMerchantId + "/approve").header("Authorization", "Bearer " + admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

	private long seedPendingMerchant() {
		String email = "pendiente-it-" + System.nanoTime() + "@urbandelivery.local";
		jdbc.update("INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, 'MERCHANT')", email,
				passwordEncoder.encode(PASSWORD), "Dueño de prueba");
		Long ownerId = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
		jdbc.update("INSERT INTO merchants (owner_user_id, name, address, offers_pickup, delivery_fee, approval_status) "
				+ "VALUES (?, ?, ?, TRUE, 0, 'PENDING')", ownerId, "Comercio de prueba " + ownerId, "Calle Falsa 123");
		return jdbc.queryForObject("SELECT id FROM merchants WHERE owner_user_id = ?", Long.class, ownerId);
	}

	private String token(String email) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"" + email + "\", \"password\": \"" + PASSWORD + "\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

}
