# Hardware Store Inventory Management System

ระบบจัดการคลังสินค้าของร้านอุปกรณ์การช่าง พัฒนาด้วย **Spring Boot** และ **React** โดยมีฟังก์ชันสำหรับจัดการสินค้า หมวดหมู่ ผู้จำหน่าย ลูกค้า คลังสินค้า การเคลื่อนไหวของสินค้า การสั่งซื้อ และการขายสินค้า

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

### 1. การจัดการสินค้า

* เพิ่ม แก้ไข ลบ และค้นหาข้อมูลสินค้า
* จัดการหมวดหมู่สินค้า
* จัดการข้อมูลผู้จำหน่าย
* ค้นหาสินค้าด้วย SKU และชื่อสินค้า
* ค้นหาสินค้าตามหมวดหมู่
* รองรับ Pagination และ Sorting

### 2. การจัดการคลังสินค้า

* ตรวจสอบจำนวนสินค้าคงเหลือ
* ปรับปรุงจำนวนสินค้า
* ตรวจสอบสินค้าที่มีจำนวนไม่เพียงพอ
* ตรวจสอบสินค้าที่มีจำนวนต่ำกว่าจุดสั่งซื้อขั้นต่ำ

### 3. การบันทึกการเคลื่อนไหวของสินค้า

* รับสินค้าเข้า (`IN`)
* จ่ายสินค้าออก (`OUT`)
* ปรับยอดสินค้า (`ADJUSTMENT`)
* ดูประวัติการเปลี่ยนแปลงของสินค้า
* ค้นหาประวัติตามสินค้าและช่วงเวลา

### 4. การจัดซื้อสินค้า

* สร้างใบสั่งซื้อสินค้า
* เพิ่มรายการสินค้าในใบสั่งซื้อหลายรายการ
* ยืนยันการรับสินค้า
* เพิ่มจำนวนสินค้าเข้าสู่คลังโดยอัตโนมัติ
* สร้าง Stock Movement ประเภท `IN` โดยอัตโนมัติ

### 5. การจัดการลูกค้า

* เพิ่ม แก้ไข ลบ และค้นหาข้อมูลลูกค้า
* ตรวจสอบข้อมูลซ้ำ เช่น เบอร์โทรศัพท์หรืออีเมล

### 6. การขายสินค้า

* สร้าง Sales Order
* เพิ่มรายการสินค้าในคำสั่งซื้อหลายรายการ
* ตรวจสอบจำนวนสินค้าก่อนขาย
* ตัดจำนวนสินค้าออกจากคลังโดยอัตโนมัติ
* สร้าง Stock Movement ประเภท `OUT`
* จัดการสถานะของ Sales Order

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 3.x
* Maven
* Spring Web
* Spring Data JPA
* Hibernate
* Bean Validation
* PostgreSQL
* Flyway Migration
* Lombok
* Springdoc OpenAPI / Swagger UI

### Testing

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc

### Frontend

* React
* JavaScript / TypeScript
* HTML
* CSS

### Development Tools

* Git
* GitHub
* VS Code / IntelliJ IDEA
* Postman
* Swagger UI

### Deployment

* Docker
* Docker Compose
* Cloud Platform
* PostgreSQL Cloud Database

---

## System Architecture

ระบบใช้ **Layered Architecture** โดยแบ่งความรับผิดชอบออกเป็นแต่ละ Layer ดังนี้

```text
Frontend
    │
    │ HTTP / REST API
    ▼
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
JPA / Hibernate
    │
    ▼
PostgreSQL
```

### โครงสร้างแต่ละ Layer

* **Controller Layer**
  รับ HTTP Request และส่ง Response ผ่าน REST API

* **Service Layer**
  จัดการ Business Logic ของระบบ

* **Repository Layer**
  ติดต่อและจัดการข้อมูลใน Database ผ่าน Spring Data JPA

* **Domain Layer**
  ประกอบด้วย Entity และ Enum ที่ใช้แทนข้อมูลและสถานะของระบบ

* **DTO Layer**
  ใช้สำหรับรับและส่งข้อมูลระหว่าง Frontend และ Backend

* **Mapper Layer**
  แปลงข้อมูลระหว่าง Entity และ DTO

* **Config / Exception**
  จัดการ Configuration, Swagger/OpenAPI และ Global Exception Handler

### Backend Package Structure

```text
com.hardwarestore
├── config/
├── controller/
│   └── api/
├── service/
│   └── impl/
├── repository/
├── domain/
│   ├── entity/
│   └── enums/
├── dto/
│   ├── request/
│   └── response/
├── mapper/
├── exception/
└── common/
```

