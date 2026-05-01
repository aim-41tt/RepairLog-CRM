package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.SupplySetting;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplySettingRepository extends JpaRepository<SupplySetting, Long> {

    @EntityGraph(attributePaths = {"modifiedBy"})
    Optional<SupplySetting> findBySettingKey(String settingKey);

    @EntityGraph(attributePaths = {"modifiedBy"})
    List<SupplySetting> findAll();
}
