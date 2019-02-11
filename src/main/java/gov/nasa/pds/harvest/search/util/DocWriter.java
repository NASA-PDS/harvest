package gov.nasa.pds.harvest.search.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import gov.nasa.pds.search.core.logging.ToolsLevel;
import gov.nasa.pds.search.core.logging.ToolsLogRecord;
import gov.nasa.pds.search.core.util.InvalidDatetimeException;
import gov.nasa.pds.search.core.util.PDSDateConvert;

public class DocWriter {

  private FileWriter solrDoc;
  private Map map;
  private Map typeMap;
  private String classname;
  private String fnameprefix = "solr_doc";
  private String fnameext = "xml";

  private Logger log = Logger.getLogger(this.getClass().getName());

  public DocWriter(Map map, File basedir, int seq, String productTitle, Map typeMap) {
    try {
      this.map = map;
      this.typeMap = typeMap;
      this.classname = productTitle;
      
      // Get the filepath
      String filepath = getFilename(basedir.getAbsolutePath(), seq);

      // Open a new file writer
      open(filepath);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write() {
    List valArray;

    try {
      this.solrDoc.write("<doc>\n");

  		Set set = this.map.keySet();
  		for (Object name : set) {
  			String fieldName = String.valueOf(name);
  			valArray = (List) this.map.get(fieldName);
  			if (valArray != null) {
  				for (Object value : valArray) {
  					if (((String) this.typeMap.get(fieldName)).equalsIgnoreCase("date")) {
  						try {
  							value = PDSDateConvert.convert(fieldName, (String)value);
  						} catch (InvalidDatetimeException e) {
  					        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
  					        		e.getMessage() + " - " + fieldName));
  							value = PDSDateConvert.getDefaultTime(fieldName);
  						}
  					} else if (fieldName.equalsIgnoreCase("resclass")){
  						value = "<![CDATA[" + this.classname + "]]>";
  					} else {
  					  // If the value isn't an integer, let's wrap it
  					  try {
  					    int t = Integer.parseInt(String.valueOf(value));
  					  } catch (NumberFormatException e) {
  					    // Not an integer, so wrap in CDATA
  					    value = "<![CDATA[" + String.valueOf(value) + "]]>";
  					  }
  					}
  
  					// The XML Transformer converts the CDATA wrappers to encode
  					// &lt; and &gt; so need to write straight to a file
  					this.solrDoc.write("<field name=\"" + fieldName + "\">" + value + "</field>\n");

  				}
  			}
  		}

  		this.solrDoc.write("</doc>\n</add>\n");
      this.solrDoc.close();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.solrDoc.close();
      } catch (Exception e) {
        // do nothing
      }
    }
  }
  
  /**
   * Open a new Filewriter object
   * 
   * If the file already exists, simply open and append.
   * If the file does not exist, open and write initial <add> tag
   * 
   * @param filepath
   * @throws IOException
   */
  private void open(String filepath) throws IOException {
    // Check if file exists
    File f = new File(filepath);

    // If it exists, just open and append to it
    if (f.exists()) {
      // Remove last line of file since it has an </add> closing tag
      removeLastLine(filepath);
      this.solrDoc = new FileWriter(filepath, true);
    } else {
      // If it doesn't exist, lets open and write the initial <add> tag
      this.solrDoc = new FileWriter(filepath, true);
      this.solrDoc.write("<add>\n");
    }
  }

  private String getFilename(String path, int seq) {
//    int seq = checkThreshold(seq);

    String itemoid = String.valueOf(seq);

    /* Start profile output */
    String fname = path + File.separator + fnameprefix + "_" + itemoid + "."
        + fnameext;

    return fname;
  }

  private void removeLastLine(String filepath) throws IOException {
    // Remove the last line
    RandomAccessFile raf = new RandomAccessFile(filepath, "rw");
    byte b;
    long length = raf.length() - 1;
    do {
      length -= 1;
      raf.seek(length);
      b = raf.readByte();
    } while(b != 10 && length > 0);
    if (length == 0) { 
      raf.setLength(length);
    } else {
      raf.setLength(length + 1);
    }
    raf.close();
  }
}
