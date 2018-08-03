package com.constellio.app.ui.framework.components.display;

import com.constellio.model.entities.EnumWithSmallCode;
import com.vaadin.ui.Label;

import static com.constellio.app.ui.i18n.i18n.$;

public class EnumWithSmallCodeDisplay<T extends EnumWithSmallCode> extends Label {

	@SuppressWarnings("unchecked")
	public EnumWithSmallCodeDisplay(T enumWithSmallCode) {
		if (enumWithSmallCode != null) {
			Class<T> enumWithSmallCodeClass = (Class<T>) enumWithSmallCode.getClass();
			String enumCode = enumWithSmallCode.getCode();
			String caption = $(enumWithSmallCodeClass.getSimpleName() + "." + enumCode);
			setValue(caption);
		}
	}

}
