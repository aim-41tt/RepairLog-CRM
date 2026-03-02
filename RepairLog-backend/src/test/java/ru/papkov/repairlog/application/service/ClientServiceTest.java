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
import ru.papkov.repairlog.domain.repository.ClientRepository;

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
    @DisplayName("revokeConsent - отзыв согласия на обработку ПДн")
    void revokeConsent_success() {
        testClient.giveConsent();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        clientService.revokeConsent(1L);

        assertThat(testClient.getConsentGiven()).isFalse();
        verify(clientRepository).save(testClient);
    }

    @Test
    @DisplayName("giveConsent - ошибка если клиент не найден")
    void giveConsent_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.giveConsent(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
