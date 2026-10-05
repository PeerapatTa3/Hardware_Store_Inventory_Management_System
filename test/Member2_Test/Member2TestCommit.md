# Member 2 — รายละเอียดการทดสอบแยกตาม Commit

เอกสารนี้แจกแจง test case ที่เพิ่มหรือแก้ในแต่ละ commit ของงาน Member 2 พร้อมไฟล์ทดสอบ สิ่งที่ตรวจ และผลทดสอบหลังจบรอบนั้น โดยรวมเฉพาะ commits ที่มีการเปลี่ยนแปลงไฟล์ test

> จำนวนในผลทดสอบเป็นจำนวนสะสมของชุดทดสอบ ณ รอบนั้น ไม่ใช่จำนวน test case ที่เพิ่มใน commit ยกเว้นมีการระบุไว้ชัดเจน บาง test case เป็นการแก้ assertion ของ test เดิม จึงระบุว่า “ปรับ test เดิม” ไว้ด้วย

## Commit `268b6d9` — `fix: synchronize inventory adjustment movements`

ไฟล์ที่เกี่ยวข้อง: `InventoryStockServiceImplTest`, `StockMovementServiceImplTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `adjustStockShouldUpdateQuantity` | ปรับ test เดิม | เมื่อปรับ Inventory ต้องเรียก Stock Movement service ด้วย Product ID, ประเภท `ADJUSTMENT`, ยอดเป้าหมาย และเหตุผลที่ส่งมา และต้องไม่บันทึก Inventory ซ้ำโดยตรงจาก service นี้ |
| `create_shouldSetStockToTargetForAdjustmentMovement` | เพิ่ม | เมื่อสร้าง movement `ADJUSTMENT` ให้ตั้ง stock เป็น quantity เป้าหมาย บันทึก Inventory และบันทึก movement ที่มีชนิด จำนวน และ note ตรงกับคำขอ |
| `create_shouldRejectAdjustmentBelowReservedQuantity` | เพิ่ม | เมื่อยอดเป้าหมายน้อยกว่า reserved quantity ต้อง throw `IllegalArgumentException`, ไม่เปลี่ยนยอดคงเหลือ และไม่บันทึก Inventory หรือ movement |

**ผลทดสอบหลัง commit:** 17 tests ผ่าน (เป็นผลรวมทั้งชุด; มี test ใหม่ 2 รายการและมีการปรับ test เดิม 1 รายการ)

## Commit `2970c37` — `feat: add purchase persistence model`

ไฟล์ที่เกี่ยวข้อง: `PurchasePersistenceTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `savePurchaseOrderShouldPersistStatusSupplierAndItems` | เพิ่ม | บันทึกและอ่าน PurchaseOrder กลับจากฐานข้อมูล ตรวจสถานะเริ่มต้น `PENDING`, `createdAt`, Supplier, รายการ PurchaseItem, Product ที่รายการอ้างถึง และ subtotal ที่ persist ไว้ |

**ผลทดสอบหลัง commit:** 18 tests ผ่าน รวม persistence test นี้

## Commit `74d2244` — `feat: add purchase request and response models`

