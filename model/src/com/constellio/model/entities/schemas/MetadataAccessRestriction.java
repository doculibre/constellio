package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MetadataAccessRestriction {

	final List<String> requiredReadRoles;

	final List<String> requiredWriteRoles;

	final List<String> requiredModificationRoles;

	final List<String> requiredDeleteRoles;

	public MetadataAccessRestriction() {
		this.requiredReadRoles = new ArrayList<>();
		this.requiredWriteRoles = new ArrayList<>();
		this.requiredModificationRoles = new ArrayList<>();
		this.requiredDeleteRoles = new ArrayList<>();
	}

	public MetadataAccessRestriction(List<String> requiredReadRoles, List<String> requiredWriteRoles,
			List<String> requiredModificationRoles, List<String> requiredDeleteRoles) {
		this.requiredReadRoles = requiredReadRoles;
		this.requiredWriteRoles = requiredWriteRoles;
		this.requiredModificationRoles = requiredModificationRoles;
		this.requiredDeleteRoles = requiredDeleteRoles;
	}

	public List<String> getRequiredReadRoles() {
		return requiredReadRoles;
	}

	public List<String> getRequiredWriteRoles() {
		return requiredWriteRoles;
	}

	public List<String> getRequiredModificationRoles() {
		return requiredModificationRoles;
	}

	public List<String> getRequiredDeleteRoles() {
		return requiredDeleteRoles;
	}

	public boolean isEmpty() {
		return requiredReadRoles.isEmpty()
				&& requiredWriteRoles.isEmpty()
				&& requiredModificationRoles.isEmpty()
				&& requiredDeleteRoles.isEmpty();
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
