package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator.CategoryDefaultCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator.CategoryDefaultRetentionRuleCalculator;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

public class RMMigrationTo6_5_40 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.40";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor6_5_40(collection, provider, appLayerFactory).migrate();
		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(Category.SCHEMA_TYPE).addToDisplay(Category.DEFAULT_RETENTION_RULE, Category.DEFAULT_COPY_RULE)
				.afterMetadata(Category.RETENTION_RULES);

		schemaDisplayManager.execute(transaction.build());
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);

	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		manager.updateRole(
				manager.getRole(collection, RMRoles.RGD).withNewPermissions(asList(USE_EXTERNAL_APIS_FOR_COLLECTION)));
	}

	public static class SchemaAlterationsFor6_5_40 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_40(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder retentionRuleSchemaType = types().getSchemaType(RetentionRule.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder categorySchemaType = types().getSchemaType(Category.SCHEMA_TYPE);

			categorySchemaType.getDefaultSchema().create(Category.DEFAULT_COPY_RULE_ID).setType(MetadataValueType.STRING);

			categorySchemaType.getDefaultSchema().create(Category.DEFAULT_RETENTION_RULE)
					.defineReferencesTo(retentionRuleSchemaType)
					.defineDataEntry().asCalculated(CategoryDefaultRetentionRuleCalculator.class);

			categorySchemaType.getDefaultSchema().create(Category.DEFAULT_COPY_RULE)
					.defineStructureFactory(CopyRetentionRuleFactory.class)
					.defineDataEntry().asCalculated(CategoryDefaultCopyRuleCalculator.class);

		}
	}

}
