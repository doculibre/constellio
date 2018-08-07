package com.constellio.app.modules.rm.reports.builders.BatchProssessing;

import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import jxl.write.Number;
import net.sf.jasperreports.engine.util.FileBufferedWriter;

import java.io.FileWriter;
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
        return "xls";
    }

    @Override
    public void write(OutputStream output) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(output);
        try {
            addHeader(writer, getColumnsTitles());
            createContent(writer, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writer.flush();
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHeader(OutputStreamWriter writer, List<Object> columnsTitles)
            throws IOException {
        writeLine(writer, columnsTitles);
    }

    private void createContent(OutputStreamWriter writer, BatchProcessingResultModel model) throws IOException {
        int currentExcelLine = 0;
        int currentNumberOfRecord = 0;
        List<Object> resultHeaderLine = getColumnsTitles();

        for(int lineNumber =0; lineNumber < model.resultsCount(); lineNumber++) {
            BatchProcessRecordModifications currentResult = model.getResult(lineNumber);
            writeLine(writer, asList(model.getResultTitle(currentResult)));
            writeLine(writer, resultHeaderLine);
            for(List<Object> currentLine : model.getResultLines(currentResult)) {
                writeLine(writer, currentLine);
                currentExcelLine++;
            }
            for(List<Object> currentImpact : model.getImpacts(currentResult)) {
                writeLine(writer, currentImpact);
            }
        }
    }


    private void writeLine(OutputStreamWriter writer, List<Object> currentLine) throws IOException {
        for(int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++){
            Object cellObject = currentLine.get(columnNumber);
            /*if(columnNumber == 0){
                cellObject.set
            }*/
            if(cellObject == null){
                continue;
            }
            writer.append(cellObject.toString() + ";");
        }
    }
}