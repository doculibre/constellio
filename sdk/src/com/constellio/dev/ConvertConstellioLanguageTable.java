package com.constellio.dev;

import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003Sheet;
import com.constellio.app.services.schemas.bulkImport.data.excel.ExcelImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.excel.ExcelSheet;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.model.conf.FoldersLocator;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertConstellioLanguageTable implements ReportWriter {

    public static final String INFOS_SEPARATOR = "=";
    public static final String DEFAULT_FILE_CHARSET = "UTF-8";
    private static final WritableFont.FontName FONT = WritableFont.ARIAL;
    private static final int FONT_SIZE = 10;

    private ReportModelImpl model;
    private WritableWorkbook workbook;
    private File[] filesAndFolders;
    private String minVersion;

    private File outputFile;
    private FileOutputStream fileOutputStream;
    private Set<File> filesInPath;

    public static void main(String[] args) throws IOException {
//        writeLanguageFile();
        readLanguageFile();
    }

    private static void writeLanguageFile() throws IOException {
        ConvertConstellioLanguageTable convertConstellioLanguageTable = new ConvertConstellioLanguageTable("7_6_3", true);

        convertConstellioLanguageTable.write(convertConstellioLanguageTable.getFileOutputStream());
        convertConstellioLanguageTable.prepareConversion(convertConstellioLanguageTable.getFilesAndFolders());
        convertConstellioLanguageTable.convert();
        convertConstellioLanguageTable.endWrite();
    }

    public ConvertConstellioLanguageTable(String minVersion, boolean isWriteMode) throws IOException {
        filesInPath = new TreeSet<>(Collections.<File>reverseOrder());
        this.minVersion = minVersion;
        initSystemFiles(isWriteMode);
    }

    private void initSystemFiles(boolean deletePreviousInfos) throws IOException {
        FoldersLocator foldersLocator = new FoldersLocator();
        File i18nFolder = foldersLocator.getI18nFolder();
        File outputDirectory = new File(i18nFolder+ File.separator + "excelOutput");
        outputFile = new File(outputDirectory, "output."+getFileExtension());

        if(deletePreviousInfos){
            // deletes previous output so its not in input files
            if(outputDirectory.exists()){
                FileUtils.deleteDirectory(outputDirectory);
            }

            // get files to convert
            filesAndFolders = i18nFolder.listFiles();

            // creates output files
            outputDirectory.mkdir();
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }

            fileOutputStream = new FileOutputStream(outputFile);
        }
    }

    private void convert(){

        for (File file : filesInPath) {

            String fileName = file.getName();

            if (isBasePropertyFile(fileName) || isComboPropertyFile(fileName) || isRootPropertyFile(fileName)) {
                initExcelModel();

                Map<String, String> frenchInfos = getFileInfos(file.getParentFile(), fileName);
                Map<String, String> englishInfos = getFileInfos(file.getParentFile(), fileName.replace(".properties", "_en.properties"));

                for (Map.Entry<String, String> entry : frenchInfos.entrySet()) {

                    String property = entry.getKey();
                    String frenchTerm = removeSpecialChars(entry.getValue());

                    List<Object> line = new ArrayList<>();
                    line.add(property);
                    line.add(frenchTerm);

                    // languages other than french can have missing properties
                    if (englishInfos.containsKey(property)) {
                        line.add(removeSpecialChars(englishInfos.get(property)));
                    }

                    model.addLine(line);
                }

                writeSheet(file.getName().replace(".properties", ""));
            }
        }
    }

    private String removeSpecialChars(String value) {

        char[] charArray = value.toCharArray();

        for(int i = 0; i < charArray.length; i++){

            char currentChar = charArray[i];

            if((int) currentChar > 255){
                value = value.replace(currentChar,' ');
            }
        }

        return value.trim();
    }

    private void prepareConversion(File[] files) throws IOException {

        for (File file : files) {

            String fileName = file.getName();

            if (file.isDirectory() && (!isVersionNumber(fileName) || (isVersionNumber(fileName) && fileName.compareTo(minVersion)>0) )) {
                prepareConversion(file.listFiles());
            }
            else if(!file.isDirectory()){

                filesInPath.add(file);

            }
        }
    }

    private static Map<String, String> getFileInfos(File folder, String fileName) {

        Map<String, String> infos = new LinkedHashMap<>();

        File file = new File(folder, fileName);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_FILE_CHARSET))) {

            String previousLine = "";
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (isNotClosed(currentLine)) {
                    previousLine = currentLine;
                } else {
                    currentLine = previousLine + currentLine;
                    previousLine = "";

                    String currentProperty = getPropertyName(currentLine);

                    if (currentProperty != null) {
                        infos.put(currentProperty, currentLine.replace(currentProperty + INFOS_SEPARATOR, ""));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return infos;
    }

    private static String getPropertyName(String currentLine) {

        String propertyName = null;
        String[] lineParts = currentLine.split(INFOS_SEPARATOR);

        if (lineParts.length == 2) {
            propertyName = lineParts[0];
        }

        return propertyName;
    }

    private static boolean isNotClosed(String currentLine) {
        java.util.regex.Pattern pattern = Pattern.compile("\\\\$");
        Matcher matcher = pattern.matcher(currentLine);

        return matcher.find();
    }

    // EXCEL SETUP

    private void initExcelModel(){
        model = new ReportModelImpl();
        model.addTitle("Property");
        model.addTitle("French");
        model.addTitle("English");
        model.addTitle("Arabic");
    }

    // EXCEL WRITER

    @Override
    public String getFileExtension() {
        return "xls";
    }

    public void write(OutputStream output)
            throws IOException {
        WorkbookSettings wbSettings = new WorkbookSettings();
//      wbSettings.setLocale(locale);
        workbook = Workbook.createWorkbook(output, wbSettings);
   }

    private void writeSheet(String sheetName){
        workbook.createSheet(sheetName, 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        try {
            addHeader(excelSheet, model.getColumnsTitles());
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }
        try {
            createContent(excelSheet, model.getResults());
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }
    }

    private void endWrite() throws IOException {
        workbook.write();
        try {
            workbook.close();
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }
    }

    // EXCEL TOOLS

    private void addHeader(WritableSheet sheet, List<String> columnsTitles)
            throws WriteException {
        WritableCellFormat boldFont = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE, WritableFont.BOLD));

        CellView cv = new CellView();
        cv.setFormat(boldFont);
        cv.setAutosize(true);
        for (int i = 0; i < columnsTitles.size(); i++) {
            String columnTitle = columnsTitles.get(i);
            addString(sheet, boldFont, i, 0, columnTitle);
        }
    }

    private void createContent(WritableSheet sheet, List<List<Object>> lines)
            throws WriteException {
        WritableCellFormat font = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE));

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            List<Object> currentLine = lines.get(lineNumber);
            writeLine(sheet, currentLine, lineNumber + 1, font);
        }
    }

    private void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font)
            throws WriteException {
        CellView cv = new CellView();
        cv.setFormat(font);
        cv.setAutosize(true);
        for (int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++) {
            Object cellObject = currentLine.get(columnNumber);
            if (cellObject == null) {
                continue;
            }
            if (cellObject instanceof Float ||
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
                           double d)
            throws WriteException {
        Number number = new Number(column, row, d, font);
        sheet.addCell(number);
    }

    // INCLUSION/EXCLUSION TOOLS

    private static boolean isBasePropertyFile(String currentLine) {
        Pattern pattern = Pattern.compile("\\d.properties$");
        Matcher matcher = pattern.matcher(currentLine);

        return matcher.find();
    }

    private static boolean isRootPropertyFile(String currentLine) {
        Pattern pattern = Pattern.compile("i18n.properties$");
        Matcher matcher = pattern.matcher(currentLine);

        return matcher.find();
    }

    private static boolean isComboPropertyFile(String currentLine) {
        Pattern pattern = Pattern.compile("combo.properties$");
        Matcher matcher = pattern.matcher(currentLine);

        return matcher.find();
    }

    private boolean isVersionNumber(String currentLine) {
        Pattern pattern = Pattern.compile("^\\d+_\\d");
        Matcher matcher = pattern.matcher(currentLine);

        return matcher.find();
    }

    // DATA HOLDERS

    public File[] getFilesAndFolders() {
        return filesAndFolders;
    }

    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public class ReportModelImpl {
        private final List<List<Object>> results = new ArrayList<>();
        private final List<String> columnsTitles = new ArrayList<>();

        public List<List<Object>> getResults() {
            return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
        }

        public List<String> getColumnsTitles() {
            return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
        }

        public void addTitle(String title) {
            columnsTitles.add(title);
        }

        public void addLine(List<Object> recordLine) {
            results.add(recordLine);
        }
    }

    // FILE READING

    private static void readLanguageFile() throws IOException {

    }

    public Excel2003ImportDataProvider getNewDataProvider(File file){
        return new Excel2003ImportDataProvider(file);
    }

    private class Excel2003ImportDataProvider {

        private File excelFile;

        private Workbook workbook;

        public Excel2003ImportDataProvider(File excelFile) {
            this.excelFile = excelFile;
        }

        public ExcelImportDataIterator newDataIterator(String sheet){
            return new ExcelImportDataIterator(getExcelSheet(sheet));
        }

        public void initialize() {
            this.workbook = loadWorkbook(excelFile);
        }

        public void close() {
            workbook.close();
        }

        public Workbook loadWorkbook(File workbookFile) {
            WorkbookSettings settings = new WorkbookSettings();
            settings.setEncoding(DEFAULT_FILE_CHARSET);
            try {
                return Workbook.getWorkbook(workbookFile, settings);
            } catch (BiffException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        public jxl.Sheet[] getExcelSheets(){
            return workbook.getSheets();
        }

        public ExcelSheet getExcelSheet(String sheet) {
            return new Excel2003Sheet(workbook.getSheet(sheet));
        }
    }

}