---

## SOLID Principles

โปรเจกต์นำหลัก SOLID มาใช้ในการออกแบบ Software ดังนี้

* **Single Responsibility Principle (SRP)**
  แยกความรับผิดชอบของ Controller, Service, Repository, DTO และ Mapper ออกจากกัน

* **Open/Closed Principle (OCP)**
  รองรับการเพิ่ม Business Rule ใหม่ผ่าน Design Pattern โดยไม่ต้องแก้ Logic หลักทั้งหมด

* **Liskov Substitution Principle (LSP)**
  Implementation ของ Interface สามารถใช้งานแทน Abstraction ได้อย่างถูกต้อง

* **Interface Segregation Principle (ISP)**
  แยก Interface ตามหน้าที่ของแต่ละ Module ไม่สร้าง Interface ที่มีขนาดใหญ่เกินไป

* **Dependency Inversion Principle (DIP)**
  Service และ Component ต่าง ๆ พึ่งพา Abstraction ผ่าน Interface และ Constructor Injection

รายละเอียดเพิ่มเติม:

* [`doc/solid-analysis.md`](doc/solid-analysis.md)

---

## Design Patterns

ระบบใช้ทั้ง Enterprise / Architectural Patterns และ Behavioral Design Patterns

### Enterprise / Architectural Patterns

* Layered Architecture
* MVC
* Repository Pattern
* Service Layer Pattern
* DTO Pattern
* Mapper Pattern
* Dependency Injection

### Behavioral Design Patterns

#### State Pattern

ใช้จัดการสถานะของ Sales Order เช่น

```text
PENDING
   │
   ├── CONFIRMED
   │      │
   │      ├── SHIPPED
   │      │      │
   │      │      └── COMPLETED
   │      │
   │      └── CANCELLED
   │
   └── CANCELLED
```

#### Strategy Pattern

ใช้สำหรับ Business Rule ที่สามารถเปลี่ยนรูปแบบการคำนวณได้ เช่น การคำนวณส่วนลด

```text
DiscountStrategy
├── NoDiscountStrategy
├── MemberDiscountStrategy
└── BulkDiscountStrategy
```

#### Observer Pattern

ใช้ตอบสนองต่อ Event ของระบบ เช่น การเปลี่ยนแปลงของ Stock หรือการตรวจสอบ Low Stock

```text
Stock Changed
     │
     ▼
StockChangedEvent
     │
     ├── Low Stock Handler
     └── Other Event Listener
```

รายละเอียดเพิ่มเติม:

* [`doc/design-patterns.md`](doc/design-patterns.md)

---

## Database Design

ระบบใช้ **PostgreSQL** เป็นฐานข้อมูลหลัก และใช้ **Spring Data JPA / Hibernate** ในการจัดการข้อมูล

### ตารางหลัก

```text
categories
products
suppliers
inventory_stocks
stock_movements
customers
sales_orders
sales_order_items
purchase_orders
purchase_items
```

### ความสัมพันธ์หลัก

```text
Category 1 ───── N Product

Product 1 ───── 1 InventoryStock

Product 1 ───── N StockMovement

Supplier 1 ───── N PurchaseOrder

PurchaseOrder 1 ───── N PurchaseItem

Customer 1 ───── N SalesOrder

SalesOrder 1 ───── N SalesOrderItem
```

### ความสัมพันธ์ที่ใช้แสดงตาม Requirement

* **One-to-One:** Product → InventoryStock
* **One-to-Many:** Category → Product
* **One-to-Many:** Supplier → PurchaseOrder
* **One-to-Many:** Customer → SalesOrder
* **One-to-Many:** Product → StockMovement
* **One-to-Many:** PurchaseOrder → PurchaseItem
* **One-to-Many:** SalesOrder → SalesOrderItem

ER Diagram:

* [`doc/diagrams/`](doc/diagrams/)

Data Dictionary:

* [`doc/data-dictionary.md`](doc/data-dictionary.md)

---

## Main Business Flow

### การรับสินค้าเข้าคลัง

```text
Create Purchase Order
        ↓
      PENDING
        ↓
   Receive Purchase
        ↓
Increase Inventory
        ↓
Create Stock Movement (IN)
        ↓
    COMPLETED
```

### การขายสินค้า

```text
Create Sales Order
        ↓
Check Product
        ↓
Check Stock
        ↓
Calculate Discount
        ↓
Create Sales Order
        ↓
Decrease Inventory
        ↓
Create Stock Movement (OUT)
```

### กรณีสินค้าไม่เพียงพอ

