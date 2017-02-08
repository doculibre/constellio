package com.constellio.model.services.taxonomies;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;

public class FastContinueInfos {

	boolean finishedConceptsIteration;

	int lastReturnRecordIndex;

	List<String> shownRecordsWithVisibleChildren;

	public FastContinueInfos(boolean finishedConceptsIteration, int lastReturnRecordIndex,
			List<String> shownRecordsWithVisibleChildren) {
		this.finishedConceptsIteration = finishedConceptsIteration;
		this.lastReturnRecordIndex = lastReturnRecordIndex;
		this.shownRecordsWithVisibleChildren = shownRecordsWithVisibleChildren;
	}

	public boolean isFinishedConceptsIteration() {
		return finishedConceptsIteration;
	}

	public int getLastReturnRecordIndex() {
		return lastReturnRecordIndex;
	}

	public List<String> getShownRecordsWithVisibleChildren() {
		return shownRecordsWithVisibleChildren;
	}
}
