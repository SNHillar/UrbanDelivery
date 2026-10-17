package ar.urbandelivery.history;

import java.time.Clock;
import java.util.List;

import ar.urbandelivery.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusHistoryService {

	private final OrderStatusHistoryRepository history;
	private final UserRepository users;
	private final Clock clock;

	public StatusHistoryService(OrderStatusHistoryRepository history, UserRepository users, Clock clock) {
		this.history = history;
		this.users = users;
		this.clock = clock;
	}

	/**
	 * Invariante #11: cada transición queda registrada con su actor.
	 * MANDATORY obliga a llamarlo dentro de la transacción que cambia el estado:
	 * si el cambio se revierte, el registro también.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void record(Long orderId, HistoryEntity entity, Enum<?> from, Enum<?> to, Long actorUserId, String reason) {
		history.save(new OrderStatusHistory(orderId, entity, from == null ? null : from.name(), to.name(),
				actorUserId == null ? null : users.getReferenceById(actorUserId), reason, clock.instant()));
	}

	@Transactional(readOnly = true)
	public List<StatusChangeResponse> forOrder(Long orderId) {
		return history.findByOrderIdOrderByCreatedAtAscIdAsc(orderId).stream()
			.map(StatusChangeResponse::from)
			.toList();
	}

}