```text
Requested Quantity > Available Stock
                ↓
             Reject
                ↓
  InsufficientStockException
```

---

## REST API

ทุก API ใช้ Prefix:

```text
/api/v1/...
```

### Product

```http
GET    /api/v1/products
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

### Category

```http
GET    /api/v1/categories
GET    /api/v1/categories/{id}
POST   /api/v1/categories
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

### Supplier

```http
GET    /api/v1/suppliers
GET    /api/v1/suppliers/{id}
POST   /api/v1/suppliers
PUT    /api/v1/suppliers/{id}
DELETE /api/v1/suppliers/{id}
```

### Inventory

```http
GET /api/v1/inventory/products/{productId}
PUT /api/v1/inventory/products/{productId}
```

### Stock Movement

```http
GET /api/v1/stock-movements
GET /api/v1/stock-movements/products/{productId}
```

### Purchase

```http
GET  /api/v1/purchases
GET  /api/v1/purchases/{id}
POST /api/v1/purchases
PUT  /api/v1/purchases/{id}
POST /api/v1/purchases/{id}/receive
```

### Customer

```http
GET    /api/v1/customers
GET    /api/v1/customers/{id}
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

### Sales Order

```http
GET  /api/v1/orders
GET  /api/v1/orders/{id}
POST /api/v1/orders
PUT  /api/v1/orders/{id}
POST /api/v1/orders/{id}/status
```

---

## Validation และ Exception Handling

ระบบใช้ **Bean Validation** สำหรับตรวจสอบข้อมูลก่อนประมวลผล

ตัวอย่าง:

```java
@NotBlank
private String name;
```

```java
@NotNull
@Positive
private BigDecimal price;
```

```java
@PositiveOrZero
private Integer quantity;
```

ระบบมี Global Exception Handler สำหรับจัดการข้อผิดพลาดของ REST API

ตัวอย่าง Response:

```json
{
  "timestamp": "2026-10-05T10:00:00",
  "status": 404,
  "error": "PRODUCT_NOT_FOUND",
  "message": "Product with id 10 not found",
  "path": "/api/v1/products/10"
}
```

---

## API Documentation

ใช้ **Springdoc OpenAPI / Swagger UI** สำหรับดูและทดสอบ REST API

เมื่อรัน Backend สำเร็จ สามารถเข้าใช้งานได้ที่:

* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Installation & Setup

### สิ่งที่ต้องติดตั้ง

* Java 21
* Maven หรือ Maven Wrapper
* PostgreSQL
* Node.js
* Git
* Docker (สำหรับการรันผ่าน Container)

### Clone Repository

```bash
git clone <repository-url>
cd Hardware_Store_Inventory_Management_System
```

### Backend

เข้าไปยังโฟลเดอร์ Backend:

```bash
cd code/backend/hardware-store
```

ติดตั้งและ Build:

```bash
./mvnw clean install
```

รัน Spring Boot:

```bash
./mvnw spring-boot:run
```

สำหรับ Windows:

```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

### Database

กำหนดค่าการเชื่อมต่อ PostgreSQL ใน:

```text
src/main/resources/application.yml
```

ตัวอย่าง:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hardware_store
    username: postgres
    password: your_password

  jpa:
    hibernate:
      ddl-auto: validate
```

---

## How to Run

### Backend

```bash
cd code/backend/hardware-store
./mvnw spring-boot:run
```

จากนั้นเปิด:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Frontend

เมื่อพัฒนา Frontend แล้ว:

```bash
cd code/frontend
npm install
npm run dev
```

---

## How to Run Tests

เข้า Backend:

```bash
cd code/backend/hardware-store
```

รัน Test:

```bash
./mvnw test
```

สำหรับ Windows:

```bash
mvnw.cmd test
```

การทดสอบครอบคลุม:

* Unit Test
* Service Test
* Controller Test
* Validation Test
* Exception Test
* Inventory Test
* Purchase Test
* Sales Order Test
* Design Pattern Test

---

## Git Workflow

ระบบใช้โครงสร้าง Branch ดังนี้:

```text
main
  │
  └── develop
       │
       ├── peerapol_673380287-8_01
       ├── phisit_673380285-2_01
       └── peerapat_673380288-6_01
```

### Branch

แต่ละคนทำงานบน Branch ของตัวเอง และ Merge ผ่าน Pull Request ไปยัง `develop`

```text
Personal Branch
      ↓
Pull Request
      ↓
develop
      ↓
