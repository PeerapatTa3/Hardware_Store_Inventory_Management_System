# Data Dictionary

พจนานุกรมข้อมูลของ **Hardware Store Inventory Management System** จัดทำจาก 2 แหล่งที่ต้องตรงกัน:

- **DDL จริง:** `code/backend/hardware-store/src/main/resources/schema.sql` (ฐานข้อมูล H2 in-memory ปัจจุบัน; `spring.jpa.hibernate.ddl-auto: validate` ใน `application.yml` : 15)
- **Entity:** `code/backend/hardware-store/src/main/java/com/hardwarestore/domain/entity/`

> ตอนนี้ฐานข้อมูลคือ H2 (`jdbc:h2:mem:hardwaredb`) สร้างใหม่ทุกครั้งที่รัน และใส่ข้อมูลตั้งต้นจาก `data.sql` — เมื่อย้ายไป PostgreSQL ให้ใช้โครงสร้างเดียวกันนี้ (ดูหัวข้อ "ข้อควรปรับเมื่อย้ายไป PostgreSQL")

---

## 1. ER Diagram

---

## 2. Tables

คำย่อ: **PK** primary key · **FK** foreign key · **UK** unique · **NN** not null

### 2.1 `categories` — หมวดหมู่สินค้า (Entity: `Category`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | รหัสหมวดหมู่ |
| `name` | VARCHAR(100) | NN, UK | | ชื่อหมวดหมู่ (ไม่ซ้ำ ไม่สนตัวพิมพ์เล็กใหญ่ในการตรวจที่ service) |
| `description` | VARCHAR(255) | | | คำอธิบาย |
| `is_active` | BOOLEAN | NN | TRUE | `FALSE` = ถูกลบแบบ soft delete |

### 2.2 `suppliers` — ซัพพลายเออร์ (Entity: `Supplier`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | รหัสซัพพลายเออร์ |
| `name` | VARCHAR(150) | NN, UK | | ชื่อ (entity ประกาศ length 100) |
| `phone` | VARCHAR(20) | | | เบอร์โทร (service ตรวจไม่ซ้ำ) |
| `email` | VARCHAR(150) | | | อีเมล (entity ประกาศ unique; service ตรวจไม่ซ้ำ) |
| `address` | VARCHAR(500) | | | ที่อยู่ (entity ประกาศ length 255) |
| `is_active` | BOOLEAN | NN | TRUE | soft delete |

### 2.3 `products` — สินค้า (Entity: `Product`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | รหัสสินค้า |
| `sku` | VARCHAR(50) | NN, UK | | รหัส SKU (entity length 100; ตรวจซ้ำแบบไม่สนตัวพิมพ์) |
| `name` | VARCHAR(150) | NN | | ชื่อสินค้า |
| `description` | VARCHAR(1000) | | | รายละเอียด (entity length 500) |
| `unit` | VARCHAR(50) | | | หน่วยนับ เช่น Piece, Set, Bag (entity: NN) |
| `price` | DECIMAL(19,2) | NN | | ราคาขาย — ใช้เป็น `unit_price` ของ Sales Order |
| `cost_price` | DECIMAL(19,2) | NN | | ราคาต้นทุน — เห็นเฉพาะ `ProductAdminResponse` |
| `minimum_stock` | INT | NN | 0 | จุดแจ้งเตือนสต็อกต่ำ |
| `category_id` | BIGINT | NN, FK → `categories.id` | | หมวดหมู่ (`fk_product_category`) |
| `supplier_id` | BIGINT | NN, FK → `suppliers.id` | | ซัพพลายเออร์หลัก (`fk_product_supplier`) |
| `is_active` | BOOLEAN | NN | TRUE | soft delete |

Index: `idx_product_sku (sku)`, `idx_product_name (name)`

### 2.4 `inventory_stocks` — ยอดสต็อกปัจจุบัน (Entity: `InventoryStock`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | |
| `product_id` | BIGINT | NN, UK, FK → `products.id` | | 1 สินค้า มีได้ 1 แถว (`fk_stock_product`) |
| `quantity` | INT | NN | 0 | จำนวนในคลังทั้งหมด |
| `reserved_quantity` | INT | NN | 0 | จำนวนที่จองไว้ |

**Derived (ไม่เก็บใน DB):** `availableQuantity = quantity - reserved_quantity` (`InventoryStock.getAvailableQuantity`, `InventoryStock.java` : 31-33) ใช้ตัดสินว่าสต็อกพอหรือไม่

