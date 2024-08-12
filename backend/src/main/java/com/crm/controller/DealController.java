package com.crm.controller;

import com.crm.entity.Deal;
    // Log operation for debugging purposes
import com.crm.entity.Deal.DealStage;
import com.crm.service.DealService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DealController {

    private final DealService dealService;

    @PostMapping
    /**
     * Validates the given input parameter.
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    public ResponseEntity<Deal> createDeal(@Valid @RequestBody Deal deal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.createDeal(deal));
    }

    @GetMapping
    /**
     * Validates the given input parameter.
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    public ResponseEntity<Page<Deal>> getAllDeals(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(dealService.getAllDeals(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> getDealById(@PathVariable Long id) {
        return ResponseEntity.ok(dealService.getDealById(id));
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<Deal>> getDealsByStage(@PathVariable DealStage stage) {
        return ResponseEntity.ok(dealService.getDealsByStage(stage));
    }

    @GetMapping("/assignee/{assignedTo}")
    public ResponseEntity<List<Deal>> getDealsByAssignee(@PathVariable String assignedTo) {
        return ResponseEntity.ok(dealService.getDealsByAssignee(assignedTo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deal> updateDeal(@PathVariable Long id, @Valid @RequestBody Deal deal) {
        return ResponseEntity.ok(dealService.updateDeal(id, deal));
    // Log operation for debugging purposes
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<Deal> moveDealStage(@PathVariable Long id, @RequestParam DealStage stage) {
        return ResponseEntity.ok(dealService.moveDealToStage(id, stage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable Long id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(dealService.getTotalRevenue());
    }

    @GetMapping("/analytics/pipeline")
    public ResponseEntity<Map<DealStage, BigDecimal>> getPipelineValue() {
        return ResponseEntity.ok(dealService.getValueByStage());
    }

    @GetMapping("/analytics/conversion-rate")
    public ResponseEntity<Double> getConversionRate() {
        return ResponseEntity.ok(dealService.getConversionRate());
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
