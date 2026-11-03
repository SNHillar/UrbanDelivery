package ar.urbandelivery.history;

import java.time.Instant;

import ar.urbandelivery.user.Role;

public record StatusChangeResponse(HistoryEntity entity, String fromStatus, String toStatus, String actorName,
		Role actorRole, String reason, Instant at) {

	static StatusChangeResponse from(OrderStatusHistory h) {
		return new StatusChangeResponse(h.getEntity(), h.getFromStatus(), h.getToStatus(),
				h.getActor() == null ? "Sistema" : h.getActor().getFullName(),
				h.getActor() == null ? null : h.getActor().getRole(), h.getReason(), h.getCreatedAt());
	}

}
