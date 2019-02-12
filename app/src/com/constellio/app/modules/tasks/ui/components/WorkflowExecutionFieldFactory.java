package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

import java.util.Locale;

public class WorkflowExecutionFieldFactory extends MetadataFieldFactory {

	@Override
	public Field<?> build(MetadataVO metadata, Locale locale) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		String currentCollection = metadata.getCollection();
		Field<?> field = appLayerFactory.getExtensions().forCollection(currentCollection).getFieldForMetadata();
		postBuild(field, metadata);
		if(field instanceof CustomTaskField) {
			postBuild(field, metadata);
		}
		return field;
	}
}
