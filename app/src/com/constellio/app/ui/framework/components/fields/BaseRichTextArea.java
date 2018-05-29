package com.constellio.app.ui.framework.components.fields;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Property;

public class BaseRichTextArea extends CKEditorTextField {

	public BaseRichTextArea() {
		super(newConfig());
		init();
	}

	public BaseRichTextArea(String caption) {
		super(newConfig());
		init();
		setCaption(caption);
	}

	public BaseRichTextArea(Property<?> dataSource) {
		super(newConfig());
		init();
		setPropertyDataSource(dataSource);
	}

	public BaseRichTextArea(String caption, Property<?> dataSource) {
		super(newConfig());
		init();
		setCaption(caption);
		setPropertyDataSource(dataSource);
	}

	public BaseRichTextArea(String caption, String value) {
		super(newConfig());
		init();
		setCaption(caption);
		setValue(value);
	}

	private void init() {
		setWidth("100%");
		setHeight("600px");
		setCaptionAsHtml(true);
		addStyleName("base-rich-text");
	}

	@Override
	public void setRequired(boolean required) {
		super.setRequired(required);
		if (required) {
			String caption = getCaption();
			String suffix = "<span class=\"v-required-field-indicator\" aria-hidden=\"true\">*</span>";
			setCaption(caption + suffix);
		}
	}

	private static CKEditorConfig newConfig() {
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		String toolbarConfig = modelLayerFactory.getSystemConfigs().getCKEditorToolbarConfig();
		CKEditorConfig config = new CKEditorConfig();
		config.addCustomToolbarLine(toolbarConfig);
		if (isRightToLeft()) {
//			config.addExtraConfig("contentsLangDirection",  "rtl");
//			config.addExtraConfig("language", "fr");
		}
		config.setWidth("100%");
		return config;
	}
}
