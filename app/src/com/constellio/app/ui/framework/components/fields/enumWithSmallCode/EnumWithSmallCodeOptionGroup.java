package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;

public class EnumWithSmallCodeOptionGroup<E extends EnumWithSmallCode> extends ListOptionGroup implements EnumWithSmallCodeField {
	
	private Class<E> enumWithSmallCodeClass;
	
	private EnumWithSmallCodeFieldPresenter presenter;
	
	public EnumWithSmallCodeOptionGroup(Class<E> enumWithSmallCodeClass) {
		super();
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
		this.presenter = new EnumWithSmallCodeFieldPresenter(this);
		this.presenter.forEnumClass(enumWithSmallCodeClass);
	}

	@Override
	public void setOptions(List<EnumWithSmallCode> enumConstants) {
	    for (EnumWithSmallCode enumWithSmallCode : enumConstants) {
			String enumCode = enumWithSmallCode.getCode();
			addItem(enumWithSmallCode);
			// TODO Use EnumWithSmallCodeToCaptionConverter
			String caption = $(enumWithSmallCodeClass.getSimpleName() + "." + enumCode);
			setItemCaption(enumWithSmallCode, caption);
		}	
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
