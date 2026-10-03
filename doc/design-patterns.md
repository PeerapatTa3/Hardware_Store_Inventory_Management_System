# Design Patterns

## Layered Architecture
- แยกชั้น controller, service, repository, domain, dto, mapper อย่างชัดเจน

## Repository Pattern
- `CategoryRepository`, `SupplierRepository`, `ProductRepository` ใช้ Spring Data JPA

## DTO Pattern + Mapper
- `CategoryRequest`, `CategoryResponse`, `SupplierRequest`, `SupplierResponse`, `ProductRequest`, `ProductResponse`
- Mapper แปลงข้อมูลระหว่าง entity กับ API contract

## Dependency Injection
- ใช้ constructor injection ผ่าน `@RequiredArgsConstructor`

## Behavioral Pattern (สำหรับต่อยอด)
- Strategy: เหมาะใช้ในการคำนวณ discount หรือ pricing logic
- State: เหมาะใช้สำหรับ Sales Order status
- Observer: เหมาะใช้ในการแจ้งเตือน low stock หรือ event-driven notification

## Conclusion
- โครงสร้างปัจจุบันมี pattern ที่จำเป็นสำหรับระบบพัฒนาแบบ enterprise และพร้อมต่อยอดไปยัง Sales Order / Inventory ต่อไป
