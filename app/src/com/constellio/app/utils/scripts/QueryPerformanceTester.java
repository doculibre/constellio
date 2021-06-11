package com.constellio.app.utils.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.utils.ScriptsUtils;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordCachesServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class QueryPerformanceTester {

	private static User user;

	public static void main(String[] args)
			throws Exception {

		String collection = args[0];
		String username = args[1];
		int nbOfThreads = Integer.valueOf(args[2]);
		int nbOfExecute = Integer.valueOf(args[3]);

		AppLayerFactory appLayerFactory = ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads();
		runTest(appLayerFactory, collection, username, nbOfThreads, nbOfExecute);

	}

	public static void runTest(AppLayerFactory appLayerFactory, final String collection, String username,
							   int nbOfThreads,
							   int nbOfExecute)
			throws Exception {
		final ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		user = modelLayerFactory.newUserServices().getUserInCollection(username, collection);
		new RecordCachesServices(modelLayerFactory).loadCachesIn(collection);
		dataLayerFactory.getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		ThreadList<Thread> threads = new ThreadList<>();
		final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		for (int i = 0; i < (nbOfThreads - 1); i++) {
			threads.addAndStart(new Thread() {
				@Override
				public void run() {
					while (atomicBoolean.get()) {
						doSearch(collection, modelLayerFactory);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		long start = new Date().getTime();

		for (int j = 0; j < nbOfExecute; j++) {
			System.out.println(">" + j);
			doSearch(collection, modelLayerFactory);
		}
		long duration = (new Date().getTime() - start);
		atomicBoolean.set(false);
		threads.joinAll();

		long durationByRequests = duration / nbOfExecute;
		System.out.println("---------------------------------");
		System.out.println("Number of threads : " + threads);
		System.out.println("Number of requests : " + nbOfExecute);
		System.out.println("Requests are taking " + durationByRequests + "ms");
	}

	private static void doSearch(String collection, ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);

		List<MetadataSchemaType> types = asList(rm.folder.schemaType(), rm.documentSchemaType(), rm.containerRecord.schemaType());
		LogicalSearchQuery query = new LogicalSearchQuery(from(types).returnAll())
				.setFreeTextQuery("agence rap")
				.filteredWithUserRead(user)
				.filteredByStatus(StatusFilter.ACTIVES)
				.setPreferAnalyzedFields(true)
				.setNumberOfRows(10)
				.setHighlighting(true)
				.setReturnedMetadatas(ReturnedMetadatasFilter
						.onlyMetadatas(Schemas.TITLE, rm.folder.administrativeUnit(), rm.folder.category(), rm.folder.openingDate(),
								rm.folder.semiactiveRetentionType(),
								rm.folder.activeRetentionType(), rm.folder.archivisticStatus(), rm.folder.closingDate(),
								rm.folder.retentionRule()));
		List<Record> records = searchServices.search(query);

		LogicalSearchQuery facetQuery = new LogicalSearchQuery(from(types).returnAll())
				.setFreeTextQuery("agence rap")
				.filteredWithUserRead(user)
				.filteredByStatus(StatusFilter.ACTIVES)
				.setPreferAnalyzedFields(true)
				.setNumberOfRows(0);
		facetQuery.addFieldFacet(rm.folder.administrativeUnit().getDataStoreCode());
		facetQuery.addFieldFacet(rm.folder.category().getDataStoreCode());
		facetQuery.addFieldFacet("schema_s");
		facetQuery.addFieldFacet(rm.folder.archivisticStatus().getDataStoreCode());
		searchServices.search(facetQuery);
	}
}