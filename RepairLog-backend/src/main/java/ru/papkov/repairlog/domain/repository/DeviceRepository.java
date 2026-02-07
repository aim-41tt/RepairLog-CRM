package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Device;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с устройствами.
 * 
 * @author aim-41tt
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySerialNumber(String serialNumber);
    
    List<Device> findByClient(Client client);
    
    List<Device> findByIsClientOwnedTrue();
    
    List<Device> findByIsClientOwnedFalse();
    
    @Query("SELECT d FROM Device d WHERE d.client = :client ORDER BY d.createdAt DESC")
    List<Device> findByClientOrderByCreatedAtDesc(@Param("client") Client client);
}
