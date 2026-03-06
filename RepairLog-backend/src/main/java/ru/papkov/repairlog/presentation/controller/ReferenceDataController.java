package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для справочных данных.
 * Возвращает типы устройств, бренды, модели, статусы и приоритеты.
 * Доступен любому авторизованному пользователю.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/reference")
@Tag(name = "Справочники", description = "Типы устройств, бренды, модели, статусы")
public class ReferenceDataController {

    private final DeviceTypeRepository deviceTypeRepository;
    private final BrandRepository brandRepository;
    private final ModelRepository modelRepository;
    private final RepairStatusRepository repairStatusRepository;
    private final RepairPriorityRepository repairPriorityRepository;
    private final DegreeWearRepository degreeWearRepository;

    public ReferenceDataController(DeviceTypeRepository deviceTypeRepository,
                                   BrandRepository brandRepository,
                                   ModelRepository modelRepository,
                                   RepairStatusRepository repairStatusRepository,
                                   RepairPriorityRepository repairPriorityRepository,
                                   DegreeWearRepository degreeWearRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.repairStatusRepository = repairStatusRepository;
        this.repairPriorityRepository = repairPriorityRepository;
        this.degreeWearRepository = degreeWearRepository;
    }

    @GetMapping("/device-types")
    @Operation(summary = "Список типов устройств")
    public ResponseEntity<List<Map<String, Object>>> getDeviceTypes() {
        return ResponseEntity.ok(deviceTypeRepository.findAll().stream()
                .map(dt -> Map.<String, Object>of("id", dt.getId(), "name", dt.getName()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/brands")
    @Operation(summary = "Список брендов")
    public ResponseEntity<List<Map<String, Object>>> getBrands() {
        return ResponseEntity.ok(brandRepository.findAll().stream()
                .map(b -> Map.<String, Object>of("id", b.getId(), "name", b.getName()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/brands/{brandId}/models")
    @Operation(summary = "Модели по бренду")
    public ResponseEntity<List<Map<String, Object>>> getModelsByBrand(@PathVariable Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ru.papkov.repairlog.domain.exception.EntityNotFoundException(
                        "Бренд с id=" + brandId + " не найден"));
        return ResponseEntity.ok(modelRepository.findByBrand(brand).stream()
                .map(m -> Map.<String, Object>of("id", m.getId(), "name", m.getName()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/repair-statuses")
    @Operation(summary = "Список статусов ремонта")
    public ResponseEntity<List<Map<String, Object>>> getRepairStatuses() {
        return ResponseEntity.ok(repairStatusRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/priorities")
    @Operation(summary = "Список приоритетов")
    public ResponseEntity<List<Map<String, Object>>> getPriorities() {
        return ResponseEntity.ok(repairPriorityRepository.findAll().stream()
                .map(p -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put("sortOrder", p.getSortOrder());
                    map.put("colorHex", p.getColorHex());
                    return map;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/degree-wears")
    @Operation(summary = "Список степеней износа")
    public ResponseEntity<List<Map<String, Object>>> getDegreeWears() {
        return ResponseEntity.ok(degreeWearRepository.findAll().stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .collect(Collectors.toList()));
    }
}
