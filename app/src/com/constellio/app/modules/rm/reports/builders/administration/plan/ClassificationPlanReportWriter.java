package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ClassificationPlanReportWriter implements ReportWriter {

	private static final int COLUMN_NUMBER = 20;
	public static final int TABLE_WIDTH_PERCENTAGE = 90;
	public static final int INITIAL_FONT_SIZE = 14;
	public static final int INITIAL_LEVEL = 0;
	private static final float MAX_LEVEL = 4;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 70f;
	public static final float MARGIN_BOTTOM = 20f;
	public static final int ADMINISTRATIVE_UNIT_TITLE_FONT_SIZE = INITIAL_FONT_SIZE + 2;

	private ClassificationPlanReportModel model;
	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public ClassificationPlanReportWriter(
			ClassificationPlanReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();

		this.foldersLocator = foldersLocator;
	}

	public String getFileExtension() {
		return PdfTableUtils.PDF;
	}

	public void write(OutputStream output)
			throws IOException {
		Document document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			configPageEvents(writer);
			document.open();
			document.add(createReport(writer));
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {
		PageEvent pageEvent = new PageEvent(foldersLocator);

		String title;
		if (model.isByAdministrativeUnit()) {
			title = "ClassificationPlanByAdministrativeUnitReport.Title";
		} else if (model.isDetailed()) {
			title = "ClassificationPlanReport.Title";
		} else {
			title = "ClassificationPlanReport.Title";
		}
		pageEvent.setTitle($(title));
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}

	private PdfPTable createReport(PdfWriter writer) {
		PdfPTable table = new PdfPTable(1);

		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
		table.setExtendLastRow(true);

		if (model.isByAdministrativeUnit()) {
			pdfTableUtils.addEmptyRows(table, 4, table.getDefaultCell().getHeight());
			for (Entry<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> adminUnitCategoriesEntry : model
					.getCategoriesByAdministrativeUnitMap().entrySet()) {
				PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);
				subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				String codeTitle =
						adminUnitCategoriesEntry.getKey().getCode() + " - " + adminUnitCategoriesEntry.getKey().getTitle();
				pdfTableUtils
						.addCenterPhraseRow(subTable, codeTitle, INITIAL_FONT_SIZE + 2);
				table.addCell(subTable);
				pdfTableUtils.addEmptyRows(table, 1, table.getDefaultCell().getHeight());
				for (ClassificationPlanReportModel_Category category : adminUnitCategoriesEntry.getValue()) {
					int level = INITIAL_LEVEL;
					int fontSize = INITIAL_FONT_SIZE;
					float rowHeight = PdfTableUtils.ROW_HEIGHT;
					createSubTable(table, category, level, fontSize, rowHeight);
				}
			}
		} else {
			for (ClassificationPlanReportModel_Category category : model.getRootCategories()) {
				int level = INITIAL_LEVEL;
				int fontSize = INITIAL_FONT_SIZE;
				float rowHeight = PdfTableUtils.ROW_HEIGHT;
				createSubTable(table, category, level, fontSize, rowHeight);
			}
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
		pdfTableUtils.addLeftPhraseRow(subTable, codeLabel, fontSize, colspan);
		pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
		int newlevel = increaseLevel(level);
		int newfontSize = decreaseFontSize(level, fontSize);
		float newRowHeight = decreaseRowHeight(level, rowHeight);
		if (model.isDetailed()) {
			if (category.getDescription() != null) {
				pdfTableUtils.addLeftPhraseRow(subTable, category.getDescription(), fontSize, colspan);
			}
		}

		if (model.isByAdministrativeUnit()) {
			pdfTableUtils.addEmptyRows(subTable, 1, rowHeight);
			pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
			pdfTableUtils.addLeftPhraseRow(subTable, $("ClassificationPlanReport.keywords"), fontSize, colspan);
			for (String keyword : category.getKeywords()) {
				pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
				pdfTableUtils.addLeftPhraseRow(subTable, keyword.toUpperCase(), fontSize, colspan);

			}
			pdfTableUtils.addEmptyRows(subTable, 1, rowHeight);
			pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
			pdfTableUtils.addLeftPhraseCell(subTable, $("ClassificationPlanReport.retentionRules"), fontSize, colspan);
			pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
			pdfTableUtils
					.addLeftPhraseCell(subTable, category.getRetentionRules().toString().replaceFirst("\\[", "").replace("]", ""),
							fontSize,
							colspan);
		}

		table.addCell(subTable);

		if (!model.isByAdministrativeUnit()) {
			for (ClassificationPlanReportModel_Category subCategory : category.getCategories()) {
				createSubTable(table, subCategory, newlevel, newfontSize, newRowHeight);
			}
		}
		pdfTableUtils.addEmptyRows(subTable, 1, newRowHeight);
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
