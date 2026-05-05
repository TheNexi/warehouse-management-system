package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import warehouse.management.system.masi.model.OrderHistory;
import warehouse.management.system.masi.repository.OrderHistoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = OrderHistoryService.class)
@ActiveProfiles("test")
class OrderHistoryServiceTest {

    @Autowired
    private OrderHistoryService orderHistoryService;

    @MockitoBean
    private OrderHistoryRepository orderHistoryRepository;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should return history for administrator")
    void shouldReturnHistoryForAdministrator() {
        when(orderHistoryRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        ResponseEntity<?> response = orderHistoryService.getHistory(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authContextService).requireAdministrator(request);
        verify(orderHistoryRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should store order history record")
    void shouldStoreOrderHistoryRecord() {
        ArgumentCaptor<OrderHistory> captor = ArgumentCaptor.forClass(OrderHistory.class);

        orderHistoryService.record("PRODUCT_CREATE", "Created product Tape", "admin");

        verify(orderHistoryRepository).save(captor.capture());
        OrderHistory saved = captor.getValue();

        assertEquals("PRODUCT_CREATE", saved.getOperationType());
        assertEquals("Created product Tape", saved.getDetails());
        assertEquals("admin", saved.getPerformedBy());
        assertNotNull(saved.getCreatedAt());
    }
}
