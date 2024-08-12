package com.crm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class CustomerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String company;

    @NotNull(message = "Deal value is required")
    @Positive(message = "Deal value must be positive")
    private BigDecimal dealValue;

    @NotBlank(message = "Status is required")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastContactDate;

    private String phone;

    private String industry;

    private String source;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public CustomerDTO() {
    }

    public CustomerDTO(Long id, String name, String email, String company,
                       BigDecimal dealValue, String status, LocalDate lastContactDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.dealValue = dealValue;
        this.status = status;
        this.lastContactDate = lastContactDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public BigDecimal getDealValue() {
        return dealValue;
    }

    public void setDealValue(BigDecimal dealValue) {
        this.dealValue = dealValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getLastContactDate() {
        return lastContactDate;
    }

    public void setLastContactDate(LocalDate lastContactDate) {
        this.lastContactDate = lastContactDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if the customer has been contacted within the given number of days.
     *
     * @param days the number of days to check against
     * @return true if the customer was contacted within the given days
     */
    public boolean wasContactedWithinDays(int days) {
        if (lastContactDate == null) {
            return false;
        }
        return lastContactDate.isAfter(LocalDate.now().minusDays(days));
    }

    /**
     * Returns the number of days since last contact, or -1 if never contacted.
     */
    public long daysSinceLastContact() {
        if (lastContactDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(lastContactDate, LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;
        CustomerDTO that = (CustomerDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", dealValue=" + dealValue +
                ", status='" + status + '\'' +
                ", lastContactDate=" + lastContactDate +
                '}';
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
