# Member 2 — Test Inventory

เอกสารนี้รวบรวม test cases ที่ Member 2 เพิ่มหรือปรับในงาน Inventory, Stock Movement และ Purchase โดยตรวจเทียบกับ test files และ commits ใน Git history

## ภาพรวม

- มี test case ใหม่ **48 รายการ** และปรับ test เดิม **1 รายการ** ในชุดงาน Member 2
- Regression test ล่าสุดหลังเพิ่ม Stock Movement read API และ controller/integration tests ผ่าน **63 tests, 0 failures, 0 errors**
- ตารางด้านล่างแสดง commit, test file, test method และสิ่งที่ทดสอบ

## รายการ Test Cases

| Commit | Test file / method | สิ่งที่ทดสอบ |
|---|---|---|
| `268b6d9` — `fix: synchronize inventory adjustment movements` | `InventoryStockServiceImplTest.adjustStockShouldUpdateQuantity` *(ปรับ test เดิม)* | ปรับ Inventory แล้วต้องเรียก Stock Movement service ด้วย Product ID, `ADJUSTMENT`, ยอดเป้าหมาย และเหตุผลที่ถูกต้อง และไม่บันทึก Inventory ซ้ำโดยตรงจาก Inventory service |
| `268b6d9` — `fix: synchronize inventory adjustment movements` | `StockMovementServiceImplTest.create_shouldSetStockToTargetForAdjustmentMovement` | Movement `ADJUSTMENT` ตั้ง stock เป็นยอดเป้าหมาย บันทึก Inventory และเก็บ movement type, quantity และ note ตาม request |
| `268b6d9` — `fix: synchronize inventory adjustment movements` | `StockMovementServiceImplTest.create_shouldRejectAdjustmentBelowReservedQuantity` | ปฏิเสธยอดเป้าหมายที่ต่ำกว่า reserved quantity โดยไม่เปลี่ยน stock และไม่บันทึก Inventory หรือ movement |
| `2970c37` — `feat: add purchase persistence model` | `PurchasePersistenceTest.savePurchaseOrderShouldPersistStatusSupplierAndItems` | บันทึกและโหลด PurchaseOrder จากฐานข้อมูล ตรวจสถานะเริ่มต้น `PENDING`, created time, Supplier, PurchaseItem, Product และ subtotal |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderRequestValidationTest.shouldRejectMissingSupplierAndEmptyItems` | ปฏิเสธ request ที่ไม่มี Supplier ID และมีรายการว่าง |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderRequestValidationTest.shouldRejectInvalidNestedPurchaseItemValues` | ปฏิเสธ Supplier/Product ID, quantity และ unit cost ที่ไม่เป็นค่าบวก รวมถึงตรวจ nested item validation |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderRequestValidationTest.shouldAcceptValidPurchaseOrderRequest` | ยอมรับ Purchase request ที่มี Supplier และรายการสินค้าซึ่งมีค่าถูกต้อง |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderRequestValidationTest.shouldRejectNullPurchaseItem` | ปฏิเสธ item ที่เป็น `null` ในรายการ `items` และมี validation error ที่ตำแหน่ง item |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderMapperTest.toEntityShouldMapItemsAndCalculateTotals` | Map Supplier/Product และรายการสินค้า คำนวณ subtotal รายบรรทัดและ total รวม ตั้งสถานะเริ่มต้น `PENDING` และเชื่อม item กลับไปยัง order |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderMapperTest.toEntityShouldRejectUnresolvedProduct` | ปฏิเสธ Product ID ที่ไม่สามารถ resolve เป็น Product ได้ และระบุ ID ใน error |
| `74d2244` — `feat: add purchase request and response models` | `PurchaseOrderMapperTest.toResponseShouldMapOrderAndItems` | Map order, Supplier และ item fields เช่น Product name และ subtotal ไปยัง response DTO |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.createShouldResolveReferencesGenerateNumberAndSaveOrder` | ตรวจการ resolve Supplier/Products, สร้าง purchase number, ลดการ lookup Product ซ้ำ, บันทึก order และคืน mapped response |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.createShouldFailWhenSupplierDoesNotExist` | เมื่อไม่พบ Supplier ให้ throw `ResourceNotFoundException` และไม่ทำขั้นตอน resolve Product, map หรือ save order ต่อ |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.createShouldFailWhenProductDoesNotExist` | เมื่อไม่พบ Product ให้ throw `ResourceNotFoundException` และไม่ map หรือ save order |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.findAllShouldMapOrdersInCreatedDateDescendingOrder` | โหลดรายการ Purchase โดยเรียง created date จากใหม่ไปเก่า และ map เป็น response |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.findByIdShouldMapPurchaseOrder` | คืน mapped response เมื่อค้นพบ Purchase ตาม ID |
| `b41be12` — `feat: add purchase creation and lookup` | `PurchaseOrderServiceImplTest.findByIdShouldThrowWhenPurchaseDoesNotExist` | โยน `ResourceNotFoundException` เมื่อไม่พบ Purchase ตาม ID |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `GlobalExceptionHandlerTest.invalidPurchaseStateShouldReturnConflictResponse` | แปลง `InvalidPurchaseStateException` เป็น HTTP 409 พร้อม error code `INVALID_PURCHASE_STATE` และ request path |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `PurchaseOrderMapperTest.updatePendingOrderShouldReplaceItemsAndRecalculateTotal` | แทน Supplier และ items ของ order ที่ยัง `PENDING`, คำนวณ total ใหม่ และคง purchase number กับสถานะเดิม |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `PurchasePersistenceTest.updatePendingOrderShouldReplacePurchaseItemsInDatabase` | ตรวจการแทน Supplier/item ในฐานข้อมูลจริง, สถานะและ purchase number ที่คงเดิม, total ที่คำนวณใหม่ และไม่มี item เก่าค้าง |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `PurchaseOrderServiceImplTest.updateShouldReplacePendingOrderSupplierItemsAndTotal` | service แก้ Purchase สถานะ `PENDING` โดยเปลี่ยน Supplier/items และบันทึกพร้อม total ใหม่ |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `PurchaseOrderServiceImplTest.updateShouldRejectNonPendingPurchaseWithoutChangingIt` | ปฏิเสธการแก้ Purchase ที่ไม่ใช่ `PENDING` โดยไม่เรียกขั้นตอนแก้ข้อมูลหรือ save |
| `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า` | `PurchaseOrderServiceImplTest.updateShouldFailWhenPurchaseDoesNotExist` | โยน `ResourceNotFoundException` เมื่อไม่พบ Purchase และไม่ทำขั้นตอนอื่นต่อ |
| `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback` | `PurchasePersistenceTest.receiveShouldRollbackAllStockMovementsAndStatusWhenAnItemOverflows` | เมื่อรายการหนึ่งรับเข้าแล้วเกิด integer overflow ตรวจว่า transaction rollback stock ทุกรายการ, movement และสถานะ โดย order ยังคง `PENDING` |
| `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback` | `PurchasePersistenceTest.receiveShouldUpdateStockAndMovementsAndRejectRepeatedReceive` | รับสินค้าหลายรายการ ตรวจ stock เพิ่มตาม quantity, มี movement `IN` อ้าง purchase number, เปลี่ยนสถานะเป็น `COMPLETED` และการรับซ้ำไม่เปลี่ยน stock/movement เพิ่ม |
| `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback` | `PurchaseOrderServiceImplTest.receiveShouldAddInboundMovementForEachItemAndCompletePurchase` | ตรวจว่า service สร้าง movement `IN` ต่อ item ด้วย Product ID, quantity และ purchase number ที่ถูกต้อง ก่อนเปลี่ยนสถานะเป็น `COMPLETED` |
| `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback` | `PurchaseOrderServiceImplTest.receiveShouldRejectPurchaseThatIsNotPending` | ปฏิเสธการรับ order ที่ไม่ใช่ `PENDING` และไม่สร้าง movement หรือบันทึก order |
| `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback` | `PurchaseOrderServiceImplTest.receiveShouldFailWhenPurchaseDoesNotExist` | โยน `ResourceNotFoundException` เมื่อไม่พบ Purchase และไม่เรียก movement service |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.createShouldReturnCreatedPurchase` | `POST /api/v1/purchases` รับ request ที่ถูกต้อง ตอบ HTTP 201 พร้อม ID, `PENDING`, total และเรียก service |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.findAllAndFindByIdShouldReturnPurchaseResponses` | ทดสอบ GET collection และ GET by ID ว่าตอบ HTTP 200 พร้อมข้อมูล Purchase และเรียก service ตาม endpoint |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.updateShouldReturnUpdatedPurchase` | `PUT /api/v1/purchases/{id}` ตอบ HTTP 200 พร้อม Purchase response และเรียก update service ด้วย ID |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.receiveShouldReturnCompletedPurchase` | `POST /api/v1/purchases/{id}/receive` ตอบ HTTP 200 พร้อมสถานะ `COMPLETED` และเรียก receive service |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.invalidRequestShouldReturnValidationError` | POST/PUT ที่ request ไม่ผ่าน validation ตอบ HTTP 400 พร้อม `VALIDATION_FAILED` และ path ถูกต้อง โดยไม่เรียก service |
| `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests` | `PurchaseOrderControllerTest.serviceErrorsShouldUseStandardNotFoundAndConflictResponses` | ตรวจ exception จาก service ถูกแปลงเป็น HTTP 404 `RESOURCE_NOT_FOUND` และ HTTP 409 `INVALID_PURCHASE_STATE` |

