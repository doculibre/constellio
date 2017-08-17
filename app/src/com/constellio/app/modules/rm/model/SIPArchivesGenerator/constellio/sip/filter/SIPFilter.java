package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.filter;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class SIPFilter {
    private int startIndex;
    private String file;
    private String bagInfo;
    private String rubriqueCode;
    private String administrativeUnit;
    private boolean documentTypeRequired;
    private List<String> includeFolderIds;
    private List<String> excludeFolderIds;
    private List<String> includeDocumentIds;
    private String extractionFolder;
    private boolean limitSize;

    private String collection;
    private AppLayerFactory factory;
    private RMSchemasRecordsServices rm;

    public SIPFilter(String collection, AppLayerFactory factory){
        this.collection = collection;
        this.factory = factory;
        this.rm = new RMSchemasRecordsServices(collection, factory);
        includeDocumentIds = new ArrayList<>();
        includeFolderIds = new ArrayList<>();
        excludeFolderIds = new ArrayList<>();
    }

    public SIPFilter withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public SIPFilter withFile(String file) {
        this.file = file;
        return this;
    }

    public SIPFilter withBagInfo(String bagInfo) {
        this.bagInfo = bagInfo;
        return this;
    }

    public SIPFilter withRubriqueCode(String rubriqueCode) {
        this.rubriqueCode = rubriqueCode;
        return this;
    }

    public SIPFilter withAdministrativeUnit(String administrativeUnit) {
        this.administrativeUnit = administrativeUnit;
        return this;
    }

    public SIPFilter withDocumentTypeRequired(boolean documentTypeRequired) {
        this.documentTypeRequired = documentTypeRequired;
        return this;
    }

    public SIPFilter withIncludeFolderIds(List<String> includeFolderIds) {
        this.includeFolderIds = includeFolderIds;
        return this;
    }

    public SIPFilter withExcludeFolderIds(List<String> excludeFolderIds) {
        this.excludeFolderIds = excludeFolderIds;
        return this;
    }

    public SIPFilter withIncludeDocumentIds(List<String> includeDocumentIds) {
        this.includeDocumentIds = includeDocumentIds;
        return this;
    }

    public SIPFilter withExtractionFolder(String extractionFolder) {
        this.extractionFolder = extractionFolder;
        return this;
    }

    public SIPFilter withLimitSize(boolean limitSize) {
        this.limitSize = limitSize;
        return this;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getFile() {
        return file;
    }

    public String getBagInfo() {
        return bagInfo;
    }

    public Category getRubriqueCode() {
        return rm.getCategoryWithCode(rubriqueCode);
    }

    public AdministrativeUnit getAdministrativeUnit() {
        return rm.getAdministrativeUnitWithCode(administrativeUnit);
    }

    public boolean isDocumentTypeRequired() {
        return documentTypeRequired;
    }

    public List<String> getIncludeFolderIds() {
        return includeFolderIds;
    }

    public List<String> getExcludeFolderIds() {
        return excludeFolderIds;
    }

    public List<String> getIncludeDocumentIds() {
        return includeDocumentIds;
    }

    public String getExtractionFolder() {
        return extractionFolder;
    }

    public boolean isLimitSize() {
        return limitSize;
    }

    public LogicalSearchQuery getSearchQuery(){
        MetadataSchemaType documentSchemaType = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Document.SCHEMA_TYPE);
        List<LogicalSearchCondition> whereClause = new ArrayList<>();

        if(this.isDocumentTypeRequired()) {
            whereClause.add(where(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.TYPE)).isNotNull());
        }

        if(this.getRubriqueCode() != null) {
            whereClause.add(where(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER_CATEGORY)).isEqualTo(this.getRubriqueCode().getId()));
        }

        if(this.getAdministrativeUnit() != null) {
            whereClause.add(where(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER_ADMINISTRATIVE_UNIT)).isEqualTo(this.getAdministrativeUnit()));
        }

        if(!this.getIncludeFolderIds().isEmpty()) {
            whereClause.add(where(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER)).isIn(this.getIncludeFolderIds()));
        }

        if(!this.getIncludeDocumentIds().isEmpty()) {
            whereClause.add(where(Schemas.IDENTIFIER).isIn(this.getIncludeDocumentIds()));
        }

        if(!this.getExcludeFolderIds().isEmpty()) {
            whereClause.add(where(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER)).isNotIn(this.getExcludeFolderIds()));
        }

        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType).whereAllConditions(
                whereClause
        );
        return new LogicalSearchQuery(condition).sortAsc(Schemas.IDENTIFIER).setStartRow(this.getStartIndex());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("startIndex: ").append(startIndex).append("\n");
        builder.append("codeRubrique: ").append(rubriqueCode).append("\n");
        builder.append("codeUniteAdministrative: ").append(administrativeUnit).append("\n");
        builder.append("typeDocumentRequis: ").append(documentTypeRequired).append("\n");
        builder.append("idsFichesDossiersIncluses: ").append(includeFolderIds).append("\n");
        builder.append("idsFichesDossiersExclues: ").append(excludeFolderIds).append("\n");
        builder.append("idsFichesDocumentsIncluses: ").append(includeDocumentIds).append("\n");
        builder.append("dossierExtraction: ").append(extractionFolder).append("\n");
        builder.append("limiterTaille: ").append(limitSize).append("\n");
        return builder.toString();
    }

}
