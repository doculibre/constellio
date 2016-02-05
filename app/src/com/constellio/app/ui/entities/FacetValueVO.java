package com.constellio.app.ui.entities;

import com.constellio.data.dao.dto.records.FacetValue;

public class FacetValueVO {

	String facetId;

	String value;

	String label;

	int count;

	public FacetValueVO() {

	}

	public FacetValueVO(String facetId, FacetValue value) {
		this.facetId = facetId;
		this.value = value.getValue();
		this.count = (int) value.getQuantity();
	}

	public FacetValueVO(String facetId, FacetValue value, String label) {
		this.facetId = facetId;
		this.value = value.getValue();
		this.label = label;
		this.count = (int) value.getQuantity();
	}

	public FacetValueVO(String facetId, String value, String label, int count) {
		this.facetId = facetId;
		this.value = value;
		this.label = label;
		this.count = count;
	}

	public String getFacetId() {
		return facetId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
