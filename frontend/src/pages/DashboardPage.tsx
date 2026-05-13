import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { getStock, updateStock } from '../services/api';
import { ApiError } from '../services/http';
import type { Warehouse } from '../types/api';

const DashboardPage = () => {
  const [warehouse, setWarehouse] = useState<Warehouse | null>(null);
  const [absoluteStock, setAbsoluteStock] = useState('');
  const [changeBy, setChangeBy] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingAbsolute, setIsSavingAbsolute] = useState(false);
  const [isSavingRelative, setIsSavingRelative] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackType, setFeedbackType] = useState<'success' | 'error'>('success');

  const loadStock = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getStock();
      setWarehouse(data);
    } catch (loadError) {
      if (loadError instanceof ApiError) {
        setError(loadError.message);
      } else {
        setError('Nie można pobrać stanu magazynu.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadStock();
  }, []);

  const capacityUsage = useMemo(() => {
    if (!warehouse || warehouse.capacity <= 0) {
      return 0;
    }

    const percentage = Math.round((warehouse.currentStockLevel / warehouse.capacity) * 100);
    return Math.max(0, Math.min(100, percentage));
  }, [warehouse]);

  const handleAbsoluteUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const parsed = Number.parseInt(absoluteStock, 10);
    if (!Number.isInteger(parsed) || parsed < 0) {
      setFeedback('Wartość musi być nieujemną liczbą całkowitą.');
      setFeedbackType('error');
      return;
    }

    setIsSavingAbsolute(true);
    setFeedback(null);

    try {
      const updated = await updateStock({ newStockLevel: parsed });
      setWarehouse(updated);
      setAbsoluteStock('');
      setFeedback('Poziom zapasów magazynu został zaktualizowany.');
      setFeedbackType('success');
    } catch (saveError) {
      if (saveError instanceof ApiError) {
        setFeedback(saveError.message);
      } else {
        setFeedback('Nie można zaktualizować poziomu zapasów.');
      }
      setFeedbackType('error');
    } finally {
      setIsSavingAbsolute(false);
    }
  };

  const handleRelativeUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const parsed = Number.parseInt(changeBy, 10);
    if (!Number.isInteger(parsed) || parsed === 0) {
      setFeedback('Wartość zmiany musi być niezerową liczbą całkowitą.');
      setFeedbackType('error');
      return;
    }

    setIsSavingRelative(true);
    setFeedback(null);

    try {
      const updated = await updateStock({ changeBy: parsed });
      setWarehouse(updated);
      setChangeBy('');
      setFeedback('Korekta zapasów magazynu została zapisana.');
      setFeedbackType('success');
    } catch (saveError) {
      if (saveError instanceof ApiError) {
        setFeedback(saveError.message);
      } else {
        setFeedback('Nie można zastosować zmiany zapasów.');
      }
      setFeedbackType('error');
    } finally {
      setIsSavingRelative(false);
    }
  };

  if (isLoading) {
    return (
      <section className="page">
        <div className="panel">Ładowanie danych magazynu...</div>
      </section>
    );
  }

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Panel główny / Stan magazynu</h2>
        <p className="page-subtitle">Monitoruj aktualny stan magazynu i wprowadzaj aktualizacje poziomów zapasów.</p>
      </header>

      {error && <p className="feedback feedback-error">{error}</p>}

      {warehouse && (
        <>
          <div className="metrics-grid">
            <article className="panel metric-card">
              <p className="metric-label">Adres magazynu</p>
              <p className="metric-value" style={{ fontSize: '16px' }}>{warehouse.address}</p>
            </article>

            <article className="panel metric-card">
              <p className="metric-label">Aktualny stan</p>
              <p className="metric-value">{warehouse.currentStockLevel}</p>
            </article>

            <article className="panel metric-card">
              <p className="metric-label">Pojemność</p>
              <p className="metric-value">{warehouse.capacity}</p>
            </article>

            <article className="panel metric-card">
              <p className="metric-label">Wykorzystanie pojemności</p>
              <p className="metric-value">{capacityUsage}%</p>
              <div className="meter">
                <div className="meter-fill" style={{ width: `${capacityUsage}%` }} />
              </div>
            </article>
          </div>

          <div className="panels-two-column">
            <article className="panel">
              <h3 className="panel-title">Ustaw bezwzględny poziom zapasów</h3>
              <p className="panel-subtitle">Użyj tego trybu, gdy inwentaryzacja jest już przeprowadzona.</p>

              <form className="stacked-form" onSubmit={handleAbsoluteUpdate}>
                <label className="field-label" htmlFor="absolute-stock">
                  Nowy poziom zapasów
                </label>
                <input
                  id="absolute-stock"
                  className="text-input"
                  type="number"
                  min={0}
                  max={warehouse.capacity} 
                  step={1}
                  value={absoluteStock}
                  onChange={(event) => setAbsoluteStock(event.target.value.replace(/\D/g, ''))}
                  required
                />
                <button className="button" type="submit" disabled={isSavingAbsolute}>
                  {isSavingAbsolute ? 'Zapisywanie...' : 'Ustaw poziom'}
                </button>
              </form>
            </article>

            <article className="panel">
              <h3 className="panel-title">Koryguj zapasy o różnicę</h3>
              <p className="panel-subtitle">Wartości dodatnie zwiększają zapasy, ujemne zmniejszają.</p>

              <form className="stacked-form" onSubmit={handleRelativeUpdate}>
                <label className="field-label" htmlFor="change-by">
                  Zmiana o wartość
                </label>
                <input
                  id="change-by"
                  className="text-input"
                  type="number"
                  step={1}
                  min={-warehouse.currentStockLevel} 
                  max={warehouse.capacity - warehouse.currentStockLevel} 
                  value={changeBy}
                  onChange={(event) => setChangeBy(event.target.value.replace(/[^-0-9]/g, ''))}
                  required
                />
                <button className="button" type="submit" disabled={isSavingRelative}>
                  {isSavingRelative ? 'Stosowanie...' : 'Zastosuj zmianę'}
                </button>
              </form>
            </article>
          </div>
        </>
      )}

      {feedback && (
        <p className={`feedback feedback-${feedbackType}`}>{feedback}</p>
      )}
    </section>
  );
};

export default DashboardPage;
