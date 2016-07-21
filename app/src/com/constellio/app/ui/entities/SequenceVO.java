package com.constellio.app.ui.entities;

import java.io.Serializable;

public class SequenceVO implements Serializable {
	
	private final String sequenceId;
	
	private final String sequenceTitle;
	
	private Long sequenceValue;

	public SequenceVO(String sequenceId, String sequenceTitle, Long sequenceValue) {
		this.sequenceId = sequenceId;
		this.sequenceTitle = sequenceTitle;
		this.sequenceValue = sequenceValue;
	}

	public Long getSequenceValue() {
		return sequenceValue;
	}

	public void setSequenceValue(Long sequenceValue) {
		this.sequenceValue = sequenceValue;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public String getSequenceTitle() {
		return sequenceTitle;
	}

}
