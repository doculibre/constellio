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
package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ClassificationPlanReportBuilder implements ReportBuilder {

	private static final int COLUMN_NUMBER = 20;
	public static final int TABLE_WIDTH_PERCENTAGE = 90;
	public static final int INITIAL_FONT_SIZE = 14;
	public static final int INITIAL_LEVEL = 0;
	private static final float MAX_LEVEL = 4;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 70f;
	public static final float MARGIN_BOTTOM = 20f;

	private ClassificationPlanReportModel model;
	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;
	
	public ClassificationPlanReportBuilder(
			ClassificationPlanReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();
		
		this.foldersLocator = foldersLocator;
	}

	public String getFileExtension() {
		return pdfTableUtils.PDF;
	}

	public void build(OutputStream output)
			throws IOException {
		Document document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			configPageEvents(writer);
			document.open();
			document.add(createReport(writer));
			document.close();
		} catch (DocumentException e){
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {
		PageEvent pageEvent = new PageEvent(foldersLocator);
		
		pageEvent.setTitle($("ClassificationPlanReport.Title"));
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}


	private PdfPTable createReport(PdfWriter writer) {
		PdfPTable table = new PdfPTable(1);

		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
		table.setExtendLastRow(true);

		for (ClassificationPlanReportModel_Category category : model.getRootCategories()) {
			int level = INITIAL_LEVEL;
			int fontSize = INITIAL_FONT_SIZE;
			float rowHeight = pdfTableUtils.ROW_HEIGHT;
			createSubTable(table, category, level, fontSize, rowHeight);
		}
		return table;
	}

	private void createSubTable(PdfPTable table, ClassificationPlanReportModel_Category category, int level, int fontSize,
			float rowHeight) {
		PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);

		subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		int colspan = COLUMN_NUMBER - level;
		pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
		String codeLabel = category.getCode() + " - " + category.getLabel();
		pdfTableUtils.addPhraseRow(subTable, codeLabel, fontSize, colspan);
		pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
		level = increaseLevel(level);
		fontSize = decreaseFontSize(level, fontSize);
		rowHeight = decreaseRowHeight(level, rowHeight);
		if (model.isDetailed()) {
			if (category.getDescription() != null) {
				pdfTableUtils.addPhraseRow(subTable, category.getDescription(), fontSize, colspan);
			}
		}

		table.addCell(subTable);

		for (ClassificationPlanReportModel_Category subCategory : category.getCategories()) {
			createSubTable(table, subCategory, level, fontSize, rowHeight);
		}
	}

	private float decreaseRowHeight(int level, float rowHeight) {
		if (level < MAX_LEVEL) {
			rowHeight = rowHeight - 2;
		}
		return rowHeight;
	}

	private int decreaseFontSize(int level, int fontSize) {
		if (level < MAX_LEVEL) {
			fontSize = fontSize - 2;
		}
		return fontSize;
	}

	private int increaseLevel(int level) {
		if (level < MAX_LEVEL) {
			level++;
		}
		return level;
	}

}
