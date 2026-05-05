package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.OrderHistory;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findAllByOrderByCreatedAtDesc();
}
