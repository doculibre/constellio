package com.constellio.data.dao.dto.records;

import com.constellio.data.services.tenant.TenantLocal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Any id that is not a zero-padded integer id of 11 digits
 * A mapping table is used to convert this id to a negative integer value in both ways
 */
public class StringRecordId implements RecordId {

	/**
	 * Question : Why am I getting this ID?
	 * <p>
	 * Answer : That's a tough one!
	 * <p>
	 * Possible situations that can cause this issue :
	 * - The config file 'legacyConstellioIdsMapping.properties' has lost some mapping entries
	 * - The id was mapped by a StringRecordIdLegacyMemoryMapping because it was runned by a test with the toggle USE_MEMORY_STRING_ID_MAPPING enabled.
	 * - The id was mapped by a StringRecordIdLegacyPersistedMapping, then StringRecordIdLegacyMemoryMapping was used for mapping
	 * <p>
	 * Is it bad? Probably not
	 * <p>
	 * What should I do?
	 * 1. Make sure the toggle USE_MEMORY_STRING_ID_MAPPING is disabled
	 * 2. Reindex
	 */
	public static final String INVALID_ID = "MappingError_See_StringRecordId_INVALID_ID";

	private static final TenantLocal<StringRecordIdLegacyMapping> mapping = new TenantLocal<>();

	private int intValue;
	private String id;

	public static void setMapping(StringRecordIdLegacyMapping mapping) {
		StringRecordId.mapping.set(mapping);
	}

	public StringRecordId(String id) {
		this.id = id;
	}

	private boolean isUUID(String id) {
		return id != null && id.length() == 36 && id.charAt(8) == '-' && id.charAt(13) == '-' && id.charAt(18) == '-' && id.charAt(23) == '-';
	}

	public StringRecordId(int id) {
		this.id = mapping.get().getStringId(id);
		this.intValue = id;
	}

	@Override
	public String stringValue() {
		return id;
	}

	@Override
	public int intValue() {
		if (intValue == 0) {
			intValue = mapping.get().getIntId(id);
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
