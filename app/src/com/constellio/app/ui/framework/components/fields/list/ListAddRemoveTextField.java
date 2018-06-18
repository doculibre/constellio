package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.model.utils.MaskUtils;
import com.constellio.model.utils.MaskUtilsException;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

@SuppressWarnings("unchecked")
public class ListAddRemoveTextField extends ListAddRemoveField<String, TextField> {

	private String inputMask = null;

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected TextField newAddEditField() {
		BaseTextField baseTextField = new BaseTextField();
		if(inputMask != null) {
			baseTextField.setInputMask(inputMask);
//			baseTextField.addValidator(new Validator() {
//				@Override
//				public void validate(Object value) throws InvalidValueException {
//					try {
//						if(value != null) {
//							MaskUtils.validate(inputMask, (String) value);
//						}
//					} catch (MaskUtilsException e) {
//						throw new InvalidValueException(e.getMessage());
//					}
//				}
//			});
		}
		return baseTextField;
	}

	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
	}
}
