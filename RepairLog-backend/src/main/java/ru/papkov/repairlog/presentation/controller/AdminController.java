package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.audit.AuditLogResponse;
import ru.papkov.repairlog.application.dto.client.ClientResponse;
import ru.papkov.repairlog.application.dto.common.PageResponse;
import ru.papkov.repairlog.application.dto.employee.CreateEmployeeRequest;
import ru.papkov.repairlog.application.dto.employee.EmployeeResponse;
import ru.papkov.repairlog.application.dto.employee.UpdateEmployeeRequest;
import ru.papkov.repairlog.application.dto.inventory.CreateInventoryItemRequest;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
import ru.papkov.repairlog.application.dto.inventory.ReceiveStockRequest;
import ru.papkov.repairlog.application.dto.notification.NotificationResponse;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.application.dto.supply.*;
import ru.papkov.repairlog.application.mapper.AuditLogMapper;
import ru.papkov.repairlog.application.mapper.ClientMapper;
import ru.papkov.repairlog.application.mapper.EmployeeMapper;
import ru.papkov.repairlog.application.mapper.InventoryItemMapper;
import ru.papkov.repairlog.application.mapper.NotificationMapper;
import ru.papkov.repairlog.application.mapper.SupplierMapper;
import ru.papkov.repairlog.application.mapper.SupplyRequestMapper;
import ru.papkov.repairlog.application.mapper.SupplySettingMapper;
import ru.papkov.repairlog.application.service.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.infrastructure.security.audit.AuditLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Контроллер администратора.
 * Управление сотрудниками, поставщиками, заявками на поставку, складом, аудитом.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Администратор", description = "Управление сотрудниками, складом, поставками, аудит")
public class AdminController {

    private final EmployeeService employeeService;
    private final AuthenticationService authService;
    private final ClientService clientService;
    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final SupplyRequestService supplyRequestService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final SupplyDashboardService supplyDashboardService;
    private final SupplySettingService supplySettingService;
    private final AuditLogger auditLogger;
    private final AuditLogMapper auditLogMapper;
    private final ClientMapper clientMapper;
    private final SupplierMapper supplierMapper;
    private final EmployeeMapper employeeMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final SupplyRequestMapper supplyRequestMapper;
    private final SupplySettingMapper supplySettingMapper;
    private final NotificationMapper notificationMapper;

    public AdminController(EmployeeService employeeService,
                           AuthenticationService authService,
                           ClientService clientService,
                           InventoryService inventoryService,
                           SupplierService supplierService,
                           SupplyRequestService supplyRequestService,
                           NotificationService notificationService,
                           AuditLogService auditLogService,
                           SupplyDashboardService supplyDashboardService,
                           SupplySettingService supplySettingService,
                           AuditLogger auditLogger,
                           AuditLogMapper auditLogMapper,
                           ClientMapper clientMapper,
                           SupplierMapper supplierMapper,
                           EmployeeMapper employeeMapper,
                           InventoryItemMapper inventoryItemMapper,
                           SupplyRequestMapper supplyRequestMapper,
                           SupplySettingMapper supplySettingMapper,
                           NotificationMapper notificationMapper) {
        this.employeeService = employeeService;
        this.authService = authService;
        this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.supplyRequestService = supplyRequestService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.supplyDashboardService = supplyDashboardService;
        this.supplySettingService = supplySettingService;
        this.auditLogger = auditLogger;
        this.auditLogMapper = auditLogMapper;
        this.clientMapper = clientMapper;
        this.supplierMapper = supplierMapper;
        this.employeeMapper = employeeMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.supplyRequestMapper = supplyRequestMapper;
        this.supplySettingMapper = supplySettingMapper;
        this.notificationMapper = notificationMapper;
    }

    // ==================== Сотрудники ====================

