package com.constellio.model.services.taxonomies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FastContinueInfos implements Serializable {

	boolean finishedConceptsIteration;

	int lastReturnRecordIndex;

	List<String> shownRecordsWithVisibleChildren;

	public FastContinueInfos(boolean finishedConceptsIteration, int lastReturnRecordIndex,
							 List<String> shownRecordsWithVisibleChildren) {

		//TODO : Remove the shownRecordsWithVisibleChildren parameter

		this.finishedConceptsIteration = finishedConceptsIteration;
		//		this.lastReturnRecordIndex = lastReturnRecordIndex;
		//		this.shownRecordsWithVisibleChildren = shownRecordsWithVisibleChildren;

		this.lastReturnRecordIndex = lastReturnRecordIndex + shownRecordsWithVisibleChildren.size();
		this.shownRecordsWithVisibleChildren = new ArrayList<>();
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
