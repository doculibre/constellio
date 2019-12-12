package com.constellio.model.services.records;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StringRecordId implements RecordId {

	private static Map<Integer, String> isMapping = new HashMap<>();
	private static Map<String, Integer> siMapping = new HashMap<>();

	private int intValue;
	private String id;

	public StringRecordId(String id) {
		this.id = id;

		if (!isUUID(id)) {

			this.intValue = Math.abs(id.hashCode()) * -1;
			//The first 100 ids are reserved to handle eventual conflicts
			if (intValue > -100) {
				//Handling the zero hashcode
				intValue -= 101;
			}
			String currentStrValue = isMapping.get(intValue);
			if (currentStrValue == null) {
				synchronized (StringRecordId.class) {
					isMapping.put(intValue, id);
					siMapping.put(id, intValue);
				}
			} else if (!id.equals(currentStrValue)) {
				throw new IllegalArgumentException("Id '" + id + "' has same hashcode value than id '" + currentStrValue + "' : " + intValue);
			}
		}
	}

	private boolean isUUID(String id) {
		return id != null && id.length() == 36 && id.charAt(8) == '-' && id.charAt(13) == '-' && id.charAt(18) == '-' && id.charAt(23) == '-';
	}

	public StringRecordId(int id) {
		this.id = isMapping.get(id);
		this.intValue = id;
	}

	@Override
	public String stringValue() {
		return id;
	}

	@Override
	public int intValue() {
		if (intValue == 0) {
			throw new IllegalStateException("UUIDs do not have an integer value");
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
		return Objects.equals(intValue, that.intValue);
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

	public static synchronized Map<String, Integer> getAnomaliesMap() {
		return new HashMap<>(siMapping);
	}
}
