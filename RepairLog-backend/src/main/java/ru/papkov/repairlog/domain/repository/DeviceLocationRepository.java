package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Device;
import ru.papkov.repairlog.domain.model.DeviceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с местоположением устройств.
 * 
 * @author aim-41tt
 */
@Repository
public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, Long> {

    List<DeviceLocation> findByDeviceOrderByMovedAtDesc(Device device);
    
    @Query("SELECT dl FROM DeviceLocation dl WHERE dl.device = :device " +
           "ORDER BY dl.movedAt DESC LIMIT 1")
    Optional<DeviceLocation> findCurrentLocation(@Param("device") Device device);
    
    List<DeviceLocation> findByLocation(String location);
}
