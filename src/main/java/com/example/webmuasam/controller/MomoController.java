package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.OrderRequest;
import com.example.webmuasam.dto.Response.CreateMomoRespone;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.MomoService;
import com.example.webmuasam.service.OrderService;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.constant.MomoParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/momo")
public class MomoController {

    private final MomoService momoService;
    private final OrderService orderService;
    public MomoController(MomoService momoService, OrderService orderService) {
        this.momoService = momoService;
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public CreateMomoRespone createQR(OrderRequest request)throws AppException {
        return momoService.createQR(request);
    }

    @GetMapping("/ipn-handler")
    public String ipnHandler(@RequestParam Map<String,String> params) throws AppException{
        Integer resultCode = Integer.valueOf(params.get(MomoParameter.RESULT_CODE));
        Long orderId= Long.valueOf(params.get(MomoParameter.ORDER_ID).split("-")[0]);
        if(resultCode == 0){
            this.orderService.confirmVnPayPayment(orderId,true);
            return "success";
        }else{
            return "fail";
        }
    }
}
