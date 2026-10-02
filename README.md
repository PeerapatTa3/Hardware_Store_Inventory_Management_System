# Hardware Store Inventory Management System

ระบบจัดการคลังสินค้าของร้านอุปกรณ์การช่าง โดยมีฟังก์ชันหลักด้านสินค้า หมวดหมู่ ผู้จำหน่าย คลังสินค้า และ API สำหรับจัดการข้อมูลแบบ RESTful

## สมาชิกกลุ่ม

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---|---|---:|---:|---|---|
| 1 | นายพิสิษฐ์ ทรัพย์อุดมโชติ | 673380285-2 | 01 | `peerapol_673380287-8_01` | Member 1: Backend skeleton, Category, Supplier, Product |
| 2 | นายพีรพล แก้วเจริญสันติสุข | 673380287-8 | 01 | `peerapol_673380287-8_01` | Inventory, Stock Movement, Purchase |
| 3 | นายพีรพัฒน์ แท่นประยุทร | 673380288-6 | 01 | `peerapol_673380287-8_01` | Customer, Sales Order, Design Pattern |

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (development)
- PostgreSQL ready for production
- Maven
- Springdoc OpenAPI / Swagger UI
- JUnit 5 + Mockito

## System Architecture

แสดงตาม Layered Architecture ดังนี้

- Presentation Layer: Controller / REST API
- Service Layer: Business logic และ validation
- Repository Layer: JPA repositories
- Domain Layer: Entity / Enum / DTO / Mapper
- Config / Exception: OpenAPI, GlobalExceptionHandler

## Database Design

โครงสร้างหลักมีความสัมพันธ์ดังนี้

- Category 1:N Product
- Supplier 1:N Product
- Product 1:1 InventoryStock (ในเวอร์ชันต่อไป)
- Customer 1:N SalesOrder
- SalesOrder 1:N SalesOrderItem
- Supplier 1:N PurchaseOrder

## Installation & Setup

```bash
cd hardware-store
./mvnw clean install
./mvnw spring-boot:run
```

## How to Run

1. เปิด Terminal แล้วเข้าโฟลเดอร์ `hardware-store`
2. รันคำสั่งด้านบน
3. เปิด Swagger UI ที่
   - http://localhost:8080/swagger-ui.html
4. H2 Console:
   - http://localhost:8080/h2-console

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## How to Run Tests

```bash
cd hardware-store
./mvnw test
```

## Deployment URL

ยังไม่ได้ deploy จริงในเวอร์ชันปัจจุบัน ต้องเตรียมในขั้นตอนต่อไป

## Project Structure

```text
hardware-store/
├── src/main/java/com/hardwarestore
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   ├── dto/
│   ├── mapper/
│   ├── exception/
│   └── ...
├── src/main/resources/
├── src/test/java/
├── Dockerfile
├── pom.xml
└── README.md
```

## Notes

โครงสร้างนี้อยู่ในระหว่างการพัฒนาเพื่อให้สอดคล้องกับ requirement ของ Member 1 และต่อยอดไปสู่ Inventory / Purchase / Sales Order ในภายหลัง

