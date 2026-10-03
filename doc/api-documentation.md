# API Documentation

## Category API
- GET /api/v1/categories
- GET /api/v1/categories/{id}
- POST /api/v1/categories
- PUT /api/v1/categories/{id}
- DELETE /api/v1/categories/{id}

## Supplier API
- GET /api/v1/suppliers
- GET /api/v1/suppliers/{id}
- POST /api/v1/suppliers
- PUT /api/v1/suppliers/{id}
- DELETE /api/v1/suppliers/{id}

## Product API
- GET /api/v1/products
- GET /api/v1/products/{id}
- POST /api/v1/products
- PUT /api/v1/products/{id}
- DELETE /api/v1/products/{id}

## Inventory API
- GET /api/v1/inventory/products/{productId}
- PUT /api/v1/inventory/products/{productId}
- The PUT request's `quantity` is the target on-hand quantity. The operation writes an `ADJUSTMENT` audit movement in the same transaction; its recorded quantity is the signed difference from the previous quantity. `reason` is stored as the movement note.

## Stock Movement API
- POST /api/v1/stock-movements
- `IN` and `OUT` request quantities are positive deltas. An `ADJUSTMENT` request quantity is the target on-hand quantity; the audit record stores the signed difference. `OUT` cannot consume reserved stock, and adjustments cannot reduce on-hand quantity below reserved quantity.
- Stock changes and their movement records are committed together. Insufficient stock returns HTTP 409; invalid movement requests return HTTP 400.

## Notes
- ใช้ Swagger UI สำหรับทดสอบ API
- Error response standardized ผ่าน `GlobalExceptionHandler`
