package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.MaskUtils;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents;
import org.apache.commons.lang.StringUtils;
import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseRichTextArea extends CKEditorTextField {

	private String inputMask;

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
		//		setHeight("600px");
		setCaptionAsHtml(true);
		addStyleName("base-rich-text");

		addFocusListener(new FieldEvents.FocusListener() {
			@Override
			public void focus(FieldEvents.FocusEvent event) {
				if (StringUtils.isNotBlank(inputMask)) {
					String id = getId();
					if (id == null) {
						id = UUID.randomUUID().toString();
						setId(id);
					}
					StringBuffer js = new StringBuffer();
					if (MaskUtils.MM_DD.equals(inputMask)) {
						inputMask = "m/d";
					}
					js.append("$(document).ready(function() {");
					js.append(" $(\"#" + id + "\").inputmask(\"" + inputMask + "\"); ");
					js.append("})");

					com.vaadin.ui.JavaScript.eval(js.toString());
				}
			}
		});
	}

	public String getInputMask() {
		return inputMask;
	}

	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
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

	@Override
	public void validate() throws InvalidValueException {
		try {
			validateIsContainingABustedMaxLength();
		} catch (InvalidValueException e) {
			throw e;
		}
		super.validate();
	}

	private void validateIsContainingABustedMaxLength() throws InvalidValueException {
		//Determines if there is a maxLength by splitting caption, might be broken if there ever is another status
		//adding a number between parenthesis to a caption.
		if (this.getCaption() != null && this.getValue() != null) {
			if (this.getCaption().contains("(") && this.getCaption().contains(")")) {
				boolean isCaptionsPartBetweenParenthesisRepresentingMaxLength =
						Character.isDigit(this.getCaption().split("\\(")[1].charAt(0));
				if (isCaptionsPartBetweenParenthesisRepresentingMaxLength) {
					Integer maxLength = Character.getNumericValue(this.getCaption().split("\\(")[1].charAt(0));
					if (this.getValue().length() > maxLength) {
						throw new InvalidValueException($("maxLengthBusted", maxLength));
					}
				}
			}
		}
	}
}
