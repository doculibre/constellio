package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel.DocumentsCertificateReportModel_Document;
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

public class DocumentsCertificateReportWriter implements ReportWriter {

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 70f;
	public static final float MARGIN_BOTTOM = 20f;
	public static final int FONT_SIZE = 8;
	public static final int COLUMN_NUMBER = 100;

	private DocumentsCertificateReportModel model;

	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public DocumentsCertificateReportWriter(DocumentsCertificateReportModel model, FoldersLocator foldersLocator) {
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

		addMainInformations(table);
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		addReportHeader(table);
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());
		pdfTableUtils.addEmptyCells(table, 100, cell.getHeight());

		for (DocumentsCertificateReportModel_Document document : model.getDocuments()) {
			addRow(document, table);
		}

		return table;
	}

	private void addMainInformations(PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table,
						$("CertificateReport.certificateCreationDate") + " : " + model.getCertificateCreationDate(), FONT_SIZE,
						40, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.destructionDate") + " : " + model.getDestructionDate(), FONT_SIZE,
						30, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, "", FONT_SIZE, 30,
						model.hasCellBorder());//$("CertificateReport.hash") + " : " + model.getHash()
	}

	private void addReportHeader(PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table, "#", FONT_SIZE, 10, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.title"), FONT_SIZE, 20, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.md5"), FONT_SIZE, 15, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.filename"), FONT_SIZE, 15, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.folder"), FONT_SIZE, 20, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.retentionRule"), FONT_SIZE, 10, model.hasCellBorder());
		pdfTableUtils
				.addLeftPhraseCell(table, $("CertificateReport.principalCopyRetentionRule"), FONT_SIZE, 10,
						model.hasCellBorder());
	}

	private void addRow(DocumentsCertificateReportModel_Document document, PdfPTable table) {

		pdfTableUtils
				.addLeftPhraseCell(table, document.getId(), FONT_SIZE, 10);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getTitle(), FONT_SIZE, 20);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getMd5(), FONT_SIZE, 15);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getFilename(), FONT_SIZE, 15);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getFolder(), FONT_SIZE, 20);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getRetentionRuleCode(), FONT_SIZE, 10);
		pdfTableUtils
				.addLeftPhraseCell(table, document.getPrincipalCopyRetentionRule(), FONT_SIZE, 10);
	}
}
