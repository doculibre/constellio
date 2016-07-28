package com.constellio.data.dao.services.sequence;

public interface SequencesManager {

	void set(String sequenceId, long value);

	long getLastSequenceValue(String sequenceId);

	long next(String sequenceId);
}
