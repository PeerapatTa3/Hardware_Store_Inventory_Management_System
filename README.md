# Hardware Store Inventory Management System

ระบบจัดการคลังสินค้าของร้านอุปกรณ์การช่าง พัฒนาด้วย **Spring Boot (Backend)** และ **React (Frontend)** โดยมีฟังก์ชันสำหรับจัดการสินค้า หมวดหมู่ ผู้จำหน่าย ลูกค้า คลังสินค้า การเคลื่อนไหวของสินค้า การสั่งซื้อ การขายสินค้า และระบบรักษาความปลอดภัยด้วย **JWT Authentication**

โครงสร้างระบบออกแบบตามหลัก **Layered Architecture**, **SOLID Principles**, **Enterprise / Architectural Design Patterns** และ **Behavioral Design Patterns** เพื่อให้สอดคล้องกับข้อกำหนดของรายวิชา **CP353002 Principles of Software Design and Development**

---

## สมาชิกกลุ่ม

| ลำดับ | ชื่อ-นามสกุล               | รหัสนักศึกษา | Section | Branch                    | หน้าที่รับผิดชอบ                                               |
| ----- | -------------------------- | -----------: | ------: | ------------------------- | -------------------------------------------------------------- |
| 1     | นายพีรพล แก้วเจริญสันติสุข |  673380287-8 |      01 | `peerapol_673380287-8_01` | Backend Setup, Exception, Swagger, Category, Supplier, Product |
| 2     | นายพิสิษฐ์ ทรัพย์อุดมโชติ  |  673380285-2 |      01 | `phisit_673380285-2_01`   | Inventory, Stock Movement, Purchase                            |
| 3     | นายพีรพัฒน์ แท่นประยุทร    |  673380288-6 |      01 | `peerapat_673380288-6_01` | Customer, Sales Order, Design Patterns                         |

---

## ฟังก์ชันหลักของระบบ

### 1. การจัดการสินค้าและหมวดหมู่
* เพิ่ม แก้ไข ลบ และค้นหาข้อมูลสินค้า
* จัดการหมวดหมู่สินค้าและข้อมูลผู้จำหน่าย
* ค้นหาสินค้าด้วย SKU และชื่อสินค้า
* รองรับ Pagination และ Sorting

### 2. การจัดการคลังสินค้า (Inventory & Stock Movement)
* ตรวจสอบจำนวนสินค้าคงเหลือ ปรับปรุงจำนวนสินค้า
* ตรวจสอบสินค้าที่มีจำนวนต่ำกว่าจุดสั่งซื้อขั้นต่ำ (Low Stock)
* บันทึกการเคลื่อนไหวของสินค้า (รับเข้า `IN`, จ่ายออก `OUT`, ปรับยอด `ADJUSTMENT`)
* ดูประวัติการเปลี่ยนแปลงของสินค้าและค้นหาตามช่วงเวลา

### 3. การจัดซื้อสินค้า (Purchase Order)
* สร้างใบสั่งซื้อสินค้าและเพิ่มรายการสินค้า
* ยืนยันการรับสินค้าเพื่อเพิ่มจำนวนสินค้าเข้าสู่คลังโดยอัตโนมัติ (สร้าง Stock Movement `IN`)

### 4. การจัดการลูกค้าและการขายสินค้า (Sales Order)
* สร้าง Sales Order และตัดจำนวนสินค้าออกจากคลังโดยอัตโนมัติ (สร้าง Stock Movement `OUT`)
* ตรวจสอบจำนวนสินค้าก่อนขาย ป้องกันสินค้าติดลบ
* จัดการสถานะคำสั่งซื้อ (PENDING, CONFIRMED, SHIPPED, COMPLETED, CANCELLED)

### 5. ระบบรักษาความปลอดภัย (Security)
* มีระบบ Authentication และ Authorization ด้วย **Spring Security** และ **JWT (JSON Web Token)**

---

## Tech Stack

### Backend
* **Language:** Java 17
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security & JWT (`jjwt`)
* **Database & ORM:** Spring Data JPA, Hibernate, PostgreSQL (Production), H2 (Development/Testing)
* **Tools/Libraries:** Maven, Lombok, MapStruct (Data Mapper)
* **API Docs:** Springdoc OpenAPI / Swagger UI

### Frontend
* **Library:** React 18
* **Routing:** React Router v6
* **HTTP Client:** Axios
* **UI Components/Feedback:** React Toastify
* **Build Tool:** Create React App (`react-scripts`)

### Deployment
* **Containerization:** Docker & Docker Compose
* **Platform:** รองรับ Cloud Deployment

---

## System Architecture

ระบบใช้ **Layered Architecture** โดยแบ่งความรับผิดชอบอย่างชัดเจนดังนี้

```text
Frontend (React)
    │
    │ HTTP / REST API (JSON)
    ▼
Controller (Spring Web)
    │
    ▼
Service Layer (Business Logic)
    │
    ▼
Repository (Spring Data JPA)
    │
    ▼
Database (H2 / PostgreSQL)
```

