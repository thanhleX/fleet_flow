-- ============================================================================
-- FLEETFLOW SYSTEM BOOTSTRAP DATA (MOCK & DEMO DATASET)
-- ============================================================================

-- 1. WAREHOUSES / HUBS (Các trung tâm logistics và kho trung chuyển toàn quốc)
INSERT IGNORE INTO warehouses (id, code, name, address, province, district, active, created_at, updated_at) VALUES
(1, 'HUB_HN_01', 'Kho Trung Tâm Hà Nội', '120 Cầu Giấy, Hà Nội', 'Hà Nội', 'Cầu Giấy', true, NOW(), NOW()),
(2, 'HUB_HCM_01', 'Kho Trung Tâm TP.HCM', '458 Nguyễn Thị Minh Khai, Q.3, TP.HCM', 'TP. Hồ Chí Minh', 'Quận 3', true, NOW(), NOW()),
(3, 'HUB_DN_01', 'Kho Trung Chuyển Đà Nẵng', '89 Nguyễn Văn Linh, Đà Nẵng', 'Đà Nẵng', 'Hải Châu', true, NOW(), NOW()),
(4, 'HUB_HP_01', 'Kho Trung Chuyển Hải Phòng', '15 Lê Hồng Phong, Hải Phòng', 'Hải Phòng', 'Ngô Quyền', true, NOW(), NOW()),
(5, 'HUB_CT_01', 'Kho Miền Tây Cần Thơ', '77 Đường 30 Tháng 4, Cần Thơ', 'Cần Thơ', 'Ninh Kiều', true, NOW(), NOW()),
(6, 'HUB_BD_01', 'Tổng Kho Logistics Bình Dương', 'Đại lộ Bình Dương, Thủ Dầu Một', 'Bình Dương', 'Thủ Dầu Một', true, NOW(), NOW());

-- 2. VEHICLES (Đội xe giao hàng: Xe máy, Van, Xe tải 1 tấn, 5 tấn)
INSERT IGNORE INTO vehicles (id, license_plate, vehicle_type, max_payload_kg, max_volume_m3, active, created_at, updated_at) VALUES
(1, '29A-12345', 'MOTORBIKE', 50.0, 0.20, true, NOW(), NOW()),
(2, '29B-67890', 'MOTORBIKE', 50.0, 0.20, true, NOW(), NOW()),
(3, '51D-99999', 'VAN', 800.0, 3.50, true, NOW(), NOW()),
(4, '43C-88888', 'TRUCK_1T', 1500.0, 7.00, true, NOW(), NOW()),
(5, '29C-11223', 'TRUCK_1T', 1500.0, 7.00, true, NOW(), NOW()),
(6, '59A-33445', 'MOTORBIKE', 60.0, 0.25, true, NOW(), NOW()),
(7, '51C-77889', 'TRUCK_5T', 5000.0, 22.00, true, NOW(), NOW()),
(8, '29D-55667', 'VAN', 1000.0, 4.20, true, NOW(), NOW());

