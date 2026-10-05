# Member 2 Checklist

รายการงานนี้สรุปจาก [PROJECT_PLAN.md](../../PROJECT_PLAN.md), [team-assignment.md](./team-assignment.md) และโค้ดที่มีอยู่ใน repository ตรวจสถานะล่าสุดวันที่ 2026-10-06

## สถานะที่ตรวจพบในโปรเจค

- [x] มี `InventoryStock`, `StockMovement` และ API สำหรับอ่าน/ปรับสต็อกและบันทึก stock movement แล้ว; การปรับยอดสร้าง `ADJUSTMENT` movement ใน transaction เดียวกัน
- [x] Stock Movement API รองรับ POST, GET รายการทั้งหมด และ GET ตาม Product ID ตาม `PROJECT_PLAN.md`
- [x] มี unit tests ของ `InventoryStockService` และ `StockMovementService`; ตรวจและต่อยอด ไม่สร้างโมดูลซ้ำ
- [x] มี `PurchaseOrder` / `PurchaseItem` และ Purchase API ใน backend
- [x] งานรับสินค้าเข้า (Purchase Receive) เชื่อม Purchase, Inventory และ StockMovement แล้ว
- [x] ทุกการเปลี่ยนจำนวน stock ผ่าน API ปัจจุบันของ Inventory หรือ StockMovement บันทึก StockMovement ควบคู่กันใน transaction เดียว
- [x] งานที่ทำในรอบนี้เน้น Backend; ยังไม่เริ่ม Frontend ตามแนวทางให้พิจารณาหลัง Backend เสร็จ

> สถานะโค้ดตรวจจาก working tree และรัน `.\mvnw.cmd test` ล่าสุดผ่าน 63 tests, 0 failures, 0 errors; รายการที่ยังไม่ครบระบุไว้เป็น unchecked ด้านล่าง

## งานที่ต้องทำ

### 1. เตรียม branch และประสานสัญญา API

- [x] ทำงานบน branch ของ Member 2 (`phisit_673380285-2_01`); branch ติดตาม `origin/phisit_673380285-2_01` และ `develop` เป็น ancestor ของ branch ปัจจุบัน
- [ ] คุยกับผู้ดูแลโมดูลเดิมเพื่อยืนยัน DTO, exception, URL, วิธีปรับสต็อก และกติกา StockMovement ก่อนต่อยอด
- [x] เก็บผล baseline ก่อนแก้โค้ดไว้แล้ว: 15 tests ผ่าน; ผลล่าสุดหลังพัฒนาให้ดูผล regression ด้านล่าง
- [x] ใช้ convention และ dependency ที่โปรเจคมีอยู่; ยังไม่เพิ่ม Flyway เพราะ backend ปัจจุบันไม่ได้ใช้ migration tooling

### 2. ตรวจและทำ Inventory / StockMovement ให้ครบตามกติกา

- [x] ทบทวน `InventoryStock`: เชื่อมกับ `Product` แบบ `@OneToOne` และมี quantity/reserved quantity
- [x] ยืนยันกติกาจำนวนคงเหลือ: ห้ามติดลบ, `OUT` ต้องไม่เกิน available quantity และ `ADJUSTMENT.quantity` คือยอดคงเหลือใหม่ โดยยอดใหม่ต้องไม่น้อยกว่า reserved quantity
- [x] ปรับ Inventory adjustment ให้สร้าง `ADJUSTMENT` movement ควบคู่กับการแก้ quantity
- [x] Purchase Receive เรียก flow รับสินค้าเพียงครั้งเดียว เพื่อไม่ให้ Inventory และ StockMovement ถูกปรับ/บันทึกซ้ำ
- [x] คงการเปลี่ยน Inventory และการบันทึก StockMovement ให้อยู่ใน transaction เดียวกัน
- [x] ตรวจ API อ่าน/ปรับ Inventory: มี GET/PUT ตามแผน; มี service test กรณีไม่พบ Product และ error handler กลาง
- [x] เพิ่ม Stock Movement GET endpoints ตาม `PROJECT_PLAN.md` (`GET /api/v1/stock-movements` และ `GET /api/v1/stock-movements/products/{productId}`); เรียง movement ล่าสุดก่อน, Product ไม่มีตอบ 404 และ Product ที่ไม่มี movement คืน list ว่าง
- [x] เพิ่ม Inventory และ Stock Movement Controller tests ครอบคลุม GET/PUT/POST, validation และ not-found response
- [x] เพิ่มหรือปรับ service tests ให้ครอบคลุมการปรับยอด, การบันทึก `ADJUSTMENT` movement และการปฏิเสธยอดต่ำกว่า reserved quantity

