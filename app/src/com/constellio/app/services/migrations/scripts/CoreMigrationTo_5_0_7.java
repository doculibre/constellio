package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.app.ui.pages.search.criteria.FacetSelectionsFactory;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SavedSearch.SortOrder;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadataFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddressFactory;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.UserTokensCalculator2;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;

public class CoreMigrationTo_5_0_7 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor5_0_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
		createDefaultCoreFacets(collection, migrationResourcesProvider, appLayerFactory);
	}

	private void createDefaultCoreFacets(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory)
			throws RecordServicesException {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = rm.getModelLayerFactory().newRecordServices();

		recordServices.add(rm.newFacetField().setOrder(0)
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.type"))
				.setFieldDataStoreCode(Schemas.SCHEMA.getDataStoreCode()));

		recordServices.add(rm.newFacetQuery().setOrder(1)
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.createModification"))
				.withQuery("modifiedOn_dt:[NOW-1MONTH TO NOW]", "Modifiés les 30 derniers jours")
				.withQuery("modifiedOn_dt:[NOW-7DAY TO NOW]", "Modifiés les 7 derniers jours")
				.withQuery("createdOn_dt:[NOW-1MONTH TO NOW]", "Créés les 30 derniers jours")
				.withQuery("createdOn_dt:[NOW-7DAY TO NOW]", "Créés les 7 derniers jours"));
	}

	private class CoreSchemaAlterationFor5_0_7 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor5_0_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createReportSchemaType(typesBuilder);
			createSendEmail(typesBuilder);
			createSavedSearchSchemaType(typesBuilder);
			changeTokensCalculators(typesBuilder);
			createFacetSchema(typesBuilder);
		}

		private void createFacetSchema(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder facetTypes = typesBuilder.createNewSchemaTypeWithSecurity(Facet.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder facetSchema = facetTypes.getDefaultSchema();

			facetSchema.createUndeletable(Facet.FIELD_DATA_STORE_CODE).setType(MetadataValueType.STRING)
					.setEssential(true);
			facetSchema.createUndeletable(Facet.FACET_TYPE).setType(MetadataValueType.ENUM)
					.defineAsEnum(FacetType.class)
					.setDefaultRequirement(true);
			facetSchema.createUndeletable(Facet.PAGES).setType(MetadataValueType.NUMBER)
					.setDefaultValue(1);
			facetSchema.createUndeletable(Facet.ELEMENT_PER_PAGE).setType(MetadataValueType.NUMBER)
					.setDefaultValue(5)
					.setDefaultRequirement(true);
			facetSchema.createUndeletable(Facet.ORDER_RESULT).setType(MetadataValueType.ENUM)
					.defineAsEnum(FacetOrderType.class)
					.setDefaultValue(FacetOrderType.RELEVANCE)
					.setDefaultRequirement(true);
			facetSchema.createUndeletable(Facet.ORDER).setType(MetadataValueType.NUMBER);
			facetSchema.get(Facet.TITLE).setMultiLingual(true);

			MetadataSchemaBuilder facetFieldSchema = facetTypes.createCustomSchema(Facet.FIELD_LOCAL_CODE);
			facetFieldSchema.createUndeletable(Facet.FIELD_VALUES_LABEL).setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(MapStringStringStructureFactory.class);

			MetadataSchemaBuilder facetQuerySchema = facetTypes.createCustomSchema(Facet.QUERY_LOCAL_CODE);
			facetQuerySchema.createUndeletable(Facet.LIST_QUERIES).setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(MapStringStringStructureFactory.class);

		}

		private void changeTokensCalculators(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema().getMetadata(User.USER_TOKENS)
					.defineDataEntry().asCalculated(UserTokensCalculator2.class);
			for (MetadataSchemaTypeBuilder type : typesBuilder.getTypes()) {
				type.getDefaultSchema().getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator2.class);
			}
		}

		private void createSendEmail(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaTypeBuilder schemaType = typesBuilder.createNewSchemaTypeWithSecurity(EmailToSend.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

			defaultSchema.createUndeletable(EmailToSend.FROM).setType(MetadataValueType.STRUCTURE).defineStructureFactory(
					EmailAddressFactory.class);
			defaultSchema.createUndeletable(EmailToSend.TO).setType(MetadataValueType.STRUCTURE).defineStructureFactory(
					EmailAddressFactory.class).setMultivalue(true);
			defaultSchema.createUndeletable(EmailToSend.BCC).setType(MetadataValueType.STRUCTURE).defineStructureFactory(
					EmailAddressFactory.class).setMultivalue(true);
			defaultSchema.createUndeletable(EmailToSend.CC).setType(MetadataValueType.STRUCTURE).defineStructureFactory(
					EmailAddressFactory.class).setMultivalue(true);
			defaultSchema.createUndeletable(EmailToSend.SUBJECT).setType(MetadataValueType.STRING);
			defaultSchema.createUndeletable(EmailToSend.PARAMETERS).setType(MetadataValueType.STRING).setMultivalue(true);
			defaultSchema.createUndeletable(EmailToSend.TEMPLATE).setType(MetadataValueType.STRING);
			defaultSchema.createUndeletable(EmailToSend.SEND_ON).setType(MetadataValueType.DATE_TIME);
			defaultSchema.createUndeletable(EmailToSend.TRYING_COUNT).setType(MetadataValueType.NUMBER).setDefaultValue(0d)
					.setDefaultRequirement(true);
			defaultSchema.createUndeletable(EmailToSend.ERROR).setType(MetadataValueType.STRING);
		}

		private MetadataSchemaTypeBuilder createReportSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(Report.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
			defaultSchema.get(Report.TITLE).setMultiLingual(true);
			defaultSchema.createUndeletable(Report.USERNAME).setType(STRING);
			defaultSchema.createUndeletable(Report.SCHEMA_TYPE_CODE).setType(STRING).setDefaultRequirement(true);
			defaultSchema.createUndeletable(Report.COLUMNS_COUNT).setType(NUMBER);
			defaultSchema.createUndeletable(Report.LINES_COUNT).setType(NUMBER).setDefaultRequirement(true);
			defaultSchema.createUndeletable(Report.SEPARATOR).setType(STRING);
			defaultSchema.createUndeletable(Report.REPORTED_METADATA).setType(STRUCTURE).setMultivalue(true)
					.defineStructureFactory(ReportedMetadataFactory.class);

			return type;
		}

		private MetadataSchemaTypeBuilder createSavedSearchSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(SavedSearch.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();

			defaultSchema.createUndeletable(SavedSearch.USER).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			defaultSchema.createUndeletable(SavedSearch.PUBLIC).setType(BOOLEAN);
			defaultSchema.createUndeletable(SavedSearch.SORT_FIELD).setType(STRING);
			defaultSchema.createUndeletable(SavedSearch.FACET_SELECTIONS).setType(STRUCTURE)
					.defineStructureFactory(FacetSelectionsFactory.class).setMultivalue(true);
			defaultSchema.createUndeletable(SavedSearch.FREE_TEXT_SEARCH).setType(STRING);
			defaultSchema.createUndeletable(SavedSearch.ADVANCED_SEARCH).setType(STRUCTURE)
					.defineStructureFactory(CriterionFactory.class).setMultivalue(true);
			defaultSchema.createUndeletable(SavedSearch.SCHEMA_FILTER).setType(STRING);
			defaultSchema.createUndeletable(SavedSearch.SEARCH_TYPE).setType(STRING);
			defaultSchema.createUndeletable(SavedSearch.SORT_ORDER).defineAsEnum(SortOrder.class);

			return type;
		}
	}
}
