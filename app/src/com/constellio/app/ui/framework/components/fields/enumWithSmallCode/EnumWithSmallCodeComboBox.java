package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class EnumWithSmallCodeComboBox<E extends EnumWithSmallCode> extends BaseComboBox implements EnumWithSmallCodeField {

	private Class<E> enumWithSmallCodeClass;

	private EnumWithSmallCodeFieldPresenter presenter;

	private boolean codeAsCaption;

	public EnumWithSmallCodeComboBox(Class<E> enumWithSmallCodeClass) {
		this(enumWithSmallCodeClass, false);
	}

	public EnumWithSmallCodeComboBox(Class<E> enumWithSmallCodeClass, boolean codeAsCaption) {
		super();
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
		this.codeAsCaption = codeAsCaption;
		this.presenter = new EnumWithSmallCodeFieldPresenter(this);
		this.presenter.forEnumClass(enumWithSmallCodeClass);
	}

	@Override
	public void setOptions(List<EnumWithSmallCode> enumConstants) {
		Collections.sort(enumConstants, new Comparator<EnumWithSmallCode>() {
			@Override
			public int compare(EnumWithSmallCode o1, EnumWithSmallCode o2) {
				String caption1 = $(DisposalType.class.getSimpleName() + "." + o1.getCode());
				String caption2 = $(DisposalType.class.getSimpleName() + "." + o2.getCode());
				return caption1.compareTo(caption2);
			}
		});
	    for (EnumWithSmallCode enumWithSmallCode : enumConstants) {
			String enumCode = enumWithSmallCode.getCode();
			if (!isIgnored(enumCode)) {
				addItem(enumWithSmallCode);
				String caption;
				if (codeAsCaption) {
					caption = enumCode;
				} else {
					// TODO Use EnumWithSmallCodeToCaptionConverter
					caption = $(enumWithSmallCodeClass.getSimpleName() + "." + enumCode);
				}
				setItemCaption(enumWithSmallCode, caption);
			}
		}
	}

	protected boolean isIgnored(String enumCode) {
		return false;
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
