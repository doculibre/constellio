package com.constellio.app.modules.rm.reports.model.labels;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;

public enum LabelsReportLayout {

	AVERY_5159(2, 7, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((2f / 16f) * 72f), ((4f / 32f) * 72f)),
	AVERY_5159_V2(2, 7, PageSize.LETTER, 12f, 0f, 17f, 17f),
	AVERY_5161(2, 10, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((6f / 16f) * 72f), ((12f / 32f) * 72f)),

	//AVERY_5162(2, 7, PageSize.LETTER, 0.1525f * 72f, 0.1525f *72f, 0.88f *72f, 0.88f *72f),

	//AVERY_5162(2, 7, new RectangleReadOnly(612.0F, 900.0F), 18, 18, 60, 60),//new RectangleReadOnly(612.0F, 792.0F)
	AVERY_5162(2, 7, PageSize.LETTER, 18, 18, 50,50),
	//AVERY_5162(2, 7, PageSize.LETTER, 12, 12, 12, 57, 57),
	AVERY_5162_V1(2, 7, PageSize.LETTER, 12, 12, 57f, 57f),//60, 60),OK
	//AVERY_5162(2, 7, PageSize.LETTER, 2, 3, 60, 60),

	AVERY_5163(2, 10, PageSize.LETTER, ((6f / 32f) * 72f), ((2f / 16f) * 72f), ((6f / 16f) * 72f), ((12f / 32f) * 72f)),
	LABEL_1_5_X_5_25(2, 5, PageSize.LETTER.rotate(), 16.0f, 20.f, 36.0f, 37.0f),
	AVERY_5168(2, 2, PageSize.LETTER, 22.0f, 0f, 20.0f, 20.0f);

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

	public int getNumberOfLabelsPerPage() {
		return numberOfColumns * numberOfRows;
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
