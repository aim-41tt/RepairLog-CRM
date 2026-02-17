package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.DeviceType;

import java.util.Optional;

/**
 * Repository для работы с типами устройств.
 * 
 * @author aim-41tt
 */
@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {

    Optional<DeviceType> findByName(String name);
    
    boolean existsByName(String name);
}
