package ar.urbandelivery.auth;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.urbandelivery.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Spike de seguridad: login → JWT → endpoint protegido → validación por rol. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthIT {

	@Autowired
	MockMvc mvc;

	@Test
	void loginReturnsJwtThatIdentifiesTheUser() throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"ROTISERIA@urbandelivery.local\", \"password\": \"demo1234\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.role").value("MERCHANT"))
			.andExpect(jsonPath("$.user.merchantId").isNumber())
			.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(body, "$.token");

		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("rotiseria@urbandelivery.local"));
		// Rol equivocado para el endpoint
		mvc.perform(get("/api/orders/mine").header("Authorization", "Bearer " + token))
			.andExpect(status().isForbidden());
	}

	@Test
	void wrongPasswordAndMissingOrForgedTokenAreRejected() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"cliente@urbandelivery.local\", \"password\": \"otra\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
		mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.e30.firma-falsa"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void publicCatalogNeedsNoToken() throws Exception {
		mvc.perform(get("/api/merchants"))
			.andExpect(status().isOk())
			// Comercio pendiente de aprobación no aparece
			.andExpect(jsonPath("$[?(@.name == 'Verdulería Don José')]").isEmpty())
			// V1 solo retiro: ningún comercio publica envío ni tarifa (migración V2)
			.andExpect(jsonPath("$[*].offersDelivery", everyItem(is(false))))
			.andExpect(jsonPath("$[*].offersPickup", everyItem(is(true))))
			.andExpect(jsonPath("$[*].deliveryFee", everyItem(is(0.0))));
	}

	@Test
	void clientRegistersAndIsOperativeRightAway() throws Exception {
		String email = "cliente-it-" + System.nanoTime() + "@urbandelivery.local";
		String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "password": "demo1234", "fullName": "Cliente de prueba", "role": "CLIENT"}
						""".formatted(email)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.role").value("CLIENT"))
			.andExpect(jsonPath("$.user.merchantId").doesNotExist())
			.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(body, "$.token");

		mvc.perform(get("/api/orders/mine").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
	}

	@Test
	void merchantRegistersPendingAndOnlyAppearsAfterApproval() throws Exception {
		String merchantEmail = "comercio-it-" + System.nanoTime() + "@urbandelivery.local";
		String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "password": "demo1234", "fullName": "Dueña de prueba", "role": "MERCHANT",
						 "merchant": {"name": "Almacén de prueba", "address": "Calle Falsa 123"}}
						""".formatted(merchantEmail)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.role").value("MERCHANT"))
			.andExpect(jsonPath("$.user.merchantId").isNumber())
			.andReturn().getResponse().getContentAsString();
		long merchantId = ((Number) JsonPath.read(body, "$.user.merchantId")).longValue();

		// Pendiente: no aparece en el catálogo público todavía
		mvc.perform(get("/api/merchants"))
			.andExpect(jsonPath("$[?(@.id == " + merchantId + ")]").isEmpty());

		String admin = token("admin@urbandelivery.local");
		mvc.perform(post("/api/admin/merchants/" + merchantId + "/approve").header("Authorization", "Bearer " + admin))
			.andExpect(status().isOk());

		mvc.perform(get("/api/merchants"))
			.andExpect(jsonPath("$[?(@.id == " + merchantId + ")]").exists());
	}

	@Test
	void registerRejectsDuplicateEmailAndNonPublicRoles() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "cliente@urbandelivery.local", "password": "demo1234", "fullName": "X", "role": "CLIENT"}
						"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));

		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "nuevo-admin@urbandelivery.local", "password": "demo1234", "fullName": "X", "role": "ADMIN"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_ROLE"));
	}

	private String token(String email) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"" + email + "\", \"password\": \"demo1234\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

}
