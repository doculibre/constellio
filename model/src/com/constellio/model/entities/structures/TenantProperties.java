package com.constellio.model.entities.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class TenantProperties {

	protected String name;
	protected String code;
	protected byte id;
	protected List<String> hostnames;

	public TenantProperties() {

	}

	public TenantProperties(String name, String code, int id, List<String> hostnames) {
		this(name, code, (byte) id, hostnames);
	}

	public TenantProperties(String name, String code, byte id, List<String> hostnames) {
		this.name = name;
		this.code = code;
		this.id = id;
		this.hostnames = hostnames;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String value) {
		code = value;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte value) {
		id = value;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> value) {
		if (value == null || value.isEmpty()) {
			hostnames = null;
		} else {
			hostnames = value;
		}
	}

	@Override
	public String toString() {
		return "TenantProperties{" +
			   "name='" + name + '\'' +
			   ", code='" + code + '\'' +
			   ", id='" + id + '\'' +
			   ", hostnames='" + hostnames +
			   '}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
