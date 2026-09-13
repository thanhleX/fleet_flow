package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.common.exception.DuplicateResourceException;
import com.example.fleetflowbe.dto.request.LoginRequest;
import com.example.fleetflowbe.dto.request.RegisterRequest;
import com.example.fleetflowbe.dto.response.AuthResponse;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.User;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.UserRepository;
import com.example.fleetflowbe.security.JwtTokenProvider;
import com.example.fleetflowbe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("[AUTH-REGISTER] Nhận yêu cầu đăng ký: username='{}', role={}, email='{}'",
                request.getUsername(), request.getRole(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("[AUTH-REGISTER-FAIL] Username '{}' đã tồn tại!", request.getUsername());
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' đã tồn tại");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AUTH-REGISTER-FAIL] Email '{}' đã tồn tại!", request.getEmail());
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        Role assignedRole = request.getRole() != null ? request.getRole().toCanonical() : Role.ROLE_STAFF;

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(assignedRole)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Long driverId = null;
        if (savedUser.getRole() == Role.ROLE_DRIVER) {
            Driver driver = Driver.builder()
                    .user(savedUser)
                    .licenseNumber("LIC-" + savedUser.getUsername().toUpperCase())
                    .build();
            Driver savedDriver = driverRepository.save(driver);
            driverId = savedDriver.getId();
            log.info("[AUTH-REGISTER] Đã tự động tạo Driver profile: driverId={}", driverId);
        }

        UserPrincipal principal = UserPrincipal.create(savedUser, driverId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getUsername());

        log.info("[AUTH-REGISTER-SUCCESS] Đăng ký thành công tài khoản: id={}, username='{}', role={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .driverId(driverId)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("[AUTH-LOGIN] Nhận yêu cầu đăng nhập từ tài khoản: username='{}'", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUsername());

        log.info("[AUTH-LOGIN-SUCCESS] Xác thực thành công: userId={}, username='{}', role={}, driverId={}",
                userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getRole(), userPrincipal.getDriverId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .fullName(userPrincipal.getFullName())
                .role(userPrincipal.getRole())
                .driverId(userPrincipal.getDriverId())
                .build();
    }
}
