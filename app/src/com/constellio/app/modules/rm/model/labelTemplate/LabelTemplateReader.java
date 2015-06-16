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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate.SchemaType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldHorizontalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldVerticalAlignment;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;

public class LabelTemplateReader {

	private static final String KEY = "key";
	private static final String NAME = "name";
	private static final String SCHEMA_TYPE = "schemaType";
	private static final String LAYOUT = "layout";
	private static final String COLUMNS = "columns";
	private static final String LINES = "lines";
	private static final String FIELD = "field";

	private static final String METADATA_CODE_FIELD = "metadataCode";
	private static final String REFERENCE_METADATA_CODE_FIELD = "referenceMetadataCode";
	private static final String X_FIELD = "x";
	private static final String Y_FIELD = "y";
	private static final String WIDTH_FIELD = "width";
	private static final String HEIGHT_FIELD = "height";
	private static final String FONT_NAME_FIELD = "fontName";
	private static final String FONT_SIZE_FIELD = "fontSize";
	private static final String HORIZONTAL_ALIGMENT_FIELD = "horizontalAlignment";
	private static final String VERTICAL_ALIGMENT_FIELD = "verticalAlignment";
	public static final String MAX_LENGTH = "maxLength";

	Document document;

	public LabelTemplateReader(Document document) {
		this.document = document;
	}

	public LabelTemplate createLabelTemplate() {
		LabelTemplate labelTemplate = createLabelTemplateObject();
		return labelTemplate;
	}

	private LabelTemplate createLabelTemplateObject() {
		LabelTemplate labelTemplate;

		Element labelTemplateElement = document.getRootElement();
		String key = labelTemplateElement.getChildText(KEY);
		String name = labelTemplateElement.getChildText(NAME);
		SchemaType schemaType = SchemaType.valueOf(labelTemplateElement.getChildText(SCHEMA_TYPE));
		LabelsReportLayout layout = LabelsReportLayout.valueOf(labelTemplateElement.getChildText(LAYOUT));
		int columns = Integer.valueOf(labelTemplateElement.getChildText(COLUMNS));
		int lines = Integer.valueOf(labelTemplateElement.getChildText(LINES));

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();
		for (Element fieldElement : labelTemplateElement.getChildren(FIELD)) {
			String metadataCode = fieldElement.getChildText(METADATA_CODE_FIELD);
			String referenceMetadataCode = fieldElement.getChildText(REFERENCE_METADATA_CODE_FIELD);
			int x = Integer.valueOf(fieldElement.getChildText(X_FIELD));
			int y = Integer.valueOf(fieldElement.getChildText(Y_FIELD));
			int width = Integer.valueOf(fieldElement.getChildText(WIDTH_FIELD));
			int height = Integer.valueOf(fieldElement.getChildText(HEIGHT_FIELD));
			String fontName = fieldElement.getChildText(FONT_NAME_FIELD);
			int fontSize = Integer.valueOf(fieldElement.getChildText(FONT_SIZE_FIELD));
			int maxLength = Integer.valueOf(fieldElement.getChildText(MAX_LENGTH));
			String horizontalAligment = fieldElement.getChildText(HORIZONTAL_ALIGMENT_FIELD);
			LabelTemplateFieldHorizontalAlignment labelTemplateFieldHorizontalAlignment = LabelTemplateFieldHorizontalAlignment
					.valueOf(horizontalAligment);
			String verticalAligment = fieldElement.getChildText(VERTICAL_ALIGMENT_FIELD);
			LabelTemplateFieldVerticalAlignment labelTemplateFieldVerticalAlignment = LabelTemplateFieldVerticalAlignment
					.valueOf(verticalAligment);

			LabelTemplateField labelTemplateField = new LabelTemplateField(metadataCode, referenceMetadataCode, x, y, width,
					height, fontName, fontSize, maxLength, labelTemplateFieldHorizontalAlignment,
					labelTemplateFieldVerticalAlignment);
			labelTemplateFields.add(labelTemplateField);
		}
		labelTemplate = new LabelTemplate(key, name, layout, schemaType, columns, lines, labelTemplateFields);
		return labelTemplate;
	}

}
