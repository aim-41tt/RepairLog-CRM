package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.device.DeviceResponse;
import ru.papkov.repairlog.application.dto.diagnostic.DiagnosticResponse;
import ru.papkov.repairlog.application.dto.order.RepairOrderResponse;
import ru.papkov.repairlog.application.dto.receipt.ReceiptResponse;
import ru.papkov.repairlog.infrastructure.config.CompanyProperties;
import ru.papkov.repairlog.infrastructure.integration.DocumentApiClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Сервис генерации PDF-документов.
 * Собирает данные из БД и вызывает Document API.
 *
 * @author aim-41tt
 */
@Service
public class DocumentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final RepairOrderService orderService;
    private final ReceiptService receiptService;
    private final DeviceService deviceService;
    private final DiagnosticService diagnosticService;
    private final CompanyProperties company;
    private final DocumentApiClient documentApiClient;

    public DocumentService(RepairOrderService orderService,
                           ReceiptService receiptService,
                           DeviceService deviceService,
                           DiagnosticService diagnosticService,
                           CompanyProperties company,
                           DocumentApiClient documentApiClient) {
        this.orderService = orderService;
        this.receiptService = receiptService;
        this.deviceService = deviceService;
        this.diagnosticService = diagnosticService;
        this.company = company;
        this.documentApiClient = documentApiClient;
    }

    /**
     * Квитанция приёмки устройства.
     */
    @Transactional(readOnly = true)
    public byte[] generateReceipt(Long orderId) {
        RepairOrderResponse order = orderService.getById(orderId);
        DeviceResponse device = deviceService.getById(order.getDeviceId());

        Map<String, Object> request = buildBase(order, device);
        request.put("deviceCondition", order.getExternalCondition());
        request.put("estimatedPrice", order.getTotalAmount());
        request.put("defectDescription", order.getClientComplaint());

        return documentApiClient.generateReceipt(request);
    }

    /**
     * Акт выполненных работ.
     */
    @Transactional(readOnly = true)
    public byte[] generateCompletionAct(Long orderId) {
        RepairOrderResponse order = orderService.getById(orderId);
        DeviceResponse device = deviceService.getById(order.getDeviceId());
        ReceiptResponse receipt = receiptService.getByOrderId(orderId);

        Map<String, Object> request = buildBase(order, device);
        request.put("engineerName", order.getAssignedMasterName());
        request.put("items", mapWorks(receipt.getWorks()));
        request.put("subtotal", receipt.getSubtotal());
        request.put("discount", receipt.getDiscountAmount());
        request.put("total", receipt.getTotalAmount());
        request.put("defectDescription", order.getClientComplaint());

        // Сумма уже оплаченного
        BigDecimal prepayment = receipt.getPayments() != null
                ? receipt.getPayments().stream()
                    .map(ReceiptResponse.PaymentResponse::getPaidAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        request.put("prepayment", prepayment);

        return documentApiClient.generateCompletionAct(request);
    }

    /**
     * Гарантийный талон.
     */
    @Transactional(readOnly = true)
    public byte[] generateWarrantyCard(Long orderId) {
        RepairOrderResponse order = orderService.getById(orderId);
        DeviceResponse device = deviceService.getById(order.getDeviceId());
        ReceiptResponse receipt = receiptService.getByOrderId(orderId);

        Map<String, Object> request = buildBase(order, device);
        request.put("engineerName", order.getAssignedMasterName());
        request.put("items", mapWorks(receipt.getWorks()));
        request.put("subtotal", receipt.getSubtotal());
        request.put("discount", receipt.getDiscountAmount());
        request.put("total", receipt.getTotalAmount());
        request.put("defectDescription", order.getClientComplaint());

        // Примечания из диагностики
        try {
            DiagnosticResponse diag = diagnosticService.getByOrderId(orderId);
            request.put("repairNotes", diag.getSolution());
        } catch (Exception ignored) {
            // Диагностика может отсутствовать
        }

        return documentApiClient.generateWarrantyCard(request);
    }

    /**
     * Отказной лист.
     */
    @Transactional(readOnly = true)
    public byte[] generateRejectionSheet(Long orderId, String rejectionReason) {
        RepairOrderResponse order = orderService.getById(orderId);
        DeviceResponse device = deviceService.getById(order.getDeviceId());

        Map<String, Object> request = buildBase(order, device);
        request.put("rejectionReason", rejectionReason);
        request.put("executorName", order.getAssignedMasterName());
        request.put("defectDescription", order.getClientComplaint());
        request.put("deviceCondition", order.getExternalCondition());

        // Если есть квитанция с работами — добавляем
        try {
            ReceiptResponse receipt = receiptService.getByOrderId(orderId);
            request.put("items", mapWorks(receipt.getWorks()));
            request.put("subtotal", receipt.getSubtotal());
            request.put("discount", receipt.getDiscountAmount());
            request.put("total", receipt.getTotalAmount());
        } catch (Exception ignored) {
            // Квитанция может отсутствовать при отказе
        }

        return documentApiClient.generateRejectionSheet(request);
    }

    // ──────────────────────────────────────────────────────────────────

    private Map<String, Object> buildBase(RepairOrderResponse order, DeviceResponse device) {
        Map<String, Object> map = new HashMap<>();

        // Документ
        map.put("documentNumber", order.getOrderNumber());
        map.put("documentDate", LocalDate.now().format(DATE_FMT));

        // Компания
        map.put("companyName", company.getName());
        map.put("companyAddress", company.getAddress());
        map.put("companyPhone", company.getPhone());
        map.put("companyEmail", company.getEmail());
        map.put("companyInn", company.getInn());
        map.put("companySiteUrl", company.getSiteUrl());
        map.put("companyMessengers", company.getMessengers());

        // Клиент
        map.put("customerName", order.getClientFullName());
        map.put("customerPhone", order.getClientPhone());

        // Устройство
        String deviceName = String.join(" ",
                Optional.ofNullable(device.getDeviceTypeName()).orElse(""),
                Optional.ofNullable(device.getBrandName()).orElse(""),
                Optional.ofNullable(device.getModelName()).orElse("")
        ).trim();
        map.put("deviceName", deviceName);
        map.put("deviceSerial", device.getSerialNumber());

        return map;
    }

    private List<Map<String, Object>> mapWorks(List<ReceiptResponse.RepairWorkResponse> works) {
        if (works == null) return List.of();
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < works.size(); i++) {
            ReceiptResponse.RepairWorkResponse w = works.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("code", String.valueOf(i + 1));
            item.put("name", w.getDescription());
            item.put("quantity", 1);
            item.put("unit", "усл.");
            item.put("price", w.getPrice());
            item.put("amount", w.getPrice());
            items.add(item);
        }
        return items;
    }
}
