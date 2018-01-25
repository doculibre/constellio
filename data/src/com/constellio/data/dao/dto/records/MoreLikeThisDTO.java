package com.constellio.data.dao.dto.records;

import java.io.Serializable;

public class MoreLikeThisDTO implements Serializable {

	RecordDTO record;

	double score;

	public MoreLikeThisDTO(RecordDTO record, double score) {
		this.record = record;
		this.score = score;
	}

	public RecordDTO getRecord() {
		return record;
	}

	public double getScore() {
		return score;
	}
}
