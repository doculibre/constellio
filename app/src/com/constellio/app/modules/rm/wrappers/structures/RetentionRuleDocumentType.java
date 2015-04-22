/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
