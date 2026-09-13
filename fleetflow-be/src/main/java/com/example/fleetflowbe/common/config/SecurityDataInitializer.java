package com.example.fleetflowbe.common.config;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.User;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[DATA-INIT] Bắt đầu đồng bộ mật khẩu chuẩn 'password123' và quyền cho hệ thống...");

        // Quản trị viên
        initOrUpdateUser("admin", "password123", "System Administrator", "0901000001", "admin@fleetflow.io", Role.ROLE_ADMIN, null);

        // Quản lý các chi nhánh Hub
        initOrUpdateUser("manager_hn", "password123", "Trần Quản Lý (HN)", "0901000002", "manager_hn@fleetflow.io", Role.ROLE_MANAGER, null);
        initOrUpdateUser("manager_hcm", "password123", "Nguyễn Quản Lý (HCM)", "0901000006", "manager_hcm@fleetflow.io", Role.ROLE_MANAGER, null);

        // Điều phối viên kho / văn phòng
        initOrUpdateUser("staff_hn", "password123", "Lê Điều Phối (HN)", "0901000003", "staff_hn@fleetflow.io", Role.ROLE_STAFF, null);
        initOrUpdateUser("staff_hcm", "password123", "Võ Điều Phối (HCM)", "0901000007", "staff_hcm@fleetflow.io", Role.ROLE_STAFF, null);

        // Đội ngũ tài xế giao hàng
        initOrUpdateUser("driver_nam", "password123", "Nguyễn Văn Nam (Driver HN)", "0901000004", "driver_nam@fleetflow.io", Role.ROLE_DRIVER, "B2-99887711");
        initOrUpdateUser("driver_tuan", "password123", "Phạm Tuấn (Driver HN)", "0901000005", "driver_tuan@fleetflow.io", Role.ROLE_DRIVER, "B2-99887722");
        initOrUpdateUser("driver_hung", "password123", "Trần Quang Hùng (Driver HCM)", "0901000008", "driver_hung@fleetflow.io", Role.ROLE_DRIVER, "B2-99887733");
        initOrUpdateUser("driver_minh", "password123", "Đặng Nhật Minh (Driver HCM)", "0901000009", "driver_minh@fleetflow.io", Role.ROLE_DRIVER, "C-11223344");
        initOrUpdateUser("driver_hoang", "password123", "Lý Minh Hoàng (Driver HN)", "0901000010", "driver_hoang@fleetflow.io", Role.ROLE_DRIVER, "B2-99887755");

        log.info("[DATA-INIT] Hoàn tất đồng bộ tài khoản mẫu! Tất cả 10 tài khoản có mật khẩu chuẩn: 'password123'");
    }

    private void initOrUpdateUser(String username, String rawPassword, String fullName, String phone, String email, Role role, String licenseNumber) {
        User user = userRepository.findByUsername(username).orElse(null);
        String encodedPass = passwordEncoder.encode(rawPassword);

        if (user == null) {
            log.info("[DATA-INIT] Tạo mới tài khoản: username='{}', role={}", username, role);
            user = User.builder()
                    .username(username)
                    .passwordHash(encodedPass)
                    .fullName(fullName)
                    .phone(phone)
                    .email(email)
                    .role(role)
                    .active(true)
                    .build();
            user = userRepository.save(user);
        } else {
            log.info("[DATA-INIT] Cập nhật hash mật khẩu mới và role chuẩn cho tài khoản: username='{}', role={}", username, role);
            user.setPasswordHash(encodedPass);
            user.setRole(role);
            user.setActive(true);
            user = userRepository.save(user);
        }

        if (role == Role.ROLE_DRIVER) {
            final User driverUser = user;
            driverRepository.findByUserId(driverUser.getId()).ifPresentOrElse(
                    d -> {
                        if (d.getLicenseNumber() == null || d.getLicenseNumber().isBlank()) {
                            d.setLicenseNumber(licenseNumber != null ? licenseNumber : "LIC-" + username.toUpperCase());
                            driverRepository.save(d);
                        }
                    },
                    () -> {
                        Driver driver = Driver.builder()
                                .user(driverUser)
                                .licenseNumber(licenseNumber != null ? licenseNumber : "LIC-" + username.toUpperCase())
                                .build();
                        driverRepository.save(driver);
                    }
            );
        }
    }
}
