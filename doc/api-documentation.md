# API Documentation

## ภาพรวม
โครงการนี้มี REST API แบบ Spring Boot สำหรับการจัดการสินค้าในคลัง การจัดซื้อ การขาย และข้อมูลลูกค้า/ผลิตภัณฑ์ API นี้ถูกจัดไว้ภายใต้ prefix `/api/v1` และออกแบบมาเพื่อรองรับการทำงานของคลังสินค้าด้วยการตรวจสอบข้อมูล การอัปเดตแบบปลอดภัยทางธุรกรรม และการจัดการข้อผิดพลาดที่เป็นมาตรฐาน

Base URL:
- `http://localhost:8080/api/v1`

## Category API
### Endpoints
- `GET /api/v1/categories`
- `GET /api/v1/categories/{id}`
- `POST /api/v1/categories`
- `PUT /api/v1/categories/{id}`
- `DELETE /api/v1/categories/{id}`

### Note
- ใช้สำหรับจัดการกลุ่มสินค้า
- ชื่อหมวดหมู่ควรมีความเป็นเอกลักษณ์
- หากไม่มี ID ที่ระบุจะคืนค่า `404 Not Found`

## Supplier API
### Endpoints
- `GET /api/v1/suppliers`
- `GET /api/v1/suppliers/{id}`
- `POST /api/v1/suppliers`
- `PUT /api/v1/suppliers/{id}`
- `DELETE /api/v1/suppliers/{id}`

### Note
- จัดการข้อมูลหลักของซัพพลายเออร์
- เบอร์โทรศัพท์และอีเมลต้องไม่ซ้ำ
- ข้อผิดพลาดด้านการตรวจสอบข้อมูลจะถูกส่งกลับผ่าน `GlobalExceptionHandler`

## Customer API
### Endpoints
- `GET /api/v1/customers`
- `GET /api/v1/customers/{id}`
- `POST /api/v1/customers`
- `PUT /api/v1/customers/{id}`
- `DELETE /api/v1/customers/{id}`

### ตัวอย่าง Request
```json
{
  "name": "Jane Doe",
  "phone": "0812345678",
  "email": "jane@example.com",
  "address": "Bangkok"
}
```

### Validation rules
- `name`, `phone`, และ `email` ต้องไม่เป็นค่าว่าง
- `email` ต้องมีรูปแบบอีเมลที่ถูกต้อง
- `phone` อาจมีตัวเลขหรืออักขระสำหรับจัดรูปแบบ เช่น `+`, `-`, `(`, `)`, และช่องว่าง
- ค่า `phone` หรือ `email` ที่ซ้ำกันจะคืนค่า `409 Conflict`
- หากไม่มี ID ของลูกค้า จะคืนค่า `404 Not Found`

