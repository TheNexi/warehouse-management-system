package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.request.ProductRequest;
import warehouse.management.system.masi.service.ProductService;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(HttpServletRequest request) {
        return productService.getProducts(request);
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest productRequest,
                                           HttpServletRequest request) {
        return productService.createProduct(productRequest, request);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @RequestBody ProductRequest productRequest,
                                           HttpServletRequest request) {
        return productService.updateProduct(id, productRequest, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id,
                                           HttpServletRequest request) {
        return productService.deleteProduct(id, request);
    }
}
