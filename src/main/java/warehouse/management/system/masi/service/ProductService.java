package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Product;
import warehouse.management.system.masi.repository.ProductRepository;
import warehouse.management.system.masi.request.ProductRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AuthContextService authContextService;
    private final OrderHistoryService orderHistoryService;

    public ResponseEntity<?> getProducts(HttpServletRequest request) {
        authContextService.requireAuthenticated(request);
        return ResponseEntity.ok(productRepository.findAll());
    }

    public ResponseEntity<?> createProduct(ProductRequest request, HttpServletRequest httpRequest) {
        Employee employee = authContextService.requireAuthenticated(httpRequest);
        validateProductRequest(request);
        Product product = buildProduct(request);
        productRepository.save(product);
        orderHistoryService.record("PRODUCT_CREATE", "Created product " + product.getName(), employee.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    public ResponseEntity<?> updateProduct(Long id, ProductRequest request, HttpServletRequest httpRequest) {
        Employee employee = authContextService.requireAuthenticated(httpRequest);
        validateProductRequest(request);
        Product product = getProductById(id);
        applyProductData(product, request);
        productRepository.save(product);
        orderHistoryService.record("PRODUCT_UPDATE", "Updated product " + product.getName(), employee.getUsername());
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<?> deleteProduct(Long id, HttpServletRequest request) {
        Employee employee = authContextService.requireAuthenticated(request);
        Product product = getProductById(id);
        productRepository.delete(product);
        orderHistoryService.record("PRODUCT_DELETE", "Deleted product " + product.getName(), employee.getUsername());
        return ResponseEntity.ok(new ApiMessageResponse("Product deleted"));
    }

    private Product buildProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .availability(request.getAvailability())
                .build();
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void applyProductData(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setAvailability(request.getAvailability());
    }

    private void validateProductRequest(ProductRequest request) {
        if (request == null || request.getPrice() == null || request.getAvailability() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product name, price and availability are required");
        }

        if (isBlank(request.getName()) || isBlank(request.getDescription()) || isBlank(request.getCategory())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product name, description and category are required");
        }

        if (request.getPrice().signum() < 0 || request.getAvailability() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Price and availability cannot be negative");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
