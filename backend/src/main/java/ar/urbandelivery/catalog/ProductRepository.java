package ar.urbandelivery.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByMerchantIdAndActiveTrueOrderByNameAsc(Long merchantId);

}
