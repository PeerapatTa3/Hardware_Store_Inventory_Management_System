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

ตัวอย่าง request สำหรับปรับยอด:
```json
{
  "quantity": 15,
  "reason": "ตรวจนับสินค้า"
}
```
- `quantity` คือยอดคงเหลือใหม่ ไม่ใช่จำนวนที่เพิ่มหรือลด
- การปรับยอดจะอัปเดต Inventory และสร้าง Stock Movement ประเภท `ADJUSTMENT` ใน transaction เดียวกัน
- `ADJUSTMENT.quantity` ในประวัติคือยอดคงเหลือใหม่ และยอดใหม่ต้องไม่น้อยกว่า reserved quantity

## Stock Movement API
- POST /api/v1/stock-movements

การสร้าง movement ประเภท `IN` หรือ `OUT` จะปรับ Inventory ใน transaction เดียวกัน
ส่วน `OUT` จะถูกปฏิเสธเมื่อจำนวนที่ขอเกิน available quantity.

## Purchase API
Endpoints ที่วางแผนไว้สำหรับ Purchase module:
- GET /api/v1/purchases
- GET /api/v1/purchases/{id}
- POST /api/v1/purchases
- PUT /api/v1/purchases/{id}
- POST /api/v1/purchases/{id}/receive

หมายเหตุ: Purchase REST controller ยังไม่ถูกเพิ่ม; endpoints เหล่านี้ยังเรียกใช้งานไม่ได้จนกว่าจะเสร็จในงาน API/controller
แก้ไข Purchase ได้เฉพาะสถานะ `PENDING`; สถานะอื่นจะถูกปฏิเสธด้วย HTTP 409
การแก้ไขแทนที่ Supplier และรายการสินค้าทั้งหมด พร้อมคำนวณ subtotal/total ใหม่จากรายการ
สถานะและยอดรวมไม่ได้รับจาก request เพื่อไม่ให้ client เปลี่ยน lifecycle หรือกำหนดยอดเอง

เมื่อเรียก receive ผ่าน service: ระบบเพิ่ม stock และบันทึก Stock Movement ประเภท `IN`
สำหรับทุกรายการ แล้วเปลี่ยนสถานะ Purchase เป็น `COMPLETED` ภายใน transaction เดียว
หากรายการใดล้มเหลว การเปลี่ยน stock, movement และสถานะจะ rollback ทั้งหมด
Purchase ที่ไม่ใช่ `PENDING` จะรับซ้ำไม่ได้; การรับสำเร็จอ้างอิง movement ด้วย purchase number

## Notes
- ใช้ Swagger UI สำหรับทดสอบ API
- Error response standardized ผ่าน `GlobalExceptionHandler`
