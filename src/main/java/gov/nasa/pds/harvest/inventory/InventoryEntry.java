// Copyright 2006-2010, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
package gov.nasa.pds.harvest.inventory;

import java.io.File;

/**
 * Class representation of a single entry in a PDS Inventory file.
 *
 * @author mcayanan
 *
 */
public class InventoryEntry {
    /** A product file. */
    private File file;

    /** A checksum. */
    private String checksum;

    /** A logical identifier. */
    private String identifier;

    /** Default constructor */
    public InventoryEntry() {
        this.file = null;
        this.checksum = "";
        this.identifier = "";
    }

    /**
     * Constructor.
     *
     * @param file A product file.
     * @param checksum checksum.
     * @param identifier logical identifier.
     */
    public InventoryEntry(File file, String checksum, String identifier) {
        this.file = file;
        this.checksum = checksum;
        this.identifier = identifier;
    }

    /**
     * Gets the file.
     *
     * @return The file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the checksum.
     *
     * @return Checksum value.
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Gets the logical identifier.
     *
     * @return A LID or LIDVID.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Determines whether the object is empty.
     *
     * @return true if the object is empty, false otherwise.
     */
    public boolean isEmpty() {
        if (this.file == null && this.checksum.isEmpty()
            && this.identifier.isEmpty()) {
          return true;
        } else {
          return false;
        }
    }
}
