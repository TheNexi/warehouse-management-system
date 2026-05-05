package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
