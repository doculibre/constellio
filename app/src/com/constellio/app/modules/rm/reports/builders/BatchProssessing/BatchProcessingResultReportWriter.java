package com.constellio.app.modules.rm.reports.builders.BatchProssessing;

import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.*;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel.getColumnsTitles;
import static java.util.Arrays.asList;


public class BatchProcessingResultReportWriter implements ReportWriter {
    private static final WritableFont.FontName FONT = WritableFont.TIMES;
    private static final int FONT_SIZE = 10;
    BatchProcessingResultModel model;
    Locale locale;

    public BatchProcessingResultReportWriter(BatchProcessingResultModel model, Locale locale) {
        this.model = model;
        this.locale = locale;
    }

    @Override
    public String getFileExtension() {
        return "xls";
    }

    @Override
    public void write(OutputStream output) throws IOException {
        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(locale);

        WritableWorkbook workbook = Workbook.createWorkbook(output, wbSettings);
        workbook.createSheet(i18n.$("Report.sheetName"), 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        excelSheet.setColumnView(0, 30);
        excelSheet.setColumnView(1, 30);
        excelSheet.setColumnView(2, 30);
        try {
            createContent(excelSheet, model);
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }

        workbook.write();
        try {
            workbook.close();
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHeader(WritableSheet sheet, List<String> columnsTitles)
            throws WriteException {
        WritableCellFormat boldFont = new WritableCellFormat (new WritableFont(FONT, FONT_SIZE, WritableFont.BOLD));

        CellView cv = new CellView();
        cv.setFormat(boldFont);
        cv.setAutosize(true);
        for(int i = 0; i < columnsTitles.size(); i++){
            String columnTitle = columnsTitles.get(i);
            addString(sheet, boldFont, i, 0, columnTitle);
        }
    }

    private void createContent(WritableSheet sheet, BatchProcessingResultModel model) throws WriteException{
        WritableCellFormat  font = new WritableCellFormat (new WritableFont(FONT, FONT_SIZE));
        WritableCellFormat boldFont = new WritableCellFormat (new WritableFont(FONT, FONT_SIZE, WritableFont.BOLD));
        WritableCellFormat redFont = new WritableCellFormat (new WritableFont(FONT, FONT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED));

        int currentExcelLine = 0;
        List<Object> resultHeaderLine = getColumnsTitles();

        for(int lineNumber =0; lineNumber < model.resultsCount(); lineNumber++) {
            BatchProcessRecordModifications currentResult = model.getResult(lineNumber);
            writeLine(sheet, asList(model.getResultTitle(currentResult)), currentExcelLine, boldFont);
            currentExcelLine++;
            writeLine(sheet, resultHeaderLine, currentExcelLine, boldFont);
            currentExcelLine++;
            for(List<Object> currentLine : model.getResultLines(currentResult)) {
                writeLine(sheet, currentLine, currentExcelLine, font);
                currentExcelLine++;
            }
            for(List<Object> currentImpact : model.getImpacts(currentResult)) {
                writeLine(sheet, currentImpact, currentExcelLine, redFont);
                currentExcelLine++;
            }
            currentExcelLine ++;
        }
    }


    private void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font) throws WriteException {
        for(int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++){
            Object cellObject = currentLine.get(columnNumber);
            /*if(columnNumber == 0){
                cellObject.set
            }*/
            if(cellObject == null){
                continue;
            }
            if(cellObject instanceof Float ||
                    cellObject instanceof Integer ||
                    cellObject instanceof Double) {
                addNumber(sheet, font, columnNumber, lineNumber, new Double(cellObject.toString()));
            } else {
                addString(sheet, font, columnNumber, lineNumber, cellObject.toString());
            }
        }
    }

    private void addString(WritableSheet sheet, WritableCellFormat font, int column, int row, String s)
            throws WriteException {
        Label label = new Label(column, row, s, font);
        sheet.addCell(label);
    }

    private void addNumber(WritableSheet sheet, WritableCellFormat font, int column, int row,
                           double d) throws WriteException {
        Number number = new Number(column, row, d, font);
        sheet.addCell(number);
    }

}