package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.model.OrderHistory;
import warehouse.management.system.masi.repository.OrderHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final AuthContextService authContextService;

    public ResponseEntity<?> getHistory(HttpServletRequest request) {
        authContextService.requireAdministrator(request);
        return ResponseEntity.ok(orderHistoryRepository.findAllByOrderByCreatedAtDesc());
    }

    public void record(String operationType, String details, String performedBy) {
        OrderHistory history = OrderHistory.builder()
                .operationType(operationType)
                .details(details)
                .createdAt(LocalDateTime.now())
                .performedBy(performedBy)
                .build();

        orderHistoryRepository.save(history);
    }
}
