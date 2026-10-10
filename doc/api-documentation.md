# API Documentation

REST API ของ **Hardware Store Inventory Management System** (Spring Boot 4.1.1, Java 17)
เอกสารนี้จัดทำจาก controller/DTO จริงใน `code/backend/hardware-store/src/main/java/com/hardwarestore/`

| รายการ | ค่า |
|--------|-----|
| Base URL (local) | `http://localhost:8080` |
| Prefix ทุก endpoint (ยกเว้น auth) | `/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` (หรือ `/swagger-ui/index.html`) |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| H2 Console (dev) | `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:hardwaredb`, user `sa`, รหัสผ่านว่าง |
| Content-Type | `application/json` |
| การยืนยันตัวตน | JWT Bearer (`Authorization: Bearer <token>`) |

> **หมายเหตุ:** ทุก endpoint ต้องส่ง HTTP method ให้ถูก — เปิด URL ใน browser (GET) กับ endpoint ที่เป็น POST จะได้ error ไม่ใช่หน้าข้อมูล ให้ทดสอบผ่าน Swagger UI หรือ Postman

---

## 1. การยืนยันตัวตน (Authentication)

`SecurityConfig` (`…/config/SecurityConfig.java` : 27-42) ตั้งค่า:
- `/api/auth/**` เปิดให้เรียกได้โดยไม่ต้อง login
- `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/h2-console/**` เปิดให้เข้าได้
- **ทุก path อื่นต้องมี JWT** (`anyRequest().authenticated()`)
- Session แบบ `STATELESS`, ปิด CSRF

### POST `/api/auth/login`

**Request**
```json
{ "username": "admin", "password": "<รหัสผ่าน>" }
```

