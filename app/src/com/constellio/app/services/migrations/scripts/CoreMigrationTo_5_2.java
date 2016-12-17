package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.search.entities.SearchBoost;

public class CoreMigrationTo_5_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		String titleTitle = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(Collection.DEFAULT_SCHEMA).get(Collection.TITLE).getLabel(Language.French);
		SearchBoost boost = new SearchBoost(SearchBoost.QUERY_TYPE, "title_s", titleTitle, 20d);

		appLayerFactory.getModelLayerFactory().getSearchBoostManager().add(collection, boost);

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

}
