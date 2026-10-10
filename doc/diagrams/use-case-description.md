# Use Case Description

Use Case Diagram: `01-use-case.png` · สิทธิ์อ้างอิงจาก `@PreAuthorize` ในฝั่ง backend และ `RoleRoute` ใน `code/frontend/src/App.js`

## UC-01 Login

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Cashier, Stock Manager, Owner |
| Precondition | มีบัญชีผู้ใช้ในตาราง `users` |
| Main flow | 1) ผู้ใช้กรอก username/password 2) ระบบเรียก `POST /api/auth/login` 3) ตรวจรหัสผ่านด้วย BCrypt 4) ระบบออก JWT และคืน `token`, `username`, `role` 5) frontend เก็บ token และแนบ `Authorization: Bearer` ในทุก request |
| Exception flow | username หรือ password ผิด → 401 · token ไม่ถูกต้องหรือไม่มี → 401 · role ไม่มีสิทธิ์ → 403 |
| Postcondition | ผู้ใช้เข้าถึงเมนูตาม role |

## UC-02 Create / edit sales order

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Cashier (Owner ทำได้เช่นกัน) |
| Precondition | login แล้ว มีสินค้าและสต็อกในระบบ |
| Main flow | 1) กรอกรายการสินค้า (+ ลูกค้า ที่อยู่จัดส่ง วิธีชำระเงิน ถ้ามี) 2) `POST /api/v1/orders` 3) Validation Chain ตรวจ ลูกค้า → สินค้า → สต็อก 4) ระบบใช้ราคาจาก `products.price` และเลือกส่วนลด (สมาชิก 10% / ซื้อ ≥ 10 ชิ้น 10% / ราคาเต็ม) 5) บันทึกใบสั่งขาย 6) บันทึก stock movement `OUT` ต่อรายการ 7) ตอบ 201 |
| Alternative flow | แก้ไขใบสั่งขาย `PUT /api/v1/orders/{id}` ทำได้เฉพาะสถานะ `PENDING` ระบบตรวจซ้ำและปรับสต็อกเฉพาะส่วนต่าง |
| Exception flow | ไม่พบลูกค้า/สินค้า → 404 · สต็อกไม่พอ → 400 · ข้อมูลไม่ผ่าน validation → 400 · แก้ไขใบที่ไม่ใช่ `PENDING` → 409 |
| Postcondition | มีใบสั่งขายและสต็อกถูกตัดแล้ว หากสต็อกที่ใช้ได้ ≤ `minimumStock` ระบบส่ง `LowStockEvent` |

## UC-03 Change sales order status

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Cashier (Owner ทำได้เช่นกัน) |
| Precondition | login แล้ว มีใบสั่งขาย |
| Main flow | 1) เลือกสถานะใหม่ 2) `POST /api/v1/orders/{id}/status` 3) `SalesOrder` ส่งต่อไปยัง State ปัจจุบัน 4) State ที่อนุญาตเปลี่ยนสถานะ 5) บันทึก 6) ตอบ 200 |
| Alternative flow | เปลี่ยนเป็น `CANCELLED` → ระบบบันทึก stock movement `IN` เพื่อคืนสต็อกทุกรายการ |
| Exception flow | ไม่พบใบสั่งขาย → 404 · การเปลี่ยนที่ State ไม่อนุญาต (เช่น `SHIPPED` → `CANCELLED`, สถานะ `COMPLETED`/`CANCELLED` เปลี่ยนต่อไม่ได้) หรือขอกลับเป็น `PENDING` → 409 |
| Postcondition | สถานะใบสั่งขายเปลี่ยนตามกติกาของ State |

