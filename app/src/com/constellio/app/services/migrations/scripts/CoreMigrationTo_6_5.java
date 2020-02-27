package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class CoreMigrationTo_6_5 implements MigrationScript {
	private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_5.class);

	@Override
	public String getVersion() {
		return "6.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor6_5(collection, provider, appLayerFactory).migrate();
		setAllDeleteLogicallyToNow(collection, appLayerFactory);
	}

	private void setAllDeleteLogicallyToNow(String collection, AppLayerFactory appLayerFactory) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				fromAllSchemasIn(collection).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());
		List<Record> results = appLayerFactory.getModelLayerFactory().newSearchServices().search(query);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory()
				.newRecordServices();
		LocalDateTime now = TimeProvider.getLocalDateTime();
		for (Record record : results) {
			try {
				recordServices.add(record.set(Schemas.LOGICALLY_DELETED_ON, now));
			} catch (RecordServicesException e) {
				LOGGER.warn("Delete date was not set correctly for record " + record.getId(), e);
			}
		}
	}

	private class CoreSchemaAlterationFor6_5 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_5(String collection, MigrationResourcesProvider provider,
										  AppLayerFactory appLayerFactory) {
			super(collection, provider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//add delete metadata

			typesBuilder.getSchema(User.DEFAULT_SCHEMA).create("visibleTableColumns")
					.defineStructureFactory(MapStringListStringStructureFactory.class);
		}
	}

}
