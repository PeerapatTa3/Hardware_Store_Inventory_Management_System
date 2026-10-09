# PR Review Notes / สรุปสถานะที่เหลือและสิ่งที่ควรรีวิว

## 1. สภาพปัจจุบันของโปรเจค

โปรเจคนี้มีความคืบหน้าในระดับ "Core backend + frontend shell + operational workflows" แต่ยังไม่พร้อมสำหรับยืนยันว่า "เสร็จสมบูรณ์ตามใบงาน" เนื่องจากยังมีงานสำคัญที่ต้องทำให้ครบก่อนส่งหรือ merge

### ทำแล้วแล้ว
- Spring Boot backend หลักทำงานได้ตาม flow ของ Inventory / Purchase / Stock Movement
- มี CRUD สำหรับ Category / Supplier / Product
- มี frontend React SPA สำหรับหน้าทำงานหลัก เช่น dashboard, catalog, inventory, purchases, stock movement
- มีเอกสารโปรเจคและคู่มือ frontend / handoff สำหรับ AI ที่เข้ามาทำต่อ
- Frontend build ผ่านการตรวจสอบ local command: `npm run build`

### ยังเหลือ / ยังไม่เสร็จตามใบงาน
- Customer module ยังต้องตรวจสอบและอาจยังไม่สมบูรณ์
- Sales Order flow ยังไม่ใช่สิ่งที่ยืนยันเรียบร้อย
- Designing Pattern จริง ๆ (State / Strategy / Observer) ยังต้องเช็กว่ามีการใช้ใน logic จริงหรือไม่
- Diagram และ documents ที่ต้องส่งตามใบงานยังไม่ครบตามที่กำหนด
- Deployment/public URL ยังไม่ได้ยืนยันว่าใช้งานจริง
- Pull request / reviewer / merge flow ยังไม่ได้มีการสรุปและยืนยันจริง

---

## 2. งานที่ต้องทำต่อแบบลำดับความสำคัญ

### Priority 1: Customer + Sales flow
สิ่งที่ควรทำต่อก่อน:
- Customer CRUD และ validation
- SalesOrder creation flow
- stock deduction logic
- stock movement OUT generation
- validation สำหรับ stock ไม่ให้ติดลบ
- rollback / invalid status cases

### Priority 2: Design Pattern ระดับจริง
ต้องมั่นใจว่า pattern จริง ๆ ถูกนำไปใช้จริง เช่น:
- State pattern สำหรับการเปลี่ยนสถานะของ SalesOrder
- Strategy pattern สำหรับ discount rules
- Observer pattern สำหรับ low-stock notification/event

### Priority 3: Deployment / Integration check
- ตรวจ Docker / Compose / Railway config
- ทดสอบการรันแบบ production
- ตรวจว่า DB production seed หรือ migration มีข้อมูล login สำรองหรือไม่
- ยืนยัน public URL หรือ deployment status

### Priority 4: Documentation and reviewer handoff
- สร้าง diagram ให้ครบตามใบงาน
- อัปเดต README/Progress ให้ตรงกับโค้ดจริง
- จัดเตรียม PR description สำหรับคนรีวิว

---

## 3. ข้อมูลที่ควรสรุปใน PR

### Title ตัวอย่าง
- `feat: add frontend workflows and inventory management UI`
- `feat: implement hardware store frontend and stock operations`
- `feat: complete backend inventory flow and frontend integration`

### Summary ที่ควรเขียนลง PR
- เพิ่ม frontend สำหรับการใช้งานระบบคลังสินค้า
- รองรับ CRUD สำหรับ catalog และ inventory workflows
- เพิ่ม authentication flow กับ backend
- เชื่อม frontend กับ REST API หลัก
- เพิ่มเอกสารสรุปสถานะและ handoff สำหรับการทำต่อ

### สิ่งที่ reviewer ควรเช็ก
- Frontend build ผ่านหรือไม่
- API endpoint ถูกเรียกถูก port และ CORS หรือไม่
- Auth flow ล็อกอินได้หรือไม่
- Backend local profile / prod profile การทำงานต่างกันอย่างไร
- ระบบมีข้อมูล seed สำหรับ login หรือไม่
- customer/sales flow ที่ยังค้างมีความสำคัญมากแค่ไหน

---

## 4. ข้อควรระวังสำหรับ reviewer

### 1. Local profile vs Production profile
โปรเจคมีความต่างระหว่าง default profile และ prod profile:
- default profile ใช้ H2 memory และ `data.sql` สำหรับ seed user
- prod profile ใช้ PostgreSQL และ `spring.sql.init.mode: never`

ดังนั้น reviewer ควรระวังว่าในเครื่องคอมพิวเตอร์ปกติ login กับ `admin/admin` อาจใช้ได้เฉพาะตอนรันใน local profile เท่านั้น

### 2. Frontend build สามารถ run ได้
โค้ด frontend ถูกตรวจสอบแล้วว่า build ผ่านด้วย `npm run build`

### 3. Deploy ยังไม่ได้ยืนยัน
- ยังไม่มี public URL ที่ยืนยันว่า deploy จริง
- ต้องไม่เขียนว่า deploy แล้ว ถ้าไม่มี evidence จริง

---

## 5. Suggested PR text template

```md
## Summary
This PR adds the frontend and operational inventory management workflows for the hardware store system. It includes catalog CRUD, inventory summary, stock movement views, purchase flows, and the app shell for authenticated access.

## What is included
- React frontend shell and routes
- authenticated login flow
- category/supplier/product CRUD screens
- inventory and stock movement pages
- purchase order management and receive flow
- project handoff documentation for follow-up work

## Validation
- `npm run build` in `code/frontend` succeeds
- core backend endpoints exist and match the frontend contract

## Known follow-up work
- verify and complete Customer module
- finalize Sales Order and stock deduction logic
- confirm State/Strategy/Observer implementation in runtime business logic
- complete diagrams and deployment documentation
- publish or verify public deployment URL if required
```

---

## 6. Commit message ที่ควรใช้ต่อเนื่อง

### ส่วนที่ทำแล้ว
- `chore: ignore generated frontend files and lock deps`
- `docs: add AI handoff summary for project status`

### ส่วนที่ควรทำต่อ
- `feat: add customer module and validation`
- `feat: implement sales order creation and stock deduction`
- `feat: finalize state strategy observer flow`
- `docs: add diagrams and final project documentation`
- `deploy: configure production deploy verification`

---

## 7. ข้อสรุปสั้น ๆ

โปรเจคนี้ใช้เวลาในการพัฒนาซึ่งทำให้มีโครงสร้างที่ชัดเจนและส่วนประกอบสำคัญเริ่มมีครบแล้ว แต่ยังไม่ถึงภาวะ “เสร็จตามใบงาน” เพราะงานที่เหลือมีทั้ง backend business logic, diagrams, deployment และ reviewer handoff ที่ต้องทำให้ละเอียดก่อน merge หรือส่ง

สำหรับ PR ครั้งนี้ควรเขียนให้ชัดว่า:
- ส่วนไหนทำเสร็จแล้ว
- ตรวจได้อย่างไร
- ส่วนไหนยังค้างและจะทำต่อ
- reviewer ต้องเช็กอะไรบ้าง

---

## 8. Reference files

- [README.md](../../README.md)
- [progress.md](../../progress.md)
- [PROJECT_PLAN.md](../../PROJECT_PLAN.md)
- [code/frontend/README.md](../../code/frontend/README.md)
- [doc/เอกสารที่จำเป็นชั่วคราว/AI_Project_HandOff.md](../เอกสารที่จำเป็นชั่วคราว/AI_Project_HandOff.md)
