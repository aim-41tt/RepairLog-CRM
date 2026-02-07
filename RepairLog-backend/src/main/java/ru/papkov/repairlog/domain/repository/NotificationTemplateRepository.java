package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.NotificationTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с шаблонами уведомлений.
 * 
 * @author aim-41tt
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByName(String name);
    
    List<NotificationTemplate> findByTriggerEvent(String triggerEvent);
    
    List<NotificationTemplate> findByIsActiveTrue();
    
    Optional<NotificationTemplate> findByTriggerEventAndIsActiveTrue(String triggerEvent);
}
