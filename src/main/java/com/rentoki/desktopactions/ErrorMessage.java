package com.rentoki.desktopactions;

public enum ErrorMessage {
    URL_IS_NULL("URL cannot be empty or null."),
    INVALID_URL("Invalid URL: "),
    NOT_SUPPORTED("Desktop actions not supported."),
    BROWSE_FAILED("Failed to open browser."),
    FILE_IS_NULL("File doesn't exist."),
    FILE_PATH_IS_NULL("File path cannot be null or empty."),
    FILE_IS_NOT_DIRECTORY("File is not a directory."),
    OPEN_FILE_LOCATION_FAILED("Failed to open file: "),
    OPEN_FILE_DIRECTORY_FAILED("Failed to open directory: ");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
