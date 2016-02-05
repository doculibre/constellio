package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.StringToEnumWithSmallCodeConverter;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.model.entities.EnumWithSmallCode;

@SuppressWarnings("unchecked")
public class ListAddRemoveEnumWithSmallCodeComboBox<E extends EnumWithSmallCode> extends ListAddRemoveField<E, EnumWithSmallCodeComboBox<E>> {

	private Class<E> enumWithSmallCodeClass;

	@SuppressWarnings("rawtypes")
	public ListAddRemoveEnumWithSmallCodeComboBox(Class<E> enumWithSmallCodeClass) {
		super();
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
		setItemConverter(new StringToEnumWithSmallCodeConverter(enumWithSmallCodeClass));
	}

	@Override
	protected EnumWithSmallCodeComboBox<E> newAddEditField() {
		return new EnumWithSmallCodeComboBox<E>(enumWithSmallCodeClass);
	}

}
