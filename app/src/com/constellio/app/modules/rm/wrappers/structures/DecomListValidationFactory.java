package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.joda.time.LocalDate;

import java.util.StringTokenizer;

public class DecomListValidationFactory implements CombinedStructureFactory {
	private static final String NULL = "~null~";
	private static final String ELEMENT_SEPARATOR = ":";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");
		DecomListValidation decomListFolderDetail = new DecomListValidation();
		decomListFolderDetail.userId = readString(stringTokenizer);
		decomListFolderDetail.requestDate = readLocalDate(stringTokenizer);
		decomListFolderDetail.validationDate = readLocalDate(stringTokenizer);
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		DecomListValidation validation = (DecomListValidation) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, validation.getUserId());
		writeLocalDate(stringBuilder, validation.getRequestDate());
		writeLocalDate(stringBuilder, validation.getValidationDate());
		return stringBuilder.toString();
	}

	private LocalDate readLocalDate(StringTokenizer stringTokenizer) {
		String localDate = readString(stringTokenizer);
		return localDate == null ? null : LocalDate.parse(localDate);
	}

	private String readString(StringTokenizer stringTokenizer) {
		if (!stringTokenizer.hasMoreElements()) {
			return null;
		}
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ELEMENT_SEPARATOR);
		}
	}

	private void writeLocalDate(StringBuilder stringBuilder, LocalDate value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(ELEMENT_SEPARATOR);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.toString("yyyy-MM-dd"));
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(ELEMENT_SEPARATOR);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(ELEMENT_SEPARATOR, "~~~"));
		}
	}
}
