import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { getEmployees, payEmployee } from '../services/api';
import { ApiError } from '../services/http';
import type { Employee, Payment } from '../types/api';

const formatter = new Intl.NumberFormat('pl-PL', {
  style: 'currency',
  currency: 'PLN',
});

const STORAGE_KEY = 'recentPayments';

const toDisplayCurrency = (value: number | string): string => {
  const normalized = typeof value === 'number' ? value : Number.parseFloat(value);

  if (Number.isNaN(normalized)) {
    return String(value);
  }

  return formatter.format(normalized);
};

const PayrollPage = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('');
  const [amount, setAmount] = useState('');
  const [bonusAmount, setBonusAmount] = useState('0');

  const [recentPayments, setRecentPayments] = useState<Payment[]>(() => {
    try {
      return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '[]');
    } catch {
      return [];
    }
  });

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
        setError('Nie można załadować listy pracowników do wynagrodzeń.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadEmployees();
  }, []);

  useEffect(() => {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(recentPayments));
  }, [recentPayments]);

  const selectedEmployee = useMemo(() => {
    const parsedId = Number.parseInt(selectedEmployeeId, 10);
    if (!Number.isInteger(parsedId)) {
      return null;
    }

    return employees.find((employee) => employee.id === parsedId) ?? null;
  }, [employees, selectedEmployeeId]);

  const amountNumber = Number.parseFloat(amount || '0');
  const bonusNumber = Number.parseFloat(bonusAmount || '0');

  const totalPayment =
    Number.isNaN(amountNumber) || Number.isNaN(bonusNumber)
      ? 0
      : amountNumber + bonusNumber;

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);

    const parsedEmployeeId = Number.parseInt(selectedEmployeeId, 10);
    const parsedAmount = Number.parseFloat(amount);
    const parsedBonus = Number.parseFloat(bonusAmount);

    if (!Number.isInteger(parsedEmployeeId)) {
      setFeedback('Wybierz pracownika, aby kontynuować.');
      setFeedbackType('error');
      return;
    }

    if (
      Number.isNaN(parsedAmount) ||
      parsedAmount < 0 ||
      parsedAmount > 1000000 ||
      Number.isNaN(parsedBonus) ||
      parsedBonus < 0 ||
      parsedBonus > 1000000
    ) {
      setFeedback('Kwota i premia muszą być nieujemnymi liczbami i nie przekraczać 1 000 000.');
      setFeedbackType('error');
      return;
    }

    setIsSubmitting(true);

    try {
      const payment = await payEmployee(parsedEmployeeId, {
        amount: parsedAmount,
        bonusAmount: parsedBonus,
      });

      setRecentPayments((previous) => [payment, ...previous].slice(0, 10));

      setFeedback('Wpis płatności został pomyślnie utworzony.');
      setFeedbackType('success');

      setAmount('');
      setBonusAmount('0');
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setFeedback(submitError.message);
      } else {
        setFeedback('Nie można przetworzyć operacji płatności.');
      }
      setFeedbackType('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Wynagrodzenia / Panel premii</h2>
        <p className="page-subtitle">
          Wypłacaj wynagrodzenia i premie dla wybranych pracowników.
        </p>
      </header>

      <div className="panels-two-column">
        <article className="panel">
          <h3 className="panel-title">Utwórz wypłatę</h3>

          {isLoading && <p className="inline-note">Ładowanie pracowników...</p>}
          {error && <p className="feedback feedback-error">{error}</p>}

          {!isLoading && !error && (
            <form className="stacked-form" onSubmit={handleSubmit}>
              <label className="field-label" htmlFor="payment-employee">
                Pracownik
              </label>
              <select
                id="payment-employee"
                className="select-input"
                value={selectedEmployeeId}
                onChange={(event) => setSelectedEmployeeId(event.target.value)}
                required
              >
                <option value="">Wybierz pracownika</option>
                {employees.map((employee) => (
                  <option key={employee.id} value={employee.id}>
                    #{employee.id} {employee.firstName} {employee.lastName} ({employee.username})
                  </option>
                ))}
              </select>

              <label className="field-label" htmlFor="payment-amount">
                Kwota bazowa (PLN)
              </label>
              <input
                id="payment-amount"
                className="text-input"
                type="number"
                min={0}
                max={1000000}
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value.replace(/[^0-9.]/g, ''))}
                required
              />

              <label className="field-label" htmlFor="payment-bonus">
                Kwota premii (PLN)
              </label>
              <input
                id="payment-bonus"
                className="text-input"
                type="number"
                min={0}
                max={1000000}
                step="0.01"
                value={bonusAmount}
                onChange={(event) => setBonusAmount(event.target.value.replace(/[^0-9.]/g, ''))}
                required
              />

              <p className="inline-note">
                Łącznie do wypłaty: {formatter.format(totalPayment)}
              </p>

              <button className="button" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Przetwarzanie...' : 'Wypłać'}
              </button>
            </form>
          )}

          {selectedEmployee && (
            <div className="sub-panel">
              <p className="inline-note">Profil wybranego pracownika</p>
              <p>
                {selectedEmployee.firstName} {selectedEmployee.lastName} — {selectedEmployee.position}
              </p>
              <p>
                Rola:{' '}
                <strong style={{ color: 'var(--text-h)' }}>
                  {selectedEmployee.role === 'ADMINISTRATOR'
                    ? 'Administrator'
                    : 'Magazynier'}
                </strong>
              </p>
            </div>
          )}

          {feedback && (
            <p className={`feedback feedback-${feedbackType}`} style={{ marginTop: '12px' }}>
              {feedback}
            </p>
          )}
        </article>

        <article className="panel">
          <h3 className="panel-title">Ostatnie wypłaty (bieżąca sesja)</h3>

          {recentPayments.length === 0 ? (
            <p className="inline-note">Brak nowych wypłat w tej sesji.</p>
          ) : (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Pracownik</th>
                    <th>Podstawa</th>
                    <th>Premia</th>
                    <th>Łącznie</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  {recentPayments.map((payment) => {
                    const total =
                      Number.parseFloat(String(payment.amount)) +
                      Number.parseFloat(String(payment.bonusAmount));

                    return (
                      <tr key={payment.id}>
                        <td>{payment.id}</td>
                        <td>
                          {payment.employee.firstName} {payment.employee.lastName}
                        </td>
                        <td>{toDisplayCurrency(payment.amount)}</td>
                        <td>{toDisplayCurrency(payment.bonusAmount)}</td>
                        <td>{formatter.format(total)}</td>
                        <td>{payment.paymentDate}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </div>
    </section>
  );
};

export default PayrollPage;