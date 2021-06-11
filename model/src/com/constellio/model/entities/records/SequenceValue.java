package com.constellio.model.entities.records;

import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory.MapBasedStructure;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SequenceValue extends MapBasedStructure {

	public static final String SEQUENCE_TABLE = "seqTable";
	public static final String SEQUENCE_VALUE = "seqValue";
	public static final String VALUE = "value";


	public SequenceValue(String sequenceTable, Integer sequenceValue, String displayValue) {
		setSequenceTable(sequenceTable);
		setSequenceValue(sequenceValue);
		setDisplayValue(displayValue);
		dirty = false;
	}

	public SequenceValue setSequenceTable(String sequenceTable) {
		set(SEQUENCE_TABLE, sequenceTable);
		return this;
	}

	public SequenceValue setSequenceValue(int sequenceValue) {
		set(SEQUENCE_VALUE, "" + sequenceValue);
		return this;
	}

	public SequenceValue setDisplayValue(String displayValue) {
		set(VALUE, displayValue);
		return this;
	}

	public String getSequenceTable() {
		return get(SEQUENCE_TABLE);
	}

	public Integer getSequenceValue() {
		String seqValueStr = get(SEQUENCE_VALUE);
		return seqValueStr == null ? null : Integer.valueOf(seqValueStr);
	}

	public String getDisplayValue() {
		return get(VALUE);
	}

}
