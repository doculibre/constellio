package com.constellio.model.services.search.entities;

import java.io.Serializable;

public class SearchBoost implements Serializable {
	
	public static final String METADATA_TYPE = "metadata";
	public static final String QUERY_TYPE = "query";

	private String key;

	private String label;

	private double value;

	private String type;

	public SearchBoost() {
	}

	public SearchBoost(String type, String key, String label, double value) {
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

	public String getLabel() {
		return label;
	}

	public double getValue() {
		return value;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
