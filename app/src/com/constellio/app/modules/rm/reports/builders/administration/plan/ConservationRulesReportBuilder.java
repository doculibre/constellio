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
import java.util.Map;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
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
	public static final int COLUMN_NUMBER = 30;

	public ConservationRulesReportBuilder(
			ConservationRulesReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();
		this.foldersLocator = foldersLocator;
	}

	@Override
	public String getFileExtension() {
		return pdfTableUtils.PDF;
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

		for (ConservationRulesReportModel_Rule rule : model.getRules()) {
			createSubTable(table, rule);
		}
		return table;
	}

	private void createSubTable(PdfPTable table, ConservationRulesReportModel_Rule rule) {

		PdfPTable subTable = new PdfPTable(COLUMN_NUMBER);

		subTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		pdfTableUtils.addPhraseCell(subTable, rule.getRuleNumber(), TITLE_FONT_SIZE, 5);
		pdfTableUtils.addPhraseRow(subTable, rule.getTitle(), TITLE_FONT_SIZE, COLUMN_NUMBER);
		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		pdfTableUtils.addPhraseRow(subTable, rule.getDescription(), TITLE_FONT_SIZE, COLUMN_NUMBER);
		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		pdfTableUtils.addPhraseCell(subTable, $("ConservationRulesReport.principalHolders"), TITLE_FONT_SIZE, 10);
		pdfTableUtils.addPhraseCell(subTable, $("ConservationRulesReport.copies"), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addPhraseCell(subTable, $("ConservationRulesReport.supportTypes"), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addPhraseCell(subTable, $("ConservationRulesReport.active"), TITLE_FONT_SIZE, 2);
		pdfTableUtils.addPhraseCell(subTable, $("ConservationRulesReport.semiActive"), TITLE_FONT_SIZE, 3);
		pdfTableUtils.addPhraseRow(subTable, $("ConservationRulesReport.inactive"), TITLE_FONT_SIZE, 2);

		pdfTableUtils.addEmptyCells(subTable, 5, 1);
		PdfPCell principalHoldersCell = new PdfPCell();
		principalHoldersCell.setColspan(10);
		principalHoldersCell.setPadding(0);
		principalHoldersCell.setBorder(Rectangle.NO_BORDER);
		PdfPTable principalHoldersTable = new PdfPTable(20);
		principalHoldersTable.setWidthPercentage(100);
		for (Map.Entry<String, String> principalHolder : rule.getAdministrativeUnits().entrySet()) {
			System.out.println(principalHolder.getKey());
			System.out.println(principalHolder.getValue());
			pdfTableUtils.addPhraseCell(principalHoldersTable, principalHolder.getKey(), TITLE_FONT_SIZE, 5);
			pdfTableUtils.addPhraseCell(principalHoldersTable, principalHolder.getValue(), TITLE_FONT_SIZE, 15);
		}
		principalHoldersCell.addElement(principalHoldersTable);
		subTable.addCell(principalHoldersCell);

		PdfPCell copiesCell = new PdfPCell();
		copiesCell.setColspan(15);
		copiesCell.setPadding(0);
		copiesCell.setBorder(Rectangle.NO_BORDER);
		PdfPTable copiesTable = new PdfPTable(15);
		copiesTable.setWidthPercentage(100);
		pdfTableUtils.addPhraseCell(copiesTable, $("ConservationRulesReport.principal"), TITLE_FONT_SIZE, 4);
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
			pdfTableUtils.addPhraseCell(copiesTable, stringBuffer.toString(), TITLE_FONT_SIZE, 4);
			pdfTableUtils.addPhraseCell(copiesTable, principalCopy.getActive(), TITLE_FONT_SIZE, 2);
			pdfTableUtils.addPhraseCell(copiesTable, principalCopy.getSemiActive(), TITLE_FONT_SIZE, 3);
			pdfTableUtils.addPhraseCell(copiesTable, principalCopy.getInactive(), TITLE_FONT_SIZE, 2);
			if (principalCopy.getObservations() != null) {
				pdfTableUtils.addPhraseRow(copiesTable, principalCopy.getObservations(), TITLE_FONT_SIZE, 15);
			}
			if (i != principalsCopiesNumber - 1) {
				pdfTableUtils.addEmptyCells(copiesTable, 4, copiesTable.getDefaultCell().getHeight());
			}
		}

		pdfTableUtils.addPhraseCell(copiesTable, $("ConservationRulesReport.secondary"), TITLE_FONT_SIZE, 4);
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
		pdfTableUtils.addPhraseCell(copiesTable, stringBuffer.toString(), TITLE_FONT_SIZE, 4);
		pdfTableUtils.addPhraseCell(copiesTable, secondaryCopy.getActive(), TITLE_FONT_SIZE, 2);
		pdfTableUtils.addPhraseCell(copiesTable, secondaryCopy.getSemiActive(), TITLE_FONT_SIZE, 3);
		pdfTableUtils.addPhraseCell(copiesTable, secondaryCopy.getInactive(), TITLE_FONT_SIZE, 2);
		if (secondaryCopy.getObservations() != null) {
			pdfTableUtils.addPhraseRow(copiesTable, secondaryCopy.getObservations(), TITLE_FONT_SIZE, 15);
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
