package com.crm.service;

import com.crm.entity.Deal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for the sales pipeline view.
 * Provides aggregated pipeline data for visualization and analysis.
 */
public interface PipelineService {

    /**
     * Returns deals grouped by their pipeline stage.
     */
    Map<Deal.DealStage, List<Deal>> getDealsByStage();

    /**
     * Returns the total monetary value of deals in each stage.
     */
    Map<Deal.DealStage, BigDecimal> getValueByStage();

    /**
     * Moves a deal to a new pipeline stage with validation of allowed transitions.
     *
     * @param dealId   the deal to move
     * @param newStage the target stage
     * @return the updated deal
     */
    Deal moveDealToStage(Long dealId, Deal.DealStage newStage);

    /**
     * Returns a summary snapshot of the entire pipeline.
     */
    PipelineSummary getPipelineSummary();

    /**
     * Returns deals assigned to a specific representative.
     */
    List<Deal> getDealsByRepresentative(String assignedTo);

    /**
     * Aggregate summary of the sales pipeline.
     */
    record PipelineSummary(
            long totalDeals,
            BigDecimal totalValue,
            BigDecimal weightedValue,
            double avgProbability,
            Map<Deal.DealStage, Long> countByStage
    ) {}
}
