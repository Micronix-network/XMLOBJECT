package it.micronixnetwork.gaf.util.xml;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * <b>Description </b> <br/>
 * </p>
 *
 * @author Andrea Riboldi <br/>
 */
class XMLObjectParser extends XMLHandlerAdapter {

    private XMLReader parser;

    /**
     *
     * @uml.property name="outObj"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private XMLObject outObj;

    /**
     *
     * @uml.property name="activeXMLobj"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private XMLObject activeXMLobj;

    XMLObjectParser() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            parser = saxParser.getXMLReader();
            parser.setContentHandler(this);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String name, String fullname, Attributes list) throws SAXException {
        if (activeXMLobj == null) {
            outObj.setTagName(name);
            activeXMLobj = outObj;
        } else {
            XMLObject child = new XMLObject(name);
            activeXMLobj.addContent(child);
            activeXMLobj = child;
        }
        for (int i = 0; i < list.getLength(); i++) {
            activeXMLobj.addAttribute(list.getLocalName(i), list.getValue(i));

        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length);
        if (value.trim().length() > 0) {
            if (value.equals("'")) {
                value = "''";
            }
            if (activeXMLobj.getText() != null) {
                activeXMLobj.setText(activeXMLobj.getText() + value);
            } else {
                activeXMLobj.setText(value);
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        activeXMLobj = activeXMLobj.getParent();
    }

    void createObject(XMLObject obj, InputStream stream) {
        outObj = obj;
        activeXMLobj = null;
        try {
            parser.parse(new InputSource(stream));
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parser = null;
        }
    }

}
