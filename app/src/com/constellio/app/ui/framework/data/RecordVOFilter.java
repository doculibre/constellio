package com.constellio.app.ui.framework.data;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Container;
import com.vaadin.data.Item;

public class RecordVOFilter implements Container.Filter {
    private final MetadataVO propertyId;
    private final Object value;

    public RecordVOFilter(MetadataVO propertyId, Object value) {
        this.propertyId = propertyId;
        this.value = value;
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return false;
    }

    public void addCondition(LogicalSearchQuery query) {
        SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(propertyId.getSchema().getCode(), ConstellioFactories.getInstance(), ConstellioUI.getCurrentSessionContext());
        Metadata metadata = schemaPresenterUtils.getMetadata(propertyId.getLocalCode());

        switch (metadata.getType()) {
            case TEXT:
            case STRING:
	            {
	            	String stringValue = (String) value;
	            	if (StringUtils.isNotBlank(stringValue)) {
	                    query.setCondition(query.getCondition().andWhere(metadata).isStartingWithText(stringValue));
	            	}
	            }
                break;
            case REFERENCE:
            default:
            	if (value instanceof String) {
                	String stringValue = (String) value;
                	if (StringUtils.isNotBlank(stringValue)) {
//                        query.setCondition(query.getCondition().andWhere(metadata).isStartingWithText(stringValue));
                	}
            	}
//                query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(value));
        }
    }
}
