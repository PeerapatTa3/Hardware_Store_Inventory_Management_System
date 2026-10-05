# Project Progress

สถานะนี้ประเมินจากโค้ด เอกสาร และไฟล์ใน repository เทียบกับ [PROJECT_PLAN.md](./PROJECT_PLAN.md) ไม่ถือว่าเอกสารแผนหรือ README เป็นหลักฐานว่า feature ทำงานแล้ว

## สรุปสถานะ

**โปรเจกต์อยู่ช่วง Phase 4 — Purchase** โดยมี Backend พื้นฐาน, CRUD ของ Category / Supplier / Product, InventoryStock, StockMovement, Purchase persistence model/DTO, service และ REST endpoints สำหรับ create/list/get/update/receive แล้ว

อย่างไรก็ตาม **Phase 2 ยังไม่ครบ** เพราะยังไม่พบ Customer module; และแม้ Purchase REST API กับ workflow receive จะทำงานผ่าน controller/service แล้ว แต่ยังไม่มี HTTP-to-database integration tests จึงยังไม่ควรถือว่า Backend หรือโปรเจกต์โดยรวมเสร็จแล้ว

การตรวจสอบชุดทดสอบล่าสุด: `.\mvnw.cmd test` ใน `code/backend/hardware-store/` ผ่าน **48 tests, 0 failures, 0 errors** รวม Purchase Controller tests, receive success, repeated receive และ transaction rollback tests

## ความคืบหน้าตาม Roadmap

| Phase | หัวข้อ | สถานะ | หลักฐาน / สิ่งที่ยังขาด |
|---|---|---|---|
| 1 | Project Setup | ทำแล้วเป็นส่วนใหญ่ | มี Spring Boot, Maven, Java 17, JPA, H2, PostgreSQL driver, OpenAPI และ Global Exception Handler; ยังไม่พบ Flyway migration หรือ Docker Compose |
| 2 | Core CRUD | ทำบางส่วน | Category, Supplier และ Product มี CRUD; ยังไม่พบ Customer |
| 3 | Inventory System | ทำบางส่วน | มี InventoryStock GET/PUT และ StockMovement POST; การปรับยอดสร้าง `ADJUSTMENT` ใน transaction เดียวกัน; ยังขาด Stock Movement GET endpoints ตาม PROJECT_PLAN และ Controller tests ของ Inventory/Stock Movement |
| 4 | Purchase | ทำแกนหลักแล้ว | มี Purchase entities, status, repositories, DTOs, validation, mapper, service และ REST controller สำหรับ create/list/get/update/receive; receive ปรับ Inventory, บันทึก StockMovement และเปลี่ยนสถานะใน transaction เดียว |
| 5 | Sales | ยังไม่เริ่ม | ไม่พบ Customer, SalesOrder, SalesOrderItem หรือการตรวจและตัด stock |
| 6 | Design Patterns | ยังไม่เริ่ม | State, Strategy และ Observer ปรากฏในเอกสารเป็นแนวทาง แต่ยังไม่พบการนำไปใช้ใน business logic |
| 7 | Frontend | ยังไม่เริ่ม | ไม่พบ React application หรือ frontend source |
| 8 | Integration | ทำบางส่วน | Purchase receive เชื่อม Purchase, Inventory และ StockMovement ใน service และมี REST endpoints; ยังไม่มี HTTP-to-database integration tests หรือ Sales flow |
| 9 | Testing | ทำบางส่วน | Service tests 29 รายการ, JPA persistence tests 4 รายการ, Purchase DTO validation tests 4 รายการ, mapper tests 4 รายการ, exception handler test 1 รายการ และ Purchase Controller tests 6 รายการผ่าน |
| 10 | Documentation | ทำบางส่วน | มี README, API documentation (รวม Purchase endpoints และระบุ Stock Movement GET ที่ยังขาด), Data Dictionary ครอบคลุม Inventory/StockMovement/Purchase, SOLID analysis และ Design Patterns; ยังขาด diagrams และ API docs ของ Sales |
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
| Purchase / Sales modules | ทำแล้วบางส่วน | Purchase มี entities, repositories, DTOs, validation, mapper, service และ REST endpoints สำหรับ create/list/get/update/receive; Sales ยังไม่เริ่ม |
| State / Strategy / Observer | ยังไม่เริ่ม | พบเฉพาะคำอธิบายในเอกสาร ยังไม่พบ implementation |
| Unit / Persistence tests | ทำแล้วบางส่วน | Service 29, JPA persistence 4, Purchase DTO validation 4, mapper 4, exception handler 1 และ Purchase Controller 6 tests ผ่าน; รวม 48 tests |
| Controller / Workflow integration tests | ทำแล้วบางส่วน | Purchase Controller tests ครอบคลุม routes, validation, responses, not-found/conflict; persistence-backed tests ครอบคลุม receive success/duplicate/rollback; ยังขาด Inventory/Stock Movement Controller tests, Stock Movement GET endpoints และ HTTP-to-database Purchase integration tests |
| Flyway / Database migrations | ยังไม่เริ่ม | ไม่พบ migration scripts; ปัจจุบัน JPA ใช้ `ddl-auto: update` |
| Docker | ทำแล้วบางส่วน | มี Dockerfile แต่ยังไม่มี Compose |
| Frontend / Cloud deployment / Public URL | ยังไม่เริ่ม | ไม่พบ implementation หรือ URL |
| Required diagrams | ยังไม่เริ่ม | ยังไม่พบ diagram assets ใน `doc/diagrams/` |
| Git workflow / PR | ทำแล้วบางส่วน | มี branch Member 2 และ push branch ขึ้น origin; ยังไม่พบ Pull Request จาก branch นี้ และยังไม่มี reviewer/merge เข้า `develop` ให้ยืนยัน |

## งานแนะนำลำดับถัดไป

1. ทำ Customer CRUD ให้ครบตาม Phase 2
2. เพิ่ม Stock Movement GET endpoints ตาม PROJECT_PLAN และ Controller tests สำหรับ Inventory/StockMovement; พิจารณา HTTP-to-database tests สำหรับ Purchase
3. ทำ SalesOrder / SalesOrderItem พร้อมตรวจ stock, ป้องกัน stock ติดลบ และเชื่อม Customer
4. ใช้ State, Strategy และ Observer กับ business logic จริง พร้อม test
5. สร้าง diagrams จากระบบที่พัฒนาแล้ว (Data Dictionary อัปเดต Inventory/StockMovement/Purchase แล้ว)
6. เพิ่ม Docker Compose, environment configuration, Frontend และ deployment
7. ตรวจ checklist Phase 12 และ Definition of Done อีกครั้งก่อนสรุปว่าเสร็จ

## หลักฐานที่ตรวจ

- [Backend source และ tests](./code/backend/hardware-store/src/)
- [Maven configuration](./code/backend/hardware-store/pom.xml)
- [Application configuration](./code/backend/hardware-store/src/main/resources/application.yml)
- [API documentation](./doc/api-documentation.md)
- [Data Dictionary](./doc/data-dictionary.md)
- [Documentation and diagram status](./doc/diagrams/README.md)
- [Dockerfile](./code/backend/hardware-store/Dockerfile)
- [README](./README.md)
