package com.crm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * In-memory implementation of EmailTemplateService for managing
 * email templates with placeholder substitution support.
 *
 * Templates support placeholders in the format {{variableName}} which
 * are replaced at render time with actual values.
 */
@Service
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final Map<Long, EmailTemplate> templateStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");
    private static final int MAX_TEMPLATE_NAME_LENGTH = 100;
    private static final int MAX_SUBJECT_LENGTH = 200;

    @Override
    public EmailTemplate createTemplate(String name, String subject, String body, String category) {
        validateTemplateInputs(name, subject, body);

        Long id = idGenerator.getAndIncrement();
        EmailTemplate template = new EmailTemplate(id, name.trim(), subject.trim(), body, category);
        templateStore.put(id, template);

        log.info("Created email template: id={}, name='{}', category='{}'", id, name, category);
        return template;
    }

    @Override
    public Optional<EmailTemplate> getTemplateById(Long id) {
        return Optional.ofNullable(templateStore.get(id));
    }

    @Override
    public List<EmailTemplate> listTemplates(String category) {
        if (category == null || category.isBlank()) {
            return new ArrayList<>(templateStore.values());
        }

        return templateStore.values().stream()
                .filter(t -> category.equalsIgnoreCase(t.category()))
                .collect(Collectors.toList());
    }

    @Override
    public EmailTemplate updateTemplate(Long id, String name, String subject, String body, String category) {
        EmailTemplate existing = templateStore.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Email template not found with id: " + id);
        }

        validateTemplateInputs(name, subject, body);

        EmailTemplate updated = new EmailTemplate(id, name.trim(), subject.trim(), body, category);
        templateStore.put(id, updated);

        log.info("Updated email template: id={}, name='{}'", id, name);
        return updated;
    }

    @Override
    public void deleteTemplate(Long id) {
        EmailTemplate removed = templateStore.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("Email template not found with id: " + id);
        }
        log.info("Deleted email template: id={}, name='{}'", id, removed.name());
    }

    @Override
    public RenderedEmail renderTemplate(Long templateId, Map<String, String> variables) {
        EmailTemplate template = templateStore.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Email template not found with id: " + templateId);
        }

        String renderedSubject = replacePlaceholders(template.subject(), variables);
        String renderedBody = replacePlaceholders(template.body(), variables);

        log.debug("Rendered template id={} with {} variables", templateId, variables.size());
        return new RenderedEmail(renderedSubject, renderedBody);
    }

    /**
     * Replaces all {{placeholder}} occurrences in the text with values from the map.
     * Unmatched placeholders are left as-is so the caller can detect missing variables.
     */
    private String replacePlaceholders(String text, Map<String, String> variables) {
        if (text == null || variables == null || variables.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.getOrDefault(varName, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private void validateTemplateInputs(String name, String subject, String body) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name must not be empty");
        }
        if (name.length() > MAX_TEMPLATE_NAME_LENGTH) {
            throw new IllegalArgumentException("Template name must not exceed " + MAX_TEMPLATE_NAME_LENGTH + " characters");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Template subject must not be empty");
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException("Template subject must not exceed " + MAX_SUBJECT_LENGTH + " characters");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Template body must not be empty");
        }
    }
}
