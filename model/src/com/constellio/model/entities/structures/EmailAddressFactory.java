package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringTokenizer;

public class EmailAddressFactory implements CombinedStructureFactory {

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
