/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.exception;

public class MagicException extends Exception {
    public MagicException() {
    }

    public MagicException(String message) {
        super(message);
    }

    public MagicException(String message, Throwable cause) {
        super(message, cause);
    }

    public MagicException(Throwable cause) {
        super(cause);
    }

    public MagicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
