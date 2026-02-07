package ru.papkov.repairlog.domain.exception;

/**
 * Исключение, выбрасываемое при нарушении бизнес-правил.
 * 
 * @author aim-41tt
 */
public class BusinessLogicException extends DomainException {

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
