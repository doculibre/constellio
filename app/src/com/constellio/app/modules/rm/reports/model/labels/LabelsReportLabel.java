/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
