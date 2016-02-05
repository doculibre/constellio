package com.constellio.app.modules.es.connectors.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

public class LDAPObjectAttribute {
	private List<Object> value = new ArrayList<>();

	public LDAPObjectAttribute(Attribute attribute) {
		try {
			for (int i = 0; i < attribute.size(); i++) {
				Object current = attribute.get(i);
				value.add(current);
			}
		} catch (NamingException e) {
			//TODO
			throw new RuntimeException(e);
		}
	}

	public LDAPObjectAttribute() {
	}

	public List<Object> getValue() {
		return value;
	}

	public String getStringValue() {
		if (value == null || value.isEmpty()) {
			return null;
		} else {
			return (String) value.get(0);
		}
	}

	public byte[] getByteValue() {
		if (value == null || value.isEmpty()) {
			//TODO
			throw new RuntimeException("Invalid byte");
		} else {
			return (byte[]) value.get(0);
		}
	}

	public LDAPObjectAttribute setValue(Object value) {
		if (value == null) {
			this.value = new ArrayList<>();
		} else {
			if (value instanceof List) {
				this.value = (List<Object>) value;
			} else {
				this.value = new ArrayList<>();
				this.value.add(value);
			}
		}
		return this;
	}

}
