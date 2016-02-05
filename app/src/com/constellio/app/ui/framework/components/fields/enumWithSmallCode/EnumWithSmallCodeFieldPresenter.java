package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class EnumWithSmallCodeFieldPresenter implements Serializable {
	
	private EnumWithSmallCodeField enumWithSmallCodeField;

	public EnumWithSmallCodeFieldPresenter(EnumWithSmallCodeField enumWithSmallCodeField) {
		this.enumWithSmallCodeField = enumWithSmallCodeField;
	}
	
	public void forEnumClass(Class<? extends EnumWithSmallCode> enumWithSmallCodeClass) {
	    if (!enumWithSmallCodeClass.isEnum()) {
	    	throw new IllegalArgumentException(enumWithSmallCodeClass.getName() + " is not an enum");
	    }
	    List<EnumWithSmallCode> enumConstants = EnumWithSmallCodeUtils.toEnumWithSmallCodeConstants(enumWithSmallCodeClass);
		enumWithSmallCodeField.setOptions(enumConstants);
	}

}
