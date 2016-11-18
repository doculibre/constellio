package com.constellio.app.ui.framework.components.fields;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.data.Property;

public class BaseRichTextArea extends CKEditorTextField {

	public BaseRichTextArea() {
		super(newConfig());
		init();
	}

	public BaseRichTextArea(String caption) {
		super(newConfig());
		setCaption(caption);
		init();
	}

	public BaseRichTextArea(Property<?> dataSource) {
		super(newConfig());
		setPropertyDataSource(dataSource);
		init();
	}

	public BaseRichTextArea(String caption, Property<?> dataSource) {
		super(newConfig());
		setCaption(caption);
		setPropertyDataSource(dataSource);
		init();
	}

	public BaseRichTextArea(String caption, String value) {
		super(newConfig());
		setCaption(caption);
		setValue(value);
		init();
	}
	
	private void init() {
		setWidth("100%");
	}
	
	private static CKEditorConfig newConfig() {
		CKEditorConfig config = new CKEditorConfig();
        config.useCompactTags();
        config.disableElementsPath();
        config.setResizeDir(CKEditorConfig.RESIZE_DIR.HORIZONTAL);
        config.disableSpellChecker();
        config.setWidth("100%");
        return config;
	}

}
