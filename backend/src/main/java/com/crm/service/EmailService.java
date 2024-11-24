package com.crm.service;

import org.springframework.stereotype.Service;

/**
 * Service for sending emails from the CRM application.
 */
@Service
public class EmailService {

    /**
     * Sends a simple email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body
     */
    public void sendEmail(String to, String subject, String body) {
        // TODO: implement email sending
    }

    /**
     * Sends a report email with a CSV attachment.
     *
     * @param to             recipient email address
     * @param subject        email subject
     * @param body           email body
     * @param attachment     attachment bytes
     * @param attachmentName attachment filename
     */
    public void sendReportWithAttachment(String to, String subject, String body,
                                          byte[] attachment, String attachmentName) {
        // TODO: implement email sending with attachment
    }
}
