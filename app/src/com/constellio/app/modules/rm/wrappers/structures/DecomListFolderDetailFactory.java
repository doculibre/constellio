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

import java.util.StringTokenizer;

import org.joda.time.LocalDate;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class DecomListFolderDetailFactory implements StructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId(readString(stringTokenizer));
		decomListFolderDetail.setFolderIncluded(Boolean.valueOf(readString(stringTokenizer)));
		decomListFolderDetail.validationUserId = readString(stringTokenizer);
		decomListFolderDetail.validationUsername = readString(stringTokenizer);
		decomListFolderDetail.setValidationDate(readLocalDate(stringTokenizer));
		decomListFolderDetail.setContainerRecordId(readString(stringTokenizer));
		decomListFolderDetail.setReversedSort(Boolean.valueOf(readString(stringTokenizer)));
		decomListFolderDetail.dirty = false;
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		DecomListFolderDetail decomListFolderDetail = (DecomListFolderDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, "" + (decomListFolderDetail.getFolderId() == null ?
				NULL :
				decomListFolderDetail.getFolderId()));
		writeString(stringBuilder, "" + decomListFolderDetail.isFolderIncluded() == null ?
				String.valueOf(false) :
				String.valueOf(decomListFolderDetail.isFolderIncluded()));
		writeString(stringBuilder, "" + decomListFolderDetail.getValidationUserId() == null ?
				NULL :
				decomListFolderDetail.getValidationUserId());
		writeString(stringBuilder, "" + decomListFolderDetail.getValidationUsername() == null ?
				"" :
				decomListFolderDetail.getValidationUsername());
		if (decomListFolderDetail.getValidationDate() != null) {
			writeString(stringBuilder, decomListFolderDetail.getValidationDate().toString("yyyy-MM-dd"));
		} else {
			writeString(stringBuilder, NULL);
		}
		writeString(stringBuilder, "" + decomListFolderDetail.getContainerRecordId() == null ?
				NULL :
				decomListFolderDetail.getContainerRecordId());
		writeString(stringBuilder, "" + decomListFolderDetail.isReversedSort() == null ?
				String.valueOf(false) :
				String.valueOf(decomListFolderDetail.isReversedSort()));

		return stringBuilder.toString();
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}

	private LocalDate readLocalDate(StringTokenizer stringTokenizer) {
		String localDate = readString(stringTokenizer);
		return localDate == null ? null : LocalDate.parse(localDate);
	}

}
