package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.numberfilter.NumberInterval;

import java.util.Date;

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
				if (getValue() instanceof String) {
					String stringValue = (String) getValue();
					if (StringUtils.isNotBlank(stringValue)) {
						if (metadata.isSortable()) {
							query.setCondition(query.getCondition().andWhere(metadata.getSortField()).isContainingText(AccentApostropheCleaner.cleanAll(stringValue.toLowerCase())));
						} else {
							query.setCondition(query.getCondition().andWhere(metadata).isContainingText(stringValue));
						}
					}
				}
				break;
			case DATE:
				if (getValue() instanceof DateInterval) {
					DateInterval interval = (DateInterval) getValue();
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
				if (getValue() instanceof NumberInterval) {
					NumberInterval interval = (NumberInterval) getValue();
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
						try {
							query.setCondition(query.getCondition().andWhere(metadata).isGreaterThan(new Double(greaterThanValue)));
						} catch (NumberFormatException e) {
							// Ignore
						}
					}
					if (StringUtils.isNotBlank(lessThanValue)) {
						try {
							query.setCondition(query.getCondition().andWhere(metadata).isLessThan(new Double(lessThanValue)));
						} catch (NumberFormatException e) {
							// Ignore
						}
					}
				}
				break;
			case REFERENCE:
				if (getValue() instanceof String) {
					String stringValue = (String) getValue();
					if (StringUtils.isNotBlank(stringValue)) {
						query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(value));
					}
				}
				break;
			default:
				break;
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
