package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.RepairPriority;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с приоритетами заказов.
 * 
 * @author aim-41tt
 */
@Repository
public interface RepairPriorityRepository extends JpaRepository<RepairPriority, Long> {

    Optional<RepairPriority> findByName(String name);
    
    List<RepairPriority> findAllByOrderBySortOrderAsc();
}
