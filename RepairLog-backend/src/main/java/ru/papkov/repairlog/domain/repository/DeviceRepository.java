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

    @Query("SELECT d FROM Device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.serialNumber = :serialNumber")
    Optional<Device> findBySerialNumber(@Param("serialNumber") String serialNumber);

    @Query("SELECT d FROM Device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.client = :client " +
           "ORDER BY d.createdAt DESC")
    List<Device> findByClient(@Param("client") Client client);

    @Query("SELECT d FROM Device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.id = :id")
    Optional<Device> findByIdWithDetails(@Param("id") Long id);

    List<Device> findByIsClientOwnedTrue();

    List<Device> findByIsClientOwnedFalse();

    @Query("SELECT d FROM Device d " +
           "LEFT JOIN FETCH d.deviceType " +
           "LEFT JOIN FETCH d.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.client = :client ORDER BY d.createdAt DESC")
    List<Device> findByClientOrderByCreatedAtDesc(@Param("client") Client client);
}
