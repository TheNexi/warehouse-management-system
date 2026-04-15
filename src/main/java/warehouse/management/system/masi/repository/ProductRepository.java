package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
