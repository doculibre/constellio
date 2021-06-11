package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.entities.records.wrappers.RecordAuthorization.END_DATE;
import static com.constellio.model.entities.records.wrappers.RecordAuthorization.ROLES;
import static com.constellio.model.entities.records.wrappers.RecordAuthorization.START_DATE;
import static com.constellio.model.entities.records.wrappers.RecordAuthorization.SYNCED;
import static com.constellio.model.entities.records.wrappers.RecordAuthorization.TARGET;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class CoreMigrationTo_7_0 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_0.class);

	@Override
	public String getVersion() {
		return "7.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		removeQueryFacet(collection, appLayerFactory);
		new CoreSchemaAlterationFor7_0(collection, migrationResourcesProvider, appLayerFactory).migrate();

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	private void removeQueryFacet(String collection, AppLayerFactory appLayerFactory)
			throws Exception {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		List<Facet> facetList = schemas.wrapFacets(searchServices.search(query(from(schemas.facetSchemaType()).whereAllConditions(
				where(schemas.facetQuerySchema().getMetadata(Facet.FACET_TYPE)).isEqualTo(FacetType.QUERY),
				where(schemas.facetQuerySchema().getMetadata(Facet.TITLE)).isEqualTo("Création/Modification date")
		))));

		Transaction transaction = new Transaction();
		for (Facet facet : facetList) {
			transaction.update(facet.setActive(false).getWrappedRecord());
		}
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private class CoreSchemaAlterationFor7_0 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createReportSchemaType(typesBuilder);
			setNewTokenCalculator(typesBuilder);
		}

		private void setNewTokenCalculator(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				DataEntry dataEntry = typeBuilder.getDefaultSchema().get(TOKENS).getDataEntry();
			}
		}

		private MetadataSchemaTypeBuilder createReportSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(RecordAuthorization.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();

			defaultSchema.createUndeletable(ROLES).setType(STRING).setMultivalue(true).setDefaultRequirement(true);
			defaultSchema.createUndeletable(SYNCED).setType(BOOLEAN);
			defaultSchema.createUndeletable(START_DATE).setType(DATE);
			defaultSchema.createUndeletable(END_DATE).setType(DATE);
			defaultSchema.createUndeletable(TARGET).setType(STRING).setDefaultRequirement(true);

			return type;

		}
	}
}
