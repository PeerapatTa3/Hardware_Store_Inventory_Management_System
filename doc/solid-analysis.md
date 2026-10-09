# SOLID Analysis

เอกสารนี้วิเคราะห์การประยุกต์ใช้หลักการ SOLID ในโปรเจค **Hardware Store Inventory Management System**
ทุกตัวอย่างอ้างอิงไฟล์และเลขบรรทัดจริงใน `code/backend/hardware-store/src/main/java/com/hardwarestore/`
(path ด้านล่างย่อเป็น `…/` แทน prefix นี้)

แต่ละหลักการแบ่งเป็น **สิ่งที่ทำได้ดี** และ **ข้อจำกัดที่ยังเหลือ** เพื่อให้เห็นภาพตามจริง

---

## ภาพรวมสถาปัตยกรรม

```
Controller  →  Service (interface)  →  Repository (Spring Data JPA)  →  Entity
   │                │
   │                ├── Mapper (MapStruct)       แปลง Entity ⇄ DTO
   │                ├── validation/ (Chain)      ตรวจสอบ Sales Order
   │                ├── strategy/ (Strategy)     คำนวณส่วนลด
   │                ├── state/ (State)           สถานะ Sales Order
   │                └── event/ + listener/       แจ้งเตือน Low Stock (Observer)
   └── exception/GlobalExceptionHandler          แปลง Exception → ErrorResponse
```

---

## 1. Single Responsibility Principle (SRP)

> แต่ละคลาสควรมีเหตุผลเดียวที่ทำให้ต้องแก้ไข

### สิ่งที่ทำได้ดี

| คลาส | ไฟล์ : บรรทัด | หน้าที่เดียวที่รับผิดชอบ |
|------|---------------|---------------------------|
| `ProductController` | `…/controller/api/ProductController.java` : 14-76 | รับ HTTP request, ตรวจ `@Valid`, ส่งต่อให้ `ProductService` — ไม่มี business logic |
| `SalesOrderMapper` | `…/mapper/SalesOrderMapper.java` : 20-75 | แปลง `SalesOrderRequest` ⇄ `SalesOrder` ⇄ `SalesOrderResponse` |
| `StockMovementServiceImpl.create` | `…/service/impl/StockMovementServiceImpl.java` : 34-69 | บันทึกความเคลื่อนไหวสต็อก; การแจ้งเตือนแยกไปที่ `publishLowStockEventIfNeeded` (71-81) และ `LowStockAlertListener` |
| `CustomerExistsHandler` | `…/validation/CustomerExistsHandler.java` : 7-26 | ตรวจ "ลูกค้ามีอยู่จริงไหม" เรื่องเดียว |
| `ProductsExistHandler` | `…/validation/ProductsExistHandler.java` : 12-34 | ตรวจ "สินค้าทุกชิ้นมีอยู่จริงไหม" เรื่องเดียว |
| `StockAvailableHandler` | `…/validation/StockAvailableHandler.java` : 16-54 | ตรวจ "สต็อกพอไหม" เรื่องเดียว |
| `GlobalExceptionHandler` | `…/exception/GlobalExceptionHandler.java` : 14-77 | แปลง Exception ทั้งระบบเป็น `ErrorResponse` รูปแบบเดียวกัน |
| `LowStockAlertListener` | `…/listener/LowStockAlertListener.java` : 13-23 | ตอบสนองต่อ `LowStockEvent` อย่างเดียว |

### ข้อจำกัดที่ยังเหลือ
- `SalesOrderServiceImpl` (`…/service/impl/SalesOrderServiceImpl.java` : 40-228) ทำหลายอย่างในคลาสเดียว: orchestration การสร้าง/แก้ order, ตรวจเงื่อนไขการเปลี่ยนสถานะ (สวิตช์บรรทัด 114-157) และสร้าง `StockMovementRequest` 3 แบบ (บรรทัด 174-227) — ถ้าจะแยกเพิ่ม ควรย้ายส่วนตัดสต็อกไปเป็นคลาสเฉพาะ
- `ProductServiceImpl` มีโค้ดเลือก query ตาม keyword/categoryId ซ้ำกันสองที่ — `findAll` (51-69) กับ `findAllAdmin` (73-88)

---

## 2. Open/Closed Principle (OCP)

> เปิดให้ขยาย แต่ปิดไม่ให้แก้ของเดิม

### สิ่งที่ทำได้ดี

**2.1 Strategy — กฎส่วนลด**
- `…/strategy/DiscountStrategy.java` : 5-7 — interface เมธอดเดียว `apply(unitPrice, quantity)`
- `NormalDiscount` (6-15), `BulkDiscount` (6-23, ลด 10% เมื่อซื้อตั้งแต่ 10 ชิ้น), `MemberDiscount` (6-18, ลด 10% สำหรับสมาชิก)
- `SalesOrder.calculateTotal()` (`…/domain/entity/SalesOrder.java` : 181-199) เรียก `strategy.apply(...)` โดยไม่รู้ว่าเป็นกฎอะไร

