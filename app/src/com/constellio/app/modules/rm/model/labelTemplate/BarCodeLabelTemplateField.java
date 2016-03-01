package com.constellio.app.modules.rm.model.labelTemplate;

public class BarCodeLabelTemplateField extends LabelTemplateField {

	public BarCodeLabelTemplateField(String metadataCode, String referenceMetadataCode, int x, int y, int width, int height, String fontName, float fontSize, boolean bold,
			boolean italic, int maxLength, LabelTemplateFieldHorizontalAlignment horizontalAlignment, LabelTemplateFieldVerticalAlignment verticalAlignment, String prefix,
			String suffix, String pattern) {
		super(metadataCode, referenceMetadataCode, x, y, width, height, fontName, fontSize, bold, italic, maxLength, horizontalAlignment, verticalAlignment, prefix, suffix,
				pattern);
	}

	public BarCodeLabelTemplateField(String metadataCode, String referenceMetadataCode, String referenceMetadataCode2, String referenceMetadataCode3, int x, int y, int width,
			int height, String fontName, float fontSize, boolean bold, boolean italic, int maxLength, LabelTemplateFieldHorizontalAlignment horizontalAlignment,
			LabelTemplateFieldVerticalAlignment verticalAlignment, String prefix, String suffix, String pattern, String concatenateMetadataCode,
			String concatenateReferenceMetadataCode, boolean uppercase, String separator, boolean empty) {
		super(metadataCode, referenceMetadataCode, referenceMetadataCode2, referenceMetadataCode3, x, y, width, height, fontName, fontSize, bold, italic, maxLength,
				horizontalAlignment, verticalAlignment, prefix, suffix, pattern, concatenateMetadataCode, concatenateReferenceMetadataCode, uppercase, separator, empty);
	}

}
