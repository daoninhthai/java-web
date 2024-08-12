package com.crm.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing reusable email templates used in CRM
 * communications such as follow-ups, onboarding sequences, and campaigns.
 */
public interface EmailTemplateService {

    /**
     * Creates a new email template.
     *
     * @param name    template name
     * @param subject email subject line (may contain placeholders)
     * @param body    email body (may contain placeholders)
     * @param category template category (e.g., FOLLOW_UP, ONBOARDING)
     * @return the created template
     */
    EmailTemplate createTemplate(String name, String subject, String body, String category);

    /**
     * Retrieves a template by its unique identifier.
     */
    Optional<EmailTemplate> getTemplateById(Long id);

    /**
     * Lists all templates, optionally filtered by category.
     */
    List<EmailTemplate> listTemplates(String category);

    /**
     * Updates an existing template.
     */
    EmailTemplate updateTemplate(Long id, String name, String subject, String body, String category);

    /**
     * Deletes a template by ID.
     */
    void deleteTemplate(Long id);

    /**
     * Renders a template by replacing placeholders with the provided variables.
     *
     * @param templateId the template to render
     * @param variables  map of placeholder names to values
     * @return rendered subject and body
     */
    RenderedEmail renderTemplate(Long templateId, Map<String, String> variables);

    /**
     * Lightweight DTO for a stored email template.
     */
    record EmailTemplate(Long id, String name, String subject, String body, String category) {}

    /**
     * Holds the rendered email content after placeholder substitution.
     */
    record RenderedEmail(String subject, String body) {}
}
