package com.example.webmuasam.controller;

import java.util.Base64;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.webmuasam.dto.Request.ChangePasswordRequest;
import com.example.webmuasam.dto.Request.LoginRequest;
import com.example.webmuasam.dto.Response.CreateUserResponse;
import com.example.webmuasam.dto.Response.LoginResponse;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.EmailService;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.annotation.ApiMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${jwt.refreshable-duration}")
    private long refreshToken_duration;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil,
            UserService userService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginResponse res = new LoginResponse();
        User currentUserDB = null;
        try {
            currentUserDB = this.userService.handleGetUserByUserName(loginRequest.getUsername());
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        if (currentUserDB != null) {
            String base64Images = null;
            if (currentUserDB.getImage() != null) {
                base64Images = Base64.getEncoder().encodeToString(currentUserDB.getImage());
            } else {
                // Có thể set ảnh mặc định
                base64Images = ""; // hoặc base64 của avatar mặc định
            }
            LoginResponse.UserLogin userLogin = new LoginResponse.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getUsername(),
                    currentUserDB.getRole(),
                    base64Images);
            res.setUser(userLogin);
        }

        // create Token
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);
        String refreshToken = this.securityUtil.createRefreshToken(loginRequest.getUsername(), res);

        // update token
        this.userService.updateUserToken(refreshToken, loginRequest.getUsername());

        // set cookies
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshToken_duration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @GetMapping("/account")
    @ApiMessage("fetch account")
    public ResponseEntity<LoginResponse.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        User currentUserDB = null;
        try {
            currentUserDB = this.userService.handleGetUserByUserName(email);
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        LoginResponse.UserGetAccount userGetAccount = new LoginResponse.UserGetAccount();
        if (currentUserDB != null) {
            userGetAccount.setId(currentUserDB.getId());
            userGetAccount.setEmail(currentUserDB.getEmail());
            userGetAccount.setName(currentUserDB.getUsername());
            userGetAccount.setRole(currentUserDB.getRole());
            userGetAccount.setAddress(currentUserDB.getAddress());
            userGetAccount.setGender(currentUserDB.getGender());
            if (currentUserDB.getImage() != null) {
                userGetAccount.setImage(Base64.getEncoder().encodeToString(currentUserDB.getImage()));
            }
        }
        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/refresh")
    @ApiMessage("get user by refresh token")
    public ResponseEntity<LoginResponse> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken) throws AppException {
        if (refreshToken.equals("abc")) {
            throw new AppException("bạn không có refresh token ở cookie");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User currentUserDB = this.userService.getUserByFreshTokenAndEmail(email, refreshToken);
        if (currentUserDB == null) {
            throw new AppException("Token không hợp lệ");
        }
        LoginResponse loginResponse = new LoginResponse();
        User currentUser = this.userService.handleGetUserByUserName(email);
        if (currentUser != null) {
            String base64Images = null;
            if (currentUserDB.getImage() != null) {
                base64Images = Base64.getEncoder().encodeToString(currentUserDB.getImage());
            } else {
                // Có thể set ảnh mặc định
                base64Images = ""; // hoặc base64 của avatar mặc định
            }
            LoginResponse.UserLogin userLogin = new LoginResponse.UserLogin(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    currentUser.getRole(),
                    base64Images);
            loginResponse.setUser(userLogin);
        }
        String accessToken = this.securityUtil.createAccessToken(email, loginResponse);

        loginResponse.setAccessToken(accessToken);

        this.userService.updateUserToken(accessToken, email);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshToken_duration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/logout")
    @ApiMessage("logout user")
    public ResponseEntity<Void> logout() throws AppException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (email.equals("")) {
            throw new AppException("Access token không hợp lệ");
        }

        this.userService.updateUserToken(null, email);
        ResponseCookie deleteRefreshToken = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshToken.toString())
                .build();
    }

    @PostMapping("/register")
    @ApiMessage("Register a user")
    public ResponseEntity<CreateUserResponse> register(@Valid @RequestBody User user) throws AppException {

        return ResponseEntity.ok(this.userService.CreateUser(user));
    }

    @PutMapping("/change-password")
    @ApiMessage("Change password user")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        User user = this.userService.handleGetUserByUserName(email);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu không chính xác");
        }
        this.userService.changePassword(user, request.getNewPassword());
        return ResponseEntity.ok().body("Đổi mật khẩu thành công");
    }

    @GetMapping("/email")
    @ApiMessage("Send email success")
    public String forgotPassword(@RequestParam String email) throws AppException {
        log.info(email);
        int randomPin = (int) (Math.random() * 900000) + 100000;
        String newpassword = String.valueOf(randomPin);
        User user = this.userService.handleGetUserByUserName(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newpassword));
            userRepository.save(user);
        }
        String htmlContent =
                """
		<div style="font-family:Arial,Helvetica,sans-serif;max-width:520px;margin:0 auto;">
		<h2 style="color:#2563eb;">Đặt lại mật khẩu</h2>
		<p>Xin chào <b>%s</b>,</p>
		<p>Mật khẩu mới của bạn là:</p>
		<div style="padding:10px 16px;border:1px dashed #999;
					display:inline-block;font-size:18px;font-weight:bold;">
			%s
		</div>
		<p>Vui lòng đăng nhập và đổi lại mật khẩu ngay.</p>
		</div>
		"""
                        .formatted(user.getUsername(), newpassword);
        this.emailService.sendEmailSync(email, "Mật khẩu mới của bạn", htmlContent, false, true);
        return "ok";
    }
}