### 2.5 `stock_movements` — ประวัติความเคลื่อนไหวสต็อก (Entity: `StockMovement`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | |
| `product_id` | BIGINT | NN, FK → `products.id` | | (`fk_movement_product`) |
| `movement_type` | VARCHAR(20) | NN | | `IN` / `OUT` / `ADJUSTMENT` |
| `quantity` | INT | NN | | จำนวน (`ADJUSTMENT` = ค่าสต็อกใหม่ ไม่ใช่ส่วนต่าง) |
| `reference_no` | VARCHAR(100) | | | เลขอ้างอิง เช่น `SO-…`, `PO-…` |
| `note` | VARCHAR(500) | | | หมายเหตุ |
| `movement_at` | TIMESTAMP | NN | | เวลาเกิดรายการ (`@CreatedDate`, กำหนดใน `@PrePersist`) |
| `created_by` | VARCHAR(50) | | | ผู้สร้าง (audit) |
| `updated_by` | VARCHAR(50) | | | ผู้แก้ล่าสุด (audit) |
| `updated_at` | TIMESTAMP | | | เวลาแก้ล่าสุด (audit) |
| `status` | VARCHAR(20) | | `'APPROVED'` | สถานะรายการ |
| `approved_by` | VARCHAR(50) | | | ผู้อนุมัติ |

ตารางนี้เป็น **append-only** — ไม่มี API ลบ/แก้

### 2.6 `customers` — ลูกค้า (Entity: `Customer`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | |
| `name` | VARCHAR(150) | NN | | ชื่อลูกค้า |
| `phone` | VARCHAR(20) | NN, UK | | เบอร์โทร (ไม่ซ้ำ) |
| `email` | VARCHAR(150) | | | อีเมล (service ตรวจไม่ซ้ำ) |
| `address` | VARCHAR(500) | | | ที่อยู่ |
| `is_member` | BOOLEAN | NN | FALSE | เป็นสมาชิกหรือไม่ — ได้ `MemberDiscount` |
| `created_at` | TIMESTAMP | NN | | วันที่สร้าง |
| `is_active` | BOOLEAN | NN | TRUE | soft delete |

Index: `idx_customer_phone (phone)`, `idx_customer_email (email)`

### 2.7 `sales_orders` — ใบสั่งขาย (Entity: `SalesOrder`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | |
| `order_number` | VARCHAR(50) | NN, UK | | เลขที่ใบสั่งขาย รูปแบบ `SO-<UUID>` |
| `customer_id` | BIGINT | **nullable**, FK → `customers.id` | | ลูกค้า (`NULL` = ขายหน้าร้านไม่ระบุลูกค้า) (`fk_so_customer`) |
| `status` | VARCHAR(20) | NN | | `PENDING` / `CONFIRMED` / `SHIPPED` / `COMPLETED` / `CANCELLED` |
| `total_amount` | DECIMAL(19,2) | NN | | ยอดรวม (ก่อนปิดงาน = ราคาเต็ม, ตอน `COMPLETED` = หลังส่วนลด) |
| `shipping_address` | VARCHAR(500) | | | ที่อยู่จัดส่ง |
| `payment_method` | VARCHAR(50) | | | วิธีชำระเงิน |
| `created_at` | TIMESTAMP | NN | | (audit, `@CreatedDate`) |
| `created_by` | VARCHAR(50) | | | (audit) |
| `updated_by` | VARCHAR(50) | | | (audit) |
| `updated_at` | TIMESTAMP | | | (audit) |

Index: `idx_sales_order_number (order_number)`

### 2.8 `sales_order_items` — รายการสินค้าในใบสั่งขาย (Entity: `SalesOrderItems`)

| Column | Type | Constraint | คำอธิบาย |
|--------|------|-----------|-----------|
| `id` | BIGINT | PK, auto-increment | |
| `sales_order_id` | BIGINT | NN, FK → `sales_orders.id` | (`fk_soi_so`) cascade ALL + orphanRemoval จาก `SalesOrder.items` |
| `product_id` | BIGINT | NN, FK → `products.id` | (`fk_soi_product`) |
| `quantity` | INT | NN | จำนวน |
| `unit_price` | DECIMAL(19,2) | NN | ราคา ณ เวลาที่ขาย — คัดลอกจาก `products.price` ฝั่งเซิร์ฟเวอร์ |
| `subtotal` | DECIMAL(19,2) | NN | `unit_price × quantity` (ปัดทศนิยม 2 ตำแหน่ง HALF_UP) |

### 2.9 `purchase_orders` — ใบสั่งซื้อจากซัพพลายเออร์ (Entity: `PurchaseOrder`)