main
```

### Commit Convention

ใช้รูปแบบ:

```text
feat:
fix:
refactor:
test:
docs:
chore:
```

ตัวอย่าง:

```bash
git commit -m "feat: add inventory stock entity"
git commit -m "test: add inventory service tests"
git commit -m "fix: prevent negative stock"
```

สมาชิกแต่ละคนต้องมีอย่างน้อย **15 meaningful commits** และต้องกระจายการ Commit ตลอดระยะเวลาการพัฒนา

---

## Project Structure

```text
.

├── README.md
├── PROJECT_PLAN.md
├── progress.md
│
├── code/
│   ├── backend/
│   │   └── hardware-store/
│   │       ├── src/
│   │       │   ├── main/
│   │       │   └── test/
│   │       ├── Dockerfile
│   │       ├── pom.xml
│   │       └── ...
│   │
│   └── frontend/
│
├── test/
├── doc/
│   ├── diagrams/
│   ├── solid-analysis.md
│   ├── design-patterns.md
│   ├── data-dictionary.md
│   └── api-documentation.md
│
├── img/
│
└── project-files.json
```

---

## Documentation

รายละเอียดเพิ่มเติมของโครงการอยู่ในโฟลเดอร์ `doc/`

```text
doc/
├── diagrams/
├── solid-analysis.md
├── design-patterns.md
├── data-dictionary.md
└── api-documentation.md
```

### Diagram ที่ต้องจัดทำ

* Use Case Diagram
* Use Case Description
* Domain Model
* Class Diagram
* Sequence Diagram อย่างน้อย 3 กรณี
* Activity Diagram
* ER Diagram
* Component Diagram
* Deployment Diagram
* State Diagram

---

## Development Roadmap

```text
1. Project Setup
       ↓
2. Category / Supplier / Product
       ↓
3. Inventory / Stock Movement
       ↓
4. Purchase
       ↓
5. Customer
       ↓
6. Sales Order
       ↓
7. State / Strategy / Observer
       ↓
8. Backend Integration
       ↓
9. Backend Testing
       ↓
10. Frontend Development
       ↓
11. Frontend + Backend Integration
       ↓
12. Documentation / Diagrams
       ↓
13. Docker
       ↓
14. Deployment
       ↓
15. Final Testing
```

---

## Deployment

ในปัจจุบันระบบยังอยู่ในระหว่างการพัฒนา

### Backend

```text
Deployment URL:
ยังไม่ได้ Deploy
```

### Frontend

```text
Deployment URL:
ยังไม่ได้ Deploy
```

### Swagger

```text
Swagger URL:
ยังไม่ได้ Deploy
```

เมื่อระบบพร้อม Deploy จะเพิ่ม URL จริงในส่วนนี้

---

## Academic Requirements Checklist

| Requirement                                   | สถานะ |
| --------------------------------------------- | ----- |
| Spring Boot 3.x+                              | ✅     |
| Java 17+                                      | ✅     |
| Layered Architecture                          | ✅     |
| SOLID Principles                              | ✅     |
| Enterprise / Architectural Patterns           | ✅     |
| Behavioral Design Patterns อย่างน้อย 3 รูปแบบ | ⬜     |
| One-to-One Relationship                       | ✅     |
| One-to-Many Relationship                      | ✅     |
| Database อย่างน้อย 6 ตาราง                    | ✅     |
| RESTful API                                   | ✅     |
| CRUD อย่างน้อย 2 Resources                    | ✅     |
| Validation                                    | ✅     |
| Global Exception Handler                      | ✅     |
| Pagination / Sorting                          | ⬜     |
| Swagger / OpenAPI                             | ✅     |
| JUnit 5                                       | ✅     |
| Mockito                                       | ✅     |
| Git / GitHub                                  | ✅     |
| Pull Request + Reviewer                       | ⬜     |
| 15+ Meaningful Commits / คน                   | ⬜     |
| Dockerfile                                    | ⬜     |
| docker-compose.yml                            | ⬜     |
| Cloud Deployment                              | ⬜     |
| Public URL                                    | ⬜     |
| Required Diagrams                             | ⬜     |

---

## สถานะโครงการ

โครงการอยู่ในระหว่างการพัฒนา โดยเริ่มจากการจัดเตรียม **Spring Boot Backend** และแบ่งการพัฒนาออกเป็นโมดูลตามสมาชิกในทีม ได้แก่ Product, Supplier, Category, Inventory, Stock Movement, Purchase, Customer และ Sales Order

หลังจาก Backend ของแต่ละโมดูลเสร็จ จะดำเนินการรวมระบบ ทดสอบการทำงานร่วมกัน พัฒนา Frontend เชื่อมต่อ REST API จัดทำเอกสารและ Diagram รวมถึง Docker และ Deployment ตามลำดับ
