package ru.papkov.repairlog.domain.model.enums;

/**
 * Способ оплаты поставщику.
 * Отдельный enum (не переиспользует ReceiptPayment.PaymentMethod),
 * так как расчёты с поставщиками могут иметь расширенный набор методов.
 *
 * @author aim-41tt
 */
public enum SupplierPaymentMethod {
    CASH,           // Наличные
    CARD,           // Корпоративная карта
    TRANSFER,       // Банковский перевод (общий)
    BANK_TRANSFER,  // Банковское платёжное поручение
    INVOICE,        // Оплата по счёту-фактуре
    OTHER           // Иной способ
}
