package com.constellio.app.modules.rm.services.sip.filter;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
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
	private String categoryCode;
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
	private SearchServices searchService;

	public SIPFilter(String collection, AppLayerFactory factory) {
		this.collection = collection;
		this.factory = factory;
		this.rm = new RMSchemasRecordsServices(collection, factory);
		this.searchService = factory.getModelLayerFactory().newSearchServices();
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

	public SIPFilter withCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
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

	public Category getCategory() {
		return rm.getCategoryWithCode(categoryCode);
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

	public List<Document> getDocument() {

		MetadataSchemaType documentSchemaType = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Document.SCHEMA_TYPE);
		final List<LogicalSearchCondition> whereClauseAll = new ArrayList<>();
		final List<LogicalSearchCondition> whereClauseAny = new ArrayList<>();


		if (!this.getIncludeFolderIds().isEmpty()) {
			for (String folderId : this.getIncludeFolderIds()) {
				Folder folder = rm.getFolder(folderId);
				whereClauseAny.add(where(documentSchemaType.getMetadata(Schemas.PRINCIPAL_PATH.getCode())).isStartingWithText(folder.<String>get(Schemas.PRINCIPAL_PATH)));
			}
		}

		if (!this.getIncludeDocumentIds().isEmpty()) {
			whereClauseAny.add(where(documentSchemaType.getMetadata(Schemas.IDENTIFIER.getCode())).isIn(this.getIncludeDocumentIds()));
		}

		if (!this.getExcludeFolderIds().isEmpty()) {
			for (String folderId : this.getIncludeFolderIds()) {
				Folder folder = rm.getFolder(folderId);
				whereClauseAny.add(where(documentSchemaType.getMetadata(Schemas.PRINCIPAL_PATH.getCode())).isNot(LogicalSearchQueryOperators.startingWithText(folder.<String>get(Schemas.PRINCIPAL_PATH))));
			}
		}

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType).whereAnyCondition(
				whereClauseAny
		);
		List<Document> documentList = rm.wrapDocuments(searchService.search(new LogicalSearchQuery(condition).sortAsc(Schemas.IDENTIFIER).setStartRow(this.getStartIndex())));
		List<Document> finalDocumentList = new ArrayList<>();
		for (Document document : documentList) {
			if (!((this.isDocumentTypeRequired() && document.getType() == null)
				  || (this.getCategory() != null && !document.getFolderCategory().equals(this.getCategory().getId()))
				  || (this.getAdministrativeUnit() != null && !document.getFolderAdministrativeUnit().equals(this.getAdministrativeUnit().getId())))) {
				finalDocumentList.add(document);
			}
		}
		return finalDocumentList;
	}
}
