package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.model.enums.OrderMethod;
import ru.papkov.repairlog.domain.model.enums.PriceSource;
import ru.papkov.repairlog.domain.repository.SupplierRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления поставщиками.
 * CRUD-операции над справочником поставщиков.
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * Получить список всех поставщиков.
     *
     * @return список поставщиков
     */
    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить поставщика по ID.
     *
     * @param id идентификатор поставщика
     * @return данные поставщика
     * @throws EntityNotFoundException если поставщик не найден
     */
    public SupplierResponse getById(Long id) {
        return toResponse(findById(id));
    }

    /**
     * Создать нового поставщика.
     *
     * @param request данные для создания
     * @return созданный поставщик
     */
    /**
     * Получить активных поставщиков.
     */
    public List<SupplierResponse> getActive() {
        return supplierRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить поставщиков по типу интеграции.
     */
    public List<SupplierResponse> getByIntegrationType(String type) {
        IntegrationType integrationType = IntegrationType.valueOf(type);
        return supplierRepository.findByIntegrationType(integrationType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        mapRequestToEntity(request, supplier);
        supplier.setActive(true);

        return toResponse(supplierRepository.save(supplier));
    }

    /**
     * Обновить данные поставщика.
     *
     * @param id      идентификатор поставщика
     * @param request обновлённые данные
     * @return обновлённый поставщик
     */
    @Transactional
    public SupplierResponse update(Long id, CreateSupplierRequest request) {
        Supplier supplier = findById(id);
        mapRequestToEntity(request, supplier);

        return toResponse(supplierRepository.save(supplier));
    }

    /**
     * Деактивировать / активировать поставщика.
     *
     * @param id     идентификатор поставщика
     * @param active новый статус
     */
    @Transactional
    public void toggleActive(Long id, boolean active) {
        Supplier supplier = findById(id);
        supplier.setActive(active);
        supplierRepository.save(supplier);
    }

    // ========== Вспомогательные методы ==========

    private Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Поставщик не найден: " + id));
    }

    private void mapRequestToEntity(CreateSupplierRequest request, Supplier supplier) {
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setInn(request.getInn());
        if (request.getIntegrationType() != null) {
            supplier.setIntegrationType(IntegrationType.valueOf(request.getIntegrationType()));
        }
        if (request.getPriceSource() != null) {
            supplier.setPriceSource(PriceSource.valueOf(request.getPriceSource()));
        }
        if (request.getOrderMethod() != null) {
            supplier.setOrderMethod(OrderMethod.valueOf(request.getOrderMethod()));
        }
        supplier.setWebsiteUrl(request.getWebsiteUrl());
        supplier.setContactMessenger(request.getContactMessenger());
        supplier.setPriceListEmail(request.getPriceListEmail());
        supplier.setExternalSupplierId(request.getExternalSupplierId());
    }

    private SupplierResponse toResponse(Supplier s) {
        SupplierResponse r = new SupplierResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setContactPerson(s.getContactPerson());
        r.setPhone(s.getPhone());
        r.setEmail(s.getEmail());
        r.setAddress(s.getAddress());
        r.setInn(s.getInn());
        r.setActive(s.getActive());
        r.setCreatedAt(s.getCreatedAt());
        r.setIntegrationType(s.getIntegrationType() != null ? s.getIntegrationType().name() : null);
        r.setPriceSource(s.getPriceSource() != null ? s.getPriceSource().name() : null);
        r.setOrderMethod(s.getOrderMethod() != null ? s.getOrderMethod().name() : null);
        r.setWebsiteUrl(s.getWebsiteUrl());
        r.setContactMessenger(s.getContactMessenger());
        r.setPriceListEmail(s.getPriceListEmail());
        r.setExternalSupplierId(s.getExternalSupplierId());
        return r;
    }
}
