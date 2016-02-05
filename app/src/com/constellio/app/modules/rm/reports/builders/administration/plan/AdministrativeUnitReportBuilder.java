package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class AdministrativeUnitReportBuilder implements ReportBuilder {

	private static final int COLUMN_NUMBER = 20;
	public static final int TABLE_WIDTH_PERCENTAGE = 90;
	public static final int INITIAL_FONT_SIZE = 14;
	public static final int INITIAL_LEVEL = 0;
	private static final float MAX_LEVEL = 4;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 87f;
	public static final float MARGIN_BOTTOM = 20f;

	private AdministrativeUnitReportModel model;

	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public AdministrativeUnitReportBuilder(AdministrativeUnitReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();

		this.foldersLocator = foldersLocator;
	}

	public String getFileExtension() {
		return PdfTableUtils.PDF;
	}

	public void build(OutputStream output)
			throws IOException {
		Document document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			configPageEvents(writer);

			document.open();
			document.add(createReport());
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {
		PageEvent pageEvent = new PageEvent(foldersLocator);

		String title;
		if (model.isDetailed()) {
			title = $("AdministrativeUnitReport.TitleWithUsers");
		} else {
			title = $("AdministrativeUnitReport.Title");
		}

		pageEvent.setTitle(title);
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}

	private PdfPTable createReport() {

		PdfPTable table = new PdfPTable(1);

		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
		table.setExtendLastRow(true);

		for (AdministrativeUnitReportModel_AdministrativeUnit adminUnit : model.getAdministrativeUnits()) {
			int level = INITIAL_LEVEL;
			int fontSize = INITIAL_FONT_SIZE;
			float rowHeight = PdfTableUtils.ROW_HEIGHT;

			createSubTable(table, adminUnit, level, fontSize, rowHeight);
		}
		return table;
	}

	private void createSubTable(PdfPTable table, AdministrativeUnitReportModel_AdministrativeUnit adminUnit, int level,
			int fontSize, float rowHeight) {

		PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);
		subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

		printAdminUnitLabel(table, adminUnit, level, fontSize, rowHeight);

		level = increaseLevel(level);
		printUsers(table, adminUnit, level, fontSize, rowHeight);

		for (AdministrativeUnitReportModel_AdministrativeUnit childAdminUnit : adminUnit.getChildAdministrativeUnits()) {
			createSubTable(table, childAdminUnit, level, fontSize, rowHeight);
		}
	}

	private void printAdminUnitLabel(PdfPTable table, AdministrativeUnitReportModel_AdministrativeUnit adminUnit, int level,
			int fontSize, float rowHeight) {
		String adminUnitLabel = adminUnit.getCode() + " - " + adminUnit.getLabel();
		PdfPTable line = getLine(adminUnitLabel, level, fontSize, rowHeight);
		table.addCell(line);
	}

	private void printUsers(PdfPTable table, AdministrativeUnitReportModel_AdministrativeUnit adminUnit, int level,
			int fontSize, float rowHeight) {

		for (AdministrativeUnitReportModel_User user : adminUnit.getUsers()) {
			addUser(table, user, level, fontSize, rowHeight);
		}

	}

	private PdfPTable addUser(PdfPTable subtable, AdministrativeUnitReportModel_User user, int level,
			int fontSize, float rowHeight) {

		String userLabel = user.getFirstName() + " " + user.getLastName() + "(" + user.getUserName() + ")";
		PdfPTable userLine = getLine(userLabel, level, fontSize, rowHeight);
		subtable.addCell(userLine);

		return subtable;
	}

	private PdfPTable getLine(String line, int level, int fontSize, float rowHeight) {

		PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);
		subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		int colspan = COLUMN_NUMBER - level;

		pdfTableUtils.addEmptyCells(subTable, level, rowHeight);
		pdfTableUtils.addLeftPhraseRow(subTable, line, fontSize, colspan);
		pdfTableUtils.addEmptyCells(subTable, level, rowHeight);

		return subTable;
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
