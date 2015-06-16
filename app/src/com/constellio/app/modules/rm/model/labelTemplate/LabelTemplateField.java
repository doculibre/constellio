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

public class LabelTemplateField implements Serializable {

	private String metadataCode;
	private String referenceMetadataCode;
	private int x;
	private int y;
	private int width;
	private int height;
	private String fontName;
	private float fontSize;
	private int maxLength;
	private LabelTemplateFieldHorizontalAlignment horizontalAlignment;
	private LabelTemplateFieldVerticalAlignment verticalAlignment;

	public LabelTemplateField(String metadataCode, String referenceMetadataCode, int x, int y, int width, int height,
			String fontName, float fontSize, int maxLength,
			LabelTemplateFieldHorizontalAlignment horizontalAlignment,
			LabelTemplateFieldVerticalAlignment verticalAlignment) {
		this.metadataCode = metadataCode;
		this.referenceMetadataCode = referenceMetadataCode;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.maxLength = maxLength;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public String getReferenceMetadataCode() {
		return referenceMetadataCode;
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

	public LabelTemplateFieldHorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public LabelTemplateFieldVerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public enum LabelTemplateFieldVerticalAlignment {TOP, CENTER, BOTTOM}

	public enum LabelTemplateFieldHorizontalAlignment {LEFT, CENTER, RIGHT}

}
