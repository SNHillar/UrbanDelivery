package ar.urbandelivery.auth;

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
			.andExpect(jsonPath("$[?(@.name == 'Verdulería Don José')]").isEmpty());
	}

}
