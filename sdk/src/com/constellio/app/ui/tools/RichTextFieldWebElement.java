package com.constellio.app.ui.tools;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class RichTextFieldWebElement {
	
	ConstellioWebElement element;

	public RichTextFieldWebElement(ConstellioWebElement element) {
		this.element = element;
		this.element.scrollIntoView();
	}
	
	public RichTextFieldWebElement setValue(String text) {
		clearField();
		getFrameComponent().sendKeys(text);
		return this;
	}

	public RichTextFieldWebElement clearField() {
		getFrameComponent().sendKeys(Keys.CONTROL + "a");
		getFrameComponent().sendKeys(Keys.DELETE);
		return this;
	}
	
	private ConstellioWebElement getFrameComponent() {
		element.waitUntilElementExist(By.className("gwt-RichTextArea"));
		return element.findElement(By.className("gwt-RichTextArea"));
	}

}
