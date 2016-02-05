package com.constellio.app.modules.rm.reports.model.decommissioning;

public class ReportBooleanField {
	private String label;

	private Boolean value;

	public ReportBooleanField(String label, Boolean value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public Boolean getValue() {
		return value;
	}
}