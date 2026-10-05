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
- GET /api/v1/stock-movements
- GET /api/v1/stock-movements/products/{productId}

การสร้าง movement ประเภท `IN` หรือ `OUT` จะปรับ Inventory ใน transaction เดียวกัน
ส่วน `OUT` จะถูกปฏิเสธเมื่อจำนวนที่ขอเกิน available quantity.

- GET endpoints คืนรายการเรียงจาก movement ล่าสุดไปเก่าสุด
- การค้นหาด้วย Product ID ที่ไม่มีอยู่ตอบ HTTP 404; Product ที่มีอยู่แต่ไม่มี movement คืนรายการว่าง

## Purchase API
### Endpoints
- GET /api/v1/purchases
- GET /api/v1/purchases/{id}
- POST /api/v1/purchases
- PUT /api/v1/purchases/{id}
- POST /api/v1/purchases/{id}/receive

`POST` สำหรับสร้าง Purchase ตอบกลับ HTTP 201; `GET`, `PUT` และ `POST .../receive` ตอบกลับ HTTP 200 เมื่อสำเร็จ

ตัวอย่าง request สำหรับสร้างหรือแก้ไข:
```json
{
  "supplierId": 3,
  "items": [
    {
      "productId": 4,
      "quantity": 2,
      "unitCost": 12.50
    }
  ]
}
```

- `supplierId`, `productId`, `quantity` และ `unitCost` ต้องเป็นค่าบวก; `items` ต้องมีอย่างน้อยหนึ่งรายการ
- ระบบคำนวณ subtotal และ total จากรายการ; `status` และยอดรวมไม่ได้รับจาก request
- `PUT` แทนที่ Supplier และรายการสินค้าทั้งหมด และอนุญาตเฉพาะ Purchase สถานะ `PENDING`; สถานะอื่นตอบ HTTP 409
- ID ที่ไม่พบตอบ HTTP 404; request ที่ไม่ผ่าน validation ตอบ HTTP 400 ผ่าน `GlobalExceptionHandler`

`POST /api/v1/purchases/{id}/receive` รับสินค้าเข้า stock และบันทึก Stock Movement ประเภท `IN`
สำหรับทุกรายการ แล้วเปลี่ยนสถานะ Purchase เป็น `COMPLETED` ภายใน transaction เดียว
หากรายการใดล้มเหลว การเปลี่ยน stock, movement และสถานะจะ rollback ทั้งหมด
Purchase ที่ไม่ใช่ `PENDING` จะรับซ้ำไม่ได้; การรับสำเร็จอ้างอิง movement ด้วย purchase number

## Notes
- ใช้ Swagger UI สำหรับทดสอบ API
- Error response standardized ผ่าน `GlobalExceptionHandler`
