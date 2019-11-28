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
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMSIPExportService {

	private SearchServices searchServices;
	private AppLayerFactory appLayerFactory;
	private List<String> idsWithIncludedMetadataToTrue;
	private List<String> includedRecordIds;

	public RMSIPExportService(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public List<String> getIncludedIds(String collection, String includeInExportFolderMetadataCode,
									   String includeInExportDocumentMetadataCode,
									   String excludeFromExportFolderMetadataCode,
									   String excludeFromExportDocumentMetadataCode) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery includedFoldersQuery = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.schemaType().getMetadata(Folder.DEFAULT_SCHEMA + "_" + includeInExportFolderMetadataCode)).isTrue());
		LogicalSearchQuery includedDocumentsQuery = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.schemaType().getMetadata(Document.DEFAULT_SCHEMA + "_" + includeInExportDocumentMetadataCode)).isTrue());
		idsWithIncludedMetadataToTrue = new ArrayList<>();
		includedRecordIds = new ArrayList<>();

		includeRecordsAndParents(rm, searchServices.search(includedFoldersQuery));
		includeRecordsAndParents(rm, searchServices.search(includedDocumentsQuery));

		includedRecordIds.addAll(idsWithIncludedMetadataToTrue);

		LogicalSearchQuery excludedFoldersQuery = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.schemaType().getMetadata(Folder.DEFAULT_SCHEMA + "_" + excludeFromExportFolderMetadataCode)).isTrue());
		LogicalSearchQuery excludedDocumentsQuery = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.schemaType().getMetadata(Document.DEFAULT_SCHEMA + "_" + excludeFromExportDocumentMetadataCode)).isTrue());
		List<String> idsWithExcludedMetadataToTrue = new ArrayList<>();
		idsWithExcludedMetadataToTrue.addAll(searchServices.searchRecordIds(excludedFoldersQuery));
		idsWithExcludedMetadataToTrue.addAll(searchServices.searchRecordIds(excludedDocumentsQuery));
		LogicalSearchQuery allFoldersAndDocumentsQuery = new LogicalSearchQuery(from(rm.folder.schemaType(), rm.document.schemaType()).returnAll());

		SearchResponseIterator<Record> recordSearchResponseIterator = searchServices.recordsIterator(allFoldersAndDocumentsQuery, 10000);
		while (recordSearchResponseIterator.hasNext()) {
			Record record = recordSearchResponseIterator.next();
			List<String> pathParts = record.get(Schemas.PATH_PARTS);
			boolean includeRecord = false;
			if (!ListUtils.intersection(pathParts, idsWithIncludedMetadataToTrue).isEmpty()) {
				if (!(ListUtils.intersection(pathParts, idsWithExcludedMetadataToTrue).isEmpty())) {
					for (String pathPart : pathParts) {
						if (pathPart.contains("_LAST_") &&
							idsWithIncludedMetadataToTrue.contains(pathPart.replace("_LAST_", ""))) {
							includeRecord = true;
							break;
						}
					}
				} else {
					includeRecord = true;
				}
			}
			String recordId = record.getId();
			if (!idsWithExcludedMetadataToTrue.contains(recordId) && includeRecord) {
				includedRecordIds.add(recordId);
			}
		}

		return includedRecordIds;
	}

	private void includeRecordsAndParents(RMSchemasRecordsServices rm, List<Record> records) {
		for (Record record : records) {
			List<String> pathParts = record.get(Schemas.PATH_PARTS);
			idsWithIncludedMetadataToTrue.add(record.getId());
			for (String pathPart : pathParts) {
				if (!pathPart.startsWith("_LAST_")) {
					Record pathPartRecord = rm.get(pathPart);
					if (Folder.DEFAULT_SCHEMA.equals(pathPartRecord.getSchemaCode())) {
						includedRecordIds.add(pathPartRecord.getId());
					}
				}
			}
		}
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
