export type EmployeeRole = 'ADMINISTRATOR' | 'WAREHOUSEMAN';

export type DeliveryStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface ApiMessageResponse {
  message: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  employeeId: number;
  username: string;
  role: EmployeeRole;
}

export interface ErrorResponse {
  message: string;
}

export interface Warehouse {
  id: number;
  address: string;
  capacity: number;
  currentStockLevel: number;
}

export interface StockUpdateRequest {
  newStockLevel?: number;
  changeBy?: number;
}

export interface Product {
  id: number;
  name: string;
  price: number | string;
  description: string;
  category: string;
  availability: number;
}

export interface ProductRequest {
  name: string;
  price: number;
  description: string;
  category: string;
  availability: number;
}

export interface Delivery {
  id: number;
  deliveryDate: string;
  status: DeliveryStatus;
  deliveryAddress: string;
  courierCompany: string;
  product: Product;
  quantity: number;
}

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  position: string;
  role: EmployeeRole;
  username: string;
}

export interface EmployeeRequest {
  firstName: string;
  lastName: string;
  position: string;
  role: EmployeeRole;
  username: string;
  password?: string;
}

export interface PaymentRequest {
  amount: number;
  bonusAmount: number;
}

export interface Payment {
  id: number;
  employee: Employee;
  amount: number | string;
  bonusAmount: number | string;
  paymentDate: string;
}

export interface OrderHistory {
  id: number;
  operationType: string;
  details: string;
  createdAt: string;
  performedBy: string;
}
