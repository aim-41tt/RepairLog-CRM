package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Brand;
import ru.papkov.repairlog.domain.model.Model;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с моделями устройств.
 * 
 * @author aim-41tt
 */
@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    Optional<Model> findByNameAndBrand(String name, Brand brand);
    
    List<Model> findByBrand(Brand brand);
    
    List<Model> findByNameContainingIgnoreCase(String name);
}
