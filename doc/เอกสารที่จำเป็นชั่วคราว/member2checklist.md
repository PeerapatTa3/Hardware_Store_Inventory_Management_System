# Member 2 Checklist

รายการงานนี้สรุปจาก [PROJECT_PLAN.md](../../PROJECT_PLAN.md), [team-assignment.md](./team-assignment.md) และโค้ดที่มีอยู่ใน repository ขณะจัดทำเอกสาร

## สถานะที่ตรวจพบในโปรเจค

- [x] มี `InventoryStock`, `StockMovement` และ API สำหรับอ่าน/ปรับสต็อกและบันทึก stock movement แล้ว; การปรับยอดสร้าง `ADJUSTMENT` movement ใน transaction เดียวกัน
- [x] มี unit tests ของ `InventoryStockService` และ `StockMovementService`; ตรวจและต่อยอด ไม่สร้างโมดูลซ้ำ
- [ ] ยังไม่พบ `PurchaseOrder` / `PurchaseItem` หรือ Purchase API ใน backend
- [ ] งานรับสินค้าเข้า (Purchase Receive) ที่เชื่อม Purchase, Inventory และ StockMovement ยังต้องทำ
- [x] ทุกการเปลี่ยนจำนวน stock ผ่าน API ปัจจุบันของ Inventory หรือ StockMovement บันทึก StockMovement ควบคู่กันใน transaction เดียว
- [ ] เน้นพัฒนา Backend ก่อน; หลัง Backend เสร็จสมบูรณ์จึงพิจารณาขอบเขตและแนวทาง Frontend ร่วมกับทีม

> สถานะข้างต้นเป็นการอ่านไฟล์ใน working tree ไม่ใช่การยืนยันว่าโค้ดผ่านการทดสอบล่าสุด

## งานที่ต้องทำ

### 1. เตรียม branch และประสานสัญญา API

- [ ] ทำงานบน branch ของ Member 2 ตามชื่อที่ทีม/อาจารย์กำหนด (`phisit_673380285-2_01` ตาม team-assignment) และ sync กับ `develop`
- [ ] คุยกับผู้ดูแลโมดูลเดิมเพื่อยืนยัน DTO, exception, URL, วิธีปรับสต็อก และกติกา StockMovement ก่อนต่อยอด
- [x] รัน `mvnw.cmd test` จาก `code/backend/hardware-store` เพื่อเก็บผล baseline ก่อนแก้โค้ด (15 tests ผ่าน)
- [ ] ใช้ convention และ dependency ที่โปรเจคมีอยู่ก่อน; `PROJECT_PLAN.md` กล่าวถึง Flyway แต่ backend ที่ตรวจยังไม่มี dependency นี้ ให้ตกลงกับทีมก่อนเพิ่ม migration tooling

### 2. ตรวจและทำ Inventory / StockMovement ให้ครบตามกติกา

- [ ] ทบทวน `InventoryStock` ว่าความสัมพันธ์กับ `Product` เป็นหนึ่งต่อหนึ่ง และรองรับ quantity/reserved quantity ตามแบบข้อมูล
- [x] ยืนยันกติกาจำนวนคงเหลือ: ห้ามติดลบ, `OUT` ต้องไม่เกิน available quantity และ `ADJUSTMENT.quantity` คือยอดคงเหลือใหม่ โดยยอดใหม่ต้องไม่น้อยกว่า reserved quantity
- [x] ปรับ Inventory adjustment ให้สร้าง `ADJUSTMENT` movement ควบคู่กับการแก้ quantity
- [ ] เมื่อทำ Purchase Receive ให้เรียก flow รับสินค้าเพียงครั้งเดียว เพื่อไม่ให้ Inventory และ StockMovement ถูกปรับ/บันทึกซ้ำ
- [x] คงการเปลี่ยน Inventory และการบันทึก StockMovement ให้อยู่ใน transaction เดียวกัน
- [ ] ตรวจ API อ่าน stock และรายการ movement รวมถึงกรณีไม่พบ Product/Inventory และรูปแบบ error response
- [x] เพิ่มหรือปรับ service tests ให้ครอบคลุมการปรับยอด, การบันทึก `ADJUSTMENT` movement และการปฏิเสธยอดต่ำกว่า reserved quantity

### 3. สร้าง Purchase module

