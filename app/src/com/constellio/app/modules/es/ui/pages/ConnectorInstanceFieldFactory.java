package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.ui.components.TraversalSchedulesComponent;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.vaadin.ui.Field;

public class ConnectorInstanceFieldFactory extends RecordFieldFactory {
	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadata) {
		if (metadata.getCode().contains(ConnectorInstance.CONNECTOR_TYPE)) {
			Field<?> field = super.build(recordVO, metadata);
			if (field != null) {
				field.setReadOnly(true);
			}
			return field;
		} else if (metadata.getCode().contains(ConnectorInstance.PROPERTIES_MAPPING)) {
			return null;
		} else if (metadata.getCode().contains(ConnectorInstance.TRAVERSAL_CODE)) {
			return null;
		} else if (metadata.getCode().contains(ConnectorInstance.TRAVERSAL_SCHEDULE)) {
			TraversalSchedulesComponent field = new TraversalSchedulesComponent(recordVO, metadata);
			postBuild(field, recordVO, metadata);
			return field;
		} else {
			return super.build(recordVO, metadata);
		}
	}
}
