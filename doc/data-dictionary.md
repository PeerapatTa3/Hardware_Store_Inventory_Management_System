# Data Dictionary

## categories
- id: primary key
- name: ชื่อหมวดหมู่สินค้า, unique
- description: รายละเอียดหมวดหมู่

## suppliers
- id: primary key
- name: ชื่อผู้จำหน่าย, unique
- phone: เบอร์โทรศัพท์
- email: อีเมล, unique
- address: ที่อยู่

## products
- id: primary key
- sku: รหัสสินค้า, unique
- name: ชื่อสินค้า
- description: รายละเอียดสินค้า
- unit: หน่วยนับ เช่น pcs, box
- price: ราคาขาย
- cost_price: ต้นทุน
- minimum_stock: ระดับขั้นต่ำที่ต้องแจ้งเตือน
- category_id: FK ไปยัง categories
- supplier_id: FK ไปยัง suppliers

## inventory_stocks
- id: primary key
- product_id: FK ไปยัง products, unique (หนึ่ง Product มี InventoryStock ได้หนึ่งรายการ)
- quantity: จำนวนคงเหลือ
- reserved_quantity: จำนวนที่สำรองไว้
- available quantity: คำนวณจาก `quantity - reserved_quantity` ไม่ได้เก็บเป็น column

## stock_movements
- id: primary key
- product_id: FK ไปยัง products
- movement_type: ประเภท movement (`IN`, `OUT`, `ADJUSTMENT`)
- quantity: จำนวนที่รับ/จ่าย หรือยอดคงเหลือเป้าหมายเมื่อเป็น `ADJUSTMENT`
- reference_no: เลขอ้างอิง เช่น Purchase number
- note: หมายเหตุ
- movement_at: วันเวลาที่บันทึก movement

## purchase_orders
- id: primary key
- purchase_number: เลขที่ Purchase, unique
- supplier_id: FK ไปยัง suppliers
- status: สถานะ (`PENDING`, `COMPLETED`)
- total_amount: ยอดรวม คำนวณจากรายการ PurchaseItem
- created_at: วันเวลาที่สร้าง Purchase

## purchase_items
- id: primary key
- purchase_order_id: FK ไปยัง purchase_orders
- product_id: FK ไปยัง products
- quantity: จำนวนสินค้าในรายการ
- unit_cost: ต้นทุนต่อหน่วย
- subtotal: ผลรวมของ quantity × unit_cost
