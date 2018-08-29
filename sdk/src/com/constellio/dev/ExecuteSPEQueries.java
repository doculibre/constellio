package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ExecuteSPEQueries {

	private static final String COLLECTION = "collection";

	public static void main(String argv[]) {

		AppLayerFactory appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(COLLECTION, appLayerFactory);

		long foldersCount = searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll());
		long documentsCount = searchServices.getResultsCount(from(rm.documentSchemaType()).returnAll());
		System.out.println("Nombre de dossiers : " + foldersCount);
		System.out.println("Nombre de documents : " + documentsCount);
	}

}
