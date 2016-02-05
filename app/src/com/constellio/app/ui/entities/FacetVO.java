package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;

public class FacetVO implements Serializable {
	private final String id;
	private final String label;
	private final List<FacetValueVO> values;
	private final boolean open;
	private final int valuesPerPage;

	public FacetVO(String id, String label, List<FacetValueVO> values, boolean open, int valuesPerPage) {
		this.id = id;
		this.label = label;
		this.values = values;
		this.open = open;
		this.valuesPerPage = valuesPerPage;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public List<FacetValueVO> getValues() {
		return values;
	}

	public boolean isOpen() {
		return open;
	}

	public int getValuesPerPage() {
		return valuesPerPage;
	}
}
