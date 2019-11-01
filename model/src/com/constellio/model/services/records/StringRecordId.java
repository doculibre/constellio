package com.constellio.model.services.records;

import com.constellio.data.utils.ImpossibleRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StringRecordId implements RecordId {

	private String id;

	public StringRecordId(String id) {
		this.id = id;
	}

	@Override
	public String stringValue() {
		return id;
	}

	@Override
	public int intValue() {
		throw new ImpossibleRuntimeException("This id is not an integer : " + id);
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
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
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
