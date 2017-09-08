package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RMMigrationTo7_3_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.3.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_3_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setFilePlanTaxonomyLabel(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
		setAdmUnitsTaxonomyLabel(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);

	}

	private void setFilePlanTaxonomyLabel(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		TaxonomiesManager manager = modelLayerFactory.getTaxonomiesManager();
		Taxonomy filePlanTaxo = manager.getEnabledTaxonomyWithCode(collection, RMTaxonomies.CLASSIFICATION_PLAN);
		filePlanTaxo = filePlanTaxo.withTitle(migrationResourcesProvider.getDefaultLanguageString("init.rm.plan"));
		manager.editTaxonomy(filePlanTaxo);

	}

	private void setAdmUnitsTaxonomyLabel(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		TaxonomiesManager manager = modelLayerFactory.getTaxonomiesManager();
		Taxonomy admUnitTaxo = manager.getEnabledTaxonomyWithCode(collection, RMTaxonomies.ADMINISTRATIVE_UNITS);
		admUnitTaxo = admUnitTaxo.withTitle(migrationResourcesProvider.getDefaultLanguageString("taxo.admUnits"));
		manager.editTaxonomy(admUnitTaxo);

	}

	class SchemaAlterationFor7_3_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_3_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.3.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
		}
	}

}