**2.2 Polymorphic enum — ประเภทความเคลื่อนไหวสต็อก**
- `…/domain/enums/StockMovementType.java` : 5-39 — `IN`, `OUT`, `ADJUSTMENT` แต่ละค่า override `process(inventory, quantity)` เอง
- `StockMovementServiceImpl.java` : 54 เรียก `request.getMovementType().process(inventory, quantity)` ตรงๆ ไม่มี `if/switch` — เพิ่มประเภทใหม่ได้โดยไม่แก้ service

**2.3 Chain of Responsibility — การตรวจสอบ order**
- `…/validation/OrderValidationHandler.java` : 7-26 — เพิ่ม handler ใหม่ (เช่น ตรวจวงเงินเครดิต) ได้โดยสร้างคลาสใหม่แล้วต่อใน `buildValidationChain()` (`SalesOrderServiceImpl.java` : 166-172)

**2.4 Observer — แจ้งเตือนสต็อกต่ำ**
- `StockMovementServiceImpl.java` : 71-81 publish `LowStockEvent` โดยไม่รู้ว่าใครฟังอยู่ — เพิ่ม listener ส่งอีเมล/LINE ได้โดยไม่แก้ service (ตามที่ comment ใน `LowStockAlertListener.java` : 8-12 ระบุไว้)

### ข้อจำกัดที่ยังเหลือ (ยังไม่ OCP เต็มที่)
- `SalesOrder.resolveDiscountStrategy()` (`SalesOrder.java` : 164-179) เลือก strategy ด้วย `if/else` และ `new MemberDiscount()/new BulkDiscount()` เอง — เพิ่มส่วนลดชนิดใหม่ต้องแก้เมธอดนี้ด้วย
- `SalesOrder.createState()` (205-217) และ `SalesOrderServiceImpl.updateStatus` (114-157) เป็น `switch` ตาม enum — เพิ่มสถานะใหม่ต้องแก้ทั้งสองที่

---

## 3. Liskov Substitution Principle (LSP)

> คลาสลูกต้องแทนที่คลาสแม่/interface ได้โดยไม่ทำให้พฤติกรรมของโปรแกรมผิดเพี้ยน

### สิ่งที่ทำได้ดี

**3.1 `OrderState` และ 5 implementation**
- Interface: `…/state/OrderState.java` : 6-16 (`confirm`, `cancel`, `ship`, `complete`, `getStatus`)
- Implementation: `PendingState`, `ConfirmedState`, `ShippedState`, `CompletedState`, `CancelledState`
- ทุก implementation ทำตามสัญญาเดียวกัน คือ "ทำ transition ที่ถูกต้อง หรือโยน `InvalidSalesOrderStateException`" เช่น
  - `PendingState.ship()` (`PendingState.java` : 21-23) โยน exception
  - `ConfirmedState.confirm()` (`ConfirmedState.java` : 9-11) โยน exception
- `SalesOrder.confirm()/cancel()/ship()/complete()` (`SalesOrder.java` : 114-128) เรียกผ่าน `getCurrentState()` โดยไม่ต้องรู้ว่าเป็นสถานะไหน

**3.2 `DiscountStrategy`**
- ทั้ง 3 implementation รับ `unitPrice == null` แล้วคืน `BigDecimal.ZERO` และคืนค่า scale 2 / `HALF_UP` เหมือนกัน (`NormalDiscount.java` : 9-14, `BulkDiscount.java` : 12-22, `MemberDiscount.java` : 11-17) จึงสลับใช้แทนกันได้ใน `calculateTotal()`

**3.3 `OrderValidationHandler`**
- `handle()` เป็น `final` (บรรทัด 17) ส่วน subclass override เฉพาะ `validate()` — subclass จึงทำลายลำดับการส่งต่อใน chain ไม่ได้

### ข้อจำกัดที่ยังเหลือ
- `SalesOrderStatus` (`…/domain/enums/SalesOrderStatus.java` : 6-38) มีเมธอด `apply(SalesOrder)` ซึ่ง `SalesOrderServiceImpl` ไม่ได้เรียกใช้ (ใช้ `switch` เองที่บรรทัด 114) ทำให้มีสองเส้นทางสำหรับ transition เดียวกัน อาจมีพฤติกรรมไม่ตรงกันเมื่อมีคนแก้ฝั่งใดฝั่งหนึ่ง

---

## 4. Interface Segregation Principle (ISP)

> client ไม่ควรถูกบังคับให้พึ่งพาเมธอดที่ตัวเองไม่ใช้

