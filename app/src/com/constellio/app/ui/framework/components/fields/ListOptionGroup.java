package com.constellio.app.ui.framework.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.Container;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.OptionGroup;

/**
 * @author Vincent
 */
public class ListOptionGroup extends OptionGroup {
	
	public static final String NULL_ITEM_ID = "__NULL__";

	public ListOptionGroup() {
		init();
	}

	public ListOptionGroup(String caption) {
		super(caption);
		init();
	}

	public ListOptionGroup(String caption, Collection<?> options) {
		super(caption, options);
		init();
	}

	public ListOptionGroup(String caption, Container dataSource) {
		super(caption, dataSource);
		init();
	}
	
	private void init() {
	}

    @Override
	public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
		super.setNullSelectionAllowed(nullSelectionAllowed);
		if (nullSelectionAllowed) {
			addItem(NULL_ITEM_ID);
			setNullSelectionItemId(NULL_ITEM_ID);
			setItemCaption(NULL_ITEM_ID, $("null"));
		} else {
			removeItem(NULL_ITEM_ID);
		}
	}

	@Override
	protected Object getInternalValue() {
		Object internalValue = super.getInternalValue();
		return NULL_ITEM_ID.equals(internalValue) ? null : internalValue;
	}

	@Override
	public void attach() {
		if (isMultiSelect()) {
			setConverter(new ObjectToListConverter());
		}
		super.attach();
	}

	/**
     * Returns the type of the property. <code>getValue</code> and
     * <code>setValue</code> methods must be compatible with this type: one can
     * safely cast <code>getValue</code> to given type and pass any variable
     * assignable to this type as a parameter to <code>setValue</code>.
     * 
     * @return the Type of the property.
     */
    @Override
    public Class<?> getType() {
        if (isMultiSelect()) {
            return List.class;
        } else {
            return Object.class;
        }
    }
	
	private static class ObjectToListConverter implements Converter<Object, List<?>> {

		@Override
		public List<?> convertToModel(Object value, Class<? extends List<?>> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			List<?> model;
			if (value instanceof List) {
				model = (List<?>) value;
			} else if (value instanceof Collection) {
				model = new ArrayList<Object>((Collection<?>) value);
			} else {
				model = null;
			}
			return model;
		}

		@Override
		public Object convertToPresentation(List<?> value, Class<? extends Object> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return value;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getModelType() {
			return List.class;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getPresentationType() {
			return Object.class;
		}
		
	}

}
