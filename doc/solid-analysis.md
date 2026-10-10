# SOLID Analysis

เอกสารนี้วิเคราะห์การประยุกต์ใช้หลักการ SOLID ในโปรเจค **Hardware Store Inventory Management System**
ทุกตัวอย่างอ้างอิงไฟล์และเลขบรรทัดจริงใน code/backend/hardware-store/src/main/java/com/hardwarestore/
(path ด้านล่างย่อเป็น …/ แทน prefix นี้)

---

## ภาพรวมสถาปัตยกรรม

`
Controller  →  Service (interface)  →  Repository (Spring Data JPA)  →  Entity
   │                │
   │                ├── Mapper (MapStruct)       แปลง Entity ⇄ DTO
   │                ├── validation/ (Chain)      ตรวจสอบ Sales Order
   │                ├── strategy/ (Strategy)     คำนวณส่วนลด
   │                ├── state/ (State)           สถานะ Sales Order
   │                └── event/ + listener/       แจ้งเตือน Low Stock (Observer)
   └── exception/GlobalExceptionHandler          แปลง Exception → ErrorResponse
`

---

## 1. Single Responsibility Principle (SRP)

> แต่ละคลาสควรมีเหตุผลเดียวที่ทำให้ต้องแก้ไข

### สิ่งที่ทำได้ดี
| คลาส | ไฟล์ | หน้าที่เดียวที่รับผิดชอบ |
|------|---------------|---------------------------|
| LowStockAlertListener | …/listener/LowStockAlertListener.java | ตอบสนองต่อ LowStockEvent อย่างเดียว |
| StockAvailableHandler | …/validation/StockAvailableHandler.java | ตรวจ "สต็อกพอไหม" อย่างเดียว (แยกจากเช็คลูกค้าและสินค้า) |
| CustomerExistsHandler | …/validation/CustomerExistsHandler.java | ตรวจ "ลูกค้ามีอยู่จริงไหม" เรื่องเดียว |
| SalesOrderMapper | …/mapper/SalesOrderMapper.java | แปลง Entity <-> DTO (ปัจจุบันไม่มีลอจิกคิดราคาเจือปนแล้ว) |
| GlobalExceptionHandler | …/exception/GlobalExceptionHandler.java | ดักจับ Error รวมศูนย์และปั้น Response |

*(หมายเหตุ: ล่าสุดได้มีการ refactor ProductServiceImpl.java ยุบรวมโค้ดค้นหาซ้ำซ้อนไปรวมกันที่เมธอด getProductsPage ทำให้คลาสนี้สะอาดขึ้นมาก)*

---

## 2. Open/Closed Principle (OCP)

> เปิดให้ขยาย แต่ปิดไม่ให้แก้ของเดิม

### สิ่งที่ทำได้ดี
**2.1 Strategy — กฎส่วนลด**
- …/strategy/DiscountStrategy.java — Interface หลัก
- มี 3 แบบ: NormalDiscount, BulkDiscount, MemberDiscount
- SalesOrder.applyPricing() สามารถคำนวณส่วนลดได้โดยเรียกใช้ pply() จาก Strategy โดยไม่ต้องมีลอจิกคณิตศาสตร์ซับซ้อนในตัวมันเอง
- **หากจะเพิ่มส่วนลดใหม่:** แค่สร้างคลาสใหม่มาสืบทอด DiscountStrategy ไม่ต้องแก้ลอจิกเดิม

**2.2 Polymorphic enum — ประเภทความเคลื่อนไหวสต็อก**
- …/domain/enums/StockMovementType.java (IN, OUT, ADJUSTMENT) แต่ละค่า override process() ด้วยตัวเอง
- ใน Service สามารถเรียก equest.getMovementType().process(inventory, quantity) ตรงๆ ไม่มี if/switch เลย

**2.3 Observer — แจ้งเตือนสต็อกต่ำ**
- StockMovementServiceImpl publish LowStockEvent เฉยๆ หากวันหน้าต้องการส่ง LINE หรือ SMS ก็แค่เพิ่ม Listener ไม่ต้องแก้ Service

---

## 3. Liskov Substitution Principle (LSP)

> คลาสลูกต้องแทนที่คลาสแม่/interface ได้โดยไม่ทำให้พฤติกรรมของโปรแกรมผิดเพี้ยน

### สิ่งที่ทำได้ดี
**3.1 OrderState และ 5 implementation**
- Interface: …/state/OrderState.java
- Implementation: PendingState, ConfirmedState, ShippedState, CompletedState, CancelledState
- SalesOrder สามารถทำงานร่วมกับสถานะใดๆ ก็ได้ผ่าน getCurrentState() หากผู้ใช้พยายามข้ามขั้นการจ่ายเงิน (เช่นพยายาม ship สินค้าที่ยกเลิกไปแล้ว) คลาสลูกจะจัดการพ่น InvalidSalesOrderStateException ตามกฎหมายของตัวเองอย่างถูกต้อง

**3.2 OrderValidationHandler**
- handle() ถูกออกแบบให้เป็น inal คลาสลูกบังคับ override แค่ alidate() จึงไม่มีใครทำลายลำดับห่วงโซ่ของ Chain of Responsibility ได้

---

## 4. Interface Segregation Principle (ISP)

> client ไม่ควรถูกบังคับให้พึ่งพาเมธอดที่ตัวเองไม่ใช้

### สิ่งที่ทำได้ดี
- บริการ Service ถูกแบ่งแยกส่วนอย่างชัดเจน:
  - StockMovementService.java จัดการการปรับสต็อก (เช่น เข้า/ออก/ปรับปรุง)
  - InventoryStockService.java เน้นการตรวจสอบยอดรวม
  - SalesOrderService.java เน้นเรื่องบิลขาย
- ไม่มี StoreService ขนาดยักษ์ที่รวมทุกสรรพสิ่งเข้าไว้ด้วยกัน ทำให้ SalesOrderController หยิบไปใช้แค่ SalesOrderService ได้โดยไม่ต้องถือโค้ดพะรุงพะรัง

---

## 5. Dependency Inversion Principle (DIP)

> โมดูลระดับสูงควรพึ่งพา abstraction ไม่ใช่ concrete class

### สิ่งที่ทำได้ดี
- **Controller → Service interface:** 
  - SalesOrderController.java ประกาศตัวแปร private final SalesOrderService salesOrderService; (เป็น Interface)
- **Service → Repository interface:** 
  - Service ทุกตัวถือ Repository ที่สืบทอดมาจาก Spring Data JpaRepository ไม่ผูกติดกับฐานข้อมูลตรงๆ
- **Service → Service interface:** 
  - การทำงานข้าม Domain เช่น SalesOrderServiceImpl จะเรียก StockMovementService (Interface) ไม่ใช่ไปเรียก Impl ตรงๆ
- **Constructor Injection:** 
  - ใช้งานผ่าน @RequiredArgsConstructor (Lombok) ฉีด Dependency อัตโนมัติ ปลอดภัยและเหมาะกับการเขียน Unit Test
