package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

import java.util.StringTokenizer;

public class DecomListContainerDetailFactory implements CombinedStructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setContainerRecordId(readString(stringTokenizer));
		decomListContainerDetail.setFull(Boolean.valueOf(readString(stringTokenizer)));
		try {
			decomListContainerDetail.setAvailableSize(Double.valueOf(readString(stringTokenizer)));
		} catch (Exception e) {
		}

		decomListContainerDetail.dirty = false;
		return decomListContainerDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		DecomListContainerDetail decomListContainerDetail = (DecomListContainerDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, "" + (decomListContainerDetail.getContainerRecordId() == null ?
										 NULL :
										 decomListContainerDetail.getContainerRecordId()));
		writeString(stringBuilder, "" + decomListContainerDetail.isFull() == null ?
								   String.valueOf(false) :
								   String.valueOf(decomListContainerDetail.isFull()));
		writeString(stringBuilder, "" + decomListContainerDetail.getAvailableSize() == null ?
								   NULL :
								   String.valueOf(decomListContainerDetail.getAvailableSize()));
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
}
