package com.constellio.app.modules.rm.reports.model.labels;

import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

public class LabelsReportFont {

	private boolean bold = false;
	private boolean italic = false;
	private boolean underlined = false;
	private float size = Font.DEFAULTSIZE;
	private FontFamily fontFamily = FontFamily.HELVETICA;

	public boolean isBold() {
		return bold;
	}

	public LabelsReportFont setBold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public boolean isItalic() {
		return italic;
	}

	public LabelsReportFont setItalic(boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public LabelsReportFont setUnderlined(boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public float getSize() {
		return size;
	}

	public LabelsReportFont setSize(float size) {
		this.size = size;
		return this;
	}

	public FontFamily getFontFamily() {
		return fontFamily;
	}

	public LabelsReportFont setFontFamily(FontFamily fontFamily) {
		this.fontFamily = fontFamily;
		return this;
	}

	public Font getFont() {
		int style = Font.NORMAL;
		if (isBold() && isItalic()) {
			style = Font.BOLDITALIC;
		} else if (isBold()) {
			style = Font.BOLD;
		} else if (isItalic()) {
			style = Font.ITALIC;
		}
		Font font = new Font(fontFamily, size, style);
		return font;
	}
}
