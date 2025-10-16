package com.example.webmuasam.exception;

import com.example.webmuasam.entity.ApiResponse;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Builder
public class GlobalException  {

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
//        ApiResponse<Object> apiResponse = new ApiResponse<>();
//        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
//        apiResponse.setMessage(e.getMessage());
//        apiResponse.setError("apvalidtion");
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
//
//    }
    @ExceptionHandler(value = PermissionException.class)
    public ResponseEntity<ApiResponse<Object>> handlePermissionException(Exception e) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
        apiResponse.setMessage(e.getMessage());
        apiResponse.setError("Bạn không có quyền truy cập");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        //lấy ra danh sách lỗi
        BindingResult bindingResult = e.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        ApiResponse<Object> api= new ApiResponse<Object>();
        api.setStatusCode(HttpStatus.BAD_REQUEST.value());
        api.setError(e.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(
                f -> f.getDefaultMessage()).collect(Collectors.toUnmodifiableList());

        api.setMessage(errors.size()>1 ?errors : errors.get(0));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(api);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Sai username hoặc password");
    }
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(Exception e) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setMessage(e.getMessage());
        apiResponse.setError("lỗi người dùng");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

    }
//    @ExceptionHandler(value = {
//            StoregeException.class
//    } )
//    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(Exception e) {
//        ApiResponse<Object> apiResponse = ApiResponse.builder()
//                .statusCode(HttpStatus.BAD_REQUEST.value())
//                .message(e.getMessage())
//                .error("Exception upload file")
//                .build();
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
//    }
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException e) {
        ApiResponse<Object> api = new ApiResponse<Object>();
        api.setStatusCode(HttpStatus.NOT_FOUND.value());
        api.setMessage(e.getMessage());
        api.setError("404 not Found...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(api);
    }

}
