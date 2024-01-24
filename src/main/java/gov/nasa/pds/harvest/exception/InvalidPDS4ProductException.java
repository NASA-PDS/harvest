package gov.nasa.pds.harvest.exception;

/**
 * InvalidPDS4ProductException is a custom exception to throw when there is an
 * error reading bundle/collection references.
 */
public class InvalidPDS4ProductException extends Exception {
    private static final long serialVersionUID = 8063779503526800982L;

    public InvalidPDS4ProductException(String errorMessage) {
        super(errorMessage);
    }
}