    @GetMapping("/employees")
    @Operation(summary = "Список всех сотрудников")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable).map(employeeMapper::toResponse));
    }

    @GetMapping("/employees/{id}")
    @Operation(summary = "Получить сотрудника по ID")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeMapper.toResponse(employeeService.getById(id)));
    }

    @GetMapping("/employees/role/{roleName}")
    @Operation(summary = "Получить сотрудников по роли")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(employeeMapper.toResponseList(employeeService.getByRole(roleName)));
    }

    @PostMapping("/employees")
    @Operation(summary = "Создать сотрудника")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeMapper.toResponse(employeeService.create(request)));
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Обновить данные сотрудника")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeMapper.toResponse(employeeService.update(id, request)));
    }

    @PatchMapping("/employees/{id}/password")
    @Operation(summary = "Установить пароль сотруднику")
    public ResponseEntity<Map<String, String>> setPassword(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        employeeService.setPassword(id, body.get("password"));
        return ResponseEntity.ok(Map.of("message", "Пароль установлен"));
    }

    @PatchMapping("/employees/{id}/block")
    @Operation(summary = "Заблокировать/разблокировать сотрудника")
    public ResponseEntity<Map<String, String>> toggleBlock(@PathVariable Long id,
                                                            @RequestBody Map<String, Boolean> body) {
        employeeService.toggleBlock(id, body.get("blocked"));
        return ResponseEntity.ok(Map.of("message", "Статус блокировки изменён"));
    }

    @PostMapping("/employees/{id}/terminate-session")
    @Operation(summary = "Принудительно завершить сессию сотрудника")
    public ResponseEntity<Map<String, String>> terminateSession(@PathVariable Long id) {
        authService.terminateSession(id);
        return ResponseEntity.ok(Map.of("message", "Сессия завершена"));
    }

    // ==================== Склад ====================

    @GetMapping("/inventory")
    @Operation(summary = "Полный список складских позиций")
    public ResponseEntity<Page<InventoryItemResponse>> getAllInventory(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAll(pageable).map(inventoryItemMapper::toResponse));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Позиции с низким остатком")
    public ResponseEntity<List<InventoryItemResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryItemMapper.toResponseList(inventoryService.getLowStock()));
    }

    @PostMapping("/inventory")
    @Operation(summary = "Создать складскую позицию (подарок, возврат от клиента, излишки и т.д.)")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryItemMapper.toResponse(inventoryService.createItem(request, user.getUsername())));
    }

    @DeleteMapping("/inventory/{itemId}")
    @Operation(summary = "Удалить складскую позицию")
    public ResponseEntity<Map<String, String>> deleteInventoryItem(@PathVariable Long itemId) {
        inventoryService.deleteItem(itemId);
        return ResponseEntity.ok(Map.of("message", "Позиция удалена"));
    }

    @PostMapping("/inventory/{itemId}/receive")
    @Operation(summary = "Приёмка товара на склад")
    public ResponseEntity<Map<String, String>> receiveStock(@PathVariable Long itemId,
                                                             @Valid @RequestBody ReceiveStockRequest request,
                                                             @AuthenticationPrincipal UserDetails user) {
        String comment = request.getComment() == null ? "" : request.getComment();
        inventoryService.receiveStock(itemId, request.getQuantity(), user.getUsername(), comment);
        return ResponseEntity.ok(Map.of("message", "Товар принят на склад"));
    }

    // ==================== Поставщики ====================

    @GetMapping("/suppliers")
    @Operation(summary = "Список всех поставщиков")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(supplierService.getAll(pageable).map(supplierMapper::toResponse));
    }

    @GetMapping("/suppliers/{id}")
    @Operation(summary = "Получить поставщика по ID")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierMapper.toResponse(supplierService.getById(id)));
    }

    @PostMapping("/suppliers")
    @Operation(summary = "Создать поставщика")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierMapper.toResponse(supplierService.create(request)));
    }

    @PutMapping("/suppliers/{id}")
    @Operation(summary = "Обновить данные поставщика")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id,
                                                            @Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(supplierMapper.toResponse(supplierService.update(id, request)));
    }

    @PatchMapping("/suppliers/{id}/active")
    @Operation(summary = "Активировать/деактивировать поставщика")
    public ResponseEntity<Map<String, String>> toggleSupplierActive(@PathVariable Long id,
                                                                     @RequestBody Map<String, Boolean> body) {
        supplierService.toggleActive(id, body.get("active"));
        return ResponseEntity.ok(Map.of("message", "Статус поставщика обновлён"));
    }

    @GetMapping("/suppliers/active")
    @Operation(summary = "Активные поставщики")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierMapper.toResponseList(supplierService.getActive()));
    }

    @GetMapping("/suppliers/integration-type/{type}")
    @Operation(summary = "Поставщики по типу интеграции")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByIntegrationType(@PathVariable String type) {
        return ResponseEntity.ok(supplierMapper.toResponseList(supplierService.getByIntegrationType(type)));
    }

    // ==================== Заявки на поставку ====================

    @GetMapping("/supply-requests")
    @Operation(summary = "Все заявки на поставку")
    public ResponseEntity<Page<SupplyRequestResponse>> getAllSupplyRequests(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(supplyRequestService.getAll(pageable).map(supplyRequestMapper::toResponse));
    }

    @GetMapping("/supply-requests/status/{statusName}")
    @Operation(summary = "Заявки по статусу")
    public ResponseEntity<List<SupplyRequestResponse>> getSupplyRequestsByStatus(@PathVariable String statusName) {
        return ResponseEntity.ok(supplyRequestMapper.toResponseList(supplyRequestService.getByStatus(statusName)));
    }

    @GetMapping("/supply-requests/{id}")
    @Operation(summary = "Получить заявку по ID")
    public ResponseEntity<SupplyRequestResponse> getSupplyRequest(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.getById(id)));
    }

    @PostMapping("/supply-requests")
    @Operation(summary = "Создать заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> createSupplyRequest(
            @Valid @RequestBody CreateSupplyRequestRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestMapper.toResponse(supplyRequestService.create(request, user.getUsername())));
    }

    @PostMapping("/supply-requests/{id}/approve")
    @Operation(summary = "Подтвердить заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> approveSupplyRequest(@PathVariable Long id,
                                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.approve(id, user.getUsername())));
    }

    @PostMapping("/supply-requests/{id}/cancel")
    @Operation(summary = "Отменить заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> cancelSupplyRequest(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.cancel(id)));
    }

    @PostMapping("/supply-requests/{id}/ordered")
    @Operation(summary = "Отметить заявку как заказанную")
    public ResponseEntity<SupplyRequestResponse> markOrdered(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.markOrdered(id)));
    }

    @PostMapping("/supply-requests/{id}/in-transit")
    @Operation(summary = "Отметить заявку как 'в пути'")
    public ResponseEntity<SupplyRequestResponse> markInTransit(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.markInTransit(id)));
    }

    @PostMapping("/supply-requests/{id}/delivered")
    @Operation(summary = "Отметить заявку как доставленную (автоматически пополняет склад)")
    public ResponseEntity<SupplyRequestResponse> markDelivered(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(
                supplyRequestService.markDelivered(id, user.getUsername())));
    }

    @PostMapping("/supply-requests/{id}/assign-supplier")
    @Operation(summary = "Привязать поставщика к заявке")
    public ResponseEntity<SupplyRequestResponse> assignSupplier(
            @PathVariable Long id,
            @Valid @RequestBody AssignSupplierRequest request) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(
                supplyRequestService.assignSupplier(id, request.getSupplierId())));
    }

    // ==================== Позиции заявки ====================

    @PostMapping("/supply-requests/{id}/items")
    @Operation(summary = "Добавить позицию в заявку")
    public ResponseEntity<SupplyRequestResponse> addItem(@PathVariable Long id,
                                                          @Valid @RequestBody CreateSupplyRequestItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestMapper.toResponse(supplyRequestService.addItem(id, request)));
    }

    @PutMapping("/supply-requests/{id}/items/{itemId}")
    @Operation(summary = "Обновить позицию заявки")
    public ResponseEntity<SupplyRequestResponse> updateItem(@PathVariable Long id,
                                                             @PathVariable Long itemId,
                                                             @Valid @RequestBody CreateSupplyRequestItemRequest request) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(
                supplyRequestService.updateItem(id, itemId, request)));
    }

    @DeleteMapping("/supply-requests/{id}/items/{itemId}")
    @Operation(summary = "Удалить позицию из заявки")
    public ResponseEntity<SupplyRequestResponse> deleteItem(@PathVariable Long id,
                                                             @PathVariable Long itemId) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(supplyRequestService.deleteItem(id, itemId)));
    }

    @PutMapping("/supply-requests/{id}/comment")
    @Operation(summary = "Обновить комментарий заявки")
    public ResponseEntity<SupplyRequestResponse> updateComment(@PathVariable Long id,
                                                                @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(supplyRequestMapper.toResponse(
                supplyRequestService.updateComment(id, body.get("comment"))));
    }

    // ==================== Оплата поставщикам ====================

    @PostMapping("/supply-requests/{id}/payment")
    @Operation(summary = "Записать оплату поставщику")
    public ResponseEntity<SupplierPaymentResponse> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupplierPaymentRequest request,
            @AuthenticationPrincipal UserDetails user) {
        request.setSupplyRequestId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestMapper.toPaymentResponse(
                        supplyRequestService.recordPayment(request, user.getUsername())));
    }

    @GetMapping("/supply-requests/{id}/payments")
    @Operation(summary = "Список оплат по заявке")
    public ResponseEntity<List<SupplierPaymentResponse>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toPaymentResponseList(supplyRequestService.getPayments(id)));
    }

    @PostMapping("/supply-requests/{id}/invoice")
    @Operation(summary = "Привязать счёт от поставщика")
    public ResponseEntity<SupplierInvoiceResponse> createInvoice(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupplierInvoiceRequest request) {
        request.setSupplyRequestId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestMapper.toInvoiceResponse(supplyRequestService.createInvoice(request)));
    }

    @GetMapping("/supply-requests/{id}/invoices")
    @Operation(summary = "Список счетов по заявке")
    public ResponseEntity<List<SupplierInvoiceResponse>> getInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestMapper.toInvoiceResponseList(supplyRequestService.getInvoices(id)));
    }

    // ==================== Дашборд поставок ====================

    @GetMapping("/supply-dashboard")
    @Operation(summary = "Дашборд управления поставками")
    public ResponseEntity<SupplyDashboardResponse> getSupplyDashboard() {
        return ResponseEntity.ok(supplyDashboardService.getDashboard());
    }

    // ==================== Настройки поставок ====================

    @GetMapping("/supply-settings")
    @Operation(summary = "Все настройки поставок")
    public ResponseEntity<List<SupplySettingResponse>> getSupplySettings() {
        return ResponseEntity.ok(supplySettingMapper.toResponseList(supplySettingService.getAll()));
    }

    @PutMapping("/supply-settings/{key}")
    @Operation(summary = "Обновить настройку поставок")
    public ResponseEntity<SupplySettingResponse> updateSupplySetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSupplySettingRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplySettingMapper.toResponse(
                supplySettingService.update(key, request, user.getUsername())));
    }

    // ==================== Уведомления ====================

    @GetMapping("/notifications/pending")
    @Operation(summary = "Непрочитанные уведомления")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications() {
        return ResponseEntity.ok(notificationMapper.toResponseList(notificationService.getPending()));
    }

    @PostMapping("/notifications/{id}/sent")
    @Operation(summary = "Отметить уведомление как отправленное")
    public ResponseEntity<Map<String, String>> markNotificationSent(@PathVariable Long id) {
        notificationService.markAsSent(id);
        return ResponseEntity.ok(Map.of("message", "Уведомление отмечено как отправленное"));
    }

    // ==================== Анонимизация клиентов (152-ФЗ) ====================

    @GetMapping("/clients/search")
    @Operation(summary = "Поиск клиентов для анонимизации ПДн (ФИО, телефон, дата рождения DD.MM.YYYY)")
    public ResponseEntity<List<ClientResponse>> searchClientsForAnonymize(@RequestParam String query) {
        return ResponseEntity.ok(clientMapper.toResponseList(clientService.searchForAdmin(query)));
    }

    @GetMapping("/clients/{id}")
    @Operation(summary = "Получить клиента по ID (для предпросмотра перед анонимизацией)")
    public ResponseEntity<ClientResponse> getClientForAnonymize(@PathVariable Long id) {
        return ResponseEntity.ok(clientMapper.toResponse(clientService.getById(id)));
    }

    @PostMapping("/clients/{id}/anonymize")
    @Operation(summary = "Принудительная анонимизация ПДн клиента (152-ФЗ, ст. 21)")
    public ResponseEntity<Map<String, String>> anonymizeClient(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        Client client = clientService.getById(id);
        if (clientService.isAnonymized(client)) {
            throw new BusinessLogicException("Данные клиента #" + id + " уже анонимизированы");
        }
        clientService.anonymizeClient(client);
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_DELETE, user.getUsername(), "CLIENT", id, "ANONYMIZE", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.ok(Map.of("message", "Данные клиента #" + id + " анонимизированы"));
    }

    // ==================== Аудит безопасности (152-ФЗ) ====================

    @GetMapping("/audit")
    @Operation(summary = "Журнал аудита безопасности")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLog(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(auditLogService.getAll(pageable), auditLogMapper::toResponse));
    }

    @GetMapping("/audit/employee/{employeeId}")
    @Operation(summary = "Аудит по сотруднику")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(auditLogService.getByEmployee(employeeId, pageable), auditLogMapper::toResponse));
    }

    @GetMapping("/audit/period")
    @Operation(summary = "Аудит за период")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(auditLogService.getByPeriod(from, to, pageable), auditLogMapper::toResponse));
    }
}
