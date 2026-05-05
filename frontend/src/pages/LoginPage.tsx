import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../services/http';

const LoginPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const user = await login({ username, password });
      navigate(user.role === 'ADMINISTRATOR' ? '/employees' : '/dashboard', { replace: true });
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setError(submitError.message);
      } else {
        setError('Nie można się zalogować. Sprawdź dostępność serwera i spróbuj ponownie.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-layout">
      <section className="panel login-panel">
        <p className="panel-kicker">Dostęp do magazynu</p>
        <h2 className="panel-title">Zaloguj się, aby kontynuować</h2>
        <p className="panel-subtitle">
          Logowanie używa sesji serwera. Uruchom backend, a następnie zaloguj się istniejącym kontem pracownika.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label className="field-label" htmlFor="username">
            Nazwa użytkownika
          </label>
          <input
            id="username"
            className="text-input"
            type="text"
            autoComplete="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />

          <label className="field-label" htmlFor="password">
            Hasło
          </label>
          <input
            id="password"
            className="text-input"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          {error && <p className="feedback feedback-error">{error}</p>}

          <button className="button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Logowanie...' : 'Zaloguj się'}
          </button>
        </form>
      </section>
    </div>
  );
};

export default LoginPage;
