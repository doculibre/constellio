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

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

public enum LabelsReportLayout {

	AVERY_5159(2, 7, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((2f / 16f) * 72f), ((4f / 32f) * 72f)),
	AVERY_5161(2, 10, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((6f / 16f) * 72f), ((12f / 32f) * 72f)),
	AVERY_5163(2, 10, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((6f / 16f) * 72f), ((12f / 32f) * 72f));

//	Top 0.5", Bottom 0.5", Left 0.18", Right 0.18"

	private int numberOfColumns;
	private int numberOfRows;
	private Rectangle pageSize;
	private float rightMargin;
	private float leftMargin;
	private float topMargin;
	private float bottomMargin;

	private LabelsReportLayout(int numberOfColumns, int numberOfRows, Rectangle pageSize, float rightMargin, float leftMargin,
			float topMargin, float bottomMargin) {
		this.numberOfColumns = numberOfColumns;
		this.numberOfRows = numberOfRows;
		this.pageSize = pageSize;
		this.rightMargin = rightMargin;
		this.leftMargin = leftMargin;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public Rectangle getPageSize() {
		return pageSize;
	}

	public float getRightMargin() {
		return rightMargin;
	}

	public float getLeftMargin() {
		return leftMargin;
	}

	public float getTopMargin() {
		return topMargin;
	}

	public float getBottomMargin() {
		return bottomMargin;
	}
}
