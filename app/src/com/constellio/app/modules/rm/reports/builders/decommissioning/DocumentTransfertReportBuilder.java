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
package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Calendar;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Document;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Identification;
import com.constellio.app.modules.rm.reports.model.decommissioning.ReportBooleanField;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

//TODO : Bordures en pointillé
//TODO : revoir style du header/footer

public class DocumentTransfertReportBuilder implements ReportBuilder {

	private final static Logger LOG = Logger.getLogger(DocumentTransfertReportBuilder.class.getName());

	private static final String TEMP_FILE_RESOURCE = "DocumentVersementReportBuilder-tempFile";
	private static final String TEMP_FILE_IN_RESOURCE = "DocumentVersementReportBuilder-tempFileIn";

	private final Font fontTitle = FontFactory.getFont("Arial", 8, Font.BOLD);
	private final Font fontHeader = FontFactory.getFont("Arial", 8, Font.BOLD);
	private final Font fontValue = FontFactory.getFont("Arial", 8);

	private final BaseColor NO_COLOR = new BaseColor(255, 255, 255);
	private final BaseColor CELL_BACKGROUND_COLOR = new BaseColor(217, 217, 217);

	private DocumentReportModel model;

	private IOServices ioServices;

	private FoldersLocator foldersLocator;

	public DocumentTransfertReportBuilder(DocumentReportModel model, IOServices ioServices, FoldersLocator foldersLocator) {
		this.model = model;
		this.ioServices = ioServices;
		this.foldersLocator = foldersLocator;

	}

	public String getFileExtension() {
		return "pdf";
	}

