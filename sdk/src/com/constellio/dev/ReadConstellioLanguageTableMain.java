package com.constellio.dev;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.util.*;

public class ReadConstellioLanguageTableMain extends ConstellioLanguageTableIO {

    DataFormatter excelDataFormatter;
    Map<String, String> manualTraductionPropertiesWithTraductedValues;

    public ReadConstellioLanguageTableMain() throws IOException {
        super(null, null, false);
        excelDataFormatter = new DataFormatter();
        initManualTraductions();
    }

    private void initManualTraductions() {

        // TODO manually add missing traductions here

        // terms with recongition problem (back to .properties problem)
        manualTraductionPropertiesWithTraductedValues = new HashMap<>();
        manualTraductionPropertiesWithTraductedValues.put("BaseMultiFileUpload.dropZoneCaption", "\uF061 \u0627\u0646\u0642\u0631 \u0639\u0644\u0649 \u0627\u0644\u0632\u0631 \u0623\u0648 \uF016 \u0627\u0633\u062D\u0628\u0647");
        manualTraductionPropertiesWithTraductedValues.put("perm.core.manageSystemDataImports", "\u0627\u0633\u062A\u064A\u0631\u0627\u062F");
        manualTraductionPropertiesWithTraductedValues.put("AdvancedSearchView.searchPrompt", "\u0627\u0646\u0642\u0631 \u0639\u0644\u0649   \u0644\u0644\u0628\u062D\u062B \u0627\u0644\u0645\u062A\u0642\u062F\u0645");
        manualTraductionPropertiesWithTraductedValues.put("SystemConfigurationGroup.others.defaultStartTab", "\u0635\u0641\u062D\u0629 \u0627\u0644\u0628\u062F\u0621 (<b> \u0627\u0644\u062A\u0635\u0646\u064A\u0641\u0627\u062A </ b> \u0644\u0644\u0645\u0633\u0627\u062D\u0627\u062A \u0627\u0644\u0627\u0641\u062A\u0631\u0627\u0636\u064A\u0629\u060C <b>lastViewedFolders </ b> \u0644\u0646\u0634\u0627\u0637 \u0627\u0644\u0645\u062C\u0644\u062F\u0627\u062A\u060C <b> lastViewedDocuments </ b> \u0644\u0646\u0634\u0627\u0637 \u0627\u0644\u0648\u062B\u0627\u0626\u0642\u060C <b> checkedOutDocuments </ b> \u0644\u0644\u0648\u062B\u0627\u0626\u0642 \u0627\u0644\u0645\u0637\u0644\u0639 \u0639\u0644\u064A\u0647\u0627 )");
        manualTraductionPropertiesWithTraductedValues.put("SystemConfigurationGroup.decommissioning.linkableCategoryMustNotBeRoot", "\u064A\u062C\u0628 \u0639\u0644\u0649 \u0627\u0644\u0635\u0646\u0641 \u0627\u0644\u0642\u0627\u0628\u0644 \u0644\u0644\u0631\u0628\u0637 \u0623\u0644\u0627 \u064A\u0643\u0648\u0646 \u062C\u0630\u0631");
        manualTraductionPropertiesWithTraductedValues.put("DisplayWorkflowDefinitionViewImpl.warning.draft", "\u0623\u0646\u062A \u062A\u0639\u0645\u0644 \u062D\u0627\u0644\u064A\u0627 \u0639\u0644\u0649 \u0646\u0633\u062E\u0629 \u0645\u0633\u0648\u062F\u0629 \u0645\u0646 \u0648\u0648\u0631\u0643\u0641\u0644\u0648. \u0644\u062A\u0641\u0639\u064A\u0644\u0647 \u0627\u0646\u0642\u0631 \u0639\u0644\u0649 \u0627\u0644\u0646\u0634\u0631.");
        manualTraductionPropertiesWithTraductedValues.put("ActionAfterClassification.d", "\u0627\u062A\u0644\u0627\u0641 \u0627\u0644\u0648\u062B\u0627\u0626\u0642 \u0645\u0646 \u0645\u0644\u0641 \u0645\u0634\u0627\u0631\u0643\u062A\u0643 \u0628\u0639\u062F \u0627\u0644\u062A\u062D\u0648\u064A\u0644 \u0628\u0634\u0643\u0644 \u062F\u0627\u0626\u0645  \"\u0644\u0627 \u064A\u0645\u0643\u0646 \u0627\u0644\u0631\u062C\u0648\u0639 \u0627\u0644\u0649 \u0627\u0644\u062E\u0644\u0641 \u0641\u064A \u062D\u0627\u0644\u0629 \u062E\u0637\u0623!\"");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_requiredId", "\u0644\u0645 \u064A\u062A\u0645 \u062A\u062D\u062F\u064A\u062F \u0645\u0639\u0631\u0641 \u0644\u0644\u0633\u062C\u0644 \u0641\u064A \u0627\u0644\u0645\u0648\u0636\u0639{index}");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidMetadataCode", "\u0627\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadata}\" \u063A\u064A\u0631 \u0645\u0648\u062C\u0648\u062F\u0629 \u0641\u064A \u0645\u062E\u0637\u0637 \u0627\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{schema}\" ({schemaLabel})");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidResolverMetadataCode", "\u062A\u0633\u062A\u062E\u062F\u0645 \u0627\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\" \u0628\u064A\u0627\u0646\u0627\u062A \u0648\u0635\u0641\u064A\u0629 \"{resolverMetadata}\" \u063A\u064A\u0631 \u0645\u0648\u062C\u0648\u062F\u0629 \u0623\u0648 \u063A\u064A\u0631 \u0645\u0642\u0628\u0648\u0644\u0629 \u0644\u0643\u064A \u062A\u062D\u0635\u0644 \u0639\u0644\u0649 \u0633\u062C\u0644 \u0645\u0631\u062C\u0639\u064A");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_unresolvedValue", "\u0644\u0627 \u064A\u0648\u062C\u062F \u0633\u062C\u0644 \u0644\u0644\u0646\u0648\u0639 \"{referencedSchemaTypeLabel}\" \u0644\u0647 \u0627\u0644\u0642\u064A\u0645\u0629 \"{unresolvedValue}\" \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\"");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_automaticMetadataCode", "\u064A\u062A\u0645 \u062D\u0633\u0627\u0628 \u0627\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\" \u062A\u0644\u0642\u0627\u0626\u064A\u0627\u060C \u0648\u0644\u0627 \u064A\u0645\u0643\u0646 \u0627\u062F\u062E\u0627\u0644\u0647\u0627 \u064A\u062F\u0648\u064A\u0627");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_legacyIdNotUnique", "\u0627\u0644\u0645\u0639\u0631\u0641 \"{Value}\" \u0644\u064A\u0633 \u0641\u0631\u064A\u062F\u0627 \u0641\u064A \u0627\u0644\u0645\u0644\u0641");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidSchemaTypeCode", "\u0644\u0627 \u064A\u0648\u062C\u062F \u0646\u0648\u0639 \u0627\u0644\u0645\u062E\u0637\u0637 \"{schemaType}\"");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidEnumValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\"\u063A\u064A\u0631 \u0635\u0627\u0644\u062D\u0629. \u064A\u062A\u0645 \u0642\u0628\u0648\u0644 \u0627\u0644\u0642\u064A\u0645\u0627\u062A  \"{acceptedValues}\"  \u0641\u0642\u0637");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidStringValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\" \u0644\u064A\u0633\u062A \u0633\u0644\u0633\u0644\u0629 \u0623\u062D\u0631\u0641 \u0635\u0627\u0644\u062D\u0629");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidNumberValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\"  \u0644\u064A\u0633\u062A \u0631\u0642\u0645\u0627");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidBooleanValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\"\u063A\u064A\u0631 \u0635\u0627\u0644\u062D\u0629.\u064A\u062A\u0645 \u0642\u0628\u0648\u0644 \u0627\u0644\u0642\u064A\u0645\u0627\u062A  \"true, false\"  \u0641\u0642\u0637");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidDateValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\" \u0644\u064A\u0633\u062A \u062A\u0627\u0631\u064A\u062E\u0627 ");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.services.schemas.bulkImport.RecordsImportServices_invalidDatetimeValue", "\u0627\u0644\u0642\u064A\u0645\u0629 \"{value}\" \u0627\u0644\u0645\u062D\u062F\u062F\u0629 \u0644\u0644\u0628\u064A\u0627\u0646\u0627\u062A \u0627\u0644\u0648\u0635\u0641\u064A\u0629 \"{metadataLabel}\" \u0644\u064A\u0633\u062A \u062A\u0627\u0631\u064A\u062E\u0627 \u0632\u0645\u0646\u064A");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.modules.rm.extensions.RMSystemCheckExtension_typeCannotBeChangeForTypeType", "\u0625\u0635\u0644\u0627\u062D \u0645\u062E\u0637\u0637 \u0627\u0644\u0633\u062C\u0644 {recordId}\u063A\u064A\u0631 \u0645\u0645\u0643\u0646");
        manualTraductionPropertiesWithTraductedValues.put("BatchProcess.title.reindex.transaction", "\u0625\u0639\u0627\u062F\u0629 \u0627\u0644\u062A\u0643\u0634\u064A\u0641 \u0628\u0639\u062F \u062A\u0639\u062F\u064A\u0644 \u0627\u0644\u0633\u062C\u0644\u0627\u062A");
        manualTraductionPropertiesWithTraductedValues.put("SearchResultDisplay.elevation", "\u0627\u0631\u062A\u0641\u0627\u0639");
        manualTraductionPropertiesWithTraductedValues.put("SearchResultDisplay.exclusion", "\u0625\u0642\u0635\u0627\u0621");
        manualTraductionPropertiesWithTraductedValues.put("SearchResultDisplay.unexclusion", "\u0625\u0632\u0627\u0644\u0629 \u0627\u0644\u0627\u0633\u062A\u0628\u0639\u0627\u062F");
        manualTraductionPropertiesWithTraductedValues.put("SearchResultDisplay.unelevation", "\u0625\u0632\u0627\u0644\u0629 \u0627\u0644\u0627\u0631\u062A\u0641\u0627\u0639");
        manualTraductionPropertiesWithTraductedValues.put("AddEditCapsuleViewImpl.title", "\u0625\u0636\u0627\u0641\u0629 \u0643\u0628\u0633\u0648\u0644\u0629 \u0627\u0644\u0628\u062D\u062B");
        manualTraductionPropertiesWithTraductedValues.put("SystemConfigurationGroup.reports", "\u0623\u0636\u0641 \u062A\u0642\u0631\u064A\u0631 \u0627\u0644\u0625\u062D\u0635\u0627\u0626\u064A\u0627\u062A \u0625\u0644\u0649 \u0627\u0644\u062E\u064A\u0627\u0631\u0627\u062A \u0627\u0644\u0645\u062A\u0627\u062D\u0629");

        // never traducted/missing terms
        manualTraductionPropertiesWithTraductedValues.put("Language.ar", "العربية");
        manualTraductionPropertiesWithTraductedValues.put("SystemConfigurationGroup.reports", "تقرير");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.extensions.WorkflowPageExtension_noWorkflowItem", "لا يوجد سير عمل تم نشره");
        manualTraductionPropertiesWithTraductedValues.put("ReportTabButton.ShowError", "خطأ");
        manualTraductionPropertiesWithTraductedValues.put("ReportTabButton.ErrorMessage", "لا يتوفر نموذج تقرير للنوع أو النماذج المحددة");
        manualTraductionPropertiesWithTraductedValues.put("com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerView_requiredValueForMetadata", "يجب ملء أحد البيانات الوصفية التالية: لا يوجد صندوق عملاء أو أي حاوية");
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

                if(property.endsWith(".icon") || property.equals("ConstellioSetupView.setup.ar")){ // setup welcome (non inherent from language) / icon paths should never be in property files - anyways, if no value is found, it will refer to the default property file (where untraducted and good paths will be found)
                    // do not add it - default property value will be taken
                }
                else if(frenchValue!=null && frenchValue.isEmpty()){ // if parsed string was an icon only
                    arabicInfosWithIcons.put(property, frenchValueWithIcons);
                }
                else if(frenchValue!=null && frenchValueWithIcons.contains(frenchValue) && arabicInfos.containsKey(property)){ // only if french and arabic data in Excel is reliable (not humanly modified or icons are in middle of text parsed or no traduction available at all), we can retreive icon
                    String iconWithFrenchSpace = frenchValueWithIcons.replace(frenchValue, "");
                    String arabicValueWithProgrammaticElemsInversed = arabicValue;

                    arabicValueWithProgrammaticElemsInversed = inverse(arabicValueWithProgrammaticElemsInversed, "{code}", "{title}");
                    arabicValueWithProgrammaticElemsInversed = inverse(arabicValueWithProgrammaticElemsInversed, "{taskName}", "{taskId}");

                    arabicInfosWithIcons.put(property, iconWithFrenchSpace + arabicValueWithProgrammaticElemsInversed);
                }
                else if(manualTraductionPropertiesWithTraductedValues.containsKey(property)){
                    arabicInfosWithIcons.put(property, manualTraductionPropertiesWithTraductedValues.get(property));
                }
                else{ // no info in Excel about this property
                    if(PROPERTIES_FILE_NO_TRADUCTION_VALUE!=null) { // if default key is null, key will not be added to property file
                        arabicInfosWithIcons.put(property, PROPERTIES_FILE_NO_TRADUCTION_VALUE);
                    }
                    // REPORTS MISSING/INCORRECT ENTRIES
//                  System.out.println("Onglet : "+sheetName+ "; propriété : "+ property + "; ligne originale : "+lineNumber);
                    // REPORTS MISSING/INCORRECT VALUES
//                    if(arabicValue==null) {
//                        System.out.println(sheetName + ";" + property + ";" + frenchValue + ";" + "UNKNOWN" + ";" + arabicValue);
//                    }
                    if(arabicValue!=null){
                        System.out.println("manualTraductionPropertiesWithTraductedValues.put(\""+property+'"' + ", " + '"'+ StringEscapeUtils.escapeJava(arabicValue)+"\");");
                    }
                }

                lineNumber++;
            }

            // append to result
            sheetsWithArabicValuesWithIcons.put(sheetName, arabicInfosWithIcons);
        }

        return sheetsWithArabicValuesWithIcons;
    }

    private String inverse(String string, String exp1, String exp2) {

        String inversedString = string;
        final String tempExp1 = "{tempExp1}";

        if(string.contains(exp1) && string.contains(exp1)){
            inversedString = string.replace(exp1,tempExp1).replace(exp2,exp1).replace(tempExp1,exp2);
        }

        return inversedString;
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