package com.constellio.app.modules.rm.reports.builders.search.stats;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.constellio.app.modules.rm.reports.PageEvent;
import com.constellio.app.modules.rm.reports.PdfTableUtils;
import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.ui.framework.reports.ReportBuilder;
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

public class StatsReportBuilder implements ReportBuilder {

	public static final int TABLE_WIDTH_PERCENTAGE = 90;

	public static final float MARGIN_LEFT = 0f;
	public static final float MARGIN_RIGHT = 0f;
	public static final float MARGIN_TOP = 87f;
	public static final float MARGIN_BOTTOM = 20f;

	private final Font fontValue = FontFactory.getFont("Arial", 10);

	private StatsReportModel model;

	private FoldersLocator foldersLocator;

	public StatsReportBuilder(StatsReportModel model, FoldersLocator foldersLocator) {
		this.model = model;

		this.foldersLocator = foldersLocator;
	}

	public String getFileExtension() {
		return PdfTableUtils.PDF;
	}

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
			throw new RuntimeException(e);
		}
	}

	private void configPageEvents(PdfWriter writer)
			throws BadElementException, IOException {
		PageEvent pageEvent = new PageEvent(foldersLocator);

		String title = $("FolderLinearMeasureStatsReport.Title");

		pageEvent.setTitle(title);
		pageEvent.setLogo("constellio-logo.png");
		pageEvent.setFooter(TimeProvider.getLocalDateTime().toString("yyyy-MM-dd HH:mm"));

		writer.setPageEvent(pageEvent);
	}

	private PdfPTable createReport(PdfWriter writer) {

		PdfPTable table = new PdfPTable(1);

		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
		table.setExtendLastRow(true);

		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		Map<String, Object> stats = model.getStats();
		if(stats != null){
			for(KeyCaption keyCaption: orderAccordingToCaptions(stats.keySet())){
				String caption = keyCaption.getCaption();
				Object value = stats.get(keyCaption.getKey());
				if(value != null){
					if(keyCaption.getKey().equals("sum")){
						value = Double.valueOf(value.toString()) /100;
					}
					cell = addLine(writer, caption, value.toString());
					table.addCell(cell);
					table.completeRow();
				}
			}
		}else{
			cell = addLine(writer, $("FolderLinearMeasureStatsReport.sum"), "0");
			table.addCell(cell);
			table.completeRow();
		}

		return table;
	}

	private java.util.List<KeyCaption> orderAccordingToCaptions(Set<String> keys) {
		java.util.List<KeyCaption> keyCaptionList = new ArrayList<>();
		for(String key : keys){
			String caption = $("FolderLinearMeasureStatsReport." + key);
			keyCaptionList.add(new KeyCaption(key, caption));
		}
		Collections.sort(keyCaptionList, new Comparator<KeyCaption>() {
			@Override
			public int compare(KeyCaption keyCaption1, KeyCaption keyCaption2) {

				return keyCaption1.caption.compareTo(keyCaption2.caption);
			}
		});

		return keyCaptionList;
	}

	private PdfPCell addLine(PdfWriter writer, String caption, String value) {
		PdfPTable userTable = new PdfPTable(2);

		float[] columnWidths = new float[] { 0.7f, 0.8f};

		try {
			userTable.setWidths(columnWidths);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}

		addUserInfoCell(userTable, caption, Rectangle.ALIGN_LEFT);
		addUserInfoCell(userTable, value.toString(), Rectangle.ALIGN_LEFT);

		PdfPCell userCell = new PdfPCell(userTable);
		userCell.setBorder(Rectangle.NO_BORDER);
		return userCell;
	}

	private void addUserInfoCell(PdfPTable table, String info, int alignment) {

		if (info == null)
			info = "";

		Chunk chunk = new Chunk(info);
		chunk.setFont(fontValue);

		Paragraph text = new Paragraph(chunk);
		text.setAlignment(alignment);

		PdfPCell currentCell = new PdfPCell();
		currentCell.addElement(text);

		currentCell.setBorder(Rectangle.NO_BORDER);

		table.addCell(currentCell);
	}

	private class KeyCaption {
		private String caption;
		private String key;

		public KeyCaption(String key, String caption) {
			this.key = key;
			this.caption = caption;
		}

		public String getCaption() {
			return caption;
		}

		public String getKey() {
			return key;
		}

	}
}
