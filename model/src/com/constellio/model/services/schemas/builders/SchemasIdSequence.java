package com.constellio.model.services.schemas.builders;

public class SchemasIdSequence {

	//First position is id 1
	boolean[] first2000 = new boolean[2000];
	boolean[] nextPositiveIds = null;
	boolean[] negativeIds = null;

	public void markAsAssigned(short id) {

		if (id > 0) {

			if (id < 2000) {
				first2000[id - 1] = true;
			} else {
				if (nextPositiveIds == null) {
					nextPositiveIds = new boolean[Short.MAX_VALUE - 2000 - 1];
				}
				nextPositiveIds[id - 1] = true;
			}

		}

		if (id < 0) {
			if (negativeIds == null) {
				negativeIds = new boolean[Short.MAX_VALUE + 1];
			}
			negativeIds[-1 * id] = true;
		}

	}

	public short getNewId() {
		for (short i = 0; i < 2000; i++) {
			if (!first2000[i]) {
				first2000[i] = true;
				return (short) (i + 1);
			}
		}

		if (nextPositiveIds == null) {
			nextPositiveIds = new boolean[Short.MAX_VALUE - 2000];
		}
		for (short i = 0; i < nextPositiveIds.length; i++) {
			if (!nextPositiveIds[i]) {
				nextPositiveIds[i] = true;
				return (short) (2001 + i);
			}
		}

		if (negativeIds == null) {
			negativeIds = new boolean[Short.MAX_VALUE + 1 - 1000];
		}
		for (short i = 0; i <= negativeIds.length; i++) {
			if (!negativeIds[i]) {
				negativeIds[i] = true;
				return (short) (-1 * i - 1000);
			}
		}

		throw new IllegalStateException("All 65535 ids are assigned");
	}

}
