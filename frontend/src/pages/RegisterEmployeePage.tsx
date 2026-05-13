import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerEmployee } from '../services/api';
import { ApiError } from '../services/http';
import type { EmployeeRole } from '../types/api';

interface RegisterFormState {
  firstName: string;
  lastName: string;
  position: string;
  role: EmployeeRole;
  username: string;
  password: string;
}

const EMPTY_FORM: RegisterFormState = {
  firstName: '',
  lastName: '',
  position: '',
  role: 'WAREHOUSEMAN',
  username: '',
  password: '',
};

const RegisterEmployeePage = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState<RegisterFormState>(EMPTY_FORM);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackType, setFeedbackType] = useState<'success' | 'error'>('success');

  const updateField = (field: keyof RegisterFormState, value: string) => {
    setForm((previous) => ({ ...previous, [field]: value }));
  };

  const resetForm = () => {
    setForm(EMPTY_FORM);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);
    setIsSubmitting(true);

    try {
      await registerEmployee({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        position: form.position.trim(),
        role: form.role,
        username: form.username.trim(),
        password: form.password,
      });

      setFeedback('Pracownik został pomyślnie zarejestrowany.');
      setFeedbackType('success');
      resetForm();
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setFeedback(submitError.message);
      } else {
        setFeedback('Nie można zarejestrować pracownika.');
      }
      setFeedbackType('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Rejestracja nowego pracownika</h2>
        <p className="page-subtitle">Formularz rejestracji dostępny tylko dla administratorów.</p>
      </header>

      <article className="panel">
        <form className="stacked-form" onSubmit={handleSubmit}>
          <label className="field-label" htmlFor="register-first-name">
            Imię
          </label>
          <input
            id="register-first-name"
            className="text-input"
            value={form.firstName}
            onChange={(event) => updateField('firstName', event.target.value)}
            pattern="^[A-Za-zżźćńółęąśŻŹĆŃÓŁĘĄŚ\s\-]+$"
            minLength={2}
            maxLength={50}
            title="Imię musi zawierać przynajmniej 2 litery i nie może zawierać cyfr."
            required
          />

          <label className="field-label" htmlFor="register-last-name">
            Nazwisko
          </label>
          <input
            id="register-last-name"
            className="text-input"
            value={form.lastName}
            onChange={(event) => updateField('lastName', event.target.value)}
            pattern="^[A-Za-zżźćńółęąśŻŹĆŃÓŁĘĄŚ\s\-]+$"
            minLength={2}
            maxLength={50}
            title="Nazwisko musi zawierać przynajmniej 2 litery i nie może zawierać cyfr."
            required
          />

          <label className="field-label" htmlFor="register-position">
            Stanowisko
          </label>
          <input
            id="register-position"
            className="text-input"
            value={form.position}
            onChange={(event) => updateField('position', event.target.value)}
            minLength={2}
            maxLength={50}
            title="Stanowisko musi mieć od 2 do 50 znaków."
            required
          />

          <label className="field-label" htmlFor="register-role">
            Rola
          </label>
          <select
            id="register-role"
            className="select-input"
            value={form.role}
            onChange={(event) => updateField('role', event.target.value)}
            required
          >
            <option value="WAREHOUSEMAN">Magazynier</option>
            <option value="ADMINISTRATOR">Administrator</option>
          </select>

          <label className="field-label" htmlFor="register-username">
            Nazwa użytkownika
          </label>
          <input
            id="register-username"
            className="text-input"
            value={form.username}
            onChange={(event) => updateField('username', event.target.value)}
            pattern="^[A-Za-z0-9_]+$"
            minLength={3}
            maxLength={30}
            title="Nazwa użytkownika od 3 do 30 znaków (litery, cyfry, podkreślenie)."
            required
          />

          <label className="field-label" htmlFor="register-password">
            Hasło
          </label>
          <input
            id="register-password"
            className="text-input"
            type="password"
            value={form.password}
            onChange={(event) => updateField('password', event.target.value)}
            minLength={4}
            maxLength={50}
            title="Hasło musi mieć co najmniej 4 znaki."
            required
          />

          <div className="button-group">
            <button className="button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Rejestrowanie...' : 'Zarejestruj pracownika'}
            </button>
            <button className="button button-secondary" type="button" onClick={() => navigate('/employees')}>
              Przejdź do pracowników
            </button>
          </div>
        </form>

        {feedback && (
          <p className={`feedback feedback-${feedbackType}`} style={{ marginTop: '12px' }}>
            {feedback}
          </p>
        )}
      </article>
    </section>
  );
};

export default RegisterEmployeePage;
