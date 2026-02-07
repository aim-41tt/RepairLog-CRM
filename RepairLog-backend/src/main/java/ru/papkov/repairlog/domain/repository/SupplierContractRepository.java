package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.SupplierContract;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с договорами поставщиков.
 * 
 * @author aim-41tt
 */
@Repository
public interface SupplierContractRepository extends JpaRepository<SupplierContract, Long> {

    Optional<SupplierContract> findByContractNumber(String contractNumber);
    
    List<SupplierContract> findBySupplier(Supplier supplier);
    
    List<SupplierContract> findByIsActiveTrue();
    
    @Query("SELECT sc FROM SupplierContract sc WHERE sc.isActive = true " +
           "AND (sc.validUntil IS NULL OR sc.validUntil >= :currentDate)")
    List<SupplierContract> findValidContracts(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT sc FROM SupplierContract sc WHERE sc.supplier = :supplier " +
           "AND sc.isActive = true " +
           "AND (sc.validUntil IS NULL OR sc.validUntil >= :currentDate)")
    List<SupplierContract> findValidContractsBySupplier(@Param("supplier") Supplier supplier,
                                                          @Param("currentDate") LocalDate currentDate);
}
