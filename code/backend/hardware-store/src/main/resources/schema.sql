-- Create schema for Hardware Store Inventory Management System

-- Categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Suppliers
CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(150),
    address VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Products
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    unit VARCHAR(50),
    price DECIMAL(19,2) NOT NULL,
    cost_price DECIMAL(19,2) NOT NULL,
    minimum_stock INT NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_name ON products(name);

-- Inventory Stocks
CREATE TABLE inventory_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Stock Movements
CREATE TABLE stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reference_no VARCHAR(100),
    note VARCHAR(500),
    movement_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'APPROVED',
    approved_by VARCHAR(50),
    CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Customers
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150),
    address VARCHAR(500),
    is_member BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_customer_phone ON customers(phone);
CREATE INDEX idx_customer_email ON customers(email);

-- Purchase Orders
CREATE TABLE purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);
CREATE INDEX idx_purchase_order_number ON purchase_orders(purchase_number);

-- Purchase Items
CREATE TABLE purchase_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    received_quantity INT NOT NULL DEFAULT 0,
    unit_cost DECIMAL(19,2) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_pi_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Sales Orders
CREATE TABLE sales_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    shipping_address VARCHAR(500),
    payment_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
CREATE INDEX idx_sales_order_number ON sales_orders(order_number);

-- Sales Order Items
CREATE TABLE sales_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_soi_so FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products(id)
);


-- Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(150),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
