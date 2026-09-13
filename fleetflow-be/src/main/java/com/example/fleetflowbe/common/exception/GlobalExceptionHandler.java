package com.example.fleetflowbe.common.exception;

import com.example.fleetflowbe.common.response.ApiError;
import com.example.fleetflowbe.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("[EXCEPTION-BUSINESS] Mã lỗi='{}', Chi tiết='{}'", ex.getErrorCode(), ex.getMessage());
        ApiError error = ApiError.builder()
                .code(ex.getErrorCode())
                .details(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.fail(ex.getMessage(), error));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        String msg = "Xung đột đồng thời: Dữ liệu đơn hàng vừa được cập nhật bởi một người dùng khác. Vui lòng tải lại trang và thử lại.";
        log.warn("[EXCEPTION-OPTIMISTIC-LOCK] {}", ex.getMessage());
        ApiError error = ApiError.builder()
                .code("CONCURRENT_OPTIMISTIC_LOCK_CONFLICT")
                .details(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(msg, error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String causeMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.error("[EXCEPTION-DATA-INTEGRITY] Vi phạm ràng buộc dữ liệu: {}", causeMsg);
        String msg = "Vi phạm ràng buộc dữ liệu (Duplicate Key hoặc Foreign Key Constraint)";
        ApiError error = ApiError.builder()
                .code("DATA_INTEGRITY_VIOLATION")
                .details(causeMsg)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(msg, error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("[EXCEPTION-VALIDATION] Dữ liệu request không hợp lệ: {}", fieldErrors);
        ApiError error = ApiError.builder()
                .code("VALIDATION_FAILED")
                .details("Dữ liệu đầu vào không hợp lệ")
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Lỗi xác thực dữ liệu", error));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("[EXCEPTION-AUTH] Mật khẩu đăng nhập không chính xác");
        ApiError error = ApiError.builder()
                .code("INVALID_CREDENTIALS")
                .details("Mật khẩu không chính xác")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Đăng nhập thất bại: Mật khẩu không đúng", error));
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
        log.warn("[EXCEPTION-AUTH] Không tìm thấy người dùng: {}", ex.getMessage());
        ApiError error = ApiError.builder()
                .code("USER_NOT_FOUND")
                .details(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Tài khoản không tồn tại trên hệ thống", error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[EXCEPTION-ACCESS-DENIED] Bị từ chối truy cập: {}", ex.getMessage());
        ApiError error = ApiError.builder()
                .code("ACCESS_DENIED")
                .details(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Không có quyền truy cập", error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("[EXCEPTION-INTERNAL-500] Lỗi hệ thống nghiêm trọng chưa bắt được: {}", ex.getMessage(), ex);
        ApiError error = ApiError.builder()
                .code("INTERNAL_SERVER_ERROR")
                .details(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Đã có lỗi hệ thống xảy ra: " + ex.getMessage(), error));
    }
}
