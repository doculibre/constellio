package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo8_1_0_3 implements MigrationScript {
	KeySetMap<String, String> FAVORITES_LIST_MAP = new KeySetMap<>();

	@Override
	public String getVersion() {
		return "8.1.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor8_1_0_3(collection, provider, appLayerFactory).migrate();

	}

	private class SchemaAlterationFor8_1_0_3 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_1_0_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
			SearchServices searchServices = modelLayerFactory.newSearchServices();
			LogicalSearchQuery query = new LogicalSearchQuery(from(rm.cartSchema()).returnAll());

			for (Record record : searchServices.search(query)) {
				Cart cart = rm.wrapCart(record);
				addToFavoritesList(cart.getFolders(), cart.getId());
				addToFavoritesList(cart.getDocuments(), cart.getId());
				addToFavoritesList(cart.getContainers(), cart.getId());
			}

			modifyRecords(rm.folder.schemaType(), Folder.FAVORITES_LIST);
			modifyRecords(rm.document.schemaType(), Document.FAVORITES_LIST);
			modifyRecords(rm.containerRecord.schemaType(), ContainerRecord.FAVORITES_LIST);

		}

		private void modifyRecords(final MetadataSchemaType metadataSchemaType, final String metadataCode) {
			onCondition(from(metadataSchemaType).returnAll()).modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
				@Override
				public void modifyRecord(Record record) {
					Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record).getMetadata(metadataCode);
					if (FAVORITES_LIST_MAP.contains(record.getId())) {
						List<String> favoritesList = new ArrayList<>();
						for (String value : FAVORITES_LIST_MAP.get(record.getId())) {
							favoritesList.add(value);
						}
						record.set(metadata, favoritesList);
					}
				}
			});
		}

		public void addToFavoritesList(List<String> records, String cartId) {
			for (String recordId : records) {
				FAVORITES_LIST_MAP.add(recordId, cartId);
			}
		}

		public ConditionnedActionExecutorInBatchBuilder onCondition(LogicalSearchCondition condition) {
			return new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, condition);
		}

	}

}
