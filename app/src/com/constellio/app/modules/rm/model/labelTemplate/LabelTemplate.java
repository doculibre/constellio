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
package com.constellio.app.modules.rm.model.labelTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;

public class LabelTemplate implements Serializable {

	private final LabelsReportLayout labelsReportLayout;
	private final String key;
	private final String name;
	private final String schemaType;
	private final int columns;
	private final int lines;
	private final List<LabelTemplateField> fields;

	public LabelTemplate() {
		this.key = "";
		this.name = "";
		this.schemaType = "";
		this.columns = 1;
		this.lines = 1;
		this.fields = new ArrayList<>();
		this.labelsReportLayout = LabelsReportLayout.AVERY_5159;

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
}
