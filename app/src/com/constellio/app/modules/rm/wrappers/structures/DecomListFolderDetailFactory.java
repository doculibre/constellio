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

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class DecomListFolderDetailFactory implements StructureFactory {
	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId(readString(stringTokenizer));
		decomListFolderDetail.folderExcluded = Boolean.valueOf(readString(stringTokenizer));
		// Skip deprecated fields validationUserId, validationUserName and ValidationDate
		readString(stringTokenizer);
		readString(stringTokenizer);
		readString(stringTokenizer);
		decomListFolderDetail.setContainerRecordId(readString(stringTokenizer));
		decomListFolderDetail.setReversedSort(Boolean.valueOf(readString(stringTokenizer)));
		decomListFolderDetail.setFolderLinearSize(readDouble(stringTokenizer));
		decomListFolderDetail.dirty = false;
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		DecomListFolderDetail decomListFolderDetail = (DecomListFolderDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, decomListFolderDetail.getFolderId());
		writeString(stringBuilder, String.valueOf(decomListFolderDetail.isFolderExcluded()));
		// Skip deprecated fields validationUserId, validationUserName and ValidationDate
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, decomListFolderDetail.getContainerRecordId());
		writeString(stringBuilder, String.valueOf(decomListFolderDetail.isReversedSort()));
		writeDouble(stringBuilder, decomListFolderDetail.getFolderLinearSize());
		return stringBuilder.toString();
	}

	private Double readDouble(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		return value == null ? null : Double.valueOf(value);
	}

	private String readString(StringTokenizer stringTokenizer) {
		if (!stringTokenizer.hasMoreElements()) {
			return null;
		}
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private void writeDouble(StringBuilder stringBuilder, Double value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(String.valueOf(value));
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
}
