package gov.nasa.pds.harvest.exception;

/**
 * InvalidPDS4ProductException is a custom exception to throw when there is an
 * error reading bundle/collection references.
 */
public class InvalidPDS4ProductException extends Exception {
    public InvalidPDS4ProductException(String errorMessage) {
        super(errorMessage);
    }
}
