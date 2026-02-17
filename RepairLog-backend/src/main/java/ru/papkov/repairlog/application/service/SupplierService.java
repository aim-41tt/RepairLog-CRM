package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Supplier;
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
    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setInn(request.getInn());
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
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setInn(request.getInn());

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
        return r;
    }
}
