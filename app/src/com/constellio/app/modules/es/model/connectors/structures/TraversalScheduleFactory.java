package com.constellio.app.modules.es.model.connectors.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

import java.util.StringTokenizer;

public class TraversalScheduleFactory implements CombinedStructureFactory {
	private static final String NULL = "~null~";
	private static final String ELEMENT_SEPARATOR = ":";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");
		TraversalSchedule decomListFolderDetail = new TraversalSchedule();
		decomListFolderDetail.weekDay = readInt(stringTokenizer);
		decomListFolderDetail.startTime = readString(stringTokenizer);
		decomListFolderDetail.endTime = readString(stringTokenizer);
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		TraversalSchedule validation = (TraversalSchedule) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeInt(stringBuilder, validation.getWeekDay());
		writeString(stringBuilder, validation.getStartTime());
		writeString(stringBuilder, validation.getEndTime());
		return stringBuilder.toString();
	}

	private int readInt(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		return value == null ? null : Integer.parseInt(value);
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

	private void writeInt(StringBuilder stringBuilder, int value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(ELEMENT_SEPARATOR);
		}
		stringBuilder.append(value);
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
