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
import org.jetbrains.annotations.NotNull;

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
        Metadata metadata = getMetadata();

        switch (metadata.getType()) {
            case TEXT:
            case STRING:
	            {
	            	String stringValue = (String) getValue();
	            	if (StringUtils.isNotBlank(stringValue)) {
	                    query.setCondition(query.getCondition().andWhere(metadata).isStartingWithText(stringValue));
	            	}
	            }
                break;
            case REFERENCE:
            default:
            	if (getValue() instanceof String) {
                	String stringValue = (String) getValue();
                	if (StringUtils.isNotBlank(stringValue)) {
//                        query.setCondition(query.getCondition().andWhere(metadata).isStartingWithText(stringValue));
                	}
            	}
//                query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(value));
        }
    }

    protected Metadata getMetadata() {
        SchemaPresenterUtils schemaPresenterUtils = getSchemaPresenterUtils();
        return schemaPresenterUtils.getMetadata(getPropertyId().getLocalCode());
    }

    @NotNull
    protected SchemaPresenterUtils getSchemaPresenterUtils() {
        return new SchemaPresenterUtils(getPropertyId().getSchema().getCode(), ConstellioFactories.getInstance(), ConstellioUI.getCurrentSessionContext());
    }

    public final MetadataVO getPropertyId() {
        return propertyId;
    }

    public final Object getValue() {
        return value;
    }
}
