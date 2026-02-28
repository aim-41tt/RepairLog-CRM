package ru.papkov.repairlog.application.service;

/**
 * Константы статусов заявок на поставку.
 * Соответствуют значениям в таблице supply_request_statuses.
 */
public final class SupplyStatusConstants {

    private SupplyStatusConstants() {}

    public static final String NEW = "NEW";
    public static final String AUTO_FORMED = "AUTO_FORMED";
    public static final String APPROVED = "APPROVED";
    public static final String ORDERED = "ORDERED";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String PARTIALLY_DELIVERED = "PARTIALLY_DELIVERED";
    public static final String CANCELLED = "CANCELLED";
}
