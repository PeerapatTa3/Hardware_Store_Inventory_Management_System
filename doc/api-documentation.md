# API Documentation — Hardware Store Inventory Management System

REST API (Spring Boot, Java 17) จัดทำจาก controller และ DTO จริงใน `code/backend/hardware-store/src/main/java/com/hardwarestore/`

| รายการ | ค่า |
|---|---|
| Base URL (local) | `http://localhost:8080` |
| Prefix | `/api/v1` (ยกเว้น `/api/auth`) |
| Swagger UI | `/swagger-ui.html` |
| OpenAPI JSON | `/v3/api-docs` |
| Content-Type | `application/json` |
| Authentication | JWT Bearer (`Authorization: Bearer <token>`) |

## 1. Authentication

ทุก endpoint ต้องมี JWT ยกเว้น `/api/auth/**`, Swagger UI และ `/v3/api-docs` (`config/SecurityConfig.java:36-40`)

### POST `/api/auth/login`

Request

```json
{ "username": "admin", "password": "<password>" }
```

Response `200`

```json
{ "token": "<jwt>", "username": "admin", "role": "OWNER" }
```

Response `401` — username หรือ password ไม่ถูกต้อง

ใน Swagger UI: เรียก login → คัดลอก `token` → กด **Authorize** → วาง token

### Roles

`CASHIER`, `STOCK_MANAGER`, `OWNER` ใช้ `@PreAuthorize` ร่วมกับ `@EnableMethodSecurity` (`SecurityConfig.java:19`) ทำให้บังคับสิทธิ์จริง

| สิทธิ์ที่ต้องมี | Endpoint |
|---|---|
| `OWNER` หรือ `STOCK_MANAGER` | `POST /products`, `PUT /products/{id}`, `GET /products/admin`, `GET /products/admin/{id}` |
| `OWNER` | `DELETE /products/{id}`, `POST /stock-movements/{id}/approve`, `POST /stock-movements/{id}/reject`, `POST /purchases/{id}/approve` |
| ผู้ที่ login แล้ว | endpoint อื่นทั้งหมด |

## 2. รูปแบบ Error

ทุก error ผ่าน `GlobalExceptionHandler` (`@RestControllerAdvice`) และตอบรูปแบบเดียวกัน

```json
{
  "timestamp": "2026-10-09T10:21:23.668",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product not found with id: 99",
  "path": "/api/v1/products/99"
}
```

| HTTP | `error` | เกิดเมื่อ |
|---|---|---|
| 400 | `VALIDATION_FAILED` | body ไม่ผ่าน `@Valid` (`message` = `field: ข้อความ`) |
| 400 | `INVALID_OPERATION` | ค่าไม่ถูกต้อง เช่น สต็อกไม่พอ |
| 401 | `UNAUTHORIZED` | ยืนยันตัวตนไม่ผ่าน |
| 403 | `FORBIDDEN` | role ไม่มีสิทธิ์ |
| 404 | `RESOURCE_NOT_FOUND` | ไม่พบข้อมูล |
| 409 | `DUPLICATE_RESOURCE` | ข้อมูลที่ต้องไม่ซ้ำ (SKU, ชื่อ, อีเมล, เบอร์) ซ้ำ |
| 409 | `INVALID_PURCHASE_STATE` | ทำรายการกับใบสั่งซื้อ/รายการสต็อกที่สถานะไม่ถูกต้อง |
| 409 | `INVALID_SALES_ORDER_STATE` | ทำรายการกับใบสั่งขายที่สถานะไม่ถูกต้อง |
| 500 | `INTERNAL_SERVER_ERROR` | error อื่นๆ |

## 3. Endpoints

### 3.1 Categories — `/api/v1/categories`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/categories` | สร้าง | `CategoryRequest` | 201 |
| GET | `/api/v1/categories` | รายการทั้งหมด | — | 200 |
| GET | `/api/v1/categories/{id}` | ดูรายตัว | — | 200 |
| PUT | `/api/v1/categories/{id}` | แก้ไข | `CategoryRequest` | 200 |
| DELETE | `/api/v1/categories/{id}` | ลบ (soft delete) | — | 204 |

### 3.2 Suppliers — `/api/v1/suppliers`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/suppliers` | สร้าง | `SupplierRequest` | 201 |
| GET | `/api/v1/suppliers` | รายการทั้งหมด | — | 200 |
| GET | `/api/v1/suppliers/{id}` | ดูรายตัว | — | 200 |
| PUT | `/api/v1/suppliers/{id}` | แก้ไข | `SupplierRequest` | 200 |
| DELETE | `/api/v1/suppliers/{id}` | ลบ (soft delete) | — | 204 |

### 3.3 Customers — `/api/v1/customers`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/customers` | สร้าง | `CustomerRequest` | 201 |
| GET | `/api/v1/customers` | รายการทั้งหมด | — | 200 |
| GET | `/api/v1/customers/search?phone=` | ค้นหาจากเบอร์โทร | — | 200 |
| GET | `/api/v1/customers/{id}` | ดูรายตัว | — | 200 |
| PUT | `/api/v1/customers/{id}` | แก้ไข | `CustomerRequest` | 200 |
| DELETE | `/api/v1/customers/{id}` | ลบ (soft delete) | — | 204 |

