package com.constellio.app.ui.framework.data.event;

import java.io.Serializable;

public class EventStatistics implements Serializable{
	private String label;
	private Float value;

	public void setLabel(String label) {
		this.label = label;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getLabel() {

		return label;
	}
}
