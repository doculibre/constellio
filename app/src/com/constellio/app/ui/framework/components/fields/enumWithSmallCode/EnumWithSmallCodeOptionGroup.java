package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;

import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class EnumWithSmallCodeOptionGroup<E extends EnumWithSmallCode> extends ListOptionGroup implements EnumWithSmallCodeField {

	private Class<E> enumWithSmallCodeClass;

	private EnumWithSmallCodeFieldPresenter presenter;

	MetadataDisplayType metadataDisplayType;

	public EnumWithSmallCodeOptionGroup(Class<E> enumWithSmallCodeClass) {
		super();
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
		this.presenter = new EnumWithSmallCodeFieldPresenter(this);
		this.presenter.forEnumClass(enumWithSmallCodeClass);
	}

	public EnumWithSmallCodeOptionGroup(Class<E> enumWithSmallCodeClass, MetadataDisplayType metadataDisplayType) {
		super();
		this.metadataDisplayType = metadataDisplayType;
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
		this.presenter = new EnumWithSmallCodeFieldPresenter(this);
		this.presenter.forEnumClass(enumWithSmallCodeClass);
	}

	@Override
	public void setOptions(List<EnumWithSmallCode> enumConstants) {
		initStyleName();
		for (EnumWithSmallCode enumWithSmallCode : enumConstants) {
			String enumCode = enumWithSmallCode.getCode();
			if(codesToIgnore().contains(enumCode)) {
				continue;
			}

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

	public void initStyleName() {
		if (metadataDisplayType != null && metadataDisplayType.equals(MetadataDisplayType.HORIZONTAL)) {
			this.addStyleName("horizontal");
		} else {
			this.addStyleName("vertical");
		}
	}

	public List<String> codesToIgnore() {
		return Collections.emptyList();
	}
}
