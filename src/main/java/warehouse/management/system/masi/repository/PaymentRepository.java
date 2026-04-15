package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
