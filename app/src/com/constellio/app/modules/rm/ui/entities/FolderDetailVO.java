package com.constellio.app.modules.rm.ui.entities;

import java.io.Serializable;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;

public class FolderDetailVO implements Serializable {
	private String folderId;
	private String folderLegacyId;
	private boolean folderIncluded;
	private String containerRecordId;
	private FolderMediaType mediumType;
	private String retentionRuleId;
	private String categoryCode;
	private boolean packageable;
	private boolean sortable;
	private boolean reversedSort;
	private boolean selected;
	private Double linearSize;

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getFolderLegacyId() {
		return folderLegacyId;
	}

	public void setFolderLegacyId(String folderLegacyId) {
		this.folderLegacyId = folderLegacyId;
	}

	public boolean isFolderIncluded() {
		return folderIncluded;
	}

	public void setFolderIncluded(boolean folderIncluded) {
		this.folderIncluded = folderIncluded;
	}

	public FolderMediaType getMediumType() {
		return mediumType;
	}

	public void setMediumType(FolderMediaType mediumType) {
		this.mediumType = mediumType;
	}

	public String getRetentionRuleId() {
		return retentionRuleId;
	}

	public void setRetentionRuleId(String retentionRuleId) {
		this.retentionRuleId = retentionRuleId;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getContainerRecordId() {
		return containerRecordId;
	}

	public void setContainerRecordId(String containerRecordId) {
		this.containerRecordId = containerRecordId;
	}

	public boolean isPackageable() {
		return packageable;
	}

	public void setPackageable(boolean packageable) {
		this.packageable = packageable;
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public boolean isReversedSort() {
		return reversedSort;
	}

	public void setReversedSort(boolean reversedSort) {
		this.reversedSort = reversedSort;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Double getLinearSize() {
		return linearSize;
	}

	public void setLinearSize(Double linearSize) {
		this.linearSize = linearSize;
	}
}
