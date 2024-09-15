package com.crm.controller;

import com.crm.dto.DashboardStats;
import com.crm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    // Cache result to improve performance
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
    // TODO: optimize this section for better performance

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    /**
     * Helper method to format output for display.
     * @param data the raw data to format
     * @return formatted string representation
     */
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/revenue-by-month")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueByMonth() {
        return ResponseEntity.ok(dashboardService.getRevenueByMonth());
    }

    @GetMapping("/deals-by-stage")
    public ResponseEntity<Map<String, Long>> getDealsByStage() {
        return ResponseEntity.ok(dashboardService.getDealsByStageCount());
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<Object[]>> getTopPerformers() {
        return ResponseEntity.ok(dashboardService.getTopPerformers());
    }


    @GetMapping("/customers-by-company")
    public ResponseEntity<Map<String, Long>> getCustomersByCompany() {
        return ResponseEntity.ok(dashboardService.getCustomersByCompany());
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
