package ru.papkov.repairlog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.papkov.repairlog.domain.model.Client;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с клиентами.
 * 
 * @author aim-41tt
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Найти клиента по телефону.
     *
     * @param phone телефон клиента
     * @return Optional с клиентом
     */
    Optional<Client> findByPhone(String phone);

    /**
     * Найти клиента по email.
     *
     * @param email email клиента
     * @return Optional с клиентом
     */
    Optional<Client> findByEmail(String email);

    /**
     * Поиск клиентов по фамилии (регистронезависимый).
     *
     * @param surname фамилия
     * @return список клиентов
     */
    List<Client> findBySurnameContainingIgnoreCase(String surname);

    /**
     * Полнотекстовый поиск по ФИО.
     *
     * @param searchTerm поисковый запрос
     * @return список клиентов
     */
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.patronymic) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> searchByFullName(@Param("searchTerm") String searchTerm);

    /**
     * Найти клиентов, у которых истёк срок хранения персональных данных.
     *
     * @param currentDate текущая дата
     * @return список клиентов
     */
    List<Client> findByDataRetentionUntilBefore(LocalDate currentDate);

    /**
     * Найти клиентов, которые не дали согласие на обработку ПДн.
     *
     * @return список клиентов
     */
    List<Client> findByConsentGivenFalse();
}
