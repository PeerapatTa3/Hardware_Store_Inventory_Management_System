# Project Progress

สถานะนี้ประเมินจากโค้ด เอกสาร และไฟล์ใน repository เทียบกับ [PROJECT_PLAN.md](./PROJECT_PLAN.md) ไม่ถือว่าเอกสารแผนหรือ README เป็นหลักฐานว่า feature ทำงานแล้ว

## สรุปสถานะ

**โปรเจกต์อยู่ช่วง Phase 4 — Purchase** โดยมี Backend พื้นฐาน, CRUD ของ Category / Supplier / Product, InventoryStock, StockMovement, Purchase persistence model และ Purchase DTO/validation/mapper แล้ว

อย่างไรก็ตาม **Phase 2 ยังไม่ครบ** เพราะยังไม่พบ Customer module และ Purchase ยังไม่มี service/API/workflow เชื่อม Inventory กับ StockMovement เป็น end-to-end flow จึงยังไม่ควรถือว่า Backend หรือโปรเจกต์โดยรวมเสร็จแล้ว

การตรวจสอบชุดทดสอบล่าสุด: `.\mvnw.cmd test` ใน `code/backend/hardware-store/` ผ่าน **25 tests, 0 failures, 0 errors** หลังเพิ่ม Purchase DTO, validation และ mapper tests

## ความคืบหน้าตาม Roadmap

| Phase | หัวข้อ | สถานะ | หลักฐาน / สิ่งที่ยังขาด |
|---|---|---|---|
| 1 | Project Setup | ทำแล้วเป็นส่วนใหญ่ | มี Spring Boot, Maven, Java 17, JPA, H2, PostgreSQL driver, OpenAPI และ Global Exception Handler; ยังไม่พบ Flyway migration หรือ Docker Compose |
| 2 | Core CRUD | ทำบางส่วน | Category, Supplier และ Product มี CRUD; ยังไม่พบ Customer |
| 3 | Inventory System | ทำแกนหลักแล้ว | มี InventoryStock API สำหรับอ่าน/ปรับจำนวน และ StockMovement API; การปรับยอด Inventory สร้าง movement ประเภท `ADJUSTMENT` ใน transaction เดียวกัน; ยังไม่มี Controller/Integration tests |
| 4 | Purchase | กำลังพัฒนา | มี Purchase entities, status, repositories, DTOs, validation และ mapper; ยังไม่มี service, API หรือ workflow รับสินค้า |
| 5 | Sales | ยังไม่เริ่ม | ไม่พบ Customer, SalesOrder, SalesOrderItem หรือการตรวจและตัด stock |
| 6 | Design Patterns | ยังไม่เริ่ม | State, Strategy และ Observer ปรากฏในเอกสารเป็นแนวทาง แต่ยังไม่พบการนำไปใช้ใน business logic |
| 7 | Frontend | ยังไม่เริ่ม | ไม่พบ React application หรือ frontend source |
| 8 | Integration | ทำบางส่วน | มี Product, Inventory และ StockMovement แยกเป็นโมดูล; ยังไม่มี purchase/sales end-to-end flow |
| 9 | Testing | ทำบางส่วน | Service unit tests 17 รายการ, JPA persistence test 1 รายการ, Purchase DTO validation tests 4 รายการ และ mapper tests 3 รายการผ่าน; ยังไม่มี Controller tests หรือทดสอบ Purchase workflow แบบ end-to-end |
| 10 | Documentation | ทำบางส่วน | มี README, API documentation, Data Dictionary, SOLID analysis และ Design Patterns; ไฟล์ diagrams มีเพียง README และ API docs ยังไม่ครอบคลุม Inventory/Purchase/Sales |
| 11 | Docker & Deployment | ทำบางส่วน | มี Dockerfile; ยังไม่พบ `docker-compose.yml`, deployment configuration หรือ Public URL |
| 12 | Final Verification | ยังไม่เริ่ม | ยังมี requirement สำคัญที่ขาดตาม checklist ด้านล่าง |

## สถานะ Requirement สำคัญ

