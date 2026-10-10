# Test Suite and Reports

Backend tests ใช้ JUnit 5, Mockito และ Spring Boot Test อยู่ใน `code/backend/hardware-store/src/test/`.

รันจาก `code/backend/hardware-store/`:

```powershell
.\mvnw.cmd test
```

Maven Surefire สร้างรายงานผลแยกตาม test class ใน `code/backend/hardware-store/target/surefire-reports/`.
