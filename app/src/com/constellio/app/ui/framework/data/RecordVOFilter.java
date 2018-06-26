package com.constellio.app.ui.framework.data;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.numberfilter.NumberInterval;

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
            	if (value instanceof String) {
                	String stringValue = (String) value;
	            	if (StringUtils.isNotBlank(stringValue)) {
	                    query.setCondition(query.getCondition().andWhere(metadata).isStartingWithText(stringValue));
	            	}
	            }
                break;
            case DATE:
            	if (value instanceof DateInterval) {
            		DateInterval interval = (DateInterval) value;
            		Date from = interval.getFrom();
            		Date to = interval.getTo();
            		
            		if (from != null) {
            			query.setCondition(query.getCondition().andWhere(metadata).isGreaterOrEqualThan(new LocalDate(from.getTime())));
            		}
            		if (to != null) {
            			query.setCondition(query.getCondition().andWhere(metadata).isLessOrEqualThan(new LocalDate(to.getTime())));
            		}
            	}
            	break;
            case NUMBER:
            	if (value instanceof NumberInterval) {
            		NumberInterval interval = (NumberInterval) value;
            		String equalsValue = interval.getEqualsValue();
            		String lessThanValue = interval.getLessThanValue();
            		String greaterThanValue = interval.getGreaterThanValue();
            		
            		if (StringUtils.isNotBlank(equalsValue)) {
            			try {
                			query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(new Double(equalsValue)));
            			} catch (NumberFormatException e) {
            				// Ignore
            			}
            		}
            		if (StringUtils.isNotBlank(greaterThanValue)) {
            			query.setCondition(query.getCondition().andWhere(metadata).isGreaterOrEqualThan(new Double(greaterThanValue)));
            		}
            		if (StringUtils.isNotBlank(lessThanValue)) {
            			query.setCondition(query.getCondition().andWhere(metadata).isLessOrEqualThan(new Double(lessThanValue)));
            		}
            	}
            	break;
            case REFERENCE:
            	if (value instanceof String) {
                	String stringValue = (String) value;
                	if (StringUtils.isNotBlank(stringValue)) {
                      query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(value));
                	}
            	} 
                break;
            default:
                break;
        }
    }
}