- [ ] เพิ่ม `PurchaseOrder` และ `PurchaseItem` ตามแบบข้อมูลและความสัมพันธ์ใน PROJECT_PLAN: PurchaseOrder มีหลายรายการ, แต่ละรายการอ้าง Product
- [ ] กำหนดสถานะและ lifecycle ของ Purchase ให้ชัดเจน เช่น สร้างเป็น `PENDING` และเปลี่ยนเป็น `COMPLETED` หลังรับสินค้า
- [ ] เพิ่ม repository, request/response DTO, mapper, service และ controller ตามรูปแบบ layered architecture ที่มีอยู่
- [ ] ตรวจสอบ Supplier และ Product ที่อ้างถึงว่ามีอยู่จริง; ตรวจจำนวนและ unit cost เป็นค่าที่ถูกต้อง; คำนวณ subtotal/total จากรายการ ไม่เชื่อยอดรวมที่ client ส่งมา
- [ ] รองรับ endpoint ตามแผน: `GET /api/v1/purchases`, `GET /api/v1/purchases/{id}`, `POST /api/v1/purchases`, `PUT /api/v1/purchases/{id}` และ `POST /api/v1/purchases/{id}/receive`
- [ ] กำหนดกติกาแก้ไข Purchase ให้ชัด เช่น แก้ได้เฉพาะก่อนรับสินค้า และปฏิเสธการรับซ้ำหรือเปลี่ยนสถานะที่ไม่ถูกต้อง
- [ ] ทำ Receive แบบ transaction เดียว: เพิ่ม stock ตามทุกรายการ, สร้าง StockMovement ชนิด `IN` ที่อ้างอิง Purchase และเปลี่ยนสถานะ Purchase; หากขั้นตอนใดล้มเหลวต้อง rollback ทั้งหมด
- [ ] ใช้ validation และ exception handler กลางของโปรเจค; ไม่เพิ่ม error handling ที่คืนผลสำเร็จเมื่อเกิดข้อผิดพลาด

### 4. ทดสอบและเชื่อมระบบ

- [ ] Unit tests: สร้าง Purchase รายการเดียว/หลายรายการ, คำนวณยอด, Product/Supplier ไม่มี, ข้อมูลผิดรูปแบบ และแก้ Purchase ตามสถานะ
- [ ] Receive tests: stock เพิ่มตามจำนวน, สร้าง movement `IN`, เปลี่ยนสถานะ, ปฏิเสธรับซ้ำ และไม่มีข้อมูลบางส่วนเมื่อเกิด failure/rollback
- [ ] Controller tests: HTTP method/status, request validation, response และ error cases ของ Purchase API
- [ ] ทดสอบร่วมกับ Inventory/StockMovement เดิมและรัน `mvnw.cmd test` ทั้งชุดก่อนเปิด PR
- [ ] ตรวจ Swagger/API documentation และอัปเดตเอกสารที่เกี่ยวข้องให้ตรงกับ API และ business rules จริง
- [ ] หลัง Backend เสร็จสมบูรณ์ ให้ทีมพิจารณาว่าจะทำ Frontend หรือไม่; หากตกลงทำ ให้กำหนดขอบเขตและแนวทางก่อนพัฒนาหน้า Inventory/Purchase และทดสอบ flow เรียก API จริง
- [ ] เปิด Pull Request จาก branch Member 2 เข้า `develop`, สรุปสิ่งที่ทำ/ผลทดสอบ และขอ reviewer อย่างน้อย 1 คน

## จุดแบ่งงานและ commit ที่แนะนำ

ทำเครื่องหมายเมื่อแต่ละ checkpoint เสร็จและตรวจสอบแล้ว ไม่ต้องสร้าง commit เปล่าหรือแยกการเปลี่ยนแปลงย่อยเกินจำเป็น รายการนี้แบ่งเป็น 8 checkpoints ตาม PROJECT_PLAN

| สถานะ | ลำดับ | งานที่ต้องเสร็จและตรวจสอบ | ตัวอย่าง commit message |
|---|---:|---|---|
| [x] | 1 | ปรับ Inventory/StockMovement ให้สอดคล้องกัน พร้อม unit tests | `fix: synchronize inventory adjustment movements` |
| [ ] | 2 | เพิ่ม PurchaseOrder/PurchaseItem, status, relationships และ repositories | `feat: add purchase persistence model` |
| [ ] | 3 | เพิ่ม Purchase DTO, validation, mapper และ tests ที่เกี่ยวข้อง | `feat: add purchase request and response models` |
| [ ] | 4 | ทำ create/list/get Purchase พร้อม service tests | `feat: add purchase creation and lookup` |
| [ ] | 5 | ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า | `feat: manage pending purchase orders` |
| [ ] | 6 | ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback | `feat: receive purchase into inventory` |
| [ ] | 7 | เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests | `test: verify purchase api and backend regression` |
| [ ] | 8 | อัปเดตเอกสาร API/ผลทดสอบ และตรวจความพร้อมก่อนเปิด PR | `docs: document inventory and purchase workflow` |

หลัง Backend เสร็จสมบูรณ์ ให้ทีมพิจารณา Frontend แยกต่างหาก หากตกลงทำ ให้จัด commit ตามขอบเขตงานจริง โดยไม่จำเป็นต้องนับรวมใน checkpoints ของ Backend ข้างต้น

## Definition of Done

- [ ] Inventory, StockMovement และ Purchase ทำงานร่วมกันโดยไม่ทำให้ quantity กับ movement history คลาดเคลื่อน
- [ ] การรับ Purchase อัปเดต stock, movement และสถานะครบใน transaction เดียว และป้องกันการรับซ้ำ
- [ ] Validation, business rules, error responses และ API ตรงกับเอกสาร
- [ ] Unit/controller tests ที่เกี่ยวข้องผ่าน และ `mvnw.cmd test` ผ่านทั้งชุด
- [ ] Pull Request เข้า `develop` ผ่าน review; ไม่มีการ push ตรงเข้า `main`
