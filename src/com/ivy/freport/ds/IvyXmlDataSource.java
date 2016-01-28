/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

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

import com.ivy.freport.utils.StringUtils;

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
    
    private List<File> xmlFiles;
    private int size;
    private List<String> skipElementsName;
    private List<IvyDSAccessListener<Attributes>> listeners;
    
    private String curXmlFile;
    
    /**
     * @param xmlFiles
     * @param skipElementsName
     */
    public IvyXmlDataSource(List<File> xmlFiles, int size, List<String> skipElementsName) {
        super();
        this.xmlFiles = xmlFiles;
        this.size = size;
        this.skipElementsName = skipElementsName;
    }
    
    /**
     * @param xmlFile
     * @param skipElementsName
     */
    public IvyXmlDataSource(File xmlFile, int size, List<String> skipElementsName) {
        this((List<File>)null, size, skipElementsName);
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
    
    public void addListener(IvyDSAccessListener<Attributes> listener) {
        if (listeners == null) {
            listeners = new ArrayList<IvyDSAccessListener<Attributes>>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
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
            
            onNextItem(attributes);
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

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#onNextItem()
     */
    @Override
    public void onNextItem(Attributes attributes) {
        if (listeners != null) {
            for (IvyDSAccessListener<Attributes> ivyDSAccessListener : listeners) {
                ivyDSAccessListener.nextElement(attributes);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#getTotal()
     */
    @Override
    public int size() {
        return size;
    }
}
