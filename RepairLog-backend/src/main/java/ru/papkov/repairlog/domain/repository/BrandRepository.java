package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Brand;

import java.util.Optional;

/**
 * Repository для работы с брендами устройств.
 * 
 * @author aim-41tt
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByName(String name);
    
    boolean existsByName(String name);
}
