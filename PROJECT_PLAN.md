# Hardware Store Inventory Management System

## 1. Project Overview

**Hardware Store Inventory Management System** คือระบบจัดการสินค้าคงคลังสำหรับร้านฮาร์ดแวร์ พัฒนาด้วย Spring Boot โดยมี REST API เป็น Backend และ Frontend สำหรับใช้งานผ่านระบบ

ระบบรองรับการจัดการสินค้า หมวดหมู่สินค้า Supplier ลูกค้า สินค้าคงคลัง การเคลื่อนไหวของสต็อก การสั่งซื้อสินค้าเข้า และการขายสินค้าออก โดยออกแบบตาม Layered Architecture และนำ SOLID Principles รวมถึง Design Patterns มาใช้งานจริง

### เป้าหมายของโครงการ

* จัดการข้อมูลสินค้าและหมวดหมู่
* จัดการ Supplier และ Purchase Order
* ตรวจสอบและปรับปรุงจำนวนสินค้าในคลัง
* บันทึกประวัติการเคลื่อนไหวของสินค้า
* จัดการ Customer และ Sales Order
* ตรวจสอบจำนวนสินค้าไม่ให้ขายเกิน Stock
* รองรับการทำงานผ่าน REST API
* พัฒนาระบบตามหลัก Software Design ที่เรียนในรายวิชา

---

# 2. Technology Stack

## Backend

* Java 17+ / Java 21
* Spring Boot 3.x
* Maven
* Spring Web
* Spring Data JPA
* Hibernate
* Bean Validation
* PostgreSQL
* Flyway
* Lombok
* Springdoc OpenAPI / Swagger
* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc

## Frontend

* React
* JavaScript / TypeScript
* HTML / CSS

## Development Tools

* Git
* GitHub
* VS Code / IntelliJ IDEA
* Postman
* Swagger UI

## Deployment

* Docker
* Docker Compose
* Cloud Platform
* PostgreSQL Cloud Database

---

# 3. Project Structure

โครงสร้าง Repository หลัก:

```text
Hardware_Store_Inventory_Management_System/
│
├── README.md
│
├── code/
│   │
│   ├── backend/
│   │   └── hardware-store/
│   │       ├── .mvn/
│   │       ├── .vscode/
│   │       ├── src/
│   │       │   ├── main/
│   │       │   │   ├── java/
│   │       │   │   │   └── com/
│   │       │   │   │       └── hardwarestore/
│   │       │   │   │           ├── config/
│   │       │   │   │           ├── controller/
│   │       │   │   │           │   └── api/
│   │       │   │   │           ├── service/
│   │       │   │   │           │   └── impl/
│   │       │   │   │           ├── repository/
│   │       │   │   │           ├── domain/
│   │       │   │   │           │   ├── entity/
│   │       │   │   │           │   └── enums/
│   │       │   │   │           ├── dto/
│   │       │   │   │           │   ├── request/
│   │       │   │   │           │   └── response/
│   │       │   │   │           ├── mapper/
│   │       │   │   │           ├── exception/
│   │       │   │   │           └── common/
│   │       │   │   └── resources/
│   │       │   │       ├── application.yml
│   │       │   │       └── db/
│   │       │   │           └── migration/
│   │       │   └── test/
│   │       │
│   │       ├── pom.xml
│   │       ├── mvnw
│   │       └── mvnw.cmd
│   │
│   └── frontend/
│
├── test/
│
├── doc/
│   ├── diagrams/
│   ├── solid-analysis.md
│   ├── design-patterns.md
│   ├── data-dictionary.md
│   └── api-documentation.md
│
└── img/
```

> `hardware-store/` คือ Spring Boot project ที่สร้างจาก Spring Initializr ในปัจจุบัน

---

# 4. System Architecture

ระบบใช้ **Layered Architecture**

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

### หน้าที่ของแต่ละ Layer

### Controller

