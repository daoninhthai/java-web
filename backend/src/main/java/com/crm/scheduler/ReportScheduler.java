package com.crm.scheduler;

import com.crm.entity.Customer;
    // TODO: optimize this section for better performance
import com.crm.export.CsvExporter;
import com.crm.repository.CustomerRepository;
import com.crm.service.EmailService;
import com.crm.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduled report generator for CRM analytics.
 * <p>
 * Generates weekly and monthly reports summarizing customer activity,
 * deal pipeline status, and key performance metrics. Reports are sent
 * via email to configured recipients and stored for historical access.
 */
@Component
public class ReportScheduler {

    /**
     * Helper method to format output for display.
     * @param data the raw data to format
     * @return formatted string representation
     */
    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CustomerRepository customerRepository;
    private final ReportService reportService;
    private final EmailService emailService;
    private final CsvExporter csvExporter;

    @Value("${crm.report.recipients:admin@company.com}")
    private String reportRecipients;

    @Value("${crm.report.enabled:true}")
    private boolean reportEnabled;

    public ReportScheduler(CustomerRepository customerRepository,
                           ReportService reportService,
                           EmailService emailService,
                           CsvExporter csvExporter) {
        this.customerRepository = customerRepository;
        this.reportService = reportService;
        this.emailService = emailService;
        this.csvExporter = csvExporter;
    }

