package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DocumentUtil {
	public static Document createCopyFrom(Document document, AppLayerFactory appLayerFactory) {
		String collection = document.getCollection();
		Document doc = createNewDocument(collection, appLayerFactory);

		for (Metadata metadata : document.getSchema().getMetadatas()) {
			if (!metadata.isSystemReserved() && metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				doc.set(metadata.getLocalCode(), document.get(metadata));
			}
		}

		return doc;
	}

	public static Document createNewDocument(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return rm.newDocument();
	}

	public static List<Document> getDocumentsInFolder(String folderId, String collection,
													  AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return getDocumentsInFolder(rm.getFolder(folderId), appLayerFactory);
	}

	public static List<Document> getDocumentsInFolder(Folder folder, AppLayerFactory appLayerFactory) {
		LogicalSearchQuery query = new LogicalSearchQuery();

		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(folder.getCollection());

		query.setCondition(from(types.getSchemaType(Document.SCHEMA_TYPE)).where(types.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Document.FOLDER)).is(folder));
		query.sortAsc(Schemas.TITLE);


		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(folder.getCollection(), appLayerFactory);
		Function<Record, Document> recordToDocument = new Function<Record, Document>() {
			@Override
			public Document apply(Record input) {
				return rm.wrapDocument(input);
			}
		};

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		List<Record> records = searchServices.query(query).getRecords();
		return Lists.transform((List<Record>) CollectionUtils.emptyIfNull(records), recordToDocument);
	}
}
