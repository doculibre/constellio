package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

import java.util.StringTokenizer;

public class DecomListFolderDetailFactory implements CombinedStructureFactory {
	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId(readString(stringTokenizer));
		String folderDetailStatus = readString(stringTokenizer);
		switch (folderDetailStatus) {
			case "true":
				decomListFolderDetail.setFolderDetailStatus(FolderDetailStatus.INCLUDED);
				break;
			case "false":
				decomListFolderDetail.setFolderDetailStatus(FolderDetailStatus.EXCLUDED);
				break;
			case "e":
			decomListFolderDetail.setFolderDetailStatus(FolderDetailStatus.EXCLUDED);
			break;
			case "i":
			decomListFolderDetail.setFolderDetailStatus(FolderDetailStatus.INCLUDED);
			break;
			case "s":
			decomListFolderDetail.setFolderDetailStatus(FolderDetailStatus.SELECTED);
			break;
		}

		// Skip deprecated fields validationUserId, validationUserName and ValidationDate
		readString(stringTokenizer);
		readString(stringTokenizer);
		readString(stringTokenizer);
		decomListFolderDetail.setContainerRecordId(readString(stringTokenizer));
		decomListFolderDetail.setReversedSort(Boolean.valueOf(readString(stringTokenizer)));
		decomListFolderDetail.setFolderLinearSize(readDouble(stringTokenizer));
		try {
			decomListFolderDetail.setIsPlacedInContainer(Boolean.valueOf(readString(stringTokenizer)));
		} catch (Exception e) {
			decomListFolderDetail.setIsPlacedInContainer(false);
		}

		decomListFolderDetail.dirty = false;
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		DecomListFolderDetail decomListFolderDetail = (DecomListFolderDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, decomListFolderDetail.getFolderId());
		writeString(stringBuilder, decomListFolderDetail.getFolderDetailStatus().getDescription());
		// Skip deprecated fields validationUserId, validationUserName and ValidationDate
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, decomListFolderDetail.getContainerRecordId());
		writeString(stringBuilder, String.valueOf(decomListFolderDetail.isReversedSort()));
		writeDouble(stringBuilder, decomListFolderDetail.getFolderLinearSize());
		writeString(stringBuilder, "" + decomListFolderDetail.isPlacedInContainer() == null ?
								   String.valueOf(false) :
								   String.valueOf(decomListFolderDetail.isPlacedInContainer()));
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
