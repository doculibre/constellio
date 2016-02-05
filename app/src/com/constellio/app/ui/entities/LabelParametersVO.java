package com.constellio.app.ui.entities;

import java.io.Serializable;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;

public class LabelParametersVO implements Serializable {
	private LabelTemplate labelConfiguration;
	private int startPosition;
	private int numberOfCopies;

	public LabelParametersVO(LabelTemplate labelConfiguration) {
		this.labelConfiguration = labelConfiguration;
		startPosition = 1;
		numberOfCopies = 1;
	}

	public LabelTemplate getLabelConfiguration() {
		return labelConfiguration;
	}

	public void setLabelConfiguration(LabelTemplate labelConfiguration) {
		this.labelConfiguration = labelConfiguration;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

	public void setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}
}