| Column | Type | Constraint | Default | คำอธิบาย |
|--------|------|-----------|---------|-----------|
| `id` | BIGINT | PK, auto-increment | | |
| `purchase_number` | VARCHAR(50) | NN, UK | | เลขที่ใบสั่งซื้อ รูปแบบ `PO-<UUID>` |
| `supplier_id` | BIGINT | NN, FK → `suppliers.id` | | (`fk_po_supplier`) |
| `status` | VARCHAR(20) | NN | | `PENDING` / `COMPLETED` |
| `total_amount` | DECIMAL(19,2) | NN | | ผลรวม `subtotal` ของทุกรายการ |
| `created_at` | TIMESTAMP | NN | | (audit) |
| `created_by` / `updated_by` | VARCHAR(50) | | | (audit) |
| `updated_at` | TIMESTAMP | | | (audit) |

Index: `idx_purchase_order_number (purchase_number)`

### 2.10 `purchase_items` — รายการสินค้าในใบสั่งซื้อ (Entity: `PurchaseItem`)

| Column | Type | Constraint | คำอธิบาย |
|--------|------|-----------|-----------|
| `id` | BIGINT | PK, auto-increment | |
| `purchase_order_id` | BIGINT | NN, FK → `purchase_orders.id` | (`fk_pi_po`) cascade ALL + orphanRemoval |
| `product_id` | BIGINT | NN, FK → `products.id` | (`fk_pi_product`) |
| `quantity` | INT | NN | จำนวนที่สั่ง |
| `unit_cost` | DECIMAL(19,2) | NN | ต้นทุนต่อหน่วย |
| `subtotal` | DECIMAL(19,2) | NN | `unit_cost × quantity` |

### 2.11 `users` — ผู้ใช้ระบบ (Entity: `User`)

| Column | Type | Constraint | คำอธิบาย |
|--------|------|-----------|-----------|
| `id` | BIGINT | PK, auto-increment | |
| `username` | VARCHAR(50) | NN, UK | ชื่อผู้ใช้สำหรับ login |
| `password` | VARCHAR(100) | NN | รหัสผ่านที่เข้ารหัส **BCrypt** (`BCryptPasswordEncoder`) — ห้ามเก็บ plain text |
| `name` | VARCHAR(150) | | ชื่อ-นามสกุล |
| `role` | VARCHAR(50) | NN | `CASHIER` / `STOCK_MANAGER` / `OWNER` |
| `created_at` | TIMESTAMP | NN | วันที่สร้าง |

---

## 3. Enumerations

เก็บเป็นข้อความ (`@Enumerated(EnumType.STRING)`)

| Enum | ใช้ใน | ค่า | ความหมาย |
|------|-------|-----|-----------|
| `Role` (`Role.java`) | `users.role` | `CASHIER` | พนักงานขาย |
| | | `STOCK_MANAGER` | ผู้จัดการคลัง |
| | | `OWNER` | เจ้าของร้าน |
| `SalesOrderStatus` (`SalesOrderStatus.java`) | `sales_orders.status` | `PENDING` | สร้างแล้ว รอยืนยัน (แก้ไขได้) |
| | | `CONFIRMED` | ยืนยันแล้ว |
| | | `SHIPPED` | จัดส่งแล้ว (ยกเลิกไม่ได้) |
| | | `COMPLETED` | ปิดงานแล้ว (คิดส่วนลดตอนนี้) |
| | | `CANCELLED` | ยกเลิกแล้ว (คืนสต็อก) |
| `PurchaseOrderStatus` (`PurchaseOrderStatus.java`) | `purchase_orders.status` | `PENDING` | รอรับของ (แก้ไขได้) |
| | | `COMPLETED` | รับของแล้ว (สต็อกเพิ่มแล้ว) |
| `StockMovementType` (`StockMovementType.java`) | `stock_movements.movement_type` | `IN` | รับเข้า (+) |
| | | `OUT` | จ่ายออก (−) |
| | | `ADJUSTMENT` | ปรับยอดเป็นค่าที่กำหนด |

---

## 4. Relationships และ Cascade

| ความสัมพันธ์ | ชนิด | หมายเหตุ |
|---------------|------|-----------|
| `categories` 1 — N `products` | Many-to-One จาก Product (LAZY) | |
| `suppliers` 1 — N `products` | Many-to-One จาก Product (LAZY) | |
| `products` 1 — 1 `inventory_stocks` | One-to-One (LAZY) | `product_id` UK |
| `products` 1 — N `stock_movements` | Many-to-One (LAZY) | |
| `customers` 0..1 — N `sales_orders` | Many-to-One (LAZY), nullable | |
| `sales_orders` 1 — N `sales_order_items` | One-to-Many, `cascade = ALL`, `orphanRemoval = true`, `@OrderBy("id ASC")` | |
| `purchase_orders` 1 — N `purchase_items` | One-to-Many, `cascade = ALL`, `orphanRemoval = true`, `@OrderBy("id ASC")` | |
| `suppliers` 1 — N `purchase_orders` | Many-to-One (LAZY) | |

