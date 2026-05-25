package com.hireconnect.exception;

/**
 * BusinessException represents business rule violations in the HRMS.
 *
 * Examples:
 * - Attendance already started
 * - No active session to end
 * - Invalid break operation
 * - Correction request for future date
 *
 * This is NOT a system error. It is a controlled, expected condition.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
