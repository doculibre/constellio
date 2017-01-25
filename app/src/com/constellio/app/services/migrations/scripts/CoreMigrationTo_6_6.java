package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;

public class CoreMigrationTo_6_6 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		setDefaultTitleBoost(collection, appLayerFactory);
	}
	
	private void setDefaultTitleBoost(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchBoostManager searchBoostManager = modelLayerFactory.getSearchBoostManager();
		
		String titleTitle = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(Collection.DEFAULT_SCHEMA).get(Collection.TITLE).getLabel(Language.French);
		SearchBoost oldBoost = new SearchBoost(SearchBoost.QUERY_TYPE, "title_s", titleTitle, 20d);
		SearchBoost newBoost = new SearchBoost(SearchBoost.METADATA_TYPE, "title_s", titleTitle, 20d);
		searchBoostManager.delete(collection, oldBoost);
		searchBoostManager.add(collection, newBoost);
	}

}
