package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.search.SearchServices;
import org.joda.time.LocalDate;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo9_0_4_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		new ActionExecutorInBatch(searchServices, "Fixing publishing end date of documents", 1000) {

			@Override
			public void doActionOnBatch(List<Record> records) throws Exception {

				Transaction tx = new Transaction();
				tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
				records.forEach(record->{
					Document document = rm.wrapDocument(record);
					LocalDate startTime = document.getPublishingStartDate();
					LocalDate endTime = document.getPublishingEndDate();
					if (startTime != null && startTime.isEqual(endTime)) {
						document.setPublishingEndDate(null);
					}
					tx.add(document);
				});

				appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(tx);

			}
		}.execute(from(rm.document.schemaType())
				.where(rm.document.publishingStartDate()).isNotNull()
				.andWhere(rm.document.publishingExpirationDate()).isNotNull());

	}


}
