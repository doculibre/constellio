package com.constellio.app.ui.entities;

public class FolderUnicityVO {
	String prefix;
	MetadataVO metadataVO;
	boolean isAlwaysShown;
	Integer referenceMetadataDisplay;

	public Integer getReferenceMetadataDisplay() {
		return referenceMetadataDisplay;
	}

	public void setReferenceMetadataDisplay(Integer referenceMetadataDisplay) {
		this.referenceMetadataDisplay = referenceMetadataDisplay;
	}

	public String getPrefix() {
		return prefix;
	}

	public FolderUnicityVO setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}

	public FolderUnicityVO setMetadataVO(MetadataVO metadataVO) {
		this.metadataVO = metadataVO;
		return this;
	}

	public boolean isAlwaysShown() {
		return isAlwaysShown;
	}

	public FolderUnicityVO setAlwaysShown(boolean alwaysShown) {
		isAlwaysShown = alwaysShown;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FolderUnicityVO that = (FolderUnicityVO) o;

		if (isAlwaysShown != that.isAlwaysShown) {
			return false;
		}
		if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
			return false;
		}
		return metadataVO != null ? metadataVO.equals(that.metadataVO) : that.metadataVO == null;
	}

	@Override
	public int hashCode() {
		int result = prefix != null ? prefix.hashCode() : 0;
		result = 31 * result + (metadataVO != null ? metadataVO.hashCode() : 0);
		result = 31 * result + (isAlwaysShown ? 1 : 0);
		return result;
	}
}