รับ HTTP Request จาก Frontend และส่ง Response กลับ

Controller ไม่ควรมี Business Logic และไม่เรียก Repository โดยตรง

```text
Controller → Service
```

### Service

จัดการ Business Logic ของระบบ

```text
Service → Repository
```

### Repository

รับผิดชอบการเข้าถึง Database ผ่าน Spring Data JPA

### Entity

แทนข้อมูลและความสัมพันธ์ใน Database

### DTO

ใช้รับและส่งข้อมูลระหว่าง Client กับ Backend โดยไม่เปิดเผย Entity โดยตรง

### Mapper

แปลงระหว่าง Entity และ DTO

---

# 5. Database Design

ระบบจะมีอย่างน้อย 9–10 ตารางหลัก

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

## Relationships

### Category → Product

```text
Category 1 ───── N Product
```

หนึ่ง Category มีสินค้าได้หลายรายการ

### Product → InventoryStock

```text
Product 1 ───── 1 InventoryStock
```

ใช้เป็นความสัมพันธ์ One-to-One ตาม Requirement ของวิชา

### Product → StockMovement

```text
Product 1 ───── N StockMovement
```

### Supplier → PurchaseOrder

```text
Supplier 1 ───── N PurchaseOrder
```

### PurchaseOrder → PurchaseItem

```text
PurchaseOrder 1 ───── N PurchaseItem
```

### Customer → SalesOrder

```text
Customer 1 ───── N SalesOrder
```

### SalesOrder → SalesOrderItem

```text
SalesOrder 1 ───── N SalesOrderItem
```

### Product → PurchaseItem

```text
Product 1 ───── N PurchaseItem
```

### Product → SalesOrderItem

```text
Product 1 ───── N SalesOrderItem
```

---

# 6. Main Entities

## Category

```text
id
name
description
created_at
```

## Product

```text
id
sku
name
description
unit
price
cost_price
minimum_stock
category_id
supplier_id
created_at
updated_at
```

## Supplier

```text
id
name
phone
email
address
created_at
```

## InventoryStock

```text
id
product_id
quantity
reserved_quantity
updated_at
```

## StockMovement

```text
id
product_id
type
quantity
reference_type
reference_id
reason
created_at
```

ประเภท Movement:

```text
IN
OUT
ADJUSTMENT
```

## PurchaseOrder

```text
id
purchase_number
supplier_id
status
total_amount
created_at
```

## PurchaseItem

```text
id
purchase_order_id
product_id
quantity
unit_cost
subtotal
```

## Customer

```text
id
name
phone
email
address
created_at
```

## SalesOrder

```text
id
order_number
customer_id
status
total_amount
created_at
```

## SalesOrderItem

```text
id
sales_order_id
product_id
quantity
unit_price
subtotal
```

---

# 7. Main Business Flows

## 7.1 Product Management

```text
Frontend
   ↓
ProductController
   ↓
ProductService
   ↓
ProductRepository
   ↓
PostgreSQL
```

รองรับ:

* Create Product
* Get Product
* Get Product by ID
* Update Product
* Delete Product
* Search Product
* Search by Category
* Search by SKU
* Pagination
* Sorting

---

# 8. Inventory Flow

เมื่อมีการเปลี่ยนจำนวนสินค้า ต้องสร้าง Stock Movement ด้วย

```text
Inventory Change
      │
      ├── Update InventoryStock
      │
      └── Create StockMovement
```

ตัวอย่างรับสินค้า:

```text
Purchase Received
      ↓
InventoryStock + Quantity
      ↓
StockMovement = IN
```

ตัวอย่างขายสินค้า:

```text
Sales Order
      ↓
Check Stock
      ↓
InventoryStock - Quantity
      ↓
StockMovement = OUT
```

---

# 9. Purchase Flow

```text
Create Purchase Order
        ↓
PENDING
        ↓
Confirm Receive
        ↓
Update InventoryStock
        ↓
Create StockMovement(IN)
        ↓
COMPLETED
```

