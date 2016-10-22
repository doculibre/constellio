package com.constellio.app.modules.rm.model.labelTemplate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

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
	private static final String PRINT_BORDERS = "printBorders";

	private static final String METADATA_CODE_FIELD = "metadataCode";
	private static final String REFERENCE_METADATA_CODE_FIELD = "referenceMetadataCode";
	private static final String REFERENCE_METADATA_CODE2_FIELD = "referenceMetadataCode2";
	private static final String REFERENCE_METADATA_CODE3_FIELD = "referenceMetadataCode3";
	private static final String X_FIELD = "x";
	private static final String Y_FIELD = "y";
	private static final String WIDTH_FIELD = "width";
	private static final String HEIGHT_FIELD = "height";
	private static final String FONT_NAME_FIELD = "fontName";
	private static final String FONT_SIZE_FIELD = "fontSize";
	private static final String HORIZONTAL_ALIGMENT_FIELD = "horizontalAlignment";
	private static final String VERTICAL_ALIGMENT_FIELD = "verticalAlignment";
	public static final String MAX_LENGTH = "maxLength";
	public static final String PREFIX = "prefix";
	public static final String SUFFIX = "suffix";
	public static final String PATTERN = "pattern";
	public static final String ITALIC = "italic";
	public static final String BOLD = "bold";
	public static final String CONCATENATE_METADATA_CODE = "concatenateMetadataCode";
	public static final String CONCATENATE_REFERENCE_METADATA_CODE = "concatenateReferenceMetadataCode";
	public static final String UPPERCASE = "uppercase";
	public static final String SEPARATOR = "separator";
	public static final String EMPTY = "empty";
	public static final String DISPLAY_ENUM_TITLE = "displayEnumTitle";

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
		String schemaType = labelTemplateElement.getChildText(SCHEMA_TYPE);
		LabelsReportLayout layout = LabelsReportLayout.valueOf(labelTemplateElement.getChildText(LAYOUT));
		int columns = Integer.valueOf(labelTemplateElement.getChildText(COLUMNS));
		int lines = Integer.valueOf(labelTemplateElement.getChildText(LINES));
		boolean printBorders = "true".equals(labelTemplateElement.getChildText(PRINT_BORDERS));

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();
		for (Element fieldElement : labelTemplateElement.getChildren(FIELD)) {
			String metadataCode = fieldElement.getChildText(METADATA_CODE_FIELD);
			String referenceMetadataCode = fieldElement.getChildText(REFERENCE_METADATA_CODE_FIELD);
			String referenceMetadataCode2 = fieldElement.getChildText(REFERENCE_METADATA_CODE2_FIELD);
			String referenceMetadataCode3 = fieldElement.getChildText(REFERENCE_METADATA_CODE3_FIELD);
			int x = Integer.valueOf(fieldElement.getChildText(X_FIELD));
			int y = Integer.valueOf(fieldElement.getChildText(Y_FIELD));
			int width = Integer.valueOf(fieldElement.getChildText(WIDTH_FIELD));
			int height = Integer.valueOf(fieldElement.getChildText(HEIGHT_FIELD));
			String fontName = fieldElement.getChildText(FONT_NAME_FIELD);
			float fontSize = Float.valueOf(fieldElement.getChildText(FONT_SIZE_FIELD));
			boolean bold = Boolean.valueOf(fieldElement.getChildText(BOLD));
			boolean italic = Boolean.valueOf(fieldElement.getChildText(ITALIC));
			int maxLength = Integer.valueOf(fieldElement.getChildText(MAX_LENGTH));
			String horizontalAligment = fieldElement.getChildText(HORIZONTAL_ALIGMENT_FIELD);
			LabelTemplateFieldHorizontalAlignment labelTemplateFieldHorizontalAlignment = LabelTemplateFieldHorizontalAlignment
					.valueOf(horizontalAligment);
			String verticalAligment = fieldElement.getChildText(VERTICAL_ALIGMENT_FIELD);
			LabelTemplateFieldVerticalAlignment labelTemplateFieldVerticalAlignment = LabelTemplateFieldVerticalAlignment
					.valueOf(verticalAligment);
			String prefix = fieldElement.getChildText(PREFIX);
			String suffix = fieldElement.getChildText(SUFFIX);
			String pattern = fieldElement.getChildText(PATTERN);

			String concatenateMetadataCode = fieldElement.getChildText(CONCATENATE_METADATA_CODE);
			String concatenateReferenceMetadataCode = fieldElement.getChildText(CONCATENATE_REFERENCE_METADATA_CODE);
			boolean uppercase = Boolean.valueOf(fieldElement.getChildText(UPPERCASE));
			String separator = fieldElement.getChildText(SEPARATOR);
			boolean empty = Boolean.valueOf(fieldElement.getChildText(EMPTY));
			boolean displayEnumTitle = Boolean.valueOf(fieldElement.getChildText(DISPLAY_ENUM_TITLE));

			LabelTemplateField labelTemplateField;
			if (StringUtils.isBlank(referenceMetadataCode2) && StringUtils.isBlank(concatenateMetadataCode) && StringUtils
					.isBlank(fieldElement.getChildText(UPPERCASE)) && StringUtils.isBlank(fieldElement.getChildText(EMPTY))) {
				labelTemplateField = new LabelTemplateField(metadataCode, referenceMetadataCode, x, y, width,
						height, fontName, fontSize, bold, italic, maxLength, labelTemplateFieldHorizontalAlignment,
						labelTemplateFieldVerticalAlignment, prefix, suffix, pattern);
			} else {
				labelTemplateField = new LabelTemplateField(metadataCode, referenceMetadataCode, referenceMetadataCode2,
						referenceMetadataCode3, x, y,
						width,
						height, fontName, fontSize, bold, italic, maxLength, labelTemplateFieldHorizontalAlignment,
						labelTemplateFieldVerticalAlignment, prefix, suffix, pattern, concatenateMetadataCode,
						concatenateReferenceMetadataCode, uppercase, separator, empty);
			}
			labelTemplateField.setDisplayEnumTitle(displayEnumTitle);
			labelTemplateFields.add(labelTemplateField);
		}
		labelTemplate = new LabelTemplate(key, name, layout, schemaType, columns, lines, labelTemplateFields, printBorders);
		return labelTemplate;
	}

}
