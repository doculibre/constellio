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
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.TokensCalculator3;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;

public class CoreMigrationTo_6_7 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_7.class);

	@Override
	public String getVersion() {
		return "6.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor6_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
		convertXMLAuthorizationDetailsToSolrAuthorizationDetails(collection, appLayerFactory);

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	private void convertXMLAuthorizationDetailsToSolrAuthorizationDetails(String collection, AppLayerFactory appLayerFactory)
			throws RecordServicesException {

		AuthorizationDetailsManager manager = appLayerFactory.getModelLayerFactory().getAuthorizationDetailsManager();
		Map<String, AuthorizationDetails> xmlAuthorizationDetailsList = manager.getAuthorizationsDetails(collection);
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection,
				appLayerFactory.getModelLayerFactory());

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		Set<String> convertedAuthDetails = new HashSet<>();

		try {
			KeySetMap<String, String> authsTargets = getAuthsTargets(collection, appLayerFactory);

			List<AuthToConvert> authToConverts = new ArrayList<>();

			for (AuthorizationDetails details : xmlAuthorizationDetailsList.values()) {
				convertedAuthDetails.add(details.getId());
				AuthToConvert authToConvert = new AuthToConvert();
				authToConvert.details = (XMLAuthorizationDetails) details;
				authToConvert.targets = authsTargets.get(details.getId());
				authToConverts.add(authToConvert);
			}

			for (Map.Entry<String, Set<String>> authTarget : authsTargets.getNestedMap().entrySet()) {
				if (!convertedAuthDetails.contains(authTarget.getKey())) {
					LOGGER.warn("No such authorization detail for auth id '" + authTarget.getKey() + "' on targets '" + authTarget
							.getValue() + "'");
				}
			}

			Iterator<List<AuthToConvert>> iterator = new BatchBuilderIterator<>(authToConverts.iterator(), 1000);

			KeySetMap<String, String> authCopies = buildSolrAuthorizationDetails(iterator, schemasRecordsServices,
					appLayerFactory);

			convertUsersAndGroupAuths(appLayerFactory, schemasRecordsServices, authCopies, convertedAuthDetails);
		} catch (Exception e) {
			throw new RuntimeException("Migration failed", e);
		}
	}

	private KeySetMap<String, String> getAuthsTargets(String collection, final AppLayerFactory appLayerFactory)
			throws Exception {
		final KeySetMap<String, String> authsTargets = new KeySetMap<>();

		final List<String> restrictedSchemaTypes = asList(User.SCHEMA_TYPE, Group.SCHEMA_TYPE);

		new ActionExecutorInBatch(appLayerFactory.getModelLayerFactory().newSearchServices(), "Find auth targets", 1000) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {

				Transaction transaction = new Transaction();
				transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
				transaction.getRecordUpdateOptions().setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

				for (Record record : records) {
					if (!restrictedSchemaTypes.contains(record.getTypeCode())) {
						for (String auth : record.<String>getList(Schemas.AUTHORIZATIONS)) {
							authsTargets.add(auth, record.getId());
						}

						record.set(Schemas.AUTHORIZATIONS, new ArrayList<>());
						transaction.add(record);
					}

				}

				appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(transaction);
			}
		}.execute(fromAllSchemasIn(collection).where(Schemas.AUTHORIZATIONS).isNotNull());

		return authsTargets;
	}

	private void convertUsersAndGroupAuths(final AppLayerFactory appLayerFactory,
			final SchemasRecordsServices schemasRecordsServices, final KeySetMap<String, String> authCopies,
			final Set<String> convertedAuthDetails)
			throws Exception {

		final Set<String> oldAuths = new HashSet<>();

		new ActionExecutorInBatch(appLayerFactory.getModelLayerFactory().newSearchServices(), "Convert users/group auths", 1000) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {

				Transaction transaction = new Transaction();
				transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
				transaction.getRecordUpdateOptions().setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

				for (Record record : records) {
					oldAuths.addAll(convertAuthsOf(record, authCopies, schemasRecordsServices, convertedAuthDetails));
					transaction.add(record);
				}

				appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(transaction);
			}
		}.execute(from(schemasRecordsServices.group.schemaType(), schemasRecordsServices.user.schemaType()).returnAll());

	}

	private List<String> convertAuthsOf(Record record, KeySetMap<String, String> authCopies, SchemasRecordsServices schemas,
			Set<String> convertedAuthDetails) {
		List<String> oldAuths = record.getList(Schemas.AUTHORIZATIONS);
		List<String> newAuths = new ArrayList<>();

		for (String oldAuth : oldAuths) {
			Set<String> newCopies = authCopies.get(oldAuth);
			newAuths.addAll(newCopies);

			if (!convertedAuthDetails.contains(oldAuth)) {
				String code = record.getTypeCode().equals(User.SCHEMA_TYPE) ? record.<String>get(schemas.user.username()) :
						("group " + record.get(schemas.group.code()));
				LOGGER.warn("No such authorization detail for auth id '" + oldAuth + "' on principal '" + code + "'");
			}
		}

		record.set(Schemas.AUTHORIZATIONS, newAuths);
		return oldAuths;
	}

	private List<String> findTargets(SearchServices searchServices, SchemasRecordsServices schemas,
			AuthorizationDetails details) {
		return searchServices.searchRecordIds(
				fromAllSchemasIn(details.getCollection()).where(AUTHORIZATIONS).isEqualTo(details.getId()));
	}

	private static class AuthToConvert {

		XMLAuthorizationDetails details;
		Set<String> targets;

	}

	private KeySetMap<String, String> buildSolrAuthorizationDetails(Iterator<List<AuthToConvert>> authsToConvertIterator,
			SchemasRecordsServices schemasRecordsServices, AppLayerFactory appLayerFactory)
			throws RecordServicesException {

		KeySetMap<String, String> authCopies = new KeySetMap<>();

		while (authsToConvertIterator.hasNext()) {
			List<AuthToConvert> authsToConvert = authsToConvertIterator.next();
			Transaction transaction = new Transaction();
			transaction.getRecordUpdateOptions().setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

			for (AuthToConvert authToConvert : authsToConvert) {
				for (String target : authToConvert.targets) {
					SolrAuthorizationDetails solrAuthorizationDetails = schemasRecordsServices
							.newSolrAuthorizationDetails();

					if (authToConvert.details.getStartDate() == null
							|| authToConvert.details.getStartDate().getYear() < 2007) {
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
					solrAuthorizationDetails.setTarget(target);

					transaction.add(solrAuthorizationDetails);
					authCopies.add(authToConvert.details.getId(), solrAuthorizationDetails.getId());
				}
			}

			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);

		}
		return authCopies;
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
				DataEntry dataEntry = typeBuilder.getDefaultSchema().get(TOKENS).getDataEntry();
				if (dataEntry.getType() == CALCULATED
						&& ((CalculatedDataEntry) dataEntry).getCalculator() instanceof TokensCalculator2) {
					typeBuilder.getDefaultSchema().get(TOKENS).defineDataEntry().asCalculated(TokensCalculator3.class);
				}
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
