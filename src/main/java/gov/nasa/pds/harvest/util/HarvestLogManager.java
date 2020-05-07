package gov.nasa.pds.harvest.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HarvestLogManager
{
    public static Logger getMinInfoLogger()
    {
        return LogManager.getLogger("harvest-min-info");
    }
}
