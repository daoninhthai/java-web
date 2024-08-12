package com.crm.service;

import com.crm.entity.Customer;
import com.crm.entity.Customer.CustomerStatus;
import com.crm.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ContactImportService that handles CSV file parsing,
 * validation, duplicate detection, and batch persistence of customer records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContactImportServiceImpl implements ContactImportService {

    private final CustomerRepository customerRepository;

    private static final List<String> REQUIRED_HEADERS = List.of("first_name", "last_name", "email");
    private static final int MAX_ROWS = 10_000;

    @Override
    @Transactional
    public ImportResult importFromCsv(MultipartFile file) {
        log.info("Starting CSV contact import: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int skippedDuplicates = 0;
        int failedCount = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, 0, 0, List.of("CSV file is empty"));
            }

            Map<String, Integer> headerIndex = parseHeaders(headerLine);
            List<String> validationErrors = validateHeaders(headerIndex);
            if (!validationErrors.isEmpty()) {
                return new ImportResult(0, 0, 0, 0, validationErrors);
            }

            String line;
            while ((line = reader.readLine()) != null && totalRows < MAX_ROWS) {
                totalRows++;
                try {
                    String[] fields = parseCsvLine(line);
                    Customer customer = mapToCustomer(fields, headerIndex);

                    if (customerRepository.existsByEmail(customer.getEmail())) {
                        skippedDuplicates++;
                        log.debug("Skipping duplicate email: {}", customer.getEmail());
                        continue;
                    }

                    customer.setStatus(CustomerStatus.LEAD);
                    customerRepository.save(customer);
                    importedCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add("Row " + totalRows + ": " + e.getMessage());
                    log.warn("Failed to import row {}: {}", totalRows, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Failed to read CSV file: {}", e.getMessage(), e);
            errors.add("Failed to read file: " + e.getMessage());
        }

        log.info("CSV import completed: total={}, imported={}, duplicates={}, failed={}",
                totalRows, importedCount, skippedDuplicates, failedCount);

        return new ImportResult(totalRows, importedCount, skippedDuplicates, failedCount, errors);
    }

    @Override
    public List<String> validateCsvFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file.isEmpty()) {
            errors.add("File is empty");
            return errors;
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.contains("csv") && !contentType.contains("text/plain")) {
            errors.add("Invalid file type. Expected CSV but got: " + contentType);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            errors.add("File size exceeds 10MB limit");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine != null) {
                Map<String, Integer> headerIndex = parseHeaders(headerLine);
                errors.addAll(validateHeaders(headerIndex));
            } else {
                errors.add("CSV file has no header row");
            }
        } catch (Exception e) {
            errors.add("Cannot read file: " + e.getMessage());
        }

        return errors;
    }

    @Override
    public List<Customer> previewCsv(MultipartFile file, int limit) {
        List<Customer> preview = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return preview;

            Map<String, Integer> headerIndex = parseHeaders(headerLine);
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < limit) {
                String[] fields = parseCsvLine(line);
                preview.add(mapToCustomer(fields, headerIndex));
                count++;
            }
        } catch (Exception e) {
            log.error("Error previewing CSV: {}", e.getMessage());
        }

        return preview;
    }

    private Map<String, Integer> parseHeaders(String headerLine) {
        String[] headers = headerLine.toLowerCase().split(",");
        return java.util.stream.IntStream.range(0, headers.length)
                .boxed()
                .collect(Collectors.toMap(i -> headers[i].trim(), i -> i, (a, b) -> a));
    }

    private List<String> validateHeaders(Map<String, Integer> headerIndex) {
        List<String> missing = REQUIRED_HEADERS.stream()
                .filter(h -> !headerIndex.containsKey(h))
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            return List.of("Missing required columns: " + String.join(", ", missing));
        }
        return List.of();
    }

    private Customer mapToCustomer(String[] fields, Map<String, Integer> headerIndex) {
        return Customer.builder()
                .firstName(getField(fields, headerIndex, "first_name"))
                .lastName(getField(fields, headerIndex, "last_name"))
                .email(getField(fields, headerIndex, "email"))
                .phone(getField(fields, headerIndex, "phone"))
                .company(getField(fields, headerIndex, "company"))
                .city(getField(fields, headerIndex, "city"))
                .country(getField(fields, headerIndex, "country"))
                .notes(getField(fields, headerIndex, "notes"))
                .build();
    }

    private String getField(String[] fields, Map<String, Integer> headerIndex, String column) {
        Integer idx = headerIndex.get(column);
        if (idx == null || idx >= fields.length) return null;
        String value = fields[idx].trim();
        return value.isEmpty() ? null : value;
    }

    private String[] parseCsvLine(String line) {
        return Arrays.stream(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                .map(field -> field.trim().replaceAll("^\"|\"$", ""))
                .toArray(String[]::new);
    }
}