**Soft delete:** `products`, `customers`, `suppliers`, `categories` ใช้ `@SQLDelete` + `@SQLRestriction("is_active = true")` — การลบคือ `UPDATE … SET is_active = false` และ query ปกติไม่เห็นแถวที่ถูกลบ
`sales_orders`, `purchase_orders`, `stock_movements`, `*_items`, `inventory_stocks`, `users` **ไม่มี** soft delete และไม่มี API ลบ

**Audit:** `sales_orders`, `purchase_orders`, `stock_movements` ใช้ `AuditingEntityListener` (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`) เปิดใช้ด้วย `@EnableJpaAuditing` ใน `JpaConfig.java` : 7 และดึงชื่อผู้ใช้จาก `SecurityAuditorAware`

---

## 5. Business Rules ที่กระทบข้อมูล

| กฎ | ที่บังคับ |
|----|-----------|
| SKU, ชื่อหมวดหมู่, ชื่อ/อีเมล/เบอร์ซัพพลายเออร์, เบอร์/อีเมลลูกค้า ห้ามซ้ำ | Service → `DuplicateResourceException` (HTTP 409) + UK ใน DB |
| ราคาขาย/ต้นทุน/จุดสั่งซื้อขั้นต่ำต้องมากกว่า 0 | `ProductRequest` (`@Positive`) |
| สต็อกห้ามติดลบ | `StockMovementType.OUT` และ `StockAvailableHandler` |
| ขายแล้วตัดสต็อกทันทีตอนสร้าง order, ยกเลิกแล้วคืน | `SalesOrderServiceImpl` : 58, 160-162 |
| แก้ Sales Order ได้เฉพาะ `PENDING`; แก้ Purchase Order ได้เฉพาะ `PENDING` | `SalesOrderServiceImpl` : 85, `PurchaseOrderServiceImpl` : 73 |
| รับของ (`receive`) ได้ครั้งเดียว และจะเพิ่มสต็อกเป็น `IN` ต่อรายการ | `PurchaseOrderServiceImpl.receive` : 89-110 |
| สต็อกต่ำ (`available <= minimum_stock`) → publish `LowStockEvent` | `StockMovementServiceImpl` : 71-81 |

---

## 6. ข้อมูลตั้งต้น (`data.sql`)

| ตาราง | จำนวน | ตัวอย่าง |
|-------|-------|-----------|
| `categories` | 4 | Hand Tools, Power Tools, Building Materials, Plumbing |
| `suppliers` | 2 | Siam Hardware Supply, Makita Thailand |
| `products` | 3 | `HT-001` Claw Hammer 16oz (250.00), `PT-001` Cordless Drill 18V (3500.00), `BM-001` Portland Cement 50kg (150.00) |
| `inventory_stocks` | 3 | 50 / 15 / 200 ชิ้น |
| `customers` | 2 | General Walk-in (ไม่เป็นสมาชิก), Somchai Contractor (สมาชิก) |
| `users` | 1 | `admin` สิทธิ์ `OWNER` (รหัสผ่านเข้ารหัส BCrypt) |

---

## 7. ความไม่ตรงกันระหว่าง `schema.sql` กับ Entity (ควรรู้)

`ddl-auto: validate` ตรวจเฉพาะ**ชนิดคอลัมน์** ไม่ตรวจความยาว จึงยังรันได้ แต่ควรปรับให้ตรงกันก่อนขึ้น PostgreSQL:

| ตาราง.คอลัมน์ | `schema.sql` | Entity |
|---------------|--------------|--------|
| `products.sku` | VARCHAR(50) | length 100 (และ `ProductRequest` อนุญาต 100) |
| `products.description` | VARCHAR(1000) | length 500 |
| `products.unit` | nullable | `nullable = false` |
| `suppliers.name` | VARCHAR(150) | length 100 |
| `suppliers.address` | VARCHAR(500) | length 255 |
| `suppliers.email` | ไม่ unique | `unique = true` |
| `products.minimum_stock` | DEFAULT 0 | `ProductRequest` บังคับ `@Positive` (>0) |

## 8. ข้อควรปรับเมื่อย้ายไป PostgreSQL

- `BIGINT AUTO_INCREMENT` เป็นไวยากรณ์ H2 → ใน PostgreSQL ใช้ `BIGSERIAL` หรือ `BIGINT GENERATED BY DEFAULT AS IDENTITY` (entity ใช้ `GenerationType.IDENTITY` ซึ่งเข้ากันได้)
- `data.sql` ใช้ `CURRENT_TIMESTAMP` ซึ่งใช้ได้ทั้งสองระบบ
- ตั้ง `spring.sql.init.mode` ให้เหมาะสม (ตอนนี้ `always`) และพิจารณาใช้ Flyway/Liquibase แทน `schema.sql`

