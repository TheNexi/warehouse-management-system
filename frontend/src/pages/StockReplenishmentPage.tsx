import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { getProducts, getStock, updateProduct, updateStock } from '../services/api';
import { ApiError } from '../services/http';
import type { Product } from '../types/api';

const normalizePrice = (value: number | string): number => {
  return typeof value === 'number' ? value : Number.parseFloat(value);
};

const StockReplenishmentPage = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackType, setFeedbackType] = useState<'success' | 'error'>('success');
  const [error, setError] = useState<string | null>(null);

  const loadProducts = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getProducts();
      setProducts(data);
    } catch (loadError) {
      if (loadError instanceof ApiError) {
        setError(loadError.message);
      } else {
        setError('Nie można załadować produktów do uzupełnienia.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  const selectedProduct = useMemo(() => {
    const id = Number.parseInt(selectedProductId, 10);

    if (!Number.isInteger(id)) {
      return null;
    }

    return products.find((product) => product.id === id) ?? null;
  }, [products, selectedProductId]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);

    const parsedQuantity = Number.parseInt(quantity, 10);
    if (!Number.isInteger(parsedQuantity) || parsedQuantity <= 0) {
      setFeedback('Ilość musi być dodatnią liczbą całkowitą.');
      setFeedbackType('error');
      return;
    }

    if (!selectedProduct) {
      setFeedback('Wybierz produkt przed uzupełnieniem.');
      setFeedbackType('error');
      return;
    }

    setIsSubmitting(true);

    try {
      const warehouse = await getStock();
      const projectedStock = warehouse.currentStockLevel + parsedQuantity;

      if (projectedStock > warehouse.capacity) {
        setFeedback(`Pojemność magazynu przekroczona: maks. ${warehouse.capacity}, próbowano ${projectedStock}.`);
        setFeedbackType('error');
        return;
      }

      await updateProduct(selectedProduct.id, {
        name: selectedProduct.name,
        price: normalizePrice(selectedProduct.price),
        description: selectedProduct.description,
        category: selectedProduct.category,
        availability: selectedProduct.availability + parsedQuantity,
      });

      await updateStock({ changeBy: parsedQuantity });

      setFeedback(`Zapasy uzupełnione o ${parsedQuantity} szt. dla produktu ${selectedProduct.name}.`);
      setFeedbackType('success');
      setQuantity('');
      await loadProducts();
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setFeedback(submitError.message);
      } else {
        setFeedback('Nie można przetworzyć uzupełnienia zapasów.');
      }
      setFeedbackType('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Uzupełnianie zapasów</h2>
        <p className="page-subtitle">
          Dodaj zapasy do istniejącego produktu i zsynchronizuj poziom zapasów w magazynie.
        </p>
      </header>

      <article className="panel">
        {isLoading && <p className="inline-note">Ładowanie produktów...</p>}
        {error && <p className="feedback feedback-error">{error}</p>}

        {!isLoading && !error && (
          <form className="stacked-form" onSubmit={handleSubmit}>
            <label className="field-label" htmlFor="replenishment-product">
              Produkt
            </label>
            <select
              id="replenishment-product"
              className="select-input"
              value={selectedProductId}
              onChange={(event) => setSelectedProductId(event.target.value)}
              required
            >
              <option value="">Wybierz produkt</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  #{product.id} {product.name}
                </option>
              ))}
            </select>

            {selectedProduct && (
              <p className="inline-note">Aktualna dostępność: {selectedProduct.availability} szt.</p>
            )}

            <label className="field-label" htmlFor="replenishment-quantity">
              Ilość do dodania
            </label>
            <input
              id="replenishment-quantity"
              className="text-input"
              type="number"
              min={1}
              step={1}
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              required
            />

            <button className="button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Stosowanie...' : 'Zastosuj uzupełnienie'}
            </button>
          </form>
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

export default StockReplenishmentPage;
