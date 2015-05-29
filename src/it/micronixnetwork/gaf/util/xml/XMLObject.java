package it.micronixnetwork.gaf.util.xml;

import java.awt.Color;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Andrea Riboldi <br/>
 */
public class XMLObject implements Serializable {

    private boolean encodingFlag = false;
    private boolean cdataFlag = true;
    /**
     * Il genitore dell'oggetto
     *
     * @uml.property name="parent"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private XMLObject parent;
    /**
     * Testo contenuto dal tag
     */
    private String text;
    /**
     * Il Vettore di XMLObject contenuti
     */
    protected ArrayList<XMLObject> xmlObjects = new ArrayList(0);
    /**
     * Hash di riferiemtno nominale agli attributi
     */
    private HashMap<String, XMLAttribute> attributes = new HashMap<String, XMLAttribute>();
    /**
     * Vettore di riferimento ordinato agli attributi
     */
    private ArrayList<XMLAttribute> att_ref = new ArrayList<XMLAttribute>();
    /**
     */
    private String tagName = null;

    // /////////////////////////////////////
    // operations
    /**
     */
    public void addAttribute(String name, String value) {
        if (value != null) {
            XMLAttribute att = attributes.get(name);
            if (att != null) {
                att.setValue(value);
            } else {
                att = new XMLAttribute(name, value);
                attributes.put(name, att);
                att_ref.add(att);
            }
        }
    } // end addAttribute

    /**
     */
    public String getAttribute(String name) {
        XMLAttribute attribute = attributes.get(name);
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    } // end getAttribute

    /**
     */
    public String[] getAllValues() {
        String[] result = new String[0];
        if (att_ref.size() != 0) {
            result = new String[att_ref.size()];
            for (int i = 0; i < att_ref.size(); i++) {
                result[i] = att_ref.get(i).getValue();
            }
        }
        return result;
    } // end getAllValues

    public boolean isCdataFlag() {
        return cdataFlag;
    }

    public void setCdataFlag(boolean cdataFlag) {
        this.cdataFlag = cdataFlag;
    }

    public boolean isEncodingFlag() {
        return encodingFlag;
    }

    public void setEncodingFlag(boolean encodingFlag) {
        this.encodingFlag = encodingFlag;
    }

    /**
     * Return all children
     * @return
     */
    public List<XMLObject> getChildren() {
        return xmlObjects;
    }

