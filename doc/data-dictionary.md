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
- product_id: FK ไปยัง products, unique
- quantity: จำนวนสินค้าคงเหลือ
- reserved_quantity: จำนวนสินค้าที่ถูกจองและยังเบิกออกไม่ได้

## stock_movements
- id: primary key
- product_id: FK ไปยัง products
- movement_type: IN, OUT หรือ ADJUSTMENT
- quantity: จำนวนที่เคลื่อนไหว; ADJUSTMENT เก็บส่วนต่างแบบมีเครื่องหมายระหว่างยอดใหม่กับยอดเดิม
- reference_no: เลขอ้างอิง, optional
- note: หมายเหตุหรือเหตุผลการปรับ
- movement_at: วันเวลาที่บันทึกรายการ
