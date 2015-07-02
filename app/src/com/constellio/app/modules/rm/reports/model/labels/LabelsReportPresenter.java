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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.itextpdf.text.Font.FontFamily;

public class LabelsReportPresenter {

	public static final LabelsReportFont FONT = new LabelsReportFont().setSize(8.0f).setBold(true).setItalic(true);

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;

	private int startPosition;

	public LabelsReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public LabelsReportModel build(List<String> ids, int startPosition, int copies,
			final LabelTemplate labelTemplate) {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.startPosition = startPosition;

		LabelsReportModel labelsReportModel = new LabelsReportModel();
		labelsReportModel.setLayout(labelTemplate.getLabelsReportLayout());
		labelsReportModel.setColumnsNumber(labelTemplate.getColumns());
		labelsReportModel.setRowsNumber(labelTemplate.getLines());
		labelsReportModel.setPrintBorders(true);

		List<LabelsReportLabel> labels = new ArrayList<>();

		addBlankLabelsBelowStartPosition(labels);

		List<Record> records = modelLayerFactory.newRecordServices().getRecordsById(collection, ids);
		for (Record record : records) {
			for (int i = 0; i < copies; i++) {
				List<LabelsReportField> fields = new ArrayList<>();
				for (LabelTemplateField fieldInfo : labelTemplate.getFields()) {
					String value = getPrintedValue(fieldInfo, record);
					LabelsReportField field = buildField(fieldInfo, value);
					fields.add(field);
				}
				LabelsReportLabel label = new LabelsReportLabel(fields);
				labels.add(label);
			}
		}

		if (labels.isEmpty()) {
			labels.add(new LabelsReportLabel(new ArrayList<LabelsReportField>()));
		}

		labelsReportModel.setLabelsReportLabels(labels);

		return labelsReportModel;
	}

	private LabelsReportField buildField(LabelTemplateField fieldInfo, String value) {
		FontFamily fontFamily = FontFamily.valueOf(fieldInfo.getFontName());
		LabelsReportFont font = new LabelsReportFont().setFontFamily(fontFamily)
				.setSize(fieldInfo.getFontSize()).setBold(fieldInfo.isBold()).setItalic(fieldInfo.isItalic());
		int width = fieldInfo.getWidth() != 0 ? fieldInfo.getWidth() : value.length();
		int horizontalAlignment;
		int verticalAlignment;
		horizontalAlignment = getHorizontalAligment(fieldInfo);

		verticalAlignment = getVerticalAligment(fieldInfo);
		LabelsReportField labelsReportField = newField(value, fieldInfo.getX(), fieldInfo.getY(), fieldInfo.getHeight(),
				width, horizontalAlignment, verticalAlignment, font);
		return labelsReportField;
	}

	private int getVerticalAligment(LabelTemplateField fieldInfo) {
		int verticalAlignment;
		switch (fieldInfo.getVerticalAlignment()) {
		case TOP:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_TOP;
			break;
		case CENTER:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		case BOTTOM:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_BOTTOM;
			break;
		default:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		}
		return verticalAlignment;
	}

	private int getHorizontalAligment(LabelTemplateField fieldInfo) {
		int horizontalAlignment;
		switch (fieldInfo.getHorizontalAlignment()) {
		case LEFT:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
			break;
		case CENTER:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		case RIGHT:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_RIGHT;
			break;
		default:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
			break;
		}
		return horizontalAlignment;
	}

	private String getPrintedValue(LabelTemplateField fieldInfo, Record record) {
		MetadataSchemaTypes types = rmSchemasRecordsServices.getTypes();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		String value;
		if (StringUtils.isNotBlank(fieldInfo.getReferenceMetadataCode())) {
			Metadata metadata = types.getMetadata(fieldInfo.getMetadataCode());
			String referenceId = record.get(metadata);
			Record referenceRecord = recordServices.getDocumentById(referenceId);
			Metadata referenceMetadata = types.getMetadata(fieldInfo.getReferenceMetadataCode());
			Object valueObject = referenceRecord.get(referenceMetadata);
			value = getStringValue(valueObject, fieldInfo);
		} else if (StringUtils.isNotBlank(fieldInfo.getMetadataCode())) {
			Metadata metadata = types.getMetadata(fieldInfo.getMetadataCode());
			Object valueObject = record.get(metadata);
			value = getStringValue(valueObject, fieldInfo);
		} else {
			value = getStringValue("", fieldInfo);
		}
		value = truncate(value, fieldInfo.getMaxLength());
		return value;
	}

	private String getStringValue(Object valueObject, LabelTemplateField fieldInfo) {
		String value;
		if (valueObject instanceof EnumWithSmallCode) {
			value = ((EnumWithSmallCode) valueObject).getCode();
		} else if (valueObject instanceof LocalDate || valueObject instanceof LocalDateTime) {
			if (StringUtils.isNotBlank(fieldInfo.getPattern())) {
				if (valueObject instanceof LocalDate) {
					value = ((LocalDate) valueObject).toString(fieldInfo.getPattern());
				} else {
					value = ((LocalDateTime) valueObject).toString(fieldInfo.getPattern());
				}
			} else {
				value = valueObject.toString();
			}
		} else {
			value = (String) valueObject;
		}
		if (StringUtils.isNotBlank(fieldInfo.getPrefix())) {
			value = fieldInfo.getPrefix() + value;
		}
		if (StringUtils.isNotBlank(fieldInfo.getSuffix())) {
			value = value + fieldInfo.getSuffix();
		}
		return value;
	}

	private void addBlankLabelsBelowStartPosition(List<LabelsReportLabel> labels) {
		for (int i = 1; i < startPosition; i++) {
			List<LabelsReportField> fields = new ArrayList<>();
			LabelsReportLabel emptyLabel = new LabelsReportLabel(fields);
			labels.add(emptyLabel);
		}
	}

	private LabelsReportField newField(String value, int x, int y, int height, int width, int horizontalAlignment,
			int verticalAlignment, LabelsReportFont font) {
		LabelsReportField field = new LabelsReportField();

		field.setValue(value);
		field.positionX = x;
		field.positionY = y;
		field.height = height;
		field.width = width;
		field.horizontalAlignment = horizontalAlignment;
		field.verticalAlignment = verticalAlignment;
		field.setFont(font);
		return field;
	}

	private String truncate(String value, int maximumCharacters) {
		return StringUtils.substring(value, 0, maximumCharacters);
	}

	private String pad(String value, int size) {
		return StringUtils.leftPad(value, size, " ");
	}

}
