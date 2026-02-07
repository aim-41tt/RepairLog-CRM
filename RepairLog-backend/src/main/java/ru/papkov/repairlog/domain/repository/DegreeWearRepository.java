package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.DegreeWear;

import java.util.Optional;

/**
 * Repository для работы со степенями износа.
 * 
 * @author aim-41tt
 */
@Repository
public interface DegreeWearRepository extends JpaRepository<DegreeWear, Long> {

    Optional<DegreeWear> findByName(String name);
}
