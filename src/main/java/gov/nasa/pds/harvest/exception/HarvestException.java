package gov.nasa.pds.harvest.exception;

/**
 * Thrown when Harvest completes processing but the outcome is a failure
 * (e.g. one or more files could not be loaded).
 */
public class HarvestException extends Exception {
  private static final long serialVersionUID = 1L;

  public HarvestException(String message) {
    super(message);
  }
}