**Response 200**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "username": "admin", "role": "OWNER" }
```

**Response 401** — body เป็นข้อความธรรมดา `Invalid username or password` (ไม่ใช่ `ErrorResponse`)

**วิธีใช้ token ใน Swagger UI:** เรียก `/api/auth/login` → คัดลอก `token` → กดปุ่ม **Authorize** → วาง token (ไม่ต้องพิมพ์คำว่า `Bearer`) → Authorize

**ข้อมูลผู้ใช้ตั้งต้น:** `data.sql` มี user `admin` (role `OWNER`) เก็บรหัสผ่านแบบ BCrypt เท่านั้น — ถ้าไม่ทราบรหัสผ่านเดิม ให้สร้าง hash ใหม่ด้วย `BCryptPasswordEncoder` แล้วแทนค่าในคอลัมน์ `password` ของ `data.sql`

**รายละเอียด token** (`…/config/JwtUtil.java`): HS256, หมดอายุ 10 ชั่วโมง, `subject` = username
> คีย์ลงนามถูกสร้างสุ่มใหม่ทุกครั้งที่แอปสตาร์ท (`Keys.secretKeyFor`, บรรทัด 20) — **รีสตาร์ทแอปแล้ว token เก่าใช้ไม่ได้ ต้อง login ใหม่**

### สิทธิ์ (Roles)
`CASHIER`, `STOCK_MANAGER`, `OWNER` — ดูหัวข้อ 4 สำหรับสถานะการบังคับใช้จริง

---

## 2. รูปแบบ Error (`ErrorResponse`)

ทุก exception ที่ผ่าน `GlobalExceptionHandler` (`…/exception/GlobalExceptionHandler.java`) จะตอบรูปแบบเดียวกัน:

```json
{
  "timestamp": "2026-10-09T10:21:23.668",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product not found with id: 99",
  "path": "/api/v1/products/99"
}
```

| HTTP | `error` | เกิดเมื่อ | Exception |
|------|---------|-----------|-----------|
| 400 | `VALIDATION_FAILED` | body ไม่ผ่าน `@Valid` — `message` = `field: ข้อความ` คั่นด้วย `, ` | `MethodArgumentNotValidException` |
| 400 | `INVALID_OPERATION` | สต็อกไม่พอ, ค่าไม่ถูกต้อง | `IllegalArgumentException` |
| 404 | `RESOURCE_NOT_FOUND` | ไม่พบข้อมูล (id ไม่มี หรือถูก soft-delete) | `ResourceNotFoundException` |
| 409 | `DUPLICATE_RESOURCE` | SKU/ชื่อ/อีเมล/เบอร์ซ้ำ | `DuplicateResourceException` |
| 409 | `INVALID_PURCHASE_STATE` | แก้/รับของใบสั่งซื้อที่ไม่ใช่ `PENDING` | `InvalidPurchaseStateException` |
| 409 | `INVALID_SALES_ORDER_STATE` | เปลี่ยนสถานะ/แก้ใบสั่งขายผิดเงื่อนไข | `InvalidSalesOrderStateException`, `IllegalStateException` |
| 500 | `INTERNAL_SERVER_ERROR` | exception อื่นทั้งหมด (`message` คงที่ `Unexpected error occurred`) | `Exception` |

### ข้อควรระวัง: handler `Exception.class` (บรรทัด 60-65) กลืน error ของ Spring ไว้
เพราะจับ `Exception` ทุกชนิด error ต่อไปนี้จึงถูกแปลงเป็น **500** แทนที่จะเป็นรหัสที่ถูกต้อง:
- เรียก endpoint ผิด HTTP method (เช่น เปิด `/api/auth/login` ใน browser) → ควรเป็น 405
- body JSON พัง / enum ผิดค่า (เช่น `status: "XYZ"`) → ควรเป็น 400
- `AccessDeniedException` จาก `@PreAuthorize` (ถ้าเปิดใช้) → ควรเป็น 403

ถ้าเจอ 500 กับ `Unexpected error occurred` ให้ตรวจสอบสามกรณีนี้ก่อน

---

## 3. Endpoints

ลำดับตาราง: Method · Path · คำอธิบาย · Request · Response (สถานะสำเร็จ)

### 3.1 Categories — `/api/v1/categories` (`CategoryController`)

| Method | Path | คำอธิบาย | Request | Response |
|--------|------|-----------|---------|----------|
| POST | `/api/v1/categories` | สร้างหมวดหมู่ | `CategoryRequest` | 201 `CategoryResponse` |
| GET | `/api/v1/categories` | รายการทั้งหมด | — | 200 `CategoryResponse[]` |
| GET | `/api/v1/categories/{id}` | ดูรายตัว | — | 200 `CategoryResponse` |
| PUT | `/api/v1/categories/{id}` | แก้ไข | `CategoryRequest` | 200 `CategoryResponse` |
| DELETE | `/api/v1/categories/{id}` | ลบ (soft delete) | — | 204 |

### 3.2 Suppliers — `/api/v1/suppliers` (`SupplierController`)

| Method | Path | คำอธิบาย | Request | Response |
|--------|------|-----------|---------|----------|
| POST | `/api/v1/suppliers` | สร้าง (ตรวจชื่อ/อีเมล/เบอร์ไม่ซ้ำ) | `SupplierRequest` | 201 `SupplierResponse` |
| GET | `/api/v1/suppliers` | รายการทั้งหมด | — | 200 `SupplierResponse[]` |
| GET | `/api/v1/suppliers/{id}` | ดูรายตัว | — | 200 `SupplierResponse` |
| PUT | `/api/v1/suppliers/{id}` | แก้ไข | `SupplierRequest` | 200 `SupplierResponse` |
| DELETE | `/api/v1/suppliers/{id}` | ลบ (soft delete) | — | 204 |

### 3.3 Customers — `/api/v1/customers` (`CustomerController`)

| Method | Path | คำอธิบาย | Request | Response |
|--------|------|-----------|---------|----------|
| POST | `/api/v1/customers` | สร้าง (ตรวจเบอร์/อีเมลไม่ซ้ำ) | `CustomerRequest` | 201 `CustomerResponse` |
| GET | `/api/v1/customers` | รายการทั้งหมด | — | 200 `CustomerResponse[]` |
| GET | `/api/v1/customers/{id}` | ดูรายตัว | — | 200 `CustomerResponse` |
| PUT | `/api/v1/customers/{id}` | แก้ไข | `CustomerRequest` | 200 `CustomerResponse` |
| DELETE | `/api/v1/customers/{id}` | ลบ (soft delete) | — | 204 |

### 3.4 Products — `/api/v1/products` (`ProductController`)

| Method | Path | คำอธิบาย | Request | Response | สิทธิ์ที่ประกาศ (`@PreAuthorize`) |
|--------|------|-----------|---------|----------|-------------------------------------|
| POST | `/api/v1/products` | สร้างสินค้า | `ProductRequest` | 201 `ProductAdminResponse` | OWNER, STOCK_MANAGER |
| GET | `/api/v1/products` | ค้นหา/แบ่งหน้า (ไม่มีราคาต้นทุน) | query ด้านล่าง | 200 `Page<ProductResponse>` | ทุกคนที่ login |
| GET | `/api/v1/products/admin` | ค้นหา/แบ่งหน้า (มี `costPrice`) | query ด้านล่าง | 200 `Page<ProductAdminResponse>` | OWNER, STOCK_MANAGER |
| GET | `/api/v1/products/{id}` | ดูรายตัว (ไม่มีราคาต้นทุน) | — | 200 `ProductResponse` | ทุกคนที่ login |
| GET | `/api/v1/products/admin/{id}` | ดูรายตัว (มี `costPrice`) | — | 200 `ProductAdminResponse` | OWNER, STOCK_MANAGER |
| PUT | `/api/v1/products/{id}` | แก้ไข | `ProductRequest` | 200 `ProductAdminResponse` | OWNER, STOCK_MANAGER |
| DELETE | `/api/v1/products/{id}` | ลบ (soft delete) | — | 204 | OWNER |

**Query parameters ของรายการสินค้า**

| ชื่อ | ชนิด | ค่าเริ่มต้น | คำอธิบาย |
|------|------|-------------|-----------|
| `page` | int | `0` | หน้าที่ (เริ่มที่ 0) |
| `size` | int | `10` | จำนวนต่อหน้า |
| `sortBy` | string | `id` | ฟิลด์ที่เรียง (ต้องเป็นชื่อฟิลด์ของ `Product` เช่น `name`, `price`) |
| `direction` | string | `asc` | `asc` หรือ `desc` |
| `keyword` | string | — | ค้นหาในชื่อสินค้า (ไม่สนตัวพิมพ์) |
| `categoryId` | long | — | กรองตามหมวดหมู่ (ใช้ร่วมกับ `keyword` ได้) |

ผลลัพธ์เป็น `Page` ของ Spring Data (`content`, `totalElements`, `totalPages`, `number`, `size`, …)

### 3.5 Inventory — `/api/v1/inventory` (`InventoryController`)

| Method | Path | คำอธิบาย | Request | Response |
|--------|------|-----------|---------|----------|
| GET | `/api/v1/inventory/products/{productId}` | ดูสต็อกของสินค้า | — | 200 `InventoryStockResponse` |
| PUT | `/api/v1/inventory/products/{productId}` | ปรับยอดสต็อกเป็นค่าที่กำหนด (สร้าง movement ชนิด `ADJUSTMENT`) | `InventoryStockRequest` | 200 `InventoryStockResponse` |

### 3.6 Stock Movements — `/api/v1/stock-movements` (`StockMovementController`)

| Method | Path | คำอธิบาย | Request | Response | สิทธิ์ที่ประกาศ |
|--------|------|-----------|---------|----------|----------------|
| POST | `/api/v1/stock-movements` | บันทึกการรับเข้า/จ่ายออก/ปรับยอด | `StockMovementRequest` | 201 `StockMovementResponse` | — |
| GET | `/api/v1/stock-movements` | ประวัติทั้งหมด (ใหม่สุดก่อน) | — | 200 `StockMovementResponse[]` | — |
| GET | `/api/v1/stock-movements/products/{productId}` | ประวัติของสินค้า (ใหม่สุดก่อน) | — | 200 `StockMovementResponse[]` | — |
| POST | `/api/v1/stock-movements/{id}/approve` | อนุมัติการเคลื่อนไหวสต็อก | — | 200 `StockMovementResponse` | OWNER |
| POST | `/api/v1/stock-movements/{id}/reject` | ปฏิเสธการเคลื่อนไหวสต็อก | — | 200 `StockMovementResponse` | OWNER |

หลังบันทึก หากสต็อก `available <= minimumStock` ระบบ log คำเตือน `LOW STOCK` (ไม่กระทบ response)

### 3.7 Purchase Orders — `/api/v1/purchases` (`PurchaseOrderController`)

| Method | Path | คำอธิบาย | Request | Response | สิทธิ์ที่ประกาศ |
|--------|------|-----------|---------|----------|----------------|
| POST | `/api/v1/purchases` | สร้างใบสั่งซื้อ (สถานะ `PENDING`, เลข `PO-<UUID>`) | `PurchaseOrderRequest` | 201 `PurchaseOrderResponse` | — |
| GET | `/api/v1/purchases` | รายการทั้งหมด (ใหม่สุดก่อน) | — | 200 `PurchaseOrderResponse[]` | — |
| GET | `/api/v1/purchases/{id}` | ดูรายตัว | — | 200 `PurchaseOrderResponse` | — |
| PUT | `/api/v1/purchases/{id}` | แก้ไข — เฉพาะ `PENDING` | `PurchaseOrderRequest` | 200 `PurchaseOrderResponse` | — |
| POST | `/api/v1/purchases/{id}/approve` | อนุมัติใบสั่งซื้อ | — | 200 `PurchaseOrderResponse` | OWNER |
| POST | `/api/v1/purchases/{id}/receive` | รับของ: เพิ่มสต็อก (`IN`) ตามที่ระบุ → เปลี่ยนสถานะ | `ReceivePurchaseRequest` | 200 `PurchaseOrderResponse` | — |

### 3.8 Sales Orders — `/api/v1/orders` (`SalesOrderController`)

| Method | Path | คำอธิบาย | Request | Response |
|--------|------|-----------|---------|----------|
| POST | `/api/v1/orders` | สร้างใบสั่งขาย (สถานะ `PENDING`, เลข `SO-<UUID>`) — ตรวจลูกค้า/สินค้า/สต็อก แล้ว**ตัดสต็อกทันที** | `SalesOrderRequest` | 201 `SalesOrderResponse` |
| GET | `/api/v1/orders` | รายการทั้งหมด (ใหม่สุดก่อน) | — | 200 `SalesOrderResponse[]` |
| GET | `/api/v1/orders/{id}` | ดูรายตัว | — | 200 `SalesOrderResponse` |
| PUT | `/api/v1/orders/{id}` | แก้ไข — เฉพาะ `PENDING` (ปรับสต็อกเฉพาะส่วนต่าง) | `SalesOrderRequest` | 200 `SalesOrderResponse` |
| POST | `/api/v1/orders/{id}/status` | เปลี่ยนสถานะ | `SalesOrderStatusRequest` | 200 `SalesOrderResponse` |

**กฎการเปลี่ยนสถานะ** (`POST /{id}/status`)

| จาก → ไป | ผล |
|----------|-----|
| `PENDING` → `CONFIRMED` | ✔ |
| `PENDING` → `CANCELLED` | ✔ คืนสต็อก |
| `CONFIRMED` → `SHIPPED` | ✔ |
| `CONFIRMED` → `COMPLETED` | ✔ คิดส่วนลดแล้วเขียนทับ `totalAmount` |
| `CONFIRMED` → `CANCELLED` | ✔ คืนสต็อก |
| `SHIPPED` → `COMPLETED` | ✔ |
| `SHIPPED` → `CANCELLED` | ✘ 409 |
| `COMPLETED` / `CANCELLED` → อะไรก็ตาม | ✘ 409 |
| `PENDING` → `SHIPPED` / `COMPLETED` | ✘ 409 |
| ส่ง `status: "PENDING"` เมื่อไม่ได้เป็น `PENDING` | ✘ 409 |

**ส่วนลด** (คำนวณตอน `COMPLETED`): สมาชิก → ลด 10%; ไม่ใช่สมาชิกแต่ซื้อรวม ≥ 10 ชิ้น → ลด 10%; นอกนั้นราคาเต็ม — รายละเอียดใน `design-patterns.md`

---

## 4. สถานะการบังคับสิทธิ์ (ควรรู้ก่อนส่งงาน)

`ProductController`, `StockMovementController`, และ `PurchaseOrderController` มี `@PreAuthorize` แต่ใน `SecurityConfig` **ไม่มี `@EnableMethodSecurity`** ดังนั้นตอนนี้ annotation เหล่านี้ **ยังไม่ถูกบังคับใช้** — user ที่ login แล้ว role ใดก็เรียก endpoint เหล่านี้ได้

ถ้าต้องการให้สิทธิ์ทำงานตามที่ประกาศ ต้องเพิ่ม `@EnableMethodSecurity` บน `SecurityConfig` และแก้ `GlobalExceptionHandler` ให้ปล่อย `AccessDeniedException` ผ่าน (ไม่เช่นนั้นจะได้ 500 แทน 403)

---

## 5. Request DTO และกฎ Validation

ข้อความ error คือ `message` ที่ระบุใน annotation (ภาษาอังกฤษ)

### `CategoryRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `name` | string | จำเป็น (`@NotBlank`), ≤ 100 ตัวอักษร |
| `description` | string | ≤ 255 |

