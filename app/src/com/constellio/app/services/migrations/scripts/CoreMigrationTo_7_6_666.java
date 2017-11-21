package com.constellio.app.services.migrations.scripts;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

public class CoreMigrationTo_7_6_666 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_6_666.class);

	@Override
	public String getVersion() {
		return "7.6.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			final AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_666(collection, migrationResourcesProvider, appLayerFactory).migrate();
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		final Taxonomy principalTaxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getPrincipalTaxonomy(collection);

		if (principalTaxonomy != null) {
			new ActionExecutorInBatch(searchServices, "set auth's target schema type", 10000) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {

					Transaction tx = new Transaction();
					tx.setOptions(validationExceptionSafeOptions().setOptimisticLockingResolution(EXCEPTION));

					for (Record record : records) {
						SolrAuthorizationDetails auth = schemas.wrapSolrAuthorizationDetails(record);

						try {
							Record targetRecord = recordServices.getDocumentById(auth.getTarget());
							auth.setTargetSchemaType(targetRecord.getTypeCode());
							if (!principalTaxonomy.getSchemaTypes().contains(targetRecord.getTypeCode())) {
								auth.setLastTokenRecalculate(LocalDate.now());
							}
							tx.add(auth);
						} catch (Exception e) {
							LOGGER.info("Auth target is deleted, a case fixed with a system check", e);
						}

					}

					recordServices.execute(tx);

				}
			}.execute(from(schemas.authorizationDetails.schemaType()).returnAll());
		}

	}

	class CoreSchemaAlterationFor_7_6_666 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA);
			authorizationSchema.createUndeletable(SolrAuthorizationDetails.TARGET_SCHEMA_TYPE).setType(STRING);
			authorizationSchema.createUndeletable(SolrAuthorizationDetails.LAST_TOKEN_RECALCULATE).setType(DATE);

			//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				MetadataBuilder tokens = typeBuilder.getDefaultSchema().get(Schemas.TOKENS);
			//				if (!asList(Collection.SCHEMA_TYPE, User.SCHEMA_TYPE, Group.SCHEMA_TYPE).contains(typeBuilder.getCode())
			//						&& ((CalculatedDataEntry) tokens.getDataEntry()).getCalculator().getClass()
			//						.equals(TokensCalculator2.class)) {
			//					tokens.defineDataEntry().asCalculated(TokensCalculator4.class);
			//				}
			//
			//			}

		}
	}
}
