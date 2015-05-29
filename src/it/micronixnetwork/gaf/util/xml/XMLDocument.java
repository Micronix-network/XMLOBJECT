package it.micronixnetwork.gaf.util.xml;

import java.io.Serializable;

/**
 * @author Andrea Riboldi
 * <br/>
 */

public class XMLDocument implements Serializable{

	/**
	 * Identifica la codifica del documento
	 */
	private String encoding = null;

	/**
	 * Identifica la versione XML del documento 
	 */
	private String version = null;

	/**
	 * Identifica il dtd da utilizzare per verificare il documento. 
	 */
	private String dtd = null;

    /**
     * Il tag di root del documento XML.
     * 
     * @uml.property name="xmlRoot"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private XMLObject xmlRoot;

 


/**
 * Costruttore di default
 */
    public  XMLDocument() {  
        //codifica di default
        encoding="UTF-8";
        //versione di default
        version="1.0";
    } // end XMLDocument          

/**
 * Crea un documento XML con uno specifica versione e codifica
 * @param version la versione
 * @param encoding la codifica
 */
    public  XMLDocument(String version, String encoding) {        
        this.encoding=encoding;
        this.version=version;
    } // end XMLDocument        

/**
 * Descrive il documento
 * @return a String with ...
 */
    public String describe() {        
        StringBuffer out=new StringBuffer("<?xml version=\""+version+"\" encoding=\""+encoding+"\"?>\n");
        if(dtd!=null)
        {
            out.append(dtd+"\n\n");
        }
        if(xmlRoot!=null)
            out.append(xmlRoot.describe());
        return out.toString();
    } // end describe

    /**
     * Setta la codifica
     * @param encoding la codifica.
     * 
     * @uml.property name="encoding"
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    } // end setEncoding

    /**
     * Setta la versione
     * @param version la versione.
     * 
     * @uml.property name="version"
     */
    public void setVersion(String version) {
        this.version = version;
    } // end setVersion

    /**
     * Restitiusce la codifica
     * @return la stringa che rappresenta la codifica
     * 
     * @uml.property name="encoding"
     */
    public String getEncoding() {
        return (this.encoding);
    } // end getEncoding

    /**
     * Does ...
     * @return a String with ...
     * 
     * @uml.property name="version"
     */
    public String getVersion() {
        return (this.version);
    } // end getVersion

    /**
     * Retituisce il dtd.
     * @return una stringa che rappresenta il dtd.
     * 
     * @uml.property name="dtd"
     */
    public String getDtd() {
        return dtd;
    } // end getDtd

    /**
     * Setta il dtd del documento.
     * @param dtd il dtd del documento.
     * 
     * @uml.property name="dtd"
     */
    public void setDtd(String dtd) {
        this.dtd = dtd;
    } // end setDtd

/**
 * <p>
 * Debug main
 * </p><p>
 * 
 * </p><p>
 * 
 * @param args ...
 * </p>

    public static void main(String[] args) {        
        XMLDocument doc=new XMLDocument();
        XMLTag tag=new XMLTag("test");
        XMLTag tag2=new XMLTag("test2");
        tag.addAttribute("nome", "pippo");
        tag.addAttribute("test_att", "test_value");
        tag2.addXMLObject(new XMLText("Test text"));
        
        tag.addXMLObject(tag2);
        doc.setXMLRoot(tag);
        System.out.println(doc.describe());
    } // end main        
*/
    
/**
 * Restituisce il tag root del documento.
 * @return il tag che rappresenta la root del documento.
 */
    public XMLObject getXMLRoot() {        
        // your code here
        return xmlRoot;
    } // end getXMLRoot        

/**
 * Setta il tag di root del documento.
 * @param xmlRoot il tag di root.
 */
    public void setXMLRoot(XMLObject xmlRoot) {        
        this.xmlRoot=xmlRoot;
    } // end setXMLRoot        

} // end XMLDocument



