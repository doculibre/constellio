package com.constellio.model.services.records;

import com.constellio.data.utils.LangUtils;

import java.io.Serializable;

/**
 * The Main advantage of this interface is to delay as longer as possible the transformation of integer ids to String,
 * which are then less efficient for comparison
 */
public interface RecordId extends Comparable, Serializable {

	String stringValue();

	int intValue();

	boolean isInteger();

	boolean lesserThan(RecordId anotherRecordId);

	boolean lesserOrEqual(RecordId anotherRecordId);

	boolean greaterThan(RecordId anotherRecordId);

	boolean greaterOrEqual(RecordId anotherRecordId);

	static RecordId toId(String stringValue) {
		if (stringValue.length() == 11) {
			long intValue = LangUtils.tryParseLong(stringValue, -1);
			return intValue == -1 ? new StringRecordId(stringValue) : new IntegerRecordId((int) intValue);
		} else {
			return new StringRecordId(stringValue);
		}
	}

	static RecordId toId(int intValue) {
		return new IntegerRecordId(intValue);
	}
}
