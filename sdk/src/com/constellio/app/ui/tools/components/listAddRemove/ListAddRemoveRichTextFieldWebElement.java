package com.constellio.app.ui.tools.components.listAddRemove;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveRichTextFieldWebElement  extends ListAddRemoveFieldWebElement<TextFieldWebElement> {

	public ListAddRemoveRichTextFieldWebElement(ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected TextFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new TextFieldWebElement(element);
	}

	public ListAddRemoveRichTextFieldWebElement add(String value) {
		getFrameComponent().sendKeys(value);
		getAddButtonWebElement().sendKeys("");
		getAddButtonWebElement().click();
		return this;
	}
	
	public ListAddRemoveRichTextFieldWebElement remove(int index) {
		getRemoveButtonWebElement(index).click();
		return this;
	}

	public ListAddRemoveRichTextFieldWebElement modifyTo(int index, String value) {
		clickModify(index);
		clearRichText();
		getFrameComponent().sendKeys(value);
		clickAdd();
		return this;
	}

	private void clearRichText() {
		getFrameComponent().sendKeys(Keys.CONTROL + "a");
		getFrameComponent().sendKeys(Keys.DELETE);
	}
	
	private ConstellioWebElement getFrameComponent() {
		return nestedElement.findElement(By.className("gwt-RichTextArea"));
	}
	

	
	

	
}