### โครงสร้างแต่ละ Layer ใน Backend

* **Controller Layer**: จัดการ HTTP Requests และ Responses
* **Service Layer**: จัดการ Business Logic หลักทั้งหมดของระบบ
* **Repository Layer**: จัดการติดต่อฐานข้อมูลผ่าน Spring Data JPA
* **Domain Layer**: Entity Models ( mapped ไปยังตารางฐานข้อมูล)
* **DTO Layer**: Data Transfer Objects รับ-ส่งข้อมูล
* **Mapper Layer**: ใช้ `MapStruct` เพื่อช่วยแมปข้อมูลระหว่าง Entity และ DTO อย่างปลอดภัย
* **Security Layer**: ตั้งค่า Spring Security, JWT Filter, Authentication

---

## การติดตั้งและใช้งาน (How to Run)

### Requirements
* **Java 17** และ **Maven**
* **Node.js** และ **npm**
* **Docker** และ **Docker Compose** (หากต้องการรันผ่าน Container)
* **PostgreSQL** (สำหรับรัน Production mode)

### 1. การรัน Backend (Development Mode - H2 Database)
ด้วยโหมดนี้ ระบบจะใช้ In-memory Database (H2) ซึ่งข้อมูลจะหายไปเมื่อปิดโปรแกรม เหมาะสำหรับการพัฒนา

```bash
# 1. เข้าไปที่โฟลเดอร์ backend
cd code/backend/hardware-store

# 2. Build และติดตั้ง dependencies
./mvnw clean install    # (บน Windows ให้ใช้ mvnw.cmd clean install)

# 3. รัน Spring Boot Application
./mvnw spring-boot:run  # (บน Windows ให้ใช้ mvnw.cmd spring-boot:run)
```
* Backend รันอยู่ที่พอร์ต: `http://localhost:8080`
* Swagger UI (API Docs): `http://localhost:8080/swagger-ui.html`

### 2. การรัน Frontend
```bash
# 1. เข้าไปที่โฟลเดอร์ frontend
cd code/frontend

# 2. ติดตั้ง Dependencies
npm install

# 3. รัน React Application
npm start
```
* Frontend รันอยู่ที่พอร์ต: `http://localhost:3000`

### 3. การรันด้วย Docker Compose (Production Mode)
ใช้สำหรับการ Deploy โดยจะรัน Backend ร่วมกับฐานข้อมูลที่เป็น PostgreSQL หากกำหนด Environment Variable ให้ครบถ้วน

```bash
# 1. อยู่ที่ Root directory ของโปรเจกต์
# 2. ตั้งค่า Environment Variables ก่อน (หรือกำหนดใน .env)
# ตัวอย่าง:
# export DB_URL=jdbc:postgresql://<host>:5432/hardware_store
# export DB_USERNAME=postgres
# export DB_PASSWORD=your_password

# 3. รัน Docker Compose
docker-compose up -d --build
```
ระบบ Backend จะสตาร์ทที่ `http://localhost:8080`

---

## การรัน Test (How to Run Tests)
ระบบมีการทดสอบด้วย JUnit 5 และ Mockito

```bash
cd code/backend/hardware-store
./mvnw test       # (บน Windows ให้ใช้ mvnw.cmd test)
```

การทดสอบครอบคลุมถึง:
* Unit Test, Service Test, Controller Test (MockMvc)
* Validation Test และ Exception Test
* Security (JWT) Test

---

## Design Patterns & SOLID Principles

ระบบถูกพัฒนาภายใต้แนวคิด **SOLID Principles** เช่น Single Responsibility (การแบ่งแยก Controller, Service, DTO) และ Dependency Inversion (การใช้งาน Interface สำหรับ Service และ Repository)

การนำ **Design Patterns** มาปรับใช้:
* **Enterprise Patterns:** Layered Architecture, MVC, DTO, Repository
* **Behavioral Patterns:**
  * **State Pattern:** ใช้จัดการสถานะของ Order (Pending -> Confirmed -> Shipped -> Completed/Cancelled)
  * **Strategy Pattern:** (ถ้ามีการประยุกต์) เช่น ระบบคิดส่วนลด (Member, Bulk)
  * **Observer Pattern:** ติดตาม Event สินค้าเข้า-ออก เพื่อประเมินยอดคงเหลือ (Low Stock Warning)

*(รายละเอียดการวิเคราะห์ SOLID และ Patterns อยู่ในโฟลเดอร์ `doc/`)*

---

## เอกสารเพิ่มเติม (Documentation)

สามารถดูรายละเอียดเจาะลึกได้ในโฟลเดอร์ `doc/`:
* [Data Dictionary & Database Schema](doc/data-dictionary.md)
* [Design Patterns & Architecture](doc/design-patterns.md)
* [SOLID Analysis](doc/solid-analysis.md)
* [API Documentation](doc/api-documentation.md)
* ไดอะแกรมเพิ่มเติมเช่น ER Diagram, Sequence Diagram, Use Case จะอยู่ที่ [doc/diagrams/](doc/diagrams/)
