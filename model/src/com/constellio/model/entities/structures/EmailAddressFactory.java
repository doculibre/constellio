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
package com.constellio.model.entities.structures;

import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class EmailAddressFactory implements StructureFactory {

	private static final String NULL = "~null~";
	private static final String DELIMITER = ":";
	private static final String START_EMAIL = "<";
	private static final String END_EMAIL = ">";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, DELIMITER);

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.name = readString(stringTokenizer);
		emailAddress.email = readString(stringTokenizer);
		emailAddress.dirty = false;
		return emailAddress;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		EmailAddress emailAddress = (EmailAddress) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, emailAddress.getName() == null ?
				NULL :
				emailAddress.getName());
		writeString(stringBuilder, emailAddress.getEmail() == null ?
				NULL :
				START_EMAIL + emailAddress.getEmail() + END_EMAIL);
		return stringBuilder.toString();
	}

	public String toAddress(ModifiableStructure structure) {
		return toString(structure).replace(DELIMITER, " ");
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			value = value.replace("~~~", DELIMITER);
			value = value.replace(START_EMAIL, "");
			value = value.replace(END_EMAIL, "");
			return value;
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(DELIMITER);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			value = value.replace(DELIMITER, "~~~");
			stringBuilder.append(value);

		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
