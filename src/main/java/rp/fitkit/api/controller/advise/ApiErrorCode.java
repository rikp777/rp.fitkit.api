package rp.fitkit.api.controller.advise;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApiErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS"),
    BAD_CREDENTIALS("BAD_CREDENTIALS"),
    ACCESS_DENIED("ACCESS_DENIED"),
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION"),
    UNEXPECTED_ERROR("UNEXPECTED_ERROR"),
    INVALID_DATE_RANGE("INVALID_DATE_RANGE"),
    GENERIC_ERROR("GENERIC_ERROR");

    private final String code;

    ApiErrorCode(String code) {
        this.code = code;
    }

    @JsonValue // Zorgt ervoor dat in JSON de string-waarde wordt gebruikt
    public String getCode() {
        return code;
    }
}
