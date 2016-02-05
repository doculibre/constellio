package com.constellio.app.modules.rm.wrappers.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class RetentionRuleDocumentType implements ModifiableStructure {

	private boolean dirty = false;

	String documentTypeId;

	DisposalType disposalType;

	public RetentionRuleDocumentType() {
	}

	public RetentionRuleDocumentType(String documentTypeId) {
		this.documentTypeId = documentTypeId;
	}

	public RetentionRuleDocumentType(String documentTypeId, DisposalType disposalType) {
		this.documentTypeId = documentTypeId;
		this.disposalType = disposalType;
	}

	public String getDocumentTypeId() {
		return documentTypeId;
	}

	public void setDocumentTypeId(String documentTypeId) {
		if (!LangUtils.isEqual(this.documentTypeId, documentTypeId)) {
			this.dirty = true;
			this.documentTypeId = documentTypeId;
		}
	}

	public DisposalType getDisposalType() {
		return disposalType;
	}

	public void setDisposalType(DisposalType disposalType) {
		if (!LangUtils.isEqual(this.disposalType, disposalType)) {
			this.dirty = true;
			this.disposalType = disposalType;
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		if (disposalType == null) {
			return documentTypeId;
		} else {
			return documentTypeId + ":" + disposalType;
		}
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
