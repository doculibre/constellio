package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.StringTokenizer;

import static com.constellio.model.utils.EnumWithSmallCodeUtils.toEnumWithSmallCode;

public class PendingAlertFactory implements CombinedStructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		PendingAlert pendingAlert = new PendingAlert();
		pendingAlert.setOn(readLocalDateTime(stringTokenizer));
		//		pendingAlert.setAlertCode(AlertCode.valueOf(readString(stringTokenizer)));
		pendingAlert.setAlertCode((AlertCode) toEnumWithSmallCode(AlertCode.class, readString(stringTokenizer)));
		pendingAlert.setReminderCount(readDouble(stringTokenizer));
		pendingAlert.dirty = false;
		return pendingAlert;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		PendingAlert pendingAlert = (PendingAlert) structure;
		StringBuilder stringBuilder = new StringBuilder();
		if (pendingAlert.getOn() != null) {
			writeString(stringBuilder, pendingAlert.getOn().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
		writeString(stringBuilder, pendingAlert.getAlertCode() == null ?
								   NULL :
								   pendingAlert.getAlertCode().getCode());
		writeString(stringBuilder, pendingAlert.getReminderCount() == null ?
								   NULL :
								   pendingAlert.getReminderCount().toString());

		return stringBuilder.toString();
	}

	private Double readDouble(StringTokenizer stringTokenizer) {
		String doubleAsString = readString(stringTokenizer);
		if (StringUtils.isBlank(doubleAsString) || doubleAsString.contains(NULL)) {
			return null;
		} else {
			return Double.valueOf(doubleAsString);
		}
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

	private LocalDateTime readLocalDateTime(StringTokenizer stringTokenizer) {
		String localDateTime = readString(stringTokenizer);
		return localDateTime == null ? null : LocalDateTime.parse(localDateTime);
	}

}
