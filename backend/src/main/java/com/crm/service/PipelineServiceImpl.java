package com.crm.service;

import com.crm.entity.Deal;
import com.crm.entity.Deal.DealStage;
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
 * Implementation of PipelineService that provides sales pipeline views,
 * stage transitions with validation, and weighted pipeline analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

    private final DealRepository dealRepository;

    /**
     * Defines the allowed forward transitions in the sales pipeline.
     * Deals can only move forward through stages or be marked as LOST from any stage.
     */
    private static final Map<DealStage, Set<DealStage>> ALLOWED_TRANSITIONS = Map.of(
            DealStage.LEAD, Set.of(DealStage.QUALIFIED, DealStage.LOST),
            DealStage.QUALIFIED, Set.of(DealStage.PROPOSAL, DealStage.LOST),
            DealStage.PROPOSAL, Set.of(DealStage.NEGOTIATION, DealStage.LOST),
            DealStage.NEGOTIATION, Set.of(DealStage.WON, DealStage.LOST),
            DealStage.WON, Set.of(),
            DealStage.LOST, Set.of(DealStage.LEAD)
    );

    @Override
    public Map<DealStage, List<Deal>> getDealsByStage() {
        List<Deal> allDeals = dealRepository.findAll();
        Map<DealStage, List<Deal>> grouped = new EnumMap<>(DealStage.class);

        for (DealStage stage : DealStage.values()) {
            grouped.put(stage, new ArrayList<>());
        }

        for (Deal deal : allDeals) {
            grouped.get(deal.getStage()).add(deal);
        }

        log.debug("Pipeline view: {} total deals across {} stages", allDeals.size(), grouped.size());
        return grouped;
    }

    @Override
    public Map<DealStage, BigDecimal> getValueByStage() {
        List<Object[]> stageValues = dealRepository.sumValueByStage();
        Map<DealStage, BigDecimal> valueMap = new EnumMap<>(DealStage.class);

        for (DealStage stage : DealStage.values()) {
            valueMap.put(stage, BigDecimal.ZERO);
        }

        for (Object[] row : stageValues) {
            DealStage stage = (DealStage) row[0];
            BigDecimal value = (BigDecimal) row[1];
            valueMap.put(stage, value != null ? value : BigDecimal.ZERO);
        }

        return valueMap;
    }

    @Override
    @Transactional
    public Deal moveDealToStage(Long dealId, DealStage newStage) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found with id: " + dealId));

        DealStage currentStage = deal.getStage();
        Set<DealStage> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStage, Set.of());

        if (!allowed.contains(newStage)) {
            throw new IllegalStateException(String.format(
                    "Cannot move deal from %s to %s. Allowed transitions: %s",
                    currentStage, newStage, allowed));
        }

        deal.setStage(newStage);

        if (newStage == DealStage.WON) {
            deal.setActualCloseDate(LocalDate.now());
            deal.setProbability(100);
        } else if (newStage == DealStage.LOST) {
            deal.setActualCloseDate(LocalDate.now());
            deal.setProbability(0);
        }

        Deal saved = dealRepository.save(deal);
        log.info("Deal '{}' moved from {} to {}", deal.getTitle(), currentStage, newStage);
        return saved;
    }

    @Override
    public PipelineSummary getPipelineSummary() {
        List<Deal> allDeals = dealRepository.findAll();
        long totalDeals = allDeals.size();

        BigDecimal totalValue = allDeals.stream()
                .map(d -> d.getValue() != null ? d.getValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedValue = allDeals.stream()
                .map(d -> {
                    BigDecimal val = d.getValue() != null ? d.getValue() : BigDecimal.ZERO;
                    int prob = d.getProbability() != null ? d.getProbability() : 0;
                    return val.multiply(BigDecimal.valueOf(prob))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgProbability = allDeals.stream()
                .mapToInt(d -> d.getProbability() != null ? d.getProbability() : 0)
                .average()
                .orElse(0.0);

        Map<DealStage, Long> countByStage = allDeals.stream()
                .collect(Collectors.groupingBy(Deal::getStage, Collectors.counting()));

        log.info("Pipeline summary: {} deals, total={}, weighted={}", totalDeals, totalValue, weightedValue);
        return new PipelineSummary(totalDeals, totalValue, weightedValue, avgProbability, countByStage);
    }

    @Override
    public List<Deal> getDealsByRepresentative(String assignedTo) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException("Representative name must not be empty");
        }
        return dealRepository.findByAssignedTo(assignedTo);
    }
}
