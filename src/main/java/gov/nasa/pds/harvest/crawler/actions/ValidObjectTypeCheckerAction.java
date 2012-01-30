package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.crawler.metadata.PDSCoreMetKeys;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;

public class ValidObjectTypeCheckerAction extends CrawlerAction {
    private static Logger log = Logger.getLogger(
            RegistryUniquenessCheckerAction.class.getName());
    private final String ID = "ValidObjectTypeCheckerAction";
    private final String DESCRIPTION = "Checks if the object type is part of "
        + "the list of valid object types.";
    private List<String> objectTypes;

    public ValidObjectTypeCheckerAction(List<String> objectTypes) {
        super();
        this.objectTypes = new ArrayList<String>();
        this.objectTypes.addAll(objectTypes);
        String []phases = {CrawlerActionPhases.PRE_INGEST};
        setPhases(Arrays.asList(phases));
        setId(ID);
        setDescription(DESCRIPTION);
    }

    @Override
    public boolean performAction(File product, Metadata productMetadata)
            throws CrawlerActionException {
        String type = productMetadata.getMetadata(PDSCoreMetKeys.OBJECT_TYPE);
        for(String validType : objectTypes) {
            if(validType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        log.log(new ToolsLogRecord(Level.WARNING,
                "\'" + type + "\' is not an object type found in the policy "
                + "file.", product));
        return false;
    }

}
