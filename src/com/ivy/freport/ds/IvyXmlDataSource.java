/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ivy.freport.utils.StringUtils;
import com.ivy.freport.writer.pdf.IvyXmlAttrConsumer;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月27日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月27日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */
public class IvyXmlDataSource implements IvyDataSource<Attributes> {
    
    public final Logger logger = Logger.getLogger(IvyXmlDataSource.class);
    
    private Consumer<Attributes> consumer;

    private List<File> xmlFiles;
    private List<String> skipElementsName;
    
    private String curXmlFile;
    
    private int curRow = 0;
    
    /**
     * @param xmlFiles
     * @param skipElementsName
     */
    public IvyXmlDataSource(List<File> xmlFiles, Consumer<Attributes> consumer, List<String> skipElementsName) {
        super();
        this.xmlFiles = xmlFiles;
        this.consumer = consumer;
        this.skipElementsName = skipElementsName;
        
        if (consumer == null) {
            consumer = new IvyXmlAttrConsumer();
        }
    }
    
    /**
     * @param xmlFile
     * @param skipElementsName
     */
    public IvyXmlDataSource(File xmlFile, Consumer<Attributes> consumer, List<String> skipElementsName) {
        this((List<File>)null, consumer, skipElementsName);
        xmlFiles = new ArrayList<File>(1);
        xmlFiles.add(xmlFile);
    }
    
    /**
     * 忽略节点
     * @param name
     * @return
     */
    public boolean isSkip(String name) {
        if (StringUtils.isEmpty(name)) {
            return true;
        }
        
        if (skipElementsName != null && skipElementsName.size() > 0) {
            for (String _name : skipElementsName) {
                if (name.equals(_name)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    class XmlDSSaxHandler extends DefaultHandler {
        
        @Override
        public void startDocument() throws SAXException {
            logger.debug("------------["+ curXmlFile +"] start document------------------");
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            logger.debug("------------["+ curXmlFile +"] end document------------------");
            super.endDocument();
        }

        @Override
        public void startElement(String uri,
                            String localName,
                            String qName,
                            Attributes attributes) throws SAXException {
            
            if (isSkip(qName)) {
                return;
            }
            
            consumer.accept(attributes);
            curRow ++;
            
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
    
    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#curRow()
     */
    @Override
    public int curRow() {
        return curRow;
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#hasNext()
     */
    @Override
    public boolean hasNext() {
        throw new RuntimeException("not supported!!!");
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#next(java.util.function.Consumer)
     */
    @Override
    public void next() {
        try {
            if (xmlFiles == null || xmlFiles.size() == 0) {
                return;
            }
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XmlDSSaxHandler dh = new XmlDSSaxHandler();
            for (File xmlFile : xmlFiles) {
                if (xmlFile == null) {
                    continue;
                }
                curXmlFile = xmlFile.getAbsolutePath();
                parser.parse(xmlFile, dh);
                parser.reset();
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
}
