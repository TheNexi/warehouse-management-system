package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    boolean existsByUsername(String username);
}
