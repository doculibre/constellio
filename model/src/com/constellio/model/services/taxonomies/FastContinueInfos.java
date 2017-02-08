package com.constellio.model.services.taxonomies;

import java.util.List;

import com.constellio.model.entities.records.Record;

public class FastContinueInfos {

	boolean finishedConceptsIteration;

	int lastReturnRecordIndex;

	List<Record> notYetShownRecordsWithVisibleChildren;

	public FastContinueInfos(boolean finishedConceptsIteration, int lastReturnRecordIndex,
			List<Record> notYetShownRecordsWithVisibleChildren) {
		this.finishedConceptsIteration = finishedConceptsIteration;
		this.lastReturnRecordIndex = lastReturnRecordIndex;
		this.notYetShownRecordsWithVisibleChildren = notYetShownRecordsWithVisibleChildren;
	}

	public boolean isFinishedConceptsIteration() {
		return finishedConceptsIteration;
	}

	public int getLastReturnRecordIndex() {
		return lastReturnRecordIndex;
	}

	public List<Record> getNotYetShownRecordsWithVisibleChildren() {
		return notYetShownRecordsWithVisibleChildren;
	}
}
