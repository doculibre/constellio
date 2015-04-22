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

import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

public class LabelsReportFont {

	private boolean bold = false;
	private boolean italic = false;
	private boolean underlined = false;
	private float size = Font.DEFAULTSIZE;

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

	public Font getFont() {
		int style = Font.NORMAL;
		if (isBold() && isItalic()) {
			style = Font.BOLDITALIC;
		} else if (isBold()) {
			style = Font.BOLD;
		} else if (isItalic()) {
			style = Font.ITALIC;
		}
		Font font = new Font(FontFamily.HELVETICA, size, style);
		return font;
	}
}