การยืนยันรับสินค้าควรใช้ Transaction:

```java
@Transactional
```

เพื่อป้องกันกรณีที่ Inventory สำเร็จแต่ StockMovement ไม่สำเร็จ หรือกลับกัน

---

# 10. Sales Flow

```text
Create Sales Order
       ↓
Check Product
       ↓
Check Stock
       ↓
Calculate Price / Discount
       ↓
Create Sales Order
       ↓
Decrease Inventory
       ↓
Create StockMovement(OUT)
```

ห้ามขายสินค้าหากจำนวน Stock ไม่เพียงพอ

---

# 11. Design Patterns

ใช้ Design Pattern กลุ่ม **Behavioral**

อย่างน้อย 3 Pattern

## 11.1 State Pattern

ใช้จัดการสถานะของ Sales Order

ตัวอย่าง:

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

แทนการเขียน `if-else` จำนวนมากสำหรับ State Transition

---

# 12. Strategy Pattern

ใช้สำหรับการคำนวณส่วนลดหรือราคาขาย

ตัวอย่าง:

```text
DiscountStrategy
      │
      ├── NoDiscountStrategy
      ├── MemberDiscountStrategy
      └── BulkDiscountStrategy
```

Service สามารถเลือก Strategy ที่เหมาะสมได้

ข้อดีคือสามารถเพิ่มวิธีคำนวณส่วนลดใหม่โดยไม่ต้องแก้ Business Logic หลักมากนัก

---

# 13. Observer Pattern

ใช้แจ้งเหตุการณ์เมื่อ Stock เปลี่ยนแปลงหรือ Stock ต่ำกว่ากำหนด

ตัวอย่าง:

```text
InventoryStock Changed
        ↓
StockChangedEvent
        ↓
Event Listeners
        ├── LowStockNotification
        └── Audit / Stock Logger
```

สามารถใช้ Spring:

```text
ApplicationEvent
@EventListener
```

---

# 14. SOLID Principles

## Single Responsibility Principle

แยกความรับผิดชอบ:

```text
Controller → HTTP
Service → Business Logic
Repository → Database
Mapper → DTO Mapping
```

## Open/Closed Principle

ใช้ Strategy และ State เพื่อให้สามารถเพิ่มพฤติกรรมใหม่โดยไม่ต้องแก้ Logic เดิมจำนวนมาก

## Liskov Substitution Principle

Implementation ของ interface ต้องสามารถนำมาแทน interface ได้โดยไม่ทำให้ระบบผิดพฤติกรรม

## Interface Segregation Principle

ไม่สร้าง Service interface ขนาดใหญ่เกินไป

ตัวอย่าง:

```text
ProductService
InventoryService
PurchaseService
SalesOrderService
CustomerService
```

## Dependency Inversion Principle

Service พึ่งพา abstraction:

```text
Controller
   ↓
Service Interface
   ↑
Service Implementation
```

ใช้ Constructor Injection

---

# 15. REST API

ทุก Endpoint ต้องใช้:

```text
/api/v1/...
```

## Product

```http
GET    /api/v1/products
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

## Category

```http
GET    /api/v1/categories
GET    /api/v1/categories/{id}
POST   /api/v1/categories
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

## Supplier

```http
GET    /api/v1/suppliers
GET    /api/v1/suppliers/{id}
POST   /api/v1/suppliers
PUT    /api/v1/suppliers/{id}
DELETE /api/v1/suppliers/{id}
```

## Inventory

```http
GET /api/v1/inventory/products/{productId}
PUT /api/v1/inventory/products/{productId}
```

## Stock Movement

```http
GET /api/v1/stock-movements
GET /api/v1/stock-movements/products/{productId}
```

## Purchase

```http
GET  /api/v1/purchases
GET  /api/v1/purchases/{id}
POST /api/v1/purchases
PUT  /api/v1/purchases/{id}
POST /api/v1/purchases/{id}/receive
```

