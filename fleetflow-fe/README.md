# 🖥️ FleetFlow Frontend (Test & Operations Console)

Giao diện Web Frontend trực quan phục vụ kiểm thử và mô phỏng toàn bộ chức năng của hệ thống **FleetFlow Logistics Backend**.

---

## ⚡ Cách 1: Chạy Thử Ngay Lập Tức (Không cần cài đặt Node.js)
Bạn chỉ cần mở trực tiếp file sau bằng bất kỳ trình duyệt nào (Chrome, Edge, Firefox):
👉 **`d:\SOURCE CODE\fleetflow-fe\standalone.html`**

File này đã tích hợp sẵn React 18, Babel và TailwindCSS qua CDN, kết nối trực tiếp đến Spring Boot backend tại `http://localhost:8080`.

---

## 🚀 Cách 2: Chạy Dự Án Chuẩn React + Vite
```bash
cd "d:\SOURCE CODE\fleetflow-fe"

# 1. Cài đặt dependencies
npm install

# 2. Khởi chạy dev server
npm run dev
```
Truy cập: `http://localhost:3000` (đã cấu hình reverse proxy tự động sang backend port 8080).

---

## 🎯 Các Chức Năng Kiểm Thử Được Tích Hợp

1. **Chuyển Đổi Vai Trò Nhanh (Role Switcher)**:
   - 👑 **Admin** (`admin` / `password123`)
   - 📋 **Staff Điều Phối** (`staff_hn` / `password123`)
   - 🛵 **Tài Xế** (`driver_nam` / `password123`)

2. **Báo Cáo Dashboard**:
   - Thống kê thời gian thực: Tổng đơn, tỷ lệ giao thành công %, tổng doanh thu cước phí, tổng tiền COD đã thu.

3. **Quản Lý Vận Đơn (Shipment)**:
   - Form tạo đơn có **Preview Tính Cước (Pricing Engine)** trực tiếp khi nhập khối lượng và kích thước $L \times W \times H$.
   - Lọc đơn theo từng trạng thái (`PENDING`, `ASSIGNED`, `PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNING`).

4. **Kiểm Thử Xung Đột Đồng Thời (⚡ Test Concurrency)**:
   - Bấm nút "Test Concurrency" trên từng đơn PENDING để bắn 5 requests đồng thời phân công tài xế $\rightarrow$ Xác minh trực quan duy nhất 1 request thành công 200 OK và các request còn lại nhận lỗi xung đột 409 Conflict (Optimistic Locking).

5. **Cổng Thao Tác Của Tài Xế (Driver Portal)**:
   - Xem các đơn được phân công cho chính tài xế đăng nhập (`/api/v1/shipments/driver/my-tasks`).
   - Nút chuyển trạng thái nhanh: Đã nhận hàng (`PICKED_UP`), Bắt đầu giao (`OUT_FOR_DELIVERY`), Giao thành công (thu tiền COD), Giao thất bại (chọn lý do).
   - Nút giả lập Ping vị trí GPS của tài xế lên server.

6. **Đối Soát COD (💰 Idempotency Test)**:
   - Xem các khoản COD tài xế đã thu (`COLLECTED`) chờ nộp về kho.
   - Nút "🛡️ Test Idempotency (Click Đúp)" gửi 2 request với cùng một `Idempotency-Key` $\rightarrow$ Xác minh số tiền và đơn hàng không bị nhân đôi!

7. **Tra Cứu Công Khai (Public Tracking)**:
   - Nhập mã vận đơn bất kỳ (không cần đăng nhập) để xem timeline hành trình từng bước và thông tin người nhận đã ẩn danh (masked).

