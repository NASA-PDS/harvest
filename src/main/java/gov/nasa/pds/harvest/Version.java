package gov.nasa.pds.harvest;

public class Version extends gov.nasa.pds.registry.common.Version {
  private static Version self = null;
  public static synchronized Version instance() {
    if (self == null) {
      self = new Version();
    }
    return self;
  }
  @Override
  public String getName() {
    return "harvest";
  }
}
