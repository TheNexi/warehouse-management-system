package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Product;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.ProductRepository;
import warehouse.management.system.masi.request.ProductRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ProductService.class)
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private OrderHistoryService orderHistoryService;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should return products for authenticated user")
    void shouldReturnProductsForAuthenticatedUser() {
        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(productRepository.findAll()).thenReturn(List.of(createProduct()));

        ResponseEntity<?> response = productService.getProducts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should create product")
    void shouldCreateProduct() {
        ProductRequest productRequest = createRequest();

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());

        ResponseEntity<?> response = productService.createProduct(productRequest, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productRepository).save(org.mockito.ArgumentMatchers.any(Product.class));
        verify(orderHistoryService).record("PRODUCT_CREATE", "Created product Tape", "admin");
    }

    @Test
    @DisplayName("Should throw exception for invalid product request")
    void shouldThrowExceptionForInvalidProductRequest() {
        ProductRequest productRequest = createRequest();
        productRequest.setPrice(new BigDecimal("-1.00"));

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());

        ApiException exception = assertThrows(ApiException.class,
                () -> productService.createProduct(productRequest, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Price and availability cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should update product")
    void shouldUpdateProduct() {
        Product product = createProduct();
        ProductRequest productRequest = createRequest();

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ResponseEntity<?> response = productService.updateProduct(1L, productRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tape", product.getName());
        verify(productRepository).save(product);
        verify(orderHistoryService).record("PRODUCT_UPDATE", "Updated product Tape", "admin");
    }

    @Test
    @DisplayName("Should throw exception when updating unknown product")
    void shouldThrowExceptionWhenUpdatingUnknownProduct() {
        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> productService.updateProduct(99L, createRequest(), request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        Product product = createProduct();

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ResponseEntity<?> response = productService.deleteProduct(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productRepository).delete(product);
        verify(orderHistoryService).record("PRODUCT_DELETE", "Deleted product Tape", "admin");
    }

    private Employee createEmployee() {
        return Employee.builder()
                .id(1L)
                .firstName("System")
                .lastName("Administrator")
                .position("Administrator")
                .role(EmployeeRole.ADMINISTRATOR)
                .username("admin")
                .passwordHash("hash")
                .build();
    }

    private Product createProduct() {
        return Product.builder()
                .id(1L)
                .name("Tape")
                .price(new BigDecimal("7.50"))
                .description("desc")
                .category("Packaging")
                .availability(20)
                .build();
    }

    private ProductRequest createRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Tape");
        request.setPrice(new BigDecimal("7.50"));
        request.setDescription("desc");
        request.setCategory("Packaging");
        request.setAvailability(20);
        return request;
    }
}
