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
