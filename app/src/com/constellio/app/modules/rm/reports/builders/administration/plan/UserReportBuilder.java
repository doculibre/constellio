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

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_FilingSpace;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class UserReportBuilder implements ReportBuilder {

	private static final int COLUMN_NUMBER = 20;
	public static final int TABLE_WIDTH_PERCENTAGE = 90;
	public static final int INITIAL_FONT_SIZE = 14;
	public static final int INITIAL_LEVEL = 0;
	private static final float MAX_LEVEL = 4;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 60f;
	public static final float MARGIN_BOTTOM = 20f;

	private final Font fontValue = FontFactory.getFont("Arial", 10);
	private final Font fontHeader = FontFactory.getFont("Arial", 14);

	private UserReportModel model;

	private PdfTableUtils pdfTableUtils;
	private FoldersLocator foldersLocator;

	public UserReportBuilder(UserReportModel model, FoldersLocator foldersLocator) {
		this.model = model;
		this.pdfTableUtils = new PdfTableUtils();
		this.foldersLocator = foldersLocator;

	}

	public String getFileExtension() {
		return pdfTableUtils.PDF;
	}

	public void build(OutputStream output)
			throws IOException {
		Document document = new Document(PageSize.A4.rotate(), MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			configPageEvents(writer);

			document.open();
			document.add(createReport(writer));
			document.close();
		} catch (DocumentException e) {
			// TODO Exception
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {

		PageEvent pageEvent = new PageEvent(foldersLocator);

		pageEvent.setTitle($("UserReport.Title"));
		// TODO Rida get logo from model
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));
		pageEvent.setLandscape();

		writer.setPageEvent(pageEvent);
	}

	private PdfPTable createReport(PdfWriter writer) {

		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(90);

		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);

		table.addCell(getUserTableHeader());
		table.setHeaderRows(1);

		for (UserReportModel_User user : model.getUsers()) {
			cell = getUserTable(user);
			table.addCell(cell);
			table.completeRow();
		}

		return table;
	}

	private PdfPCell getUserTableHeader() {

		PdfPTable tableHeader = new PdfPTable(7);

		float[] columnWidths = new float[] { 0.7f, 0.8f, 0.8f, 1.6f, 1.7f, 0.7f, 0.7f };

		try {
			tableHeader.setWidths(columnWidths);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}

		addHeaderCell(tableHeader, "#", Rectangle.ALIGN_LEFT);
		addHeaderCell(tableHeader, $("UserReport.lastname"), Rectangle.ALIGN_LEFT);
		addHeaderCell(tableHeader, $("UserReport.firstname"), Rectangle.ALIGN_LEFT);
		addHeaderCell(tableHeader, $("UserReport.username"), Rectangle.ALIGN_LEFT);
		addHeaderCell(tableHeader, $("UserReport.gradingStations"), Rectangle.ALIGN_LEFT);
		addHeaderCell(tableHeader, $("UserReport.unit"), Rectangle.ALIGN_CENTER);
		addHeaderCell(tableHeader, $("UserReport.status"), Rectangle.ALIGN_CENTER);

		PdfPCell headerCell = new PdfPCell(tableHeader);
		headerCell.setBorder(Rectangle.NO_BORDER);

		return headerCell;
	}

	private PdfPCell getUserTable(UserReportModel_User user) {

		PdfPTable userTable = new PdfPTable(7);

		float[] columnWidths = new float[] { 0.7f, 0.8f, 0.8f, 1.6f, 1.7f, 0.7f, 0.7f };

		try {
			userTable.setWidths(columnWidths);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}

		addUserInfoCell(userTable, user.getUserId(), Rectangle.ALIGN_LEFT);
		addUserInfoCell(userTable, user.getLastName(), Rectangle.ALIGN_LEFT);
		addUserInfoCell(userTable, user.getFirstName(), Rectangle.ALIGN_LEFT);
		addUserInfoCell(userTable, user.getUserName(), Rectangle.ALIGN_LEFT);
		addUserInfoCell(userTable, getFilingSpaces(user), Rectangle.ALIGN_LEFT);

		addUserInfoCell(userTable, getAdminUnits(user), Rectangle.ALIGN_CENTER);
		addUserInfoCell(userTable, user.getStatus(), Rectangle.ALIGN_CENTER);

		PdfPCell userCell = new PdfPCell(userTable);
		userCell.setBorder(Rectangle.NO_BORDER);

		return userCell;
	}

	private void addHeaderCell(PdfPTable table, String text, int aligment) {

		Chunk chunk = new Chunk(text);
		chunk.setFont(fontHeader);

		Paragraph ptext = new Paragraph(chunk);
		ptext.setAlignment(aligment);

		PdfPCell currentCell = new PdfPCell();
		currentCell.addElement(ptext);

		currentCell.setBorder(Rectangle.NO_BORDER);

		table.addCell(currentCell);
	}

	private void addUserInfoCell(PdfPTable table, String info, int aligment) {

		if (info == null)
			info = "";

		Chunk chunk = new Chunk(info);
		chunk.setFont(fontValue);

		Paragraph text = new Paragraph(chunk);
		text.setAlignment(aligment);

		PdfPCell currentCell = new PdfPCell();
		currentCell.addElement(text);

		currentCell.setBorder(Rectangle.NO_BORDER);

		table.addCell(currentCell);
	}

	private String getFilingSpaces(UserReportModel_User user) {

		String fillingspaces = "";
		String fillingSpaceLabel = "";

		for (UserReportModel_FilingSpace fillingSpace : user.getFilingSpaces()) {
			fillingSpaceLabel = fillingSpace.getCode() + " - " + fillingSpace.getLabel();
			fillingspaces += fillingSpaceLabel + "\n";
		}

		fillingspaces += "\n";

		return fillingspaces;
	}

	private String getAdminUnits(UserReportModel_User user) {

		String adminUnits = "";
		String adminUnitLabel = "";

		for (UserReportModel_AdministrativeUnit adminUnit : user.getAdministrativeUnits()) {
			adminUnitLabel = adminUnit.getCode();
			adminUnits += adminUnitLabel + "\n";
		}

		adminUnits += "\n";

		return adminUnits;
	}
}