ไฟล์ที่เกี่ยวข้อง: `PurchaseOrderRequestValidationTest`, `PurchaseOrderMapperTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `shouldRejectMissingSupplierAndEmptyItems` | เพิ่ม | ปฏิเสธ request ที่ไม่มี `supplierId` และไม่มีรายการสินค้า โดยต้องมี validation errors ของทั้งสอง field |
| `shouldRejectInvalidNestedPurchaseItemValues` | เพิ่ม | ตรวจ nested validation: supplier/product ID ต้องเป็นบวก, quantity ต้องเป็นบวก และ unit cost ต้องเป็นบวก |
| `shouldAcceptValidPurchaseOrderRequest` | เพิ่ม | ยอมรับ request ที่มี supplier และรายการสินค้าที่ค่าถูกต้อง โดยไม่มี validation violations |
| `shouldRejectNullPurchaseItem` | เพิ่ม | ปฏิเสธสมาชิก `null` ในรายการ `items` และชี้ validation error ไปที่ตำแหน่งรายการนั้น |
| `toEntityShouldMapItemsAndCalculateTotals` | เพิ่ม | map Supplier/Product และรายการสองบรรทัด, ตั้งสถานะเริ่มต้น `PENDING`, คำนวณ subtotal ของแต่ละรายการและ total รวมได้ถูกต้อง |
| `toEntityShouldRejectUnresolvedProduct` | เพิ่ม | ปฏิเสธการ map เมื่อ Product ID ใน request ไม่มี Product ที่ resolve ได้ พร้อมข้อความระบุ ID |
| `toResponseShouldMapOrderAndItems` | เพิ่ม | map PurchaseOrder และรายละเอียด supplier/item ไปยัง response DTO รวมชื่อ Product และ subtotal |

**ผลทดสอบหลัง commit:** 25 tests ผ่าน

## Commit `b41be12` — `feat: add purchase creation and lookup`

ไฟล์ที่เกี่ยวข้อง: `PurchaseOrderServiceImplTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `createShouldResolveReferencesGenerateNumberAndSaveOrder` | เพิ่ม | resolve Supplier และ Products, สร้าง Purchase number, map รายการโดย resolve Product ซ้ำเพียงครั้งเดียว, บันทึก order และคืน response ที่ mapper สร้าง |
| `createShouldFailWhenSupplierDoesNotExist` | เพิ่ม | เมื่อไม่พบ Supplier ให้ throw `ResourceNotFoundException` และไม่เรียก Product repository, mapper หรือ Purchase repository |
| `createShouldFailWhenProductDoesNotExist` | เพิ่ม | เมื่อไม่พบ Product ให้ throw `ResourceNotFoundException` ที่สื่อว่าไม่พบ Product และไม่ map หรือบันทึก order |
| `findAllShouldMapOrdersInCreatedDateDescendingOrder` | เพิ่ม | โหลด Purchase เรียง `createdAt` จากใหม่ไปเก่าและแปลงแต่ละ order เป็น response |
| `findByIdShouldMapPurchaseOrder` | เพิ่ม | เมื่อพบ ID ให้แปลง PurchaseOrder เป็น response และคืน response นั้น |
| `findByIdShouldThrowWhenPurchaseDoesNotExist` | เพิ่ม | เมื่อไม่พบ ID ให้ throw `ResourceNotFoundException` |

**ผลทดสอบหลัง commit:** 31 tests ผ่าน

## Commit `bbea261` — `feat: ทำการแก้ Purchase และกติกาการเปลี่ยนสถานะก่อนรับสินค้า`

ไฟล์ที่เกี่ยวข้อง: `GlobalExceptionHandlerTest`, `PurchaseOrderMapperTest`, `PurchasePersistenceTest`, `PurchaseOrderServiceImplTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `invalidPurchaseStateShouldReturnConflictResponse` | เพิ่ม | แปลง `InvalidPurchaseStateException` เป็น HTTP 409 พร้อม error code `INVALID_PURCHASE_STATE` และ path ของ request |
| `updatePendingOrderShouldReplaceItemsAndRecalculateTotal` | เพิ่ม | mapper แทน Supplier และรายการเดิมด้วยข้อมูลใหม่ คำนวณ total ใหม่ และคง purchase number กับสถานะ `PENDING` พร้อมผูก item ใหม่กลับไปยัง order |
| `updatePendingOrderShouldReplacePurchaseItemsInDatabase` | เพิ่ม | บันทึกการแทน Supplier และ PurchaseItem ลงฐานข้อมูลจริง ตรวจรายการ/Product ใหม่, สถานะและเลข Purchase เดิม, total ใหม่ และไม่มี item เก่าค้างอยู่ |
| `updateShouldReplacePendingOrderSupplierItemsAndTotal` | เพิ่ม | service แก้ Purchase สถานะ `PENDING` โดยแทน Supplier/รายการ และคืนข้อมูล total ที่ปรับใหม่ |
| `updateShouldRejectNonPendingPurchaseWithoutChangingIt` | เพิ่ม | ปฏิเสธการแก้ Purchase ที่ไม่ใช่ `PENDING` และตรวจว่าไม่เกิดการบันทึก order หรือ side effect ที่ไม่ควรเกิด |
| `updateShouldFailWhenPurchaseDoesNotExist` | เพิ่ม | เมื่อไม่พบ Purchase ID ให้ throw `ResourceNotFoundException` |

**ผลทดสอบหลัง commit:** 37 tests ผ่าน

## Commit `97d8e39` — `feat: ทำ Receive แบบ transaction เชื่อม Inventory/StockMovement พร้อม tests สำหรับ receive/rollback`

ไฟล์ที่เกี่ยวข้อง: `PurchasePersistenceTest`, `PurchaseOrderServiceImplTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `receiveShouldRollbackAllStockMovementsAndStatusWhenAnItemOverflows` | เพิ่ม | ทำให้รายการหนึ่งรับเข้าแล้วเกินขอบเขตจำนวนเต็ม ตรวจว่า transaction rollback: stock ของทุกรายการกลับค่าเดิม, ไม่มี movement ของ Purchase และสถานะยังเป็น `PENDING` |
| `receiveShouldUpdateStockAndMovementsAndRejectRepeatedReceive` | เพิ่ม | รับหลายรายการผ่าน persistence จริง ตรวจ stock เพิ่มตามจำนวน, มี `IN` movement ต่อรายการโดยอ้าง purchase number, เปลี่ยนเป็น `COMPLETED`; การรับซ้ำถูกปฏิเสธและไม่เพิ่ม stock/movement ซ้ำ |
| `receiveShouldAddInboundMovementForEachItemAndCompletePurchase` | เพิ่ม | service เรียก Stock Movement service หนึ่งครั้งต่อ item โดยใช้ Product ID, quantity, movement type `IN` และ purchase number ที่ถูกต้อง ก่อนบันทึกสถานะ `COMPLETED` |
| `receiveShouldRejectPurchaseThatIsNotPending` | เพิ่ม | ปฏิเสธการรับ order ที่ไม่ใช่ `PENDING` โดยไม่เรียก Stock Movement service หรือบันทึก Purchase |
| `receiveShouldFailWhenPurchaseDoesNotExist` | เพิ่ม | เมื่อไม่พบ Purchase ID ให้ throw `ResourceNotFoundException` |

