import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/AppShell';
import { AdminRoute, GuestOnlyRoute, ProtectedRoute } from './components/RouteGuards';
import { AuthProvider } from './context/AuthContext';
import DashboardPage from './pages/DashboardPage';
import DeliveriesPage from './pages/DeliveriesPage';
import EmployeesPage from './pages/EmployeesPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import PayrollPage from './pages/PayrollPage';
import ProductsPage from './pages/ProductsPage';
import RegisterEmployeePage from './pages/RegisterEmployeePage';
import StockReplenishmentPage from './pages/StockReplenishmentPage';

const App = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<GuestOnlyRoute />}>
            <Route path="/login" element={<LoginPage />} />
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route element={<AppShell />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/products" element={<ProductsPage />} />
              <Route path="/stock-replenishment" element={<StockReplenishmentPage />} />
              <Route path="/deliveries" element={<DeliveriesPage />} />

              <Route element={<AdminRoute />}>
                <Route path="/employees/register" element={<RegisterEmployeePage />} />
                <Route path="/employees" element={<EmployeesPage />} />
                <Route path="/payroll" element={<PayrollPage />} />
                <Route path="/orders-history" element={<OrderHistoryPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
