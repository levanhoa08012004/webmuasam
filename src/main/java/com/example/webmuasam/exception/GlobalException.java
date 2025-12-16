package com.example.webmuasam.exception;

import java.util.Date;

import com.example.webmuasam.dto.Response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.webmuasam.dto.Response.ApiResponse;

import lombok.Builder;

@RestControllerAdvice
@Builder
public class GlobalException {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        String message = String.format(
                "Parameter '%s' must be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid parameter type")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handlePermissionException(
            Exception e,
            WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }


//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
//            MethodArgumentNotValidException e) {
//        // lấy ra danh sách lỗi
//        BindingResult bindingResult = e.getBindingResult();
//        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//
//        ApiResponse<Object> api = new ApiResponse<Object>();
//        api.setStatusCode(HttpStatus.BAD_REQUEST.value());
//        api.setError(e.getBody().getDetail());
//
//        List<String> errors =
//                fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toUnmodifiableList());
//
//        api.setMessage(errors.size() > 1 ? errors : errors.get(0));
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(api);
//    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource not found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid request body")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Invalid parameter");

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid request parameter")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Incorrect username or password");
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(Exception e) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setMessage(e.getMessage());
        apiResponse.setError("User not found");
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
