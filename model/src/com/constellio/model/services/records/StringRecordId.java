package com.constellio.model.services.records;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StringRecordId implements RecordId {

	private static StringRecordIdLegacyMapping mapping;

	private int intValue;
	private String id;

	public static void setMapping(StringRecordIdLegacyMapping mapping) {
		StringRecordId.mapping = mapping;
	}

	public StringRecordId(String id) {
		this.id = id;
	}

	private boolean isUUID(String id) {
		return id != null && id.length() == 36 && id.charAt(8) == '-' && id.charAt(13) == '-' && id.charAt(18) == '-' && id.charAt(23) == '-';
	}

	public StringRecordId(int id) {
		this.id = mapping.getStringId(id);
		this.intValue = id;
	}

	@Override
	public String stringValue() {
		return id;
	}

	@Override
	public int intValue() {
		if (intValue == 0) {
			intValue = mapping.getIntId(id);
		}
		return intValue;
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public boolean lesserThan(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return false;
		} else {
			return stringValue().compareTo(anotherRecordId.stringValue()) < 0;
		}
	}

	@Override
	public boolean lesserOrEqual(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return false;
		} else {
			return stringValue().compareTo(anotherRecordId.stringValue()) <= 0;
		}
	}

	@Override
	public boolean greaterThan(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return true;
		} else {
			return stringValue().compareTo(anotherRecordId.stringValue()) > 0;
		}
	}

	@Override
	public boolean greaterOrEqual(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return true;
		} else {
			return stringValue().compareTo(anotherRecordId.stringValue()) >= 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StringRecordId that = (StringRecordId) o;
		if (intValue != 0 && that.intValue != 0) {
			return Objects.equals(intValue, that.intValue);
		} else {
			return Objects.equals(id, that.id);
		}
	}

	@Override
	public int hashCode() {
		return intValue;
	}

	@Override
	public int compareTo(@NotNull Object anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return 1;
		} else if (anotherRecordId instanceof StringRecordId) {
			return stringValue().compareTo(((StringRecordId) anotherRecordId).stringValue());

		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return id;
	}

}
