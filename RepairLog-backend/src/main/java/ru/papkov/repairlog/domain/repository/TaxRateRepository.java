package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.TaxRate;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с налоговыми ставками.
 * 
 * @author aim-41tt
 */
@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    Optional<TaxRate> findByName(String name);
    
    List<TaxRate> findByIsActiveTrue();
}
