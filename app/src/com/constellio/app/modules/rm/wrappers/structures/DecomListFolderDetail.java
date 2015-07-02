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
import org.joda.time.LocalDate;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class DecomListFolderDetail implements ModifiableStructure {
	String folderId;
	boolean folderIncluded;
	String validationUserId;
	String validationUsername;
	LocalDate validationDate;
	String containerRecordId;
	boolean reversedSort;
	boolean dirty;
	Double folderLinearSize;

	public DecomListFolderDetail() {
	}

	public DecomListFolderDetail(String folderId) {
		this.folderId = folderId;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		dirty = true;
		this.folderId = folderId;
	}

	public boolean isFolderIncluded() {
		return folderIncluded;
	}

	public void setFolderIncluded(boolean folderIncluded) {
		dirty = true;
		this.folderIncluded = folderIncluded;
	}

	public String getValidationUserId() {
		return validationUserId;
	}

	public String getValidationUsername() {
		return validationUsername;
	}

	public void setValidationUser(User user) {
		dirty = true;
		if (user != null) {
			validationUserId = user.getId();
			validationUsername = user.getUsername();
		}
	}

	public LocalDate getValidationDate() {
		return validationDate;
	}

	public void setValidationDate(LocalDate validationDate) {
		dirty = true;
		this.validationDate = validationDate;

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

	public void setReversedSort(boolean reversedSort) {
		dirty = true;
		this.reversedSort = reversedSort;
	}

	public Double getFolderLinearSize() {
		return folderLinearSize;
	}

	public void setFolderLinearSize(Double folderLinearSize) {
		dirty = true;
		this.folderLinearSize = folderLinearSize;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "DecommissioningListFolderDetail{" +
				"folderId='" + folderId + '\'' +
				", folderIncluded=" + folderIncluded +
				", validationUser='" + validationUserId + validationUsername + '\'' +
				", validationDate=" + validationDate +
				", containerRecordId='" + containerRecordId + '\'' +
				", reversedSort=" + reversedSort +
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
