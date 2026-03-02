package ru.papkov.repairlog.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.AbstractIntegrationTest;
import ru.papkov.repairlog.domain.model.Client;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест ClientRepository с реальной PostgreSQL через Testcontainers.
 * Каждый тест выполняется в транзакции с откатом (@Transactional) для изоляции данных.
 *
 * @author aim-41tt
 */
@Transactional
class ClientRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setName("Иван");
        testClient.setSurname("Петров");
        testClient.setPatronymic("Сергеевич");
        testClient.setDateBirth(LocalDate.of(1990, 5, 15));
        testClient.setPhone("+79001234567");
        testClient.setEmail("petrov@test.ru");
        testClient.setConsentGiven(false);
    }

    @Test
    @DisplayName("save и findById — сохранение клиента и поиск по ID")
    void saveAndFindById() {
        Client saved = clientRepository.save(testClient);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Client> found = clientRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Иван");
        assertThat(found.get().getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("findByPhone — поиск клиента по номеру телефона")
    void findByPhone() {
        clientRepository.save(testClient);

        Optional<Client> found = clientRepository.findByPhone("+79001234567");
        assertThat(found).isPresent();
        assertThat(found.get().getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("findByPhone — возвращает пусто для несуществующего номера")
    void findByPhone_notFound() {
        Optional<Client> found = clientRepository.findByPhone("+70000000000");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByEmail — поиск клиента по email")
    void findByEmail() {
        clientRepository.save(testClient);

        Optional<Client> found = clientRepository.findByEmail("petrov@test.ru");
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("+79001234567");
    }

    @Test
    @DisplayName("findBySurnameContainingIgnoreCase — нечёткий поиск по фамилии")
    void findBySurnameContaining() {
        clientRepository.save(testClient);

        List<Client> found = clientRepository.findBySurnameContainingIgnoreCase("петр");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Иван");
    }

    @Test
    @DisplayName("searchByFullName — JPQL поиск по части ФИО")
    void searchByFullName() {
        clientRepository.save(testClient);

        List<Client> results = clientRepository.searchByFullName("Петров");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("findByConsentGivenFalse — клиенты без согласия на обработку данных")
    void findByConsentGivenFalse() {
        clientRepository.save(testClient);

        // Клиент с согласием
        Client clientWithConsent = new Client();
        clientWithConsent.setName("Анна");
        clientWithConsent.setSurname("Сидорова");
        clientWithConsent.setDateBirth(LocalDate.of(1985, 3, 10));
        clientWithConsent.setPhone("+79009876543");
        clientWithConsent.setConsentGiven(true);
        clientWithConsent.giveConsent();
        clientRepository.save(clientWithConsent);

        List<Client> withoutConsent = clientRepository.findByConsentGivenFalse();
        assertThat(withoutConsent).hasSize(1);
        assertThat(withoutConsent.get(0).getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("findByDataRetentionUntilBefore — клиенты с истекшим сроком хранения данных")
    void findByDataRetentionExpired() {
        testClient.setDataRetentionUntil(LocalDate.of(2024, 1, 1));
        clientRepository.save(testClient);

        // Клиент с актуальным сроком
        Client activeClient = new Client();
        activeClient.setName("Мария");
        activeClient.setSurname("Козлова");
        activeClient.setDateBirth(LocalDate.of(1992, 7, 20));
        activeClient.setPhone("+79005551111");
        activeClient.setConsentGiven(true);
        activeClient.setDataRetentionUntil(LocalDate.of(2030, 12, 31));
        clientRepository.save(activeClient);

        List<Client> expired = clientRepository.findByDataRetentionUntilBefore(LocalDate.now());
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getSurname()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("giveConsent и revokeConsent — управление согласием на обработку ПД")
    void consentManagement() {
        Client saved = clientRepository.save(testClient);
        assertThat(saved.getConsentGiven()).isFalse();

        saved.giveConsent();
        clientRepository.save(saved);

        Client updated = clientRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getConsentGiven()).isTrue();
        assertThat(updated.getConsentDate()).isNotNull();
    }
}
