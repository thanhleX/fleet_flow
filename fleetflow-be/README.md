# 🚛 FleetFlow – Enterprise Logistics & Delivery Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue.svg)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

**FleetFlow** là hệ thống quản lý vận chuyển và giao hàng phân cấp doanh nghiệp (Enterprise Logistics & Delivery Platform) tập trung chuyên sâu vào kiến trúc Backend hiệu năng cao, thiết kế cơ sở dữ liệu thực tế, giải quyết bài toán đồng thời (Concurrency), tính toàn vẹn tài chính (Idempotency) và lộ trình tối ưu hoá từng bước bằng đo lường (Benchmarking).

---

## 📌 1. Các Điểm Sáng Kỹ Thuật (Senior Backend Highlights)

| Tính năng / Vấn đề kỹ thuật | Giải pháp kiến trúc trong FleetFlow |
| :--- | :--- |
| **Shipment State Machine** | Quản lý chặt chẽ vòng đời vận đơn: `PENDING` $\rightarrow$ `ASSIGNED` $\rightarrow$ `PICKED_UP` $\rightarrow$ `IN_TRANSIT` $\rightarrow$ `OUT_FOR_DELIVERY` $\rightarrow$ `DELIVERED`, tự động xử lý ngoại lệ giao thất bại, hẹn giao lại hoặc hoàn hàng `RETURNING`. |
| **Xử lý Race Condition khi gán tài xế** | Kết hợp **Optimistic Locking (`@Version`)** trên `Shipment` để chặn 2 nhân viên cùng gán 1 đơn, và **Pessimistic Locking (`SELECT FOR UPDATE`)** trên `Driver` để kiểm soát số đơn tối đa và tải trọng xe (`Vehicle.max_payload_kg`). |
| **Tính Idempotency trong Đối soát COD** | Sử dụng header `Idempotency-Key` với ràng buộc Unique Constraint. Nếu client retry hoặc double-click nộp tiền, hệ thống trả về kết quả cũ đã lưu mà không trừ tiền / tạo giao dịch kép. |
| **Pricing Engine** | Tự động tính trọng lượng quy đổi thể tích $\frac{L \times W \times H}{5000}$, cước theo vùng (`INTRA_PROVINCE`, `INTER_PROVINCE`), gói cước (`STANDARD`, `EXPRESS`), phụ phí COD và bảo hiểm. |
| **Bảo mật Đa tầng (RBAC + ABAC)** | Phân quyền vai trò kết hợp kiểm tra quyền ở cấp tài nguyên (`SecurityEvaluator`): Tài xế chỉ được xem và cập nhật đơn hàng thuộc về mình. |
| **Domain Events & Audit Logging** | Sử dụng Spring Event Publisher phát sự kiện `ShipmentDeliveredEvent`, `DeliveryFailedEvent` để ghi Audit Trail bất đồng bộ (`@Async` thread pool), không làm nghẽn luồng request chính. |
| **Background Scheduled Tasks** | Tự động quét đơn treo lâu ngày, quét đơn thất bại quá 3 lần để chuyển hoàn hàng, cảnh báo tiền COD chưa được đối soát sau 24h. |
| **Kiểm thử Tải & Đo lường Hiệu năng (k6)** | Thiết kế sẵn bộ kịch bản k6 kiểm tra Concurrency, Read-Heavy Tracking lookup và High Write Load GPS Telemetry. |

---

## 🏛️ 2. Kiến trúc & Sơ đồ Vòng đời (State Machines)

### Vòng đời Vận đơn (Shipment Lifecycle)
```
[PENDING] ────► [ASSIGNED] ────► [PICKED_UP] ────► [IN_TRANSIT] ────► [OUT_FOR_DELIVERY]
                                                                             │
                         ┌───────────────────────────────────────────────────┼────────────────────────────────────────┐
                         │                                                   ▼                                        ▼
                         │                                             [DELIVERED]                        [DELIVERY_FAILED]
                         │                                            (Thu tiền COD)                                  │
                         │                                                                                            ▼
                         │                                                             ┌──────────────────────────────┴────────────────────────────┐
                         │                                                             ▼                                                           ▼
                         └────────────────────────────────────────────────────── [RESCHEDULED]                                                [RETURNING]
                                                                                  (Lần giao < 3)                                             (Lần giao >= 3)
                                                                                                                                                   │
                                                                                                                                                   ▼
                                                                                                                                              [RETURNED]
```

---

## 🚀 3. Khởi chạy Hệ thống

### 3.1. Yêu cầu Môi trường
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### 3.2. Khởi chạy Hạ tầng với Docker Compose
```bash
docker-compose up -d
```
Lệnh trên sẽ tự động khởi tạo:
- **MySQL 8.0**: Cổng `3306` (Database: `fleetflow_db`, User: `root`, Pass: `root`)
- **Redis 7.0**: Cổng `6379`
- **RabbitMQ 3.13**: Cổng `5672` (Web Management: `http://localhost:15672`, User: `guest`, Pass: `guest`)
- **Adminer (Database Web UI)**: `http://localhost:8081`

