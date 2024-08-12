package com.crm.service;

import com.crm.entity.Customer.CustomerStatus;
import com.crm.entity.Deal;
import com.crm.entity.Deal.DealStage;
import com.crm.repository.CustomerRepository;
import com.crm.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for computing advanced dashboard analytics and KPIs.
 * Provides trend data, forecasting, and performance metrics beyond
 * the basic stats offered by DashboardService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardAnalyticsService {

    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    /**
     * Computes month-over-month customer acquisition trend for the last N months.
     *
     * @param months number of months to look back
     * @return ordered map of month labels to new customer counts
     */
    public LinkedHashMap<String, Long> getCustomerAcquisitionTrend(int months) {
        LinkedHashMap<String, Long> trend = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            List<?> newCustomers = customerRepository.findAll().stream()
                    .filter(c -> c.getCreatedAt() != null
                            && !c.getCreatedAt().toLocalDate().isBefore(monthStart)
                            && !c.getCreatedAt().toLocalDate().isAfter(monthEnd))
                    .collect(Collectors.toList());

            String label = monthStart.getMonth().name().substring(0, 3) + " " + monthStart.getYear();
            trend.put(label, (long) newCustomers.size());
        }

        log.debug("Customer acquisition trend computed for {} months", months);
        return trend;
    }

    /**
     * Computes the average deal cycle time in days for won deals.
     *
     * @return average days from creation to close, or 0 if no data
     */
    public double getAverageDealCycleTime() {
        List<Deal> wonDeals = dealRepository.findByStage(DealStage.WON);

        OptionalDouble avgDays = wonDeals.stream()
                .filter(d -> d.getActualCloseDate() != null && d.getCreatedAt() != null)
                .mapToLong(d -> java.time.temporal.ChronoUnit.DAYS.between(
                        d.getCreatedAt().toLocalDate(), d.getActualCloseDate()))
                .average();

        double result = avgDays.orElse(0.0);
        log.debug("Average deal cycle time: {} days", result);
        return result;
    }

    /**
     * Computes revenue forecast based on deals in active pipeline stages,
     * weighted by their probability of closing.
     *
     * @return forecasted revenue amount
     */
    public BigDecimal getRevenueForecast() {
        Set<DealStage> activeStages = Set.of(
                DealStage.QUALIFIED, DealStage.PROPOSAL, DealStage.NEGOTIATION);

        List<Deal> activeDeals = dealRepository.findAll().stream()
                .filter(d -> activeStages.contains(d.getStage()))
                .collect(Collectors.toList());

        BigDecimal forecast = activeDeals.stream()
                .map(d -> {
                    BigDecimal value = d.getValue() != null ? d.getValue() : BigDecimal.ZERO;
                    int probability = d.getProbability() != null ? d.getProbability() : 0;
                    return value.multiply(BigDecimal.valueOf(probability))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Revenue forecast: {} from {} active deals", forecast, activeDeals.size());
        return forecast;
    }

    /**
     * Returns the win rate for each sales representative.
     *
     * @return map of representative name to win rate percentage
     */
    public Map<String, Double> getWinRateByRepresentative() {
        List<Deal> closedDeals = dealRepository.findAll().stream()
                .filter(d -> d.getStage() == DealStage.WON || d.getStage() == DealStage.LOST)
                .filter(d -> d.getAssignedTo() != null && !d.getAssignedTo().isBlank())
                .collect(Collectors.toList());

        Map<String, List<Deal>> byRep = closedDeals.stream()
                .collect(Collectors.groupingBy(Deal::getAssignedTo));

        Map<String, Double> winRates = new HashMap<>();
        byRep.forEach((rep, deals) -> {
            long won = deals.stream().filter(d -> d.getStage() == DealStage.WON).count();
            double rate = (double) won / deals.size() * 100;
            winRates.put(rep, Math.round(rate * 100.0) / 100.0);
        });

        return winRates;
    }

    /**
     * Returns customer churn rate as a percentage.
     * Calculated as churned customers divided by total customers.
     *
     * @return churn rate percentage
     */
    public double getChurnRate() {
        long total = customerRepository.count();
        if (total == 0) return 0.0;

        long churned = customerRepository.countByStatus(CustomerStatus.CHURNED);
        double rate = (double) churned / total * 100;

        log.debug("Churn rate: {}% ({} churned out of {} total)", rate, churned, total);
        return Math.round(rate * 100.0) / 100.0;
    }

    /**
     * Returns the top N deals by value currently in the pipeline.
     *
     * @param limit maximum number of deals to return
     * @return list of top deals sorted by value descending
     */
    public List<Deal> getTopDealsByValue(int limit) {
        return dealRepository.findAll().stream()
                .filter(d -> d.getStage() != DealStage.WON && d.getStage() != DealStage.LOST)
                .filter(d -> d.getValue() != null)
                .sorted(Comparator.comparing(Deal::getValue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
