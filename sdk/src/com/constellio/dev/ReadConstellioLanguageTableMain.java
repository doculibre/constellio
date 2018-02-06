package com.constellio.dev;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.util.*;

public class ReadConstellioLanguageTableMain extends ConstellioLanguageTableIO {

    DataFormatter excelDataFormatter;

    public ReadConstellioLanguageTableMain() throws IOException {
        super(null, null, false);
        excelDataFormatter = new DataFormatter();
    }

    public static void main(String[] args) throws IOException {
        readLanguageFile();
    }

    // FILE READING

    private static void readLanguageFile() throws IOException {
        ReadConstellioLanguageTableMain convertConstellioLanguageTable = new ReadConstellioLanguageTableMain();
//        FROM ALTERNATIVE 1 - convertConstellioLanguageTable.getConversionFiles(convertConstellioLanguageTable.getFilesAndFolders());

        Map<String, Map<String,String>> valuesInArabicWithoutIcons = convertConstellioLanguageTable.getExcelFileInfos(convertConstellioLanguageTable.getInputFile(), 0, 3);
        Map<String, Map<String,String>> valuesInFrenchWithoutIcons = convertConstellioLanguageTable.getExcelFileInfos(convertConstellioLanguageTable.getInputFile(), 0, 1);
        Map<String, Map<String,String>> valuesInArabicWithIcons = convertConstellioLanguageTable.addIconsFromFrenchPropertyFiles(valuesInArabicWithoutIcons, valuesInFrenchWithoutIcons);

        convertConstellioLanguageTable.writeExcelInfosToPropertyFiles(valuesInArabicWithIcons);

        // NEW PART (alternative 1)
//        ReadConstellioComboPropertiesFiles readConstellioComboPropertiesFiles = new ReadConstellioComboPropertiesFiles(VERSION_NUMBER_SEPARATOR);
//        readConstellioComboPropertiesFiles.readLanguageFile();

        // NEW PART
        Map<String, String> valuesInArabicWithIconsFromCombos = flattenMapWithFilter(valuesInArabicWithIcons, "combo");
//        File files = convertConstellioLanguageTable.getConversionFiles(convertConstellioLanguageTable.getFilesAndFolders(), null, VERSION_NUMBER_SEPARATOR);
    }

    private static Map<String, String> flattenMapWithFilter(Map<String, Map<String, String>> twoDimensionMap, String keyNameFilter) {

        Map<String, String> flatMap = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String,String>> firstLevelEntry : twoDimensionMap.entrySet()) {

            // filtering
            if(firstLevelEntry.getKey().endsWith(keyNameFilter)){

                for (Map.Entry<String, String> secondaryLevelEntry : firstLevelEntry.getValue().entrySet()) {

                    // flattening
                    flatMap.put(secondaryLevelEntry.getKey(), secondaryLevelEntry.getValue());
                }
            }
        }