### `SupplierRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `name` | string | จำเป็น, ≤ 100 |
| `phone` | string | ตัวเลข `+ - ( )` และช่องว่างเท่านั้น, ≤ 20 |
| `email` | string | รูปแบบอีเมล, ≤ 150 |
| `address` | string | ≤ 255 |

### `CustomerRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `name` | string | จำเป็น, ≤ 150 |
| `phone` | string | จำเป็น, ตัวเลข `+ - ( )` และช่องว่างเท่านั้น, ≤ 20 |
| `email` | string | รูปแบบอีเมล, ≤ 150 |
| `address` | string | ≤ 500 |

> `CustomerRequest` ไม่มีฟิลด์ `member` — การตั้งสถานะสมาชิกยังไม่มี API (ค่าเริ่มต้น `false`, ตั้งผ่าน `data.sql`/DB)

### `ProductRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `sku` | string | จำเป็น, ≤ 100, ไม่ซ้ำ |
| `name` | string | จำเป็น, ≤ 150 |
| `description` | string | ≤ 500 |
| `unit` | string | จำเป็น, ≤ 50 |
| `price` | decimal | จำเป็น, > 0 |
| `costPrice` | decimal | จำเป็น, > 0 |
| `minimumStock` | integer | จำเป็น, > 0 |
| `categoryId` | long | จำเป็น |
| `supplierId` | long | จำเป็น |

