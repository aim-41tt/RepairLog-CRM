package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.client.ClientResponse;
import ru.papkov.repairlog.application.dto.client.CreateClientRequest;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;
    private CreateClientRequest createRequest;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Иван");
        testClient.setSurname("Петров");
        testClient.setPatronymic("Сергеевич");
        testClient.setDateBirth(LocalDate.of(1990, 5, 15));
        testClient.setPhone("+79001234567");
        testClient.setEmail("petrov@mail.ru");
        testClient.setConsentGiven(false);

        createRequest = new CreateClientRequest();
        createRequest.setName("Иван");
        createRequest.setSurname("Петров");
        createRequest.setPatronymic("Сергеевич");
        createRequest.setDateBirth(LocalDate.of(1990, 5, 15));
        createRequest.setPhone("+79001234567");
        createRequest.setEmail("petrov@mail.ru");
        createRequest.setConsentGiven(false);
    }

    @Test
    @DisplayName("getAll - возвращает список всех клиентов")
    void getAll_returnsAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(testClient));

        List<ClientResponse> result = clientService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Иван");
        assertThat(result.get(0).getSurname()).isEqualTo("Петров");
        verify(clientRepository).findAll();
    }

    @Test
    @DisplayName("getAll - возвращает пустой список если клиентов нет")
    void getAll_returnsEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponse> result = clientService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getById - возвращает клиента по ID")
    void getById_returnsClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        ClientResponse result = clientService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isEqualTo("+79001234567");
    }

    @Test
    @DisplayName("getById - выбрасывает исключение если клиент не найден")
    void getById_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("search - поиск по номеру телефона")
    void search_byPhone() {
        when(clientRepository.findByPhone("+79001234567")).thenReturn(Optional.of(testClient));

        List<ClientResponse> result = clientService.search("+79001234567");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhone()).isEqualTo("+79001234567");
    }

    @Test
    @DisplayName("search - поиск по телефону, клиент не найден")
    void search_byPhone_notFound() {
        when(clientRepository.findByPhone("+79999999999")).thenReturn(Optional.empty());

        List<ClientResponse> result = clientService.search("+79999999999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("search - поиск по ФИО")
    void search_byFullName() {
        when(clientRepository.searchByFullName("Петров")).thenReturn(List.of(testClient));

        List<ClientResponse> result = clientService.search("Петров");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("create - успешное создание клиента без согласия")
    void create_success_withoutConsent() {
        when(clientRepository.findByPhone("+79001234567")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClientResponse result = clientService.create(createRequest);

        assertThat(result.getName()).isEqualTo("Иван");
        assertThat(result.isConsentGiven()).isFalse();
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("create - успешное создание клиента с согласием на ПДн")
    void create_success_withConsent() {
        createRequest.setConsentGiven(true);
        when(clientRepository.findByPhone("+79001234567")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClientResponse result = clientService.create(createRequest);

        assertThat(result.isConsentGiven()).isTrue();
        assertThat(result.getDataRetentionUntil()).isNotNull();
    }

    @Test
    @DisplayName("create - ошибка при дублировании телефона")
    void create_throwsOnDuplicatePhone() {
        when(clientRepository.findByPhone("+79001234567")).thenReturn(Optional.of(testClient));

        assertThatThrownBy(() -> clientService.create(createRequest))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("телефоном уже существует");
    }

    @Test
    @DisplayName("update - успешное обновление клиента")
    void update_success() {
        CreateClientRequest updateReq = new CreateClientRequest();
        updateReq.setName("Алексей");
        updateReq.setSurname("Сидоров");
        updateReq.setPatronymic("Иванович");
        updateReq.setDateBirth(LocalDate.of(1985, 3, 10));
        updateReq.setPhone("+79005555555");
        updateReq.setEmail("sidorov@mail.ru");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        ClientResponse result = clientService.update(1L, updateReq);

        assertThat(result).isNotNull();
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("update - ошибка если клиент не найден")
    void update_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(99L, createRequest))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("giveConsent - предоставление согласия на обработку ПДн")
    void giveConsent_success() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        clientService.giveConsent(1L);

        assertThat(testClient.getConsentGiven()).isTrue();
        assertThat(testClient.getDataRetentionUntil()).isEqualTo(LocalDate.now().plusYears(3));
        verify(clientRepository).save(testClient);
    }

    @Test
    @DisplayName("revokeConsent - отзыв согласия и установка 30-дневного срока уничтожения (152-ФЗ)")
    void revokeConsent_success() {
        testClient.giveConsent();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        clientService.revokeConsent(1L);

        assertThat(testClient.getConsentGiven()).isFalse();
        // 152-ФЗ ст. 21: 30 дней на уничтожение после отзыва согласия
        assertThat(testClient.getDataRetentionUntil()).isEqualTo(LocalDate.now().plusDays(30));
        verify(clientRepository).save(testClient);
    }

    @Test
    @DisplayName("giveConsent - ошибка если клиент не найден")
    void giveConsent_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.giveConsent(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ========== Тесты анонимизации (152-ФЗ) ==========

    @Test
    @DisplayName("anonymizeClient - анонимизация клиента и всех связанных сущностей")
    void anonymizeClient_success() {
        testClient.setId(42L);
        testClient.giveConsent();
        testClient.setDataRetentionUntil(LocalDate.now().minusDays(1));

        Device device = new Device();
        device.setId(1L);
        device.setSerialNumber("SN-12345");

        RepairOrder order = new RepairOrder();
        order.setId(1L);
        order.setClientComplaint("Не работает экран, звонить +79001234567");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setMessage("Уважаемый Петров И.С., ваш заказ готов");
        notification.setNotificationType(Notification.NotificationType.SMS);
        notification.setClient(testClient);

        when(deviceRepository.findByClient(testClient)).thenReturn(List.of(device));
        when(repairOrderRepository.findByClient(testClient)).thenReturn(List.of(order));
        when(notificationRepository.findByClient(testClient)).thenReturn(List.of(notification));

        clientService.anonymizeClient(testClient);

        // Проверка анонимизации клиента
        assertThat(testClient.getName()).isEqualTo("Удалён");
        assertThat(testClient.getSurname()).isEqualTo("Удалён");
        assertThat(testClient.getPatronymic()).isNull();
        assertThat(testClient.getDateBirth()).isEqualTo(LocalDate.of(1900, 1, 1));
        assertThat(testClient.getPhone()).isEqualTo("000000042");
        assertThat(testClient.getEmail()).isNull();
        assertThat(testClient.getConsentGiven()).isFalse();
        assertThat(testClient.getConsentDate()).isNull();
        assertThat(testClient.getDataRetentionUntil()).isNull();

        // Проверка анонимизации связанных сущностей
        assertThat(device.getSerialNumber()).isEqualTo("ANON-1");
        assertThat(order.getClientComplaint()).isEqualTo("[Данные удалены по 152-ФЗ]");
        assertThat(notification.getMessage()).isEqualTo("[Данные удалены по 152-ФЗ]");

        verify(clientRepository).save(testClient);
        verify(deviceRepository).save(device);
        verify(repairOrderRepository).save(order);
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("anonymizeClient - устройство без серийного номера не трогается")
    void anonymizeClient_deviceWithoutSerialNumber() {
        testClient.setId(10L);

        Device device = new Device();
        device.setId(2L);
        device.setSerialNumber(null);

        when(deviceRepository.findByClient(testClient)).thenReturn(List.of(device));
        when(repairOrderRepository.findByClient(testClient)).thenReturn(List.of());
        when(notificationRepository.findByClient(testClient)).thenReturn(List.of());

        clientService.anonymizeClient(testClient);

        assertThat(device.getSerialNumber()).isNull();
        verify(deviceRepository).save(device);
    }

    @Test
    @DisplayName("isAnonymized - возвращает true для анонимизированного клиента")
    void isAnonymized_returnsTrue() {
        testClient.setName("Удалён");
        testClient.setSurname("Удалён");

        assertThat(clientService.isAnonymized(testClient)).isTrue();
    }

    @Test
    @DisplayName("isAnonymized - возвращает false для обычного клиента")
    void isAnonymized_returnsFalse() {
        assertThat(clientService.isAnonymized(testClient)).isFalse();
    }
}
