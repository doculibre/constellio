package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.converters.CommentToStringConverter;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditablePasswordField extends CustomField<String> {

	private CommentToStringConverter converter = new CommentToStringConverter();

	private I18NHorizontalLayout mainLayout;
	private BasePasswordField passwordField;
	private BaseButton editPasswordButton;

	public EditablePasswordField() {
		this(null);
	}

	public EditablePasswordField(String caption) {
	    setCaption(caption);
		mainLayout = new I18NHorizontalLayout();
		mainLayout.setSpacing(true);

		passwordField = new BasePasswordField();
		passwordField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				String newValue = (String) event.getProperty().getValue();
				EditablePasswordField.this.setValue(newValue);
				passwordField.setInternalValue(StringUtils.repeat("*", StringUtils.defaultIfBlank((String) newValue, "").length()));
				boolean readOnly = !StringUtils.isBlank(getValue());
				passwordField.setReadOnly(readOnly);
				editPasswordButton.setVisible(readOnly);
			}
		});

		editPasswordButton = new BaseButton($("EditablePasswordField.editPasswordButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				passwordField.setReadOnly(false);
				passwordField.setValue(null);
			}
		};
		passwordField.setValue(getValue());
		editPasswordButton.addStyleName(ValoTheme.BUTTON_LINK);
		mainLayout.addComponents(passwordField, editPasswordButton);
		mainLayout.setComponentAlignment(editPasswordButton, Alignment.MIDDLE_LEFT);
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@SuppressWarnings("rawtypes")
	public Property getPropertyDataSource() {
		return super.getPropertyDataSource();//passwordField.getPropertyDataSource();
	}

	@SuppressWarnings("rawtypes")
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(newDataSource);//passwordField.setPropertyDataSource(newDataSource);
	}

	public void setValue(String newValue)
			throws ReadOnlyException {
		super.setValue(newValue);
		if(passwordField != null) {
			passwordField.setValue(newValue);
		}
	}

	public void addStyleName(String style) {
		passwordField.addStyleName(style);
	}

	public void focus() {
		passwordField.focus();
	}

	public String getRequiredError() {
		return passwordField.getRequiredError();
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		passwordField.setRequiredError(requiredMessage);
	}

	@Override
	public String getConversionError() {
		return passwordField.getConversionError();
	}

	@Override
	public void setConversionError(String valueConversionError) {
		passwordField.setConversionError(valueConversionError);
	}

	@Override
	public ErrorMessage getComponentError() {
		return passwordField.getComponentError();
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		passwordField.setComponentError(componentError);
	}

	public void commit()
			throws SourceException, InvalidValueException {
		passwordField.commit();
	}

	public void discard()
			throws SourceException {
		passwordField.discard();
	}

	public boolean isValid() {
		return passwordField.isValid();
	}

	public void validate()
			throws InvalidValueException {
		passwordField.validate();
	}


}