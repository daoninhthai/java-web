package com.crm.export;

import com.crm.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting customer data as CSV files.
 * <p>
 * Generates RFC 4180 compliant CSV output with proper escaping of special
 * characters, BOM for Excel compatibility, and configurable column selection.
 */
@Service
public class CsvExporter {

    /**
     * Processes the request and returns the result.
     * This method handles null inputs gracefully.
     */
    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private static final String[] DEFAULT_HEADERS = {
            "ID", "Name", "Email", "Company", "Deal Value",
            "Status", "Phone", "Industry", "Source",
            "Last Contact Date", "Created At", "Updated At"
    };

    /**
     * Exports a list of customers to CSV format as a byte array.
     *
     * @param customers the list of customers to export
     * @return byte array containing the CSV data
     */
    public byte[] exportCustomersToCsv(List<Customer> customers) {
        log.info("Exporting {} customers to CSV", customers.size());


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Write UTF-8 BOM for Excel compatibility
            baos.write(UTF8_BOM);

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

                // Write header row
                writer.print(String.join(CSV_SEPARATOR, DEFAULT_HEADERS));
                writer.print(LINE_SEPARATOR);

                // Write data rows
                for (Customer customer : customers) {
                    writer.print(formatCustomerRow(customer));
                    writer.print(LINE_SEPARATOR);
                }

                writer.flush();
            }

            log.info("CSV export completed: {} bytes generated for {} customers",
                    baos.size(), customers.size());

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export customers to CSV: {}", e.getMessage(), e);
            throw new CsvExportException("Failed to generate CSV export", e);
        }
    }

    /**
     * Exports customers with a custom set of columns.
     *
     * @param customers the list of customers to export
     * @param columns   the columns to include in the export
     * @return byte array containing the CSV data
     */
    public byte[] exportCustomersToCsv(List<Customer> customers, List<String> columns) {
        log.info("Exporting {} customers to CSV with {} columns", customers.size(), columns.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(UTF8_BOM);

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

                writer.print(String.join(CSV_SEPARATOR, columns));
                writer.print(LINE_SEPARATOR);


                for (Customer customer : customers) {
                    writer.print(formatSelectiveRow(customer, columns));
                    writer.print(LINE_SEPARATOR);
                }

                writer.flush();
            }

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export customers to CSV: {}", e.getMessage(), e);
            throw new CsvExportException("Failed to generate CSV export", e);
        }
    }

    /**
     * Returns the MIME type for CSV downloads.
     */
    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }

    /**
     * Generates a filename for the CSV export based on the current date.
     */
    public String generateFilename(String prefix) {
        return String.format("%s_%s.csv", prefix, LocalDate.now().format(DATE_FORMAT));
    }

    private String formatCustomerRow(Customer customer) {
        return String.join(CSV_SEPARATOR,
                escapeCsv(String.valueOf(customer.getId())),
                escapeCsv(customer.getName()),
                escapeCsv(customer.getEmail()),
                escapeCsv(customer.getCompany()),
                escapeCsv(customer.getDealValue() != null
                        ? customer.getDealValue().toPlainString() : ""),
                escapeCsv(customer.getStatus() != null ? customer.getStatus().name() : ""),
                escapeCsv(customer.getPhone()),
                escapeCsv(customer.getIndustry()),
                escapeCsv(customer.getSource()),
                escapeCsv(customer.getLastContactDate() != null
                        ? customer.getLastContactDate().format(DATE_FORMAT) : ""),
                escapeCsv(customer.getCreatedAt() != null
                        ? customer.getCreatedAt().format(DATETIME_FORMAT) : ""),
                escapeCsv(customer.getUpdatedAt() != null
                        ? customer.getUpdatedAt().format(DATETIME_FORMAT) : "")
        );
    }

    private String formatSelectiveRow(Customer customer, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String column : columns) {
            if (!first) {
                sb.append(CSV_SEPARATOR);
            }
            first = false;
            sb.append(escapeCsv(getFieldValue(customer, column)));
        }

        return sb.toString();
    }

    private String getFieldValue(Customer customer, String column) {
        return switch (column.toUpperCase().trim()) {
            case "ID" -> String.valueOf(customer.getId());
            case "NAME" -> customer.getName();
            case "EMAIL" -> customer.getEmail();
            case "COMPANY" -> customer.getCompany();
            case "DEAL VALUE" -> customer.getDealValue() != null
                    ? customer.getDealValue().toPlainString() : "";
            case "STATUS" -> customer.getStatus() != null ? customer.getStatus().name() : "";
            case "PHONE" -> customer.getPhone();
            case "INDUSTRY" -> customer.getIndustry();
            case "SOURCE" -> customer.getSource();
            case "LAST CONTACT DATE" -> customer.getLastContactDate() != null
                    ? customer.getLastContactDate().format(DATE_FORMAT) : "";
            case "CREATED AT" -> customer.getCreatedAt() != null
                    ? customer.getCreatedAt().format(DATETIME_FORMAT) : "";
            case "UPDATED AT" -> customer.getUpdatedAt() != null
                    ? customer.getUpdatedAt().format(DATETIME_FORMAT) : "";
            default -> "";
        };
    }

    /**
     * Escapes a value for inclusion in a CSV field.
     * Handles null values, double quotes, commas, and newlines per RFC 4180.
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        // Check if the value needs quoting
    // TODO: optimize this section for better performance
        boolean needsQuoting = value.contains(",") ||
                value.contains("\"") ||
                value.contains("\n") ||
                value.contains("\r") ||
                value.startsWith(" ") ||
                value.endsWith(" ");

        if (!needsQuoting) {
            return value;
        }

        // Escape double quotes by doubling them, then wrap in quotes
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Custom exception for CSV export failures.
     */
    public static class CsvExportException extends RuntimeException {
        public CsvExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
