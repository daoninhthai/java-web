package com.model;

/**
 * Generic API response wrapper.
 * Provides a consistent response format for all API endpoints.
 *
 * @param <T> the type of data in the response
 */
public class ApiResponse649<T> {

    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse649() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse649(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public ApiResponse649(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public static <T> ApiResponse649<T> success(T data) {
        return new ApiResponse649<>(true, "Success", data);
    }

    public static <T> ApiResponse649<T> success(String message, T data) {
        return new ApiResponse649<>(true, message, data);
    }

    public static <T> ApiResponse649<T> error(String message) {
        return new ApiResponse649<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

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


    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
