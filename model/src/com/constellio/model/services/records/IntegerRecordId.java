package com.constellio.model.services.records;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IntegerRecordId implements RecordId {

	private int id;

	public IntegerRecordId(int id) {
		this.id = id;
	}

	@Override
	public String stringValue() {
		return RecordUtils.toStringId(id);
	}

	@Override
	public int intValue() {
		return id;
	}

	@Override
	public boolean isInteger() {
		return true;
	}

	@Override
	public boolean lesserThan(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return id < ((IntegerRecordId) anotherRecordId).id;
		} else {
			return true;
		}
	}

	@Override
	public boolean lesserOrEqual(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return id <= ((IntegerRecordId) anotherRecordId).id;
		} else {
			return true;
		}
	}

	@Override
	public boolean greaterThan(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return id > ((IntegerRecordId) anotherRecordId).id;
		} else {
			return false;
		}
	}

	@Override
	public boolean greaterOrEqual(RecordId anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return id >= ((IntegerRecordId) anotherRecordId).id;
		} else {
			return false;
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
		IntegerRecordId that = (IntegerRecordId) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return stringValue();
	}

	@Override
	public int compareTo(@NotNull Object anotherRecordId) {
		if (anotherRecordId instanceof IntegerRecordId) {
			return id - ((IntegerRecordId) anotherRecordId).id;
		} else {
			return -1;
		}
	}
}
