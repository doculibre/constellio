package com.constellio.app.modules.rm.reports;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PdfTableUtils {

	public static final float ROW_HEIGHT = 20f;
	public static final String PDF = "pdf";

	public void addEmptyCells(PdfPTable table, int number, float rowHeight) {
		for (int i = 0; i < number; i++) {
			PdfPCell cell = invisibleCell();
			cell.setFixedHeight(rowHeight);
			table.addCell(cell);
		}
	}

	public PdfPCell invisibleCell() {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		return cell;
	}

	public void addEmptyRows(PdfPTable table, int number, float rowHeight) {
		for (int i = 0; i < number; i++) {
			PdfPCell cell = invisibleCell();
			cell.setFixedHeight(rowHeight);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			table.completeRow();
		}
	}

	public void addCenterPhraseRow(PdfPTable table, String phrase, int fontSize) {
		addCenterPhraseCell(table, phrase, fontSize, table.getNumberOfColumns());
		table.completeRow();
	}

	public void addCenterPhraseCellWithFont(PdfPTable table, String text, Font font) {
		Phrase pdfPhrase = new Phrase(text, font);
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(table.getNumberOfColumns());
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
	}

	public void addCenterPhraseCell(PdfPTable table, String text, int fontSize, int colspan) {
		Phrase pdfPhrase = newPhrase(text, fontSize);
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
	}

	public void addLeftPhraseRow(PdfPTable table, String phrase, int fontSize, int colspan) {
		addLeftPhraseCell(table, phrase, fontSize, colspan);
		table.completeRow();
	}

	public void addLeftPhraseCell(PdfPTable table, String text, int fontSize, int colspan) {
		Phrase pdfPhrase = newPhrase(text, fontSize);
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);
	}

	public void addLeftPhraseCell(PdfPTable table, String text, int fontSize, int colspan, boolean border) {
		Phrase pdfPhrase = newPhrase(text, fontSize);
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		if (border) {
			cell.setBorder(Rectangle.BOX);
		}
		table.addCell(cell);
	}

	public Phrase newPhrase(String text, int fontSize) {
		return new Phrase(text, getFont(fontSize));
	}

	private Font getFont(float fontSize) {
		Font font = new Font();
		font.setSize(fontSize);
		return font;
	}
}
