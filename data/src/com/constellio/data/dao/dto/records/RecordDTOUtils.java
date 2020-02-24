package com.constellio.data.dao.dto.records;

public class RecordDTOUtils {
	public static String toStringId(int intId) {

		//Since this transformation is done very often, we are using this faster approach instead of StringUtils.leftPad

		if (intId < 10_000) {
			if (intId < 0) {
				throw new IllegalArgumentException("Negative ids are not supported");

			} else if (intId < 10) {
				return "0000000000" + intId;

			} else if (intId < 100) {
				return "000000000" + intId;

			} else if (intId < 1000) {
				return "00000000" + intId;

			} else {
				return "0000000" + intId;
			}
		} else {
			if (intId < 100_000) {
				return "000000" + intId;

			} else if (intId < 1_000_000) {
				return "00000" + intId;

			} else if (intId < 10_000_000) {
				return "0000" + intId;

			} else if (intId < 100_000_000) {
				return "000" + intId;

			} else if (intId < 1_000_000_000) {
				return "00" + intId;

			} else {
				return "0" + intId;
			}
		}

	}
}
