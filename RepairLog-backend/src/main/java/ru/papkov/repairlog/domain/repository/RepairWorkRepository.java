package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.Receipt;
import ru.papkov.repairlog.domain.model.RepairWork;

import java.util.List;

/**
 * Repository для работы с выполненными работами.
 * 
 * @author aim-41tt
 */
@Repository
public interface RepairWorkRepository extends JpaRepository<RepairWork, Long> {

    List<RepairWork> findByReceipt(Receipt receipt);
    
    List<RepairWork> findByEmployee(Employee employee);
}
