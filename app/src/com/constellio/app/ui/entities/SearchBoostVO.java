package com.constellio.app.ui.entities;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SearchBoostVO implements Serializable {

	private String key;

	private String label;

	private double value;

	private String type;

	public SearchBoostVO() {
	}

	public SearchBoostVO(String type, String key, String label, double value) {
		this.type = type;
		this.key = key;
		this.label = label;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
