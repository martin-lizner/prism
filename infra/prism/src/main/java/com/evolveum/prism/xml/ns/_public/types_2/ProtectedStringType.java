//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.04 at 01:34:24 PM CET 
//


package com.evolveum.prism.xml.ns._public.types_2;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.exception.SystemException;


/**
 * 
 * 				Specific subtype for protected STRING data.
 * 			
 * 
 * <p>Java class for ProtectedStringType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProtectedStringType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://prism.evolveum.com/xml/ns/public/types-2}ProtectedDataType">
 *       &lt;sequence>
 *         &lt;element name="clearValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectedStringType")
public class ProtectedStringType extends ProtectedDataType<String> {

	public static final QName COMPLEX_TYPE = new QName("http://prism.evolveum.com/xml/ns/public/types-2", "ProtectedStringType");
	
	private static final String CHARSET = "UTF-8";
	
	public ProtectedStringType() {
		content = new ContentList();
	}
	
	@Override
	public byte[] getClearBytes() {
		String clearValue = getClearValue();
		try {
			// We want fixed charset here, independent of locale. We want consistent and portable encryption/decryption.
			return clearValue.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new SystemException("Unsupported charset '"+CHARSET+"', is this system from 19th century?", e);
		}
	}

	@Override
	public void setClearBytes(byte[] bytes) {
        setClearValue(bytesToString(bytes));
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

    @Override
    public ProtectedStringType clone() {
        ProtectedStringType cloned = new ProtectedStringType();
        cloneTo(cloned);
        return cloned;
    }

    public static String bytesToString(byte[] clearBytes) {
        try {
            // We want fixed charset here, independent of locale. We want consistent and portable encryption/decryption.
            return new String(clearBytes, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new SystemException("Unsupported charset '"+CHARSET+"', is this system from 19th century?", e);
        }
    }
}