### 3. สร้าง Purchase module

- [x] เพิ่ม `PurchaseOrder` และ `PurchaseItem` ตามแบบข้อมูลและความสัมพันธ์ใน PROJECT_PLAN: PurchaseOrder มีหลายรายการ, แต่ละรายการอ้าง Product
- [x] กำหนดสถานะและ lifecycle ของ Purchase: สร้างเป็น `PENDING` และเปลี่ยนเป็น `COMPLETED` หลังรับสินค้า (แก้ไขได้เฉพาะ `PENDING`)
- [x] เพิ่ม repository, request/response DTO, mapper และ service ตามรูปแบบ layered architecture ที่มีอยู่
- [x] ตรวจสอบ Supplier และ Product ที่อ้างถึงว่ามีอยู่จริง; validate จำนวนและ unit cost; คำนวณ subtotal/total จากรายการ ไม่รับยอดรวมจาก client
- [x] รองรับ endpoint ตามแผน: `GET /api/v1/purchases`, `GET /api/v1/purchases/{id}`, `POST /api/v1/purchases`, `PUT /api/v1/purchases/{id}` และ `POST /api/v1/purchases/{id}/receive`
- [x] กำหนดกติกาแก้ไข Purchase ให้แก้ได้เฉพาะ `PENDING`; สถานะอื่นตอบกลับ HTTP 409
- [x] ทำ Receive แบบ transaction เดียว: เพิ่ม stock ตามทุกรายการ, สร้าง StockMovement ชนิด `IN` ที่อ้างอิง Purchase และเปลี่ยนสถานะ Purchase; หากขั้นตอนใดล้มเหลวต้อง rollback ทั้งหมด
- [x] ใช้ validation และ exception handler กลางของโปรเจค; ไม่เพิ่ม error handling ที่คืนผลสำเร็จเมื่อเกิดข้อผิดพลาด

### 4. ทดสอบและเชื่อมระบบ

- [x] Unit tests: สร้าง Purchase รายการเดียว/หลายรายการ, คำนวณยอด, Product/Supplier ไม่มี, ข้อมูลผิดรูปแบบ และแก้ Purchase ตามสถานะ
- [x] Receive tests: stock เพิ่มตามจำนวน, สร้าง movement `IN`, เปลี่ยนสถานะ, ปฏิเสธรับซ้ำ และไม่มีข้อมูลบางส่วนเมื่อเกิด failure/rollback
- [x] Controller tests: HTTP method/status, request validation, response และ error cases ของ Purchase API
- [x] ทดสอบ receive ร่วมกับ Inventory/StockMovement และรัน `mvnw.cmd test` ทั้งชุดก่อนเปิด PR
- [x] Integration test ผ่าน HTTP API ตั้งแต่สร้าง Purchase ถึงรับสินค้า โดยตรวจ Purchase status, Inventory และ Stock Movement ที่บันทึกในฐานข้อมูล
- [x] ตรวจ API documentation และอัปเดต endpoints, validation, status codes และ business rules ให้ตรงกับ implementation
- [x] สร้าง [Member2_test.md](../../test/Member2_Test/Member2_test.md) สรุป test cases ของ Member 2; และ [Member2TestCommit.md](../../test/Member2_Test/Member2TestCommit.md) แยก test ตาม commit
- [ ] หลัง Backend เสร็จสมบูรณ์ ให้ทีมพิจารณาว่าจะทำ Frontend หรือไม่; หากตกลงทำ ให้กำหนดขอบเขตและแนวทางก่อนพัฒนาหน้า Inventory/Purchase และทดสอบ flow เรียก API จริง
- [ ] เปิด Pull Request จาก branch Member 2 เข้า `develop`, สรุปสิ่งที่ทำ/ผลทดสอบ และขอ reviewer อย่างน้อย 1 คน

