package gov.nasa.pds.harvest.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;


public class MonitoringUtils
{
    public static long openFiles()
    {
        OperatingSystemMXBean mb = ManagementFactory.getOperatingSystemMXBean();
        if(mb instanceof UnixOperatingSystemMXBean)
        {
            return ((UnixOperatingSystemMXBean)mb).getOpenFileDescriptorCount();
        }
        
        return 0;
    }
}
