package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.DiscountType;

import java.util.Optional;

/**
 * Repository для работы с типами скидок.
 * 
 * @author aim-41tt
 */
@Repository
public interface DiscountTypeRepository extends JpaRepository<DiscountType, Long> {

    Optional<DiscountType> findByName(String name);
}
