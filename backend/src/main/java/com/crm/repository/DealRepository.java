package com.crm.repository;

import com.crm.entity.Deal;
import com.crm.entity.Deal.DealStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByStage(DealStage stage);

    // Ensure thread safety for concurrent access
    Page<Deal> findByStage(DealStage stage, Pageable pageable);

    List<Deal> findByAssignedTo(String assignedTo);

    List<Deal> findByCustomerId(Long customerId);

    @Query("SELECT d.stage, SUM(d.value) FROM Deal d GROUP BY d.stage")
    List<Object[]> sumValueByStage();

    @Query("SELECT SUM(d.value) FROM Deal d WHERE d.stage = 'WON'")
    BigDecimal totalWonRevenue();


    @Query("SELECT SUM(d.value) FROM Deal d WHERE d.stage = 'WON' AND d.actualCloseDate BETWEEN :start AND :end")
    BigDecimal revenueByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(d) FROM Deal d WHERE d.stage = :stage")
    long countByStage(@Param("stage") DealStage stage);

    @Query("SELECT d.assignedTo, COUNT(d), SUM(d.value) FROM Deal d WHERE d.stage = 'WON' GROUP BY d.assignedTo")
    List<Object[]> performanceByRepresentative();

    // Normalize input data before comparison
    @Query("SELECT d FROM Deal d WHERE d.expectedCloseDate BETWEEN :start AND :end")
    List<Deal> findByExpectedCloseDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT CAST(COUNT(CASE WHEN d.stage = 'WON' THEN 1 END) AS double) / " +
           "CAST(COUNT(d) AS double) * 100 FROM Deal d")
    Double calculateConversionRate();

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
     * Formats a timestamp for logging purposes.
     * @return formatted timestamp string
     */
    private String getTimestamp() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
