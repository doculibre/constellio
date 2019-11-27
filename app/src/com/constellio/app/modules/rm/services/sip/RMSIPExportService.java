package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMSIPExportService {

	private SearchServices searchServices;
	private AppLayerFactory appLayerFactory;

	public RMSIPExportService(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public List<String> getIncludedIds(String collection, String includeInExportFolderMetadataCode,
									   String includeInExportDocumentMetadataCode,
									   String excludeFromExportFolderMetadataCode,
									   String excludeFromExportDocumentMetadataCode) {
		List<String> includedRecordIds = new ArrayList<>();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery includedFoldersQuery = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.schemaType().getMetadata(Folder.DEFAULT_SCHEMA + "_" + includeInExportFolderMetadataCode)).isTrue());
		LogicalSearchQuery includedDocumentsQuery = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.schemaType().getMetadata(Document.DEFAULT_SCHEMA + "_" + includeInExportDocumentMetadataCode)).isTrue());
		List<String> idsWithIncludedMetadataToTrue = new ArrayList<>();
		idsWithIncludedMetadataToTrue.addAll(searchServices.searchRecordIds(includedFoldersQuery));
		idsWithIncludedMetadataToTrue.addAll(searchServices.searchRecordIds(includedDocumentsQuery));


		LogicalSearchQuery excludedFoldersQuery = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.schemaType().getMetadata(Folder.DEFAULT_SCHEMA + "_" + excludeFromExportFolderMetadataCode)).isTrue());
		LogicalSearchQuery excludedDocumentsQuery = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.schemaType().getMetadata(Document.DEFAULT_SCHEMA + "_" + excludeFromExportDocumentMetadataCode)).isTrue());
		List<String> idsWithExcludedMetadataToTrue = new ArrayList<>();
		idsWithExcludedMetadataToTrue.addAll(searchServices.searchRecordIds(excludedFoldersQuery));
		idsWithExcludedMetadataToTrue.addAll(searchServices.searchRecordIds(excludedDocumentsQuery));
		LogicalSearchQuery allFoldersQuery = new LogicalSearchQuery(from(rm.folder.schemaType()).returnAll());
		LogicalSearchQuery allDocumentsQuery = new LogicalSearchQuery(from(rm.document.schemaType()).returnAll());

		SearchResponseIterator<Record> recordSearchResponseIterator = searchServices.recordsIterator(allFoldersQuery, 10000);
		while (recordSearchResponseIterator.hasNext()) {
			Record record = recordSearchResponseIterator.next();
			List<String> pathParts = record.get(Schemas.PATH_PARTS);
			if (pathParts.contains(idsWithIncludedMetadataToTrue) && !pathParts.contains(idsWithExcludedMetadataToTrue)) {
				includedRecordIds.add(record.getId());
			}
		}
		recordSearchResponseIterator = searchServices.recordsIterator(allDocumentsQuery, 10000);
		while (recordSearchResponseIterator.hasNext()) {
			Record record = recordSearchResponseIterator.next();
			List<String> pathParts = record.get(Schemas.PATH_PARTS);
			if (pathParts.contains(idsWithIncludedMetadataToTrue) && !pathParts.contains(idsWithExcludedMetadataToTrue)) {
				includedRecordIds.add(record.getId());
			}
		}

		return includedRecordIds;
	}

	public Provider getFilter(final List<String> includedIds) {
		return new Provider<String, Boolean>() {
			@Override
			public Boolean get(String recordId) {
				return includedIds.contains(recordId) || recordId == null;
			}
		};
	}
}
