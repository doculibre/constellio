package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DecomListContainerDetail implements ModifiableStructure {

	String containerRecordId;
	Double availableSize;
	boolean full;
	boolean dirty;

	public DecomListContainerDetail() {
	}

	public DecomListContainerDetail(String containerRecordId) {
		this.containerRecordId = containerRecordId;
	}

	public DecomListContainerDetail(ContainerRecord container) {
		this.containerRecordId = container.getId();
		this.availableSize = container.getAvailableSize();
		this.full = Boolean.TRUE.equals(container.isFull());
		this.dirty = false;
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

	public DecomListContainerDetail setFull(Boolean full) {
		dirty = true;
		this.full = Boolean.TRUE.equals(full);
		return this;
	}

	public Double getAvailableSize() {
		return availableSize;
	}

	public DecomListContainerDetail setAvailableSize(Double availableSize) {
		dirty = true;
		this.availableSize = availableSize;
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
