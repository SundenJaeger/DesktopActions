package com.rentoki.desktopactions;

public class DesktopActionException extends Exception {
    public DesktopActionException() {
        super();
    }

    public DesktopActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DesktopActionException(String message) {
        super(message);
    }

    public DesktopActionException(Throwable cause) {
        super(cause);
    }
}
