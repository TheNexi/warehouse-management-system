package warehouse.management.system.masi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.management.system.masi.model.AuthSession;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    Optional<AuthSession> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);

    void deleteByToken(String token);
}
