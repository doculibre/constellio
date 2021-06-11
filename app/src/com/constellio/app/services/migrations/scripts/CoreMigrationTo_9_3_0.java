package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Source;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import com.constellio.model.services.schemas.calculators.TokensCalculator5;

import java.util.Map;

public class CoreMigrationTo_9_3_0 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.3.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_3_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_3_0 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_3_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
					migrationResourcesProvider, "init.ddvSource");

			new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(Source.SCHEMA_TYPE, mapLanguage,
							ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique())
					.setSecurity(false);

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				if (typeBuilder.getDefaultSchema().hasMetadata(Schemas.TOKENS.getLocalCode())) {
					MetadataBuilder tokens = typeBuilder.getDefaultSchema().getMetadata(Schemas.TOKENS.getLocalCode());
					if (tokens.getDataEntry().getType() == DataEntryType.CALCULATED && ((CalculatedDataEntry) tokens.getDataEntry()).getCalculator() instanceof TokensCalculator4) {
						tokens.defineDataEntry().asCalculated(TokensCalculator5.class);
					}
				}
			}
		}
	}
}