    /**
     * Return the first child width the specified tag
     * @param name the tag
     * @return
     */
    public List<XMLObject> getChildren(String name) {
        Iterator children = xmlObjects.iterator();
        ArrayList<XMLObject> result = new ArrayList<XMLObject>();
        while (children.hasNext()) {
            XMLObject child = (XMLObject) children.next();
            if (child.getTagName().equals(name)) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Return children of specified type
     * @param type the class type of children
     * @return
     */
    public List getChildren(Class type) {
        List<XMLObject> result = new ArrayList<XMLObject>();
        for (XMLObject child : xmlObjects) {
            if (type.isAssignableFrom(child.getClass())) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     */
    public String[] getAllAttributeNames() {
        String[] result = new String[0];
        Set keys = attributes.keySet();
        if (keys != null) {
            result = new String[keys.size()];
            keys.toArray(result);
        }
        return result;
    } // end getAllAttributeNames

    /**
     *
     * @uml.property name="tagName"
     */
    public final void setTagName(String tagName) {
        this.tagName = tagName;
    } // end setTagName

    /**
     *
     * @uml.property name="tagName"
     */
    public final String getTagName() {
        return (this.tagName);
    } // end getTagName

    /**
     */
    public XMLObject() {
        // your code here
    } // end XMLTag

    /**
     */
    public XMLObject(String tagName) {
        setTagName(tagName);
    } // end XMLTag

    /**
     */
    public void addContent(XMLObject value) {
        if (value != null && text == null) {
            value.setParent(this);
            xmlObjects.add(value);
        }
    } // end addXMLObject

    /**
     * Rimuove un contenuto dall'oggetto
     * @param value l'oggetto XML da rimuovere
     */
    public boolean removeContent(XMLObject value) {
        if (value != null) {
            return xmlObjects.remove(value);
        }
        return false;
    } // end removeObject


    /**
     * Rimuove un contenuto di uno specifico tipo
     * @param type il tipo dell'oggetto da rimuovere
     * @return la lista degli oggetti rimossi
     */
//    public List removeTypedContent(Class type) {
//	ArrayList<XMLObject> result = new ArrayList<XMLObject>();
//	Iterator children = xmlObjects.iterator();
//	while (children.hasNext()) {
//	    XMLObject child = (XMLObject) children.next();
//	    if (type.isAssignableFrom(child.getClass())) {
//		result.add(child);
//		children.remove();
//	    }
//	}
//	return result;
//    } // end removeTypedContent

    /**
     * Rimuove tutti i contenuti (tag figli) dell'oggetto
     */
    public void  removeContents() {
       xmlObjects.clear();
    } // end removeContents

    /**
     */
    public boolean isEMpty() {
        return xmlObjects.isEmpty();
    } // end isEMpty

    /**
     */
    public String describe(String parentIdent, String ident) {
        String space = "";
        if (getParent() != null) {
            space = parentIdent + ident;
        }
        StringBuffer out = new StringBuffer(space + "<" + this.getTagName());
        for (int i = 0; i < att_ref.size(); i++) {
        	String att_value=att_ref.get(i).getValue();
        	if(isEncodingFlag()){
        		att_value=filterPCDATA(att_value);
        	}
            out.append(" " + att_ref.get(i).getName() + "=\"" +att_value+ "\"");
        }

        Iterator objs = xmlObjects.iterator();
        if (!objs.hasNext() && text == null) {
            out.append("/>");
        } else {
            out.append(">");
            boolean newLine = false;
            while (objs.hasNext()) {
                XMLObject obj = (XMLObject) objs.next();
                out.append("\n" + obj.describe(space, ident));
                newLine = true;
            }
            if (text != null) {
                String toOut=text;
                if(isEncodingFlag()){
                    toOut=filterPCDATA(toOut);
                }
                if(isCdataFlag()){
                    toOut=cdataBound(toOut);
                }
                out.append(toOut);
            }
            if (newLine) {
                out.append("\n" + space + "</" + this.getTagName() + ">");
            } else {
                out.append("</" + this.getTagName() + ">");
            }
        }
        return out.toString();
    } // end describe

    public Object describe() {
        return describe("", " ");
    }

    public String describeInLine() {
        StringBuffer out = new StringBuffer("<" + this.getTagName());
        for (int i = 0; i < att_ref.size(); i++) {
            out.append(" " + att_ref.get(i).getName() + "=\"" + filterPCDATA(att_ref.get(i).getValue()) + "\"");
        }
        Iterator objs = xmlObjects.iterator();
        if (!objs.hasNext() && text == null) {
            out.append("/>");
        } else {
            out.append(">");
            while (objs.hasNext()) {
                XMLObject obj = (XMLObject) objs.next();
                out.append(obj.describeInLine());
            }
            if (text != null) {
                String toOut=text;
                if(isEncodingFlag()){
                    toOut=filterPCDATA(toOut);
                }
                if(isCdataFlag()){
                    toOut=cdataBound(toOut);
                }
                out.append(toOut);
            }
            out.append("</" + this.getTagName() + ">");
        }
        return out.toString();
    }// end describeInLine

    /**
     *
     * @uml.property name="text"
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @uml.property name="text"
     */
    public void setText(String text) {
        // this.text = filterPCDATA(text);
        this.text = text;
    }

    /**
     *
     * @uml.property name="parent"
     */
    public XMLObject getParent() {
        return parent;
    } //end getParent

    /**
     *
     * @uml.property name="parent"
     */
    public void setParent(XMLObject parent) {
        this.parent = parent;
    } // end setParent

    /**
     */
    public String toString() {
        return (String) describe();
    } // end toString

    /**
     *
     * @uml.property name="attributes"
     */
    public List getAttributes() {
        return att_ref;

    }

    private static String filterPCDATA(String s) {
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '&':
                    s = s.substring(0, i) + "&amp;" + s.substring(i + 1);
                    break;
                case '>':
                    s = s.substring(0, i) + "&gt;" + s.substring(i + 1);
                    break;
                case '<':
                    s = s.substring(0, i) + "&lt;" + s.substring(i + 1);
                    break;
                case '\"':
                    s = s.substring(0, i) + "&quot;" + s.substring(i + 1);
                    break;
                case '\'':
                    s = s.substring(0, i) + "&apos;" + s.substring(i + 1);
                    break;
            }
        }
        return s;
    }

    private static String cdataBound(String s){
        return "<![CDATA["+s+"]]>";
    }

    public void load(InputStream stream) {
        XMLObjectParser parser = new XMLObjectParser();
        this.purge();
        parser.createObject(this, stream);
        parser = null;
    }

    public void purge() {
        Iterator xmlobjs = xmlObjects.iterator();
        while (xmlobjs.hasNext()) {
            XMLObject element = (XMLObject) xmlobjs.next();
            element.purge();
        }
        xmlObjects.clear();
        attributes.clear();
    }

    public Object clone() {
        XMLObject result = null;
        try {
            result = this.getClass().newInstance();
            Iterator<XMLAttribute> iter = attributes.values().iterator();
            while (iter.hasNext()) {
                XMLAttribute elem = iter.next();
                result.addAttribute(elem.getName(), elem.getValue());
            }
            Iterator<XMLObject> children = getChildren().iterator();
            while (children.hasNext()) {
                XMLObject elem = children.next();
                result.addContent((XMLObject) elem.clone());
            }
            result.setText(getText());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getIntAttrubute(String name, int def) {
        try {
            return Integer.parseInt(getAttribute(name));

        } catch (Exception e) {
        }
        return def;
    }

    public String getStringAttrubute(String name, String def) {
        String result = getAttribute(name);
        if (result == null) {
            return def;
        }
        return result;
    }

    public float getFloatAttrubute(String name, float def) {
        try {
            return Float.parseFloat(getAttribute(name));
        } catch (Exception e) {
        }
        return def;
    }

    public double getDoubleAttrubute(String name, double def) {
        try {
            return Double.parseDouble(getAttribute(name));

        } catch (Exception e) {
        }
        return def;
    }

    public boolean getBooleanAttrubute(String name, boolean def) {
        try {
            return Boolean.getBoolean(getAttribute(name));
        } catch (Exception e) {
        }
        return def;
    }

    public Color getColorAttrubute(String name, Color def) {
        try {
            return Color.decode(getAttribute(name));
        } catch (Exception e) {
        }
        return def;
    }

    public Date getDateAttribute(String name, Date def) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return sdf.parse(getAttribute(name));
        } catch (Exception e) {
        }
        return def;
    }

    public void setIntAttrubute(String name, int value) {
        addAttribute(name, Integer.toString(value));
    }

    public void setFloatAttrubute(String name, float value) {
        addAttribute(name, Float.toString(value));
    }

    public void setDoubleAttrubute(String name, double value) {
        addAttribute(name, Double.toString(value));
    }

    public void setBooleanAttrubute(String name, boolean value) {
        addAttribute(name, Boolean.toString(value));
    }

    public void setColorAttrubute(String name, Color value) {
        addAttribute(name, Integer.toString(value.getRGB()));
    }

    public void setDateAttribute(String name, Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        addAttribute(name, sdf.format(value));
    }

    public void setStringArrayAttribute(String name, String[] value) {
        addAttribute(name, tranformArrayToString(value));
    }

    public void setStringArrayAttribute(String name, List<String> value) {
        addAttribute(name, tranformArrayToString(value));
    }
    
    public String[] getStringArrayAttribute(String name, String[] def) {
        String[] result = stringToList(getAttribute(name));
        if (result == null) {
            return def;
        }
        return result;
    }

    private String tranformArrayToString(String[] value) {
        StringBuffer result = new StringBuffer("");
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                result.append("'"+value[i]+"'");
                if (i < (value.length - 1)) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }
    
    private String tranformArrayToString(List<String> value) {
        StringBuffer result = new StringBuffer("");
        if (value != null) {
            for (int i = 0; i < value.size(); i++) {
                result.append("'"+value.get(i)+"'");
                if (i < (value.size() - 1)) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    private String[] stringToList(String val) {
        if (val != null) {
        	if(val.isEmpty()) return null;
            String[] list = val.split("[ ]*','[ ]*");
            int size=list.length;
            if(size>0){
        	list[0]=list[0].substring(1);
        	list[size-1]=list[size-1].substring(0,list[size-1].length()-1);
            }
            return list;
        } else {
            return null;
        }
    }
} // end XMLObject

