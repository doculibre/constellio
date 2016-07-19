package com.constellio.app.modules.rm.reports.builders.labels;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.labels.ImageLabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLabel;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportModel;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class LabelsReportBuilder implements ReportBuilder {

	private LabelsReportModel model;
	private PdfTableUtils tableUtils;

	public LabelsReportBuilder(LabelsReportModel model) {
		this.model = model;
		this.tableUtils = new PdfTableUtils();
	}

	public String getFileExtension() {
		return "pdf";
	}

	public void build(OutputStream output)
			throws IOException {
		LabelsReportLayout layout = model.getLayout();
		Document document = new Document(layout.getPageSize(), layout.getLeftMargin(), layout.getRightMargin(),
				layout.getTopMargin(), layout.getBottomMargin());
		try {
			PdfWriter.getInstance(document, output);
			document.open();
			document.add(createPrintableLabels(layout));
			document.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private PdfPTable createPrintableLabels(LabelsReportLayout layout) {

		PdfPTable labels = new PdfPTable(layout.getNumberOfColumns());
		labels.setWidthPercentage(100f);

		float labelHeight = getLabelHeight(layout);

		float labelWidth = getLabelWidth(layout);

		for (LabelsReportLabel label : model.getLabelsReportLabels()) {
			labels.addCell(createPrintableLabel(label, labelWidth, labelHeight));
		}

		addExtraEmptyLabelIfOddNumberOfLabels(labels);
		return labels;
	}

	private float getLabelHeight(LabelsReportLayout layout) {
		float totalTopBottomMargins = layout.getTopMargin() + layout.getBottomMargin();
		float labelHeight = (layout.getPageSize().getHeight() - totalTopBottomMargins) / layout.getNumberOfRows();
		return labelHeight;
	}

	private float getLabelWidth(LabelsReportLayout layout) {
		float totalHorizontalSpaces = layout.getLeftMargin() + layout.getRightMargin();
		float labelWidth = (layout.getPageSize().getWidth() - totalHorizontalSpaces) / layout.getNumberOfColumns();
		return labelWidth;
	}

	private PdfPCell createPrintableLabel(LabelsReportLabel label, float labelWidth, float labelHeight) {
		//		int numColumns = (int) Math.ceil(labelWidth / 10);
		int numColumns = model.getColumnsNumber();

		PdfPTable printableLabel = new PdfPTable(numColumns);

		int border;
		if (model.isPrintBorders()) {
			border = Rectangle.BOX;
		} else {
			border = Rectangle.NO_BORDER;
		}
		printableLabel.getDefaultCell().setBorder(border);

		//		int numRows = approximateNumberOfRowsBasedOnHeight10PerRow(labelHeight);
		int numRows = model.getRowsNumber();

		float rowHeight = calculateExactRowHeight(labelHeight, numRows);

		for (int rowNumber = 0; rowNumber < numRows; rowNumber++) {
			List<LabelsReportField> fields = label.getFieldsInRow(rowNumber);
			if (fields.isEmpty()) {
				tableUtils.addEmptyRows(printableLabel, 1, rowHeight);
			} else {
				addRowWithFields(printableLabel, rowHeight, fields);
			}
		}

		PdfPCell printableLabelField = new PdfPCell(printableLabel);
		printableLabelField.setBorder(border);
		removeBordersIfLabelIsBlank(label, printableLabelField);
		printableLabelField.setUseAscender(true);
		return printableLabelField;
	}

	float calculateExactRowHeight(float labelHeight, int numRows) {
		return labelHeight / numRows;
	}

	private void removeBordersIfLabelIsBlank(LabelsReportLabel label, PdfPCell printableLabelField) {
		if (label.getFields().isEmpty()) {
			printableLabelField.setBorder(Rectangle.NO_BORDER);
		}
	}

	private void addRowWithFields(PdfPTable printableLabel, float rowHeight, List<LabelsReportField> fields) {
		Iterator<LabelsReportField> fieldsIterator = fields.iterator();
		int lastFieldEnd = 0;
		while (fieldsIterator.hasNext()) {
			LabelsReportField field = fieldsIterator.next();

			tableUtils.addEmptyCells(printableLabel, field.positionX - lastFieldEnd, rowHeight);

			printableLabel.addCell(createLabelField(field, rowHeight));

			lastFieldEnd = field.positionX + field.width;
		}
		printableLabel.completeRow();
	}

	private Image createImage(ImageLabelsReportField field, float rowHeight) {
		String imagePath = field.getValue();
		Image image;
		try {
			image = Image.getInstance(imagePath);
			//			image.scalePercent(20f);
		} catch (BadElementException | IOException e) {
			throw new RuntimeException(e);
		}
		return image;
	}

	private PdfPCell createLabelField(LabelsReportField field, float rowHeight) {
		PdfPCell fieldCell;
		if (field instanceof ImageLabelsReportField) {
			Image image = createImage((ImageLabelsReportField) field, rowHeight);
			fieldCell = new PdfPCell(image, true);
			fieldCell.setBorder(Rectangle.NO_BORDER);
			fieldCell.setColspan(field.width);
		} else {
			Phrase phrase = new Phrase(field.getValue(), field.getFont().getFont());
			fieldCell = new PdfPCell(phrase);
			fieldCell.setColspan(field.width);
			fieldCell.setRowspan(field.height);
			fieldCell.setFixedHeight(rowHeight);
			int border;
			if (model.isPrintBorders()) {
				border = Rectangle.BOX;
			} else {
				border = Rectangle.NO_BORDER;
			}
			fieldCell.setBorder(border);
			fieldCell.setHorizontalAlignment(field.horizontalAlignment);
			fieldCell.setVerticalAlignment(field.verticalAlignment);
		}
		return fieldCell;
	}

	private void addExtraEmptyLabelIfOddNumberOfLabels(PdfPTable labels) {
		if (model.getLabelsReportLabels().size() % 2 == 1) {
			PdfPCell emptyCell = labels.getDefaultCell();
			int border;
			if (model.isPrintBorders()) {
				border = Rectangle.BOX;
			} else {
				border = Rectangle.NO_BORDER;
			}
			emptyCell.setBorder(border);
			labels.addCell(emptyCell);
		}
	}

	private int approximateNumberOfRowsBasedOnHeight10PerRow(float labelHeight) {
		return (int) Math.floor(labelHeight / 10);
	}
}
