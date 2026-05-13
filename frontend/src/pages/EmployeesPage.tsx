import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { useLocation } from 'react-router-dom';
import {
  createEmployee,
  deleteEmployee,
  getEmployees,
  updateEmployee,
} from '../services/api';
import { ApiError } from '../services/http';
import type { Employee, EmployeeRequest, EmployeeRole } from '../types/api';

interface EmployeeFormState {
  firstName: string;
  lastName: string;
  position: string;
  role: EmployeeRole;
  username: string;
  password: string;
}

const EMPTY_FORM: EmployeeFormState = {
  firstName: '',
  lastName: '',
  position: '',
  role: 'WAREHOUSEMAN',
  username: '',
  password: '',
};

const roleLabels: Record<string, string> = {
  ADMINISTRATOR: 'Administrator',
  WAREHOUSEMAN: 'Magazynier',
};

const EmployeesPage = () => {
  const location = useLocation();

  const [employees, setEmployees] = useState<Employee[]>([]);
  const [form, setForm] = useState<EmployeeFormState>(EMPTY_FORM);
  const [editingEmployeeId, setEditingEmployeeId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackType, setFeedbackType] = useState<'success' | 'error'>('success');

  const loadEmployees = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getEmployees();
      setEmployees(data);
    } catch (loadError) {
      if (loadError instanceof ApiError) {
        setError(loadError.message);
      } else {
        setError('Nie można załadować listy pracowników.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadEmployees();
  }, [location.pathname]);

  const sortedEmployees = useMemo(() => {
    return [...employees].sort((first, second) => first.id - second.id);
  }, [employees]);

  const updateField = (key: keyof EmployeeFormState, value: string) => {
    setForm((previous) => ({ ...previous, [key]: value }));
  };

  const resetForm = () => {
    setForm(EMPTY_FORM);
    setEditingEmployeeId(null);
  };

  const toPayload = (): EmployeeRequest | null => {
    const normalizedPassword = form.password.trim();

    if (editingEmployeeId === null && normalizedPassword.length === 0) {
      setFeedback('Hasło jest wymagane przy tworzeniu pracownika.');
      setFeedbackType('error');
      return null;
    }

    const payload: EmployeeRequest = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      position: form.position.trim(),
      role: form.role,
      username: form.username.trim(),
    };

    if (normalizedPassword.length > 0) {
      payload.password = normalizedPassword;
    }

    return payload;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);

    const payload = toPayload();
    if (!payload) return;

    setIsSubmitting(true);

    try {
      if (editingEmployeeId === null) {
        await createEmployee(payload);
        setFeedback('Pracownik został pomyślnie utworzony.');
      } else {
        await updateEmployee(editingEmployeeId, payload);
        setFeedback('Dane pracownika zostały pomyślnie zaktualizowane.');
      }

      setFeedbackType('success');
      resetForm();
      await loadEmployees();
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setFeedback(submitError.message);
      } else {
        setFeedback('Nie można zapisać danych pracownika.');
      }
      setFeedbackType('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (employee: Employee) => {
    setEditingEmployeeId(employee.id);
    setForm({
      firstName: employee.firstName,
      lastName: employee.lastName,
      position: employee.position,
      role: employee.role,
      username: employee.username,
      password: '',
    });
    setFeedback(null);
  };

  const handleDelete = async (employee: Employee) => {
    const confirmed = window.confirm(
      `Usunąć pracownika "${employee.firstName} ${employee.lastName}"?`
    );
    if (!confirmed) return;

    setFeedback(null);

    try {
      await deleteEmployee(employee.id);
      setFeedback('Pracownik został pomyślnie usunięty.');
      setFeedbackType('success');
      await loadEmployees();
    } catch (deleteError) {
      if (deleteError instanceof ApiError) {
        setFeedback(deleteError.message);
      } else {
        setFeedback('Nie można usunąć pracownika.');
      }
      setFeedbackType('error');
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Zarządzanie pracownikami</h2>
        <p className="page-subtitle">
          Panel administratora do dodawania, edytowania, usuwania i przeglądania profili pracowników.
        </p>
      </header>

      <div className="panels-two-column">
        <article className="panel">
          <h3 className="panel-title">
            {editingEmployeeId === null
              ? 'Dodaj pracownika'
              : `Edytuj pracownika #${editingEmployeeId}`}
          </h3>

          <form className="stacked-form" onSubmit={handleSubmit}>
            <label className="field-label" htmlFor="employee-first-name">
              Imię
            </label>
            <input
              id="employee-first-name"
              className="text-input"
              value={form.firstName}
              onChange={(event) => updateField('firstName', event.target.value)}
              pattern="^[A-Za-zżźćńółęąśŻŹĆŃÓŁĘĄŚ\s\-]+$"
              minLength={2}
              maxLength={50}
              title="Imię musi zawierać przynajmniej 2 litery i nie może zawierać cyfr."
              required
            />

            <label className="field-label" htmlFor="employee-last-name">
              Nazwisko
            </label>
            <input
              id="employee-last-name"
              className="text-input"
              value={form.lastName}
              onChange={(event) => updateField('lastName', event.target.value)}
              pattern="^[A-Za-zżźćńółęąśŻŹĆŃÓŁĘĄŚ\s\-]+$"
              minLength={2}
              maxLength={50}
              title="Nazwisko musi zawierać przynajmniej 2 litery i nie może zawierać cyfr."
              required
            />

            <label className="field-label" htmlFor="employee-position">
              Stanowisko
            </label>
            <input
              id="employee-position"
              className="text-input"
              value={form.position}
              onChange={(event) => updateField('position', event.target.value)}
              minLength={2}
              maxLength={50}
              title="Stanowisko musi mieć od 2 do 50 znaków."
              required
            />

            <label className="field-label" htmlFor="employee-role">
              Rola
            </label>
            <select
              id="employee-role"
              className="select-input"
              value={form.role}
              onChange={(event) => updateField('role', event.target.value)}
              required
            >
              <option value="WAREHOUSEMAN">Magazynier</option>
              <option value="ADMINISTRATOR">Administrator</option>
            </select>

            <label className="field-label" htmlFor="employee-username">
              Nazwa użytkownika
            </label>
            <input
              id="employee-username"
              className="text-input"
              value={form.username}
              onChange={(event) => updateField('username', event.target.value)}
              pattern="^[A-Za-z0-9_]+$"
              minLength={3}
              maxLength={30}
              title="Nazwa użytkownika od 3 do 30 znaków (litery, cyfry, podkreślenie)."
              required
            />

            <label className="field-label" htmlFor="employee-password">
              Hasło {editingEmployeeId !== null && '(opcjonalne — zostaw puste, aby zachować)'}
            </label>
            <input
              id="employee-password"
              className="text-input"
              type="password"
              value={form.password}
              onChange={(event) => updateField('password', event.target.value)}
              minLength={form.password ? 4 : undefined} 
              maxLength={50}
              title={editingEmployeeId === null ? "Hasło musi mieć co najmniej 4 znaki." : "Zostaw puste lub wpisz nowe hasło (min. 4 znaki)."}
              required={editingEmployeeId === null}
            />

            <div className="button-group">
              <button className="button" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Zapisywanie...' : editingEmployeeId === null ? 'Utwórz pracownika' : 'Zapisz zmiany'}
              </button>

              {editingEmployeeId !== null && (
                <button
                  className="button button-secondary"
                  type="button"
                  onClick={resetForm}
                >
                  Anuluj edycję
                </button>
              )}
            </div>
          </form>

          {feedback && (
            <p className={`feedback feedback-${feedbackType}`} style={{ marginTop: '12px' }}>
              {feedback}
            </p>
          )}
        </article>

        <article className="panel">
          <h3 className="panel-title">Katalog pracowników</h3>

          {isLoading && <p className="inline-note">Ładowanie pracowników...</p>}
          {error && <p className="feedback feedback-error">{error}</p>}

          {!isLoading && !error && (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Imię i nazwisko</th>
                    <th>Stanowisko</th>
                    <th>Rola</th>
                    <th>Użytkownik</th>
                    <th>Akcje</th>
                  </tr>
                </thead>

                <tbody>
                  {sortedEmployees.map((employee) => (
                    <tr key={employee.id}>
                      <td>{employee.id}</td>
                      <td>
                        {employee.firstName} {employee.lastName}
                      </td>
                      <td>{employee.position}</td>
                      <td>{roleLabels[employee.role] ?? employee.role}</td>
                      <td>{employee.username}</td>
                      <td>
                        <div className="inline-actions">
                          <button
                            className="button button-secondary"
                            type="button"
                            onClick={() => handleEdit(employee)}
                          >
                            Edytuj
                          </button>
                          <button
                            className="button button-danger"
                            type="button"
                            onClick={() => handleDelete(employee)}
                          >
                            Usuń
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>

              </table>
            </div>
          )}
        </article>
      </div>
    </section>
  );
};

export default EmployeesPage;