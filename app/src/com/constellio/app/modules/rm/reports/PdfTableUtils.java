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
package com.constellio.app.modules.rm.reports;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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

	public void addPhraseRow(PdfPTable table, String phrase, int fontSize, int colspan) {
		addPhraseCell(table, phrase, fontSize, colspan);
		table.completeRow();
	}

	public void addPhraseCell(PdfPTable table, String text, int fontSize, int colspan) {
		Phrase pdfPhrase = newPhrase(text, fontSize);
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
