package com.constellio.app.modules.rm.model.labelTemplate;

import java.io.Serializable;

public class LabelTemplateField implements Serializable {

	private String metadataCode;
	private String referenceMetadataCode;
	private String referenceMetadataCode2;
	private String referenceMetadataCode3;
	private int x;
	private int y;
	private int width;
	private int height;
	private String fontName;
	private float fontSize;
	private boolean bold;
	private boolean italic;
	private int maxLength;
	private LabelTemplateFieldHorizontalAlignment horizontalAlignment;
	private LabelTemplateFieldVerticalAlignment verticalAlignment;
	private String prefix;
	private String suffix;
	private String separator;
	private String pattern;
	private String concatenateMetadataCode;
	private String concatenateReferenceMetadataCode;
	private boolean uppercase;
	private boolean empty;
	private boolean displayEnumTitle;

	public LabelTemplateField(String metadataCode, String referenceMetadataCode, int x, int y, int width, int height,
			String fontName, float fontSize, boolean bold, boolean italic, int maxLength,
			LabelTemplateFieldHorizontalAlignment horizontalAlignment,
			LabelTemplateFieldVerticalAlignment verticalAlignment, String prefix, String suffix, String pattern) {
		this.metadataCode = metadataCode;
		this.referenceMetadataCode = referenceMetadataCode;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.bold = bold;
		this.italic = italic;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.maxLength = maxLength;
		this.prefix = prefix;
		this.suffix = suffix;
		this.pattern = pattern;
	}

	public LabelTemplateField(String metadataCode, String referenceMetadataCode, String referenceMetadataCode2,
			String referenceMetadataCode3, int x, int y,
			int width, int height,
			String fontName, float fontSize, boolean bold, boolean italic, int maxLength,
			LabelTemplateFieldHorizontalAlignment horizontalAlignment,
			LabelTemplateFieldVerticalAlignment verticalAlignment, String prefix, String suffix, String pattern,
			String concatenateMetadataCode, String concatenateReferenceMetadataCode, boolean uppercase, String separator,
			boolean empty) {
		this.metadataCode = metadataCode;
		this.referenceMetadataCode = referenceMetadataCode;
		this.referenceMetadataCode2 = referenceMetadataCode2;
		this.referenceMetadataCode3 = referenceMetadataCode3;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.bold = bold;
		this.italic = italic;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.maxLength = maxLength;
		this.prefix = prefix;
		this.suffix = suffix;
		this.pattern = pattern;
		this.concatenateMetadataCode = concatenateMetadataCode;
		this.concatenateReferenceMetadataCode = concatenateReferenceMetadataCode;
		this.uppercase = uppercase;
		this.separator = separator;
		this.empty = empty;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public String getReferenceMetadataCode() {
		return referenceMetadataCode;
	}

	public String getReferenceMetadataCode2() {
		return referenceMetadataCode2;
	}

	public String getReferenceMetadataCode3() {
		return referenceMetadataCode3;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getFontName() {
		return fontName;
	}

	public float getFontSize() {
		return fontSize;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public LabelTemplateFieldHorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public LabelTemplateFieldVerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public boolean isEmpty() {
		return empty;
	}

	public enum LabelTemplateFieldVerticalAlignment {TOP, CENTER, BOTTOM}

	public enum LabelTemplateFieldHorizontalAlignment {LEFT, CENTER, RIGHT}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getSeparator() {
		return separator;
	}

	public String getPattern() {
		return pattern;
	}

	public String getConcatenateMetadataCode() {
		return concatenateMetadataCode;
	}

	public String getConcatenateReferenceMetadataCode() {
		return concatenateReferenceMetadataCode;
	}

	public boolean isUppercase() {
		return uppercase;
	}

	//

	public boolean isDisplayEnumTitle() {
		return displayEnumTitle;
	}

	public void setDisplayEnumTitle(boolean displayEnumTitle) {
		this.displayEnumTitle = displayEnumTitle;
	}
}
