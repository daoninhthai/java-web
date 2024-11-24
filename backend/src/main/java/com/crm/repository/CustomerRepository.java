package com.crm.repository;

import com.crm.entity.Customer;
import com.crm.entity.Customer.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByStatus(CustomerStatus status);

    List<Customer> findByCompany(String company);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> searchByName(@Param("name") String name);

    @Query("SELECT c FROM Customer c WHERE c.lastContactDate < :date AND c.status = 'ACTIVE'")
    List<Customer> findInactiveCustomers(@Param("date") LocalDate date);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    long countByStatus(@Param("status") CustomerStatus status);

    @Query("SELECT c.company, COUNT(c) FROM Customer c GROUP BY c.company ORDER BY COUNT(c) DESC")
    List<Object[]> countCustomersByCompany();

    @Query("SELECT c FROM Customer c WHERE c.createdAt >= CURRENT_DATE - 30")
    List<Customer> findRecentCustomers();

    boolean existsByEmail(String email);

    List<Customer> findByLastContactDateBeforeAndStatusNot(LocalDate date, CustomerStatus status);

    List<Customer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Customer> findByLastContactDateBetween(LocalDate start, LocalDate end);

    /**
     * Validates that the given value is within the expected range.
     * @param value the value to check
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return true if value is within range
     */
    private boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }


    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }


    /**
     * Safely parses an integer from a string value.
     * @param value the string to parse
     * @param defaultValue the fallback value
     * @return parsed integer or default value
     */
    private int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
