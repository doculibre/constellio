package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.ui.framework.components.converters.CommentToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

public class CommentField extends CustomField<Comment> {

	private CommentToStringConverter converter = new CommentToStringConverter();

	private BaseTextArea textArea;

	public CommentField() {
		textArea = new BaseTextArea();
		textArea.setConverter(converter);
		textArea.setImmediate(false);
		textArea.setWidth("100%");
		textArea.setPropertyDataSource(this);
	}

	@Override
	protected Component initContent() {
		return textArea;
	}

	@Override
	public Class<? extends Comment> getType() {
		return Comment.class;
	}

	@SuppressWarnings("rawtypes")
	public Property getPropertyDataSource() {
		return textArea.getPropertyDataSource();
	}

	@SuppressWarnings("rawtypes")
	public void setPropertyDataSource(Property newDataSource) {
		textArea.setPropertyDataSource(newDataSource);
	}

	public void setValue(Comment newValue)
			throws com.vaadin.data.Property.ReadOnlyException {
		super.setValue(newValue);
		textArea.setValue(converter.convertToPresentation(newValue, String.class, getLocale()));
	}

	public void addStyleName(String style) {
		textArea.addStyleName(style);
	}

	public void focus() {
		textArea.focus();
	}

	public String getRequiredError() {
		return textArea.getRequiredError();
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		textArea.setRequiredError(requiredMessage);
	}

	@Override
	public String getConversionError() {
		return textArea.getConversionError();
	}

	@Override
	public void setConversionError(String valueConversionError) {
		textArea.setConversionError(valueConversionError);
	}

	@Override
	public ErrorMessage getComponentError() {
		return textArea.getComponentError();
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		textArea.setComponentError(componentError);
	}

	public void commit()
			throws SourceException, InvalidValueException {
		textArea.commit();
	}

	public void discard()
			throws SourceException {
		textArea.discard();
	}

	public boolean isValid() {
		return textArea.isValid();
	}

	public void validate()
			throws InvalidValueException {
		textArea.validate();
	}

}