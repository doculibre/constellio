package com.constellio.app.modules.rm.reports.builders.BatchProssessing;

import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessPossibleImpact;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordFieldModification;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class BatchProcessingResultModel {
    private final BatchProcessResults results;
    private final Language language;

    public BatchProcessingResultModel(BatchProcessResults results, Language language) {
        this.results = results;
        this.language = language;
    }

    public static List<Object> getColumnsTitles(){
        List<Object> returnList = new ArrayList<>();
        returnList.add($("BatchProcessingResultModel.metadata"));
        returnList.add($("BatchProcessingResultModel.before"));
        returnList.add($("BatchProcessingResultModel.after"));
        return returnList;
    }

    public Object getResultTitle(BatchProcessRecordModifications result) {
        return result.getRecordId() + "-" + result.getRecordTitle();
    }

    public int resultsCount() {
        return results.getRecordModifications().size();
    }

    public List<String> getResultHeader(BatchProcessRecordModifications result) {
        return asList(result.getRecordId() + "-" + result.getRecordTitle());
    }

    public BatchProcessRecordModifications getResult(int lineNumber) {
        return results.getRecordModifications().get(lineNumber);
    }

    public List<List<Object> > getResultLines(BatchProcessRecordModifications currentResult) {
        List<List<Object>> recordModifications = new ArrayList<>();
        for(BatchProcessRecordFieldModification fieldModification : currentResult.getFieldsModifications()){
            List<Object> currentLine = new ArrayList<>();
            currentLine.add(getLabel(fieldModification.getMetadata()));
            currentLine.add(fieldModification.getValueBefore());
            currentLine.add(fieldModification.getValueAfter());

            recordModifications.add(currentLine);
        }
        return recordModifications;
    }

    private Object getLabel(Metadata metadata) {
        return metadata.getLabel(language);
    }

    public List<List<Object>> getImpacts(BatchProcessRecordModifications currentResult) {
        List<List<Object> > recordImpacts = new ArrayList<>();
        for(BatchProcessPossibleImpact possibleImpact : currentResult.getImpacts()){
            List<Object> currentLine = new ArrayList<>();
            currentLine.add($("BatchProcessingResultModel.impactOnSchema", possibleImpact.getCount(), getLabel(possibleImpact.getSchemaType())));

            recordImpacts.add(currentLine);
        }
        return recordImpacts;
    }

    private String getLabel(MetadataSchemaType schemaType) {
        return schemaType.getLabel(language);
    }
}
