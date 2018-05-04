package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_7_4 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new RMMigrationTo7_7_4.RMSchemaAlterationFor_7_7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class RMSchemaAlterationFor_7_7_4 extends MetadataSchemasAlterationHelper {

		protected RMSchemaAlterationFor_7_7_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder categoryDefaultSchema = typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE);
			categoryDefaultSchema.getMetadata(Category.TITLE).setMultiLingual(true);
			categoryDefaultSchema.getMetadata(Category.DESCRIPTION).setMultiLingual(true);
			categoryDefaultSchema.getMetadata(Category.KEYWORDS).setMultiLingual(true);

			MetadataSchemaBuilder administrativeUnitDefaultSchema = typesBuilder.getDefaultSchema(AdministrativeUnit.SCHEMA_TYPE);
			administrativeUnitDefaultSchema.getMetadata(AdministrativeUnit.TITLE).setMultiLingual(true);
			administrativeUnitDefaultSchema.getMetadata(AdministrativeUnit.DESCRIPTION).setMultiLingual(true);

			MetadataSchemaBuilder retentionRuleDefaultSchema = typesBuilder.getDefaultSchema(RetentionRule.SCHEMA_TYPE);

			retentionRuleDefaultSchema.getMetadata(RetentionRule.TITLE).setMultiLingual(true);
			retentionRuleDefaultSchema.getMetadata(RetentionRule.DESCRIPTION).setMultiLingual(true);
			retentionRuleDefaultSchema.getMetadata(RetentionRule.COPY_RULES_COMMENT).setMultiLingual(true);
			retentionRuleDefaultSchema.getMetadata(RetentionRule.GENERAL_COMMENT).setMultiLingual(true);
			retentionRuleDefaultSchema.getMetadata(RetentionRule.JURIDIC_REFERENCE).setMultiLingual(true);
			retentionRuleDefaultSchema.getMetadata(RetentionRule.KEYWORDS).setMultiLingual(true);

		}
	}
}