## งานที่เพิ่มหลัง `aadd658` — ยังไม่ commit

ข้อความ commit ที่เสนอ: `feat: add stock movement read APIs and inventory workflow tests`

| สถานะ | Test file / method | สิ่งที่ทดสอบ |
|---|---|---|
| เพิ่ม | `InventoryStockServiceImplTest.getStockByProductIdShouldThrowIfInventoryMissing` | Product มีอยู่แต่ไม่มี Inventory ต้องตอบด้วย `ResourceNotFoundException` |
| เพิ่ม | `StockMovementServiceImplTest.findAllShouldMapMovementsInDescendingMovementTimeOrder` | คืน movement ทั้งหมดที่ map แล้ว และส่ง sort ล่าสุดไปเก่าสุดให้ repository |
| เพิ่ม | `StockMovementServiceImplTest.findByProductIdShouldMapMovementsInDescendingMovementTimeOrder` | ตรวจ Product ก่อนค้น movement ตาม Product ID และเรียงจากล่าสุดไปเก่าสุด |
| เพิ่ม | `StockMovementServiceImplTest.findByProductIdShouldThrowWhenProductDoesNotExist` | Product ID ที่ไม่มีอยู่ต้อง throw `ResourceNotFoundException` และไม่ query/map movement |
| เพิ่ม | `StockMovementServiceImplTest.findByProductIdShouldReturnEmptyListWhenProductHasNoMovements` | Product ที่มีอยู่แต่ยังไม่มี movement คืนรายการว่าง |
| เพิ่ม | `InventoryControllerTest.getStockShouldReturnInventoryResponse` | GET stock ตอบ HTTP 200 พร้อม quantity, reserved และ available quantity |
| เพิ่ม | `InventoryControllerTest.adjustStockShouldReturnUpdatedInventoryResponse` | PUT ปรับยอดที่ valid ตอบ HTTP 200 พร้อม stock response ใหม่และเรียก service |
| เพิ่ม | `InventoryControllerTest.invalidAdjustmentShouldReturnValidationErrorWithoutCallingService` | PUT ที่ขาด quantity ตอบ HTTP 400 `VALIDATION_FAILED` และไม่เรียก service |
| เพิ่ม | `InventoryControllerTest.missingProductOrInventoryShouldReturnNotFound` | Inventory service not-found ถูกแปลงเป็น HTTP 404 `RESOURCE_NOT_FOUND` |
| เพิ่ม | `StockMovementControllerTest.findAllShouldReturnMovementList` | GET movement ทั้งหมดตอบ HTTP 200 เป็น list พร้อมข้อมูล movement |
| เพิ่ม | `StockMovementControllerTest.findByProductIdShouldReturnMovementList` | GET movement ตาม Product ID ตอบ HTTP 200 เป็น list ที่สัมพันธ์กับ Product |
| เพิ่ม | `StockMovementControllerTest.findByProductIdShouldReturnNotFoundForUnknownProduct` | Product ที่ไม่มีอยู่ใน GET ตาม Product ID ตอบ HTTP 404 |
| เพิ่ม | `StockMovementControllerTest.createShouldReturnCreatedMovement` | POST สร้าง movement ตอบ HTTP 201 พร้อม response ของ movement |
| เพิ่ม | `StockMovementControllerTest.invalidMovementShouldReturnValidationErrorWithoutCallingService` | POST ที่ quantity ผิด validation ตอบ HTTP 400 และไม่เรียก service |
| เพิ่ม | `InventoryPurchaseApiIntegrationTest.purchaseReceiveApiShouldPersistOrderStockAndInboundMovement` | ผ่าน HTTP สร้าง Purchase และ receive จริง ตรวจสถานะใน DB, stock เพิ่ม และ movement ปรากฏทั้ง GET by product และ GET all |

