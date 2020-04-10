package com.constellio.app.ui.pages.search;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCriteriaPresenterUtils extends BasePresenterUtils {

	public SearchCriteriaPresenterUtils(SessionContext sessionContext) {
		super(null, sessionContext);
	}

	public final Map<String, String> getMetadataSchemasList(String schemaTypeCode) {
		Map<String, String> metadataSchemasMap = new HashMap<>();
		String collection = sessionContext.getCurrentCollection();
		String language = sessionContext.getCurrentLocale().getLanguage();
		List<MetadataSchema> metadataSchemas = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(schemaTypeCode).getAllSchemas();
		for (MetadataSchema metadataSchema : metadataSchemas) {
			if (showMetadataSchema(metadataSchema)) {
				metadataSchemasMap.put(metadataSchema.getCode(), metadataSchema.getLabel(Language.withCode(language)));
			}
		}
		return metadataSchemasMap;
	}

	public boolean showMetadataSchema(MetadataSchema metadataSchema) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(constellioFactories.getModelLayerFactory().getSystemConfigurationsManager());
		return metadataSchema.isActive() || configs.areInactifSchemasEnabledInSearch();
	}
}