## Customer

```http
GET    /api/v1/customers
GET    /api/v1/customers/{id}
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

## Sales Order

```http
GET  /api/v1/orders
GET  /api/v1/orders/{id}
POST /api/v1/orders
PUT  /api/v1/orders/{id}
POST /api/v1/orders/{id}/status
```

---

# 16. Validation

Request DTO ต้องตรวจสอบข้อมูลด้วย Bean Validation

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

Controller ใช้:

```java
@Valid
@RequestBody
```

---

# 17. Exception Handling

ใช้ Global Exception Handler กลางของระบบ

```text
exception/
├── ResourceNotFoundException
├── DuplicateSkuException
├── InsufficientStockException
└── GlobalExceptionHandler
```

Response รูปแบบมาตรฐาน:

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

# 18. Swagger / OpenAPI

Backend ต้องสามารถเปิด Swagger UI ได้

Swagger ใช้สำหรับ:

* ดู API
* ทดลองเรียก API
* ตรวจ Request / Response
* ใช้ทดสอบ Backend ก่อนต่อ Frontend

---

# 19. Team Work Distribution

ทีมมี 3 คน

## Member 1

รับผิดชอบ:

```text
Project Setup
Exception / Global Error
Swagger / OpenAPI
Category
Supplier
Product
```

รวมถึง:

* Base project structure
* Database migration structure
* Docker setup
* Shared configuration

---

## Member 2

รับผิดชอบ:

```text
Inventory
StockMovement
Purchase
```

### Inventory

```text
Product 1 : 1 InventoryStock
```

### StockMovement

บันทึก:

```text
IN
OUT
ADJUSTMENT
```

### Purchase

```text
PurchaseOrder 1 : N PurchaseItem
```

เมื่อ Receive:

```text
Purchase
   ↓
Inventory
   ↓
StockMovement
```

---

## Member 3

รับผิดชอบ:

```text
Customer
Sales Order
Behavioral Design Patterns
```

Pattern:

```text
State
Strategy
Observer
```

Sales Order:

```text
Customer 1 : N SalesOrder
SalesOrder 1 : N SalesOrderItem
```

---

# 20. Dependency Between Members

ลำดับความสัมพันธ์ของงาน:

```text
Member 1
Category
Product
Supplier
    │
    ├──────────────┐
    ↓              ↓
Member 2       Member 3
Inventory      Customer
Purchase       Sales Order
StockMovement
```

### ลำดับที่แนะนำ

```text
1. Member 1 สร้าง Project Skeleton
       ↓
2. Member 1 ทำ Category / Product / Supplier
       ↓
3. Merge เข้า develop
       ↓
4. Member 2 ทำ Inventory / StockMovement / Purchase
5. Member 3 ทำ Customer
       ↓
6. Member 2 Merge Inventory
       ↓
7. Member 3 ทำ Sales Order
       ↓
8. Integration ระหว่างทุก Module
```

---

# 21. Frontend

Frontend แยกจาก Spring Boot

```text
code/
├── backend/
│   └── hardware-store/
│
└── frontend/
    └── React Application
```

Frontend จะเรียก Backend ผ่าน REST API

ตัวอย่าง:

```text
React
   │
   │ GET /api/v1/products
   ▼
Spring Boot
   │
   ▼
