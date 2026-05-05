import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { createProduct, deleteProduct, getProducts, updateProduct } from '../services/api';
import { ApiError } from '../services/http';
import type { Product, ProductRequest } from '../types/api';

interface ProductFormState {
  name: string;
  price: string;
  description: string;
  category: string;
  availability: string;
}

const EMPTY_PRODUCT_FORM: ProductFormState = {
  name: '',
  price: '',
  description: '',
  category: '',
  availability: '',
};

const normalizePrice = (value: number | string): number => {
  return typeof value === 'number' ? value : Number.parseFloat(value);
};

const currencyFormatter = new Intl.NumberFormat('pl-PL', {
  style: 'currency',
  currency: 'PLN',
});

const ProductsPage = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [form, setForm] = useState<ProductFormState>(EMPTY_PRODUCT_FORM);
  const [editingProductId, setEditingProductId] = useState<number | null>(null);
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
        setError('Nie można pobrać listy produktów.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  const sortedProducts = useMemo(() => {
    return [...products].sort((first, second) => first.id - second.id);
  }, [products]);

  const handleInputChange = (key: keyof ProductFormState, value: string) => {
    setForm((previous) => ({ ...previous, [key]: value }));
  };

  const resetForm = () => {
    setForm(EMPTY_PRODUCT_FORM);
    setEditingProductId(null);
  };

  const toRequestPayload = (): ProductRequest | null => {
    const price = Number.parseFloat(form.price);
    const availability = Number.parseInt(form.availability, 10);

    if (Number.isNaN(price) || price < 0) {
      setFeedback('Cena musi być nieujemną liczbą.');
      setFeedbackType('error');
      return null;
    }

    if (!Number.isInteger(availability) || availability < 0) {
      setFeedback('Dostępność musi być nieujemną liczbą całkowitą.');
      setFeedbackType('error');
      return null;
    }

    return {
      name: form.name.trim(),
      price,
      description: form.description.trim(),
      category: form.category.trim(),
      availability,
    };
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);

    const payload = toRequestPayload();
    if (!payload) {
      return;
    }

    setIsSubmitting(true);

    try {
      if (editingProductId === null) {
        await createProduct(payload);
        setFeedback('Produkt został pomyślnie utworzony.');
      } else {
        await updateProduct(editingProductId, payload);
        setFeedback('Produkt został pomyślnie zaktualizowany.');
      }
      setFeedbackType('success');
      resetForm();
      await loadProducts();
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setFeedback(submitError.message);
      } else {
        setFeedback('Nie można zapisać produktu.');
      }
      setFeedbackType('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (product: Product) => {
    setEditingProductId(product.id);
    setForm({
      name: product.name,
      price: String(normalizePrice(product.price)),
      description: product.description,
      category: product.category,
      availability: String(product.availability),
    });
    setFeedback(null);
  };

  const handleDelete = async (product: Product) => {
    const confirmed = window.confirm(`Usunąć produkt "${product.name}"?`);
    if (!confirmed) {
      return;
    }

    setFeedback(null);

    try {
      await deleteProduct(product.id);
      setFeedback('Produkt został pomyślnie usunięty.');
      setFeedbackType('success');
      await loadProducts();
    } catch (deleteError) {
      if (deleteError instanceof ApiError) {
        setFeedback(deleteError.message);
      } else {
        setFeedback('Nie można usunąć produktu.');
      }
      setFeedbackType('error');
    }
  };

  return (
    <section className="page">
      <header className="page-header">
        <h2 className="page-title">Zarządzanie produktami</h2>
        <p className="page-subtitle">Twórz, edytuj i usuwaj produkty z katalogu magazynowego.</p>
      </header>

      <div className="panels-two-column">
        <article className="panel">
          <h3 className="panel-title">
            {editingProductId === null ? 'Dodaj produkt' : `Edytuj produkt #${editingProductId}`}
          </h3>

          <form className="stacked-form" onSubmit={handleSubmit}>
            <label className="field-label" htmlFor="product-name">
              Nazwa
            </label>
            <input
              id="product-name"
              className="text-input"
              value={form.name}
              onChange={(event) => handleInputChange('name', event.target.value)}
              required
            />

            <label className="field-label" htmlFor="product-price">
              Cena (PLN)
            </label>
            <input
              id="product-price"
              className="text-input"
              type="number"
              min={0}
              step="0.01"
              value={form.price}
              onChange={(event) => handleInputChange('price', event.target.value)}
              required
            />

            <label className="field-label" htmlFor="product-category">
              Kategoria
            </label>
            <input
              id="product-category"
              className="text-input"
              value={form.category}
              onChange={(event) => handleInputChange('category', event.target.value)}
              required
            />

            <label className="field-label" htmlFor="product-availability">
              Dostępność
            </label>
            <input
              id="product-availability"
              className="text-input"
              type="number"
              min={0}
              step={1}
              value={form.availability}
              onChange={(event) => handleInputChange('availability', event.target.value)}
              required
            />

            <label className="field-label" htmlFor="product-description">
              Opis
            </label>
            <textarea
              id="product-description"
              className="text-area"
              rows={4}
              value={form.description}
              onChange={(event) => handleInputChange('description', event.target.value)}
              required
            />

            <div className="button-group">
              <button className="button" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Zapisywanie...' : editingProductId === null ? 'Utwórz produkt' : 'Zapisz zmiany'}
              </button>
              {editingProductId !== null && (
                <button className="button button-secondary" type="button" onClick={resetForm}>
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
          <h3 className="panel-title">Aktualne produkty</h3>

          {isLoading && <p className="inline-note">Ładowanie produktów...</p>}
          {error && <p className="feedback feedback-error">{error}</p>}

          {!isLoading && !error && (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nazwa</th>
                    <th>Kategoria</th>
                    <th>Cena</th>
                    <th>Dostępność</th>
                    <th>Akcje</th>
                  </tr>
                </thead>
                <tbody>
                  {sortedProducts.map((product) => (
                    <tr key={product.id}>
                      <td>{product.id}</td>
                      <td>{product.name}</td>
                      <td>{product.category}</td>
                      <td>{currencyFormatter.format(normalizePrice(product.price))}</td>
                      <td>{product.availability}</td>
                      <td>
                        <div className="inline-actions">
                          <button className="button button-secondary" type="button" onClick={() => handleEdit(product)}>
                            Edytuj
                          </button>
                          <button className="button button-danger" type="button" onClick={() => handleDelete(product)}>
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

export default ProductsPage;