### 3.3. Khởi chạy Spring Boot Backend
```bash
mvn clean spring-boot:run
```
Hệ thống sẽ tự động tạo bảng (JPA ddl-auto) và nạp dữ liệu mẫu ban đầu từ `src/main/resources/data.sql`.

---

## 🔑 4. Tài khoản Kiểm thử Mặc định

Tất cả các tài khoản mặc định có mật khẩu là: **`password123`**

| Username | Họ tên | Vai trò (Role) | Chức năng chính |
| :--- | :--- | :--- | :--- |
| `admin` | System Administrator | `ROLE_ADMIN` | Toàn quyền cấu hình bảng giá, xem Dashboard báo cáo, Audit logs |
| `manager_hn` | Trần Quản Lý | `ROLE_MANAGER` | Quản lý kho Hà Nội, đối soát COD |
| `staff_hn` | Lê Điều Phối | `ROLE_STAFF` | Tạo đơn hàng, điều phối phân công tài xế |
| `driver_nam` | Nguyễn Văn Nam | `ROLE_DRIVER` | Xem đơn được gán, cập nhật lần giao, ping vị trí GPS |
| `driver_tuan`| Phạm Tuấn | `ROLE_DRIVER` | Tài xế dự phòng để kiểm thử phân công đồng thời |

---

## 📖 5. Tài liệu API & Thao tác Mẫu

- **Swagger UI Interactive Documentation**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

### 5.1. Đăng nhập lấy Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "staff_hn", "password": "password123"}'
```

### 5.2. Tạo mới Đơn hàng (Shipment)
```bash
curl -X POST http://localhost:8080/api/v1/shipments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <STAFF_TOKEN>" \
  -d '{
    "senderName": "Cửa Hàng Quần Áo Vintage",
    "senderPhone": "0987654321",
    "senderAddress": "25 Phố Huế, Hoàn Kiếm",
    "senderProvince": "Hà Nội",
    "receiverName": "Nguyễn Thị Hương",
    "receiverPhone": "0912999888",
    "receiverAddress": "15 Đào Tấn, Ba Đình",
    "receiverProvince": "Hà Nội",
    "originHubId": 1,
    "serviceTier": "STANDARD",
    "totalWeightKg": 1.5,
    "lengthCm": 30, "widthCm": 20, "heightCm": 10,
    "codAmount": 350000,
    "items": [
      { "itemName": "Áo Sơ Mi Vintage", "quantity": 1, "weightKg": 1.5, "declaredPrice": 350000 }
    ]
  }'
```

### 5.3. Phân công Tài xế (Điều phối với Concurrency Lock)
```bash
curl -X POST http://localhost:8080/api/v1/dispatch/1/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <STAFF_TOKEN>" \
  -d '{"driverId": 1, "expectedVersion": 0}'
```

### 5.4. Tra cứu Hành trình Công khai (Read-heavy)
```bash
curl http://localhost:8080/api/v1/public/tracking/FF00000001
```

### 5.5. Đối soát COD an toàn với Idempotency Key
```bash
curl -X POST http://localhost:8080/api/v1/cod/settle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <STAFF_TOKEN>" \
  -H "Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d" \
  -d '{
    "driverId": 1,
    "idempotencyKey": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "note": "Đối soát ca chiều ngày 10/09"
  }'
```

---

## ⚡ 6. Kịch bản Kiểm thử Tải & Hiệu năng với k6

Thư mục `benchmark/k6/` cung cấp 4 kịch bản đo lường thực tế:

```bash
# 1. Đo lường API Tra cứu hành trình (Read-heavy)
k6 run benchmark/k6/tracking_benchmark.js

# 2. Kiểm thử Race Condition khi 30 nhân viên cùng gán 1 đơn cùng lúc
k6 run -e TOKEN="<STAFF_JWT>" -e SHIPMENT_ID=1 benchmark/k6/dispatch_concurrency.js

# 3. Đo lường thông lượng ghi (Write throughput) khi tài xế ping GPS liên tục
k6 run -e TOKEN="<DRIVER_JWT>" benchmark/k6/driver_location_ping.js

# 4. Kiểm tra tính Idempotent khi gửi trùng request đối soát tài chính
k6 run -e TOKEN="<STAFF_JWT>" benchmark/k6/cod_idempotency.js
```

---

## 📈 7. Lộ trình Tối ưu Hiệu năng (Engineering Roadmap)

- [x] **V1 (Baseline)**: Spring Boot + MySQL, thiết kế chuẩn Domain Model, Enums, State Machine, Optimistic Lock (`@Version`), Idempotency Key, RBAC/ABAC Security và Scheduled Tasks.
- [ ] **V2 (Database Tuning)**: Composite Index cho tra cứu Tracking & Dashboard, HikariCP Connection Pool sizing, tránh N+1 với EntityGraph và DTO Projections.
- [ ] **V3 (Redis Caching)**: Tích hợp Spring Cache (`@Cacheable`) cho Tracking API với cache eviction thông minh, sử dụng Redis Geo (`GEOADD`/`GEORADIUS`) cho toạ độ GPS tài xế.
- [ ] **V4 (Event-Driven Broker)**: Thay thế Spring Events nội bộ bằng RabbitMQ/Kafka Topics để decouple Audit Logs và Notification thành worker riêng biệt.