PostgreSQL
```

แต่ละคนรับผิดชอบ Frontend ของ Module ตัวเอง

```text
Member 1 → Product / Category / Supplier UI
Member 2 → Inventory / Purchase UI
Member 3 → Customer / Sales UI
```

---

# 22. Testing

ทุก Module ต้องมี Test

## Unit Test

เน้น Service

```text
ProductService
InventoryService
PurchaseService
SalesOrderService
```

ใช้:

```text
JUnit 5
Mockito
```

## Controller Test

ใช้:

```text
MockMvc
```

ทดสอบ:

* HTTP Method
* Status Code
* Request Validation
* Response
* Exception

---

# 23. Important Test Cases

## Inventory

```text
Get existing product stock
Get non-existing product stock
Increase stock
Decrease stock
Reject insufficient stock
Adjust stock
```

## Purchase

```text
Create purchase
Create purchase with multiple items
Receive purchase
Increase stock after receive
Create StockMovement(IN)
Reject invalid purchase
```

## Sales

```text
Create sales order
Check sufficient stock
Reject insufficient stock
Decrease stock
Create StockMovement(OUT)
Calculate discount
Change order state
Reject invalid state transition
```

---

# 24. Database Migration

ใช้ Flyway

ตัวอย่าง:

```text
src/main/resources/db/migration/
├── V1__create_categories.sql
├── V2__create_suppliers.sql
├── V3__create_products.sql
├── V4__create_inventory_stocks.sql
├── V5__create_stock_movements.sql
├── V6__create_purchase_orders.sql
├── V7__create_purchase_items.sql
├── V8__create_customers.sql
├── V9__create_sales_orders.sql
└── V10__create_sales_order_items.sql
```

แต่ละคนเพิ่ม Migration ของตารางตัวเอง โดยต้องระวังลำดับ Foreign Key

---

# 25. Git Workflow

Branch หลัก:

```text
main
  ↓
develop
  ↓
personal branch
```

ตัวอย่าง:

```text
develop
   │
   ├── member1_xxxxx_01
   ├── member2_xxxxx_01
   └── member3_xxxxx_01
```

Branch ของแต่ละคนต้องใช้รูปแบบที่อาจารย์กำหนด:

```text
ชื่อ_รหัสนักศึกษา_section
```

---

# 26. Git Working Rules

ก่อนเริ่มงาน:

```bash
git checkout develop
git pull
git checkout <your-branch>
git merge develop
```

หลังทำงาน:

```bash
git add <files>
git commit -m "feat: add inventory stock service"
git push
```

เปิด Pull Request:

```text
Personal Branch
      ↓
develop
```

ต้องมี Reviewer อย่างน้อย 1 คน

ไม่ Push ตรงเข้า `main`

---

# 27. Commit Convention

ใช้:

```text
feat:
fix:
refactor:
test:
docs:
chore:
```

ตัวอย่าง:

```text
feat: add inventory stock entity
feat: add inventory stock service
test: add inventory service tests
fix: prevent negative stock
docs: add inventory api documentation
```

แต่ละคนต้องมีอย่างน้อย **15 meaningful commits**

Commit ควรกระจายตลอดระยะเวลาพัฒนา

---

# 28. Documentation

## Required Documents

```text
doc/
├── solid-analysis.md
├── design-patterns.md
├── data-dictionary.md
├── api-documentation.md
└── diagrams/
```

## Required Diagrams

* Use Case Diagram
* Use Case Description
* Domain Model
* Class Diagram
* Sequence Diagram อย่างน้อย 3
* Activity Diagram
* ER Diagram
* Component Diagram
* Deployment Diagram
* State Diagram

---

# 29. Recommended Sequence Diagrams

## Scenario 1 — Create Product

```text
Frontend
 → ProductController
 → ProductService
 → ProductRepository
 → Database
```

## Scenario 2 — Receive Purchase

```text
Frontend
 → PurchaseController
 → PurchaseService
 → InventoryService
 → StockMovementService
 → Database
```

## Scenario 3 — Create Sales Order

```text
Frontend
 → SalesOrderController
 → SalesOrderService
 → InventoryService
 → Strategy
 → SalesOrderRepository
 → StockMovementService
 → Database
```

---

# 30. Docker

ระบบต้องเตรียม:

```text
Dockerfile
docker-compose.yml
```

Architecture:

```text
React Container
      │
      ▼
Spring Boot Container
      │
      ▼
PostgreSQL
```

---

# 31. Deployment

เป้าหมายสุดท้าย:

```text
GitHub
   ↓
