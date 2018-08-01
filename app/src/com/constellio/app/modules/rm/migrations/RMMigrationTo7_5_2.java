package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.rule.RuleYearTypesCalculator;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.utils.MaskUtils;

import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled;

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
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
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
			java.util.Map<Language, String> labels = MigrationUtil
					.getLabelsByLanguage(collection, modelLayerFactory, migrationResourcesProvider,
							"init.retentionRule.default.yearTypes");

			MetadataSchemaTypeBuilder dateTypeSchemaType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(YearType.SCHEMA_TYPE, labels, codeMetadataDisabled())
					.setSecurity(false);

			MetadataBuilder yearEnd = dateTypeSchemaType.getDefaultSchema().create(YearType.YEAR_END)
														.setType(MetadataValueType.STRING).setDefaultRequirement(true).setInputMask(MaskUtils.MM_DD);

			MetadataSchemaBuilder retentionRuleSchema = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE).getDefaultSchema();

			MetadataBuilder dateTypes = retentionRuleSchema.create(RetentionRule.YEAR_TYPES)
														   .defineReferencesTo(dateTypeSchemaType).setMultivalue(true)
														   .defineDataEntry().asCalculated(RuleYearTypesCalculator.class);

			retentionRuleSchema.create(RetentionRule.YEAR_TYPES_YEAR_END).setType(MetadataValueType.STRING).setMultivalue(true)
							   .defineDataEntry().asCopied(dateTypes, yearEnd);

		}
	}
}
