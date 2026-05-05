import type {
  ApiMessageResponse,
  Delivery,
  Employee,
  EmployeeRequest,
  LoginRequest,
  LoginResponse,
  OrderHistory,
  Payment,
  PaymentRequest,
  Product,
  ProductRequest,
  StockUpdateRequest,
  Warehouse,
} from '../types/api';
import { apiRequest } from './http';

export const login = (request: LoginRequest): Promise<LoginResponse> => {
  return apiRequest<LoginResponse>('/login', {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const logout = (): Promise<ApiMessageResponse> => {
  return apiRequest<ApiMessageResponse>('/logout', {
    method: 'POST',
  });
};

export const isAuthorized = (): Promise<ApiMessageResponse> => {
  return apiRequest<ApiMessageResponse>('/is-authorized');
};

export const getStock = (): Promise<Warehouse> => {
  return apiRequest<Warehouse>('/stock');
};

export const updateStock = (request: StockUpdateRequest): Promise<Warehouse> => {
  return apiRequest<Warehouse>('/stock/update', {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const getProducts = (): Promise<Product[]> => {
  return apiRequest<Product[]>('/products');
};

export const createProduct = (request: ProductRequest): Promise<Product> => {
  return apiRequest<Product>('/products', {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const updateProduct = (id: number, request: ProductRequest): Promise<Product> => {
  return apiRequest<Product>(`/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
};

export const deleteProduct = (id: number): Promise<ApiMessageResponse> => {
  return apiRequest<ApiMessageResponse>(`/products/${id}`, {
    method: 'DELETE',
  });
};

export const getDeliveries = (): Promise<Delivery[]> => {
  return apiRequest<Delivery[]>('/deliveries');
};

export const acceptDelivery = (id: number): Promise<Delivery> => {
  return apiRequest<Delivery>(`/deliveries/${id}/accept`, {
    method: 'POST',
  });
};

export const getEmployees = (): Promise<Employee[]> => {
  return apiRequest<Employee[]>('/employees');
};

export const createEmployee = (request: EmployeeRequest): Promise<Employee> => {
  return apiRequest<Employee>('/employees', {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const registerEmployee = (request: EmployeeRequest): Promise<Employee> => {
  return apiRequest<Employee>('/register', {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const updateEmployee = (id: number, request: EmployeeRequest): Promise<Employee> => {
  return apiRequest<Employee>(`/employees/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
};

export const deleteEmployee = (id: number): Promise<ApiMessageResponse> => {
  return apiRequest<ApiMessageResponse>(`/employees/${id}`, {
    method: 'DELETE',
  });
};

export const payEmployee = (employeeId: number, request: PaymentRequest): Promise<Payment> => {
  return apiRequest<Payment>(`/employees/${employeeId}/pay`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const getOrderHistory = (): Promise<OrderHistory[]> => {
  return apiRequest<OrderHistory[]>('/orders/history');
};
