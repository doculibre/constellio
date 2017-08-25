package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_4_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.4.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class CoreSchemaAlterationFor7_4_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataBuilder temp : typesBuilder.getAllCalculatedMetadatas()) {
				if (temp.getDataEntry().getType().equals(DataEntryType.CALCULATED) && ((CalculatedDataEntry) temp.getDataEntry())
						.getCalculator() instanceof JEXLMetadataValueCalculator) {
					MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) temp.getDataEntry()).getCalculator();
					String currentScript = ((JEXLMetadataValueCalculator) calculator).getExpression();
					temp.defineDataEntry().asJexlScript("#STRICT:" + currentScript);
				}
			}
		}
	}
}
