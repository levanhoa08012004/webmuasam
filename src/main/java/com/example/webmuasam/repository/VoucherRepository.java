package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> , JpaSpecificationExecutor<Voucher> {
    boolean existsByCode(String code);

    @Override
    Page<Voucher> findAll(Specification<Voucher> spec, Pageable pageable);

    Optional<Voucher> findByCodeAndStatusTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String code, LocalDate startDate, LocalDate endDate);

    Optional<Voucher> findByCode(String code);
}