## UC-04 Create / approve purchase order

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Stock Manager สร้าง/แก้ไข · Owner อนุมัติ |
| Precondition | login แล้ว มีซัพพลายเออร์และสินค้า |
| Main flow | 1) กรอกซัพพลายเออร์และรายการ (จำนวน ต้นทุนต่อหน่วย) 2) `POST /api/v1/purchases` ได้ใบสั่งซื้อสถานะ `PENDING` 3) Owner เรียก `POST /api/v1/purchases/{id}/approve` 4) สถานะเป็น `APPROVED` |
| Alternative flow | แก้ไขใบสั่งซื้อ `PUT /api/v1/purchases/{id}` ได้เฉพาะ `PENDING` |
| Exception flow | ไม่พบซัพพลายเออร์/สินค้า → 404 · อนุมัติหรือแก้ไขใบที่ไม่ใช่ `PENDING` → 409 · ผู้ที่ไม่ใช่ Owner อนุมัติ → 403 |
| Postcondition | ใบสั่งซื้อสถานะ `APPROVED` พร้อมรับของ |

## UC-05 Receive purchase

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Stock Manager (Owner ทำได้เช่นกัน) |
| Precondition | ใบสั่งซื้อสถานะ `APPROVED` หรือ `PARTIAL` |
| Main flow | 1) ระบุจำนวนที่รับต่อสินค้า 2) `POST /api/v1/purchases/{id}/receive` 3) ระบบบวก `receivedQuantity` และบันทึก stock movement `IN` ต่อรายการ 4) ถ้ารับครบทุกรายการสถานะเป็น `COMPLETED` ถ้ายังไม่ครบเป็น `PARTIAL` |
| Exception flow | ไม่พบใบสั่งซื้อ → 404 · สถานะไม่ใช่ `APPROVED`/`PARTIAL` → 409 · ข้อมูลไม่ผ่าน validation → 400 · เกิด exception ใดๆ → rollback ทั้งหมด |
| Postcondition | สต็อกเพิ่มตามที่รับ และสถานะใบสั่งซื้อถูกอัปเดต |

## UC-06 Request stock adjustment / approve stock movement

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Stock Manager ขอปรับยอด · Owner อนุมัติ/ปฏิเสธ |
| Precondition | login แล้ว มีสินค้า |
| Main flow | 1) ระบุยอดสต็อกใหม่ 2) `PUT /api/v1/inventory/products/{productId}` สร้างรายการ `ADJUSTMENT` สถานะ `PENDING` 3) Owner เรียก `POST /api/v1/stock-movements/{id}/approve` 4) ระบบปรับยอดสต็อกและสถานะเป็น `APPROVED` |
| Alternative flow | Owner เรียก `.../reject` → สถานะเป็น `REJECTED` สต็อกไม่เปลี่ยน |
| Exception flow | ไม่พบสินค้า/รายการ → 404 · อนุมัติหรือปฏิเสธรายการที่ไม่ใช่ `PENDING` → 409 · ยอดใหม่น้อยกว่ายอดที่จอง → 400 · ผู้ที่ไม่ใช่ Owner → 403 |
| Postcondition | ยอดสต็อกถูกปรับ (เมื่ออนุมัติ) |

## UC-07 Manage products

| หัวข้อ | รายละเอียด |
|---|---|
| Actor | Stock Manager, Owner (ลบสินค้าได้เฉพาะ Owner) |
| Precondition | login แล้ว มีหมวดหมู่และซัพพลายเออร์ |
| Main flow | สร้าง/แก้ไข/ลบ (soft delete) สินค้าผ่าน `/api/v1/products` ดูรายการแบบแบ่งหน้าและเรียงลำดับได้ (`page`, `size`, `sortBy`, `direction`, `keyword`, `categoryId`) และดูราคาต้นทุนผ่าน `/api/v1/products/admin` |
| Exception flow | SKU ซ้ำ → 409 · ไม่พบสินค้า → 404 · ข้อมูลไม่ผ่าน validation → 400 · role ไม่มีสิทธิ์ → 403 |
| Postcondition | ข้อมูลสินค้าถูกบันทึก (การลบตั้ง `is_active = false`) |
