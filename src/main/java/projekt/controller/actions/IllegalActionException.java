package projekt.controller.actions;

/**
 * An exception that is thrown when an action is illegal.
 * An action is illegal if it cannot be executed for any reason.
 */
public class IllegalActionException extends Exception {
    public IllegalActionException(final String message) {
        super(message);
    }

    public IllegalActionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
