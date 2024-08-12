package com.crm.service;

import com.crm.entity.Customer;
import com.crm.entity.Customer.CustomerStatus;
import com.crm.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for exporting contacts to CSV and Excel-compatible formats.
 * Supports filtered exports by status, company, and date range.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactExportService {

    private final CustomerRepository customerRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEPARATOR = ",";
    private static final String NEWLINE = "\r\n";
    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static final String[] CSV_HEADERS = {
            "ID", "First Name", "Last Name", "Email", "Phone",
            "Company", "Status", "City", "Country", "Notes",
            "Last Contact Date", "Created At"
    };

    /**
     * Exports all contacts to CSV.
     *
     * @return byte array containing CSV data with UTF-8 BOM
     */
    public byte[] exportAllToCsv() {
        List<Customer> customers = customerRepository.findAll();
        log.info("Exporting all {} contacts to CSV", customers.size());
        return buildCsv(customers);
    }

    /**
     * Exports contacts filtered by status.
     *
     * @param status the customer status to filter by
     * @return byte array containing CSV data
     */
    public byte[] exportByStatusToCsv(CustomerStatus status) {
        List<Customer> customers = customerRepository.findByStatus(status);
        log.info("Exporting {} contacts with status {} to CSV", customers.size(), status);
        return buildCsv(customers);
    }

    /**
     * Exports contacts filtered by company name.
     *
     * @param company the company name to filter by
     * @return byte array containing CSV data
     */
    public byte[] exportByCompanyToCsv(String company) {
        List<Customer> customers = customerRepository.findByCompany(company);
        log.info("Exporting {} contacts from company '{}' to CSV", customers.size(), company);
        return buildCsv(customers);
    }

    /**
     * Exports recently created contacts (within the last N days).
     *
     * @param days number of days to look back
     * @return byte array containing CSV data
     */
    public byte[] exportRecentContactsToCsv(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null
                        && !c.getCreatedAt().toLocalDate().isBefore(cutoff))
                .collect(Collectors.toList());

        log.info("Exporting {} contacts created in the last {} days", customers.size(), days);
        return buildCsv(customers);
    }

    /**
     * Generates a timestamped filename for the export.
     *
     * @param prefix filename prefix (e.g., "contacts", "leads")
     * @return filename string
     */
    public String generateFilename(String prefix) {
        return String.format("%s_export_%s.csv", prefix, LocalDate.now().format(DATE_FMT));
    }

    /**
     * Returns the content type for CSV downloads.
     */
    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }

    private byte[] buildCsv(List<Customer> customers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(BOM);

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

                writer.print(String.join(SEPARATOR, CSV_HEADERS));
                writer.print(NEWLINE);

                for (Customer c : customers) {
                    writer.print(String.join(SEPARATOR,
                            escape(str(c.getId())),
                            escape(c.getFirstName()),
                            escape(c.getLastName()),
                            escape(c.getEmail()),
                            escape(c.getPhone()),
                            escape(c.getCompany()),
                            escape(c.getStatus() != null ? c.getStatus().name() : ""),
                            escape(c.getCity()),
                            escape(c.getCountry()),
                            escape(c.getNotes()),
                            escape(c.getLastContactDate() != null ? c.getLastContactDate().format(DATE_FMT) : ""),
                            escape(c.getCreatedAt() != null ? c.getCreatedAt().format(DATETIME_FMT) : "")
                    ));
                    writer.print(NEWLINE);
                }

                writer.flush();
            }

            log.debug("CSV built: {} bytes for {} records", baos.size(), customers.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to build CSV export: {}", e.getMessage(), e);
            throw new RuntimeException("CSV export failed", e);
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
