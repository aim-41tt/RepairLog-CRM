package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.client.CreateClientRequest;
import ru.papkov.repairlog.application.mapper.ClientMapper;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Device;
import ru.papkov.repairlog.domain.model.Notification;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.repository.ClientRepository;
import ru.papkov.repairlog.domain.repository.DeviceRepository;
import ru.papkov.repairlog.domain.repository.NotificationRepository;
import ru.papkov.repairlog.domain.repository.RepairOrderRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис управления клиентами.
 * Включает управление согласием на обработку ПДн (152-ФЗ).
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через {@link ClientMapper}.
 * </p>
 *
 * @author aim-41tt
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final NotificationRepository notificationRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository,
                         DeviceRepository deviceRepository,
                         RepairOrderRepository repairOrderRepository,
                         NotificationRepository notificationRepository,
                         ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.deviceRepository = deviceRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.notificationRepository = notificationRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional(readOnly = true)
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return findById(id);
    }

    @Transactional(readOnly = true)
    public List<Client> search(String query) {
        // поиск по ФИО или телефону
        if (query.matches("^[0-9+() -]+$")) {
            return clientRepository.findByPhone(query)
                    .map(List::of)
                    .orElse(List.of());
        }
        return clientRepository.searchByFullName(query);
    }

    /**
     * Расширенный поиск клиентов для администратора.
     * Поддерживает поиск по дате рождения в форматах YYYY-MM-DD и DD.MM.YYYY,
     * а также делегирует базовому поиску по ФИО/телефону.
     * Используется при ручной анонимизации ПДн (152-ФЗ).
     *
     * @param query поисковый запрос
     * @return список найденных клиентов
     */
    @Transactional(readOnly = true)
    public List<Client> searchForAdmin(String query) {
        if (query.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return clientRepository.findByDateBirth(LocalDate.parse(query));
        }
        if (query.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            return clientRepository.findByDateBirth(
                    LocalDate.parse(query, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }
        return search(query);
    }

    @Transactional
    public Client create(CreateClientRequest request) {
        clientRepository.findByPhone(request.getPhone()).ifPresent(c -> {
            throw new BusinessLogicException("Клиент с таким телефоном уже существует");
        });

        // B-15: 152-ФЗ — явное согласие обязательно для регистрации клиента
        if (!request.isConsentGiven()) {
            throw new BusinessLogicException(
                    "Невозможно создать клиента без согласия на обработку персональных данных (152-ФЗ)");
        }

        Client client = clientMapper.toEntity(request);
        client.giveConsent();
        // ПДн хранятся 3 года с момента согласия
        client.setDataRetentionUntil(LocalDate.now().plusYears(3));
        client.setNotificationsEnabled(request.isNotificationsEnabled());

        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, CreateClientRequest request) {
        Client client = findById(id);
        clientMapper.updateEntity(request, client);
        client.setNotificationsEnabled(request.isNotificationsEnabled());
        return clientRepository.save(client);
    }

    /**
     * Предоставление согласия на обработку ПДн (152-ФЗ).
     */
    @Transactional
    public void giveConsent(Long clientId) {
        Client client = findById(clientId);
        client.giveConsent();
        client.setDataRetentionUntil(LocalDate.now().plusYears(3));
        clientRepository.save(client);
    }

    /**
     * Отзыв согласия на обработку ПДн.
     * Устанавливает срок уничтожения данных — 30 дней (ст. 21 ч. 5 152-ФЗ).
     */
    @Transactional
    public void revokeConsent(Long clientId) {
        Client client = findById(clientId);
        client.revokeConsent();
        // 152-ФЗ ст. 21: 30 дней на уничтожение после отзыва согласия
        client.setDataRetentionUntil(LocalDate.now().plusDays(30));
        clientRepository.save(client);
    }

    /**
     * Анонимизация персональных данных клиента (152-ФЗ).
     * Заменяет все ПДн на маскированные значения.
     * Также анонимизирует связанные устройства, заказы и уведомления.
     *
     * @param client клиент для анонимизации (должен быть managed entity)
     */
    @Transactional
    public void anonymizeClient(Client client) {
        // 1. Анонимизация ПДн клиента
        client.anonymize();
        // Phone: UNIQUE + CHECK ^[0-9+() -]{6,20}$.
        // Префикс "00000000000" (11 нулей) исключает коллизию с любым реальным номером
        // (российские номера начинаются с 7/8). id обеспечивает уникальность среди анонимизированных.
        client.setPhone("00000000000" + client.getId());
        clientRepository.save(client);

        // 2. Анонимизация серийных номеров клиентских устройств
        List<Device> devices = deviceRepository.findByClient(client);
        for (Device device : devices) {
            if (device.getSerialNumber() != null) {
                device.setSerialNumber("ANON-" + device.getId());
            }
            deviceRepository.save(device);
        }

        // 3. Анонимизация жалоб клиента в заказах на ремонт
        List<RepairOrder> orders = repairOrderRepository.findByClient(client);
        for (RepairOrder order : orders) {
            if (order.getClientComplaint() != null) {
                order.setClientComplaint("[Данные удалены по 152-ФЗ]");
            }
            repairOrderRepository.save(order);
        }

        // 4. Анонимизация текстов уведомлений
        List<Notification> notifications = notificationRepository.findByClient(client);
        for (Notification notification : notifications) {
            notification.setMessage("[Данные удалены по 152-ФЗ]");
            notificationRepository.save(notification);
        }
    }

    /**
     * Проверить, анонимизированы ли данные клиента.
     */
    public boolean isAnonymized(Client client) {
        return client.isAnonymized();
    }

    // ========== Helpers ==========

    private Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + id));
    }
}
