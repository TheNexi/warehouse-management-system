package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.request.PaymentRequest;
import warehouse.management.system.masi.request.ProductRequest;
import warehouse.management.system.masi.request.StockUpdateRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;
import warehouse.management.system.masi.response.LoginResponse;
import warehouse.management.system.masi.service.AuthService;
import warehouse.management.system.masi.service.DeliveryService;
import warehouse.management.system.masi.service.EmployeeService;
import warehouse.management.system.masi.service.OrderHistoryService;
import warehouse.management.system.masi.service.ProductService;
import warehouse.management.system.masi.service.WarehouseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControllerLayerTest {

    private MockMvc mockMvc;

        @Mock
    private AuthService authService;

        @Mock
    private DeliveryService deliveryService;

        @Mock
    private EmployeeService employeeService;

        @Mock
    private OrderHistoryService orderHistoryService;

        @Mock
    private ProductService productService;

        @Mock
    private WarehouseService warehouseService;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(
                                new AuthController(authService),
                                new DeliveryController(deliveryService),
                                new EmployeeController(employeeService),
                                new OrderController(orderHistoryService),
                                new ProductController(productService),
                                new WarehouseController(warehouseService)
                ).build();
        }

    @Test
    @DisplayName("Should execute login endpoint")
    void shouldExecuteLoginEndpoint() throws Exception {
        doReturn(ResponseEntity.ok(new LoginResponse(1L, "admin", EmployeeRole.ADMINISTRATOR)))
                .when(authService).login(any(LoginRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));

        verify(authService).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("Should execute logout endpoint")
    void shouldExecuteLogoutEndpoint() throws Exception {
        doReturn(ResponseEntity.ok(new ApiMessageResponse("Logged out")))
                .when(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("Should execute authorization endpoint")
    void shouldExecuteAuthorizationEndpoint() throws Exception {
        doReturn(ResponseEntity.ok(new ApiMessageResponse("Authorized")))
                .when(authService).isAuthorized(any(HttpServletRequest.class));

        mockMvc.perform(get("/is-authorized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authorized"));

        verify(authService).isAuthorized(any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should execute deliveries endpoints")
    void shouldExecuteDeliveriesEndpoints() throws Exception {
        doReturn(ResponseEntity.ok(List.of()))
                .when(deliveryService).getDeliveries(any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(new ApiMessageResponse("accepted")))
                .when(deliveryService).acceptDelivery(eq(1L), any(HttpServletRequest.class));

        mockMvc.perform(get("/deliveries"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/deliveries/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("accepted"));

        verify(deliveryService).getDeliveries(any(HttpServletRequest.class));
        verify(deliveryService).acceptDelivery(eq(1L), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should execute employees endpoints")
    void shouldExecuteEmployeesEndpoints() throws Exception {
        Employee employee = Employee.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .position("Warehouseman")
                .role(EmployeeRole.WAREHOUSEMAN)
                .username("john")
                .passwordHash("x")
                .build();

        doReturn(ResponseEntity.ok(List.of(employee)))
                .when(employeeService).getEmployees(any(HttpServletRequest.class));
        doReturn(ResponseEntity.status(201).body(employee))
                .when(employeeService).createEmployee(any(EmployeeRequest.class), any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(employee))
                .when(employeeService).updateEmployee(eq(2L), any(EmployeeRequest.class), any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(new ApiMessageResponse("Employee deleted")))
                .when(employeeService).deleteEmployee(eq(2L), any(HttpServletRequest.class));
        doReturn(ResponseEntity.status(201).body(new ApiMessageResponse("paid")))
                .when(employeeService).payEmployee(eq(2L), any(PaymentRequest.class), any(HttpServletRequest.class));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));

        mockMvc.perform(post("/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"position\":\"Warehouseman\",\"role\":\"WAREHOUSEMAN\",\"username\":\"john\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"));

        mockMvc.perform(put("/employees/2")
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"position\":\"Warehouseman\",\"role\":\"WAREHOUSEMAN\",\"username\":\"john\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        mockMvc.perform(delete("/employees/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee deleted"));

        mockMvc.perform(post("/employees/2/pay")
                        .contentType(APPLICATION_JSON)
                        .content("{\"amount\":1000.00,\"bonusAmount\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("paid"));
    }

    @Test
    @DisplayName("Should execute products endpoints")
    void shouldExecuteProductsEndpoints() throws Exception {
        doReturn(ResponseEntity.ok(List.of()))
                .when(productService).getProducts(any(HttpServletRequest.class));
        doReturn(ResponseEntity.status(201).body(new ApiMessageResponse("created")))
                .when(productService).createProduct(any(ProductRequest.class), any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(new ApiMessageResponse("updated")))
                .when(productService).updateProduct(eq(1L), any(ProductRequest.class), any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(new ApiMessageResponse("deleted")))
                .when(productService).deleteProduct(eq(1L), any(HttpServletRequest.class));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Tape\",\"price\":7.50,\"description\":\"desc\",\"category\":\"Packaging\",\"availability\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("created"));

        mockMvc.perform(put("/products/1")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Tape\",\"price\":7.50,\"description\":\"desc\",\"category\":\"Packaging\",\"availability\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("updated"));

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("deleted"));
    }

    @Test
    @DisplayName("Should execute stock endpoints")
    void shouldExecuteStockEndpoints() throws Exception {
        doReturn(ResponseEntity.ok(new ApiMessageResponse("ok")))
                .when(warehouseService).getStock(any(HttpServletRequest.class));
        doReturn(ResponseEntity.ok(new ApiMessageResponse("updated")))
                .when(warehouseService).updateStock(any(StockUpdateRequest.class), any(HttpServletRequest.class));

        mockMvc.perform(get("/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ok"));

        mockMvc.perform(post("/stock/update")
                        .contentType(APPLICATION_JSON)
                        .content("{\"changeBy\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("updated"));
    }

    @Test
    @DisplayName("Should execute order history endpoint")
    void shouldExecuteOrderHistoryEndpoint() throws Exception {
        doReturn(ResponseEntity.ok(List.of()))
                .when(orderHistoryService).getHistory(any(HttpServletRequest.class));

        mockMvc.perform(get("/orders/history"))
                .andExpect(status().isOk());

        verify(orderHistoryService).getHistory(any(HttpServletRequest.class));
    }
}
