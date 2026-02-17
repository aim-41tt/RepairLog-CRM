package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.papkov.repairlog.application.dto.common.RefField;
import ru.papkov.repairlog.application.dto.device.*;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления устройствами.
 *
 * @author aim-41tt
 */
@Service
public class DeviceService {

	private final DeviceRepository deviceRepository;
	private final DeviceTypeRepository deviceTypeRepository;
	private final ModelRepository modelRepository;
	private final ClientRepository clientRepository;
	private final DeviceLocationRepository deviceLocationRepository;
	private final EmployeeRepository employeeRepository;
	private final BrandRepository brandRepository;

	public DeviceService(DeviceRepository deviceRepository,
                         DeviceTypeRepository deviceTypeRepository,
                         ModelRepository modelRepository,
                         ClientRepository clientRepository,
                         DeviceLocationRepository deviceLocationRepository,
                         EmployeeRepository employeeRepository,
                         BrandRepository brandRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.modelRepository = modelRepository;
        this.clientRepository = clientRepository;
        this.deviceLocationRepository = deviceLocationRepository;
        this.employeeRepository = employeeRepository;
        this.brandRepository = brandRepository;
    }

	@Transactional(readOnly = true)
	public DeviceResponse getById(Long id) {
		return toResponse(findDevice(id));
	}

	@Transactional(readOnly = true)
	public List<DeviceResponse> getByClient(Long clientId) {
		Client client = clientRepository.findById(clientId)
				.orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + clientId));
		return deviceRepository.findByClient(client).stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional
	public DeviceResponse create(CreateDeviceRequest request) {

		DeviceType deviceType = resolveDeviceType(request.getDeviceType());
		Brand brand = resolveBrand(request.getBrand());
		Model model = resolveModel(request.getModel(), brand);

		Device device = new Device();
		device.setDeviceType(deviceType);
		device.setModel(model);
		device.setSerialNumber(request.getSerialNumber());
		device.setIsClientOwned(request.isClientOwned());

		if (request.getClientId() != null) {
			Client client = clientRepository.findById(request.getClientId())
					.orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
			device.setClient(client);
		}

		return toResponse(deviceRepository.save(device));
	}

	private DeviceType resolveDeviceType(RefField ref) {
		if (ref.getId() != null) {
			return deviceTypeRepository.findById(ref.getId())
					.orElseThrow(() -> new EntityNotFoundException("Тип устройства не найден"));
		}
		return deviceTypeRepository.findByName(ref.getName().trim()).orElseGet(() -> {
			DeviceType dt = new DeviceType();
			dt.setName(ref.getName().trim());
			return deviceTypeRepository.save(dt);
		});
	}

	private Brand resolveBrand(RefField ref) {
		if (ref.getId() != null) {
			return brandRepository.findById(ref.getId())
					.orElseThrow(() -> new EntityNotFoundException("Бренд не найден"));
		}
		return brandRepository.findByName(ref.getName().trim()).orElseGet(() -> {
			Brand b = new Brand();
			b.setName(ref.getName().trim());
			return brandRepository.save(b);
		});
	}

	private Model resolveModel(RefField ref, Brand brand) {
		if (ref.getId() != null) {
			return modelRepository.findById(ref.getId())
					.orElseThrow(() -> new EntityNotFoundException("Модель не найдена"));
		}
		return modelRepository.findByNameAndBrand(ref.getName().trim(), brand).orElseGet(() -> {
			Model m = new Model();
			m.setName(ref.getName().trim());
			m.setBrand(brand);
			return modelRepository.save(m);
		});
	}

	/**
	 * Перемещение устройства (запись в историю местоположений).
	 */
	@Transactional
	public void moveDevice(Long deviceId, String location, String movedByLogin, String comment) {
		Device device = findDevice(deviceId);
		Employee movedBy = employeeRepository.findByLogin(movedByLogin)
				.orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

		DeviceLocation dl = new DeviceLocation();
		dl.setDevice(device);
		dl.setLocation(location);
		dl.setMovedBy(movedBy);
		dl.setComment(comment);
		deviceLocationRepository.save(dl);
	}

	private Device findDevice(Long id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + id));
	}

	private DeviceResponse toResponse(Device d) {
		DeviceResponse r = new DeviceResponse();
		r.setId(d.getId());
		r.setDeviceTypeName(d.getDeviceType().getName());
		r.setBrandName(d.getModel().getBrand().getName());
		r.setModelName(d.getModel().getName());
		r.setSerialNumber(d.getSerialNumber());
		r.setClientOwned(d.getIsClientOwned());
		r.setDescription(d.getDescription());
		r.setCreatedAt(d.getCreatedAt());
		if (d.getClient() != null) {
			r.setClientId(d.getClient().getId());
			r.setClientFullName(d.getClient().getFullName());
		}
		return r;
	}
}
