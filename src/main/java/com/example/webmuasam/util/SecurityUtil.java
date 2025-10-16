package com.example.webmuasam.util;


import com.example.webmuasam.dto.Response.LoginResponse;
import com.nimbusds.jose.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;
    @Value("${jwt.signerKey}")
    private String jwtKey;
    @Value("${jwt.valid-duration}")
    private long accessToken;
    @Value("${jwt.refreshable-duration}")
    private long refreshToken;

    //Giải mã token jwtKey và tạo token key
    private SecretKey getSecretKey(){
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }


    public String createAccessToken(String email, LoginResponse loginResponse) {
        LoginResponse.UserInsideToken userToken = new LoginResponse.UserInsideToken();

        userToken.setId(loginResponse.getUser().getId());
        userToken.setEmail(loginResponse.getUser().getEmail());
        userToken.setName(loginResponse.getUser().getName());


        Instant now = Instant.now();
        Instant validity = now.plus(this.accessToken, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user",userToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
    }


    public String createRefreshToken(String email, LoginResponse loginRes) {


        LoginResponse.UserInsideToken userToken = new LoginResponse.UserInsideToken();
        userToken.setId(loginRes.getUser().getId());
        userToken.setEmail(loginRes.getUser().getEmail());
        userToken.setName(loginRes.getUser().getName());

        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshToken, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user",userToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try{
            return jwtDecoder.decode(refreshToken);
        }catch(Exception e){
            System.out.println(">>> refreshToken error : "+ e.getMessage());
            throw e;
        }
    }

    public static Optional<String> getCurrentUserLogin(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }
    private static String extractPrincipal(Authentication authentication) {
        if(authentication == null){
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s){
            return s;
        }
        return null;
    }


    public static Optional<String> getCurrentUserJWT(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String)authentication.getCredentials());
    }
}
