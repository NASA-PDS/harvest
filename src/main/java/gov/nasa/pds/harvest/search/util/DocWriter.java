// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// * Neither the name of Caltech nor its operating division, the Jet Propulsion
// Laboratory, nor the names of its contributors may be used to endorse or
// promote products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

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
