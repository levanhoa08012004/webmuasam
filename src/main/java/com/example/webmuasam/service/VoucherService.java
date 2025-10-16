package com.example.webmuasam.service;

import com.example.webmuasam.dto.Request.VoucherUpdateDTO;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.VoucherResponse;
import com.example.webmuasam.entity.Voucher;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.VoucherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherService {
    public final VoucherRepository voucherRepository;
    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public Voucher applyVoucher(String code , Double totalPrice) throws AppException {
        LocalDate date=LocalDate.now();
        Voucher voucher = this.voucherRepository.findByCodeAndStatusTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(code,date,date).orElseThrow(()->new AppException("Voucher không hợp lệ"));
        if(totalPrice.compareTo(voucher.getMinOrder()) < 0){
            throw new AppException("Tổng tiền đơn hàng chưa đủ để sử dụng voucher này");
        }
        return voucher;
    }
    public VoucherResponse convertVoucherToVoucherResponse(Voucher voucher) throws AppException {
        VoucherResponse voucherResponse = new VoucherResponse();
        voucherResponse.setId(voucher.getId());
        voucherResponse.setCode(voucher.getCode());
        voucherResponse.setUsedCount(voucher.getUsedCount());
        voucherResponse.setDescription(voucher.getDescription());
        voucherResponse.setMinOrder(voucher.getMinOrder());
        voucherResponse.setDiscountPercent(voucher.getDiscountPercent());
        voucherResponse.setDiscountAmount(voucher.getDiscountAmount());
        voucherResponse.setStartDate(voucher.getStartDate());
        voucherResponse.setEndDate(voucher.getEndDate());
        voucherResponse.setStatus(voucher.getStatus());
        return voucherResponse;
    }
    public VoucherResponse createVoucher(Voucher voucher) throws AppException{
        if(this.voucherRepository.existsByCode(voucher.getCode())) {
            throw new AppException("Code voucher da ton tai");
        }
        if(voucher.getStartDate().isAfter(voucher.getEndDate())){
            throw new AppException("ngày bắt đầu không đươc lớn hơn ngày kết thúc");
        }
        if(voucher.getDiscountPercent()!=0 && voucher.getDiscountPercent()>100){
            throw new AppException("Phần trăm giảm không được quá 100%");
        }
        this.voucherRepository.save(voucher);
        return this.convertVoucherToVoucherResponse(voucher);
    }
    public VoucherResponse updateVoucher(VoucherUpdateDTO voucher) throws AppException{
        voucherRepository.findByCode(voucher.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(voucher.getId())) {
                throw new RuntimeException("Code voucher da ton tai");
            }
        });
        if(voucher.getStartDate().isAfter(voucher.getEndDate())){
            throw new AppException("ngày bắt đầu không đươc lớn hơn ngày kết thúc");
        }
        if(voucher.getDiscountPercent()>100){
            throw new AppException("Phần trăm giảm không được quá 100%");
        }
        Voucher updateVoucher = this.voucherRepository.findById(voucher.getId()).orElseThrow(()-> new AppException("Id voucher not found"));
        if(updateVoucher !=null){
            updateVoucher.setCode(voucher.getCode());
            updateVoucher.setDescription(voucher.getDescription());
            updateVoucher.setMinOrder(voucher.getMinOrder());
            updateVoucher.setStartDate(voucher.getStartDate());
            updateVoucher.setEndDate(voucher.getEndDate());
            updateVoucher.setStatus(voucher.getStatus());
            updateVoucher.setDiscountAmount(voucher.getDiscountAmount());
            updateVoucher.setDiscountPercent(voucher.getDiscountPercent());
            this.voucherRepository.save(updateVoucher);
            return convertVoucherToVoucherResponse(updateVoucher);
        }
        return null;
    }
    public VoucherResponse getVoucherById(@PathVariable Long id) throws AppException {
        return convertVoucherToVoucherResponse(this.voucherRepository.findById(id).orElseThrow(()-> new AppException("Id voucher not found")));
    }



    public ResultPaginationDTO getAllVoucher(Specification<Voucher> spec, Pageable pageable) {
        Page<Voucher> pageVouchers = this.voucherRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(pageVouchers.getTotalElements());
        meta.setPages(pageVouchers.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        List<VoucherResponse> voucherResponses = pageVouchers.getContent().stream().map((v)->{
            VoucherResponse voucherResponse = new VoucherResponse();
            voucherResponse.setId(v.getId());
            voucherResponse.setCode(v.getCode());
            voucherResponse.setDescription(v.getDescription());
            voucherResponse.setUsedCount(v.getUsedCount());
            voucherResponse.setMinOrder(v.getMinOrder());
            voucherResponse.setStartDate(v.getStartDate());
            voucherResponse.setEndDate(v.getEndDate());
            voucherResponse.setStatus(v.getStatus());
            voucherResponse.setDiscountAmount(v.getDiscountAmount());
            voucherResponse.setDiscountPercent(v.getDiscountPercent());
            return voucherResponse;
        }).collect(Collectors.toList());

        resultPaginationDTO.setResult(voucherResponses);
        return resultPaginationDTO;
    }

    public void deleteVoucher(Long id) throws AppException {
        Voucher voucher = this.voucherRepository.findById(id).orElseThrow(()-> new AppException("Id voucher not found"));
        if(voucher.getUsedCount()>0) {
            throw new AppException("Voucher đã được sử dụng");
        }
        this.voucherRepository.delete(voucher);

    }
}
