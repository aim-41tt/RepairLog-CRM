package ru.papkov.repairlog.application.service;

/**
 * Коды статусов заказов на ремонт. Дублируют поле {@code code} в таблице {@code repair_statuses}.
 * Использовать вместо магических строк при сравнении статусов в бизнес-логике.
 *
 * @author aim-41tt
 */
public final class RepairOrderStatusCode {

    private RepairOrderStatusCode() { }

    public static final String NEW = "NEW";
    public static final String DIAGNOSTIC = "DIAGNOSTIC";
    public static final String IN_REPAIR = "IN_REPAIR";
    public static final String WAITING_PARTS = "WAITING_PARTS";
    public static final String READY = "READY";
    public static final String ISSUED = "ISSUED";
    public static final String CANCELLED = "CANCELLED";
}
