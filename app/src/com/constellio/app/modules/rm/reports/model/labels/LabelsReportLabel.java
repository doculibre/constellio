package com.constellio.app.modules.rm.reports.model.labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LabelsReportLabel {
	private static final int FIRST_GREATER_THAN_SECOND = 1;
	private static final int FIRST_LESS_THAN_SECOND = -1;

	private List<LabelsReportField> fields;

	public LabelsReportLabel(List<LabelsReportField> fields) {
		this.fields = fields;
	}

	public List<LabelsReportField> getFields() {
		return fields;
	}

	public void addField(LabelsReportField field) {
		fields.add(field);
	}

	public List<LabelsReportField> getFieldsInRow(int row) {
		List<LabelsReportField> fieldsInRow = new ArrayList<>();
		for (LabelsReportField field : fields) {
			if (field.positionY == row) {
				fieldsInRow.add(field);
			}
		}
		Collections.sort(fieldsInRow, new Comparator<LabelsReportField>() {
			@Override
			public int compare(LabelsReportField sticker1, LabelsReportField sticker2) {
				return sticker1.positionX < sticker2.positionX ? FIRST_LESS_THAN_SECOND : FIRST_GREATER_THAN_SECOND;
			}
		});
		return fieldsInRow;
	}
}
