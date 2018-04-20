package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.*;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class VaultMigrationScript {

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
//		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	public static void migrateVault(AppLayerFactory appLayerFactory, String collection) throws RecordServicesException {
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes("zeCollection");
			MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes("zeCollection").getSchemaType("document");
		List<Record> records = searchServices.search(new LogicalSearchQuery(from(schemaType).returnAll()));
		Transaction transaction = new Transaction();
		for(Record record : records){
			Document document = new Document(record, metadataSchemaTypes);
			ContentImpl content =  (ContentImpl) document.getContent();
			content.changeHashCodesOfAllVersions();
			transaction.update(document.getWrappedRecord());
		}
		recordServices.execute(transaction);
	}

	public static void main(String argv[])
			throws Exception {
//		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		startBackend();

		migrateVault(appLayerFactory, currentCollection);
	}

}
