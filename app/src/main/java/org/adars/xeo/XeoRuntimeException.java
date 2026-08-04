package org.adars.xeo;

/**
 * Base class for all unchecked exceptions thrown by Xeo components.
 */
public class XeoRuntimeException extends RuntimeException {

    public XeoRuntimeException() {
        super();
    }

    public XeoRuntimeException(final String message) {
        super(message);
    }

    public XeoRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public XeoRuntimeException(final Throwable cause) {
        super(cause);
    }
}