### 3.4 Products — `/api/v1/products`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/products` | สร้าง | `ProductRequest` | 201 `ProductAdminResponse` |
| GET | `/api/v1/products` | ค้นหา/แบ่งหน้า/เรียงลำดับ (ไม่มีราคาต้นทุน) | query ด้านล่าง | 200 `Page<ProductResponse>` |
| GET | `/api/v1/products/admin` | เหมือนด้านบน แต่มี `costPrice` และ `minimumStock` | query ด้านล่าง | 200 `Page<ProductAdminResponse>` |
| GET | `/api/v1/products/{id}` | ดูรายตัว | — | 200 `ProductResponse` |
| GET | `/api/v1/products/admin/{id}` | ดูรายตัว (มีราคาต้นทุน) | — | 200 `ProductAdminResponse` |
| PUT | `/api/v1/products/{id}` | แก้ไข | `ProductRequest` | 200 `ProductAdminResponse` |
| DELETE | `/api/v1/products/{id}` | ลบ (soft delete) | — | 204 |

Query ของรายการสินค้า (Pagination & Sorting)

| ชื่อ | ค่าเริ่มต้น | คำอธิบาย |
|---|---|---|
| `page` | `0` | หน้าที่ (เริ่มที่ 0) |
| `size` | `10` | จำนวนต่อหน้า |
| `sortBy` | `id` | ฟิลด์ที่ใช้เรียง เช่น `name`, `price` |
| `direction` | `asc` | `asc` หรือ `desc` |
| `keyword` | — | ค้นหาในชื่อสินค้า (ไม่สนตัวพิมพ์) |
| `categoryId` | — | กรองตามหมวดหมู่ (ใช้ร่วมกับ `keyword` ได้) |

### 3.5 Inventory — `/api/v1/inventory`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| GET | `/api/v1/inventory/products/{productId}` | ดูสต็อกของสินค้า | — | 200 `InventoryStockResponse` |
| PUT | `/api/v1/inventory/products/{productId}` | ขอปรับยอดสต็อกเป็นค่าที่กำหนด | `InventoryStockRequest` | 200 `InventoryStockResponse` |

การปรับยอดสร้างรายการ `ADJUSTMENT` สถานะ `PENDING` ยอดสต็อกจะเปลี่ยนเมื่อ `OWNER` อนุมัติ (ดูหัวข้อ 3.6)

### 3.6 Stock Movements — `/api/v1/stock-movements`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/stock-movements` | บันทึกรับเข้า/จ่ายออก/ปรับยอด | `StockMovementRequest` | 201 |
| GET | `/api/v1/stock-movements` | ประวัติทั้งหมด (ใหม่สุดก่อน) | — | 200 |
| GET | `/api/v1/stock-movements/products/{productId}` | ประวัติของสินค้า | — | 200 |
| POST | `/api/v1/stock-movements/{id}/approve` | อนุมัติ (เฉพาะ `PENDING`) — `OWNER` | — | 200 |
| POST | `/api/v1/stock-movements/{id}/reject` | ปฏิเสธ (เฉพาะ `PENDING`) — `OWNER` | — | 200 |

กติกา: `IN`/`OUT` มีผลกับสต็อกทันทีและสถานะเป็น `APPROVED`; `ADJUSTMENT` สถานะเป็น `PENDING` รอ `OWNER` อนุมัติ หลังบันทึก ถ้าสต็อกที่ใช้ได้ ≤ `minimumStock` ระบบส่ง `LowStockEvent`

### 3.7 Purchase Orders — `/api/v1/purchases`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/purchases` | สร้างใบสั่งซื้อ (สถานะ `PENDING`, เลข `PO-<UUID>`) | `PurchaseOrderRequest` | 201 |
| GET | `/api/v1/purchases` | รายการทั้งหมด (ใหม่สุดก่อน) | — | 200 |
| GET | `/api/v1/purchases/{id}` | ดูรายตัว | — | 200 |
| PUT | `/api/v1/purchases/{id}` | แก้ไข (เฉพาะ `PENDING`) | `PurchaseOrderRequest` | 200 |
| POST | `/api/v1/purchases/{id}/approve` | อนุมัติ (`PENDING` → `APPROVED`) — `OWNER` | — | 200 |
| POST | `/api/v1/purchases/{id}/receive` | รับของ เพิ่มสต็อกเป็น `IN` ต่อรายการ | `ReceivePurchaseRequest` | 200 |

สถานะใบสั่งซื้อ: `PENDING` → (approve) `APPROVED` → (receive) `PARTIAL` ถ้ายังรับไม่ครบ หรือ `COMPLETED` ถ้ารับครบทุกรายการ รับของได้เฉพาะสถานะ `APPROVED` หรือ `PARTIAL`

