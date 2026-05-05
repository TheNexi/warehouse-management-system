INSERT INTO employees (id, first_name, last_name, position, role, username, password_hash) VALUES
(1, 'System', 'Administrator', 'Administrator systemu', 'ADMINISTRATOR', 'admin', '$argon2id$v=19$m=16384,t=2,p=1$jY7/D4yYsC6W667tu3908A$mTM1YDMnhHfW9BP8gYPJv5MkX7IFFzk62q1wp06s7jE'),
(2, 'Marek', 'Nowak', 'Magazynier', 'WAREHOUSEMAN', 'magazynier', '$argon2id$v=19$m=16384,t=2,p=1$jY7/D4yYsC6W667tu3908A$mTM1YDMnhHfW9BP8gYPJv5MkX7IFFzk62q1wp06s7jE'),
(3, 'Jan', 'Kowalski', 'Starszy magazynier', 'WAREHOUSEMAN', 'jan', '$argon2id$v=19$m=16384,t=2,p=1$jY7/D4yYsC6W667tu3908A$mTM1YDMnhHfW9BP8gYPJv5MkX7IFFzk62q1wp06s7jE')
ON CONFLICT (username) DO UPDATE
SET
	first_name = EXCLUDED.first_name,
	last_name = EXCLUDED.last_name,
	position = EXCLUDED.position,
	role = EXCLUDED.role,
	password_hash = EXCLUDED.password_hash;

INSERT INTO warehouses (id, address, capacity, current_stock_level) VALUES
(1, 'Magazyn glowny, Kielce', 12000, 620)
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, price, description, category, availability) VALUES
(1, 'Rekawice przemyslowe', 19.99, 'Ochronne rekawice nitrylowe do pracy w magazynie', 'Bezpieczenstwo', 300),
(2, 'Tasma pakowa', 7.50, 'Przezroczysta tasma pakowa 48 mm', 'Pakowanie', 220),
(3, 'Akumulator do wozka widlowego', 499.00, 'Akumulator 24V do elektrycznych wozkow widlowych', 'Wyposazenie', 15)
ON CONFLICT (id) DO NOTHING;

INSERT INTO deliveries (id, delivery_date, status, delivery_address, courier_company, product_id, quantity) VALUES
(1, CURRENT_DATE, 'PENDING', 'Magazyn glowny, Kielce', 'DHL', 1, 120),
(2, CURRENT_DATE - 1, 'ACCEPTED', 'Magazyn glowny, Kielce', 'InPost', 2, 80),
(3, CURRENT_DATE - 2, 'REJECTED', 'Magazyn glowny, Kielce', 'Poczta Polska', 3, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, employee_id, amount, bonus_amount, payment_date) VALUES
(1, 2, 4200.00, 300.00, CURRENT_DATE),
(2, 3, 4000.00, 250.00, CURRENT_DATE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_history (id, operation_type, details, created_at, performed_by) VALUES
(1, 'STOCK_UPDATE', 'Zaladowano poczatkowy stan magazynu z pliku SQL', NOW(), 'system'),
(2, 'PRODUCT_CREATE', 'Utworzono poczatkowy katalog produktow z pliku SQL', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('employees', 'id'), COALESCE((SELECT MAX(id) FROM employees), 1));
SELECT setval(pg_get_serial_sequence('warehouses', 'id'), COALESCE((SELECT MAX(id) FROM warehouses), 1));
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1));
SELECT setval(pg_get_serial_sequence('deliveries', 'id'), COALESCE((SELECT MAX(id) FROM deliveries), 1));
SELECT setval(pg_get_serial_sequence('payments', 'id'), COALESCE((SELECT MAX(id) FROM payments), 1));
SELECT setval(pg_get_serial_sequence('order_history', 'id'), COALESCE((SELECT MAX(id) FROM order_history), 1));
