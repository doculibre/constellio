package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateTimeFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDropDownWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveLookupWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveRichTextFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveTextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ResearchResultWebElement {

	ConstellioWebElement resultElement;
	ConstellioWebDriver resultDriver;

	public ResearchResultWebElement(ConstellioWebDriver driver) {
		this.resultElement = driver.findElement(By.className("main-component"));
		this.resultDriver = driver;
	}

	public ResearchResultWebElement clickBatchProcessingLinkAndRefreshDriver(ConstellioWebDriver driver) {
		ConstellioWebElement link = resultElement.findElement(By.className("v-button-window-button"));
		link.scrollIntoView();
		link.clickAndWaitForPageReload();
		resultDriver = driver;
		return this;
	}

	public void clickSaveButtonAndWaitForPageReload() {
		ConstellioWebElement button = getBatchProcessPopup().findElement(By.className("v-button-primary")); 
		button.clickAndWaitForPageReload();
	}

	public void clickFirstResultAndWaitForPageReload() {
		ConstellioWebElement choiceElement = resultDriver.findElement(By.className("v-button-search-result-title"));
		choiceElement.clickAndWaitForPageReload();
	}
	
	
	

	public ResearchResultWebElement toggleFirstCheckBoxInResult() {
		new CheckboxWebElement(resultElement).toggle();
		return this;
	}

	public ResearchResultWebElement toggleFirstCheckBoxInBatchProcessPopup(String checkboxId) {
		new CheckboxWebElement(getBatchProcessPopup().findElement(By.id(checkboxId))).toggle();
		return this;
	}

	public ResearchResultWebElement setModifyOption(String option) {
		new DropDownWebElement(getBatchProcessPopup().findElement(By.className("v-filterselect-prompt"))).typeAndSelectFirst(option);
		return this;
	}
	
	
	public DateFieldWebElement getDateField() {
		return new DateFieldWebElement(getBatchProcessPopup());
	}

	public DateFieldWebElement getDatetimeField() {
		return new DateFieldWebElement(getBatchProcessPopup(), "yyyy-MM-dd hh:mm:ss");
	}
	
	public TextFieldWebElement getTextField() {
		return new TextFieldWebElement(getBatchProcessPopup().findElement(By.className("v-textfield")));
	}

	public TextFieldWebElement getTextAreaField() {
		return new TextFieldWebElement(getBatchProcessPopup().findElement(By.className("v-textarea"))); 
	}
	
	public RichTextFieldWebElement getRichTextField() {
		return  new RichTextFieldWebElement(getBatchProcessPopup());
	}

	public DropDownWebElement getDropDown(String dropDownId) {
		return new DropDownWebElement(getBatchProcessPopup().findElement(By.id(dropDownId)));
	}

	public LookupWebElement getLookupField(String lookupId) {
		return new LookupWebElement(getBatchProcessPopup().findElement(By.id(lookupId)));
	}

	public ListAddRemoveDateFieldWebElement getListAddRemoveDateFieldWebElement() {
		return new ListAddRemoveDateFieldWebElement(getBatchProcessPopup());
	}

	public ListAddRemoveDateTimeFieldWebElement getListAddRemoveDateTimeFieldWebElement() {
		return new ListAddRemoveDateTimeFieldWebElement(getBatchProcessPopup());
	}
	
	public ListAddRemoveLookupWebElement getListAddRemoveLookupWebElement() {
		return new ListAddRemoveLookupWebElement(getBatchProcessPopup());
	}
	
	public ListAddRemoveTextFieldWebElement getListAddRemoveTextFieldWebElement() {
		return new ListAddRemoveTextFieldWebElement(getBatchProcessPopup());
	}

	public ListAddRemoveRichTextFieldWebElement getListAddRemoveRichTextFieldWebElement() {
		return new ListAddRemoveRichTextFieldWebElement(getBatchProcessPopup());
	}
	
	public ListAddRemoveDropDownWebElement getListAddRemoveDropDownWebElement() {
		return new ListAddRemoveDropDownWebElement(getBatchProcessPopup());
	}

	public ConstellioWebElement getBatchProcessPopup() {
		return resultDriver.waitUntilElementExist(By.className("v-window-contents"));
	}
}
