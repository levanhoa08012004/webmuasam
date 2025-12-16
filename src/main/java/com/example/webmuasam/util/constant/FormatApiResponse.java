package com.example.webmuasam.util.constant;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.example.webmuasam.dto.Response.ApiResponse;
import com.example.webmuasam.util.annotation.ApiMessage;

@RestControllerAdvice
public class FormatApiResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        HttpServletResponse httpResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = httpResponse.getStatus();
        ApiResponse<Object> apiResponse = new ApiResponse<Object>();
        apiResponse.setStatusCode(status);
        if (body instanceof String || body instanceof Resource || body instanceof ApiResponse) {
            return body;
        }
        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        if (status >= 400) {
            return body;
        } else {
            apiResponse.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            apiResponse.setMessage(message != null ? message.value() : "CALL API SUCCESS");
        }
        return apiResponse;
    }
}
