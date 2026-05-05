import { useEffect, useState } from 'react';
import { acceptDelivery, getDeliveries } from '../services/api';
import { ApiError } from '../services/http';
import type { Delivery } from '../types/api';

const formatDate = (value: string): string => {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString('pl-PL');
};

const statusLabels: Record<string, string> = {
  PENDING: 'OCZEKUJE',
  ACCEPTED: 'ZAAKCEPTOWANO',
  REJECTED: 'ODRZUCONO',
};

const DeliveriesPage = () => {
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [acceptingId, setAcceptingId] = useState<number | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackType, setFeedbackType] = useState<'success' | 'error'>('success');
  const [error, setError] = useState<string | null>(null);

  const loadDeliveries = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getDeliveries();
      setDeliveries(data);
    } catch (loadError) {
      if (loadError instanceof ApiError) {
        setError(loadError.message);
      } else {
        setError('Nie można pobrać listy dostaw.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadDeliveries();
  }, []);

  const handleAccept = async (deliveryId: number) => {
    setAcceptingId(deliveryId);
    setFeedback(null);

    try {
      await acceptDelivery(deliveryId);
      setFeedback(`Dostawa #${deliveryId} została zaakceptowana.`);
      setFeedbackType('success');
      await loadDeliveries();
    } catch (acceptError) {
      if (acceptError instanceof ApiError) {
        setFeedback(acceptError.message);
      } else {
        setFeedback('Nie można zaakceptować dostawy.');
      }
      setFeedbackType('error');
    } finally {
      setAcceptingId(null);
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Widok dostaw</h2>
        <p className="page-subtitle">Przeglądaj przychodzące dostawy i akceptuj oczekujące rekordy.</p>
      </header>

      <article className="panel">
        {isLoading && <p className="inline-note">Ładowanie dostaw...</p>}
        {error && <p className="feedback feedback-error">{error}</p>}

        {!isLoading && !error && (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Data</th>
                  <th>Status</th>
                  <th>Produkt</th>
                  <th>Ilość</th>
                  <th>Kurier</th>
                  <th>Adres</th>
                  <th>Akcja</th>
                </tr>
              </thead>
              <tbody>
                {deliveries.map((delivery) => (
                  <tr key={delivery.id}>
                    <td>{delivery.id}</td>
                    <td>{formatDate(delivery.deliveryDate)}</td>
                    <td>
                      <span className={`status-badge status-${delivery.status.toLowerCase()}`}>
                        {statusLabels[delivery.status] ?? delivery.status}
                      </span>
                    </td>
                    <td>{delivery.product.name}</td>
                    <td>{delivery.quantity}</td>
                    <td>{delivery.courierCompany}</td>
                    <td>{delivery.deliveryAddress}</td>
                    <td>
                      {delivery.status === 'PENDING' ? (
                        <button
                          className="button"
                          type="button"
                          disabled={acceptingId === delivery.id}
                          onClick={() => handleAccept(delivery.id)}
                        >
                          {acceptingId === delivery.id ? 'Akceptowanie...' : 'Akceptuj'}
                        </button>
                      ) : (
                        <span className="inline-note">Brak akcji</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {feedback && (
          <p className={`feedback feedback-${feedbackType}`} style={{ marginTop: '12px' }}>
            {feedback}
          </p>
        )}
      </article>
    </section>
  );
};

export default DeliveriesPage;