	@Override
	public void build(OutputStream output)
			throws IOException {
		// TODO Auto-generated method stub
		File tempFile = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE);
		InputStream tempFileInputStream = null;
		try {
			build(tempFile);
			tempFileInputStream = ioServices.newBufferedFileInputStream(tempFile, TEMP_FILE_IN_RESOURCE);
			IOUtils.copy(tempFileInputStream, output);

		} finally {
			ioServices.closeQuietly(tempFileInputStream);
			ioServices.deleteQuietly(tempFile);
		}
	}

	public void build(File outputFile)
			throws IOException {

		Document document = new Document(PageSize.A4, 0f, 0f, 70f, 80f);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, baos);

			writer.setPageEvent(new TableHeader());
			writer.setPageEvent(new TableFooter());

			document.open();
			document.add(createReport(writer, document));
			document.close();

			FileUtils.writeByteArrayToFile(outputFile, baos.toByteArray());
			writePageNumbers(outputFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void writePageNumbers(File outputFile)
			throws IOException, DocumentException {

		PdfReader reader = new PdfReader(outputFile.getAbsolutePath());
		int numberOfPages = reader.getNumberOfPages();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfStamper stamper = new PdfStamper(reader, baos);

		for (int pageIndex = 1; pageIndex <= numberOfPages; ++pageIndex) {

			Chunk chunkPageNumber = new Chunk(String.format($("DocumentTransfertReport.FooterPage"), pageIndex, numberOfPages));
			chunkPageNumber.setFont(fontValue);

			Paragraph paragraph = new Paragraph(chunkPageNumber);
			paragraph.setAlignment(Element.ALIGN_RIGHT);

			PdfPCell cell = new PdfPCell();
			cell.setFixedHeight(20);
			cell.setBorder(Rectangle.NO_BORDER);

			cell.addElement(paragraph);

			PdfPTable table = new PdfPTable(1);
			table.setTotalWidth(90);
			table.addCell(cell);

			table.writeSelectedRows(0, -1, 490, 60, stamper.getOverContent(pageIndex));
		}

		stamper.close();
		reader.close();

		FileUtils.writeByteArrayToFile(outputFile, baos.toByteArray());
		baos.close();
	}

	private PdfPTable createReport(PdfWriter writer, Document document)
			throws BadElementException, MalformedURLException, IOException {

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(99);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

		table.addCell(createIdentificationTable());
		table.addCell(createReservedTable());

		addEmptyRow(table);

		PdfPTable calendarTable = createCalendarTable();
		PdfPCell calendarTableCell = new PdfPCell(calendarTable);
		calendarTableCell.setColspan(2);
		table.addCell(calendarTableCell);

		addEmptyRow(table);

		PdfPTable documentTable = createDocumentTable();
		PdfPCell documentTableCell = new PdfPCell(documentTable);
		documentTableCell.setColspan(2);
		table.addCell(documentTableCell);

		return table;
	}

	private PdfPTable createIdentificationTable() {

		DocumentTransfertModel_Identification modelIdentification = model.getIdentificationModel();

		PdfPTable identificationTable = new PdfPTable(2);
		identificationTable.setWidthPercentage(50);

		float[] columnWidths = new float[] { 1.10f, 0.90f };

		try {
			identificationTable.setWidths(columnWidths);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}

		addIdentificationTableHeader(identificationTable);
		addIdentificationTableTop(identificationTable, modelIdentification);
		addIdentificationTableAdress(identificationTable, modelIdentification);
		addIdentificationTableBottom(identificationTable, modelIdentification);

		return identificationTable;
	}

	private void addIdentificationTableHeader(PdfPTable table) {
		String title = $("DocumentTransfertReport.IdentificationTableTitle");
		PdfPCell titleCell = newTitleCell(title);
		titleCell.setColspan(2);
		table.addCell(titleCell);
		table.completeRow();
	}

	private void addIdentificationTableTop(PdfPTable table, DocumentTransfertModel_Identification modelId) {
		int alignment = Element.ALIGN_LEFT;

		String title = $("DocumentTransfertReport.IdentificationSentDate");
		String value = getFormatedDate(modelId.getSentDate());
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.IdentificationBoxNumber");
		value = modelId.getBoxNumber();
		addDataCell(table, title, value, alignment);

		table.completeRow();

		title = $("DocumentTransfertReport.IdentificationOrganismName");
		value = modelId.getOrganisationName();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.IdentificationOrganismNumber");
		value = modelId.getPublicOrganisationNumber();
		addDataCell(table, title, value, alignment);

		table.completeRow();

		title = $("DocumentTransfertReport.IdentificationClassificationSpace");
		value = modelId.getEspaceClassement();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.IdentificationClassificationSpaceCode");
		value = modelId.getCodeEspaceClassement();
		addDataCell(table, title, value, alignment);

		table.completeRow();
	}

	private void addIdentificationTableAdress(PdfPTable table, DocumentTransfertModel_Identification modelId) {
		PdfPCell cell = newDataCell($("DocumentTransfertReport.IdentificationAdministrativeUnitAdress"),
				modelId.getAdministrationAddress(), Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setMinimumHeight(70f);
		table.addCell(cell);
		table.completeRow();
	}

	private void addIdentificationTableBottom(PdfPTable table, DocumentTransfertModel_Identification modelId) {
		int alignment = Element.ALIGN_LEFT;

		String title = $("DocumentTransfertReport.IdentificationDocumentResponsibleName");
		String value = modelId.getResponsible();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.IdentificationDocumentResponsibleFunction");
		value = modelId.getFunction();
		addDataCell(table, title, value, alignment);

		table.completeRow();

		title = $("DocumentTransfertReport.IdentificationDocumentResponsibleEmail");
		value = modelId.getEmail();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.IdentificationDocumentResponsiblePhone");
		value = modelId.getPhoneNumber();
		addDataCell(table, title, value, alignment);

		table.completeRow();
	}

	private PdfPTable createReservedTable() {
		PdfPTable reservedTable = new PdfPTable(3);
		reservedTable.setWidthPercentage(50);

		addReservedTableHeader(reservedTable);
		addReservedTableTop(reservedTable);
		addReservedTableBottom(reservedTable);

		return reservedTable;
	}

	private void addReservedTableHeader(PdfPTable table) {
		PdfPCell titleCell = newTitleCell($("DocumentTransfertReport.DocumentTransfertTableTitle"));
		titleCell.setColspan(3);
		table.addCell(titleCell);
		table.completeRow();
	}

	private void addReservedTableTop(PdfPTable table) {
		float height = 50f;
		int alignment = Element.ALIGN_CENTER;

		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertDate"), 1, height, alignment));
		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertVerifiedBy"), 1, height, alignment));
		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertContainerNumber"), 1, height, alignment));

		table.completeRow();
	}

	private void addReservedTableBottom(PdfPTable table) {
		int alignment = Element.ALIGN_LEFT;

		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertDeposit"), 3, 60f, alignment));
		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertAdress"), 3, 70f, alignment));
		table.addCell(getReservedTableCell($("DocumentTransfertReport.DocumentTransfertDescription"), 3, 85f, alignment));
	}

	private PdfPCell getReservedTableCell(String title, int colspan, float minHeight, int alignment) {
		PdfPCell cell = newHeaderCell(title, alignment);
		cell.setColspan(colspan);
		cell.setMinimumHeight(minHeight);

		return cell;
	}

	private PdfPTable createCalendarTable()
			throws BadElementException, MalformedURLException, IOException {

		DocumentTransfertModel_Calendar modelCalendar = model.getCalendarModel();
		PdfPTable calendarTable = new PdfPTable(4);

		addCalendarTableHeader(calendarTable);
		addCalendarTableFirstRow(calendarTable, modelCalendar);
		addCalendarTableSecondRow(calendarTable, modelCalendar);
		addCalendarTableThirdRow(calendarTable, modelCalendar);
		addCalendarTableFourthRow(calendarTable, modelCalendar);

		return calendarTable;
	}

	private void addCalendarTableHeader(PdfPTable table) {
		PdfPCell titleCell = newTitleCell($("DocumentTransfertReport.ConservationCalendarTableTitle"));
		titleCell.setColspan(4);
		table.addCell(titleCell);
		table.completeRow();
	}

	private void addCalendarTableFirstRow(PdfPTable table, DocumentTransfertModel_Calendar modelCalendar) {
		int alignment = Element.ALIGN_CENTER;

		String title = $("DocumentTransfertReport.ConservationCalendarNumber");
		String value = modelCalendar.getCalendarNumber();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.ConservationCalendarRuleNumber");
		value = modelCalendar.getRuleNumber();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.ConservationCalendarSemiactiveRange");
		value = modelCalendar.getSemiActiveRange();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.ConservationCalendarDispositionYear");
		value = modelCalendar.getDispositionYear();
		addDataCell(table, title, value, alignment);

		table.completeRow();
	}

	private void addCalendarTableSecondRow(PdfPTable table, DocumentTransfertModel_Calendar modelCalendar) {
		int alignment = Element.ALIGN_CENTER;

		PdfPCell dispoMode = newDataCell($("DocumentTransfertReport.ConservationCalendarDispositionModeTitle"), "", alignment);
		PdfPCell labelSupport = newDataCell($("DocumentTransfertReport.ConservationCalendarInformationSupportTitle"), "",
				alignment);

		dispoMode.setColspan(2);
		labelSupport.setColspan(2);

		table.addCell(dispoMode);
		table.addCell(labelSupport);
		table.completeRow();
	}

	private void addCalendarTableThirdRow(PdfPTable table, DocumentTransfertModel_Calendar modelCalendar) {
		PdfPCell dispoModeCell;
		if (!modelCalendar.getConservationDisposition().isEmpty()) {
			dispoModeCell = getCalendarDispoMode(modelCalendar.getConservationDisposition());
		} else {
			dispoModeCell = new PdfPCell();
		}
		PdfPCell supportCell = getCalendarSupport(modelCalendar.getSupports());

		dispoModeCell.setColspan(2);
		supportCell.setColspan(2);

		table.addCell(dispoModeCell);
		table.addCell(supportCell);
		table.completeRow();
	}

	private void addCalendarTableFourthRow(PdfPTable table, DocumentTransfertModel_Calendar modelCalendar) {
		int alignment = Element.ALIGN_LEFT;

		String title = $("DocumentTransfertReport.ConservationCalendarDispositionYear");
		String value = modelCalendar.getQuantity();
		addDataCell(table, title, value, alignment);

		title = $("DocumentTransfertReport.ConservationCalendarExtremeDate");
		value = modelCalendar.getExtremeDate();
		addDataCell(table, title, value, alignment);

		addEmptyCells(table, 2);
		table.completeRow();
	}

	private PdfPTable createDocumentTable() {

		PdfPTable table = new PdfPTable(5);

		float[] columnWidths = new float[] { 0.6f, 0.6f, 0.3f, 3.0f, 0.5f };

		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}

		addDocumentTableHeader(table);

		List<DocumentTransfertModel_Document> listDocuments = model.getDocumentList();

		for (DocumentTransfertModel_Document doc : listDocuments) {
			addDocumentRow(doc, table);
		}

		return table;
	}

	private PdfPCell getDocumentTableYearsHeaderCell() {

		PdfPTable tableYear = new PdfPTable(2);
		int alignment = Element.ALIGN_CENTER;

		PdfPCell yearLabel = newHeaderCell($("DocumentTransfertReport.DocumentYear"), alignment);
		yearLabel.setColspan(2);
		tableYear.addCell(yearLabel);
		tableYear.completeRow();

		PdfPCell fromLabel = newHeaderCell($("DocumentTransfertReport.DocumentYearFrom"), alignment);
		PdfPCell toLabel = newHeaderCell($("DocumentTransfertReport.DocumentYearTo"), alignment);

		tableYear.addCell(fromLabel);
		tableYear.addCell(toLabel);
		tableYear.completeRow();

		PdfPCell tableYearCell = new PdfPCell(tableYear);
		tableYearCell.setPadding(0f);

		return tableYearCell;
	}

	private PdfPCell getDocumentTableYearsCell(String year1, String year2) {

		PdfPTable year = new PdfPTable(2);

		PdfPCell fromValue = newDataCell("", year1, Element.ALIGN_CENTER);
		PdfPCell toValue = newDataCell("", year2, Element.ALIGN_CENTER);

		year.addCell(fromValue);
		year.addCell(toValue);

		PdfPCell yearCell = new PdfPCell(year);
		yearCell.setPadding(0f);

		return yearCell;
	}

	private void addDocumentTableHeader(PdfPTable table) {
		int alignment = Element.ALIGN_CENTER;

		table.addCell(newHeaderCell($("DocumentTransfertReport.DocumentCode"), alignment));
		table.addCell(newHeaderCell($("DocumentTransfertReport.DocumentRuleNumber"), alignment));
		table.addCell(newHeaderCell($("DocumentTransfertReport.DocumentReferenceID"), alignment));
		table.addCell(newHeaderCell($("DocumentTransfertReport.DocumentTitle"), alignment));
		table.addCell(getDocumentTableYearsHeaderCell());

		table.completeRow();
	}

	private void addDocumentRow(DocumentTransfertModel_Document doc, PdfPTable table) {
		table.addCell(newDataCell("", doc.getCode(), Element.ALIGN_CENTER));
		table.addCell(newDataCell("", doc.getDelayNumber(), Element.ALIGN_CENTER));
		table.addCell(newDataCell("", doc.getReferenceId(), Element.ALIGN_CENTER));
		table.addCell(newDataCell("", doc.getTitle(), Element.ALIGN_LEFT));
		table.addCell(getDocumentTableYearsCell(doc.getStartingYear(), doc.getEndingYear()));
		table.completeRow();
	}

	private PdfPCell getCalendarDispoMode(List<ReportBooleanField> options) {

		PdfPTable dispoModeChecks = new PdfPTable(1);

		boolean destruction = options.get(0).getValue();
		boolean tri = options.get(1).getValue();
		boolean construction = options.get(2).getValue();

		PdfPCell destructionCell = getCheckCell($("DocumentTransfertReport.ConservationCalendarDispositionModeDestruction"),
				destruction);
		PdfPCell triCell = getCheckCell($("DocumentTransfertReport.ConservationCalendarDispositionModeSort"), tri);
		PdfPCell constructionCell = getCheckCell($("DocumentTransfertReport.ConservationCalendarDispositionModeConservation"),
				construction);

		dispoModeChecks.addCell(destructionCell);
		dispoModeChecks.addCell(triCell);
		dispoModeChecks.addCell(constructionCell);

		PdfPCell dispoModeChecksCell = new PdfPCell(dispoModeChecks);

		PdfPTable dispoMode = new PdfPTable(2);
		dispoMode.addCell(dispoModeChecksCell);
		dispoMode.addCell(new PdfPCell());

		PdfPCell dispoModeCell = new PdfPCell(dispoMode);

		return dispoModeCell;
	}

	private PdfPCell getCalendarSupport(List<ReportBooleanField> options) {

		PdfPTable support = new PdfPTable(4);

		boolean DM = options.get(0).getValue();
		boolean PA = options.get(1).getValue();

		support.addCell(getCheckCell($("DocumentTransfertReport.ConservationCalendarInformationSupportDM"), DM));
		support.addCell(getCheckCell($("DocumentTransfertReport.ConservationCalendarInformationSupportPA"), PA));

		PdfPCell empty = new PdfPCell();
		empty.setColspan(2);
		support.addCell(empty);
		support.completeRow();

		empty = new PdfPCell();
		empty.setColspan(4);
		support.addCell(empty);
		support.completeRow();

		PdfPCell supportCell = new PdfPCell(support);

		return supportCell;
	}

	private void addDataCell(PdfPTable table, String title, String value, int alignment) {
		PdfPCell dataCell = newDataCell(title, value, alignment);
		table.addCell(dataCell);
	}

	private PdfPCell getCheckCell(String option, boolean check) {

		PdfPTable tableCheckOption = new PdfPTable(2);

		PdfPCell labelOptionCell = newDataCell("", option, Element.ALIGN_LEFT);
		labelOptionCell.setPaddingTop(3);
		labelOptionCell.setPaddingLeft(7);

		PdfPCell checkCell = getCheck(check);

		labelOptionCell.setBorder(Rectangle.NO_BORDER);
		checkCell.setBorder(Rectangle.NO_BORDER);

		tableCheckOption.addCell(labelOptionCell);
		tableCheckOption.addCell(checkCell);

		PdfPCell checkOptionCell = new PdfPCell(tableCheckOption);

		return checkOptionCell;
	}

	private PdfPCell getCheck(boolean check) {

		PdfPCell cell = invisibleCell();

		try {
			Image img;
			String imgCheckName;

			if (check)
				imgCheckName = "check.png";
			else
				imgCheckName = "uncheck.png";

			img = Image.getInstance(foldersLocator.getReportsResourceFolder() + "/" + imgCheckName);
			//img.scalePercent(120);
			cell.addElement(new Chunk(img, 0, 2));
		} catch (BadElementException | IOException e) {
			e.printStackTrace();
		}

		return cell;
	}

	private PdfPCell newTitleCell(String titleText) {
		BaseColor background_color = new BaseColor(191, 191, 191);

		PdfPCell titleCell = new PdfPCell();
		titleCell.setBackgroundColor(background_color);

		Chunk chunkTitle = new Chunk(titleText);
		chunkTitle.setFont(fontTitle);

		Paragraph paraTitle = new Paragraph(chunkTitle);
		paraTitle.setAlignment(Element.ALIGN_CENTER);

		titleCell.addElement(paraTitle);

		return titleCell;
	}

	private PdfPCell newHeaderCell(String headerText, int alignement) {

		PdfPCell labelCell = new PdfPCell();
		Chunk chunkLabel = new Chunk(headerText);
		chunkLabel.setFont(fontHeader);

		Paragraph paraCell = new Paragraph(chunkLabel);
		paraCell.setAlignment(alignement);

		labelCell.setBackgroundColor(CELL_BACKGROUND_COLOR);
		labelCell.setPaddingTop(-2);
		labelCell.addElement(paraCell);

		return labelCell;
	}

	private PdfPCell newDataCell(String labelText, String value, int alignment) {

		PdfPCell labelCell = new PdfPCell();
		Chunk chunkLabel = new Chunk(labelText);
		chunkLabel.setFont(fontHeader);

		Paragraph paraCell = new Paragraph(chunkLabel);
		paraCell.setAlignment(alignment);

		labelCell.setPaddingTop(-2);
		labelCell.setPaddingBottom(7);

		labelCell.setBackgroundColor(NO_COLOR);
		labelCell.addElement(paraCell);

		if (value != null) {
			Chunk chunkValue = new Chunk(value);
			chunkValue.setFont(fontValue);

			paraCell = new Paragraph(chunkValue);
			paraCell.setAlignment(alignment);

			labelCell.addElement(paraCell);
		}

		return labelCell;
	}

	/*private void addFooterRow(PdfPTable table, Document document, PdfWriter writer) {
		PdfPCell footerCell = new PdfPCell();
		Chunk dateChunk = new Chunk(TimeProvider.getLocalDate().toString());
		footerCell.addElement(dateChunk);
		table.addCell(footerCell);
	}*/

	private void addEmptyCells(PdfPTable table, int number) {
		for (int i = 0; i < number; i++) {
			table.addCell(invisibleCell());
		}
	}

	/*private void addPhraseRow(PdfPTable subTable, String phrase, float fontSize, int colspan) {
		subTable.addCell(newPhraseCell(phrase, fontSize, colspan));
		subTable.completeRow();
	}*/

	/*private PdfPCell newPhraseCell(String phrase, float fontSize, int colspan) {
		PdfPCell cell = getCellWithPhrase(phrase, fontSize);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		return cell;
	}*/

	/*private PdfPCell newImageCell(Image image, boolean fit, int colspan) {
		PdfPCell cell = new PdfPCell(image, fit);
		cell.setColspan(colspan);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		return cell;
	}*/

	/*private PdfPCell getCellWithPhrase(String phrase, float fontSize) {
		Phrase pdfPhrase = new Phrase(fontSize, phrase, getFont(fontSize));
		PdfPCell cell = new PdfPCell(pdfPhrase);
		cell.setBorder(Rectangle.NO_BORDER);
		return cell;
	}*/

	/*public Font getFont(float fontSize) {
		Font font = new Font();
		if (fontSize < minFontSize) {
			fontSize = minFontSize;
		}
		font.setSize(fontSize);
		return font;
	}*/

	private PdfPCell invisibleCell() {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		return cell;
	}

	private void addEmptyRow(PdfPTable table) {
		PdfPCell cell = invisibleCell();
		cell.setFixedHeight(15f);
		table.addCell(cell);
		table.completeRow();
	}

	/*private void addEmptyRows(PdfPTable table, int number) {
		for (int i = 0; i < number; i++) {
			PdfPCell cell = invisibleCell();
			cell.setFixedHeight(15f);
			table.addCell(cell);
			table.completeRow();
		}
	}*/
	private String getFormatedDate(String date) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sentDateStr = "";

		try {
			Date sentDate = dateFormat.parse(date);
			sentDateStr = dateFormat.format(sentDate);
		} catch (ParseException e) {
			LOG.log(Level.WARNING, "Unparseable date : " + date);
		}
		return sentDateStr;
	}
	
	/*class DashedBorder implements PdfPCellEvent {
		public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
			PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
			cb.setLineDash(new float[] {3.0f, 3.0f}, 0);
			cb.stroke();
		}
	}*/

	class TableFooter extends PdfPageEventHelper {
		protected PdfPTable footer;
		PdfTemplate total;

		public TableFooter() {
			footer = new PdfPTable(4);

			float[] columnWidths = new float[] { 1.40f, 1.40f, 0.60f, 0.60f };

			try {
				footer.setWidths(columnWidths);
			} catch (DocumentException e) {
				throw new RuntimeException(e);
			}

			footer.setTotalWidth(585);

			PdfPCell signatureCell = newDataCell("", $("DocumentTransfertReport.FooterSignature"), Element.ALIGN_LEFT);
			footer.addCell(signatureCell);

			PdfPTable tableContact = new PdfPTable(1);
			PdfPCell phoneCell = newDataCell("", $("DocumentTransfertReport.FooterPhone"), Element.ALIGN_LEFT);
			PdfPCell emailCell = newDataCell("", $("DocumentTransfertReport.FooterEmail"), Element.ALIGN_LEFT);
			tableContact.addCell(phoneCell);
			tableContact.completeRow();
			tableContact.addCell(emailCell);
			tableContact.completeRow();
			PdfPCell tableContactCell = new PdfPCell(tableContact);
			tableContactCell.setPadding(0f);
			footer.addCell(tableContactCell);

			PdfPCell dateCell = newDataCell("", $("DocumentTransfertReport.FooterDate"), Element.ALIGN_LEFT);
			footer.addCell(dateCell);

			footer.addCell("");

			footer.completeRow();

			PdfPCell intelligidCell = newDataCell("", $("DocumentTransfertReport.FooterSlogan"), Element.ALIGN_LEFT);
			intelligidCell.setBorder(Rectangle.TOP);
			intelligidCell.setPadding(0f);
			intelligidCell.setColspan(4);

			footer.addCell(intelligidCell);
			footer.completeRow();
		}

		//public void onOpenDocument(PdfWriter writer, Document document) {
		//	total = writer.getDirectContent().createTemplate(30, 16);
		//}

		public void onEndPage(PdfWriter writer, Document document) {
			footer.writeSelectedRows(0, -1, 5, 64, writer.getDirectContent());

		}
	}

	class TableHeader extends PdfPageEventHelper {

		protected PdfPTable header;
		PdfTemplate total;

		public TableHeader() {
			header = new PdfPTable(2);

			PdfPCell logoCell = invisibleCell();
			PdfPCell labelCell2 = newDataCell("", $("DocumentTransfertReport.Title"), Element.ALIGN_RIGHT);

			labelCell2.setBorder(Rectangle.NO_BORDER);

			float[] columnWidths = new float[] { 1.8f, 1.2f };

			try {
				header.setWidths(columnWidths);
			} catch (DocumentException e) {
				throw new RuntimeException(e);
			}

			header.setTotalWidth(585);

			Image logo;

			try {
				logo = Image.getInstance(foldersLocator.getReportsResourceFolder() + "/constellio-logo.png");
				logo.scalePercent(20);
				logoCell.addElement(new Chunk(logo, 0, 2));
			} catch (BadElementException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			header.addCell(logoCell);
			header.addCell(labelCell2);
		}

		public void onEndPage(PdfWriter writer, Document document) {
			header.writeSelectedRows(0, -1, 5, 830, writer.getDirectContent());
		}
	}

}