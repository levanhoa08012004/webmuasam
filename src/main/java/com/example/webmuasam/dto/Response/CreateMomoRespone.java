package com.example.webmuasam.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMomoRespone {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private long amount;
    private String requestTime;
    private String message;
    private int resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;

}