### `InventoryStockRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `quantity` | integer | จำเป็น, ≥ 0 (เป็นค่ายอดใหม่ ไม่ใช่ส่วนต่าง) |
| `reason` | string | ไม่บังคับ (บันทึกเป็น `note`) |

### `StockMovementRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `productId` | long | จำเป็น |
| `movementType` | enum | จำเป็น: `IN`, `OUT`, `ADJUSTMENT` |
| `quantity` | integer | จำเป็น, > 0 |
| `referenceNo` | string | ไม่บังคับ |
| `note` | string | ไม่บังคับ |

### `PurchaseOrderRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `supplierId` | long | จำเป็น, > 0 |
| `items` | `PurchaseItemRequest[]` | จำเป็น ห้ามว่าง |

`PurchaseItemRequest`: `productId` (จำเป็น, > 0), `quantity` (จำเป็น, > 0), `unitCost` (จำเป็น, > 0)

### `ReceivePurchaseRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `items` | `ReceiveItemRequest[]` | จำเป็น ห้ามว่าง |

`ReceiveItemRequest`: `productId` (จำเป็น), `receivedQuantity` (จำเป็น, ≥ 0)

### `SalesOrderRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `customerId` | long | ไม่บังคับ (ไม่ส่ง = ลูกค้าทั่วไป) |
| `shippingAddress` | string | ไม่บังคับ |
| `paymentMethod` | string | ไม่บังคับ |
| `items` | `SalesOrderItemRequest[]` | จำเป็น ห้ามว่าง |

