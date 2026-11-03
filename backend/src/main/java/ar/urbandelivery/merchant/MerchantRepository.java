package ar.urbandelivery.merchant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

	Optional<Merchant> findByOwnerId(Long ownerId);

	List<Merchant> findByApprovalStatusOrderByNameAsc(ApprovalStatus approvalStatus);

}
