package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_6(collection, provider, factory).migrate();

		addDefaultTaxonomy(factory.getModelLayerFactory());
	}

	private void addDefaultTaxonomy(ModelLayerFactory modelLayerFactory) {
		SystemConfigurationsManager configManager = modelLayerFactory.getSystemConfigurationsManager();
		String defaultTaxonomy = modelLayerFactory.getSystemConfigs().getDefaultTaxonomy(); 
		if (defaultTaxonomy == null) {
			configManager.setValue(ConstellioEIMConfigs.DEFAULT_TAXONOMY, RMTaxonomies.ADMINISTRATIVE_UNITS);			
		}
	}

	public static class SchemaAlterationsFor6_6 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_6(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}
	}

}