## Product API
### Endpoints
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products`
- `PUT /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`

### Note
- SKU ของสินค้าเป็นค่าที่ไม่ซ้ำ
- ข้อมูลสินค้าเชื่อมโยงกับหมวดหมู่และซัพพลายเออร์
- ราคาขาย ต้นทุน และขั้นต่ำของสต็อกถือเป็นฟิลด์ธุรกิจที่ต้องมีการตรวจสอบความถูกต้อง

## Inventory API
### Endpoint
- `GET /api/v1/inventory/products/{productId}`
- `PUT /api/v1/inventory/products/{productId}`

### ตัวอย่าง Request สำหรับปรับปรุงสต็อก
```json
{
  "quantity": 15,
  "reason": "ตรวจนับสินค้า"
}
```

### Behaviour
- `quantity` คือจำนวนสต็อกใหม่ ไม่ใช่จำนวนที่เพิ่มหรือลด
- การปรับปรุงคลังสินค้าและการบันทึกรายการเคลื่อนไหวจะทำภายใน transaction เดียวกัน
- ประเภทการเคลื่อนไหวสำหรับการทำงานนี้คือ `ADJUSTMENT`
- จำนวนสต็อกหลังปรับปรุงต้องไม่ต่ำกว่าจำนวนที่ถูกจองไว้
- หากสินค้าไม่พบ ระบบจะคืนค่า `404 Not Found`

## Stock API
### Endpoint
- `POST /api/v1/stock-movements`
- `GET /api/v1/stock-movements`
- `GET /api/v1/stock-movements/products/{productId}`

### Behaviour
- การเคลื่อนไหวประเภท `IN` และ `OUT` จะอัปเดตสต็อกใน transaction เดียวกัน
- การเคลื่อนไหวประเภท `OUT` จะถูกปฏิเสธเมื่อจำนวนที่ร้องขอเกินสต็อกที่มีอยู่
- รายการผลลัพธ์จะถูกส่งกลับแบบ newest-first
- ถ้าไม่มี product ID ที่ถูกส่งมาจะคืนค่า `404 Not Found`
- หากสินค้ามีอยู่แต่ไม่มีประวัติการเคลื่อนไหว ระบบจะคืนค่าเป็นรายการว่าง

## Purchase API
### Endpoint
- `GET /api/v1/purchases`
- `GET /api/v1/purchases/{id}`
- `POST /api/v1/purchases`
- `PUT /api/v1/purchases/{id}`
- `POST /api/v1/purchases/{id}/receive`

### ตัวอย่าง Request สำหรับสร้างหรืออัปเดต
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

### Rules
- `POST /api/v1/purchases` จะคืนค่า `201 Created`
- `GET`, `PUT`, และ `POST /api/v1/purchases/{id}/receive` จะคืนค่า `200 OK` เมื่อทำสำเร็จ
- `supplierId`, `productId`, `quantity`, และ `unitCost` ต้องมีค่ามากกว่า 0
- ต้องมีอย่างน้อย 1 รายการในคำขอซื้อ
- ราคารายการและราคารวมจะคำนวณอัตโนมัติจากรายการสินค้า
- `status` และยอดรวมจะไม่รับจาก client
- `PUT` จะแทนที่ซัพพลายเออร์และรายการสั่งซื้อทั้งหมด แต่เฉพาะเมื่อสั่งซื้อยังอยู่ในสถานะ `PENDING` เท่านั้น หากอยู่ในสถานะอื่นจะคืนค่า `409 Conflict`
- หากไม่มี ID ที่ระบุจะคืนค่า `404 Not Found`
- ข้อมูลที่ไม่ถูกต้องจะคืนค่า `400 Bad Request` ผ่าน `GlobalExceptionHandler`

### Receive stock flow
`POST /api/v1/purchases/{id}/receive` จะรับสินค้าเข้าคลังและบันทึก `Stock Movement` ประเภท `IN` สำหรับแต่ละรายการ พร้อมทั้งตั้งสถานะการสั่งซื้อเป็น `COMPLETED` ภายใน transaction เดียวกัน

หากมีการทำงานใด ๆ ล้มเหลว การอัปเดตสต็อก การบันทึกประวัติการเคลื่อนไหว และการเปลี่ยนสถานะจะถูก rollback ร่วมกัน การรับสินค้าอีกครั้งสำหรับคำสั่งซื้อที่ไม่อยู่ในสถานะ `PENDING` จะไม่อนุญาต

## Common response conventions
- ข้อผิดพลาดจากการตรวจสอบข้อมูลและข้อผิดพลาดทางธุรกิจจะถูกจัดการโดย `GlobalExceptionHandler`
- ข้อมูลที่หายไปจะคืนค่า `404 Not Found`
- ข้อจำกัดเรื่องข้อมูลซ้ำจะคืนค่า `409 Conflict`
- การสร้างสำเร็จจะคืนค่า `201 Created` และการอัปเดตหรืออ่านข้อมูลมักคืนค่า `200 OK`

## API usage notes
- Swagger UI เป็นเครื่องมือที่แนะนำสำหรับการทดสอบด้วยมือ
- Logic ทางธุรกิจหลักทำงานแบบ transactional และได้รับการป้องกันด้วยการตรวจสอบข้อมูลใน service layer
- กระบวนการคลังสินค้า การสั่งซื้อ และประวัติการเคลื่อนไหวถูกออกแบบให้รักษาความสอดคล้องของจำนวนสินค้าและประวัติการเคลื่อนไหว
