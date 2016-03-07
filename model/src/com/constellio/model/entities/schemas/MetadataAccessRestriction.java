package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.List;

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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MetadataAccessRestriction that = (MetadataAccessRestriction) o;

		if (requiredReadRoles != null ? !requiredReadRoles.equals(that.requiredReadRoles) : that.requiredReadRoles != null)
			return false;
		if (requiredWriteRoles != null ? !requiredWriteRoles.equals(that.requiredWriteRoles) : that.requiredWriteRoles != null)
			return false;
		if (requiredModificationRoles != null ?
				!requiredModificationRoles.equals(that.requiredModificationRoles) :
				that.requiredModificationRoles != null)
			return false;
		return requiredDeleteRoles != null ?
				requiredDeleteRoles.equals(that.requiredDeleteRoles) :
				that.requiredDeleteRoles == null;

	}

	@Override
	public int hashCode() {
		int result = requiredReadRoles != null ? requiredReadRoles.hashCode() : 0;
		result = 31 * result + (requiredWriteRoles != null ? requiredWriteRoles.hashCode() : 0);
		result = 31 * result + (requiredModificationRoles != null ? requiredModificationRoles.hashCode() : 0);
		result = 31 * result + (requiredDeleteRoles != null ? requiredDeleteRoles.hashCode() : 0);
		return result;
	}
}
