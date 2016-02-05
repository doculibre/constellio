package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ConservationRulesReportBuilder implements ReportBuilder {

	ConservationRulesReportModel model;
	PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 70f;
	public static final float MARGIN_BOTTOM = 20f;
	public static final int TABLE_WIDTH_PERCENTAGE = 90;
	public static final int TITLE_FONT_SIZE = 8;
	public static final int ADMINISTRATIVE_UNIT_TITLE_FONT_SIZE = TITLE_FONT_SIZE + 2;
	public static final int COLUMN_NUMBER = 30;

	public ConservationRulesReportBuilder(
			ConservationRulesReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();
		this.foldersLocator = foldersLocator;
	}

	@Override
	public String getFileExtension() {
		return PdfTableUtils.PDF;
	}

	@Override
	public void build(OutputStream output)
			throws IOException {
		Document document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			configPageEvents(writer);
			document.open();
			document.add(createReport(writer));
			document.close();
		} catch (DocumentException e) {
			//TODO Exception
			throw new RuntimeException(e);
		}
	}

	private PdfPTable createReport(PdfWriter writer) {
		PdfPTable table = new PdfPTable(1);

		table.getDefaultCell().setBorder(Rectangle.TOP);
		table.getDefaultCell().setBorderWidth(1f);
		table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
		table.setExtendLastRow(true);

		if (model.isByAdministrativeUnit()) {
			for (Entry<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> adminUnitRulesEntry : model
					.getRulesByAdministrativeUnitMap().entrySet()) {
				PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);
				subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				pdfTableUtils
						.addLeftPhraseCell(subTable, adminUnitRulesEntry.getKey().getCode(), ADMINISTRATIVE_UNIT_TITLE_FONT_SIZE,
								10);
				pdfTableUtils
						.addLeftPhraseCell(subTable, adminUnitRulesEntry.getKey().getTitle(), ADMINISTRATIVE_UNIT_TITLE_FONT_SIZE,
								20);
				table.addCell(subTable);
				for (ConservationRulesReportModel_Rule rule : adminUnitRulesEntry.getValue()) {
					createSubTable(table, rule);
				}
			}
		} else {
			for (ConservationRulesReportModel_Rule rule : model.getRules()) {
				createSubTable(table, rule);
			}
		}
		return table;
	}

	private void createSubTable(PdfPTable table, ConservationRulesReportModel_Rule rule) {

		PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);

		subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		pdfTableUtils.addLeftPhraseCell(subTable, rule.getRuleNumber(), TITLE_FONT_SIZE, 5);
		pdfTableUtils.addLeftPhraseRow(subTable, rule.getTitle(), TITLE_FONT_SIZE, COLUMN_NUMBER);
		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		pdfTableUtils.addLeftPhraseRow(subTable, rule.getDescription(), TITLE_FONT_SIZE, COLUMN_NUMBER);
		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		pdfTableUtils.addLeftPhraseCell(subTable, $("ConservationRulesReport.principalHolders"), TITLE_FONT_SIZE, 10);
		pdfTableUtils.addLeftPhraseCell(subTable, $("ConservationRulesReport.copies"), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addLeftPhraseCell(subTable, $("ConservationRulesReport.supportTypes"), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addLeftPhraseCell(subTable, $("ConservationRulesReport.active"), TITLE_FONT_SIZE, 2);
		pdfTableUtils.addLeftPhraseCell(subTable, $("ConservationRulesReport.semiActive"), TITLE_FONT_SIZE, 3);
		pdfTableUtils.addLeftPhraseRow(subTable, $("ConservationRulesReport.inactive"), TITLE_FONT_SIZE, 2);

		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		PdfPCell principalHoldersCell = new PdfPCell();
		principalHoldersCell.setColspan(10);
		principalHoldersCell.setPadding(0);
		principalHoldersCell.setBorder(Rectangle.NO_BORDER);
		PdfPTable principalHoldersTable = new PdfPTable(20);
		principalHoldersTable.setWidthPercentage(100);
		for (Map.Entry<String, String> principalHolder : rule.getAdministrativeUnits().entrySet()) {
			pdfTableUtils.addLeftPhraseCell(principalHoldersTable, principalHolder.getKey(), TITLE_FONT_SIZE, 5);
			pdfTableUtils.addLeftPhraseCell(principalHoldersTable, principalHolder.getValue(), TITLE_FONT_SIZE, 15);
		}
		principalHoldersCell.addElement(principalHoldersTable);
		subTable.addCell(principalHoldersCell);

		PdfPCell copiesCell = new PdfPCell();
		copiesCell.setColspan(15);
		copiesCell.setPadding(0);
		copiesCell.setBorder(Rectangle.NO_BORDER);
		PdfPTable copiesTable = new PdfPTable(15);
		copiesTable.setWidthPercentage(100);
		pdfTableUtils.addLeftPhraseCell(copiesTable, $("ConservationRulesReport.principal"), TITLE_FONT_SIZE, 4);
		int principalsCopiesNumber = rule.getPrincipalsCopies().size();
		for (int i = 0; i < principalsCopiesNumber; i++) {
			//for (ConservationRulesReportModel_Copy principalCopy : rule.getPrincipalsCopies()) {
			ConservationRulesReportModel_Copy principalCopy = rule.getPrincipalsCopies().get(i);
			int quantity = principalCopy.getSupportTypes().size();
			StringBuffer stringBuffer = new StringBuffer();
			for (int j = 0; j < quantity; j++) {
				stringBuffer.append(principalCopy.getSupportTypes().get(j));
				if (j != quantity - 1) {
					stringBuffer.append(", ");
				}
			}
			pdfTableUtils.addLeftPhraseCell(copiesTable, stringBuffer.toString(), TITLE_FONT_SIZE, 4);
			pdfTableUtils.addLeftPhraseCell(copiesTable, principalCopy.getActive(), TITLE_FONT_SIZE, 2);
			pdfTableUtils.addLeftPhraseCell(copiesTable, principalCopy.getSemiActive(), TITLE_FONT_SIZE, 3);
			pdfTableUtils.addLeftPhraseCell(copiesTable, principalCopy.getInactive(), TITLE_FONT_SIZE, 2);
			if (principalCopy.getObservations() != null) {
				pdfTableUtils.addLeftPhraseRow(copiesTable, principalCopy.getObservations(), TITLE_FONT_SIZE, 15);
			}
			if (i != principalsCopiesNumber - 1) {
				pdfTableUtils.addEmptyCells(copiesTable, 4, copiesTable.getDefaultCell().getHeight());
			}
		}

		pdfTableUtils.addLeftPhraseCell(copiesTable, $("ConservationRulesReport.secondary"), TITLE_FONT_SIZE, 4);
		//TODO verify null
		ConservationRulesReportModel_Copy secondaryCopy = rule.getSecondaryCopy();
		int quantity = secondaryCopy.getSupportTypes().size();
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < quantity; i++) {
			stringBuffer.append(secondaryCopy.getSupportTypes().get(i));
			if (i != quantity - 1) {
				stringBuffer.append(", ");
			}
		}
		pdfTableUtils.addLeftPhraseCell(copiesTable, stringBuffer.toString(), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addLeftPhraseCell(copiesTable, secondaryCopy.getActive(), TITLE_FONT_SIZE, 2);
		pdfTableUtils.addLeftPhraseCell(copiesTable, secondaryCopy.getSemiActive(), TITLE_FONT_SIZE, 3);
		pdfTableUtils.addLeftPhraseCell(copiesTable, secondaryCopy.getInactive(), TITLE_FONT_SIZE, 2);
		if (secondaryCopy.getObservations() != null) {
			pdfTableUtils.addLeftPhraseRow(copiesTable, secondaryCopy.getObservations(), TITLE_FONT_SIZE, 15);
		}

		copiesCell.addElement(copiesTable);
		subTable.addCell(copiesCell);
		pdfTableUtils.addEmptyRows(subTable, 1, subTable.getDefaultCell().getHeight());
		table.addCell(subTable);

	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {
		PageEvent pageEvent = new PageEvent(foldersLocator);

		pageEvent.setTitle(model.getTitle());
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}
}
