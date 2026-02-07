package ru.papkov.repairlog.domain.exception;

/**
 * Исключение, выбрасываемое когда запрашиваемая сущность не найдена в БД.
 * 
 * @author aim-41tt
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s с ID %d не найден", entityName, id));
    }

    public EntityNotFoundException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s с %s = %s не найден", entityName, fieldName, fieldValue));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