### สิ่งที่ทำได้ดี
- Interface ถูกแยกตาม domain และมีเมธอดน้อย ตรงกับสิ่งที่ controller ใช้จริง:

| Interface | ไฟล์ | จำนวนเมธอด |
|-----------|------|-------------|
| `StockMovementService` | `…/service/StockMovementService.java` : 9-13 | 3 |
| `InventoryStockService` | `…/service/InventoryStockService.java` : 8-10 | 2 |
| `PurchaseOrderService` | `…/service/PurchaseOrderService.java` : 10-18 | 5 (รวม `receive`) |
| `SalesOrderService` | `…/service/SalesOrderService.java` : 11-19 | 5 (รวม `updateStatus`) |
| `DiscountStrategy` | `…/strategy/DiscountStrategy.java` : 6 | 1 |

- `PurchaseOrderService` และ `SalesOrderService` **ไม่มี** `delete` เพราะธุรกิจไม่อนุญาตให้ลบ order — ไม่ได้บังคับให้ implementation ต้องมีเมธอดที่ไม่ใช้
- `InventoryStockService` ไม่ได้ใช้ช่องทางเขียนสต็อกของตัวเอง แต่เรียก `StockMovementService.create` (`InventoryStockServiceImpl.java` : 42-48) เพื่อให้ทุกการเปลี่ยนสต็อกผ่านจุดเดียว
- DTO แยก 2 ระดับตามผู้ใช้: `ProductResponse` (ไม่มี `costPrice`) กับ `ProductAdminResponse` (มี `costPrice`) — client ทั่วไปไม่ถูกบังคับให้รับข้อมูลต้นทุน

### ข้อจำกัดที่ยังเหลือ
- `ProductService` มี 7 เมธอดและมีคู่เมธอดซ้ำ (`findAll`/`findAllAdmin`, `findById`/`findByIdAdmin`) — ถ้าจะ segregate เพิ่มอาจแยกเป็น `ProductQueryService` กับ `ProductCommandService`

---

## 5. Dependency Inversion Principle (DIP)

> โมดูลระดับสูงควรพึ่งพา abstraction ไม่ใช่ concrete class

### สิ่งที่ทำได้ดี
- **Controller → Service interface:** `SalesOrderController.java` : 19 ถือ `SalesOrderService` (interface) ไม่ใช่ `SalesOrderServiceImpl`; ทุก controller ใน `…/controller/api/` ทำแบบเดียวกัน
- **Service → Repository interface:** `SalesOrderServiceImpl.java` : 42-47 ถือ `SalesOrderRepository`, `CustomerRepository`, `ProductRepository`, `InventoryStockRepository` ซึ่งเป็น interface ของ Spring Data
- **Service → Service interface:** `SalesOrderServiceImpl` และ `PurchaseOrderServiceImpl` (บรรทัด 33-37) เรียก `StockMovementService` (interface) แทนที่จะแตะ `InventoryStockRepository` เอง
- **Constructor Injection ทั้งโปรเจค:** `@RequiredArgsConstructor` + field `final` (ไม่มี `@Autowired` บน field)
- **Publisher → Event:** `StockMovementServiceImpl.java` : 32 พึ่งพา `ApplicationEventPublisher` ไม่ผูกกับ listener ตัวใด

### ข้อจำกัดที่ยังเหลือ
- `SalesOrderServiceImpl.buildValidationChain()` (บรรทัด 167-172) `new` handler ที่เป็น concrete class เอง ไม่ได้ให้ Spring inject — ทดสอบแยกชิ้นได้ยากกว่า
- `SalesOrder.resolveDiscountStrategy()` (`SalesOrder.java` : 164-179) `new` strategy เองใน entity
- `SalesOrderMapper` เป็น `abstract class` (ไม่ใช่ interface เหมือน mapper อื่น) เพราะมี logic การคำนวณราคาใน `updatePendingOrder` (บรรทัด 31-66)

---

## สรุปคะแนนรายหลักการ

| หลักการ | สถานะ | หมายเหตุ |
|---------|-------|----------|
| SRP | ดี | Controller/Mapper/Handler/Listener แยกหน้าที่ชัด; `SalesOrderServiceImpl` ใหญ่ที่สุด |
| OCP | ดี (บางจุดยัง `switch`) | Strategy, polymorphic enum, Chain, Observer ขยายได้; การเลือก strategy/state ยังเป็น `switch` |
| LSP | ดี | State/Strategy/Handler แทนที่กันได้; มีเส้นทาง transition ซ้ำใน `SalesOrderStatus.apply` |
| ISP | ดี | Interface เล็กตาม domain; `ProductService` เริ่มใหญ่ |
| DIP | ดี | พึ่ง interface ตลอดสาย; handler/strategy ยัง `new` เอง |



