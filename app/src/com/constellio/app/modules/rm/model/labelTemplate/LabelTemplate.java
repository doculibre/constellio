package com.constellio.app.modules.rm.model.labelTemplate;

import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.ui.components.Dimensionnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LabelTemplate implements Dimensionnable, Serializable {

	private final LabelsReportLayout labelsReportLayout;
	private final String key;
	private final String name;
	private final String schemaType;
	private final int columns;
	private final int lines;
	private final List<LabelTemplateField> fields;
	private boolean printBorders;

	public LabelTemplate() {
		this.key = "";
		this.name = "";
		this.schemaType = "";
		this.columns = 1;
		this.lines = 1;
		this.fields = new ArrayList<>();
		this.labelsReportLayout = LabelsReportLayout.AVERY_5159;
		this.printBorders = false;

	}

	public LabelTemplate(String key, String name, LabelsReportLayout labelsReportLayout, String schemaType, int columns,
						 int lines,
						 List<LabelTemplateField> fields) {
		this.key = key;
		this.name = name;
		this.schemaType = schemaType;
		this.columns = columns;
		this.lines = lines;
		this.fields = fields;
		this.labelsReportLayout = labelsReportLayout;
		this.printBorders = false;
	}

	public LabelTemplate(String key, String name, LabelsReportLayout labelsReportLayout, String schemaType, int columns,
						 int lines, List<LabelTemplateField> fields, boolean printBorders) {
		this.key = key;
		this.name = name;
		this.schemaType = schemaType;
		this.columns = columns;
		this.lines = lines;
		this.fields = fields;
		this.labelsReportLayout = labelsReportLayout;
		this.printBorders = printBorders;
	}

	public String getName() {
		return name;
	}

	public LabelsReportLayout getLabelsReportLayout() {
		return labelsReportLayout;
	}

	public int getColumns() {
		return columns;
	}

	public int getLines() {
		return lines;
	}

	public List<LabelTemplateField> getFields() {
		return fields;
	}

	public String getKey() {
		return key;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public boolean isPrintBorders() {
		return printBorders;
	}

	@Override
	public int getDimension() {
		return labelsReportLayout.getNumberOfLabelsPerPage();
	}
}
