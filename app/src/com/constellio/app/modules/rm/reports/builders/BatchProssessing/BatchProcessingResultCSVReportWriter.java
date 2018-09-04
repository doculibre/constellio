package com.constellio.app.modules.rm.reports.builders.BatchProssessing;

import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import jxl.write.WritableFont;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel.getColumnsTitles;
import static java.util.Arrays.asList;


public class BatchProcessingResultCSVReportWriter implements ReportWriter {
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;
	private static final int MAX_NUMBER_OF_RECORDS_PER_SHEET = 10;
	private static final int MAX_NUMBER_OF_SHEETS = 100;
	BatchProcessingResultModel model;
	Locale locale;

	public BatchProcessingResultCSVReportWriter(BatchProcessingResultModel model, Locale locale) {
		this.model = model;
		this.locale = locale;
	}

	@Override
	public String getFileExtension() {
		return "csv";
	}

	@Override
	public void write(OutputStream output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		try {
			createContent(writer, model);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		writer.flush();
	}

	private void createContent(BufferedWriter writer, BatchProcessingResultModel model) throws IOException {
		List<Object> resultHeaderLine = getColumnsTitles();

		for (int lineNumber = 0; lineNumber < model.resultsCount(); lineNumber++) {
			BatchProcessRecordModifications currentResult = model.getResult(lineNumber);
			writeLine(writer, asList(model.getResultTitle(currentResult)));
			writeLine(writer, resultHeaderLine);
			for (List<Object> currentLine : model.getResultLines(currentResult)) {
				writeLine(writer, currentLine);
			}
			for (List<Object> currentImpact : model.getImpacts(currentResult)) {
				writeLine(writer, currentImpact);
			}

			writer.newLine();
		}
	}


	private void writeLine(BufferedWriter writer, List<Object> currentLine) throws IOException {
		for (int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++) {
			Object cellObject = currentLine.get(columnNumber);
			if (cellObject == null) {
				cellObject = "";
			}
			writer.write(cellObject.toString());
			if (columnNumber < currentLine.size() - 1) {
				writer.write(";");
			}
		}
		writer.newLine();
	}
}