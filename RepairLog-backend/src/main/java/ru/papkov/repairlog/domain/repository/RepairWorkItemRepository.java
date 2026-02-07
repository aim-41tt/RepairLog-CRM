package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.RepairWork;
import ru.papkov.repairlog.domain.model.RepairWorkItem;
import ru.papkov.repairlog.domain.model.RepairWorkItemId;

import java.util.List;

/**
 * Repository для работы с запчастями в работах.
 * 
 * @author aim-41tt
 */
@Repository
public interface RepairWorkItemRepository extends JpaRepository<RepairWorkItem, RepairWorkItemId> {

    List<RepairWorkItem> findByRepairWork(RepairWork repairWork);
}
