package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DecomListFolderDetail implements ModifiableStructure {
	String folderId;
	FolderDetailStatus folderDetailStatus;
	String containerRecordId;
	int sortIndex;
	boolean reversedSort;
	Double folderLinearSize;
	boolean isPlacedInContainer;
	boolean dirty;

	@Deprecated
	public DecomListFolderDetail() {
		isPlacedInContainer = false;
	}

	public DecomListFolderDetail(Folder folder, FolderDetailStatus folderDetailStatus) {
		this.folderId = folder.getId();
		this.containerRecordId = folder.getContainer();
		this.folderLinearSize = folder.getLinearSize();
		this.isPlacedInContainer = false;
		this.folderDetailStatus = folderDetailStatus;
	}

	public String getFolderId() {
		return folderId;
	}

	public DecomListFolderDetail setFolderId(String folderId) {
		dirty = true;
		this.folderId = folderId;
		return this;
	}

	public FolderDetailStatus getFolderDetailStatus() {
		return folderDetailStatus;
	}

	public DecomListFolderDetail setFolderDetailStatus(FolderDetailStatus folderDetailStatus) {
		this.folderDetailStatus = folderDetailStatus;
		return this;
	}

	public DecomListFolderDetail setFolderExcluded(boolean folderExcluded) {
		dirty = true;
		if (folderExcluded) {
			this.folderDetailStatus = FolderDetailStatus.EXCLUDED;
		} else {
			this.folderDetailStatus = FolderDetailStatus.INCLUDED;
		}
		return this;
	}

	public String getContainerRecordId() {
		return containerRecordId;
	}

	public DecomListFolderDetail setContainerRecordId(String containerRecordId) {
		dirty = true;
		this.containerRecordId = containerRecordId;
		return this;
	}

	public boolean isReversedSort() {
		return reversedSort;
	}

	public DecomListFolderDetail setReversedSort(boolean reversedSort) {
		dirty = true;
		this.reversedSort = reversedSort;
		return this;
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public DecomListFolderDetail setSortIndex(int sortIndex) {
		dirty = true;
		this.sortIndex = sortIndex;
		return this;
	}

	public Double getFolderLinearSize() {
		return folderLinearSize;
	}

	public DecomListFolderDetail setFolderLinearSize(Double folderLinearSize) {
		dirty = true;
		this.folderLinearSize = folderLinearSize;
		return this;
	}

	public boolean isPlacedInContainer() {
		return isPlacedInContainer;
	}

	public DecomListFolderDetail setIsPlacedInContainer(boolean isPlacedInContainer) {
		dirty = true;
		this.isPlacedInContainer = isPlacedInContainer;
		return this;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "DecommissioningListFolderDetail{" +
			   "folderId='" + folderId + '\'' +
			   ", folderStatus" + folderDetailStatus.getDescription() +
			   ", containerRecordId='" + containerRecordId + '\'' +
			   ", reversedSort=" + reversedSort +
			   ", sortIndex=" + sortIndex +
			   ", dirty=" + dirty +
			   ", isPlacedInContainer=" + isPlacedInContainer +
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
