package ar.urbandelivery.order;

import static ar.urbandelivery.order.OrderStatus.CANCELLED;
import static ar.urbandelivery.order.OrderStatus.COMPLETED;
import static ar.urbandelivery.order.OrderStatus.PENDING;
import static ar.urbandelivery.order.OrderStatus.PREPARING;
import static ar.urbandelivery.order.OrderStatus.READY;
import static ar.urbandelivery.order.OrderStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

	/** Copia literal de docs/03-casos-y-estados.md §2. */
	private static final Map<OrderStatus, Set<OrderStatus>> EXPECTED = Map.of(
			PENDING, EnumSet.of(PREPARING, REJECTED, CANCELLED),
			PREPARING, EnumSet.of(READY, CANCELLED),
			READY, EnumSet.of(COMPLETED, CANCELLED),
			COMPLETED, EnumSet.noneOf(OrderStatus.class),
			REJECTED, EnumSet.noneOf(OrderStatus.class),
			CANCELLED, EnumSet.noneOf(OrderStatus.class));

	@Test
	void everyTransitionMatchesTheDocumentedStateMachine() {
		for (OrderStatus from : OrderStatus.values()) {
			for (OrderStatus to : OrderStatus.values()) {
				assertThat(from.canTransitionTo(to))
					.as("%s -> %s", from, to)
					.isEqualTo(EXPECTED.get(from).contains(to));
			}
		}
	}

	@Test
	void terminalStates() {
		assertThat(EnumSet.allOf(OrderStatus.class).stream().filter(OrderStatus::isTerminal))
			.containsExactlyInAnyOrder(COMPLETED, REJECTED, CANCELLED);
	}

	@Test
	void clientLabelForPickup() {
		assertThat(READY.clientLabel(FulfillmentType.PICKUP)).isEqualTo("Listo para retirar");
		assertThat(COMPLETED.clientLabel(FulfillmentType.PICKUP)).isEqualTo("Retirado");
	}

}
