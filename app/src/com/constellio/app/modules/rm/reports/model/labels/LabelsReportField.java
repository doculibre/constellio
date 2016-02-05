package com.constellio.app.modules.rm.reports.model.labels;

public class LabelsReportField {

	public int positionX;
	public int positionY;
	public int width;
	public int height;
	public int horizontalAlignment;
	public int verticalAlignment;

	private String value;
	private LabelsReportFont font;

	public LabelsReportField() {
		setValue("");
		setFont(new LabelsReportFont());
	}

	public String getValue() {
		return value;
	}

	public LabelsReportFont getFont() {
		return font;
	}

	public void setFont(LabelsReportFont font) {
		this.font = font;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
