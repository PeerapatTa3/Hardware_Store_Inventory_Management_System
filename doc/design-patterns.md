# Design Patterns in Hardware Store Inventory Management System (รูปแบบการออกแบบในระบบจัดการคลังสินค้าฮาร์ดแวร์)

เอกสารฉบับนี้อธิบายถึง Software Design Patterns ที่นำมาประยุกต์ใช้ในโครงสร้างสถาปัตยกรรม Backend ของระบบ Hardware Store Inventory Management System โดยการนำรูปแบบเหล่านี้มาใช้มีจุดประสงค์เพื่อให้โค้ดสามารถดูแลรักษาได้ง่าย (Maintainability) มีความยืดหยุ่น (Flexibility) และสอดคล้องกับหลักการ SOLID

## 1. State Pattern
**Package:** `com.hardwarestore.state` / `com.hardwarestore.domain.state`

**จุดประสงค์:** จัดการ Lifecycle และการเปลี่ยนสถานะ (State transitions) ของ `SalesOrder` โดยหลีกเลี่ยงการใช้เงื่อนไขตรวจสอบที่ซับซ้อน (เช่น if-else หรือ switch-case ขนาดใหญ่)

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- **`OrderState` (Interface):** กำหนดพฤติกรรมเฉพาะสำหรับแต่ละสถานะ ได้แก่ `confirm`, `cancel`, `ship`, และ `complete`
- **Concrete States (สถานะที่เป็นรูปธรรม):** 
  - `PendingState` (รอดำเนินการ)
  - `ConfirmedState` (ยืนยันแล้ว)
  - `ShippedState` (จัดส่งแล้ว)
  - `CompletedState` (เสร็จสมบูรณ์)
  - `CancelledState` (ยกเลิก)
- **บริบท (Context):** Entity `SalesOrder` จะเก็บ Reference ของ `OrderState` ปัจจุบันไว้ และส่งมอบ (Delegate) การกระทำต่างๆ ไปยัง Object ของสถานะนั้นๆ หากมีความพยายามที่จะเปลี่ยนสถานะแบบไม่ถูกต้อง (เช่น การจัดส่งออเดอร์ที่ถูกยกเลิกไปแล้ว) ระบบจะทำการโยน Exception ตามที่ถูกกำหนดไว้ใน Object ของสถานะนั้นๆ

## 2. Strategy Pattern
**Package:** `com.hardwarestore.strategy`

**จุดประสงค์:** ห่อหุ้ม (Encapsulate) อัลกอริทึมในการคำนวณราคาและส่วนลดที่แตกต่างกัน ทำให้สามารถสลับการใช้งานได้ง่ายขึ้นอยู่กับประเภทของลูกค้าหรือรายละเอียดของคำสั่งซื้อ

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- **`DiscountStrategy` (Interface):** กำหนด Contract พื้นฐานสำหรับการคำนวณส่วนลด ผ่านเมธอด `BigDecimal apply(BigDecimal unitPrice, int quantity)`
- **Concrete Strategies:**
  - `NormalDiscount`: การคิดราคามาตรฐานที่ไม่มีการหักส่วนลดพิเศษ
  - `BulkDiscount`: ใช้ส่วนลดเป็นเปอร์เซ็นต์เมื่อจำนวนสินค้าที่สั่งซื้อถึงเกณฑ์ที่กำหนด
  - `MemberDiscount`: ใช้ส่วนลดคงที่ หรือเป็นเปอร์เซ็นต์สำหรับลูกค้าที่เป็นสมาชิกของร้าน
- **บริบท (Context):** กลยุทธ์ (Strategies) เหล่านี้จะถูกกำหนดและเรียกใช้งานแบบไดนามิกภายใน Entity `SalesOrder` หรือ Service ด้านการตั้งราคา เพื่อคำนวณยอดรวมสุทธิของออเดอร์

## 3. Chain of Responsibility Pattern
**Package:** `com.hardwarestore.validation`

**จุดประสงค์:** ลดการยึดติด (Decouple) ของลอจิกการตรวจสอบคำสั่งซื้อ (Order Validation) โดยแบ่งออกเป็นลำดับขั้นตอนย่อยๆ ที่ทำงานแยกส่วนกัน 

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- **`OrderValidationHandler` (Abstract Base Class):** เก็บ Reference ของ Handler ตัวถัดไป (`next`) ในห่วงโซ่ โดยมีเมธอด `setNext()` สำหรับการตั้งค่าให้ร้อยเรียงต่อกันได้อย่างเป็นธรรมชาติ (Fluent Configuration) และมีเมธอด `handle(OrderValidationContext context)` ซึ่งจะเรียกทำงานเมธอด `validate()` ก่อนที่จะส่งต่อไปยัง Handler ตัวถัดไป
- **Concrete Handlers:**
  - `CustomerExistsHandler`: ตรวจสอบว่ารหัสลูกค้า (Customer ID) ที่เชื่อมโยงกับคำสั่งซื้อนั้นมีอยู่ในระบบจริง
  - `ProductsExistHandler`: ตรวจสอบว่าสินค้าทั้งหมดที่ร้องขอมีอยู่ในแคตตาล็อก
  - `StockAvailableHandler`: ยืนยันว่ามีสินค้าคงคลังเพียงพอที่จะตอบสนองจำนวนที่สั่งซื้อ
