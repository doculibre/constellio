package com.constellio.app.modules.rm.reports.model.labels;

import java.util.ArrayList;
import java.util.List;

public class LabelsReportModel {

	private boolean printBorders = true;

	private List<LabelsReportLabel> labelsReportLabels = new ArrayList<>();

	private LabelsReportLayout layout;

	private int columnsNumber;

	private int rowsNumber;

	public boolean isPrintBorders() {
		return printBorders;
	}

	public void setPrintBorders(boolean printBorders) {
		this.printBorders = printBorders;
	}

	public List<LabelsReportLabel> getLabelsReportLabels() {
		return labelsReportLabels;
	}

	public LabelsReportLayout getLayout() {
		return layout;
	}

	public void setLayout(LabelsReportLayout layout) {
		this.layout = layout;
	}

	public void setLabelsReportLabels(List<LabelsReportLabel> labelsReportLabels) {
		this.labelsReportLabels = labelsReportLabels;
	}

	public int getColumnsNumber() {
		return columnsNumber;
	}

	public void setColumnsNumber(int columnsNumber) {
		this.columnsNumber = columnsNumber;
	}

	public int getRowsNumber() {
		return rowsNumber;
	}

	public void setRowsNumber(int rowsNumber) {
		this.rowsNumber = rowsNumber;
	}
}
