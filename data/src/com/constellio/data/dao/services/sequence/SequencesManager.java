package com.constellio.data.dao.services.sequence;

import java.util.Map;

public interface SequencesManager {

	void set(String sequenceId, long value);

	long getLastSequenceValue(String sequenceId);

	long next(String sequenceId);

	Map<String, Long> getSequences();

}
