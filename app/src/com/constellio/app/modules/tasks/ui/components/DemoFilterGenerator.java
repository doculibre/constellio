package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import org.joda.time.LocalDate;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.numberfilter.NumberFilterPopup;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;

public class DemoFilterGenerator implements FilterGenerator {

	@Override
	public Container.Filter generateFilter(final Object propertyId, final Object value) {
		if (propertyId instanceof MetadataVO) {
			return new DemoRecordVOFilter((MetadataVO) propertyId, value);
		}

		// For other properties, use the default filter
		return null;
	}

	@Override
	public Container.Filter generateFilter(Object propertyId, Field<?> originatingField) {
		Object value = originatingField.getValue();
		return generateFilter(propertyId, value);
	}

	@Override
	public AbstractField<?> getCustomFilterComponent(Object propertyId) {
		AbstractField<?> customFilterComponent = null;
		if (propertyId instanceof MetadataVO) {
			MetadataVO metadataVO = (MetadataVO) propertyId;
			Class<?> javaType = metadataVO.getJavaType();
			if (((MetadataVO) propertyId).codeMatches(Task.STARRED_BY_USERS)) {
				ComboBox cb = new ComboBox() {
					@Override
					public void setValue(Object newValue) throws ReadOnlyException {
						super.setValue(newValue);
					}
				};
				cb.addItem(new SpecialBoolean($("yes"), true));
				cb.addItem(new SpecialBoolean($("no"), false));
				cb.setWidth("100%");
				cb.setHeight("24px");
				customFilterComponent = cb;
			} else if (LocalDate.class.isAssignableFrom(javaType)) {
				customFilterComponent = new DateFilterPopup(new DemoFilterDecorator(), propertyId);
			} else if (Number.class.isAssignableFrom(javaType)) {
				customFilterComponent = new NumberFilterPopup(new DemoFilterDecorator());
			} else {
				MetadataFieldFactory factory = new TaskFieldFactory(false) {
					@Override
					public Field<?> build(MetadataVO metadata, Locale locale) {
						Field<?> field;
						if (DATE_TIME.equals(metadata.getType())) {
							field = new JodaDateField();
							return field;
						}
						return super.build(metadata, locale);
					}
				};
				final Field<?> field = factory.build(metadataVO);
				if (field != null) {
					if ((field instanceof AbstractTextField || field instanceof ComboBox) && !(field instanceof BaseTextArea)) {
						customFilterComponent = (AbstractField) field;
					} else {
						customFilterComponent = new FilterWindowButtonField(field);
					}
				}
			}
		}

		return customFilterComponent;
	}

	public static class SpecialBoolean {
		private final boolean value;
		private final String displayValue;

		public SpecialBoolean(String displayValue, boolean value) {
			this.value = value;
			this.displayValue = displayValue;
		}

		public boolean getBoolean() {
			return value;
		}

		@Override
		public String toString() {
			return displayValue;
		}
	}

	@Override
	public void filterRemoved(Object propertyId) {
	}

	@Override
	public void filterAdded(Object propertyId, Class<? extends Container.Filter> filterType, Object value) {
	}

	@Override
	public Container.Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
		return null;
	}
}
