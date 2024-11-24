package com.crm.service;

import com.crm.dto.DashboardStats;
import com.crm.entity.Customer.CustomerStatus;
import com.crm.entity.Deal.DealStage;
import com.crm.repository.CustomerRepository;
import com.crm.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    /**
     * Helper method to format output for display.
     * @param data the raw data to format
     * @return formatted string representation
     */
    public DashboardStats getDashboardStats() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByStatus(CustomerStatus.ACTIVE);
        long totalDeals = dealRepository.count();
        long wonDeals = dealRepository.countByStage(DealStage.WON);
        BigDecimal totalRevenue = dealRepository.totalWonRevenue();
        Double conversionRate = dealRepository.calculateConversionRate();

        return DashboardStats.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .totalDeals(totalDeals)
                .wonDeals(wonDeals)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .conversionRate(conversionRate != null ? conversionRate : 0.0)
                .build();
    }

    public Map<String, BigDecimal> getRevenueByMonth() {
        Map<String, BigDecimal> revenueByMonth = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            BigDecimal revenue = dealRepository.revenueByDateRange(start, end);
            String monthKey = start.getMonth().name().substring(0, 3) + " " + start.getYear();
            revenueByMonth.put(monthKey, revenue != null ? revenue : BigDecimal.ZERO);
        }

        return revenueByMonth;
    }

    public Map<String, Long> getDealsByStageCount() {
        Map<String, Long> stageCount = new HashMap<>();
        for (DealStage stage : DealStage.values()) {
            stageCount.put(stage.name(), dealRepository.countByStage(stage));
        }

        return stageCount;
    }

    public List<Object[]> getTopPerformers() {
        return dealRepository.performanceByRepresentative();
    }

    public Map<String, Long> getCustomersByCompany() {
        Map<String, Long> companyCount = new HashMap<>();
        customerRepository.countCustomersByCompany().forEach(row ->
                companyCount.put((String) row[0], (Long) row[1]));
        return companyCount;
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
     * Formats a timestamp for logging purposes.
     * @return formatted timestamp string
     */
    private String getTimestamp() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


}