## ไฟล์ Test ที่เกี่ยวข้อง

- [InventoryStockServiceImplTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/service/impl/InventoryStockServiceImplTest.java)
- [StockMovementServiceImplTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/service/impl/StockMovementServiceImplTest.java)
- [PurchasePersistenceTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/repository/PurchasePersistenceTest.java)
- [PurchaseOrderRequestValidationTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/dto/request/PurchaseOrderRequestValidationTest.java)
- [PurchaseOrderMapperTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/mapper/PurchaseOrderMapperTest.java)
- [PurchaseOrderServiceImplTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/service/impl/PurchaseOrderServiceImplTest.java)
- [GlobalExceptionHandlerTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/exception/GlobalExceptionHandlerTest.java)
- [PurchaseOrderControllerTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/controller/api/PurchaseOrderControllerTest.java)
- [InventoryControllerTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/controller/api/InventoryControllerTest.java)
- [StockMovementControllerTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/controller/api/StockMovementControllerTest.java)
- [InventoryPurchaseApiIntegrationTest](../../code/backend/hardware-store/src/test/java/com/hardwarestore/integration/InventoryPurchaseApiIntegrationTest.java)

## รัน Regression Tests

จากโฟลเดอร์ `code/backend/hardware-store/`:

```powershell
.\mvnw.cmd test
```

ผล regression ล่าสุดใน working tree: **63 tests, 0 failures, 0 errors**. ผลในอดีตหลัง commit `aadd658` ยังคงเป็น 48 tests.

## ขอบเขต

- Commit `4862967` ไม่แสดงเป็นรายการทดสอบ เพราะไม่มี test file เปลี่ยนใน commit นั้น
- `CategoryServiceImplTest` เป็น test ของ Category และไม่ได้ถูกเพิ่มหรือปรับใน commits ของ Member 2 จึงไม่นับรวมในรายการข้างต้น