- **บริบท (Context):** เมื่อมีคำขอสั่งซื้อใหม่เข้ามา ตัว `OrderValidationContext` จะถูกส่งผ่านห่วงโซ่ที่ถูกกำหนดไว้ล่วงหน้า หาก Handler ใดๆ ทำงานล้มเหลว มันจะตัดวงจรห่วงโซ่นี้โดยการโยน Validation Exception ออกมา เพื่อให้มั่นใจว่าออเดอร์ที่ไม่ถูกต้องจะถูกปฏิเสธตั้งแต่เนิ่นๆ

## 4. Mapper (Data Transfer Object) Pattern
**Package:** `com.hardwarestore.mapper`

**จุดประสงค์:** แยก (Isolate) ตัว Domain Entities ภายใน ออกจากชั้น API ภายนอก โดยการแปลงข้อมูลไปมาระหว่าง Entities กับ Data Transfer Objects (DTOs)

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- นำ **MapStruct** มาประยุกต์ใช้งาน ผ่าน Interface ต่างๆ เช่น `ProductMapper`, `SalesOrderMapper`, และ `PurchaseOrderMapper` เพื่อกำหนดกฎเกณฑ์ในการทำ Mapping
- MapStruct ทำหน้าที่เป็น Code Generator ในช่วง Compile-time โดยจะสร้างคลาส Implementation สำหรับแปลงฟิลด์ข้อมูล (ตัวอย่างเช่น การแปลง Object `Category` ที่อยู่ใน Entity ไปเป็น `categoryId` บน Response DTO)

## 5. Dependency Injection / Inversion of Control
**Scope:** ใช้งานครอบคลุมทั้งแอปพลิเคชัน (`com.hardwarestore.*`)

**จุดประสงค์:** ส่งเสริมให้เกิด Loose Coupling และทำให้การทดสอบ (Testing) ง่ายขึ้น โดยมอบหมายหน้าที่จัดการ Lifecycle ของ Object และ Dependencies ให้กับ Spring IoC Container

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- มีการใช้งาน Spring Stereotypes มาตรฐาน (เช่น `@Service`, `@Repository`, `@RestController`, `@Component`) อย่างแพร่หลาย
- คลาสฝั่ง Service (เช่น `ProductServiceImpl`, `SalesOrderServiceImpl`) จะขึ้นตรงต่อ Abstractions (เช่น `ProductRepository`, `SalesOrderRepository`) ซึ่งถูกฉีด (Injected) เข้ามาผ่าน Constructor วิธีนี้ช่วยหลีกเลี่ยงการ Hard-code ค่า Dependencies และอำนวยความสะดวกในการทำ Unit Testing ผ่านการใช้ Mock

## 6. Facade Pattern
**Package:** `com.hardwarestore.service.impl`

**จุดประสงค์:** ซ่อนความซับซ้อนของระบบย่อย (Subsystems) ไว้หลัง Interface เดียวที่ใช้งานง่าย (ในที่นี้คือลดภาระจากหน้าเว็บที่จะต้องเรียก API หลายๆ เส้นเพื่อดึงข้อมูลสรุปยอด)

**รายละเอียดการนำไปใช้งาน (Implementation Details):**
- **`DashboardServiceImpl`:** ทำหน้าที่เป็น Facade ชั้นดี โดยการฉีด (Inject) Repository หลายๆ ตัวพร้อมกัน ได้แก่ `ProductRepository`, `InventoryStockRepository`, `PurchaseOrderRepository`, `SalesOrderRepository`, `SupplierRepository`, และ `CustomerRepository`
- **การทำงาน:** หน้าเว็บจะเรียกมาที่เมธอด `getDashboardSummary()` เพียงครั้งเดียว จากนั้นคลาสนี้จะไปทำการดึงข้อมูลและคำนวณสรุปผลจากทุกๆ Repository มารวมเป็น `DashboardSummaryResponse` ก้อนเดียวคืนให้ ทำให้ Client (หน้าเว็บ React) ไม่ต้องรู้รายละเอียดหรือยิง API เพื่อดึงข้อมูลแยกส่วนหลายๆ รอบ (แก้ปัญหา N+1 Query ทางฝั่ง Client ได้อย่างมีประสิทธิภาพ)
