package ar.urbandelivery.order;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados del pedido (lado comercio). Ver docs/03-casos-y-estados.md §2.
 */
public enum OrderStatus {

	PENDING, PREPARING, READY, COMPLETED, REJECTED, CANCELLED;

	private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
			PENDING, EnumSet.of(PREPARING, REJECTED, CANCELLED),
			PREPARING, EnumSet.of(READY, CANCELLED),
			READY, EnumSet.of(COMPLETED, CANCELLED));

	public boolean canTransitionTo(OrderStatus target) {
		return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
	}

	public boolean isTerminal() {
		return !TRANSITIONS.containsKey(this);
	}

	/** Estado que ve el cliente (docs/03 §4). Sin entrega todavía: solo depende del pedido. */
	public String clientLabel(FulfillmentType type) {
		return switch (this) {
			case PENDING -> "Esperando al comercio";
			case PREPARING -> "En preparación";
			case READY -> type == FulfillmentType.PICKUP ? "Listo para retirar" : "Listo · esperando al cadete";
			case COMPLETED -> type == FulfillmentType.PICKUP ? "Retirado" : "Entregado";
			case REJECTED -> "Rechazado";
			case CANCELLED -> "Cancelado";
		};
	}

}
