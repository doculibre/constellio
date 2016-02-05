package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Iterator;
import java.util.List;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RMMigrationTo5_0_4_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);

		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(collection)) {
			for (String schemaTypeCode : taxonomy.getSchemaTypes()) {
				MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
				Iterator<Record> recordsIterator = searchServices.recordsIterator(
						new LogicalSearchQuery(from(schemaType).returnAll()));
				Iterator<List<Record>> recordBatchesIterator = new BatchBuilderIterator<>(recordsIterator, 100);
				while (recordBatchesIterator.hasNext()) {
					Transaction transaction = new Transaction();
					transaction.setSkippingRequiredValuesValidation(true);
					for (Record record : recordBatchesIterator.next()) {
						transaction.add(record.set(Schemas.VISIBLE_IN_TREES, true));
					}
					try {
						recordServices.execute(transaction);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}

			}
		}

		boolean hasCategories = searchServices.hasResults(from(metadataSchemasManager.getSchemaTypes(collection).getSchemaType(
				Category.SCHEMA_TYPE)).returnAll());
		if (hasCategories) {
			ReindexingServices reindexingServices = appLayerFactory.getModelLayerFactory().newReindexingServices();
			reindexingServices.reindexCollection(collection, ReindexationMode.RECALCULATE_AND_REWRITE);
		}
	}

}