| Requirement | สถานะ | หมายเหตุ |
|---|---|---|
| Java 17+ และ Spring Boot 3.x+ | ทำแล้ว | ตั้งค่า Java 17 และ Spring Boot 4.1.1 ใน Maven |
| Layered Architecture | ทำแล้วในโครงสร้าง | แยก Controller, Service, Repository, Entity, DTO และ Mapper |
| CRUD อย่างน้อย 2 resources | ทำแล้ว | Category, Supplier และ Product มี CRUD |
| Bean Validation | ทำแล้วบางส่วน | ใช้ validation annotations ใน request DTO และ `@Valid` ที่ Controller |
| Global Exception Handler | ทำแล้ว | มี `GlobalExceptionHandler` และ exception เฉพาะ |
| Pagination / Sorting | ทำแล้วบางส่วน | Product API รองรับ pagination/sorting; Category และ Supplier ยังคืนรายการเป็น List |
| Swagger / OpenAPI | ทำแล้ว | มี OpenAPI config และ dependency ใน Maven |
| One-to-One | ทำแล้ว | `InventoryStock` เชื่อม `Product` ด้วย `@OneToOne` |
| One-to-Many | ทำแล้วในระดับ FK | Product อ้าง Category และ Supplier ด้วย `@ManyToOne`; ยังไม่มี inverse `@OneToMany` collection |
| อย่างน้อย 6 ตารางหลัก | ทำแล้วบางส่วน | พบ Entity 7 ตัว: Category, Product, Supplier, InventoryStock, StockMovement, PurchaseOrder และ PurchaseItem |
| Purchase / Sales modules | ทำแล้วบางส่วน | Purchase มี entities, repositories, DTOs, validation และ mapper แต่ยังไม่มี service/API/workflow; Sales ยังไม่เริ่ม |
| State / Strategy / Observer | ยังไม่เริ่ม | พบเฉพาะคำอธิบายในเอกสาร ยังไม่พบ implementation |
| Unit / Persistence tests | ทำแล้วบางส่วน | Service 17, JPA persistence 1, Purchase DTO validation 4 และ mapper 3 tests ผ่าน |
| Controller / Workflow integration tests | ยังไม่เริ่ม | ยังไม่พบ Controller tests หรือการทดสอบ workflow แบบ end-to-end |
| Flyway / Database migrations | ยังไม่เริ่ม | ไม่พบ migration scripts; ปัจจุบัน JPA ใช้ `ddl-auto: update` |
| Docker | ทำแล้วบางส่วน | มี Dockerfile แต่ยังไม่มี Compose |
| Frontend / Cloud deployment / Public URL | ยังไม่เริ่ม | ไม่พบ implementation หรือ URL |
| Required diagrams | ยังไม่เริ่ม | ยังไม่พบ diagram assets ใน `doc/diagrams/` |
| Git workflow / PR | ทำแล้วบางส่วน | มี branch และ merge เข้า `develop`; ยังไม่ยืนยัน reviewer และเกณฑ์ commit ของสมาชิกทุกคนจากไฟล์ปัจจุบัน |

## งานแนะนำลำดับถัดไป

1. ทำ Customer CRUD ให้ครบตาม Phase 2
2. เพิ่ม PurchaseOrder / PurchaseItem และ workflow รับสินค้า โดยปรับ InventoryStock และบันทึก StockMovement ภายใน transaction
3. เพิ่ม Controller และ Integration tests สำหรับ API/flow ที่มีอยู่และที่เพิ่มใหม่
4. ทำ SalesOrder / SalesOrderItem พร้อมตรวจ stock, ป้องกัน stock ติดลบ และเชื่อม Customer
5. ใช้ State, Strategy และ Observer กับ business logic จริง พร้อม test
6. อัปเดต API docs, Data Dictionary และสร้าง diagrams จากระบบที่พัฒนาแล้ว
7. เพิ่ม Docker Compose, environment configuration, Frontend และ deployment
8. ตรวจ checklist Phase 12 และ Definition of Done อีกครั้งก่อนสรุปว่าเสร็จ

## หลักฐานที่ตรวจ

- [Backend source และ tests](./code/backend/hardware-store/src/)
- [Maven configuration](./code/backend/hardware-store/pom.xml)
- [Application configuration](./code/backend/hardware-store/src/main/resources/application.yml)
- [API documentation](./doc/api-documentation.md)
- [Data Dictionary](./doc/data-dictionary.md)
- [Documentation and diagram status](./doc/diagrams/README.md)
- [Dockerfile](./code/backend/hardware-store/Dockerfile)
- [README](./README.md)