**ผลทดสอบหลัง commit:** 42 tests ผ่าน รวม receive success, repeated receive และ rollback

## Commit `aadd658` — `test:เพิ่ม Purchase REST endpoints/controller tests และรัน regression tests`

ไฟล์ที่เกี่ยวข้อง: `PurchaseOrderControllerTest`

| Test case | สถานะใน commit | รายละเอียดที่ตรวจ |
|---|---|---|
| `createShouldReturnCreatedPurchase` | เพิ่ม | `POST /api/v1/purchases` รับ request ที่ถูกต้อง ตอบ HTTP 201 พร้อม ID, สถานะ `PENDING`, total และเรียก service |
| `findAllAndFindByIdShouldReturnPurchaseResponses` | เพิ่ม | `GET /api/v1/purchases` คืนรายการพร้อมข้อมูล response; `GET /api/v1/purchases/{id}` คืนรายละเอียดตาม ID และเรียก service ที่ตรงกัน |
| `updateShouldReturnUpdatedPurchase` | เพิ่ม | `PUT /api/v1/purchases/{id}` รับ request ที่ถูกต้อง ตอบ HTTP 200 พร้อม response ของ Purchase และเรียก update service ด้วย ID |
| `receiveShouldReturnCompletedPurchase` | เพิ่ม | `POST /api/v1/purchases/{id}/receive` ตอบ HTTP 200 พร้อมสถานะ `COMPLETED` และเรียก receive service |
| `invalidRequestShouldReturnValidationError` | เพิ่ม | request ที่ validation ไม่ผ่านทั้ง POST และ PUT ตอบ HTTP 400, error code `VALIDATION_FAILED` และ path ที่ตรง endpoint; ไม่เรียก service |
| `serviceErrorsShouldUseStandardNotFoundAndConflictResponses` | เพิ่ม | service exception ถูกแปลงเป็น HTTP 404 / `RESOURCE_NOT_FOUND` และ HTTP 409 / `INVALID_PURCHASE_STATE` ตามลำดับ |

**ผลทดสอบหลัง commit:** `.\mvnw.cmd test` ผ่าน 48 tests, 0 failures, 0 errors; Controller tests มี 6 test methods

## วิธีรันชุดทดสอบล่าสุด

รันจาก `code/backend/hardware-store/`:

```powershell
.\mvnw.cmd test
```

ผลในอดีตข้างต้นอ้างอิงบันทึกผลทดสอบหลังแต่ละรอบ ส่วนการยืนยันสถานะปัจจุบันให้รันคำสั่งนี้กับ working tree ล่าสุด

## หมายเหตุ

- Commit `4862967` ไม่รวมในตาราง เพราะไม่มีไฟล์ test เปลี่ยนแปลง
- `CategoryServiceImplTest` ไม่ได้ถูกเพิ่มหรือแก้ใน commits ของ Member 2 ที่สรุปไว้ จึงไม่นำมาใส่ในตาราง test ของ commit
