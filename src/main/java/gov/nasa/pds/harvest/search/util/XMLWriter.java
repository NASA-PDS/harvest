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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XMLWriter {

  private Map map;
  private Map typeMap;
  private File basedir;
  private String filename;
  private String fnameprefix = "core";
  private String fnameext = "xml";

  private Document doc;
  private Element classElement;

  private Logger log = Logger.getLogger(this.getClass().getName());
  
  private static final String MISSING_VALUE_REPLACEMENT = "N/A";
  private static final ArrayList<String> BAD_VALUES_LIST = new ArrayList<String>();
  
  static {
    //BAD_VALUES_LIST.add("NULL");
    //BAD_VALUES_LIST.add("");
    //BAD_VALUES_LIST.add("UNK");
  }

  public XMLWriter(Map map, File basedir, int seq, String productTitle, Map typeMap) {
    try {
      this.map = map;
      this.typeMap = typeMap;
      this.basedir = basedir;

      this.filename = getFilename(seq, productTitle);

      DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
      this.doc = docBuilder.newDocument();

      Element root = doc.createElement("doc");
      this.doc.appendChild(root);

      this.classElement = doc.createElement(productTitle);
      root.appendChild(this.classElement);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write() {
    //String name = "";
    //String value = "";
    List valArray;
    try {
      Set set = this.map.keySet();
      for (Object name : set) {
        String fieldName = String.valueOf(name);
        valArray = (List) this.map.get(fieldName);
        if (valArray != null) {
          for (Object value : valArray) {
            //Debugger.debug("name: "+name);
            //Debugger.debug("value: "+value);
            addElement(fieldName, String.valueOf(value), (String)this.typeMap.get(fieldName));
          }
        } //else {
          //addElement(String.valueOf(name), "");
        //}
      }

      // set up transformer
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();

      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      trans.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount", "2");
      trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      // create file from xml tree
      StreamResult result = new StreamResult(new File(basedir, filename));

      DOMSource source = new DOMSource(this.doc);
      trans.transform(source, result);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getFilename(int seq, String classname) {
    Integer oidseqi = new Integer(seq);
    String itemoid = oidseqi.toString();

    /* Start profile output */
    String fname = fnameprefix + "_" + classname + "_" + itemoid + "."
        + fnameext;

    // log.info("Filename: "+fname);

    return fname;
  }

  public void addElement(String name, String value, String type) {
    // Temporary variables to hold name and value
    String tName, tValue;

    tName = name.trim();
    
    //if (!BAD_VALUES_LIST.contains(value.toUpperCase())) {
        tValue = value.trim();
          Element element = doc.createElement(tName);
          this.classElement.appendChild(element);

          Text text = doc.createTextNode(tValue);
          if (type != null) {
            element.setAttribute("type", type.trim());
          }
          element.appendChild(text);
    //}

    // Previous method used to encode only select HTML entities
    //tName = repAllCharWStr(tName);
    //tValue = repAllCharWStr(tValue);

  }

  /**
   * Remove String Nulls
   */
  public String remNull(String s1) {
    if (s1 == null) {
      return "NULL";
    }
    return s1;
  }

  /**
   * Replace character with string
   */
  public String repCharWStr(String str1, char rc, String rstr) {
    int p1, str1len;
    char tc;
    StringBuffer sbuff1 = new StringBuffer(str1), sbuff2 = new StringBuffer();

    p1 = 0;
    str1len = sbuff1.length();
    while (p1 < str1len) {
      tc = sbuff1.charAt(p1);
      if (tc == rc) {
        sbuff2.append(rstr);
      } else {
        sbuff2.append(tc);
      }
      p1++;
    }
    return sbuff2.toString();
  }

}
