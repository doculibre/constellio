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

import static com.constellio.model.utils.EnumWithSmallCodeUtils.toEnumWithSmallCode;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class PendingAlertFactory implements StructureFactory {

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
