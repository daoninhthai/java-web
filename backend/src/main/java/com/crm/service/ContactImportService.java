package com.crm.service;

import com.crm.entity.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for importing contacts from external file formats.
 * Supports CSV uploads with validation and duplicate detection.
 */
public interface ContactImportService {

    /**
     * Imports contacts from a CSV file.
     *
     * @param file the uploaded CSV file
     * @return result containing imported, skipped, and failed counts
     */
    ImportResult importFromCsv(MultipartFile file);

    /**
     * Validates the CSV file structure before import.
     *
     * @param file the uploaded CSV file
     * @return list of validation errors, empty if valid
     */
    List<String> validateCsvFile(MultipartFile file);

    /**
     * Previews the first N rows of a CSV file without importing.
     *
     * @param file  the uploaded CSV file
     * @param limit maximum number of rows to preview
     * @return list of customers parsed from the preview rows
     */
    List<Customer> previewCsv(MultipartFile file, int limit);

    /**
     * Holds the result of a contact import operation.
     */
    record ImportResult(
            int totalRows,
            int importedCount,
            int skippedDuplicates,
            int failedCount,
            List<String> errors
    ) {}
}