-- 3. USERS (Tất cả tài khoản có mật khẩu chuẩn: password123)
-- BCrypt: $2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a
INSERT IGNORE INTO users (id, username, password_hash, full_name, phone, email, role, active, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'System Administrator', '0901000001', 'admin@fleetflow.io', 'ROLE_ADMIN', true, NOW(), NOW()),
(2, 'manager_hn', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Trần Quản Lý (HN)', '0901000002', 'manager_hn@fleetflow.io', 'ROLE_MANAGER', true, NOW(), NOW()),
(3, 'staff_hn', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Lê Điều Phối (HN)', '0901000003', 'staff_hn@fleetflow.io', 'ROLE_STAFF', true, NOW(), NOW()),
(4, 'driver_nam', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Nguyễn Văn Nam (Driver HN)', '0901000004', 'driver_nam@fleetflow.io', 'ROLE_DRIVER', true, NOW(), NOW()),
(5, 'driver_tuan', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Phạm Tuấn (Driver HN)', '0901000005', 'driver_tuan@fleetflow.io', 'ROLE_DRIVER', true, NOW(), NOW()),
(6, 'manager_hcm', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Nguyễn Quản Lý (HCM)', '0901000006', 'manager_hcm@fleetflow.io', 'ROLE_MANAGER', true, NOW(), NOW()),
(7, 'staff_hcm', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Võ Điều Phối (HCM)', '0901000007', 'staff_hcm@fleetflow.io', 'ROLE_STAFF', true, NOW(), NOW()),
(8, 'driver_hung', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Trần Quang Hùng (Driver HCM)', '0901000008', 'driver_hung@fleetflow.io', 'ROLE_DRIVER', true, NOW(), NOW()),
(9, 'driver_minh', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Đặng Nhật Minh (Driver HCM)', '0901000009', 'driver_minh@fleetflow.io', 'ROLE_DRIVER', true, NOW(), NOW()),
(10, 'driver_hoang', '$2a$10$4WzcfXxjp2eeb4WsmoN3Meqx45sjCW4ru55WPgOzzGE8VizCHDW8a', 'Lý Minh Hoàng (Driver HN)', '0901000010', 'driver_hoang@fleetflow.io', 'ROLE_DRIVER', true, NOW(), NOW());

-- 4. DRIVERS (Hồ sơ tài xế giao nhận kết nối phương tiện và toạ độ GPS)
INSERT IGNORE INTO drivers (id, user_id, vehicle_id, current_hub_id, license_number, status, max_active_orders, current_lat, current_lng, last_location_update, created_at, updated_at) VALUES
(1, 4, 1, 1, 'B2-99887711', 'AVAILABLE', 10, 21.028511, 105.804817, NOW(), NOW(), NOW()),
(2, 5, 2, 1, 'B2-99887722', 'AVAILABLE', 10, 21.033333, 105.850000, NOW(), NOW(), NOW()),
(3, 8, 6, 2, 'B2-99887733', 'AVAILABLE', 10, 10.776889, 106.700897, NOW(), NOW(), NOW()),
(4, 9, 3, 2, 'C-11223344', 'AVAILABLE', 15, 10.782800, 106.687200, NOW(), NOW(), NOW()),
(5, 10, 5, 1, 'B2-99887755', 'BUSY', 10, 21.018000, 105.828000, NOW(), NOW(), NOW());

-- 5. CUSTOMERS (Khách hàng doanh nghiệp, cửa hàng gửi hàng thương mại)
INSERT IGNORE INTO customers (id, name, phone, email, address, province, district, active, created_at, updated_at) VALUES
(1, 'Cửa Hàng Điện Máy Xanh', '0912345678', 'dmx@retail.vn', '100 Hoàng Hoa Thám', 'Hà Nội', 'Ba Đình', true, NOW(), NOW()),
(2, 'Shop Quần Áo Vintage', '0987654321', 'vintage@shop.vn', '25 Phố Huế', 'Hà Nội', 'Hoàn Kiếm', true, NOW(), NOW()),
(3, 'Thế Giới Di Động Sài Gòn', '0933112233', 'tgdd_sg@retail.vn', '130 Trần Quang Khải', 'TP. Hồ Chí Minh', 'Quận 1', true, NOW(), NOW()),
(4, 'Tiki Trading Logistics', '0944556677', 'tiki_merchant@tiki.vn', '285 Cách Mạng Tháng 8', 'TP. Hồ Chí Minh', 'Quận 10', true, NOW(), NOW()),
(5, 'Nhà Sách Trí Tuệ Đà Nẵng', '0977889900', 'nhasachtritue@danang.vn', '56 Bạch Đằng', 'Đà Nẵng', 'Hải Châu', true, NOW(), NOW()),
(6, 'Nông Sản Sạch Đà Lạt Farm', '0966334455', 'dalat_farm@dalat.vn', '12 Phan Đình Phùng', 'Lâm Đồng', 'Đà Lạt', true, NOW(), NOW());

-- 6. PRICING RULES (Quy tắc tính cước vận chuyển tự động)
INSERT IGNORE INTO pricing_rules (id, region_zone, service_tier, base_price, base_weight_kg, step_price_per_kg, cod_fee_percentage, cod_fee_min, insurance_rate_percentage, created_at, updated_at) VALUES
(1, 'INTRA_PROVINCE', 'STANDARD', 20000.0, 2.0, 5000.0, 0.008, 15000.0, 0.005, NOW(), NOW()),
(2, 'INTRA_PROVINCE', 'EXPRESS', 35000.0, 2.0, 7000.0, 0.008, 15000.0, 0.005, NOW(), NOW()),
(3, 'INTRA_PROVINCE', 'SAME_DAY', 50000.0, 2.0, 10000.0, 0.008, 15000.0, 0.005, NOW(), NOW()),
(4, 'INTER_PROVINCE', 'STANDARD', 35000.0, 1.0, 10000.0, 0.008, 15000.0, 0.005, NOW(), NOW()),
(5, 'INTER_PROVINCE', 'EXPRESS', 60000.0, 1.0, 15000.0, 0.008, 15000.0, 0.005, NOW(), NOW()),
(6, 'SPECIAL', 'STANDARD', 50000.0, 1.0, 12000.0, 0.008, 15000.0, 0.005, NOW(), NOW());

-- 7. SHIPMENTS (20 Đơn vận chuyển mô phỏng trọn vẹn toàn bộ vòng đời trạng thái)
INSERT IGNORE INTO shipments (id, tracking_code, sender_customer_id, sender_name, sender_phone, sender_address, sender_province, receiver_name, receiver_phone, receiver_address, receiver_province, origin_hub_id, destination_hub_id, assigned_driver_id, service_tier, total_weight_kg, volumetric_weight_kg, declared_value, shipping_fee, cod_amount, payment_status, status, delivery_attempt_count, note, version, created_at, updated_at) VALUES
-- 1-2: PENDING (Chờ điều phối / Phân công tài xế)
(1, 'FF-HN240901-001', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Nguyễn Hoàng Long', '0945112233', '45 Nguyễn Trãi, Thanh Xuân', 'Hà Nội', 1, 1, NULL, 'STANDARD', 2.5, 1.8, 500000.0, 25000.0, 450000.0, 'PENDING', 'PENDING', 0, 'Giao giờ hành chính', 0, NOW(), NOW()),
(2, 'FF-HN240901-002', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Trần Mai Anh', '0967889900', '18 Hàng Gai, Hoàn Kiếm', 'Hà Nội', 1, 1, NULL, 'EXPRESS', 1.2, 0.8, 900000.0, 35000.0, 820000.0, 'PENDING', 'PENDING', 0, 'Hàng thời trang cao cấp dễ vỡ', 0, NOW(), NOW()),

-- 3-5: ASSIGNED, PICKED_UP (Khâu tiếp nhận và lấy hàng)
(3, 'FF-HN240901-003', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Lê Tuấn Kiệt', '0934112244', '88 Láng Hạ, Đống Đa', 'Hà Nội', 1, 1, 1, 'STANDARD', 0.8, 0.5, 350000.0, 20000.0, 320000.0, 'PENDING', 'ASSIGNED', 0, 'Đã phân công tài xế Nam', 0, NOW(), NOW()),
(4, 'FF-HN240901-004', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Phan Hương Lan', '0978334455', '12 Thái Hà, Đống Đa', 'Hà Nội', 1, 1, 1, 'EXPRESS', 3.0, 2.4, 1500000.0, 42000.0, 1250000.0, 'PENDING', 'ASSIGNED', 0, 'Tài xế đang di chuyển đến lấy hàng', 0, NOW(), NOW()),
(5, 'FF-HN240901-005', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Vũ Đức Hải', '0919223344', '30 Bà Triệu, Hoàn Kiếm', 'Hà Nội', 1, 1, 2, 'STANDARD', 1.5, 1.0, 700000.0, 20000.0, 670000.0, 'PENDING', 'PICKED_UP', 0, 'Đã lấy hàng từ shop, đang đưa về kho', 0, NOW(), NOW()),

-- 6-8: AT_ORIGIN_HUB, IN_TRANSIT, AT_DEST_HUB (Khâu trung chuyển liên tỉnh & phân loại tại Hub)
(6, 'FF-HN240901-006', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Trịnh Bá Đạt', '0905667788', '12 Lê Duẩn, Hải Châu', 'Đà Nẵng', 1, 3, NULL, 'STANDARD', 5.0, 4.2, 2500000.0, 75000.0, 2100000.0, 'PENDING', 'AT_ORIGIN_HUB', 0, 'Đã nhập kho Hà Nội, chờ xếp xe tải đi Đà Nẵng', 0, NOW(), NOW()),
(7, 'FF-HN240901-007', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Đỗ Thảo Vy', '0938112266', '88 Đồng Khởi, Quận 1', 'TP. Hồ Chí Minh', 1, 2, NULL, 'EXPRESS', 2.0, 1.5, 600000.0, 75000.0, 550000.0, 'PENDING', 'IN_TRANSIT', 0, 'Đang trên xe tải 5T trung chuyển Bắc - Nam', 0, NOW(), NOW()),
(8, 'FF-HCM240902-008', 3, 'Thế Giới Di Động Sài Gòn', '0933112233', '130 Trần Quang Khải', 'TP. Hồ Chí Minh', 'Lý Minh Tâm', '0918776655', '24 Nguyễn Đình Chiểu, Q.3', 'TP. Hồ Chí Minh', 2, 2, NULL, 'SAME_DAY', 1.0, 0.6, 2000000.0, 50000.0, 1800000.0, 'PENDING', 'AT_DEST_HUB', 0, 'Đã cập bến Kho Trung Tâm TP.HCM, chuẩn bị giao', 0, NOW(), NOW()),

-- 9-11: OUT_FOR_DELIVERY (Đang giao hàng - hiển thị ngay trong danh sách nhiệm vụ của Tài Xế!)
(9, 'FF-HN240902-009', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Hoàng Lan Phương', '0977223311', '55 Cầu Giấy, Cầu Giấy', 'Hà Nội', 1, 1, 1, 'STANDARD', 0.9, 0.6, 400000.0, 20000.0, 350000.0, 'PENDING', 'OUT_FOR_DELIVERY', 0, 'Tài xế Nam đang giao hàng (Mô phỏng nút Giao)', 0, NOW(), NOW()),
(10, 'FF-HN240902-010', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Ngô Quốc Huy', '0944332211', '99 Kim Mã, Ba Đình', 'Hà Nội', 1, 1, 2, 'EXPRESS', 2.8, 2.0, 1600000.0, 42000.0, 1450000.0, 'PENDING', 'OUT_FOR_DELIVERY', 0, 'Tài xế Tuấn đang trên đường giao', 0, NOW(), NOW()),
(11, 'FF-HCM240902-011', 3, 'Thế Giới Di Động Sài Gòn', '0933112233', '130 Trần Quang Khải', 'TP. Hồ Chí Minh', 'Phạm Bích Ngọc', '0903887766', '120 Hai Bà Trưng, Quận 1', 'TP. Hồ Chí Minh', 2, 2, 3, 'SAME_DAY', 1.4, 0.9, 3000000.0, 50000.0, 2800000.0, 'PENDING', 'OUT_FOR_DELIVERY', 0, 'Tài xế Hùng đang giao gấp trong ngày tại HCM', 0, NOW(), NOW()),

-- 12-14: DELIVERED (Đã giao thành công, tài xế đã thu tiền COD - Sẵn sàng đối soát!)
(12, 'FF-HN240902-012', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Đinh Quang Sáng', '0989112233', '102 Chùa Bộc, Đống Đa', 'Hà Nội', 1, 1, 1, 'STANDARD', 1.1, 0.7, 550000.0, 20000.0, 500000.0, 'PAID', 'DELIVERED', 1, 'Giao thành công, khách đã nhận hàng', 1, NOW(), NOW()),
(13, 'FF-HN240902-013', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Trần Thảo My', '0966445566', '76 Xã Đàn, Đống Đa', 'Hà Nội', 1, 1, 2, 'EXPRESS', 2.2, 1.8, 1200000.0, 35000.0, 1100000.0, 'PAID', 'DELIVERED', 1, 'Khách thanh toán tiền mặt đầy đủ', 1, NOW(), NOW()),
(14, 'FF-HCM240902-014', 4, 'Tiki Trading Logistics', '0944556677', '285 Cách Mạng Tháng 8', 'TP. Hồ Chí Minh', 'Nguyễn Tấn Dũng', '0913556677', '45 Lê Văn Sỹ, Quận 3', 'TP. Hồ Chí Minh', 2, 2, 3, 'STANDARD', 1.8, 1.2, 800000.0, 20000.0, 750000.0, 'PAID', 'DELIVERED', 1, 'Đã ký nhận và nộp tiền COD', 1, NOW(), NOW()),

-- 15-16: DELIVERY_FAILED (Giao thất bại lần 1, Hẹn lại ngày giao)
(15, 'FF-HN240902-015', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Bùi Văn Hậu', '0922887766', '60 Hoàng Cầu, Đống Đa', 'Hà Nội', 1, 1, 1, 'STANDARD', 1.0, 0.7, 650000.0, 20000.0, 600000.0, 'PENDING', 'DELIVERY_FAILED', 1, 'Giao thất bại lần 1: Khách không nghe máy', 1, NOW(), NOW()),
(16, 'FF-HN240902-016', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Dương Quỳnh Nga', '0937221100', '15 Tây Sơn, Đống Đa', 'Hà Nội', 1, 1, 2, 'EXPRESS', 1.5, 1.1, 950000.0, 35000.0, 890000.0, 'PENDING', 'DELIVERY_FAILED', 1, 'Khách bận công tác, hẹn giao lại vào sáng mai', 1, NOW(), NOW()),

-- 17-19: RETURNED, CANCELLED (Quy trình hoàn hàng và hủy đơn)
(17, 'FF-HN240902-017', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Tạ Đình Phong', '0944778899', '210 Tôn Đức Thắng, Đống Đa', 'Hà Nội', 1, 1, 1, 'STANDARD', 1.3, 0.9, 500000.0, 20000.0, 450000.0, 'PENDING', 'RETURNED', 3, 'Giao thất bại 3 lần -> Tự động chuyển hoàn hàng về shop', 3, NOW(), NOW()),
(18, 'FF-HN240902-018', 1, 'Cửa Hàng Điện Máy Xanh', '0912345678', '100 Hoàng Hoa Thám', 'Hà Nội', 'Nguyễn Thu Trang', '0912998877', '180 Giảng Võ, Ba Đình', 'Hà Nội', 1, 1, 2, 'STANDARD', 0.5, 0.3, 150000.0, 20000.0, 0.0, 'PAID', 'RETURNED', 3, 'Đã trả hàng về kho người gửi thành công', 4, NOW(), NOW()),
(19, 'FF-HN240902-019', 2, 'Shop Quần Áo Vintage', '0987654321', '25 Phố Huế', 'Hà Nội', 'Hoàng Bảo Ngọc', '0988443322', '35 Trần Hưng Đạo, Hoàn Kiếm', 'Hà Nội', 1, 1, NULL, 'STANDARD', 1.0, 0.5, 300000.0, 20000.0, 280000.0, 'PENDING', 'CANCELLED', 0, 'Người gửi hủy đơn trước khi phân công tài xế', 0, NOW(), NOW()),

-- 20: Đơn hàng hoàn tất và đã đối soát COD
(20, 'FF-DN240902-020', 5, 'Nhà Sách Trí Tuệ Đà Nẵng', '0977889900', '56 Bạch Đằng', 'Đà Nẵng', 'Phan Văn Hùng', '0905123456', '230 Hùng Vương, Hải Châu', 'Đà Nẵng', 3, 3, 1, 'STANDARD', 2.0, 1.4, 1600000.0, 25000.0, 1500000.0, 'PAID', 'DELIVERED', 1, 'Đơn hàng hoàn tất trọn vẹn, đã đối soát COD', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 8. SHIPMENT ITEMS (Chi tiết hàng hóa trong từng kiện hàng)
INSERT IGNORE INTO shipment_items (id, shipment_id, item_name, quantity, weight_kg, declared_price) VALUES
(1, 1, 'Nồi cơm điện tử Sharp 1.8L', 1, 2.5, 500000.0),
(2, 2, 'Váy dạ hội Vintage cao cấp', 1, 0.8, 600000.0),
(3, 2, 'Khăn lụa tơ tằm thêu hoa', 1, 0.4, 300000.0),
(4, 3, 'Áo len dệt kim Unisex', 1, 0.8, 350000.0),
(5, 4, 'Bàn ủi hơi nước cầm tay Philips', 1, 1.8, 850000.0),
(6, 4, 'Máy sấy tóc chuyên nghiệp 2200W', 1, 1.2, 650000.0),
(7, 5, 'Quần Jeans cạp cao Retro', 2, 1.5, 700000.0),
(8, 6, 'Lò vi sóng cơ Panasonic 20L', 1, 5.0, 2500000.0),
(9, 7, 'Set 3 Áo phông Basic Organic Cotton', 3, 2.0, 600000.0),
(10, 8, 'Tai nghe không dây Bluetooth Sony ANC', 1, 1.0, 2000000.0),
(11, 9, 'Áo Blazer Hàn Quốc màu Be', 1, 0.9, 400000.0),
(12, 10, 'Nồi chiên không dầu Lock&Lock 5.2L', 1, 2.8, 1600000.0),
(13, 11, 'Điện thoại Samsung Galaxy A54 5G', 1, 1.4, 3000000.0),
(14, 12, 'Giày Sneaker Vintage Classic', 1, 1.1, 550000.0),
(15, 13, 'Quạt lửng đứng Panasonic êm ái', 1, 2.2, 1200000.0),
(16, 14, 'Combo Sách Kinh Tế & Khởi Nghiệp (5 cuốn)', 5, 1.8, 800000.0),
(17, 15, 'Túi xách da nữ phong cách công sở', 1, 1.0, 650000.0),
(18, 16, 'Máy xay sinh tố Tefal 6 lưỡi dao', 1, 1.5, 950000.0),
(19, 17, 'Áo khoác gió chống nước The North Face', 1, 1.3, 500000.0),
(20, 18, 'Củ sạc nhanh Type-C 65W GaN', 1, 0.5, 150000.0),
(21, 19, 'Đầm maxi dạo phố hoa nhí', 1, 1.0, 300000.0),
(22, 20, 'Bộ Bách Khoa Toàn Thư Tri Thức Thế Giới', 3, 2.0, 1600000.0);

-- 9. COD SETTLEMENTS (Lịch sử đối soát tài chính tiền thu hộ)
INSERT IGNORE INTO cod_settlements (id, settlement_code, idempotency_key, driver_id, verified_by_staff_id, total_amount, total_orders, status, settled_at, note, created_at, updated_at) VALUES
(1, 'SETTLE-20260901-HN01', 'IDEM-INIT-001', 1, 3, 1500000.0, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Đối soát đợt 1 thành công cho tài xế Nam - Nộp tiền mặt đủ', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 10. COD TRANSACTIONS (Tiền thu hộ: PENDING, COLLECTED chờ nộp, và VERIFIED đã đối soát)
INSERT IGNORE INTO cod_transactions (id, shipment_id, amount, status, driver_id, settlement_id, collected_at, submitted_at, verified_at, dispute_reason, created_at, updated_at) VALUES
-- Các đơn đang chờ giao (PENDING)
(1, 9, 350000.0, 'PENDING', 1, NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),
(2, 10, 1450000.0, 'PENDING', 2, NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),
(3, 11, 2800000.0, 'PENDING', 3, NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),
-- Các đơn tài xế đã thu tiền mặt từ khách (COLLECTED) -> Đang chờ kế toán đối soát!
(4, 12, 500000.0, 'COLLECTED', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, NOW(), NOW()),
(5, 13, 1100000.0, 'COLLECTED', 2, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, NULL, NOW(), NOW()),
(6, 14, 750000.0, 'COLLECTED', 3, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), NULL, NULL, NOW(), NOW()),
-- Đơn đã hoàn tất đối soát (VERIFIED)
(7, 20, 1500000.0, 'VERIFIED', 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- 11. DELIVERY ATTEMPTS (Lịch sử các lần giao hàng của tài xế)
INSERT IGNORE INTO delivery_attempts (id, shipment_id, driver_id, attempt_number, status, failure_reason, failure_note, proof_image_url, attempted_at, rescheduled_date) VALUES
(1, 12, 1, 1, 'SUCCESS', NULL, 'Khách hàng nhận tận tay, ký nhận đầy đủ', 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=300', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL),
(2, 13, 2, 1, 'SUCCESS', NULL, 'Giao cho bảo vệ toà nhà theo yêu cầu khách', 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=300', DATE_SUB(NOW(), INTERVAL 4 HOUR), NULL),
(3, 14, 3, 1, 'SUCCESS', NULL, 'Khách kiểm tra hàng và thanh toán đủ', 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=300', DATE_SUB(NOW(), INTERVAL 5 HOUR), NULL),
(4, 15, 1, 1, 'FAILED', 'CUSTOMER_UNREACHABLE', 'Gọi 3 cuộc liên tiếp chuông reo nhưng không ai bắt máy', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL),
(5, 16, 2, 1, 'FAILED', 'RESCHEDULE_REQUESTED', 'Khách bận họp quan trọng, yêu cầu giao lại sáng mai lúc 9h', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY)),
(6, 17, 1, 1, 'FAILED', 'CUSTOMER_UNREACHABLE', 'Điện thoại thuê bao không liên lạc được', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
(7, 17, 1, 2, 'FAILED', 'WRONG_ADDRESS', 'Địa chỉ trên đơn sai số nhà, khách đã chuyển đi', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
(8, 17, 1, 3, 'FAILED', 'CUSTOMER_REFUSED', 'Khách đổi ý không muốn nhận hàng nữa', NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL);

-- 12. AUDIT LOGS (Nhật ký kiểm toán thao tác hệ thống)
INSERT IGNORE INTO audit_logs (id, actor_id, actor_username, actor_role, action, entity_name, entity_id, old_data_json, new_data_json, ip_address, performed_at) VALUES
(1, 1, 'admin', 'ROLE_ADMIN', 'INIT_SYSTEM', 'SYSTEM', '1', NULL, '{"status":"READY","version":"1.0.0"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 'staff_hn', 'ROLE_STAFF', 'CREATE_SHIPMENT', 'SHIPMENT', '9', NULL, '{"trackingCode":"FF-HN240902-009","fee":20000.0}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(3, 3, 'staff_hn', 'ROLE_STAFF', 'ASSIGN_DRIVER', 'SHIPMENT', '9', '{"status":"PENDING"}', '{"status":"ASSIGNED","driverId":1,"driverName":"Nguyễn Văn Nam"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(4, 4, 'driver_nam', 'ROLE_DRIVER', 'UPDATE_STATUS', 'SHIPMENT', '9', '{"status":"ASSIGNED"}', '{"status":"OUT_FOR_DELIVERY"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 4, 'driver_nam', 'ROLE_DRIVER', 'DELIVERY_SUCCESS', 'SHIPMENT', '12', '{"status":"OUT_FOR_DELIVERY"}', '{"status":"DELIVERED","codCollected":500000.0}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(6, 3, 'staff_hn', 'ROLE_STAFF', 'SETTLE_COD', 'COD_SETTLEMENT', '1', NULL, '{"settlementCode":"SETTLE-20260901-HN01","amount":1500000.0}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 13. Auto-normalize user roles for backwards compatibility
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'admin' AND (role IS NULL OR role = '' OR role = 'ADMIN');
UPDATE users SET role = 'ROLE_MANAGER' WHERE username LIKE 'manager_%' AND (role IS NULL OR role = '' OR role = 'MANAGER');
UPDATE users SET role = 'ROLE_STAFF' WHERE username LIKE 'staff_%' AND (role IS NULL OR role = '' OR role = 'STAFF');
UPDATE users SET role = 'ROLE_DRIVER' WHERE username LIKE 'driver_%' AND (role IS NULL OR role = '' OR role = 'DRIVER');
UPDATE users SET role = CONCAT('ROLE_', role) WHERE role NOT LIKE 'ROLE_%' AND role IS NOT NULL AND role != '';

-- 14. Auto-normalize legacy shipment statuses and payment statuses
UPDATE shipments SET status = 'ASSIGNED' WHERE status = 'PICKING_UP';
UPDATE shipments SET status = 'DELIVERY_FAILED' WHERE status = 'RESCHEDULED';
UPDATE shipments SET status = 'RETURNED' WHERE status = 'RETURNING';
UPDATE shipments SET payment_status = 'PAID' WHERE payment_status IN ('PAID_BY_SENDER', 'PAID_BY_RECEIVER', 'SETTLED');

