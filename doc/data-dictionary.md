# Data Dictionary

This document describes the main persistent entities in the hardware store system. The database design follows a typical relational model with one-to-many and one-to-one relationships between master tables and transactional tables.

## categories
- `id`: primary key
- `name`: category name, unique
- `description`: optional category description

## suppliers
- `id`: primary key
- `name`: supplier name, unique
- `phone`: supplier phone number
- `email`: supplier email, unique
- `address`: supplier address

## customers
- `id`: primary key
- `name`: customer name
- `phone`: customer phone number, unique
- `email`: customer email, unique
- `address`: customer address
- `created_at`: timestamp when the customer record was created

## products
- `id`: primary key
- `sku`: product code, unique
- `name`: product name
- `description`: optional product description
- `unit`: measurement unit such as `pcs`, `box`, or `packet`
- `price`: selling price
- `cost_price`: purchase cost used for cost analysis
- `minimum_stock`: minimum quantity threshold used for stock alerts
- `category_id`: foreign key to `categories.id`
- `supplier_id`: foreign key to `suppliers.id`

## inventory_stocks
- `id`: primary key
- `product_id`: foreign key to `products.id`, unique
- `quantity`: current stock on hand
- `reserved_quantity`: quantity reserved for pending sales or processing
- `available_quantity`: derived value, calculated as `quantity - reserved_quantity`

## stock_movements
- `id`: primary key
- `product_id`: foreign key to `products.id`
- `movement_type`: movement classification (`IN`, `OUT`, `ADJUSTMENT`)
- `quantity`: movement quantity or target value in adjustment cases
- `reference_no`: document or reference number such as purchase number or adjustment code
- `note`: reason or memo for the stock change
- `movement_at`: timestamp when the stock movement was recorded

## purchase_orders
- `id`: primary key
- `purchase_number`: unique purchase document number
- `supplier_id`: foreign key to `suppliers.id`
- `status`: purchase status (`PENDING`, `COMPLETED`)
- `total_amount`: total cost of the order
- `created_at`: order creation timestamp

## purchase_items
- `id`: primary key
- `purchase_order_id`: foreign key to `purchase_orders.id`
- `product_id`: foreign key to `products.id`
- `quantity`: quantity purchased
- `unit_cost`: unit purchase cost
- `subtotal`: line total calculated as `quantity * unit_cost`

## sales_orders
- `id`: primary key
- `order_number`: unique sales order number
- `customer_id`: foreign key to `customers.id`
- `status`: sales order lifecycle state (`PENDING`, `CONFIRMED`, `SHIPPED`, `COMPLETED`, `CANCELLED`)
- `total_amount`: order total before or after discount depending on pricing flow
- `created_at`: timestamp when the sales order was created

## sales_order_items
- `id`: primary key
- `sales_order_id`: foreign key to `sales_orders.id`
- `product_id`: foreign key to `products.id`
- `quantity`: ordered quantity
- `unit_price`: selling price per unit
- `subtotal`: line total calculated as `quantity * unit_price`

## Notes
- Many numeric values use `BigDecimal` in Java to preserve currency precision.
- Derived fields such as available quantity are not always stored as a column, but are computed in the domain layer.
- Transactional tables (`purchase_orders`, `stock_movements`, `sales_orders`) provide the operational history of the system.
- Sales order behavior is implemented with a state-machine style pattern to control valid transitions.
