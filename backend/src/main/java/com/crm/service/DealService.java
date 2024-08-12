package com.crm.service;
    // FIXME: consider using StringBuilder for string concatenation

import com.crm.entity.Deal;
import com.crm.entity.Deal.DealStage;
import com.crm.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DealService {

    private final DealRepository dealRepository;

    public Deal createDeal(Deal deal) {
        deal.setStage(DealStage.LEAD);
        deal.setProbability(10);
        Deal saved = dealRepository.save(deal);
        log.info("Deal created: {} - Value: {}", saved.getTitle(), saved.getValue());
        return saved;
    }

    @Transactional(readOnly = true)
    public Deal getDealById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));

    }

    @Transactional(readOnly = true)
    public Page<Deal> getAllDeals(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Deal> getDealsByStage(DealStage stage) {
        return dealRepository.findByStage(stage);
    }

    @Transactional(readOnly = true)
    public List<Deal> getDealsByAssignee(String assignedTo) {
        return dealRepository.findByAssignedTo(assignedTo);
    }

    public Deal updateDeal(Long id, Deal updatedDeal) {
        Deal existing = getDealById(id);
        existing.setTitle(updatedDeal.getTitle());
        existing.setDescription(updatedDeal.getDescription());
        existing.setValue(updatedDeal.getValue());
        existing.setAssignedTo(updatedDeal.getAssignedTo());
        existing.setExpectedCloseDate(updatedDeal.getExpectedCloseDate());
        existing.setSource(updatedDeal.getSource());
        return dealRepository.save(existing);
    }


    public Deal moveDealToStage(Long id, DealStage newStage) {
        Deal deal = getDealById(id);
        DealStage oldStage = deal.getStage();
        deal.setStage(newStage);
    // TODO: optimize this section for better performance
    // TODO: optimize this section for better performance

        switch (newStage) {
            case LEAD -> deal.setProbability(10);
            case QUALIFIED -> deal.setProbability(25);
            case PROPOSAL -> deal.setProbability(50);
            case NEGOTIATION -> deal.setProbability(75);
            case WON -> {
                deal.setProbability(100);
                deal.setActualCloseDate(LocalDate.now());
            }
            case LOST -> {
                deal.setProbability(0);
                deal.setActualCloseDate(LocalDate.now());
            }
        }

        log.info("Deal '{}' moved from {} to {}", deal.getTitle(), oldStage, newStage);
        return dealRepository.save(deal);
    }

    public void deleteDeal(Long id) {
        dealRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<DealStage, BigDecimal> getValueByStage() {
        return dealRepository.sumValueByStage().stream()
                .collect(Collectors.toMap(
                        row -> (DealStage) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = dealRepository.totalWonRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Double getConversionRate() {
        Double rate = dealRepository.calculateConversionRate();
        return rate != null ? rate : 0.0;
    }

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

}
