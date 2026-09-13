package com.example.fleetflowbe.security;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.User;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[SECURITY-AUTH] Đang xác thực thông tin tài khoản: username='{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[SECURITY-AUTH-FAIL] Không tìm thấy tài khoản: username='{}'", username);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username);
                });

        Long driverId = null;
        if (user.getRole() == Role.ROLE_DRIVER) {
            driverId = driverRepository.findByUserId(user.getId())
                    .map(Driver::getId)
                    .orElse(null);
            log.info("[SECURITY-AUTH] Đã xác định tài khoản DRIVER, driverId gán kèm: {}", driverId);
        }

        log.info("[SECURITY-AUTH-SUCCESS] Nạp thông tin người dùng thành công: userId={}, username='{}', role={}, active={}",
                user.getId(), user.getUsername(), user.getRole(), user.isActive());

        return UserPrincipal.create(user, driverId);
    }
}
