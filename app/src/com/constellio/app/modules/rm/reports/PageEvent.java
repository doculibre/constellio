package com.constellio.app.modules.rm.reports;

import java.io.File;
import java.io.IOException;

import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class PageEvent extends PdfPageEventHelper {

	private FoldersLocator foldersLocator;

	private Phrase titlePhrase;
	private String footer;
	private PdfTemplate footerTemplate;
	private PdfTemplate headerTemplate;
	private Image logo;

	private final Font TITLE_FONT = FontFactory.getFont("Arial", 20);

	private final int PORTRAIT_WIDTH = 540;
	private final int LANDSCAPE_WIDTH = 800;

	private final int HEADER_PORTRAIT_YPOS = 820;
	private final int HEADER_LANDSCAPE_YPOS = 580;

	private int docWidth = PORTRAIT_WIDTH;
	private int header_yPos = HEADER_PORTRAIT_YPOS;

	public PageEvent(FoldersLocator fl) {
		if (fl == null) {
			throw new IllegalArgumentException("FoldersLocator must be not null");
		}
		this.foldersLocator = fl;
	}

	public void setTitle(String title) {
		Chunk chunkTitle = new Chunk(title);
		chunkTitle.setFont(TITLE_FONT);

		this.titlePhrase = new Paragraph(chunkTitle);
	}

	public void setLogo(String logo_filename) {
		try {
			if (!logo_filename.isEmpty()) {
				File reportResourceFolder = foldersLocator.getReportsResourceFolder();
				this.logo = Image.getInstance(reportResourceFolder.getAbsolutePath() + "/" + logo_filename);
			}
		} catch (BadElementException | IOException e) {
			e.printStackTrace();
		}
	}

	public void setLandscape() {
		docWidth = LANDSCAPE_WIDTH;
		header_yPos = HEADER_LANDSCAPE_YPOS;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public void onOpenDocument(PdfWriter writer, Document document) {
		footerTemplate = writer.getDirectContent().createTemplate(30, 16);
		headerTemplate = writer.getDirectContent().createTemplate(30, 16);
		createFirstPageHeader(writer);
	}

	public void onStartPage(PdfWriter writer, Document document) {
		configDocumentMargins(writer, document);
	}

	public void onEndPage(PdfWriter writer, Document document) {
		createFooterTable(writer);
	}

	public void onCloseDocument(PdfWriter writer, Document document) {
		ColumnText.showTextAligned(footerTemplate, Element.ALIGN_LEFT,
				new Phrase(String.valueOf(writer.getPageNumber() - 1)),
				2, 2, 0);
	}

	private void createFooterTable(PdfWriter writer) {
		PdfPTable table = new PdfPTable(3);
		try {
			configFooterTable(table);
			table.addCell(footer);

			addPages(table, writer.getPageNumber());
			addFooterTable(writer, table);
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	private void createFirstPageHeader(PdfWriter writer) {
		PdfPTable table = new PdfPTable(3);

		try {
			configFirstPageHeaderTable(table);
			addLogo(table);
			addTitle(table);
			addFirstPageHeader(writer, table);
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	//
	private void configDocumentMargins(PdfWriter writer, Document document) {
		if (writer.getCurrentPageNumber() == 1) {
			document.setMargins(0f, 0f, 20f, 20f);
		}
	}

	private void addPages(PdfPTable table, int nbPages) {
		Paragraph pFooter = new Paragraph(String.format("Page %d of", nbPages));
		PdfPCell footerCell = new PdfPCell(pFooter);
		footerCell.setBorder(Rectangle.TOP);
		footerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		table.addCell(footerCell);
	}

	private void addFooterTable(PdfWriter writer, PdfPTable table)
			throws BadElementException {
		PdfPCell cell = new PdfPCell(Image.getInstance(footerTemplate));
		cell.setBorder(Rectangle.TOP);
		//cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		//cell.setBackgroundColor(new BaseColor(255,0,0));

		table.addCell(cell);
		table.writeSelectedRows(0, -1, 34, 20, writer.getDirectContent());
	}

	private void configFooterTable(PdfPTable table)
			throws DocumentException {
		table.setWidths(new int[] { 49, 49, 2 });
		table.setTotalWidth(docWidth);
		table.setLockedWidth(true);
		table.getDefaultCell().setFixedHeight(20);
		table.getDefaultCell().setBorder(Rectangle.TOP);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
	}

	private void addFirstPageHeader(PdfWriter writer, PdfPTable table)
			throws BadElementException {
		PdfPCell cell = new PdfPCell(Image.getInstance(headerTemplate));
		cell.setBorder(Rectangle.BOTTOM);
		table.addCell(cell);
		table.writeSelectedRows(0, -1, 34, header_yPos, writer.getDirectContent());
	}

	private void addTitle(PdfPTable table) {
		PdfPCell titleCell = new PdfPCell(titlePhrase);
		titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		titleCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		titleCell.setBorder(Rectangle.BOTTOM);
		titleCell.setPaddingBottom(5f);
		titleCell.setColspan(2);
		table.addCell(titleCell);
	}

	private void addLogo(PdfPTable table) {
		PdfPCell imageCell = new PdfPCell(logo, true);
		imageCell.setBorder(Rectangle.BOTTOM);
		imageCell.setPaddingBottom(5f);
		table.addCell(imageCell);
	}

	private void configFirstPageHeaderTable(PdfPTable table)
			throws DocumentException {
		table.setWidths(new int[] { 13, 10, 13 });
		table.setTotalWidth(docWidth);
		table.setLockedWidth(true);
		table.getDefaultCell().setFixedHeight(20);
		table.getDefaultCell().setBorder(Rectangle.BOTTOM);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
	}

	/*private PdfPCell addImageCell(Image image, boolean fit, int colspan) {
		PdfPCell cell = new PdfPCell(image, fit);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		return cell;
	}*/
}