### 3.8 Sales Orders — `/api/v1/orders`

| Method | Path | คำอธิบาย | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/orders` | สร้างใบสั่งขาย ตรวจลูกค้า/สินค้า/สต็อก คิดส่วนลด และตัดสต็อกทันที | `SalesOrderRequest` | 201 |
| GET | `/api/v1/orders` | รายการทั้งหมด (ใหม่สุดก่อน) | — | 200 |
| GET | `/api/v1/orders/{id}` | ดูรายตัว | — | 200 |
| PUT | `/api/v1/orders/{id}` | แก้ไข (เฉพาะ `PENDING`) | `SalesOrderRequest` | 200 |
| POST | `/api/v1/orders/{id}/status` | เปลี่ยนสถานะ | `SalesOrderStatusRequest` | 200 |

ส่วนลด: ลูกค้าสมาชิกลด 10%; ไม่ใช่สมาชิกแต่ซื้อรวม ≥ 10 ชิ้นลด 10%; นอกนั้นราคาเต็ม ราคาต่อหน่วยใช้ `products.price` ที่เซิร์ฟเวอร์ ไม่รับจาก request

กติกาเปลี่ยนสถานะ (กำหนดใน State classes) — ที่ไม่ได้ระบุคือ 409

| สถานะปัจจุบัน | ไปได้ |
|---|---|
| `PENDING` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `SHIPPED`, `COMPLETED`, `CANCELLED` |
| `SHIPPED` | `COMPLETED` |
| `COMPLETED` / `CANCELLED` | ไม่มี |

ขอเปลี่ยนกลับเป็น `PENDING` ไม่ได้ (409) เมื่อ `CANCELLED` ระบบคืนสต็อก

### 3.9 Dashboard — `/api/v1/dashboard`

| Method | Path | คำอธิบาย | Response |
|---|---|---|---|
| GET | `/api/v1/dashboard/summary` | ตัวเลขสรุปสำหรับหน้า Dashboard | 200 `DashboardSummaryResponse` |

`DashboardSummaryResponse`: `products`, `stockUnits`, `lowStock`, `purchases`, `orders`, `suppliers`, `customers`, `recentOrders[]` (ใบสั่งขายล่าสุด 5 รายการ)

## 4. Request DTO และกฎ Validation

| DTO | ฟิลด์และกฎ |
|---|---|
| `CategoryRequest` | `name` จำเป็น ≤ 100; `description` ≤ 255 |
| `SupplierRequest` | `name` จำเป็น ≤ 100; `phone` ตัวเลข `+ - ( )` และช่องว่าง ≤ 20; `email` รูปแบบอีเมล ≤ 150; `address` ≤ 255 |
| `CustomerRequest` | `name` จำเป็น ≤ 150; `phone` จำเป็น ตัวเลข `+ - ( )` และช่องว่าง ≤ 20; `email` รูปแบบอีเมล ≤ 150; `address` ≤ 500; `member` boolean |
| `ProductRequest` | `sku` จำเป็น ≤ 100; `name` จำเป็น ≤ 150; `description` ≤ 500; `unit` จำเป็น ≤ 50; `price`, `costPrice`, `minimumStock` จำเป็น > 0; `categoryId`, `supplierId` จำเป็น |
| `InventoryStockRequest` | `quantity` จำเป็น ≥ 0 (ยอดใหม่ ไม่ใช่ส่วนต่าง); `reason` ไม่บังคับ |
| `StockMovementRequest` | `productId` จำเป็น; `movementType` จำเป็น (`IN`/`OUT`/`ADJUSTMENT`); `quantity` จำเป็น > 0; `referenceNo`, `note` ไม่บังคับ |
| `PurchaseOrderRequest` | `supplierId` จำเป็น > 0; `items[]` ห้ามว่าง — แต่ละรายการ `productId` > 0, `quantity` > 0, `unitCost` > 0 |
| `PurchaseItemRequest` | `productId` จำเป็น > 0; `quantity` จำเป็น > 0; `unitCost` จำเป็น > 0 |
| `ReceivePurchaseRequest` | `items[]` จำเป็น — แต่ละรายการ `productId` จำเป็น, `receivedQuantity` จำเป็น ≥ 0 |
| `ReceiveItemRequest` | `productId` จำเป็น; `receivedQuantity` จำเป็น ≥ 0 |
| `SalesOrderRequest` | `customerId` ไม่บังคับ; `shippingAddress`, `paymentMethod` ไม่บังคับ; `items[]` ห้ามว่าง — แต่ละรายการ `productId` > 0, `quantity` > 0 |
| `SalesOrderItemRequest` | `productId` จำเป็น > 0; `quantity` จำเป็น > 0; `unitPrice` deprecated — เซิร์ฟเวอร์ไม่ใช้ค่านี้ ใช้ `products.price` แทน |
| `SalesOrderStatusRequest` | `status` จำเป็น (`PENDING`, `CONFIRMED`, `SHIPPED`, `COMPLETED`, `CANCELLED`) |