Build
   ↓
Test
   ↓
Docker
   ↓
Cloud
   ↓
Public URL
```

Backend ต้องสามารถเข้าถึงได้จริง

ตัวอย่าง:

```text
https://xxxxx.example.com
```

Swagger:

```text
https://xxxxx.example.com/swagger-ui.html
```

---

# 32. Development Roadmap

## Phase 1 — Project Setup

ทุกคนเตรียม Repository และ Environment

### Member 1

* Spring Boot
* Maven
* PostgreSQL
* Base Package
* Exception
* Swagger
* Git structure

### ทุกคน

* Clone Repository
* Setup branch
* ตรวจสอบการ Run Project

---

## Phase 2 — Core CRUD

### Member 1

```text
Category
Supplier
Product
```

### Member 3

```text
Customer
```

---

## Phase 3 — Inventory System

### Member 2

```text
InventoryStock
StockMovement
```

---

## Phase 4 — Purchase

### Member 2

```text
PurchaseOrder
PurchaseItem
Receive Purchase
Inventory Integration
StockMovement Integration
```

---

## Phase 5 — Sales

### Member 3

```text
SalesOrder
SalesOrderItem
Stock Validation
Inventory Integration
StockMovement Integration
```

---

## Phase 6 — Design Patterns

### Member 3

```text
State
Strategy
Observer
```

นำไปใช้กับ Business Logic จริง

---

## Phase 7 — Frontend

ทุกคนทำ UI ของ Module ตัวเอง

```text
Member 1
→ Product / Category / Supplier

Member 2
→ Inventory / Purchase

Member 3
→ Customer / Sales
```

---

## Phase 8 — Integration

รวมระบบทั้งหมด:

```text
Product
   ↓
Inventory
   ↓
Purchase

Product
   ↓
Inventory
   ↓
Sales

Customer
   ↓
Sales Order
```

ทดสอบ End-to-End Flow

---

## Phase 9 — Testing

รวม:

```text
Unit Test
Controller Test
Integration Test
Validation Test
Exception Test
Business Logic Test
```

---

## Phase 10 — Documentation

ทำ:

```text
ER Diagram
Class Diagram
Sequence Diagram
Activity Diagram
Component Diagram
Deployment Diagram
State Diagram
SOLID Analysis
Design Pattern Analysis
Data Dictionary
API Documentation
README
```

---

## Phase 11 — Docker & Deployment

```text
Dockerfile
docker-compose.yml
Environment Variables
Cloud Database
Cloud Backend
Public URL
Swagger
```

---

## Phase 12 — Final Verification

ตรวจสอบ Requirement ของวิชาอีกครั้ง:

```text
[ ] Spring Boot 3.x+
[ ] Java 17+
[ ] Layered Architecture
[ ] SOLID
[ ] Enterprise Patterns
[ ] 3 GoF Behavioral Patterns
[ ] One-to-One
[ ] One-to-Many
[ ] At least 6 tables
[ ] REST API
[ ] CRUD >= 2 resources
[ ] Validation
[ ] Global Exception Handler
[ ] Pagination / Sorting
[ ] Swagger
[ ] JUnit 5
[ ] Mockito
[ ] Git / GitHub
[ ] 15+ meaningful commits/person
[ ] PR + Reviewer
[ ] Dockerfile
[ ] docker-compose.yml
[ ] Cloud Deployment
[ ] Public URL
[ ] Required Diagrams
[ ] README
```

---

# 33. Definition of Done

Feature หนึ่งจะถือว่าเสร็จเมื่อมีครบ:

```text
Entity
   ↓
Repository
   ↓
Service Interface
   ↓
Service Implementation
   ↓
DTO
   ↓
Mapper
   ↓
Controller
   ↓
Validation
   ↓
Exception Handling
   ↓
Test
   ↓
