import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NotFoundPage = () => {
  const { isAuthenticated } = useAuth();

  return (
    <section className="page page-not-found">
      <article className="panel not-found-card">
        <p className="panel-kicker">404</p>
        <h2 className="panel-title">Strona nie znaleziona</h2>
        <p className="panel-subtitle">Żądana ścieżka nie istnieje w tej aplikacji.</p>
        <Link className="button" to={isAuthenticated ? '/dashboard' : '/login'}>
          {isAuthenticated ? 'Przejdź do panelu' : 'Przejdź do logowania'}
        </Link>
      </article>
    </section>
  );
};

export default NotFoundPage;
