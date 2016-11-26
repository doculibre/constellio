package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportModel.DecommissioningListReportModel_Folder;
import com.constellio.app.ui.framework.reports.ReportWriter;
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

public class DecommissioningListReportWriter implements ReportWriter {

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 70f;
	public static final float MARGIN_BOTTOM = 20f;
	public static final int FONT_SIZE = 8;
	public static final int COLUMN_NUMBER = 100;

	private DecommissioningListReportModel model;

	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public DecommissioningListReportWriter(DecommissioningListReportModel model, FoldersLocator foldersLocator) {
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
			document.add(createReport());
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {

		PageEvent pageEvent = new PageEvent(foldersLocator);

		pageEvent.setTitle(model.getTitle());
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}

	private PdfPTable createReport() {

		PdfPTable table = new PdfPTable(COLUMN_NUMBER);
		table.setWidthPercentage(90);

		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);

		addDecommissioningListTableInfo(table);
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		addDecommissioningListFolderTableHeader(table);
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());

		for (DecommissioningListReportModel_Folder folder : model.getFolders()) {
			addFolderRow(folder, table);
		}

		return table;
	}

	private void addDecommissioningListTableInfo(PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table, model.getDecommissioningListTitle(), FONT_SIZE, 40);
		pdfTableUtils
				.addLeftPhraseCell(table, model.getDecommissioningListType(), FONT_SIZE, 30);
		pdfTableUtils
				.addLeftPhraseCell(table, model.getDecommissioningListAdministrativeUnitCodeAndTitle(), FONT_SIZE, 30);
	}

	private void addDecommissioningListFolderTableHeader(PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table, "#", FONT_SIZE, 10);
		pdfTableUtils
				.addLeftPhraseCell(table, $("DecommissioningListReport.folderTitle"), FONT_SIZE, 40);
		pdfTableUtils
				.addLeftPhraseCell(table, $("DecommissioningListReport.folderRetentionRule"), FONT_SIZE, 25);
		pdfTableUtils
				.addLeftPhraseCell(table, $("DecommissioningListReport.folderCategory"), FONT_SIZE, 25);

	}

	private void addFolderRow(DecommissioningListReportModel_Folder folder, PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table, folder.getId(), FONT_SIZE, 10);
		pdfTableUtils
				.addLeftPhraseCell(table, folder.getTitle(), FONT_SIZE, 40);
		pdfTableUtils
				.addLeftPhraseCell(table, folder.getRetentionRule(), FONT_SIZE, 25);
		pdfTableUtils
				.addLeftPhraseCell(table, folder.getCategory(), FONT_SIZE, 25);
	}
}
