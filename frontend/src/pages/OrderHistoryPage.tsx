import { useEffect, useState, useMemo } from 'react';
import { getOrderHistory } from '../services/api';
import { ApiError } from '../services/http';
import type { OrderHistory } from '../types/api';

const formatTimestamp = (value: string): string => {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString('pl-PL');
};

const inferCategory = (operationType: string): string => {
  const op = operationType.toUpperCase();
  if (op.includes('EMPLOYEE') || op.includes('PAYMENT') || op.includes('REGISTER')) return 'Pracownicy i Wynagrodzenia';
  if (op.includes('PRODUCT')) return 'Zarządzanie Produktami';
  if (op.includes('DELIVERY') || op.includes('STOCK')) return 'Magazyn i Dostawy';
  return 'Inne operacje';
};

const OrderHistoryPage = () => {
  const [records, setRecords] = useState<OrderHistory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRecords = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getOrderHistory();
      setRecords(data);
    } catch (loadError) {
      if (loadError instanceof ApiError) {
        setError(loadError.message);
      } else {
        setError('Nie można załadować historii operacji.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadRecords();
  }, []);

  const groupedRecords = useMemo(() => {
    const groups: Record<string, OrderHistory[]> = {};
    for (const record of records) {
      const category = inferCategory(record.operationType);
      if (!groups[category]) {
        groups[category] = [];
      }
      groups[category].push(record);
    }
    return groups;
  }, [records]);

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Historia operacji</h2>
        <p className="page-subtitle">Dziennik audytowy generowany przez usługi backendowe.</p>
      </header>

      <article className="panel">
        {isLoading && <p className="inline-note">Ładowanie historii...</p>}
        {error && <p className="feedback feedback-error">{error}</p>}

        {!isLoading && !error && Object.keys(groupedRecords).length === 0 && (
          <p className="inline-note">Brak historii operacji.</p>
        )}

        {!isLoading && !error && Object.entries(groupedRecords).map(([category, categoryRecords]) => (
          <div key={category} style={{ marginBottom: '32px' }}>
            <h3 className="panel-title" style={{ marginBottom: '16px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
              {category}
            </h3>
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Data i czas</th>
                    <th>Operacja</th>
                    <th>Szczegóły</th>
                    <th>Wykonane przez</th>
                  </tr>
                </thead>
                <tbody>
                  {categoryRecords.map((record) => (
                    <tr key={record.id}>
                      <td>{record.id}</td>
                      <td>{formatTimestamp(record.createdAt)}</td>
                      <td>{record.operationType}</td>
                      <td>{record.details}</td>
                      <td>{record.performedBy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ))}
      </article>
    </section>
  );
};

export default OrderHistoryPage;
