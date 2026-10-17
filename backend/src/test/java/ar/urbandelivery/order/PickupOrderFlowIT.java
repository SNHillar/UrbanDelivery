package ar.urbandelivery.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Caso B (retiro) y caso C (rechazo) de docs/03-casos-y-estados.md §5, punta a punta:
 * HTTP + JWT → servicio → JPA → PostgreSQL (con los datos de db/demo).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PickupOrderFlowIT {

	private static final String PASSWORD = "demo1234";

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	String client;
	String otherClient;
	String rotiseria;
	String farmacia;
	long rotiseriaId;

	@BeforeEach
	void login() throws Exception {
		client = token("cliente@urbandelivery.local");
		otherClient = token("cliente2@urbandelivery.local");
		rotiseria = token("rotiseria@urbandelivery.local");
		farmacia = token("farmacia@urbandelivery.local");
		rotiseriaId = jdbc.queryForObject("SELECT id FROM merchants WHERE name = 'Rotisería La Esquina'", Long.class);
	}

	@Test
	void caseB_pickupOrderGoesFromPendingToCompletedAndRecordsEveryTransition() throws Exception {
		long pollo = productId("Pollo al spiedo");
		long tarta = productId("Tarta de verduras");

		String created = createOrder(client, """
				{"merchantId": %d, "fulfillmentType": "PICKUP", "notes": "Sin sal",
				 "items": [{"productId": %d, "quantity": 1}, {"productId": %d, "quantity": 2}]}
				""".formatted(rotiseriaId, pollo, tarta))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.order.status").value("PENDING"))
			.andExpect(jsonPath("$.order.statusLabel").value("Esperando al comercio"))
			.andExpect(jsonPath("$.order.deliveryFee").value(0))
			.andExpect(jsonPath("$.order.subtotal").value(12500 + 2 * 7400))
			.andExpect(jsonPath("$.order.total").value(12500 + 2 * 7400))
			.andExpect(jsonPath("$.order.items.length()").value(2))
			.andReturn().getResponse().getContentAsString();
		long orderId = ((Number) JsonPath.read(created, "$.order.id")).longValue();

		action(rotiseria, orderId, "accept").andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("PREPARING"))
			.andExpect(jsonPath("$.order.acceptedAt").isNotEmpty());
		action(rotiseria, orderId, "ready").andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("READY"))
			.andExpect(jsonPath("$.order.readyAt").isNotEmpty());

		mvc.perform(get("/api/orders/{id}", orderId).header("Authorization", bearer(client)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.order.statusLabel").value("Listo para retirar"));

		action(rotiseria, orderId, "complete").andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("COMPLETED"))
			.andExpect(jsonPath("$.order.statusLabel").value("Retirado"))
			.andExpect(jsonPath("$.order.completedAt").isNotEmpty())
			.andExpect(jsonPath("$.history.length()").value(4))
			.andExpect(jsonPath("$.history[0].fromStatus").doesNotExist())
			.andExpect(jsonPath("$.history[0].toStatus").value("PENDING"))
			.andExpect(jsonPath("$.history[0].actorRole").value("CLIENT"))
			.andExpect(jsonPath("$.history[3].fromStatus").value("READY"))
			.andExpect(jsonPath("$.history[3].toStatus").value("COMPLETED"))
			.andExpect(jsonPath("$.history[3].actorRole").value("MERCHANT"));

		// Lo que quedó en la base, no solo en la respuesta
		assertThat(jdbc.queryForList(
				"SELECT to_status FROM order_status_history WHERE order_id = ? ORDER BY id", String.class, orderId))
			.containsExactly("PENDING", "PREPARING", "READY", "COMPLETED");
		assertThat(jdbc.queryForObject("SELECT count(*) FROM order_items WHERE order_id = ?", Integer.class, orderId))
			.isEqualTo(2);
	}

	@Test
	void caseC_rejectRequiresAReason() throws Exception {
		long orderId = newPickupOrder(client);

		mvc.perform(post("/api/orders/{id}/reject", orderId).header("Authorization", bearer(rotiseria))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("REASON_REQUIRED"));

		mvc.perform(post("/api/orders/{id}/reject", orderId).header("Authorization", bearer(rotiseria))
				.contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Sin stock: empanadas de carne\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("REJECTED"))
			.andExpect(jsonPath("$.order.statusReason").value("Sin stock: empanadas de carne"))
			.andExpect(jsonPath("$.history.length()").value(2));
	}

	@Test
	void clientCancelsOnlyWhilePending() throws Exception {
		long pending = newPickupOrder(client);
		action(client, pending, "cancel").andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("CANCELLED"));

		long accepted = newPickupOrder(client);
		action(rotiseria, accepted, "accept").andExpect(status().isOk());
		action(client, accepted, "cancel").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CLIENT_CANCEL_NOT_ALLOWED"));
	}

	@Test
	void merchantCancelsAcceptedOrderWithReasonButRejectsPendingOnes() throws Exception {
		long orderId = newPickupOrder(client);
		action(rotiseria, orderId, "cancel").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("USE_REJECT"));

		action(rotiseria, orderId, "accept").andExpect(status().isOk());
		mvc.perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", bearer(rotiseria))
				.contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Se cortó la luz\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.order.status").value("CANCELLED"));
	}

	@Test
	void onlyTheOwnerActsOnAnOrder() throws Exception {
		long orderId = newPickupOrder(client);

		action(farmacia, orderId, "accept").andExpect(status().isForbidden());
		action(client, orderId, "accept").andExpect(status().isForbidden());
		mvc.perform(get("/api/orders/{id}", orderId).header("Authorization", bearer(otherClient)))
			.andExpect(status().isForbidden());
		mvc.perform(get("/api/orders/{id}", orderId)).andExpect(status().isUnauthorized());
	}

	@Test
	void invalidTransitionsAreConflicts() throws Exception {
		long orderId = newPickupOrder(client);

		action(rotiseria, orderId, "ready").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
		action(rotiseria, orderId, "complete").andExpect(status().isConflict());
	}

	@Test
	void orderCreationEnforcesCatalogRules() throws Exception {
		long unavailable = productId("Matambre a la pizza");
		long fromFarmacia = productId("Alcohol en gel 250 ml");

		createOrder(client, itemsJson(rotiseriaId, unavailable)).andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("PRODUCT_UNAVAILABLE"));
		createOrder(client, itemsJson(rotiseriaId, fromFarmacia)).andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("PRODUCT_NOT_IN_MERCHANT"));
		createOrder(client, """
				{"merchantId": %d, "fulfillmentType": "DELIVERY", "items": [{"productId": %d, "quantity": 1}]}
				""".formatted(rotiseriaId, productId("Pollo al spiedo")))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("DELIVERY_NOT_AVAILABLE"));
		createOrder(rotiseria, itemsJson(rotiseriaId, productId("Pollo al spiedo"))).andExpect(status().isForbidden());
	}

	/** Doble click o dos pestañas: aunque lleguen juntas, una sola aceptación queda registrada. */
	@Test
	void concurrentAcceptsLeaveExactlyOneTransition() throws Exception {
		long orderId = newPickupOrder(client);
		CountDownLatch start = new CountDownLatch(1);
		Callable<Integer> accept = () -> {
			start.await();
			return action(rotiseria, orderId, "accept").andReturn().getResponse().getStatus();
		};
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			List<Future<Integer>> results = new ArrayList<>(List.of(pool.submit(accept), pool.submit(accept)));
			start.countDown();
			List<Integer> statuses = new ArrayList<>();
			for (Future<Integer> f : results) {
				statuses.add(f.get());
			}
			assertThat(statuses).containsExactlyInAnyOrder(200, 409);
		}
		finally {
			pool.shutdownNow();
		}
		assertThat(jdbc.queryForObject(
				"SELECT count(*) FROM order_status_history WHERE order_id = ? AND to_status = 'PREPARING'",
				Integer.class, orderId))
			.isEqualTo(1);
	}

	private long newPickupOrder(String token) throws Exception {
		String body = createOrder(token, itemsJson(rotiseriaId, productId("Tarta de verduras")))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.order.id")).longValue();
	}

	private static String itemsJson(long merchantId, long productId) {
		return """
				{"merchantId": %d, "fulfillmentType": "PICKUP", "items": [{"productId": %d, "quantity": 1}]}
				""".formatted(merchantId, productId);
	}

	private ResultActions createOrder(String token, String json) throws Exception {
		return mvc.perform(post("/api/orders").header("Authorization", bearer(token))
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private ResultActions action(String token, long orderId, String action) throws Exception {
		return mvc.perform(post("/api/orders/{id}/{action}", orderId, action).header("Authorization", bearer(token)));
	}

	private long productId(String name) {
		return jdbc.queryForObject("SELECT id FROM products WHERE name = ?", Long.class, name);
	}

	private String token(String email) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, PASSWORD)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

}
