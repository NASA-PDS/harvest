package gov.nasa.pds.harvest.search.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;

import gov.nasa.pds.search.core.logging.ToolsLevel;
import gov.nasa.pds.search.core.logging.ToolsLogRecord;
import gov.nasa.pds.search.core.util.InvalidDatetimeException;
import gov.nasa.pds.search.core.util.PDSDateConvert;

public class DocWriter 
{
	private FileWriter solrDoc;
	private Map<String, List<String>> map;
	private Map<String, String> typeMap;
	private String classname;
	private String fnameprefix = "solr_doc";
	private String fnameext = "xml";

	private Logger log = Logger.getLogger(this.getClass().getName());
	

	public DocWriter(Map<String, List<String>> map, File basedir, int seq, 
			String productTitle, Map<String, String> typeMap) 
	{
		try 
		{
			this.map = map;
			this.typeMap = typeMap;
			this.classname = productTitle;

			// Get the filepath
			String filepath = getFilename(basedir.getAbsolutePath(), seq);

			// Open a new file writer
			open(filepath);
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	
	public void write() 
	{
		try 
		{
			this.solrDoc.write("<doc>\n");

			Set<String> set = this.map.keySet();
			
			for(String fieldName: set) 
			{
				List<String> valArray = this.map.get(fieldName);
				if(valArray != null) 
				{
					for(String value: valArray) 
					{
						if(this.typeMap.get(fieldName).equalsIgnoreCase("date")) 
						{
							try 
							{
								value = PDSDateConvert.convert(fieldName, (String) value);
							} 
							catch(InvalidDatetimeException ex) 
							{
								log.log(new ToolsLogRecord(ToolsLevel.WARNING, ex.getMessage() + " - " + fieldName));
								value = PDSDateConvert.getDefaultTime(fieldName);
							}
						} 
						else if(fieldName.equalsIgnoreCase("resclass")) 
						{
							value = this.classname;
						} 

						String escValue = StringEscapeUtils.escapeXml(value);
						this.solrDoc.write("<field name=\"" + fieldName + "\">" + escValue + "</field>\n");
					}
				}
			}

			this.solrDoc.write("</doc>\n</add>\n");
			this.solrDoc.close();
		} 
		catch(Exception ex) 
		{
			ex.printStackTrace();
		} 
		finally 
		{
			try 
			{
				this.solrDoc.close();
			} 
			catch(Exception e) 
			{
				// do nothing
			}
		}
	}

	
	/**
	 * Open a new Filewriter object
	 * 
	 * If the file already exists, simply open and append. If the file does not
	 * exist, open and write initial <add> tag
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
		String fname = path + File.separator + fnameprefix + "_" + itemoid + "." + fnameext;

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
		} while (b != 10 && length > 0);
		if (length == 0) {
			raf.setLength(length);
		} else {
			raf.setLength(length + 1);
		}
		raf.close();
	}
}
