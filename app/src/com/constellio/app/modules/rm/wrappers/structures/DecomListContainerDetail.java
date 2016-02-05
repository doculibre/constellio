package com.constellio.app.modules.rm.wrappers.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class DecomListContainerDetail implements ModifiableStructure {

	String containerRecordId;
	boolean full;
	boolean dirty;

	public DecomListContainerDetail() {
	}

	public DecomListContainerDetail(String containerRecordId) {
		this.containerRecordId = containerRecordId;
	}

	public String getContainerRecordId() {
		return containerRecordId;
	}

	public DecomListContainerDetail setContainerRecordId(String containerRecordId) {
		dirty = true;
		this.containerRecordId = containerRecordId;
		return this;
	}

	public boolean isFull() {
		return full;
	}

	public DecomListContainerDetail setFull(boolean full) {
		dirty = true;
		this.full = full;
		return this;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "DecommissioningListContainerDetail{" +
				"containerRecordId='" + containerRecordId + '\'' +
				", full=" + full +
				", dirty=" + dirty +
				'}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}
}