## จุดแบ่งงานและ commit ที่แนะนำ

ทำเครื่องหมายเมื่อแต่ละ checkpoint เสร็จและตรวจสอบแล้ว ไม่ต้องสร้าง commit เปล่าหรือแยกการเปลี่ยนแปลงย่อยเกินจำเป็น รายการเดิมแบ่งเป็น 8 checkpoints ตาม PROJECT_PLAN; checkpoint 9 เป็นงานเสริมที่พบระหว่างตรวจความครบถ้วนของ API

| สถานะ | ลำดับ | งานที่ต้องเสร็จและตรวจสอบ | ตัวอย่าง commit message |
|---|---:|---|---|
| [x] | 1 | ปรับ Inventory/StockMovement ให้สอดคล้องกัน พร้อม unit tests | `fix: synchronize inventory adjustment movements` |
| [x] | 2 | เพิ่ม PurchaseOrder/PurchaseItem, status, relationships และ repositories | `feat: add purchase persistence model` |
| [x] | 3 | เพิ่ม Purchase DTO, validation, mapper และ tests ที่เกี่ยวข้อง | `feat: add purchase request and response models` |
| [x] | 4 | ทำ create/list/get Purchase พร้อม service tests | `feat: add purchase creation and lookup` |
| [x] | 5 | ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า | `feat: manage pending purchase orders` |
| [x] | 6 | ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback | `feat: receive purchase into inventory` |
| [x] | 7 | เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests | `test: verify purchase api and backend regression` |
| [x] | 8 | อัปเดตเอกสาร API/ผลทดสอบ และตรวจความพร้อมก่อนเปิด PR | `docs: document inventory and purchase workflow` |
| [x] | 9 | เพิ่ม Stock Movement read endpoints, Inventory/Stock Movement controller tests และ Purchase REST-to-database integration test | `feat: add stock movement read APIs and inventory workflow tests` |

หลัง Backend เสร็จสมบูรณ์ ให้ทีมพิจารณา Frontend แยกต่างหาก หากตกลงทำ ให้จัด commit ตามขอบเขตงานจริง โดยไม่จำเป็นต้องนับรวมใน checkpoints ของ Backend ข้างต้น

## Definition of Done

- [x] Inventory, StockMovement และ Purchase ทำงานร่วมกันโดยไม่ทำให้ quantity กับ movement history คลาดเคลื่อนใน service flow
- [x] การรับ Purchase อัปเดต stock, movement และสถานะครบใน transaction เดียว ป้องกันการรับซ้ำ และทดสอบ rollback
- [x] Validation, business rules, error responses และ Purchase REST API ตรงกับเอกสาร
- [x] Unit/persistence tests ที่เกี่ยวข้องผ่าน และ `mvnw.cmd test` ผ่านทั้งชุด
- [x] ทำ Stock Movement GET endpoints และ Controller tests สำหรับ Inventory/Stock Movement ตาม `PROJECT_PLAN.md`
- [x] เพิ่ม HTTP-to-database integration test สำหรับ Purchase receive workflow; ตรวจสถานะ Purchase, Inventory และ Stock Movement
- [ ] Pull Request เข้า `develop` ผ่าน review; ไม่มีการ push ตรงเข้า `main`
