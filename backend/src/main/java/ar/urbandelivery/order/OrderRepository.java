package ar.urbandelivery.order;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@EntityGraph(attributePaths = { "merchant", "client", "items" })
	Optional<Order> findDetailedById(Long id);

	@EntityGraph(attributePaths = { "merchant", "client", "items" })
	List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);

	@EntityGraph(attributePaths = { "merchant", "client", "items" })
	List<Order> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

	/** Bandeja del comercio: los más viejos primero. */
	@EntityGraph(attributePaths = { "merchant", "client", "items" })
	List<Order> findByMerchantIdAndStatusInOrderByCreatedAtAsc(Long merchantId, Collection<OrderStatus> statuses);

}
