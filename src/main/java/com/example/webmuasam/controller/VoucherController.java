package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.VoucherUpdateDTO;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.VoucherResponse;
import com.example.webmuasam.entity.Voucher;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.VoucherService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/v1/voucher")
public class VoucherController {
    public final VoucherService voucherService;
    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@RequestBody  Voucher voucher) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.voucherService.createVoucher(voucher));
    }

    @PutMapping
    public ResponseEntity<VoucherResponse> updateVoucher(@RequestBody VoucherUpdateDTO request) throws AppException {
        return ResponseEntity.ok(this.voucherService.updateVoucher(request));
    }
    @GetMapping("/check")
    public ResponseEntity<Voucher> applyVoucher(@RequestParam String code, @RequestParam Double totalPrice) throws AppException {
        return ResponseEntity.ok(this.voucherService.applyVoucher(code,totalPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable("id") Long id) throws AppException {
        return ResponseEntity.ok(this.voucherService.getVoucherById(id));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllVouchers(@Filter Specification<Voucher> spec, Pageable pageable) {
        return ResponseEntity.ok(this.voucherService.getAllVoucher(spec,pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable("id") Long id) throws AppException {
        this.voucherService.deleteVoucher(id);
        return ResponseEntity.ok("success");
    }

}
