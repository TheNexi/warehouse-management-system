package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Delivery;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Product;
import warehouse.management.system.masi.model.enums.DeliveryStatus;
import warehouse.management.system.masi.repository.DeliveryRepository;
import warehouse.management.system.masi.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;
    private final AuthContextService authContextService;
    private final OrderHistoryService orderHistoryService;

    public ResponseEntity<?> getDeliveries(HttpServletRequest request) {
        authContextService.requireAuthenticated(request);
        return ResponseEntity.ok(deliveryRepository.findAllByOrderByDeliveryDateDesc());
    }

    @Transactional
    public ResponseEntity<?> acceptDelivery(Long deliveryId, HttpServletRequest request) {
        Employee employee = authContextService.requireAuthenticated(request);
        Delivery delivery = getDeliveryById(deliveryId);
        validatePendingDelivery(delivery);
        applyDeliveryToProduct(delivery);
        delivery.setStatus(DeliveryStatus.ACCEPTED);
        deliveryRepository.save(delivery);
        warehouseService.increaseStock(delivery.getQuantity(), employee.getUsername());
        orderHistoryService.record("DELIVERY_ACCEPTED", "Accepted delivery " + deliveryId, employee.getUsername());
        return ResponseEntity.ok(delivery);
    }

    private Delivery getDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));
    }

    private void validatePendingDelivery(Delivery delivery) {
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending deliveries can be accepted");
        }

        if (delivery.getQuantity() == null || delivery.getQuantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Delivery quantity must be positive");
        }
    }

    private void applyDeliveryToProduct(Delivery delivery) {
        Product product = delivery.getProduct();
        int newAvailability = product.getAvailability() + delivery.getQuantity();
        product.setAvailability(newAvailability);
        productRepository.save(product);
    }
}
