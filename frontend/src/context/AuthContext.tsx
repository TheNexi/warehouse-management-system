import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { getEmployees, isAuthorized, login as loginApi, logout as logoutApi } from '../services/api';
import { ApiError } from '../services/http';
import type { EmployeeRole, LoginRequest, LoginResponse } from '../types/api';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

interface AuthContextValue {
  user: LoginResponse | null;
  status: AuthStatus;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
}

const AUTH_STORAGE_KEY = 'wms.auth.user';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const isLoginResponse = (value: unknown): value is LoginResponse => {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const user = value as LoginResponse;
  return (
    typeof user.employeeId === 'number' &&
    typeof user.username === 'string' &&
    (user.role === 'ADMINISTRATOR' || user.role === 'WAREHOUSEMAN')
  );
};

const readStoredUser = (): LoginResponse | null => {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);

  if (!raw) {
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(raw);
    return isLoginResponse(parsed) ? parsed : null;
  } catch {
    return null;
  }
};

const inferRoleFromBackend = async (): Promise<EmployeeRole> => {
  try {
    await getEmployees();
    return 'ADMINISTRATOR';
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      return 'WAREHOUSEMAN';
    }

    throw error;
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<LoginResponse | null>(() => readStoredUser());
  const [status, setStatus] = useState<AuthStatus>('loading');

  const persistUser = useCallback((nextUser: LoginResponse | null) => {
    setUser(nextUser);

    if (nextUser) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextUser));
      return;
    }

    localStorage.removeItem(AUTH_STORAGE_KEY);
  }, []);

  const refreshSession = useCallback(async () => {
    setStatus('loading');

    try {
      await isAuthorized();

      if (!user) {
        const inferredRole = await inferRoleFromBackend();
        persistUser({
          employeeId: 0,
          username: 'session-user',
          role: inferredRole,
        });
      }

      setStatus('authenticated');
    } catch {
      persistUser(null);
      setStatus('unauthenticated');
    }
  }, [persistUser, user]);

  useEffect(() => {
    void refreshSession();
  }, [refreshSession]);

  const login = useCallback(
    async (request: LoginRequest): Promise<LoginResponse> => {
      const nextUser = await loginApi(request);
      persistUser(nextUser);
      setStatus('authenticated');
      return nextUser;
    },
    [persistUser],
  );

  const logout = useCallback(async (): Promise<void> => {
    try {
      await logoutApi();
    } finally {
      persistUser(null);
      setStatus('unauthenticated');
    }
  }, [persistUser]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      status,
      isAuthenticated: status === 'authenticated',
      login,
      logout,
      refreshSession,
    }),
    [login, logout, refreshSession, status, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
};
