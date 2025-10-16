package com.example.webmuasam.config;

import com.example.webmuasam.entity.Permission;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.PermissionException;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;


    //dùng transactional ở đây là vì class này chưa chạy đến controller
    //nên chưa có một phiên đăng nhập nào đến database nên cần
    //phải dùng transactional để cho java tạo một phiên đăng nhập khi nào kết thúc thì xóa
    @Transactional
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    )throws Exception{
        String path = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI= request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHanle");
        System.out.println(">>> path= "+path);
        System.out.println(">>> httpMethod= "+httpMethod);
        System.out.println(">>> requestURI= "+requestURI);

        //check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent()?SecurityUtil.getCurrentUserLogin().get():null;
        if(email!=null && !email.isEmpty()){
            User user = this.userService.handleGetUserByUserName(email);
            if(user!=null){
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissionList =role.getPermissions();
                    boolean isAllow = permissionList.stream()
                            .anyMatch(item ->
                                    item.getApiPath().equals(path)
                                            && item.getMethod().equals(httpMethod)
                            );
                    if(isAllow == false){
                        throw new PermissionException("Bạn không có quyền truy cập endpoint này");
                    }
                }else{
                    throw new PermissionException("Bạn không có quyền truy cập endpoint này");
                }
            }
        }

        return true;
    }
}
