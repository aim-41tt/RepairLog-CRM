package ru.papkov.repairlog.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.mapper.SupplierMapper;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.repository.SupplierRepository;

import java.util.List;

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
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    public Page<Supplier> getAll(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Supplier getById(Long id) {
        return findById(id);
    }

    public List<Supplier> getActive() {
        return supplierRepository.findByActiveTrue();
    }

    public List<Supplier> getByIntegrationType(String type) {
        IntegrationType integrationType = IntegrationType.valueOf(type);
        return supplierRepository.findByIntegrationType(integrationType);
    }

    @Transactional
    public Supplier create(CreateSupplierRequest request) {
        Supplier supplier = supplierMapper.toEntity(request);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, CreateSupplierRequest request) {
        Supplier supplier = findById(id);
        supplierMapper.updateEntity(request, supplier);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void toggleActive(Long id, boolean active) {
        Supplier supplier = findById(id);
        supplier.setActive(active);
        supplierRepository.save(supplier);
    }

    private Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Поставщик не найден: " + id));
    }
}
