package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.domain.model.Device;
import ru.papkov.repairlog.domain.model.Diagnostic;
import ru.papkov.repairlog.domain.model.Receipt;
import ru.papkov.repairlog.domain.model.ReceiptPayment;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.model.RepairWork;
import ru.papkov.repairlog.infrastructure.config.CompanyProperties;
import ru.papkov.repairlog.infrastructure.integration.DocumentApiClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Сервис генерации PDF-документов.
 * Собирает данные из БД через сервисы (entity) и вызывает Document API.
 *
 * @author aim-41tt
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
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
        RepairOrder order = orderService.getById(orderId);
        Device device = order.getDevice();

        Map<String, Object> request = buildBase(order, device);
        request.put("deviceCondition", order.getExternalCondition());
        // В entity totalAmount живёт в Receipt, не в RepairOrder
        try {
            Receipt receipt = receiptService.getByOrderId(orderId);
            request.put("estimatedPrice", receipt.getTotalAmount());
        } catch (Exception e) {
            // Чек может ещё не существовать на этапе приёмки — это штатная ситуация
            log.debug("Receipt not yet available for order {}: {}", orderId, e.getMessage());
        }
        request.put("defectDescription", order.getClientComplaint());

        return documentApiClient.generateReceipt(request);
    }

    /**
     * Акт выполненных работ.
     */
    @Transactional(readOnly = true)
    public byte[] generateCompletionAct(Long orderId) {
        RepairOrder order = orderService.getById(orderId);
        Device device = order.getDevice();
        Receipt receipt = receiptService.getByOrderId(orderId);
        List<RepairWork> works = receiptService.getWorksByReceipt(receipt);
        List<ReceiptPayment> payments = receiptService.getPaymentsByReceipt(receipt);

        Map<String, Object> request = buildBase(order, device);
        request.put("engineerName", order.getAssignedMaster() != null
                ? order.getAssignedMaster().getFullName() : null);
        request.put("items", mapWorks(works));
        request.put("subtotal", receipt.getSubtotal());
        request.put("discount", receipt.getDiscountAmount());
        request.put("total", receipt.getTotalAmount());
        request.put("defectDescription", order.getClientComplaint());

        // Сумма уже оплаченного
        BigDecimal prepayment = payments.stream()
                .map(ReceiptPayment::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        request.put("prepayment", prepayment);

        return documentApiClient.generateCompletionAct(request);
    }

    /**
     * Гарантийный талон.
     */
    @Transactional(readOnly = true)
    public byte[] generateWarrantyCard(Long orderId) {
        RepairOrder order = orderService.getById(orderId);
        Device device = order.getDevice();
        Receipt receipt = receiptService.getByOrderId(orderId);
        List<RepairWork> works = receiptService.getWorksByReceipt(receipt);

        Map<String, Object> request = buildBase(order, device);
        request.put("engineerName", order.getAssignedMaster() != null
                ? order.getAssignedMaster().getFullName() : null);
        request.put("items", mapWorks(works));
        request.put("subtotal", receipt.getSubtotal());
        request.put("discount", receipt.getDiscountAmount());
        request.put("total", receipt.getTotalAmount());
        request.put("defectDescription", order.getClientComplaint());

        // Примечания из диагностики
        try {
            Diagnostic diag = diagnosticService.getByOrderId(orderId);
            request.put("repairNotes", diag.getSolution());
        } catch (Exception e) {
            // Диагностика может отсутствовать — это штатная ситуация
            log.debug("Diagnostic not available for order {}: {}", orderId, e.getMessage());
        }

        return documentApiClient.generateWarrantyCard(request);
    }

    /**
     * Отказной лист.
     */
    @Transactional(readOnly = true)
    public byte[] generateRejectionSheet(Long orderId, String rejectionReason) {
        RepairOrder order = orderService.getById(orderId);
        Device device = order.getDevice();

        Map<String, Object> request = buildBase(order, device);
        request.put("rejectionReason", rejectionReason);
        request.put("executorName", order.getAssignedMaster() != null
                ? order.getAssignedMaster().getFullName() : null);
        request.put("defectDescription", order.getClientComplaint());
        request.put("deviceCondition", order.getExternalCondition());

        // Если есть квитанция с работами — добавляем
        try {
            Receipt receipt = receiptService.getByOrderId(orderId);
            List<RepairWork> works = receiptService.getWorksByReceipt(receipt);
            request.put("items", mapWorks(works));
            request.put("subtotal", receipt.getSubtotal());
            request.put("discount", receipt.getDiscountAmount());
            request.put("total", receipt.getTotalAmount());
        } catch (Exception ignored) {
            // Квитанция может отсутствовать при отказе
        }

        return documentApiClient.generateRejectionSheet(request);
    }

    // ──────────────────────────────────────────────────────────────────

    private Map<String, Object> buildBase(RepairOrder order, Device device) {
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
        if (order.getClient() != null) {
            map.put("customerName", order.getClient().getFullName());
            map.put("customerPhone", order.getClient().getPhone());
        }

        // Устройство
        if (device != null) {
            String deviceTypeName = device.getDeviceType() != null ? device.getDeviceType().getName() : null;
            String brandName = (device.getModel() != null && device.getModel().getBrand() != null)
                    ? device.getModel().getBrand().getName() : null;
            String modelName = device.getModel() != null ? device.getModel().getName() : null;

            String deviceName = String.join(" ",
                    Optional.ofNullable(deviceTypeName).orElse(""),
                    Optional.ofNullable(brandName).orElse(""),
                    Optional.ofNullable(modelName).orElse("")
            ).trim();
            map.put("deviceName", deviceName);
            map.put("deviceSerial", device.getSerialNumber());
        }

        return map;
    }

    private List<Map<String, Object>> mapWorks(List<RepairWork> works) {
        if (works == null) return List.of();
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < works.size(); i++) {
            RepairWork w = works.get(i);
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
