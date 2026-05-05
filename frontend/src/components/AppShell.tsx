import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface NavigationItem {
  to: string;
  label: string;
}

const baseLinks: NavigationItem[] = [
  { to: '/dashboard', label: 'Panel główny' },
  { to: '/products', label: 'Produkty' },
  { to: '/stock-replenishment', label: 'Uzupełnianie zapasów' },
  { to: '/deliveries', label: 'Dostawy' },
];

const adminLinks: NavigationItem[] = [
  { to: '/employees/register', label: 'Rejestracja pracownika' },
  { to: '/employees', label: 'Pracownicy' },
  { to: '/payroll', label: 'Wynagrodzenia' },
  { to: '/orders-history', label: 'Historia operacji' },
];

const roleLabel: Record<string, string> = {
  ADMINISTRATOR: 'Administrator',
  WAREHOUSEMAN: 'Magazynier',
};

const AppShell = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const links = user?.role === 'ADMINISTRATOR' ? [...baseLinks, ...adminLinks] : baseLinks;

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-block">
          <p className="brand-kicker">System Zarządzania Magazynem</p>
          <h1 className="brand-title">Konsola Operacyjna</h1>
        </div>

        <div className="topbar-right">
          <div className="session-box">
            <p className="session-user">{user?.username}</p>
            <p className="session-role">{user ? (roleLabel[user.role] ?? user.role) : ''}</p>
          </div>
          <button className="button button-secondary" type="button" onClick={handleLogout}>
            Wyloguj
          </button>
        </div>
      </header>

      <nav className="app-nav" aria-label="Główna">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) => (isActive ? 'nav-link nav-link-active' : 'nav-link')}
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AppShell;
