package ru.papkov.repairlog.domain.exception;

/**
 * Базовое доменное исключение для всех исключений бизнес-логики.
 * 
 * @author aim-41tt
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
