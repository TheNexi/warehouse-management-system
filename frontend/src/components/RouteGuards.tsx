import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingView from './LoadingView';

export const GuestOnlyRoute = () => {
  const { isAuthenticated, status } = useAuth();

  if (status === 'loading') {
    return <LoadingView label="Validating session..." />;
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};

export const ProtectedRoute = () => {
  const { isAuthenticated, status } = useAuth();

  if (status === 'loading') {
    return <LoadingView label="Loading workspace..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export const AdminRoute = () => {
  const { user } = useAuth();

  if (user?.role !== 'ADMINISTRATOR') {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};