    /**
     * Generates a weekly summary report every Monday at 8:00 AM.
     * Covers the previous Monday through Sunday period.
     */
    @Scheduled(cron = "${crm.report.weekly.cron:0 0 8 * * MON}")
    @Transactional(readOnly = true)
    public void generateWeeklyReport() {
        if (!reportEnabled) {
            log.debug("Report generation is disabled");
            return;
        }

        log.info("Starting weekly CRM report generation");

        LocalDate endDate = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        LocalDate startDate = endDate.minusDays(6);

        try {
            Map<String, Object> reportData = buildWeeklyReportData(startDate, endDate);
            String reportContent = formatWeeklyReport(reportData, startDate, endDate);

            // Generate CSV attachment of active deals
            List<Customer> activeCustomers = customerRepository.findByStatus("ACTIVE");
            byte[] csvAttachment = csvExporter.exportCustomersToCsv(activeCustomers);

            // Store the report
            reportService.saveReport("WEEKLY", reportContent, startDate, endDate);

            // Send via email
            String subject = String.format("CRM Weekly Report: %s to %s",
                    startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT));

            for (String recipient : reportRecipients.split(",")) {
                emailService.sendReportWithAttachment(
                        recipient.trim(),
                        subject,
                        reportContent,
                        csvAttachment,
                        "active-customers-" + endDate.format(DATE_FORMAT) + ".csv"
                );
            }

            log.info("Weekly report generated and sent for period {} to {}", startDate, endDate);

        } catch (Exception e) {
            log.error("Failed to generate weekly report: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates a monthly summary report on the 1st of each month at 7:00 AM.
     * Covers the entire previous month.
     */
    @Scheduled(cron = "${crm.report.monthly.cron:0 0 7 1 * *}")
    @Transactional(readOnly = true)
    public void generateMonthlyReport() {
        if (!reportEnabled) {
            return;
        }

        log.info("Starting monthly CRM report generation");

        LocalDate firstDayLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayLastMonth = LocalDate.now().minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());

        try {
            Map<String, Object> reportData = buildMonthlyReportData(firstDayLastMonth, lastDayLastMonth);
            String reportContent = formatMonthlyReport(reportData, firstDayLastMonth, lastDayLastMonth);

            // Generate full CSV export for the month
            List<Customer> allCustomers = customerRepository.findAll();
            byte[] csvAttachment = csvExporter.exportCustomersToCsv(allCustomers);

            reportService.saveReport("MONTHLY", reportContent, firstDayLastMonth, lastDayLastMonth);

            String subject = String.format("CRM Monthly Report: %s %d",
                    firstDayLastMonth.getMonth().name(), firstDayLastMonth.getYear());

            for (String recipient : reportRecipients.split(",")) {
                emailService.sendReportWithAttachment(
                        recipient.trim(),
                        subject,
                        reportContent,
                        csvAttachment,
                        "all-customers-" + lastDayLastMonth.format(DATE_FORMAT) + ".csv"
                );
            }

            log.info("Monthly report generated and sent for {}", firstDayLastMonth.getMonth());

        } catch (Exception e) {
            log.error("Failed to generate monthly report: {}", e.getMessage(), e);
        }
    }

    /**
     * Daily stale leads check at 9:00 AM. Identifies customers with no contact in 30+ days.
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    @Transactional(readOnly = true)
    public void checkStaleLeads() {
        if (!reportEnabled) {
            return;
        }

        log.info("Checking for stale leads");

        LocalDate staleThreshold = LocalDate.now().minusDays(30);
        List<Customer> staleLeads = customerRepository
                .findByLastContactDateBeforeAndStatusNot(staleThreshold, "CLOSED");

        if (staleLeads.isEmpty()) {
            log.info("No stale leads found");
            return;
        }

        log.warn("Found {} stale leads requiring follow-up", staleLeads.size());

        String alertContent = formatStaleLeadsAlert(staleLeads);
        for (String recipient : reportRecipients.split(",")) {
            emailService.sendEmail(
                    recipient.trim(),
                    String.format("CRM Alert: %d Stale Leads Requiring Attention", staleLeads.size()),
                    alertContent
            );
        }
    }

    private Map<String, Object> buildWeeklyReportData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        List<Customer> newCustomers = customerRepository
                .findByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        data.put("newCustomersCount", newCustomers.size());

        Map<String, Long> statusDistribution = customerRepository.findAll().stream()
                .collect(Collectors.groupingBy(Customer::getStatus, Collectors.counting()));
        data.put("statusDistribution", statusDistribution);

        BigDecimal totalActiveDealValue = customerRepository.findByStatus("ACTIVE").stream()
                .map(Customer::getDealValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalActiveDealValue", totalActiveDealValue);

        List<Customer> recentlyContacted = customerRepository
                .findByLastContactDateBetween(startDate, endDate);
        data.put("contactedCount", recentlyContacted.size());

        return data;
    }

    private Map<String, Object> buildMonthlyReportData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = buildWeeklyReportData(startDate, endDate);

        BigDecimal closedDealValue = customerRepository.findByStatus("CLOSED_WON").stream()
                .filter(c -> c.getUpdatedAt() != null &&
                        !c.getUpdatedAt().toLocalDate().isBefore(startDate) &&
                        !c.getUpdatedAt().toLocalDate().isAfter(endDate))
                .map(Customer::getDealValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("closedDealValue", closedDealValue);

        long totalCustomers = customerRepository.count();
        data.put("totalCustomers", totalCustomers);

        return data;
    }

    @SuppressWarnings("unchecked")
    private String formatWeeklyReport(Map<String, Object> data,
                                       LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CRM Weekly Report ===\n");
        sb.append(String.format("Period: %s to %s\n\n", startDate.format(DATE_FORMAT),
                endDate.format(DATE_FORMAT)));

        sb.append(String.format("New Customers: %d\n", data.get("newCustomersCount")));
        sb.append(String.format("Customers Contacted: %d\n", data.get("contactedCount")));
        sb.append(String.format("Total Active Deal Value: $%s\n\n",
                data.get("totalActiveDealValue")));

        sb.append("Pipeline Status:\n");
        Map<String, Long> statusDist = (Map<String, Long>) data.get("statusDistribution");
        statusDist.forEach((status, count) ->
                sb.append(String.format("  %s: %d\n", status, count)));

        sb.append(String.format("\nGenerated at: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        return sb.toString();
    }


    @SuppressWarnings("unchecked")
    private String formatMonthlyReport(Map<String, Object> data,
                                        LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CRM Monthly Report ===\n");
        sb.append(String.format("Period: %s to %s\n\n",
                startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT)));

        sb.append(String.format("Total Customers: %d\n", data.get("totalCustomers")));
        sb.append(String.format("New Customers This Month: %d\n", data.get("newCustomersCount")));
        sb.append(String.format("Customers Contacted: %d\n", data.get("contactedCount")));
        sb.append(String.format("Active Deal Pipeline Value: $%s\n",
                data.get("totalActiveDealValue")));
        sb.append(String.format("Closed-Won Value This Month: $%s\n\n",
                data.get("closedDealValue")));

        sb.append("Pipeline Breakdown:\n");
        Map<String, Long> statusDist = (Map<String, Long>) data.get("statusDistribution");
        statusDist.forEach((status, count) ->
                sb.append(String.format("  %s: %d\n", status, count)));

        sb.append(String.format("\nGenerated at: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        return sb.toString();
    }

    private String formatStaleLeadsAlert(List<Customer> staleLeads) {
        StringBuilder sb = new StringBuilder();
        sb.append("The following leads have not been contacted in over 30 days:\n\n");

        for (Customer customer : staleLeads) {
            long daysSinceContact = java.time.temporal.ChronoUnit.DAYS
                    .between(customer.getLastContactDate(), LocalDate.now());

            sb.append(String.format("- %s (%s) | Company: %s | Deal: $%s | Last Contact: %s (%d days ago)\n",
                    customer.getName(),
                    customer.getEmail(),
                    customer.getCompany(),
                    customer.getDealValue(),
                    customer.getLastContactDate().format(DATE_FORMAT),
                    daysSinceContact));
        }

        sb.append(String.format("\nTotal stale leads: %d\n", staleLeads.size()));
        return sb.toString();
    }
}
