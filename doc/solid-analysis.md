# SOLID Analysis — Hardware Store Inventory Management System

path ของไฟล์ทั้งหมดอ้างอิงจาก `code/backend/hardware-store/src/main/java/com/hardwarestore/`

| หลักการ | ไฟล์ : บรรทัด | เหตุผลสั้นๆ |
|---|---|---|
| **S** — Single Responsibility | `validation/CustomerExistsHandler.java:7` | ตรวจอย่างเดียวว่าลูกค้ามีอยู่จริง |
| | `validation/ProductsExistHandler.java:12` | ตรวจอย่างเดียวว่าสินค้ามีอยู่จริง |
| | `validation/StockAvailableHandler.java:16-53` | ตรวจอย่างเดียวว่าสต็อกพอ |
| | `mapper/SalesOrderMapper.java:19-69` | แปลง DTO ↔ Entity อย่างเดียว ไม่มี business rule อื่น |
| | `exception/GlobalExceptionHandler.java:18` | รวมการแปลง exception เป็น HTTP response ไว้ที่เดียว Controller ไม่ต้อง try/catch |
| **O** — Open/Closed | `strategy/DiscountStrategy.java:5-7`, `domain/entity/SalesOrder.java:186-192` | `calculateTotal()` เรียก `strategy.apply(...)` โดยไม่เช็กชนิดส่วนลด เพิ่มส่วนลดชนิดใหม่ด้วยการเพิ่มคลาสที่ implement `DiscountStrategy` (จุดเลือก strategy คือ `SalesOrder.java:164-179`) |
| | `domain/enums/StockMovementType.java` (ทั้งไฟล์), `service/impl/StockMovementServiceImpl.java:66` | แต่ละชนิด (`IN`/`OUT`/`ADJUSTMENT`) มี `process()` ของตัวเอง Service เรียก `getMovementType().process(...)` โดยไม่ใช้ if-else แยกตามชนิด |
| | `state/OrderState.java:6-16` | เพิ่มพฤติกรรมของสถานะใหม่ด้วยคลาส State ใหม่ ไม่ต้องแก้ if-else ใน Service |
| **L** — Liskov Substitution | `strategy/NormalDiscount.java:9-14`, `MemberDiscount.java:11-17`, `BulkDiscount.java:11-22` | ทุกตัวรับ `(unitPrice, quantity)` คืน `BigDecimal` ทศนิยม 2 ตำแหน่ง และจัดการ `unitPrice == null` เหมือนกัน (คืน 0) `SalesOrder.java:192` เรียกผ่าน interface ได้โดยไม่รู้ชนิดจริง |
| | `state/PendingState.java:7`, `ConfirmedState.java:7`, `ShippedState.java:7`, `CompletedState.java:7`, `CancelledState.java:7` | ทั้ง 5 คลาส implement ครบทั้ง 4 เมธอด การทำรายการที่ไม่อนุญาตจะโยน `InvalidSalesOrderStateException` ชนิดเดียวกันเสมอ ไม่มีคลาสไหนโยน `UnsupportedOperationException` |
| | `validation/OrderValidationHandler.java:17-25` | `handle()` เป็น `final` และเรียก `validate()` ที่ subclass ทั้ง 3 ตัว override ใช้แทนกันในสายได้ |
| **I** — Interface Segregation | `service/DashboardService.java` (ทั้งไฟล์ 6 บรรทัด), `service/InventoryStockService.java` (ทั้งไฟล์ 11 บรรทัด) | Service แยก interface ตามโดเมน แต่ละตัวมีเฉพาะเมธอดของโดเมนนั้น |
| | `strategy/DiscountStrategy.java:5-7` | interface มีเมธอดเดียว implementation ไม่ต้องมีเมธอดที่ไม่ใช้ |
| | `repository/ProductRepository.java:8-17` | แยก Repository ตาม Entity (9 ตัวใน `repository/`) และเพิ่มเฉพาะ query ที่ Entity นั้นใช้ |
| **D** — Dependency Inversion | `controller/api/ProductController.java:19` | Controller ถือ `ProductService` (interface) ไม่ใช่ `ProductServiceImpl` |
| | `service/impl/SalesOrderServiceImpl.java:34-43` | Service ถือ `SalesOrderRepository`, `CustomerRepository`, `ProductRepository`, `InventoryStockRepository` (interface) และ `StockMovementService` (interface) |
| | `service/impl/SalesOrderServiceImpl.java:35` และ `StockMovementServiceImpl.java:27` | Constructor Injection ด้วย `@RequiredArgsConstructor` + `final` field ไม่มี `@Autowired` ทั้งโปรเจค |
