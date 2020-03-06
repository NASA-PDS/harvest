package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.policy.PolicyReader;
import gov.nasa.pds.harvest.cfg.policy.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;


public class TestPolicyReader
{

    public static void main(String[] args) throws Exception
    {
        testRead1();
    }
    

    public static void testRead1() throws Exception
    {
        PolicyReader rd = new PolicyReader();
        Policy policy = rd.read(new File("/ws2/harvest/conf/t1.xml"));
        
        System.out.println("\nDirectories\n===============");
        System.out.println(policy.directories.paths);
        System.out.println(policy.directories.fileFilterIncludes);
        System.out.println(policy.directories.fileFilterExcludes);
        System.out.println(policy.directories.dirFilterExcludes);
        
        System.out.println("\nAccesUrl\n===============");
        for(ReplaceRule rule: policy.accessUrl.rules)
        {
            System.out.println(rule.prefix + " --> " + rule.replacement);
        }

        System.out.println("\nXPathMaps\n===============");
        System.out.println("baseDir = " + policy.xpathMaps.baseDir);
        
        for(XPathMap map: policy.xpathMaps.items)
        {
            System.out.println(map.objectType + " --> " + map.filePath);
        }
    }

    
}
