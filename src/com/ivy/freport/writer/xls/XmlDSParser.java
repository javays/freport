package com.ivy.freport.writer.xls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sf.ecbil.bill.util.Config;

public class XmlDSParser {
    
    public final Logger logger = Logger.getLogger(XmlDSParser.class);

    private List<File> xmlFiles;
    private String curFile;
    private int fileSeq = 0;
    
    private List<XmlDSListener> xmlDSListeners;
    
    /**
     * @param hssfWorkbook
     * @param cellIndex
     * @param cellWrappers
     */
    public XmlDSParser(List<File> xmlFiles) {
        super();
        this.xmlFiles = xmlFiles;
    }
    
    public void addListener(XmlDSListener xmlDSListener) {
        if (xmlDSListeners == null) {
            xmlDSListeners = new ArrayList<XmlDSListener>();
        }
        xmlDSListeners.add(xmlDSListener);
    }

    public void parse() {
        try {
            if (xmlFiles == null || xmlFiles.size() == 0) {
                return;
            }
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XmlDSSaxHandler dh = new XmlDSSaxHandler();
            for (File xmlFile : xmlFiles) {
                curFile = xmlFile.getAbsolutePath();
                parser.parse(xmlFile, dh);
                parser.reset();
                fileSeq ++;
            }
        } catch (ParserConfigurationException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (SAXException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }
    }
    
    class XmlDSSaxHandler extends DefaultHandler {
        
        @Override
        public void startDocument() throws SAXException {
            logger.debug("------------["+ curFile +"] start document------------------");
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            logger.debug("------------["+ curFile +"] end document------------------");
            super.endDocument();
        }

        @Override
        public void startElement(String uri,
                            String localName,
                            String qName,
                            Attributes attributes) throws SAXException {
            
            if (Config.CACHE_XML_ROOT_NAME.equals(qName)) {
                return;
            }
            
            for (XmlDSListener xmlDSListener : xmlDSListeners) {
                xmlDSListener.readNextElement(fileSeq, qName, attributes);
            }
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
        }
    }
}