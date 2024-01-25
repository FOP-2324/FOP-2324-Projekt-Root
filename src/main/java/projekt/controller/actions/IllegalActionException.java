package projekt.controller.actions;

public class IllegalActionException extends Exception {
    public IllegalActionException(final String message) {
        super(message);
    }

    public IllegalActionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