`SalesOrderItemRequest`: `productId` (จำเป็น, > 0), `quantity` (จำเป็น, > 0), `unitPrice` (**deprecated — เซิร์ฟเวอร์ไม่ใช้ค่านี้** ใช้ `products.price` แทน)

### `SalesOrderStatusRequest`
| ฟิลด์ | ชนิด | กฎ |
|-------|------|-----|
| `status` | enum | จำเป็น: `PENDING`, `CONFIRMED`, `SHIPPED`, `COMPLETED`, `CANCELLED` |

---

## 6. Response DTO

| DTO | ฟิลด์ |
|-----|-------|
| `AuthResponse` | `token`, `username`, `role` |
| `CategoryResponse` | `id`, `name`, `description` |
| `SupplierResponse` | `id`, `name`, `phone`, `email`, `address` |
| `CustomerResponse` | `id`, `name`, `phone`, `email`, `address` |
| `ProductResponse` | `id`, `sku`, `name`, `description`, `unit`, `price`, `categoryId`, `supplierId` |
| `ProductAdminResponse` | ทุกฟิลด์ของ `ProductResponse` + `costPrice`, `minimumStock` |
| `InventoryStockResponse` | `id`, `productId`, `productName`, `quantity`, `reservedQuantity`, `availableQuantity` |
| `StockMovementResponse` | `id`, `productId`, `productName`, `movementType`, `quantity`, `referenceNo`, `note`, `movementAt` |
| `PurchaseOrderResponse` | `id`, `purchaseNumber`, `supplierId`, `supplierName`, `status`, `totalAmount`, `createdAt`, `items[]` |
| `PurchaseItemResponse` | `id`, `productId`, `productName`, `quantity`, `unitCost`, `subtotal` |
| `SalesOrderResponse` | `id`, `orderNumber`, `customerId`, `customerName`, `shippingAddress`, `paymentMethod`, `status`, `totalAmount`, `createdAt`, `items[]` |
| `SalesOrderItemResponse` | `id`, `productId`, `productName`, `quantity`, `unitPrice`, `subtotal` |
| `ErrorResponse` | `timestamp`, `status`, `error`, `message`, `path` |

