# SOLID Analysis

## Single Responsibility Principle
- `CategoryServiceImpl` รับผิดชอบ business logic ของ Category เท่านั้น
- `CategoryMapper` รับผิดชอบแปลง DTO และ Entity เท่านั้น
- `CategoryController` รับผิดชอบ HTTP request/response เท่านั้น

## Open/Closed Principle
- โครงสร้าง service layer เขียนแบบ interface + implementation เพื่อให้เพิ่ม feature ใหม่ได้โดยไม่ต้องแก้โค้ดเดิมมาก
- `ProductService` และ `SupplierService` ใช้ interface ที่ stabilizes boundary

## Liskov Substitution Principle
- `CategoryServiceImpl` และ `SupplierServiceImpl` ใช้ interface เดียวกัน จึงสามารถถูกแทนที่ได้โดยไม่มีผลต่อการทำงานของ controller

## Interface Segregation Principle
- `CategoryService`, `SupplierService`, `ProductService` แยกตามหน้าที่ไม่รวม logic เข้า interface เดียว

## Dependency Inversion Principle
- Controller ขึ้นกับ `CategoryService`, `SupplierService`, `ProductService` interface ไม่ใช่ concrete class
- Service impl ขึ้นกับ Repository interface

## Result
- โครงการนี้มีพื้นฐานตามหลัก SOLID โดยแยกชั้น service, repository, mapper, controller และ exception อย่างชัดเจน
