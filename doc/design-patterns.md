# Design Patterns

## 1. Layered Architecture
โครงการนี้ใช้สถาปัตยกรรมแบบชั้น ๆ (Layered Architecture) คลาสสิก ได้แก่:
- `controller`: จัดการ HTTP request และความรับผิดชอบด้านการตรวจสอบข้อมูลที่ขอบเขต API
- `service`: ประกอบด้วยกฎทางธุรกิจและการประสานงาน transaction
- `repository`: ให้การเข้าถึงข้อมูลผ่าน Spring Data JPA
- `domain`: ประกอบด้วย entity และ logic ทางธุรกิจ
- `dto`: กำหนด payload ของ request/response สำหรับ API
- `mapper`: แปลงข้อมูลระหว่าง DTO และ entity

การแยกชั้นนี้ทำให้ระบบง่ายต่อการทดสอบ บำรุงรักษา และขยายฟีเจอร์ในอนาคต

## 2. Repository Pattern
Repository เช่น `CategoryRepository`, `SupplierRepository`, `ProductRepository`, และ `CustomerRepository` ถูกกำหนดเป็น interface ของ Spring Data Service layer ขึ้นกับ repository interface แทนที่จะผูกติดกับ internals ของ JPA โดยตรง

รูปแบบนี้มีประโยชน์ดังนี้:
- การซ่อนรายละเอียดการเก็บข้อมูล
- Logic ของ service ที่สะอาดขึ้น
- การ mock และทดสอบหน่วยที่ง่ายขึ้น
- การเข้าถึง CRUD และ query แบบกำหนดเองที่สอดคล้องกัน

## 3. DTO Pattern และ Mapper Layer
โครงการแยก model จากฐานข้อมูลออกจากสัญญา API ภายนอก โดยใช้ request DTO และ response DTO ร่วมกับ mapper class

ตัวอย่างที่เห็นได้ชัด ได้แก่:
- `CategoryRequest` / `CategoryResponse`
- `SupplierRequest` / `SupplierResponse`
- `ProductRequest` / `ProductResponse`
- `CustomerRequest` / `CustomerResponse`

Mapper layer ช่วยลดการรั่วไหลของความรู้เรื่องฐานข้อมูล และทำให้สามารถบังคับกฎการตรวจสอบข้อมูลที่ขอบเขต API ก่อนเรียก logic ของ domain

## 4. Dependency Injection
แอปพลิเคชันใช้ Spring ในรูปแบบ constructor-based dependency injection ซึ่งเป็นแนวทางหลักของ Spring Boot และส่งเสริมคุณลักษณะดังนี้:
- coupling ระหว่างคลาสต่ำลง
- การทดสอบหน่วยทำได้ง่ายขึ้น
- การประกอบอ็อบเจกต์ใน runtime เป็นระเบียบมากขึ้น

Controller, service, และ mapper จะถูกจัดเรียงโดย Spring IoC container ทำให้ลดความจำเป็นในการผูกอ็อบเจกต์ด้วยตนเอง

## 5. State Pattern ใน Sales Order
Entity `SalesOrder` ใช้ State Pattern ผ่าน `OrderState` และ state ที่เป็น concrete เช่น:
- `PendingState`
- `ConfirmedState`
- `CompletedState`
- `CancelledState`

การเปลี่ยนสถานะของคำสั่งซื้อถูกควบคุมผ่านเมธอด เช่น:
- `confirm()`
- `cancel()`
- `complete()`

การออกแบบนี้ทำให้พฤติกรรมวงจรชีวิตที่ถูกต้องถูกรวมไว้ที่จุดเดียว และป้องกันไม่ให้การเปลี่ยนสถานะที่ไม่ถูกต้องกระจายไปตาม service หลายตัว

## 6. Strategy Pattern สำหรับ Logic ราคา
`SalesOrder` ยังใช้ abstraction สำหรับ pricing strategy ได้แก่:
- `DiscountStrategy`
- `NormalDiscount`

สิ่งนี้ทำให้พฤติกรรมด้านราคาเปลี่ยนได้โดยไม่ต้องแก้ไข entity ของคำสั่งซื้อเอง รูปแบบนี้มีประโยชน์มากเมื่อมีความต้องการเพิ่มโมเดลราคาหรือโปรโมชั่นในอนาคต

## 7. Transactional Business Logic
กระบวนการซื้อสินค้าและสต็อกถูกออกแบบให้ทำงานแบบ transactional เป็นพิเศษสำหรับ:
- รับสินค้าเข้าคลังจากซัพพลายเออร์
- อัปเดตจำนวนสต็อก
- บันทึกประวัติการเคลื่อนไหวสินค้า
- ทำการอัปเดตสถานะคำสั่งซื้อพร้อมกันหรือ rollback เมื่อมีความล้มเหลว

การออกแบบนี้ช่วยรักษาความสอดคล้องของสต็อกในคลัง แม้ว่าแต่ละขั้นตอนจะล้มเหลวก็ตาม

## สรุป
รูปแบบการออกแบบที่ใช้ในโครงการนี้ให้พื้นฐานที่ดีสำหรับระบบจัดการคลังแบบ enterprise ความสถาปัตยกรรมมีการแยกส่วนที่ชัดเจน กฎทางธุรกิจถูกแยกจากสัญญา API และ domain model มีการใช้ state และ strategy pattern ที่สามารถขยายได้อย่างราบรื่นตามการเติบโตของระบบ
