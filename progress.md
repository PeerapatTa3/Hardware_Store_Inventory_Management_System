# Project Progress

สถานะนี้ประเมินจากโค้ด เอกสาร และไฟล์ใน repository เทียบกับ [PROJECT_PLAN.md](./PROJECT_PLAN.md) ไม่ถือว่าเอกสารแผนหรือ README เป็นหลักฐานว่า feature ทำงานแล้ว

## สรุปสถานะ

**โปรเจกต์อยู่ช่วง Phase 4 — Purchase** โดยมี Backend พื้นฐาน, CRUD ของ Category / Supplier / Product, InventoryStock, StockMovement, Purchase persistence model/DTO, service และ REST endpoints สำหรับ create/list/get/update/receive แล้ว

อย่างไรก็ตาม **Phase 2 ยังไม่ครบ** เพราะยังไม่พบ Customer module; Purchase มี REST-to-database integration test ครอบคลุม create/receive/read stock movement แล้ว แต่ยังขาด Sales flow และ requirement อื่นตาม roadmap จึงยังไม่ควรถือว่า Backend หรือโปรเจกต์โดยรวมเสร็จแล้ว

การตรวจสอบชุดทดสอบล่าสุดเมื่อ 2026-10-06: `.\mvnw.cmd test` ใน `code/backend/hardware-store/` ผ่าน **63 tests, 0 failures, 0 errors** รวม Controller tests ของ Inventory/Stock Movement/Purchase, Purchase REST-to-database integration, receive success, repeated receive และ transaction rollback tests งาน Stock Movement GET และ tests ล่าสุดอยู่ใน commit `919e439` ซึ่งตรงกับ upstream; การอัปเดตเอกสารสถานะในรอบนี้ยังเป็นการเปลี่ยนแปลงใน working tree

## ความคืบหน้าตาม Roadmap

| Phase | หัวข้อ | สถานะ | หลักฐาน / สิ่งที่ยังขาด |
|---|---|---|---|
| 1 | Project Setup | ทำแล้วเป็นส่วนใหญ่ | มี Spring Boot, Maven, Java 17, JPA, H2, PostgreSQL driver, OpenAPI และ Global Exception Handler; ยังไม่พบ Flyway migration หรือ Docker Compose |
| 2 | Core CRUD | ทำบางส่วน | Category, Supplier และ Product มี CRUD; ยังไม่พบ Customer |
| 3 | Inventory System | ทำแกนหลักแล้ว | มี InventoryStock GET/PUT และ StockMovement POST/GET ทั้งหมด/GET ตาม Product ID; adjustment เชื่อม `ADJUSTMENT` ใน transaction เดียว; เพิ่ม Inventory และ Stock Movement Controller tests แล้ว |
| 4 | Purchase | ทำแกนหลักแล้ว | มี Purchase entities, status, repositories, DTOs, validation, mapper, service และ REST controller สำหรับ create/list/get/update/receive; receive ปรับ Inventory, บันทึก StockMovement และเปลี่ยนสถานะใน transaction เดียว |
| 5 | Sales | ยังไม่เริ่ม | ไม่พบ Customer, SalesOrder, SalesOrderItem หรือการตรวจและตัด stock |
| 6 | Design Patterns | ยังไม่เริ่ม | State, Strategy และ Observer ปรากฏในเอกสารเป็นแนวทาง แต่ยังไม่พบการนำไปใช้ใน business logic |
| 7 | Frontend | ทำแล้วบางส่วน | มี React SPA สำหรับ authentication, dashboard, catalog CRUD, inventory, stock movements, purchases และ sales orders; ยังต้องตรวจ production build และทดสอบการเชื่อมต่อกับ backend ใน environment ที่มี Node.js |
| 8 | Integration | ทำบางส่วน | Purchase receive เชื่อม Purchase, Inventory และ StockMovement ผ่าน service และ REST API; มี HTTP-to-database integration test สำหรับ create/receive/read movement; ยังไม่มี Sales flow |
| 9 | Testing | ทำบางส่วน | Service 34, JPA persistence 4, Purchase DTO validation 4, mapper 4, exception handler 1, Controller 15 และ API integration 1 tests ผ่าน; รวม 63 tests |
| 10 | Documentation | ทำบางส่วน | มี README, API documentation (Inventory, Stock Movement, Purchase), Data Dictionary ครอบคลุม Inventory/StockMovement/Purchase, SOLID analysis และ Design Patterns; ยังขาด diagrams และ API docs ของ Sales |
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
| Unit / Persistence tests | ทำแล้วบางส่วน | Service 34, JPA persistence 4, Purchase DTO validation 4, mapper 4, exception handler 1, Controller 15 และ API integration 1 tests ผ่าน; รวม 63 tests |
| Controller / Workflow integration tests | ทำแล้วบางส่วน | Controller tests ครอบคลุม Inventory GET/PUT, Stock Movement POST/GET, Purchase routes, validation และ error responses; persistence-backed receive tests ครอบคลุม success/duplicate/rollback; API integration test ยืนยัน Purchase receive เชื่อม Inventory/StockMovement |
| Flyway / Database migrations | ยังไม่เริ่ม | ไม่พบ migration scripts; ปัจจุบัน JPA ใช้ `ddl-auto: update` |
| Docker | ทำแล้วบางส่วน | มี Dockerfile แต่ยังไม่มี Compose |
| Frontend / Cloud deployment / Public URL | ทำแล้วบางส่วน | มี React frontend ใน `code/frontend/` และคู่มือใน `doc/เอกสารที่จำเป็นชั่วคราว/frontend.md`; ยังไม่ยืนยัน production build, cloud deployment หรือ Public URL |
| Required diagrams | ยังไม่เริ่ม | ยังไม่พบ diagram assets ใน `doc/diagrams/` |
| Git workflow / PR | ทำแล้วบางส่วน | มี branch Member 2 และ push branch ขึ้น origin; ยังไม่พบ Pull Request จาก branch นี้ และยังไม่มี reviewer/merge เข้า `develop` ให้ยืนยัน |

## งานแนะนำลำดับถัดไป

1. ทำ Customer CRUD ให้ครบตาม Phase 2
2. ทำ SalesOrder / SalesOrderItem พร้อมตรวจ stock, ป้องกัน stock ติดลบ และเชื่อม Customer
3. ใช้ State, Strategy และ Observer กับ business logic จริง พร้อม test
4. สร้าง diagrams จากระบบที่พัฒนาแล้ว (Data Dictionary อัปเดต Inventory/StockMovement/Purchase แล้ว)
5. เพิ่ม Docker Compose, environment configuration, Frontend และ deployment
6. ตรวจ checklist Phase 12 และ Definition of Done อีกครั้งก่อนสรุปว่าเสร็จ

## หลักฐานที่ตรวจ

- [Backend source และ tests](./code/backend/hardware-store/src/)
- [Maven configuration](./code/backend/hardware-store/pom.xml)
- [Application configuration](./code/backend/hardware-store/src/main/resources/application.yml)
- [API documentation](./doc/api-documentation.md)
- [Data Dictionary](./doc/data-dictionary.md)
- [Documentation and diagram status](./doc/diagrams/README.md)
- [Dockerfile](./code/backend/hardware-store/Dockerfile)
- [README](./README.md)
