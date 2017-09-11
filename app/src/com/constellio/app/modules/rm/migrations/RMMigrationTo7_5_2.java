package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.rule.RuleDateTypesCalculator;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DateType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_5_2 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.5.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_5_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_5_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaTypeBuilder dateTypeSchemaType = typesBuilder.createNewSchemaType(DateType.SCHEMA_TYPE);
			MetadataBuilder yearEnd = dateTypeSchemaType.getDefaultSchema().create(DateType.YEAR_END)
					.setType(MetadataValueType.STRING).setDefaultRequirement(true);

			MetadataSchemaBuilder retentionRuleSchema = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder dateTypes = retentionRuleSchema.create(RetentionRule.DATE_TYPES)
					.defineReferencesTo(dateTypeSchemaType).setMultivalue(true)
					.defineDataEntry().asCalculated(RuleDateTypesCalculator.class);
			retentionRuleSchema.create(RetentionRule.DATE_TYPES_YEAR_END).setType(MetadataValueType.STRING).setMultivalue(true)
					.defineDataEntry().asCopied(dateTypes, yearEnd);
		}

	}
}
