-- Categories
INSERT INTO categories (name, description) VALUES
('Hand Tools', 'Manual tools for various tasks'),
('Power Tools', 'Tools operated by electrical or battery power'),
('Building Materials', 'Materials used for construction'),
('Plumbing', 'Pipes, fittings, and plumbing accessories');

-- Suppliers
INSERT INTO suppliers (name, phone, email, address) VALUES
('Siam Hardware Supply', '02-111-2222', 'contact@siamhardware.com', '123 Bangkok Thailand'),
('Makita Thailand', '02-333-4444', 'sales@makita.co.th', '456 Samut Prakan Thailand');

-- Products
INSERT INTO products (sku, name, description, unit, price, cost_price, minimum_stock, category_id, supplier_id) VALUES
('HT-001', 'Claw Hammer 16oz', 'Heavy duty steel hammer', 'Piece', 250.00, 150.00, 10, 1, 1),
('PT-001', 'Cordless Drill 18V', 'Makita 18V drill with battery', 'Set', 3500.00, 2800.00, 5, 2, 2),
('BM-001', 'Portland Cement 50kg', 'Standard portland cement for construction', 'Bag', 150.00, 120.00, 50, 3, 1);

-- Inventory Stocks
INSERT INTO inventory_stocks (product_id, quantity, reserved_quantity) VALUES
(1, 50, 0),
(2, 15, 0),
(3, 200, 0);

-- Customers
INSERT INTO customers (name, phone, email, address, is_member, created_at) VALUES
('General Walk-in', '000-000-0000', NULL, NULL, FALSE, CURRENT_TIMESTAMP),
('Somchai Contractor', '081-999-8888', 'somchai@build.com', '789 Nonthaburi Thailand', TRUE, CURRENT_TIMESTAMP);

-- Users
INSERT INTO users (username, password, name, role, created_at) VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGz.O6j0v1w7w0yHkK1m', 'Administrator', 'OWNER', CURRENT_TIMESTAMP);