Documentation
```

ดังนั้นเพียงแค่สร้าง Controller แล้ว API ใช้งานได้ **ยังไม่ถือว่า Feature เสร็จสมบูรณ์**

---

# 34. Definition of Backend Complete

Backend ของทั้งโปรเจกต์จะถือว่าเสร็จเมื่อ:

```text
All Modules Completed
        ↓
All APIs Working
        ↓
All Business Logic Working
        ↓
Database Relationships Correct
        ↓
Validation + Exception Handling
        ↓
Swagger
        ↓
Unit Tests
        ↓
Controller / Integration Tests
        ↓
Frontend Integration
        ↓
Docker
        ↓
Deployment
        ↓
Publicly Accessible API
```

---

# 35. Final System

ระบบสุดท้ายควรมีภาพรวมดังนี้:

```text
                    Frontend
                      │
              React Web Application
                      │
                  REST API
                      │
                      ▼
             ┌─────────────────┐
             │   Spring Boot   │
             │                 │
             │   Controller    │
             │       ↓         │
             │     Service     │
             │       ↓         │
             │    Repository   │
             │       ↓         │
             │      JPA        │
             └────────┬────────┘
                      │
                      ▼
                  PostgreSQL
```

Business Flow:

```text
Supplier
   ↓
Purchase Order
   ↓
Inventory
   ↓
Stock Movement
   ↓
Product
   ↓
Sales Order
   ↑
Customer
```

และ Design Pattern:

```text
Sales Order
    │
    ├── State
    │
    ├── Strategy
    │
    └── Observer
```

---

# 36. Final Goal

เมื่อพัฒนาเสร็จ ระบบต้องสามารถแสดงให้เห็นว่า:

1. สามารถจัดการข้อมูลหลักของร้านฮาร์ดแวร์ผ่าน REST API ได้
2. สามารถเพิ่มสินค้าเข้าคลังผ่าน Purchase ได้
3. สามารถตรวจสอบและปรับจำนวนสินค้าได้
4. สามารถบันทึก Stock Movement ได้
5. สามารถสร้าง Sales Order และตัด Stock ได้
6. ไม่สามารถขายเกินจำนวน Stock
7. สามารถจัดการสถานะ Order ด้วย State Pattern
8. สามารถคำนวณส่วนลดด้วย Strategy Pattern
9. สามารถแจ้งเหตุการณ์เมื่อ Stock เปลี่ยนแปลงด้วย Observer Pattern
10. ระบบมี Validation, Exception Handling และ Swagger
11. ระบบมี Unit Test และ Controller Test
12. ระบบสามารถเชื่อมกับ Frontend ได้
13. ระบบสามารถ Deploy และเข้าถึงผ่าน Public URL ได้

---

# 37. Recommended Development Order

ลำดับที่แนะนำสำหรับทีม:

```text
                   START
                     │
                     ▼
              Spring Boot Setup
                     │
                     ▼
          Member 1: Category
                     │
                     ▼
          Member 1: Product
                     │
                     ▼
          Member 1: Supplier
                     │
             ┌───────┴────────┐
             ▼                ▼
       Member 2             Member 3
       Inventory            Customer
             │                │
             ▼                │
       StockMovement           │
             │                │
             ▼                │
         Purchase             │
             │                │
             └───────┬────────┘
                     ▼
                Sales Order
                     │
                     ▼
             Design Patterns
          State / Strategy / Observer
                     │
                     ▼
                Integration
                     │
                     ▼
                  Testing
                     │
                     ▼
                 Frontend
                     │
                     ▼
              Docker / Compose
                     │
                     ▼
                 Deployment
                     │
                     ▼
               Final Project
```

**หลักสำคัญของแผนนี้คือ:** Backend จะพัฒนาก่อนเป็นแกนหลัก → แต่ละ Module เชื่อมกัน → ค่อยทำ Frontend Integration → ทดสอบทั้งระบบ → Docker → Deploy → ตรวจ Requirement ก่อนส่ง
