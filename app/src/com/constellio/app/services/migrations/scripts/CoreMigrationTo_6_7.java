package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.END_DATE;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.ROLES;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.START_DATE;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.SYNCED;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.TARGET;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator3;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;

public class CoreMigrationTo_6_7 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor6_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
		convertXMLAuthorizationDetailsToSolrAuthorizationDetails(collection, appLayerFactory);
	}

	private void convertXMLAuthorizationDetailsToSolrAuthorizationDetails(String collection, AppLayerFactory appLayerFactory)
			throws RecordServicesException {

		AuthorizationDetailsManager manager = appLayerFactory.getModelLayerFactory().getAuthorizationDetailsManager();
		Map<String, AuthorizationDetails> xmlAuthorizationDetailsList = manager.getAuthorizationsDetails(collection);
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection,
				appLayerFactory.getModelLayerFactory());

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		List<AuthToConvert> authToConverts = new ArrayList<>();

		for (AuthorizationDetails details : xmlAuthorizationDetailsList.values()) {

			AuthToConvert authToConvert = new AuthToConvert();
			authToConvert.details = (XMLAuthorizationDetails) details;
			authToConvert.targetId = findTargetId(searchServices, schemasRecordsServices, details);
			if (authToConvert.targetId != null) {
				authToConverts.add(authToConvert);
			}
		}

		Iterator<List<AuthToConvert>> iterator = new BatchBuilderIterator<>(authToConverts.iterator(), 1000);

		while (iterator.hasNext()) {
			buildSolrAuthorizationDetails(iterator.next(), schemasRecordsServices, appLayerFactory);

		}
	}

	private String findTargetId(SearchServices searchServices, SchemasRecordsServices schemas, AuthorizationDetails details) {
		Record record = searchServices.searchSingleResult(
				fromAllSchemasIn(details.getCollection()).where(AUTHORIZATIONS).isEqualTo(details.getId()));
		return record == null ? null : record.getId();
	}

	private static class AuthToConvert {

		XMLAuthorizationDetails details;
		String targetId;

	}

	private void buildSolrAuthorizationDetails(List<AuthToConvert> authsToConvert, SchemasRecordsServices schemasRecordsServices,
			AppLayerFactory appLayerFactory)
			throws RecordServicesException {

		Transaction transaction = new Transaction();

		for (AuthToConvert authToConvert : authsToConvert) {
			SolrAuthorizationDetails solrAuthorizationDetails = schemasRecordsServices
					.newSolrAuthorizationDetailsWithId(authToConvert.details.getId());

			if (authToConvert.details.getStartDate() == null || authToConvert.details.getStartDate().getYear() < 2007) {
				solrAuthorizationDetails.setStartDate(null);
			} else {
				solrAuthorizationDetails.setStartDate(authToConvert.details.getStartDate());
			}

			if (authToConvert.details.getEndDate() == null || authToConvert.details.getEndDate().getYear() < 2007) {
				solrAuthorizationDetails.setEndDate(null);
			} else {
				solrAuthorizationDetails.setEndDate(authToConvert.details.getEndDate());
			}

			if (!authToConvert.details.isSynced()) {
				solrAuthorizationDetails.setSynced(null);
			} else {
				solrAuthorizationDetails.setSynced(true);
			}

			solrAuthorizationDetails.setRoles(authToConvert.details.getRoles());
			transaction.add(solrAuthorizationDetails);
		}
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private class CoreSchemaAlterationFor6_7 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
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
				typeBuilder.getDefaultSchema().get(TOKENS).defineDataEntry().asCalculated(TokensCalculator3.class);
			}
		}

		private MetadataSchemaTypeBuilder createReportSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaType(SolrAuthorizationDetails.SCHEMA_TYPE);
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
