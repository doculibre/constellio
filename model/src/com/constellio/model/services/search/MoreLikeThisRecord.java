package com.constellio.model.services.search;

import com.constellio.model.entities.records.Record;

public class MoreLikeThisRecord {

	Record record;

	double score;

	public MoreLikeThisRecord(Record record, double score) {
		this.record = record;
		this.score = score;
	}

	public Record getRecord() {
		return record;
	}

	public double getScore() {
		return score;
	}

	public double getFlooredScore() {
		return Math.floor(score * 100) / 100.0;
	}

	public boolean isIdentical() {
		return score == 1.0;
	}
}
