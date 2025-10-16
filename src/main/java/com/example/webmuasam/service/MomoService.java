package com.example.webmuasam.service;

import ch.qos.logback.core.util.StringUtil;
import com.example.webmuasam.dto.Request.CreateMomoRequest;
import com.example.webmuasam.dto.Request.OrderRequest;
import com.example.webmuasam.dto.Response.CreateMomoRespone;
import com.example.webmuasam.dto.Response.OrderResponse;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.util.annotation.MomoApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class MomoService {

    @Value(value = "${momo.partner-code}")
    private String PARTNER_CODE;

    @Value(value = "${momo.access-key}")
    private String ACCESS_KEY;
    @Value(value = "${momo.secret-key}")
    private String SECRET_KEY;
    @Value(value = "${momo.return-url}")
    private String REDIRECT_URL;
    @Value(value = "${momo.ipn-url}")
    private String IPN_URL;
    @Value(value = "${momo.request-type}")
    private String REQUEST_TYPE;

    private final MomoApi momoApi;
    private final OrderService orderService;
    public MomoService(OrderService orderService, MomoApi momoApi) {
        this.orderService = orderService;
        this.momoApi = momoApi;
    }

    public CreateMomoRespone createQR(OrderRequest request) throws AppException {
        OrderResponse response = this.orderService.checkoutWithVnPay(request);
        String orderId = response.getId().toString();
        String momoOrderId = orderId + "-" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang: " + orderId;
        String requestId = UUID.randomUUID().toString();
        String extraData = "khong co khuyen mai nao het";
        long amount = (long)response.getTotal_price();

        String rawSignature = String.format("accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY,amount,extraData,IPN_URL,momoOrderId,orderInfo,PARTNER_CODE,REDIRECT_URL,requestId,REQUEST_TYPE);
        String prettySignature = "";
        try{
            prettySignature = signHmacSHA256(rawSignature,SECRET_KEY);

        }catch (Exception e){
            log.error(">>>> co loi khi hash code: "+e);
            return null;
        }

        if(prettySignature.isBlank()){
            log.error(">>>> signture is blank");
            return null;
        }

        CreateMomoRequest createMomoRequest = CreateMomoRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(REDIRECT_URL)
                .orderId(momoOrderId)
                .orderInfo(orderInfo)
                .requestId(requestId)
                .extraData(extraData)
                .amount(amount)
                .signature(prettySignature)
                .lang("vi")
                .build();
        return momoApi.createMomoQR(createMomoRequest);


    }

    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for(byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();

    }


}
