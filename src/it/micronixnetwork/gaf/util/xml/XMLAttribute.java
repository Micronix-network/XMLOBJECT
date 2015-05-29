
package it.micronixnetwork.gaf.util.xml;

import java.io.Serializable;


/**
 * @author Andrea Riboldi
 * <br/>
 * 
 */
public class XMLAttribute implements Serializable{
	
	private String name;
	private String value;

	/**
	 * 
	 */
	public XMLAttribute(String name,String value) {
		super();
		this.name=name;
		this.value=value;
	}

	/**
	 * @return Returns the name.
	 * 
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 * 
	 * @uml.property name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the value.
	 * 
	 * @uml.property name="value"
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 * 
	 * @uml.property name="value"
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		XMLAttribute result=new XMLAttribute(this.name,this.value);
		return result;
		
	}

}
