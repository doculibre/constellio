package com.constellio.app.ui.framework.components.fields;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.ui.ComboBox;

public class BaseComboBox extends ComboBox {
	
	public static final String COMBO_BOX_STYLE = "v-filterselect-suggestmenu";

	public BaseComboBox() {
		init();
	}

	public BaseComboBox(String caption, Collection<?> options) {
		super(caption, options);
		init();
	}

	public BaseComboBox(String caption, Container dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseComboBox(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		addStyleName(COMBO_BOX_STYLE);
		if (isRightToLeft()) {
			addStyleName("v-filterselect-rtl");
			setItemStyleGenerator(new ItemStyleGenerator() {
				@Override
				public String getStyle(ComboBox source, Object itemId) {
					return "rtl";
				}
			});
		}
	}
	
}
