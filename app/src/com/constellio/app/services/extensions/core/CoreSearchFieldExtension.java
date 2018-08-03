package com.constellio.app.services.extensions.core;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.events.schemas.SearchFieldPopulatorParams;
import org.jsoup.Jsoup;

public class CoreSearchFieldExtension extends SchemaExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public CoreSearchFieldExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public Object populateSearchField(SearchFieldPopulatorParams params) {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		MetadataInputType inputType = displayManager.getMetadata(collection, params.getMetadata().getCode()).getInputType();
		if (MetadataInputType.RICHTEXT == inputType && params.getValue() != null) {
			return Jsoup.parse((String) params.getValue()).text();
		}
		return super.populateSearchField(params);
	}
}