        return flatMap;
    }

    private void writeExcelInfosToPropertyFiles(Map<String, Map<String, String>> valuesInArabicWithIcons) {
        for (Map.Entry<String, Map<String,String>> sheetEntry : valuesInArabicWithIcons.entrySet()) {
            String sheetName = sheetEntry.getKey();
            File frenchFile = getFile(getFilesInPath(), sheetName+PROPERTIES_FILE_EXTENSION);
            File file = new File(frenchFile.getParentFile(), sheetName+PROPERTIES_FILE_ARABIC_SIGNATURE+PROPERTIES_FILE_EXTENSION);
            writeInfosToPropertyFile(file, sheetEntry.getValue());
        }
    }

    /**
     * Add icons to previously parsed terms based on original french file with icons by substitution.
     * @param valuesInArabicWithoutIcons
     * @param valuesInFrenchWithoutIcons
     * @return values with icons
     */
    private Map<String,Map<String,String>> addIconsFromFrenchPropertyFiles(Map<String, Map<String, String>> valuesInArabicWithoutIcons, Map<String, Map<String, String>> valuesInFrenchWithoutIcons) {

        Map<String, Map<String,String>> sheetsWithArabicValuesWithIcons = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String,String>> sheetEntry : valuesInArabicWithoutIcons.entrySet()) {

            final int headerLine = 1;
            int lineNumber = 1 + headerLine;
            String sheetName = sheetEntry.getKey();
            File file = getFile(getFilesInPath(), sheetName+PROPERTIES_FILE_EXTENSION);
            Map<String,String> arabicInfos = valuesInArabicWithoutIcons.get(sheetName);
            Map<String, String> frenchInfos = valuesInFrenchWithoutIcons.get(sheetName);
            Map<String, String> frenchInfosWithIcons = getFileInfos(file.getParentFile(), file.getName());
            Map<String, String> arabicInfosWithIcons = new LinkedHashMap<>();

            // iterates through the most reliable property list
            for (Map.Entry<String, String> propertyEntry : frenchInfosWithIcons.entrySet()) {

                String property = propertyEntry.getKey();
                String arabicValue = arabicInfos.get(property);
                String frenchValue = frenchInfos.get(property);
                String frenchValueWithIcons = getValueWithoutErrors(frenchInfosWithIcons.get(property));

                if(property.endsWith(".icon")){ // icon paths should never be in property files - anyways, if no value is found, it will refer to the default property file (where untraducted and good paths will be found)
                    // do not add it - default property value will be taken
                }
                else if(frenchValue!=null && frenchValueWithIcons.contains(frenchValue) && arabicInfos.containsKey(property)){ // only if french and arabic data in Excel is reliable (not humanly modified or icons are in middle of text parsed or no traduction available at all), we can retreive icon
                    arabicInfosWithIcons.put(property, arabicValue + frenchValueWithIcons.replace(frenchValue, ""));
                }
                else{ // no info in Excel about this property
                    if(PROPERTIES_FILE_NO_TRADUCTION_VALUE!=null) { // if default key is null, key will not be added to property file
                        arabicInfosWithIcons.put(property, PROPERTIES_FILE_NO_TRADUCTION_VALUE);
                    }
                    System.out.println("Onglet : "+sheetName+ "; propriété : "+ property + "; ligne originale : "+lineNumber);
                }

                lineNumber++;
            }

            // append to result
            sheetsWithArabicValuesWithIcons.put(sheetName, arabicInfosWithIcons);
        }

        return sheetsWithArabicValuesWithIcons;
    }

    /**
     * Fixes common problems with some properties in files.
     * @param property - the property to validate
     * @return the correct property name
     */
    private String getValueWithoutErrors(String property) {

        String correctedProperty = property;

        if(property.equals("T�l�copieur")){
            correctedProperty = "T l copieur";
        }

        return correctedProperty;
    }

    /**
     * Writes multiple key-value pair to property file.
     * @param outputFile
     * @param infos - mapped values
     */
    private static void writeInfosToPropertyFile(File outputFile, Map<String, String> infos) {

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), DEFAULT_FILE_CHARSET));) {

            for (Map.Entry<String, String> propertyEntry : infos.entrySet()) {

                String property = propertyEntry.getKey();
                String value = propertyEntry.getValue();

                bw.write(property+INFOS_SEPARATOR+value);
                bw.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get file from group provided.
     * @param files
     * @param fileName
     * @return targeted file
     */
    private File getFile(Set<File> files, String fileName) {

        File targetedFile = null;

        for(File file : files){
            if(file.getName().equals(fileName)){
                targetedFile = file;
            }
        }

        return targetedFile;
    }

    /**
     * Returns Excel content for a given file while preserving read order.
     * @param file
     * @throws IOException
     */
    private Map<String, Map<String, String>> getExcelFileInfos(File file, int columnIndexReadToKey, int columnIndexReadToValue) throws IOException {

        Map<String, Map<String,String>> sheetKeyValuePairChosen = new LinkedHashMap<>();

        FileInputStream inputStream = new FileInputStream(file);

        org.apache.poi.ss.usermodel.Workbook workbook = new HSSFWorkbook(inputStream);

        for(int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet currentSheet = workbook.getSheetAt(i);
            sheetKeyValuePairChosen.put(currentSheet.getSheetName(), getExcelSheetInfos(currentSheet,columnIndexReadToKey,columnIndexReadToValue));
        }

        workbook.close();
        inputStream.close();

        return sheetKeyValuePairChosen;
    }

    /**
     * Returns Excel sheet content.
     * @param currentSheet - sheet object
     * @param columnIndexReadToKey - column index mapped as key
     * @param columnIndexReadToValue - column index mapped as value
     * @return map with key and value chosen
     */
    private Map<String, String> getExcelSheetInfos(Sheet currentSheet, int columnIndexReadToKey, int columnIndexReadToValue) {

        Iterator<Row> iterator = currentSheet.iterator();
        Map<String,String> propertiesWithValues = new HashMap<>(); // TODO note : unsorted and not sequential

        int lineNumber = 0;

        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            if(lineNumber>0) { // first line is header of Excel sheet
                Iterator<Cell> cellIterator = nextRow.cellIterator();

                int columnNumber = 0;
                String currentProperty = "";

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellValue = excelDataFormatter.formatCellValue(cell);

                    if (columnNumber == columnIndexReadToKey) {
                        currentProperty = cellValue;
                    } else if (columnNumber == columnIndexReadToValue) {
                        propertiesWithValues.put(currentProperty, cellValue);
                    }

                    columnNumber++;
                }
            }

            lineNumber++;
        }

        return propertiesWithValues;
    }
}