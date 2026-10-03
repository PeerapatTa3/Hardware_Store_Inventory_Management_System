# การแบ่งหน้าที่และสรุปงานตามใบงานโปรเจค

## 1. รายชื่อสมาชิกและหน้าที่รับผิดชอบ

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---|---|---:|---:|---|---|
| 1 | นายพิสิษฐ์ ทรัพย์อุดมโชติ | 673380285-2 | 01 | `peerapol_673380287-8_01` | Member 1: Backend core, Category, Supplier, Product, Inventory, Stock Movement |
| 2 | นายพีรพล แก้วเจริญสันติสุข | 673380287-8 | 01 | `peerapol_673380287-8_01` | Member 2: Inventory, Purchase, Stock Operations |
| 3 | นายพีรพัฒน์ แท่นประยุทร | 673380288-6 | 01 | `peerapol_673380287-8_01` | Member 3: Customer, Sales Order, Design Pattern, Documentation |

## 2. สรุปสถานะโปรเจคปัจจุบัน

ปัจจุบันระบบ backend หลักของโปรเจคมีความคืบหน้ามากและผ่านการทดสอบจริงแล้วด้วย Maven

- Backend framework: Spring Boot 3.x + Java 17
- Build tool: Maven
- Database: H2 สำหรับ development
- API: RESTful API + Swagger/OpenAPI
- Testing: JUnit 5 + Mockito
- ผลการตรวจสอบล่าสุด: 15 tests, 0 failures, 0 errors, BUILD SUCCESS

## 3. งานที่ Member 1 ทำเสร็จแล้ว

### 3.1 Project foundation
- สร้างโครงสร้างโปรเจค Spring Boot
- ตั้งค่า Maven, application configuration
- เปิดใช้งาน Swagger/OpenAPI
- สร้าง GlobalExceptionHandler และ exception classes

### 3.2 Core modules
- Category
- Supplier
- Product

### 3.3 Inventory modules
- InventoryStock
- StockMovement

### 3.4 Layered architecture compliance
- แยก controller, service, repository, domain, dto, mapper, exception เป็นชัดเจน
- ใช้ dependency injection แบบ constructor injection
- ไม่ให้ controller เรียก repository โดยตรง

### 3.5 ปัจจุบัน
Member 1 อยู่ในสถานะที่ “ทำงานหลักของระบบ backend ครบแล้วและพร้อมต่อยอด” สำหรับส่วนที่เหลือของโปรเจคตามใบงาน

## 4. งานที่ Member 2 ควรทำต่อ

### 4.1 Scope ที่กำหนด
- Inventory / Stock management
- Purchase workflow
- การจัดการสต็อกและธุรกรรมคลัง
- ตรวจสอบ flow รับ-จ่ายสินค้า

### 4.2 งานที่ทำไปแล้วในหลักการ
- InventoryStock entity และ logic
- StockMovement entity และ enum
- API สำหรับตรวจสอบและปรับสต็อก
- Service สำหรับระบบเคลื่อนไหวสต็อก

### 4.3 สิ่งที่ยังต้องทำต่อ
- Purchase module
- PurchaseOrder / PurchaseOrderItem (ถ้ามีตามความต้องการ)
- Flow รับสินค้าเข้าคลังจริง
- ตรวจสอบ business rule ของคลังสินค้าอย่างละเอียด

## 5. งานที่ Member 3 ควรทำต่อ

### 5.1 Scope ที่กำหนด
- Customer
- Sales Order
- Sales Order Item
- Design patterns
- Analysis และ documentation

### 5.2 งานที่ทำไปแล้ว
- สร้างเอกสาร SOLID analysis
- สร้างเอกสาร design patterns
- สร้างเอกสาร API documentation
- วิเคราะห์ project structure และ design rationale

### 5.3 สิ่งที่ยังต้องทำต่อ
- Customer entity และ CRUD
- Sales Order module
- Sales Order Item module
- Diagram สำหรับ Use Case, Class Diagram, Sequence Diagram, ER Diagram
- สร้างเอกสารและ slide ส่งงานให้ครบตามใบงาน

## 6. การเชื่อมโยงกับเกณฑ์ใบงาน

### 6.1 Layered Architecture
- แบ่ง layer ตาม controller / service / repository / domain / dto / mapper / config / exception
- การทำงานแต่ละคนควรพัฒนาตาม layer ที่กำหนด

### 6.2 SOLID Principles
- Member 1 ทำหน้าที่ main backend core และต้องคงหลัก SRP, DIP, OCP ในโครงสร้าง service
- Member 3 ทำงานด้าน design และ documentation เพื่ออธิบายหลัก SOLID และ pattern ที่ใช้

### 6.3 Design Patterns
- Layered Architecture, MVC, Repository Pattern, Service Layer Pattern, DTO Pattern, Dependency Injection เป็นพื้นฐานที่ใช้แล้ว
- Pattern เพิ่มเติม เช่น Strategy / Builder / Factory สามารถเพิ่มได้ตามช่วงต่อไปของระบบ

### 6.4 Git workflow
- แต่ละคนต้องทำ Branch ของตนเองตามรูปแบบชื่อ
- Commit ต้องมีความหมายและกระจายตลอดการทำงาน
- Merge ผ่าน Pull Request และมี reviewer อย่างน้อย 1 คน

### 6.5 Testing
- ทุก module ที่ทำต้องมี test ที่ได้ยืนยันจริง
- ผลรวมปัจจุบันผ่าน Maven test แล้ว

## 7. สรุปภาพรวม

ในภาพรวมของกลุ่มแล้ว:
- Member 1: ทำพื้นฐาน backend และ core business logic ให้พร้อม
- Member 2: ต่อยอดระบบคลังและการเคลื่อนไหวสินค้า
- Member 3: ทำงานด้าน customer, sales order, design analysis และเอกสาร

ดังนั้น หากถามว่า “Member 1 ยังไม่เสร็จหรือไม่” คำตอบคือ
- ถ้าหมายถึงหน้าที่หลักของ Member 1: เกือบสิ้นสุด/เสร็จตาม scope ที่ได้รับแล้ว
- ถ้าหมายถึงทั้งโปรเจค: ยังมีงานของ Member 2 และ Member 3 ที่ต้องทำต่อให้ครบตามใบงาน

## 8. เอกสารที่เกี่ยวข้อง

- [README.md](../README.md)
- [solid-analysis.md](solid-analysis.md)
- [design-patterns.md](design-patterns.md)
- [api-documentation.md](api-documentation.md)
