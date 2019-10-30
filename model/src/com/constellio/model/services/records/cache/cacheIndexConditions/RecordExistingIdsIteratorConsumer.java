package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.model.services.records.RecordId;

import java.util.Iterator;

public class RecordExistingIdsIteratorConsumer {

	private Iterator<RecordId> recordIdIterator;

	private RecordId temp;

	public RecordExistingIdsIteratorConsumer(Iterator<RecordId> recordIdIterator) {
		this.recordIdIterator = recordIdIterator;
	}

	public boolean exists(RecordId recordId) {
		if (temp != null) {
			if (recordId.lesserThan(temp)) {
				return false;
			} else if (recordId.equals(temp)) {

				temp = null;
				return true;

			} else {
				temp = null;
				//continuing iterating
			}

		}

		while (recordIdIterator.hasNext()) {
			RecordId aRecordId = recordIdIterator.next();

			if (recordId.lesserOrEqual(aRecordId)) {
				if (recordId.lesserThan(aRecordId)) {
					temp = aRecordId;
					return false;

				} else {
					return true;

				}
			}

			//continuing iterating
		}

		onIteratorFullyConsumed();
		return false;
	}

	protected void onIteratorFullyConsumed() {

	}

}
