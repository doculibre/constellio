package com.constellio.app.ui.tools;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateTimeFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDropDownWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveLookupWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveRichTextFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveTextFieldWebElement;
import com.constellio.app.ui.tools.vaadin.TextFieldWebFacade;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.conditions.ConditionTimeoutRuntimeException;

public class RecordFormWebElement {

	ConstellioWebElement nestedElement;

	public RecordFormWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
	}

	@Deprecated
	public RecordFormWebElement setValue(String metadataCode, Object value) {
		TextFieldWebFacade textFieldWebFacade = new TextFieldWebFacade(nestedElement.findElement(By.id(metadataCode)));
		textFieldWebFacade.setValue(value.toString());
		return this;
	}

	public RecordFormWebElement setValue(String value) {
		setValue(value);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> T getValue(String metadataCode) {
		TextFieldWebFacade textFieldWebFacade = new TextFieldWebFacade(nestedElement.findElement(By.id(metadataCode)));
		return (T) textFieldWebFacade.getValue();
	}

	public void clickSaveButtonAndWaitForPageReload() {
		ConstellioWebElement button = nestedElement.findElement(By.className(RecordForm.SAVE_BUTTON));
		button.clickAndWaitForPageReload();
	}

	public void clickCancelButtonAndWaitForPageReload() {
		ConstellioWebElement button = nestedElement.findElement(By.className(RecordForm.CANCEL_BUTTON));
		button.clickAndWaitForPageReload();
	}

	public ListAddRemoveTextFieldWebElement getListAddRemoveTextField(String metadataCode) {
		return new ListAddRemoveTextFieldWebElement(getField(metadataCode));
	}

	public ListAddRemoveRichTextFieldWebElement getListAddRemoveRichTextField(String metadataCode) {
		return new ListAddRemoveRichTextFieldWebElement(getField(metadataCode));
	}

	public ListAddRemoveLookupWebElement getListAddRemoveLookupWebElement(String metadataCode) {
		return new ListAddRemoveLookupWebElement(getField(metadataCode));
	}

	public ListAddRemoveDropDownWebElement getListAddRemoveDropDownWebElement(String metadataCode) {
		return new ListAddRemoveDropDownWebElement(getField(metadataCode));
	}

	public ListAddRemoveDateFieldWebElement getListAddRemoveDateFieldWebElement(String metadataCode) {
		return new ListAddRemoveDateFieldWebElement(getField(metadataCode));
	}

	public ListAddRemoveDateTimeFieldWebElement getListAddRemoveDateTimeFieldWebElement(String metadataCode) {
		return new ListAddRemoveDateTimeFieldWebElement(getField(metadataCode));
	}

	public AutocompleteWebElement getDropDown(String metadataCode) {
		return new DropDownWebElement(getField(metadataCode));
	}

	public LookupWebElement getLookupField(String metadataCode) {
		return new LookupWebElement(getField(metadataCode));
	}

	public OptionGroupWebElement getRadioButton(String metadataCode) {
		return new OptionGroupWebElement(getField(metadataCode));
	}

	public RichTextFieldWebElement getRichTextField(String metadataCode) {
		return new RichTextFieldWebElement(getField(metadataCode));
	}

	public TextFieldWebElement getTextField(String metadataCode) {
		return new TextFieldWebElement(getField(metadataCode));
	}

	public DateFieldWebElement getDateField(String metadataCode) {
		return new DateFieldWebElement(getField(metadataCode));
	}

	public DateFieldWebElement getDatetimeField(String metadataCode) {
		return new DateFieldWebElement(getField(metadataCode), "yyyy-MM-dd hh:mm:ss");
	}

	public RecordFormWebElement toggleAllCheckbox() {
		List<ConstellioWebElement> listCheckbox = nestedElement.findAdaptElements(By.className("v-checkbox"));
		for (ConstellioWebElement checkboxNumber : listCheckbox) {
			CheckboxWebElement checkboxElement = new CheckboxWebElement(checkboxNumber);
			checkboxElement.toggle();
		}
		return this;
	}

	public RecordFormWebElement toggleFirstCheckbox(String metadataCode) {
		new CheckboxWebElement(getField(metadataCode)).toggle();
		return this;
	}

	public RecordFormWebElement removeCheckbox() {
		List<ConstellioWebElement> listCheckbox = nestedElement.findAdaptElements(By.className("v-checkbox"));
		for (ConstellioWebElement checkboxNumber : listCheckbox) {
			CheckboxWebElement checkboxElement = new CheckboxWebElement(checkboxNumber);
			if (checkboxElement.isChecked()) {
				checkboxElement.toggle();
			}
		}
		return this;
	}

	public ConstellioWebElement getField(String metadataCode) {
		try {
			nestedElement.waitUntilElementExist(By.id(metadataCode));
			ConstellioWebElement field = nestedElement.findElement(By.id(metadataCode));
			field.scrollIntoView();
			return field;
		} catch (ConditionTimeoutRuntimeException e) {
			throw new RuntimeException("Could not locate field '" + metadataCode + "'", e);
		}
	}

	public boolean isVisible(String metadataCode) {
		try {
			nestedElement.findElement(By.id(metadataCode)).getText();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