---

## 7. ตัวอย่างการใช้งาน (ลำดับทดสอบแนะนำ)

ข้อมูลตั้งต้นใน `data.sql`: สินค้า id 1-3, ลูกค้า id 1 (ทั่วไป) และ 2 (สมาชิก), ซัพพลายเออร์ id 1-2

**1) Login → ได้ token**
```http
POST /api/auth/login
{ "username": "admin", "password": "<รหัสผ่าน>" }
```

**2) สร้างใบสั่งขายให้ลูกค้าสมาชิก**
```http
POST /api/v1/orders
Authorization: Bearer <token>

{
  "customerId": 2,
  "shippingAddress": "789 Nonthaburi",
  "paymentMethod": "CASH",
  "items": [ { "productId": 1, "quantity": 2 } ]
}
```
→ 201, `status: "PENDING"`, `totalAmount: 500.00`, สต็อกสินค้า 1 ลดจาก 50 เหลือ 48

**3) ยืนยัน แล้วปิดงาน (คิดส่วนลด)**
```http
POST /api/v1/orders/{id}/status    { "status": "CONFIRMED" }
POST /api/v1/orders/{id}/status    { "status": "COMPLETED" }
```
→ `totalAmount` เปลี่ยนเป็น `450.00` (สมาชิกลด 10%)

**4) สั่งซื้อจากซัพพลายเออร์, อนุมัติ, แล้วรับของ**
```http
POST /api/v1/purchases
{ "supplierId": 1, "items": [ { "productId": 1, "quantity": 20, "unitCost": 150.00 } ] }

POST /api/v1/purchases/{id}/approve

POST /api/v1/purchases/{id}/receive
{ "items": [ { "productId": 1, "receivedQuantity": 20 } ] }
```
→ `status: "COMPLETED"`, สต็อกสินค้า 1 เพิ่มขึ้น 20

**5) ทดสอบ error**
```http
POST /api/v1/orders  { "items": [ { "productId": 1, "quantity": 99999 } ] }
```
→ 400 `INVALID_OPERATION` `Insufficient stock for product id: 1. Available: …`

```http
POST /api/v1/orders/{id}/status   { "status": "CANCELLED" }   // เมื่อสถานะเป็น COMPLETED
```
→ 409 `INVALID_SALES_ORDER_STATE`
