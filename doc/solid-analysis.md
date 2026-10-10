# การวิเคราะห์หลักการ SOLID ในระบบ Hardware Store Inventory Management System

เอกสารนี้อธิบายถึงการนำหลักการ SOLID มาประยุกต์ใช้ในโครงสร้างโค้ด Backend ของระบบ (`code/backend/hardware-store/src/main/java/com/hardwarestore/`) พร้อมยกตัวอย่างจากโค้ดจริง

## 1. Single Responsibility Principle (SRP)
**หลักการ:** คลาสควรมีหน้าที่รับผิดชอบเพียงอย่างเดียว (มีเพียงหนึ่งเหตุผลที่ทำให้ต้องแก้ไขคลาสนี้)

**ตัวอย่างในโค้ด:**
การใช้ **Chain of Responsibility Pattern** ในส่วนของการตรวจสอบข้อมูล (Validation) สำหรับคำสั่งขาย (Sales Order)
แทนที่จะมีเมธอดขนาดใหญ่ใน `SalesOrderServiceImpl` ที่ทำหน้าที่ตรวจสอบทุกอย่าง โค้ดได้ถูกแยกออกเป็นคลาสย่อยๆ ที่สืบทอดจาก `OrderValidationHandler` เช่น:
- `CustomerExistsHandler`: มีหน้าที่เดียวคือตรวจสอบว่ามีลูกค้าในระบบหรือไม่
- `ProductsExistHandler`: มีหน้าที่เดียวคือตรวจสอบว่าสินค้าที่สั่งซื้อมีอยู่ในระบบหรือไม่
- `StockAvailableHandler`: มีหน้าที่เดียวคือคำนวณและตรวจสอบว่ามีสินค้าคงคลังเพียงพอหรือไม่
การแยกคลาสแบบนี้ทำให้เมื่อมีเงื่อนไขการตรวจสอบใหม่ๆ ก็ไม่ต้องไปแก้ไขคลาสเดิม แต่เพิ่มคลาสใหม่แทน

## 2. Open/Closed Principle (OCP)
**หลักการ:** ซอฟต์แวร์ควรเปิดให้ต่อเติมขยายความสามารถได้ แต่ปิดสำหรับการแก้ไข (Open for extension, closed for modification)

**ตัวอย่างในโค้ด:**
- **Strategy Pattern** สำหรับส่วนลด: ระบบมี Interface `DiscountStrategy` และมี Implementation เช่น `BulkDiscount`, `MemberDiscount`, และ `NormalDiscount` หากในอนาคตต้องการเพิ่มโปรโมชั่นใหม่ (เช่น `SeasonalDiscount`) ก็สามารถสร้างคลาสใหม่ที่ Implement `DiscountStrategy` ได้เลยโดยไม่ต้องไปแก้ไขโค้ดของคลาสลดราคาเดิมหรือโค้ดใน Service
- **State Pattern** สำหรับสถานะคำสั่งซื้อ: Interface `OrderState` มี Implementation ต่างๆ เช่น `PendingState`, `ConfirmedState`, `ShippedState` ฯลฯ ทำให้การเพิ่มสถานะใหม่หรือเปลี่ยนกฎการเปลี่ยนสถานะทำได้โดยเพิ่มหรือแก้ที่คลาส State ที่เกี่ยวข้อง ไม่ต้องเขียน `if-else` หรือ `switch-case` ซ้อนกันมากมายใน Service

## 3. Liskov Substitution Principle (LSP)
**หลักการ:** คลาสลูกต้องสามารถนำมาใช้งานแทนคลาสแม่ได้โดยไม่ทำให้โปรแกรมทำงานผิดพลาด

**ตัวอย่างในโค้ด:**
คลาสที่ Implement `DiscountStrategy` (เช่น `BulkDiscount`) สามารถถูกเรียกใช้งานผ่าน Interface `DiscountStrategy` ได้ใน `SalesOrderServiceImpl` โดยระบบรับประกันว่ามันจะส่งคืนค่า `BigDecimal` กลับมาอย่างถูกต้องเสมอตาม Contract ที่กำหนดไว้ การส่งข้อมูลเข้าไปประมวลผล (เช่น `unitPrice`, `quantity`) จะไม่ทำให้เกิด Exception นอกเหนือจากความคาดหมาย (เช่น มีการตรวจสอบ null ใน `BulkDiscount` และส่งค่า 0 กลับ) ทำให้คลาสลูกเหล่านี้สามารถสับเปลี่ยนกันได้ 100%

## 4. Interface Segregation Principle (ISP)
**หลักการ:** Client ไม่ควรถูกบังคับให้ขึ้นต่อ Interface ที่ไม่ได้ใช้งาน (ควรแยก Interface ใหญ่ๆ ออกเป็น Interface เล็กๆ ที่เฉพาะเจาะจง)

**ตัวอย่างในโค้ด:**
- **Repository Layer**: โครงการใช้ Spring Data JPA ซึ่งมีการแยก Interface เป็นอิสระต่อกัน เช่น `ProductRepository`, `InventoryStockRepository`, และ `CustomerRepository` แทนที่จะมีคลาส Database Access ก้อนใหญ่เพียงคลาสเดียว
- **Service Layer**: แยก Interface ของ Service แต่ละโดเมนอย่างชัดเจน เช่น `ProductService`, `SalesOrderService`, `StockMovementService` ทำให้คลาส Controller ที่เรียกใช้งาน (Client) อ้างอิงเฉพาะ Interface เท่าที่จำเป็นต้องใช้เท่านั้น

## 5. Dependency Inversion Principle (DIP)
**หลักการ:** Module ระดับสูงไม่ควรขึ้นต่อ Module ระดับต่ำ แต่ควรขึ้นต่อ Abstraction (Interface/Abstract Class)

**ตัวอย่างในโค้ด:**
- Controller ของระบบ (เช่น `ProductController`) ถูก Inject ด้วย `ProductService` (ซึ่งเป็น Interface) ไม่ใช่ตัว `ProductServiceImpl` (ซึ่งเป็นคลาส Implementation)
- `SalesOrderServiceImpl` จำเป็นต้องใช้งานระบบฐานข้อมูล แต่ก็ไม่ได้ผูกติดกับคลาสการเชื่อมต่อฐานข้อมูลโดยตรง โดยจะพึ่งพา Interface `CustomerRepository`, `ProductRepository` เป็นต้น
- ตัวของ Service เองก็พึ่งพา Abstraction เช่น `DiscountStrategy` และ `OrderValidationHandler` ทำให้สามารถทำ Unit Test โดยใช้ Mock Object แทนของจริงได้ง่าย และสามารถเปลี่ยน Implementation ภายหลังได้โดยที่ Module ระดับสูง (Service/Controller) ไม่ต้องเปลี่ยนแปลงโค้ด
